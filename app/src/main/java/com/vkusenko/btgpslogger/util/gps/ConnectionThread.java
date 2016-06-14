package com.vkusenko.btgpslogger.util.gps;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;

public class ConnectionThread extends Thread {
    private final BluetoothSocket copyBtSocket;
    private final InputStream inputStream;
    private ParserNMEA parserNMEA;

    public ConnectionThread(BluetoothSocket socket){
        copyBtSocket = socket;
        InputStream tmpIn = null;
        parserNMEA = new ParserNMEA();
        try{
            tmpIn = socket.getInputStream();
        } catch (IOException e){}
        inputStream = tmpIn;
    }

    public void run()
    {
        byte[] buffer = new byte[320];
        int bytes;
        String strIncom;

        while(true){
            try{
                bytes = inputStream.read(buffer);
                strIncom= new String(buffer, 0, bytes);
                parserNMEA.bufferParser(strIncom);
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }catch(IOException e){break;}
        }
    }

    public void cancel(){
        try {
            copyBtSocket.close();
        }catch(IOException e){}
    }
}
