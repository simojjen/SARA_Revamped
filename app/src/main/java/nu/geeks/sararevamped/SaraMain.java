package nu.geeks.sararevamped;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class SaraMain extends Activity {

    private RelativeLayout root;
    private TouchThread touchThread;
    private SensorThread sensorThread;
    private BluetoothConnectionThread bluetoothConnectionThread;
    private CollectData collectData;
    private UIThread uiThread;

    private final int REQUESTCODE = 1234;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sara_main);

        uiThread = new UIThread();

        touchThread = new TouchThread(root, getWindowManager().getDefaultDisplay());
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

    private class UIThread extends Thread{

        //TODO - update UI in this thread.


        private UIThread() {

            root = ( RelativeLayout ) findViewById( R.id.root );


            /*
            cretate all views.


             */
        }

        @Override
        public void run() {


            /*
            while(1)
                get current rotation from sensorThread
                get current data from TouchThread
                update graphics on screen.


             */


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
