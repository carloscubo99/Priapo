package com.example.priapo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("EVENTO", "Se recibe el evento");
        if("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())){ //Condición que comprueba si el dispositivo ha sido reiniciado
            Log.i("ALARMA REINICIADO", "El dispositivo se ha reiniciado");
        }
    }
}
