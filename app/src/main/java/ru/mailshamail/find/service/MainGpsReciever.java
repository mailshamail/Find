package ru.mailshamail.find.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class MainGpsReciever extends BroadcastReceiver {

    public double shirota;
    public double dolgota;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        shirota = intent.getDoubleExtra("latitude", -1);
        dolgota = intent.getDoubleExtra("longitude", -1);


    }
}
