package com.example.kyj.staqua;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.DecimalFormat;

import static android.content.Context.MODE_PRIVATE;
import static com.example.kyj.staqua.MainActivity.m_dMvTu;

/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivityDiagnoTurbidity extends Fragment {

    private EditText edPrevious;
    private EditText edMeasured;

    private String m_strPrevious;
    private String m_strMeasured;

    SharedPreferences prefs;
    DecimalFormat df3Figure = new DecimalFormat("000.000");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_diagno_turbiditydiagnosis, container, false);

        edPrevious = (EditText)v.findViewById(R.id.Diagno_TD_Previous_Ed);
        edMeasured = (EditText)v.findViewById(R.id.Diagno_TD_Measured_Ed);

        v.findViewById(R.id.Diagno_TD_Measured_Meas).setOnClickListener(mClickListener);
        v.findViewById(R.id.Diagno_TD_Option1_Registration).setOnClickListener(mClickListener);

        prefs = getActivity().getSharedPreferences("Diag_Tu", MODE_PRIVATE);
        m_strPrevious = prefs.getString("Previous", "");
        if (m_strPrevious == "") edPrevious.setText("0.000");
        else edPrevious.setText(m_strPrevious);

        return v;
    }




    public static SubActivityDiagnoTurbidity newInstance(String text) {

        SubActivityDiagnoTurbidity f = new SubActivityDiagnoTurbidity();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    Button.OnClickListener mClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            SharedPreferences.Editor editor = prefs.edit();
            switch (v.getId()) {
                case R.id.Diagno_TD_Measured_Meas:
             //       SendProtocol((byte)0x52, 1, "0", m_strMode);
             //       SendProtocol((byte)0x13, 1, "0", m_strMode);
            //        SendProtocol((byte)0x04, 1, "0", m_strMode);
            //        SendProtocol((byte)0x51, 1, "0", m_strMode);
             //       SendProtocol((byte)0x11, 1, "0", m_strMode);

                    String strMv =Double.toString(m_dMvTu);
                    edMeasured.setText(strMv);

                    break;
                case R.id.Diagno_TD_Option1_Registration:
                    m_strMeasured = edMeasured.getText().toString();
                    m_strPrevious = m_strMeasured;
                    edPrevious.setText(m_strPrevious);
                    edMeasured.setText("");
                    editor.putString("Previous", m_strMeasured);
                    editor.commit();
                    Toast.makeText(getActivity(), R.string.multi_applied, Toast.LENGTH_SHORT).show();
                    break;


//
//                beforeText = edPrevious.getText().toString();
//                if(beforeText.length() == 0) beforeText = "0.000";
//                dbCalibration = parseDouble(beforeText);
//                dbCalibration = parseDouble(df3Figure.format(dbCalibration));
//                edPrevious.setText(Double.toString(dbCalibration));

            }
        }
    };
}
