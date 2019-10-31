package com.example.priapo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BootReceiver extends BroadcastReceiver {
    WifiManager wifiManager;
    WifiConfiguration wifiConfiguration;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("EVENTO", "Se recibe el evento");
        if("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())){ //Condición que comprueba si el dispositivo ha sido reiniciado
            Log.i("ALARMA REINICIADO", "El dispositivo se ha reiniciado");
            //Levanta el punto de acceso al reiniciar el dispositivo
            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(false);

            Method method = null;
            try {
                Log.e("--levantarAP", "AP a levantar");
                method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                method.invoke(wifiManager, wifiConfiguration, true);
                Log.i("--levantarAP", "AP levantado");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
