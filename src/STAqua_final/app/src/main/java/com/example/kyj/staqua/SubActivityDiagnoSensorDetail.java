package com.example.kyj.staqua;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

/**
 * Created by KYJ on 2017-12-15.
 */

public class SubActivityDiagnoSensorDetail extends Activity  {
    protected  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.sub_activity_diagno_sensordiagnosis_detail);

    }
}
