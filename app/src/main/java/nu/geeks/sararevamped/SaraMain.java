package nu.geeks.sararevamped;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/* Programming by car:

oö-7k,.......,nbvm
,
'_.,mb
'vcx-ö.zl,<mk,,,,,z.-x*h-kjbvffbnm,
m,.

-i
kaow29gjghbvcnj


 */

public class SaraMain extends Activity {


    public static boolean DEBUG = false;

    private static final String TAG = "SaraMain::: ";

    private int DPI;

    private int steeringCorrectionAmount = 0;

    private char lastSentThrottleData = 0, lastSentSteeringData = 0;

    private TouchThread touchThread;
    private SensorObject sensorObject;
    private BluetoothConnectionThread bluetoothConnectionThread;

    private Typeface font;

    private Button bMenu;
    private RelativeLayout root;
    private ImageView ivPhone, ivGasPedal, ivVisare;
    private TextView debug, tConnected, tReceived,debug2;
    private SeekBar steeringCorrection;

    private String debugText = "";
    private String debugText2 = "";

    private String infoMessage = "" +
            "Applikationen SARA Revamped, samt den tillhörande bilen SARA,\n" +
            "är utvecklad av Hannes Paulsson, Mikael André, Simon Johansson och Ramón Rodriguez " +
            "under kursen Projekt och Projektmetoder (II1302) 2015 på KTH.\n" +
            "Läs mer om oss och vad vi håller på med nu på www.geeks.nu";

    private final int REQUESTCODE = 1234;

    private final byte STEERING = (byte) 200;
    private final byte THROTTLE = (byte) 201;
    private final byte STOPFUNCTION = (byte) 202;

    private char steering;
    private char throttle;


    private boolean isSteeringCorrectionVisible = false;
    private boolean isStopFunctionActive = true;
    private boolean lastSendStopFunctionData = true;

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
        ivVisare = ( ImageView ) findViewById( R.id.imVisare );
        steeringCorrection = ( SeekBar ) findViewById( R.id.seekSteering );
        debug2 = (TextView ) findViewById(R.id.tdebug2);

        steeringCorrection.setVisibility(View.INVISIBLE);



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
                final PopupMenu popupMenu = new PopupMenu(getApplicationContext(), v);
                final MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.menu_sara_main, popupMenu.getMenu());
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {


                        if (item.getItemId() == R.id.action_settings) {

                            onConnectSelected();

                        }

                        if( item.getItemId() == R.id.action_stop ){
                           isStopFunctionActive = !isStopFunctionActive;
                           if(!isStopFunctionActive) Toast.makeText(getApplicationContext(), "Auto stop function OFF", Toast.LENGTH_LONG).show();
                           else Toast.makeText(getApplicationContext(), "Auto stop function ON", Toast.LENGTH_LONG).show();

                        }

                        if (item.getItemId() == R.id.action_calibrate) {

                           isSteeringCorrectionVisible = !isSteeringCorrectionVisible;

                            if(isSteeringCorrectionVisible) steeringCorrection.setVisibility(View.VISIBLE);
                            else steeringCorrection.setVisibility(View.INVISIBLE);

                        }

                        if (item.getItemId() == R.id.action_info) {

                            new AlertDialog.Builder(SaraMain.this)
                                    .setTitle("Information")
                                    .setMessage(infoMessage)
                                    .setPositiveButton("Ok",null)
                                    .show();
                        }

                        return false;

                    }
                });
            }
        });

        steeringCorrection.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                steeringCorrectionAmount = progress - 50;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void updateBatteryStatus(){
        if(bluetoothConnectionThread.isBluetoothConnectionWorking()) {
           // Float readValue = (Float.parseFloat(bluetoothConnectionThread.getInput()) / 1023) * 5;

            if(!bluetoothConnectionThread.isAlive()){
                Log.d(TAG, "Restarting bluetooththread");
                bluetoothConnectionThread.start();
            }

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
            if(sb.length() == 0 ){
                for(char c : s.toCharArray()){
                    if(c != '@') sb.append(c);
                }
            }
            float batteryStatus = 0;

            try {

                batteryStatus = (Float.parseFloat(sb.toString())-2.45f)*46.51f; //Math == magic
                int correctedValue = (int) batteryStatus;
                if(batteryStatus >= 0) tReceived.setText(correctedValue + " %");
                else tReceived.setText("0 %");

            }catch (NumberFormatException nb){
                       //tReceived.setText("");
                      Log.d(TAG, "couldn't parse " + s + " SB is " + sb.toString());

            }

            float visarRotation = (batteryStatus * 2f) - 100;

            ivVisare.setRotation(visarRotation);
            debugText2 = sb.toString();

        }
    }

    private void updateGraphics() {


        if (touchThread != null) {

            float val = touchThread.getThrottleValue();
            final int throttleValue = (int) (114.24f - 0.38556f * val);
            final float px = (val - (25 * (DPI) / 160)) * (DPI / 160);


            //debug.setText("" + debugText);
            //debug2.setText("" + debugText2);
            ivGasPedal.setY(px);

        }


        // px = dp * (dpi / 160)
        //px * 160 = dp * dpi
        //dp = (px * 160) / dpi



    if(sensorObject !=null)

    {

        final float rawSensorValue = sensorObject.getRaw();


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
        sensorObject = new SensorObject(manager, getApplicationContext());

        restartBluetooth();

        CountDownTimer mainLoopTimer = new CountDownTimer(500, 10) {
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
                Log.d(TAG, "ResultCode OK from bluetooth");
                //bluetoothConnectionThread.bluetoothStartedOnPhone = true; //Not used at this time.
            } else {
                Toast.makeText(getApplicationContext(), "Bluetooth NOT actvated!", Toast.LENGTH_LONG).show();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void collectData() {


        if (sensorObject.getSensorValue() != steering) {

            int steeringCalc = (int) sensorObject.getSensorValue() + steeringCorrectionAmount;

            steering = (char) steeringCalc;
          //  debugText2 = "" + (int) steering;
            if(steering != lastSentSteeringData) {
                bluetoothConnectionThread.write(STEERING, (byte) steering);
            }
            lastSentSteeringData = steering;

        }

        //Only when stop functiondata is changed.
        if(lastSendStopFunctionData != isStopFunctionActive){

            if(isStopFunctionActive) bluetoothConnectionThread.write(STOPFUNCTION, (byte) 100);
            else bluetoothConnectionThread.write(STOPFUNCTION, (byte) 101);
            lastSendStopFunctionData = isStopFunctionActive;

        }

        if (touchThread.getThrottleValue() != throttle) {

            int temp = (int) (114.24f - 0.38556f * touchThread.getThrottleValue());


            if (temp >= 100) throttle = 100;
            else if (temp <= 0) throttle = 0;
            else throttle = (char) temp;
            //if(throttle != lastSentThrottleData) {
                bluetoothConnectionThread.write(THROTTLE, (byte) throttle);

            //}
            //lastSentThrottleData = throttle;
            debugText = "" + (int) throttle;

        }
    }
}
