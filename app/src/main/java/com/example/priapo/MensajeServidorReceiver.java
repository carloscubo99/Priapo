package com.example.priapo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class MensajeServidorReceiver extends BroadcastReceiver {

    private class TareaMensajeServidor extends AsyncTask<Void, Integer, Boolean> { //Tarea para poder realizar una consulta HTTP (en un hilo asíncrono)
        String email;

        public TareaMensajeServidor(String email){
            this.email = email;
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            URL url = null;
            try {
                url = new URL("http://virtual.lab.infor.uva.es:62052/~carloscubo/");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            //Clase para comunicarse con el servidor
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                // Construir los datos que se envian al servidor
                String data = "body=" + URLEncoder.encode(email,"UTF-8");

                urlConnection = (HttpURLConnection)url.openConnection();

                // Activa el método POST
                urlConnection.setDoOutput(true);

                // Establecer el tamaño previamente conocido de los datos a enviar
                urlConnection.setFixedLengthStreamingMode(data.getBytes().length);

                // Establecer el tipo de contenido a application/x-www-form-urlencoded. El método setRequestProperty permite añadir cabeceras a la petición
                urlConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

                //Se obtiene acceso al sistema de ficheros del servidor donde se van a escribir los datos
                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());

                out.write(data.getBytes());
                out.flush();
                out.close();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(urlConnection!=null)
                    urlConnection.disconnect();
            }
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
        }

        @Override
        protected void onCancelled() {
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("MENSAJESERVIDOR", "Va todo bien");
        SharedPreferences prefs = context.getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String email = prefs.getString("serverEmail", "serverEmail");
        new TareaMensajeServidor(email).execute();
    }

}
