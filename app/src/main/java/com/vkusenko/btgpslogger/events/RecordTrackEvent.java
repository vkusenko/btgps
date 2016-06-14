package com.vkusenko.btgpslogger.events;

import com.vkusenko.btgpslogger.util.logger.RecordTrack;

public class RecordTrackEvent {
    public final RecordTrack recordTrack;

    public RecordTrackEvent(RecordTrack recordTrack) {
        this.recordTrack = recordTrack;
    }
}
