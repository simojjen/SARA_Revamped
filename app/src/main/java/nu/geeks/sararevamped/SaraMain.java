package nu.geeks.sararevamped;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class SaraMain extends Activity {

    private static final String TAG = "SaraMain::: ";

    private int DPI;

    private Handler handler;

    private TouchThread touchThread;
    private SensorThread sensorThread;
    private BluetoothConnectionThread bluetoothConnectionThread;
    private CollectData collectData;

    private Thread uiThread;

    private Button bMenu;
    private RelativeLayout root;
    private ImageView ivPhone, ivGasPedal;
    private float yGasStartPX, yGasPedalUdpdate, yGasStartDP;
    private TextView debug, tConnected;

    private final int REQUESTCODE = 1234;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sara_main);

        handler = new Handler();


        root = ( RelativeLayout ) findViewById( R.id.root );
        ivPhone = ( ImageView ) findViewById( R.id.ivPhoneIcon );
        ivGasPedal = ( ImageView ) findViewById( R.id.ivGasPedal );
        debug = ( TextView ) findViewById( R.id.debug );
        tConnected = ( TextView ) findViewById( R.id.tConnected );
        bMenu = ( Button ) findViewById( R.id.bMenu );


        DisplayMetrics dpm = getResources().getDisplayMetrics();
        DPI = dpm.densityDpi;




        handleMenuButton();

    }

    private void handleMenuButton() {
        bMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getApplicationContext(), v);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.menu_sara_main, popupMenu.getMenu());
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {


                        if(item.getItemId() == R.id.action_settings){

                            onConnectSelected();

                        }

                        return false;

                    }
                });
            }
        });
    }

    private void updateGraphics(){
        if(touchThread != null){

            float val = touchThread.getThrottleValue();
            final int throttleValue = (int) (val);
            final float px = (val - ( 25 * (DPI) / 160)) * (DPI / 160);

            final float tmp = touchThread.getThrottleValue() * 0.27f;

            debug.post(new Runnable(){
               public void run(){
                   debug.setText("" + tmp);
                   ivGasPedal.setY(px);
               }
            });





            // px = dp * (dpi / 160)
            //px * 160 = dp * dpi
            //dp = (px * 160) / dpi

        }
        if(sensorThread != null){

            final float rawSensorValue = sensorThread.getRaw();

            ivPhone.post(new Runnable() {

                @Override
                public void run() {
                    ivPhone.setRotation(-rawSensorValue*9);
                }

            });

        }
        if(bluetoothConnectionThread != null){

            tConnected.post(new Runnable() {

                @Override
                public void run() {

                    if(bluetoothConnectionThread.bluetoothStartedOnPhone){
                        tConnected.setText("CONNECTED");
                        tConnected.setTextColor(Color.GREEN);
                    }else{
                        tConnected.setText("NOT CONNECTED");
                        tConnected.setTextColor(Color.RED);
                    }

                }

            });

        }
    }

    @Override
    protected void onResume() {
        super.onResume();


        touchThread = new TouchThread( root, DPI, yGasStartDP );

        SensorManager manager = ( SensorManager ) getSystemService( SENSOR_SERVICE );
        sensorThread = new SensorThread( manager, getApplicationContext( ) );
        //sensorThread.start();

        bluetoothConnectionThread = new BluetoothConnectionThread(this);
        //bluetoothConnectionThread.start();

        collectData = new CollectData();
        //collectData.start();

        uiThread = new Thread(new UIRunnable());



        if(!touchThread.isAlive()) {
            touchThread.start();
        }
        if(!sensorThread.isAlive()) {
            sensorThread.start();
        }
        if(!bluetoothConnectionThread.isAlive() ) {
            bluetoothConnectionThread.start();
        }
        if( ! collectData.isAlive()) {
            collectData.start();
        }
        if(!uiThread.isAlive()) {
            uiThread.start();
        }

        //Wait until all threads are started before allowing bluetooth to send data.
        bluetoothConnectionThread.setSendAllowed(true);

    }

    @Override
    protected void onPause() {

        //Turn off bluetooth before killing threads.
        bluetoothConnectionThread.setSendAllowed(false);

        collectData.interrupt();
        bluetoothConnectionThread.interrupt();
        sensorThread.interrupt();
        touchThread.interrupt();
        uiThread.interrupt();

        super.onPause();

    }


    public void onConnectSelected() {

            bluetoothConnectionThread.setSendAllowed(false);

            bluetoothConnectionThread.interrupt();
            bluetoothConnectionThread = null;

            bluetoothConnectionThread = new BluetoothConnectionThread(this);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUESTCODE){
            if(resultCode == RESULT_OK){
                //todo - skriva ut bluetooth i mobilen om vi har connection
                Log.d(TAG, "ResultCode OK from bluetooth");
                bluetoothConnectionThread.bluetoothStartedOnPhone = true; //Not used at this time.
            }else{
                Toast.makeText(getApplicationContext(), "Bluetooth NOT actvated!", Toast.LENGTH_LONG).show();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private class UIRunnable implements Runnable{
        @Override
        public void run() {

            while( !Thread.currentThread().isInterrupted() ) {
                updateGraphics();

            }
        }
    }


    private class CollectData extends Thread{

        private final byte STEERING = 2;
        private final byte THROTTLE = 1;

        private char steering;
        private char throttle;
        private char honk;
        private char safeMode;

        public CollectData(){

        }

        @Override
        public void run() {

            while ( ! Thread.currentThread().isInterrupted() ) {

                if (sensorThread.getSensorValue() != steering) {
                    steering = sensorThread.getSensorValue();
                    bluetoothConnectionThread.write(STEERING, (byte) steering);
                }else

                if (touchThread.getThrottleValue() != throttle) {
                    //throttle = touchThread.getThrottleValue();
                    //bluetoothConnectionThread.write( THROTTLE , ( byte ) throttle );

                }
            }

        }
    }
}
