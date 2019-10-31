package com.example.priapo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MensajeServidorReceiver extends BroadcastReceiver {

    private class TareaMensajeServidor extends AsyncTask<Void, Integer, Boolean> { //Tarea para enviar dirección email al servidor por medio de una petición HTTP
        String email;

        public TareaMensajeServidor(String email){
            this.email = email;
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            URL url = null;
            try {
                //URL a la cual va dirigida la petición
                url = new URL("http://virtual.lab.infor.uva.es:62052/~carloscubo/android/scriptEmail.php?email="+email);
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
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                String contenido = convertirInputStreamAString(in);
                Log.i("RESPUESTA SERVIDOR", contenido);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
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

    public String convertirInputStreamAString(InputStream in) throws Exception {
        Reader reader = null;
        reader = new InputStreamReader(in, "UTF-8");
        char[] buffer = new char[1024];
        StringBuffer bufferDatos = new StringBuffer();
        int contador;
        while((contador = reader.read(buffer)) != -1){
            bufferDatos.append(buffer, 0, contador);
        }
        return bufferDatos.toString();
    }

}
