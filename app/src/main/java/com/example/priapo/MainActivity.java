package com.example.priapo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static Context appContext;
    private String ssid; //Variable para almacenar el ssid de la red
    private PendingIntent pendingIntent;
    private AlarmManager manager;
    private WifiManager wifiManager;
    private WifiInfo wifiInfo;
    private WifiConfiguration wificonfiguration;
    private EditText correo;
    private String preferencia;

    private class MiTareaAsincrona extends AsyncTask<Void, Integer, Boolean> { //Tarea para poder realizar una consulta HTTP (en un hilo asíncrono)

        @Override
        protected Boolean doInBackground(Void... params) {
            //esto se lanza en el execute
            Log.e("--hilo", "doInBackGround->ESTOY EN OTRO HILO");
            //consultaHTTP(); //Se llevará a cabo la consulta HTTP en background ?
            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Toast.makeText(MainActivity.this, "Tarea finalizada!", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onCancelled() {

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appContext = getApplicationContext();

        //Ejecutar tarea en un hilo asíncrono
        new MiTareaAsincrona().execute();


        //Levantar Punto de Acceso para poder compartir conexión y dar permisos restringidos a la aplicación si no los tiene activados
        levantarAP();

        //Activar/Desactivar Wifi
        gestionarWifi();

        //Programar alarma
        programarAlarma();

        //Programar alarma para que salte cada aproximadamente 24 horas y que compruebe si el dispositivo se ha reinicado
        programarAlarma24H();

        //Programar alarma que salte cada aproximadamente 24 horas para enviar un mensaje a un servidor
        alarmaMensajeServidor();

        //Consultar estado batería
        consultarBateria();

        //Configurar SharedPreferences para almacenar de forma persistente el email al que enviar las notificaciones
        configurarSharedPreferences();

        //Iniciando una nueva activity
        empezarActividad2();

    }


    public void levantarAP(){
        Button bt = (Button) findViewById(R.id.button);
        bt.setText("Levantar AP");
        bt.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.M)
            public void onClick(View v){
                boolean settingsCanWrite = Settings.System.canWrite(appContext);
                if(!settingsCanWrite) {
                    //Si la aplicación no tiene permisos se abre una ventana de configuración de permisos en aplicaciones
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS); //En general, Intent sirve para abrir una ventana, para empezar una nueva actividad
                    startActivity(intent);
                }else {
                    //Si la aplicación tiene ya los permisos restringidos entonces muestra un mensaje de alerta con el mensaje indicado
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setMessage("You have system write settings permission now.");
                    alertDialog.show();

                    wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                    wifiManager.setWifiEnabled(false);

                    Method method = null;
                    try {
                        Log.e("--levantarAP", "AP a levantar");
                        method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                        method.invoke(wifiManager, wificonfiguration, true);
                        Log.i("--levantarAP", "AP levantado"); //No llega aquí, ¿salta una excepción?
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void gestionarWifi(){
        Button bt2 = (Button) findViewById(R.id.button2);
        bt2.setText("Activar/Desactivar Wifi");
        bt2.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED){ //Wifi conectado
                    wifiManager.setWifiEnabled(false);
                }
                else{ //Wifi no conectado
                    wifiManager.setWifiEnabled(true);
                }

            }
        });
    }

    public void programarAlarma(){
        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);

        manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 8000;
        //El método setInexactRepeating consume menos que su método homólogo setRepeating. Con este método la alarma no saltará en el momento explícitamente indicado sino aproximadamente
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent); //AlarmManager.ELAPSED_REALTIME_WAKEUP se utiliza para despertar al dispositivo un tiempo después de que se haya iniciado ???
    }

    public void programarAlarma24H(){
        Intent intent = new Intent(MainActivity.this, BootReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);

        manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 1000*60*1440; //Intervalo de tiempo equivalente a 24 horas
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent); //AlarmManager.ELAPSED_REALTIME_WAKEUP se utiliza para despertar al dispositivo un tiempo después de que se haya iniciado ???
    }

    public void alarmaMensajeServidor(){
        Intent intent = new Intent(MainActivity.this, MensajeServidorReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);

        manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 1000*60*1440; //Intervalo de tiempo equivalente a 24 horas
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent); //AlarmManager.ELAPSED_REALTIME_WAKEUP se utiliza para despertar al dispositivo un tiempo después de que se haya iniciado ???

    }

    public void consultarBateria(){
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = appContext.registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float)scale;

        batteryPct*=100;
        int nivel;
        nivel = Math.round(batteryPct);
        String res = "Nivel de batería del dispositivo: "+nivel+"%";
        TextView txt2 = (TextView) findViewById(R.id.textView2);
        txt2.setText(res);

        //Comprobando si el nivel de batería es bajo
        if(nivel < 5){
            Log.i("--bateriaBaja", "BATERÍA BAJA");
            Toast bateriaBaja = Toast.makeText(getApplicationContext(), "BATERÍA BAJA", Toast.LENGTH_SHORT);
            bateriaBaja.show();
        }

        //Comprobando si el dispositivo se está cargando y qué tipo de carga está recibiendo
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        if(isCharging){ //El dispositivo se está cargando o está cargado
            Toast cargando = Toast.makeText(getApplicationContext(), "Dispositivo Cargando/Cargado", Toast.LENGTH_SHORT);
            cargando.show();
            //Ver cómo se está cargando el dispositivo
            int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

            if(usbCharge){
                Toast usb = Toast.makeText(getApplicationContext(), "El dispositivo está conectado por un usb", Toast.LENGTH_SHORT);
                usb.show();
            }
            if(acCharge){
                Toast ac = Toast.makeText(getApplicationContext(), "El dispositivo está conectado a la corriente", Toast.LENGTH_SHORT);
                ac.show();
            }
        }
        else{
            Toast noCargando = Toast.makeText(getApplicationContext(), "El dispositivo no se está cargando", Toast.LENGTH_SHORT);
            noCargando.show();
        }
    }

    public void configurarSharedPreferences(){
        correo = (EditText) findViewById(R.id.correo);
        correo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { //Cuando el texto cambia se actualiza el valor de la SharedPreference
                preferencia = correo.getText().toString();
                SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
                //Con el editor de abajo almaceno la SharedPreference con el nombre serverEmail
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("serverEmail", preferencia);
                editor.commit();
                String email = prefs.getString("serverEmail", "serverEmail");
                TextView txt = (TextView) findViewById(R.id.textView);
                txt.setText("Email a utilizar: " + email);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String email = prefs.getString("serverEmail", "serverEmail");
        TextView txt = (TextView) findViewById(R.id.textView);
        txt.setText(email);
    }

    public void empezarActividad2(){
        Intent i = new Intent(this, Main2Activity.class);
        startActivity(i);
    }

    public void comprobarEstadoWifi(){
        TextView txt = (TextView) findViewById(R.id.textView);
        appContext = getApplicationContext();
        WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo;
        wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
            ssid = wifiInfo.getSSID();
            Log.e("WIFI", wifiInfo.getSSID());
            Toast.makeText(MainActivity.this, "Sí estoy conectado a la wifi y el ssid de la red wifi es: " + ssid, Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(MainActivity.this, "No estoy conectado a la wifi", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onResume(){
        super.onResume();
        //Método para conocer el estado de la wifi (si el dispositivo está o no conectado a una wifi e informar del ssid de la red wifi)
        //Este método se ejecutará cada vez que esta Activity vuelve a primer plano
        comprobarEstadoWifi();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        manager.cancel(pendingIntent); //Para cancelar la alarma una vez la aplicación se destruya (Estas alarmas pueden seguir funcionando aunque la aplicación se haya destruido) ¿No funciona?
    }

    public void consultaHTTP(){
        //GET request
        URL url = null;
        try {
            url = new URL("http://www.android.com/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                String res = readStream(in);
                Log.e("--respuestaServidor", res);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            urlConnection.disconnect();
        }

    }

    public String readStream(InputStream is) { //Método para pasar la respuesta del tipo InputStream a String
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while(i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }


}
