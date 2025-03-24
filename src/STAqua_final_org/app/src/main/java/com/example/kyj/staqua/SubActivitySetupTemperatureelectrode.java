package com.example.kyj.staqua;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;
import static com.example.kyj.staqua.MainActivity.FileLogWriter;
import static com.example.kyj.staqua.MainActivity.SendProtocol_Set;
import static com.example.kyj.staqua.MainActivity.m_nCurrentTempSensor;


/**
 * Created by KYJ on 2017-01-16.
 */
public class SubActivitySetupTemperatureelectrode extends Fragment {

    RadioButton rbTu;
    RadioButton rbCh;
    RadioButton rbPh;
    RadioButton rbCo;

    private SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_setup_temperatureelectrode, container, false);
        v.findViewById(R.id.Setup_TE_Option1_Apply).setOnClickListener(mClickListener);

        rbTu = (RadioButton) v.findViewById(R.id.Setup_TE_Sensor_Radio_TU);
        rbCh = (RadioButton) v.findViewById(R.id.Setup_TE_Sensor_Radio_CH);
        rbPh = (RadioButton) v.findViewById(R.id.Setup_TE_Sensor_Radio_PH);
        rbCo = (RadioButton) v.findViewById(R.id.Setup_TE_Sensor_Radio_CO);

        if (m_nCurrentTempSensor == 1) rbPh.setChecked(true);
        else if (m_nCurrentTempSensor == 2) rbCo.setChecked(true);
        else if (m_nCurrentTempSensor == 3) rbTu.setChecked(true);
        else if (m_nCurrentTempSensor == 4) rbCh.setChecked(true);

        return v;
    }



    public static SubActivitySetupTemperatureelectrode newInstance(String text) {
        SubActivitySetupTemperatureelectrode f = new SubActivitySetupTemperatureelectrode();
        Bundle b = new Bundle();
        b.putString("msg", text);
        f.setArguments(b);
        return f;
    }

    Button.OnClickListener mClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.Setup_TE_Option1_Apply:
                    // 센서 type , Sensor Filter
                    // 01 : pH
                    // 04 : Chlorine
                    // 03 : Turbidity
                    // 02 :   Contacting Conductivity
                    byte btCommand = 0x11;
                    byte[] btData = new byte[1];
                    if(rbTu.isChecked()){
                        m_nCurrentTempSensor = 3;
                        btCommand = 0x11;
                        btData[0] = 0x02; // 소모품 타입
                    }
                    if(rbCh.isChecked()){
                        m_nCurrentTempSensor = 4;
                        btCommand = 0x12;
                        btData[0] = 0x03; // 소모품 타입
                    }
                    if(rbPh.isChecked()){
                        m_nCurrentTempSensor = 1;
                        btCommand = 0x13;
                        btData[0] = 0x01; // 소모품 타입
                    }
                    if(rbCo.isChecked()) {
                        m_nCurrentTempSensor = 2;
                        btCommand = 0x14;
                        btData[0] = 0x00; // 소모품 타입
                    }

                    prefs = getActivity().getSharedPreferences("Setup_TE", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    String strCurrentSensor = Integer.toString(m_nCurrentTempSensor);
                    editor.putString("CurrentSensor", strCurrentSensor);
                    editor.commit();



                    if (!SendProtocol_Set( 2, (byte)0xC3, btData)){
                        Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    }


                    FileLogWriter(getString(R.string.multi_setup_temperatureelectrode),"Click Apply");
                    Toast.makeText(getActivity(), R.string.multi_applied, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    break;
                }
            }
    };
}
