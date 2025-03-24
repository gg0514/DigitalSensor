package com.example.kyj.staqua;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivityTrendFlow extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_trend_flow, container, false);

//        TextView tv = (TextView) v.findViewById(R.id.tvFragSetupBluetooth);
//        tv.setText(getArguments().getString("msg"));

        return v;
    }

    public static SubActivityTrendFlow newInstance(String text) {

        SubActivityTrendFlow f = new SubActivityTrendFlow();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }
}
