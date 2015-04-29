package nu.geeks.sararevamped;

import android.graphics.Point;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Simon on 4/28/2015.
 */
public class TouchThread extends Thread {

    RelativeLayout root;


    Display display;
    int screenHeight;
    char sendValue;


    public TouchThread(RelativeLayout root, Display display){
        this.root = root;
        this.display = display;


        sendValue = 0;
    }

    public char getThrottleValue(){
        return sendValue;
    }

    @Override
    public void run() {
                Point size = new Point();
                display.getSize(size);
                screenHeight = size.y;

                root.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                public boolean onTouch(View v, MotionEvent event) {


                    float screenPercent = (event.getY() / screenHeight) * 100f;

                        float value = ((70-screenPercent) * 1.5f);

                        if ( value < 0) sendValue = 0;

                        else if( value > 100) sendValue = 100;

                        else sendValue = (char) value;

                    return true;
                }
            });



    }
}
