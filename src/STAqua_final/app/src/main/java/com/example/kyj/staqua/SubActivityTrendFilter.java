package com.example.kyj.staqua;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;
import static com.example.kyj.staqua.MainActivity.FileLogWriter;
import static com.example.kyj.staqua.MainActivity.m_dTrendConstant;
import static com.example.kyj.staqua.MainActivity.m_nTrendCount;
import static com.example.kyj.staqua.MainActivity.m_nTrendFilter;


/**
 * Created by KYJ on 2017-01-16.
 */
public class SubActivityTrendFilter extends Fragment {

    EditText edTrendCount;
    EditText edTrendConstant;

    RadioButton rbOriginal;
    RadioButton rbMovingAverage;
    RadioButton rbConfidenceLevel;


    private SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_trend_filter, container, false);

        edTrendCount = (EditText)v.findViewById(R.id.Trend_Filter_Count_Ed);
        edTrendConstant = (EditText)v.findViewById(R.id.Trend_Filter_Constant_Ed);

        rbOriginal = (RadioButton) v.findViewById(R.id.Trend_Filter_Radio_Original);
        rbMovingAverage = (RadioButton) v.findViewById(R.id.Trend_Filter_Radio_MA);
        rbConfidenceLevel = (RadioButton) v.findViewById(R.id.Trend_Filter_Radio_CL);

        v.findViewById(R.id.Trend_Filter_Option1_Apply).setOnClickListener(mClickListener);
        rbOriginal.setOnClickListener(mrgClickListener);
        rbMovingAverage.setOnClickListener(mrgClickListener);
        rbConfidenceLevel.setOnClickListener(mrgClickListener);

        if (m_nTrendFilter == 1) rbOriginal.setChecked(true);
        else if (m_nTrendFilter == 2) rbMovingAverage.setChecked(true);
        else if (m_nTrendFilter == 3) rbConfidenceLevel.setChecked(true);
        setRadioDisplay(m_nTrendFilter);

        edTrendCount.setText(String.valueOf(m_nTrendCount));
        edTrendConstant.setText(String.valueOf(m_dTrendConstant));

        return v;
    }


    public static SubActivityTrendFilter newInstance(String text) {
        SubActivityTrendFilter f = new SubActivityTrendFilter();
        Bundle b = new Bundle();
        b.putString("msg", text);
        f.setArguments(b);
        return f;
    }

    RadioButton.OnClickListener mrgClickListener  = new RadioButton.OnClickListener() {
        public void onClick(View v) {
            int nID = 0;
            if(rbOriginal.isChecked()) nID = 1;
            if(rbMovingAverage.isChecked()) nID = 2;
            if(rbConfidenceLevel.isChecked()) nID = 3;
            setRadioDisplay(nID);
        }
    };

    private void setRadioDisplay(int nID) {
        if (nID == 1) {
            edTrendCount.setVisibility(View.INVISIBLE);
            edTrendConstant.setVisibility(View.INVISIBLE);
        }
        else if (nID ==2) {
            edTrendCount.setVisibility(View.VISIBLE);
            edTrendConstant.setVisibility(View.INVISIBLE);
        }
        else if (nID ==3) {
            edTrendCount.setVisibility(View.VISIBLE);
            edTrendConstant.setVisibility(View.VISIBLE);
        }

    }


    Button.OnClickListener mClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.Trend_Filter_Option1_Apply:
                    prefs = getActivity().getSharedPreferences("Trend_Filter", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();

                    int nFilter= 1;
                    if(rbOriginal.isChecked()) nFilter = 1;
                    else if(rbMovingAverage.isChecked()) nFilter = 2;
                    else if(rbConfidenceLevel.isChecked()) nFilter = 3;
                    m_nTrendFilter = nFilter;
                    m_nTrendCount = Integer.parseInt(edTrendCount.getText().toString());
                    m_dTrendConstant = Double.parseDouble(edTrendConstant.getText().toString());
                    editor.putString("Filter", String.valueOf(nFilter));
                    editor.putString("TrendCount", edTrendCount.getText().toString());
                    editor.putString("TrendConstant", edTrendConstant.getText().toString());
                    editor.commit();

                    FileLogWriter(getString(R.string.multi_trend_filter),"Click Apply");
                    Toast.makeText(getActivity(), R.string.multi_applied, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    break;
            }
            }
    };
}
