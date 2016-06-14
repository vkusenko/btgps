package com.vkusenko.btgpslogger.events;

import android.bluetooth.BluetoothSocket;

public class BtSocketEvent {
    public final BluetoothSocket btSocket;

    public BtSocketEvent(BluetoothSocket btSocket) {
        this.btSocket = btSocket;
    }
}
