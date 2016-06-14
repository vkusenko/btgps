package com.vkusenko.btgpslogger.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.vkusenko.btgpslogger.util.gps.ConnectionThread;
import com.vkusenko.btgpslogger.events.BtSocketEvent;

import org.greenrobot.eventbus.EventBus;

public class BtGpsService extends Service {

    private ConnectionThread connectionThread;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        BtSocketEvent btSocketEvent = EventBus.getDefault().removeStickyEvent(BtSocketEvent.class);
        if (btSocketEvent != null) {
            connectionThread = new ConnectionThread(btSocketEvent.btSocket);
            connectionThread.start();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        connectionThread.cancel();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
