package nu.geeks.sararevamped;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
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


    public static boolean DEBUG = true;

    private static final String TAG = "SaraMain::: ";

    private int DPI;


    private TouchThread touchThread;
    private SensorThread sensorThread;
    private BluetoothConnectionThread bluetoothConnectionThread;

    private Typeface font;

    private Button bMenu;
    private RelativeLayout root;
    private ImageView ivPhone, ivGasPedal;
    private TextView debug, tConnected, tReceived;

    private String debugText = "";

    private final int REQUESTCODE = 1234;

    private final byte STEERING = 2;
    private final byte THROTTLE = 1;

    private char steering;
    private char throttle;
    private char honk;
    private char safeMode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sara_main);

        root = (RelativeLayout) findViewById(R.id.root);
        ivPhone = (ImageView) findViewById(R.id.ivPhoneIcon);
        ivGasPedal = (ImageView) findViewById(R.id.ivGasPedal);
        debug = (TextView) findViewById(R.id.debug);
        tConnected = (TextView) findViewById(R.id.tConnected);
        bMenu = (Button) findViewById(R.id.bMenu);
        tReceived = (TextView) findViewById(R.id.tRecieved);

        font = Typeface.createFromAsset(getAssets(), "dispfont.ttf");
        tReceived.setTypeface(font);
        tConnected.setTypeface(font);

        DisplayMetrics dpm = getResources().getDisplayMetrics();
        DPI = dpm.densityDpi;

        if (!DEBUG) debug.setVisibility(View.INVISIBLE);


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


                        if (item.getItemId() == R.id.action_settings) {

                            onConnectSelected();

                        }

                        return false;

                    }
                });
            }
        });
    }

    private void updateBatteryStatus(){
        if(bluetoothConnectionThread.isBluetoothConnectionWorking()) {
           // Float readValue = (Float.parseFloat(bluetoothConnectionThread.getInput()) / 1023) * 5;

            String s = bluetoothConnectionThread.getInput();
            StringBuilder sb = new StringBuilder();

            boolean start = false;
            for(char c : s.toCharArray()){
                if(c == '#') start = true;
                if(start){
                    if(c == '@') break;
                    if(c != '#') sb.append(c);
                }
            }
            float batteryStatus = 0;

            try {
                batteryStatus = Float.parseFloat(sb.toString()) * 20.0f;

                tReceived.setText(batteryStatus + " %");
            }catch (NumberFormatException nb){

                      Log.d(TAG, "couldn't parse float.");

            }


        }
    }

    private void updateGraphics() {
        if (touchThread != null) {

            float val = touchThread.getThrottleValue();
            final int throttleValue = (int) (114.24f - 0.38556f * val);
            final float px = (val - (25 * (DPI) / 160)) * (DPI / 160);


            debug.setText("" + debugText);
            ivGasPedal.setY(px);



            if (bluetoothConnectionThread.isBluetoothConnectionWorking()) {
                tConnected.setText("Connected");
                tConnected.setTextColor(Color.GREEN);
            } else {
                tConnected.setText("Not connected");
                tConnected.setTextColor(Color.RED);
            }
        }


        // px = dp * (dpi / 160)
        //px * 160 = dp * dpi
        //dp = (px * 160) / dpi



    if(sensorThread!=null)

    {

        final float rawSensorValue = sensorThread.getRaw();


                ivPhone.setRotation(-rawSensorValue * 9);
            }




    if(bluetoothConnectionThread!=null)

    {


                if (bluetoothConnectionThread.isBluetoothConnectionWorking()) {
                    tConnected.setText("CONNECTED");
                    tConnected.setTextColor(Color.GREEN);
                } else {
                    tConnected.setText("NOT CONNECTED");
                    tConnected.setTextColor(Color.RED);
                }

            }



    }



    @Override
    protected void onResume() {
        super.onResume();


        touchThread = new TouchThread(root, DPI);

        SensorManager manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorThread = new SensorThread(manager, getApplicationContext());

        restartBluetooth();

        CountDownTimer mainLoopTimer = new CountDownTimer(1000, 1) {
            @Override
            public void onTick(long millisUntilFinished) {
                mainLoop();
            }

            @Override
            public void onFinish() {
                updateBatteryStatus();
                start();
            }
        }.start();

        Log.d(TAG, "onResume");

        //Wait until all threads are started before allowing bluetooth to send data.
        bluetoothConnectionThread.setSendAllowed(true);

    }


    private void mainLoop() {
        updateGraphics();
        collectData();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart");
        super.onRestart();
    }

    @Override
    protected void onPause() {

        //Turn off bluetooth
        bluetoothConnectionThread.setSendAllowed(false);

        bluetoothConnectionThread.interrupt(); //Doesn't do anything at the moment, run-loop is empty.

        bluetoothConnectionThread.kill();

        Log.d(TAG, "onPause");


        super.onPause();
        // super.onDestroy();

    }


    public void onConnectSelected() {

        //TODO - snygga till knappen, antingen lägga till en till för disconnect eller ba slasha

        restartBluetooth();
    }

    public void restartBluetooth(){

        if(bluetoothConnectionThread != null) {
            bluetoothConnectionThread.setSendAllowed(false);

            bluetoothConnectionThread.interrupt();
            bluetoothConnectionThread.kill();
        }
        bluetoothConnectionThread = null;

        bluetoothConnectionThread = new BluetoothConnectionThread(this);
        if(!bluetoothConnectionThread.isAlive()) bluetoothConnectionThread.start();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUESTCODE) {
            if (resultCode == RESULT_OK) {

                //todo - skriva ut bluetooth i mobilen om vi har connection

                Log.d(TAG, "ResultCode OK from bluetooth");
                //bluetoothConnectionThread.bluetoothStartedOnPhone = true; //Not used at this time.
            } else {
                Toast.makeText(getApplicationContext(), "Bluetooth NOT actvated!", Toast.LENGTH_LONG).show();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void collectData() {


        if (sensorThread.getSensorValue() != steering) {
            steering = sensorThread.getSensorValue();
            bluetoothConnectionThread.write(STEERING, (byte) steering);
        } else if (touchThread.getThrottleValue() != throttle) {

            int temp = (int) (114.24f - 0.38556f * touchThread.getThrottleValue());


            if (temp >= 100) throttle = 100;
            else if (temp <= 0) throttle = 0;
            else throttle = (char) temp;

            bluetoothConnectionThread.write(THROTTLE, (byte) throttle);
            debugText = "" + (int) throttle;

        }
    }
}
