package nu.geeks.sararevamped;

import android.app.Activity;
import android.app.Notification;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
    private static String HC06Adress;
    private BluetoothDevice hc06;
    private final int REQUESTCODE = 1234;
    private final String TAG = "btConThread";
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final InputStream inStream;
    private final OutputStream outStream;
    private boolean bluetoothConnectionWorking;
    private String inputString;

    public boolean sendAllowed;

    public BluetoothConnectionThread(Activity activity) {
        this.activity = activity;

        inputString = "null";


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            //Bluetooth is not supported by device.
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
            //suser is not paired with HC-06
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
    }

    public void setSendAllowed(boolean sendAllowed){
        this.sendAllowed = sendAllowed;
    }

    public boolean isBluetoothConnectionWorking(){
        return bluetoothConnectionWorking;
    }

    @Override
    public void run() {

        byte[] buffer = new byte[256];
        int bytes;

        // Keep looping to listen for received messages
        while (true) {
            try {
                bytes = inStream.read(buffer);            //read bytes from input buffer

                String readMessage = new String(buffer, 0, bytes);

                // Send the obtained bytes to the UI Activity via handler

                inputString = readMessage;

              //  Log.d(TAG, inputString);
            } catch (IOException e) {
                Log.d(TAG, "breaked");
                break;
            }
        }
    }

    public String getInput(){
        return inputString;
    }


    public void kill(){
        bluetoothConnectionWorking = false;
        sendAllowed = false;
        try {
            inStream.close();
            Log.d(TAG, "inputStrem DID close");
        } catch (IOException e) {
            Log.d(TAG, "inputStrem didn't close");
            e.printStackTrace();
        }
        try {
            outStream.close();
            Log.d(TAG, "outputStrem DID close");
        } catch (IOException e) {
            Log.d(TAG, "outputStrem didn't close");
            e.printStackTrace();
        }
        try {
            btSocket.close();
            Log.d(TAG, "socket DID close");
        } catch (IOException e) {
            Log.d(TAG, "socket didn't close");
            e.printStackTrace();
        }

        //btSocket = null;
        //bluetoothAdapter = null;


    }


    public void write(byte key, byte value) {

        char dist = 0;


        byte[] msgBuffer = {(byte) 253, key, (byte) value, (byte) 254};
        boolean sent = true;


        if (sendAllowed) {


            try {
                outStream.write(msgBuffer);                //write bytes over BT connection via outstream
                bluetoothConnectionWorking = true;
                outStream.flush();


            } catch (IOException e) {
                bluetoothConnectionWorking = false;
            }
        }

        try {
            outStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
