package nu.geeks.sararevamped;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
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


    private RelativeLayout                  root;
    private ImageView                       ivPhone, ivGasPedal;
    private float                           yGasStartPX, yGasPedalUdpdate, yGasStartDP;
    private TextView debug;

    private final int REQUESTCODE = 1234;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sara_main);

        handler = new Handler();


        root = (RelativeLayout) findViewById( R.id.root );
        ivPhone = (ImageView) findViewById( R.id.ivPhoneIcon );
        ivGasPedal = (ImageView) findViewById( R.id.ivGasPedal );
        debug = (TextView) findViewById(R.id.debug);

        DisplayMetrics dpm = getResources().getDisplayMetrics();
        DPI = dpm.densityDpi;


        touchThread = new TouchThread(root, DPI, yGasStartDP);
        //touchThread.start();

        SensorManager manager = (SensorManager)getSystemService( SENSOR_SERVICE );
        sensorThread = new SensorThread( manager, getApplicationContext( ) );
        //sensorThread.start();

        bluetoothConnectionThread = new BluetoothConnectionThread(this);
        //bluetoothConnectionThread.start();

        collectData = new CollectData();
        //collectData.start();

        uiThread = new Thread(new UIRunnable());



    }

    private void updateGraphics(){


        if(touchThread != null){

            float val = touchThread.getThrottleValue();
            final int throttleValue = (int) (((80 - val * 0.162f)-30)*1.3f);
            final float px = (val - ( 25 * (DPI) / 160)) * (DPI / 160);


            debug.post(new Runnable(){
               public void run(){
                   debug.setText("" + throttleValue);
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
    }

    @Override
    protected void onResume() {
        super.onResume();

        touchThread.start();
        sensorThread.start();
        bluetoothConnectionThread.start();
        collectData.start();
        uiThread.start();

    }

    @Override
    protected void onPause() {

        collectData.interrupt();
        bluetoothConnectionThread.interrupt();
        sensorThread.interrupt();
        touchThread.interrupt();
        uiThread.interrupt();

        super.onPause();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater().inflate(R.menu.menu_sara_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //TODO - add menu buttons?
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUESTCODE){
            if(resultCode == RESULT_OK){
                //todo - skriva ut bluetooth i mobilen om vi har connection
                Log.d(TAG, "ResultCode OK from bluetooth");
                bluetoothConnectionThread.bluetoothStartedOnPhone = true; //Not used at this time.
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private class UIRunnable implements Runnable{
        @Override
        public void run() {
            while( true ) {
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

            while ( !this.isInterrupted() ) {

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
