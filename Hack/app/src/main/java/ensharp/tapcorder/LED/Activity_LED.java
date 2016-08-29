package ensharp.tapcorder.LED;

import android.os.Bundle;

import ensharp.tapcorder.BT_Preference;
import ensharp.tapcorder.R;

/**
 * Created by user on 2016-08-29.
 */
public class Activity_LED extends BT_Preference {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led);
    }
}
