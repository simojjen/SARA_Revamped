package nu.geeks.sararevamped;

import android.app.Activity;
import android.app.Notification;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Simon on 4/28/2015.
 */
public class BluetoothConnectionThread extends Thread {
    private Context context;
    private Activity activity;
    private BluetoothSocket btSocket;
    private BluetoothAdapter bluetoothAdapter;
    public boolean bluetoothStartedOnPhone = false;
    private static String HC06Adress;
    private BluetoothDevice hc06;
    private final int REQUESTCODE = 1234;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final InputStream inStream;
    private final OutputStream outStream;

    public boolean sendAllowed;

    public BluetoothConnectionThread(Activity activity) {
        this.activity = activity;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            //Todo - signal to user that bt is not supported by device
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent bt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(bt, REQUESTCODE);
        }
       // while (!bluetoothStartedOnPhone) {
            //wait on OK
            //TODO - this might be an error
        // }


        Set<BluetoothDevice> pariedDevices = bluetoothAdapter.getBondedDevices();
        boolean foundHC06 = false;
        for (BluetoothDevice bd : pariedDevices) {
            if (bd.getName().equals("HC-06")) {
                foundHC06 = true;
                hc06 = bd;
                HC06Adress = bd.getAddress();
            }
        }
        if (!foundHC06) {
            //TODO - user is not paired with HC-06
            inStream = null;
            outStream = null;

        } else {

            try {
                btSocket = hc06.createInsecureRfcommSocketToServiceRecord(BTMODULEUUID);
            } catch (IOException e) {
                e.printStackTrace();
                //Socket creation failed
            }
            try {
                btSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    btSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    //DUNNO
                }
            }

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = btSocket.getInputStream();
                tmpOut = btSocket.getOutputStream();

            } catch (IOException e) {

            }

            inStream = tmpIn;
            outStream = tmpOut;
            sendAllowed = true;
        }

        //TODO - constructor
    }

    @Override
    public void run() {
        //TODO - read bluetooth?
    }


    public void write(byte key, byte value) {

        char dist = 0;


        byte[] msgBuffer = {key, (byte) value};
        boolean sent = true;


        if (sendAllowed) {


            try {
                outStream.write(msgBuffer);                //write bytes over BT connection via outstream

                //mmOutStream.flush();

            } catch (IOException e) {

            }
        }
    }
}
