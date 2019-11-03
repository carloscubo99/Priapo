package com.example.priapo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver { //BroadcastReceiver para captar una alarma de prueba programada en un Intent

    @Override
    public void onReceive(Context context, Intent intent) { //Este método se ejecutará cada vez que se produzca el evento al que está suscrito este broadcast receiver
        Log.i("ALARMA", "RIIINNG!!!");
        Toast.makeText(context, "The alarm is running", Toast.LENGTH_SHORT).show();
    }
}
