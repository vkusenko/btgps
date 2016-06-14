package com.vkusenko.btgpslogger.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.vkusenko.btgpslogger.events.RecordTrackEvent;
import com.vkusenko.btgpslogger.util.logger.RecordTrack;

import org.greenrobot.eventbus.EventBus;

public class RecordTrackService extends Service {

    private RecordTrack recordTrack;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        RecordTrackEvent recordTrackEvent = EventBus.getDefault().removeStickyEvent(RecordTrackEvent.class);
        if (recordTrackEvent != null) {
            recordTrack = recordTrackEvent.recordTrack;
            recordTrack.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        recordTrack.finish();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
