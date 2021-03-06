package nu.geeks.sararevamped;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by Simon on 4/28/2015.
 */
public class SensorObject implements SensorEventListener{

    private SensorManager manager;
    private Context context;
    private float rawValue;
    private char sendValue;

    public SensorObject(SensorManager manager, Context context) {
        this.manager = manager;
        this.context = context;
        sendValue = 0;
        manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), manager.SENSOR_DELAY_UI);

    }



    @Override
    public void onSensorChanged(SensorEvent event) {
        //this is math, dont question it
        rawValue = event.values[1];
        float ut = -rawValue*2f;
        sendValue = (char) (100-(ut*1.6f));


    }

    public char getSensorValue(){
    return sendValue;

    }

    public float getRaw(){
        return rawValue;
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
