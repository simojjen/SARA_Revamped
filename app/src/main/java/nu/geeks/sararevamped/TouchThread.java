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
public class TouchThread {

    final int DPI;
    float yStartDP;
    float pxHalfButtonSize;
    float middle = 165;

    RelativeLayout root;

    float sendValue;


    public TouchThread(RelativeLayout root, final int DPI, float yStartDP){
        this.root = root;
        this.DPI = DPI;
        this.yStartDP = yStartDP;
        pxHalfButtonSize = 50 * (DPI / 160);
        sendValue = middle;


        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                sendValue = (160 * event.getY()) / DPI;
                // px = dp * (dpi / 160)
                //px * 160 = dp * dpi
                //dp = (px * 160) / dpi

                if(event.getAction() == MotionEvent.ACTION_UP){
                    sendValue = middle;
                }

                return true;
            }
        });

    }

    public float getThrottleValue(){
        return sendValue;
    }

}
