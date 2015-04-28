package nu.geeks.sararevamped;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class SaraMain extends Activity {

    private RelativeLayout root;
    private TextView debug;
    private int screenHeight;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private TouchThread touchThread;
    private SensorThread sensorThread;
    private BluetoothConnectionThread bluetoothConnectionThread;
    private CollectData collectData;

    private final int REQUESTCODE = 1234;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sara_main);

        root = ( RelativeLayout ) findViewById( R.id.root );
        debug= ( TextView ) findViewById( R.id.DebugText );

        touchThread = new TouchThread(root, getWindowManager().getDefaultDisplay(), debug);
        touchThread.start();

        SensorManager manager = (SensorManager)getSystemService( SENSOR_SERVICE );
        sensorThread = new SensorThread( manager, getApplicationContext( ) );
        sensorThread.start();

        bluetoothConnectionThread = new BluetoothConnectionThread(this);
        bluetoothConnectionThread.start();

        collectData = new CollectData();
        collectData.start();


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
                bluetoothConnectionThread.bluetoothStartedOnPhone = true;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
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

            while ( true ) {
                if(sensorThread.getSensorValue() != steering) {
                    steering = sensorThread.getSensorValue();
                    bluetoothConnectionThread.write ( STEERING , ( byte ) steering );
                }

                if(touchThread.getThrottleValue() != throttle) {
                    throttle = touchThread.getThrottleValue();
                    bluetoothConnectionThread.write( THROTTLE , ( byte ) throttle );

                }
            }
        }
    }
}
