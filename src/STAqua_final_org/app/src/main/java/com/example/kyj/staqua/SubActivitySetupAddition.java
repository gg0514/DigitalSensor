package com.example.kyj.staqua;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;

import java.text.DecimalFormat;

import static android.content.Context.MODE_PRIVATE;
import static com.example.kyj.staqua.MainActivity.m_bDisplayErrorCheckCh;
import static com.example.kyj.staqua.MainActivity.m_bDisplayErrorCheckCo;
import static com.example.kyj.staqua.MainActivity.m_bDisplayErrorCheckPh;
import static com.example.kyj.staqua.MainActivity.m_bDisplayErrorCheckTemp;
import static com.example.kyj.staqua.MainActivity.m_bDisplayErrorCheckTu;


/**
 * Created by KYJ on 2017-01-16.
 */
public class SubActivitySetupAddition extends Fragment {


    CheckBox checkTu;
    CheckBox checkCh;
    CheckBox checkPh;
    CheckBox checkCo;
    CheckBox checkTemp;

    CheckBox checkTuLight;


    private SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_setup_addition, container, false);


        v.findViewById(R.id.Setup_Add_Option1_FactoryReset).setOnClickListener(mClickListener);
        v.findViewById(R.id.Setup_Add_Option1_Apply).setOnClickListener(mClickListener);

        checkTu = (CheckBox) v.findViewById(R.id.Setup_Add_Sensor_Check_TU);
        checkCh = (CheckBox) v.findViewById(R.id.Setup_Add_Sensor_Check_CH);
        checkPh = (CheckBox) v.findViewById(R.id.Setup_Add_Sensor_Check_PH);
        checkCo = (CheckBox) v.findViewById(R.id.Setup_Add_Sensor_Check_CO);
        checkTemp = (CheckBox) v.findViewById(R.id.Setup_Add_Sensor_Check_TEMP);
        checkTuLight = (CheckBox) v.findViewById(R.id.Setup_Add_Tu_Light_Check);

        if (m_bDisplayErrorCheckTu)  checkTu.setChecked(true);
        else checkTu.setChecked(false);
        if (m_bDisplayErrorCheckCh)  checkCh.setChecked(true);
        else checkCh.setChecked(false);
        if (m_bDisplayErrorCheckPh)  checkPh.setChecked(true);
        else checkPh.setChecked(false);
        if (m_bDisplayErrorCheckCo)  checkCo.setChecked(true);
        else checkCo.setChecked(false);
        if (m_bDisplayErrorCheckTemp)  checkTemp.setChecked(true);
        else checkTemp.setChecked(false);

        checkTu.setChecked(true);
        return v;
    }

    public static SubActivitySetupAddition newInstance(String text) {
        SubActivitySetupAddition f = new SubActivitySetupAddition();
        Bundle b = new Bundle();
        b.putString("msg", text);
        f.setArguments(b);
        return f;
    }

    RadioButton.OnClickListener mrgClickListener  = new RadioButton.OnClickListener() {
        public void onClick(View v) {
            int nID = 0;
            // 센서 type , Sensor Filter
            // 01 : pH
            // 04 : Chlorine
            // 03 : Turbidity
            // 02 :   Contacting Conductivity
            setRadioDisplay(nID);
        }
    };

    private void setRadioDisplay(int nID) {

        // 센서 type , Sensor Filter
        // 01 : pH
        // 04 : Chlorine
        // 03 : Turbidity
        // 02 :   Contacting Conductivity
//        if (nID == 1) prefs = getActivity().getSharedPreferences("Ph_DataFilter", MODE_PRIVATE);
//        else if (nID == 2) prefs = getActivity().getSharedPreferences("Co_DataFilter", MODE_PRIVATE);
//        else if (nID == 3) prefs = getActivity().getSharedPreferences("Tu_DataFilter", MODE_PRIVATE);
//        else if (nID == 4) prefs = getActivity().getSharedPreferences("Ch_DataFilter", MODE_PRIVATE);
//        m_strFilter = prefs.getString("Filter", "");
//        if (m_strFilter != "") edFilter.setText(m_strFilter);
//        else edFilter.setText("30");
//        m_strFilterDeviation = prefs.getString("Filter_Deviation", "");
//        if (m_strFilterDeviation != "") edFilterDeviation.setText(m_strFilterDeviation);
//        else edFilterDeviation.setText("10");
//        m_strFilterWindow = prefs.getString("Filter_Window", "");
//        if (m_strFilterWindow != "") edFilterWindow.setText(m_strFilterWindow);
//        else edFilterWindow.setText("5");
//        m_strRangeHigh = prefs.getString("Range_High", "");
//        if (m_strRangeHigh != "") edRangeHigh.setText(m_strRangeHigh);
//        else edRangeHigh.setText("10");
//        m_strRangeLow = prefs.getString("Range_Low", "");
//        if (m_strRangeLow != "") edRangeLow.setText(m_strRangeLow);
//        else edRangeLow.setText("0");
    }
    private void SavePreferences(int nID) {
//        if (nID == 1) prefs = getActivity().getSharedPreferences("Ph_DataFilter", MODE_PRIVATE);
//        else if (nID == 2) prefs = getActivity().getSharedPreferences("Co_DataFilter", MODE_PRIVATE);
//        else if (nID == 3) prefs = getActivity().getSharedPreferences("Tu_DataFilter", MODE_PRIVATE);
//        else if (nID == 4) prefs = getActivity().getSharedPreferences("Ch_DataFilter", MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString("Filter", m_strFilter);
//        editor.putString("Filter_Deviation", m_strFilterDeviation);
//        editor.putString("Filter_Window", m_strFilterWindow);
//        editor.putString("Range_High", m_strRangeHigh);
//        editor.putString("Range_Low", m_strRangeLow);
//        editor.commit();
    }



    Button.OnClickListener mClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            String strTemp;
            Double dbCalibration = 0.0d;
            Integer nCalibration = 0;
            DecimalFormat df2Figure = new DecimalFormat("0.00");
            int nID = 0;
            switch (v.getId()) {
                case R.id.Setup_Add_Option1_Apply:

                    SharedPreferences prefs;
                    prefs = getActivity().getSharedPreferences("ErrorDisplay", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();

                    if (checkTu.isChecked())  m_bDisplayErrorCheckTu = true;
                    else m_bDisplayErrorCheckTu = true;
                    if (checkCh.isChecked())  m_bDisplayErrorCheckCh = true;
                    else m_bDisplayErrorCheckCh = true;
                    if (checkPh.isChecked())  m_bDisplayErrorCheckPh = true;
                    else m_bDisplayErrorCheckPh = true;
                    if (checkCo.isChecked())  m_bDisplayErrorCheckCo = true;
                    else m_bDisplayErrorCheckCo = true;
                    if (checkTemp.isChecked())  m_bDisplayErrorCheckTemp = true;
                    else m_bDisplayErrorCheckTemp = true;

                    editor.putBoolean("CheckTu", m_bDisplayErrorCheckTu);
                    editor.putBoolean("CheckCh", m_bDisplayErrorCheckCh);
                    editor.putBoolean("CheckPh", m_bDisplayErrorCheckPh);
                    editor.putBoolean("CheckCo", m_bDisplayErrorCheckCo);
                    editor.putBoolean("CheckTemp", m_bDisplayErrorCheckTemp);
                    editor.commit();




                    break;
                case R.id.Setup_Add_Option1_FactoryReset:
                    break;
            }
        }
    };
}
