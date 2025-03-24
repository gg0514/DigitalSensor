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

import java.text.DecimalFormat;

import static android.content.Context.MODE_PRIVATE;
import static com.example.kyj.staqua.MainActivity.FileLogWriter;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;


/**
 * Created by KYJ on 2017-01-16.
 */
public class SubActivitySetupDatafilter extends Fragment {

    EditText edFilter;
    EditText edFilterDeviation;
    EditText edFilterWindow;
    EditText edRangeHigh;
    EditText edRangeLow;

    RadioButton rbTu;
    RadioButton rbCh;
    RadioButton rbPh;
    RadioButton rbCo;


    private SharedPreferences prefs;

    private String m_strFilter = "";
    private String m_strFilterDeviation = "";
    private String m_strFilterWindow = "";
    private String m_strRangeHigh = "";
    private String m_strRangeLow = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_setup_datafilter, container, false);

        edFilter = (EditText)v.findViewById(R.id.Setup_DF_Filter_Ed);
        edFilterDeviation = (EditText)v.findViewById(R.id.Setup_DF_Filter_Deviation_Ed);
        edFilterWindow = (EditText)v.findViewById(R.id.Setup_DF_Filter_Window_Ed);
        edRangeHigh = (EditText)v.findViewById(R.id.Setup_DF_Range_High_Ed);
        edRangeLow = (EditText)v.findViewById(R.id.Setup_DF_Range_Low_Ed);

        v.findViewById(R.id.Setup_DF_Filter_Btn_Up).setOnClickListener(mClickListener);
        v.findViewById(R.id.Setup_DF_Filter_Btn_Down).setOnClickListener(mClickListener);
        v.findViewById(R.id.Setup_DF_Filter_Deviation_Btn_Up).setOnClickListener(mClickListener);
        v.findViewById(R.id.Setup_DF_Filter_Deviation_Btn_Down).setOnClickListener(mClickListener);
        v.findViewById(R.id.Setup_DF_Filter_Window_Btn_Up).setOnClickListener(mClickListener);
        v.findViewById(R.id.Setup_DF_Filter_Window_Btn_Down).setOnClickListener(mClickListener);
        v.findViewById(R.id.Setup_DF_Range_High_Btn_Up).setOnClickListener(mClickListener);
        v.findViewById(R.id.Setup_DF_Range_High_Btn_Down).setOnClickListener(mClickListener);
        v.findViewById(R.id.Setup_DF_Range_Low_Btn_Up).setOnClickListener(mClickListener);
        v.findViewById(R.id.Setup_DF_Range_Low_Btn_Down).setOnClickListener(mClickListener);

        v.findViewById(R.id.Setup_DF_Option1_FactoryReset).setOnClickListener(mClickListener);
        v.findViewById(R.id.Setup_DF_Option1_Apply).setOnClickListener(mClickListener);

        rbTu = (RadioButton) v.findViewById(R.id.Setup_DF_Sensor_Radio_TU);
        rbCh = (RadioButton) v.findViewById(R.id.Setup_DF_Sensor_Radio_CH);
        rbPh = (RadioButton) v.findViewById(R.id.Setup_DF_Sensor_Radio_PH);
        rbCo = (RadioButton) v.findViewById(R.id.Setup_DF_Sensor_Radio_CO);
        rbTu.setOnClickListener(mrgClickListener);
        rbCh.setOnClickListener(mrgClickListener);
        rbPh.setOnClickListener(mrgClickListener);
        rbCo.setOnClickListener(mrgClickListener);
        rbTu.setChecked(true);
        setRadioDisplay(3);

        return v;
    }



    public static SubActivitySetupDatafilter newInstance(String text) {
        SubActivitySetupDatafilter f = new SubActivitySetupDatafilter();
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
            if(rbTu.isChecked()) nID = 3;
            if(rbCh.isChecked()) nID = 4;
            if(rbPh.isChecked()) nID = 1;
            if(rbCo.isChecked()) nID = 2;
            setRadioDisplay(nID);
        }
    };

    private void setRadioDisplay(int nID) {

        // 센서 type , Sensor Filter
        // 01 : pH
        // 04 : Chlorine
        // 03 : Turbidity
        // 02 :   Contacting Conductivity
        if (nID == 1) prefs = getActivity().getSharedPreferences("Ph_DataFilter", MODE_PRIVATE);
        else if (nID == 2) prefs = getActivity().getSharedPreferences("Co_DataFilter", MODE_PRIVATE);
        else if (nID == 3) prefs = getActivity().getSharedPreferences("Tu_DataFilter", MODE_PRIVATE);
        else if (nID == 4) prefs = getActivity().getSharedPreferences("Ch_DataFilter", MODE_PRIVATE);
        m_strFilter = prefs.getString("Filter", "");
        if (m_strFilter != "") edFilter.setText(m_strFilter);
        else edFilter.setText("30");
        m_strFilterDeviation = prefs.getString("Filter_Deviation", "");
        if (m_strFilterDeviation != "") edFilterDeviation.setText(m_strFilterDeviation);
        else edFilterDeviation.setText("10");
        m_strFilterWindow = prefs.getString("Filter_Window", "");
        if (m_strFilterWindow != "") edFilterWindow.setText(m_strFilterWindow);
        else edFilterWindow.setText("5");
        m_strRangeHigh = prefs.getString("Range_High", "");
        if (m_strRangeHigh != "") edRangeHigh.setText(m_strRangeHigh);
        else edRangeHigh.setText("10");
        m_strRangeLow = prefs.getString("Range_Low", "");
        if (m_strRangeLow != "") edRangeLow.setText(m_strRangeLow);
        else edRangeLow.setText("0");
    }
    private void SavePreferences(int nID) {
        if (nID == 1) prefs = getActivity().getSharedPreferences("Ph_DataFilter", MODE_PRIVATE);
        else if (nID == 2) prefs = getActivity().getSharedPreferences("Co_DataFilter", MODE_PRIVATE);
        else if (nID == 3) prefs = getActivity().getSharedPreferences("Tu_DataFilter", MODE_PRIVATE);
        else if (nID == 4) prefs = getActivity().getSharedPreferences("Ch_DataFilter", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("Filter", m_strFilter);
        editor.putString("Filter_Deviation", m_strFilterDeviation);
        editor.putString("Filter_Window", m_strFilterWindow);
        editor.putString("Range_High", m_strRangeHigh);
        editor.putString("Range_Low", m_strRangeLow);
        editor.commit();
    }



    Button.OnClickListener mClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            String strTemp;
            Double dbCalibration = 0.0d;
            Integer nCalibration = 0;
            DecimalFormat df2Figure = new DecimalFormat("0.00");
            int nID = 0;
            switch (v.getId()) {
                case R.id.Setup_DF_Filter_Btn_Up:
                    strTemp = edFilter.getText().toString();
                    if(strTemp.length() == 0) strTemp = "0";
                    nCalibration = parseInt(strTemp);
                    nCalibration = nCalibration + 1;
                    edFilter.setText(Integer.toString(nCalibration));
                    break;
                case R.id.Setup_DF_Filter_Btn_Down:
                    strTemp = edFilter.getText().toString();
                    if(strTemp.length() == 0) strTemp = "0";
                    nCalibration = parseInt(strTemp);
                    nCalibration = nCalibration - 1;
                    edFilter.setText(Integer.toString(nCalibration));
                    break;
                case R.id.Setup_DF_Filter_Deviation_Btn_Up:
                    strTemp = edFilterDeviation.getText().toString();
                    if(strTemp.length() == 0) strTemp = "0.00";
                    dbCalibration = parseDouble(strTemp);
                    dbCalibration = dbCalibration + 0.1;
                    dbCalibration = parseDouble(df2Figure.format(dbCalibration));
                    edFilterDeviation.setText(Double.toString(dbCalibration));
                    break;
                case R.id.Setup_DF_Filter_Deviation_Btn_Down:
                    strTemp = edFilterDeviation.getText().toString();
                    if(strTemp.length() == 0) strTemp = "0.00";
                    dbCalibration = parseDouble(strTemp);
                    dbCalibration = dbCalibration - 0.1;
                    dbCalibration = parseDouble(df2Figure.format(dbCalibration));
                    edFilterDeviation.setText(Double.toString(dbCalibration));
                    break;
                case R.id.Setup_DF_Filter_Window_Btn_Up:
                    strTemp = edFilterWindow.getText().toString();
                    if(strTemp.length() == 0) strTemp = "0";
                    nCalibration = parseInt(strTemp);
                    nCalibration = nCalibration + 1;
                    edFilterWindow.setText(Integer.toString(nCalibration));
                    break;
                case R.id.Setup_DF_Filter_Window_Btn_Down:
                    strTemp = edFilterWindow.getText().toString();
                    if(strTemp.length() == 0) strTemp = "0";
                    nCalibration = parseInt(strTemp);
                    nCalibration = nCalibration - 1;
                    edFilterWindow.setText(Integer.toString(nCalibration));
                    break;
                case R.id.Setup_DF_Range_High_Btn_Up:
                    strTemp = edRangeHigh.getText().toString();
                    if(strTemp.length() == 0) strTemp = "0.00";
                    dbCalibration = parseDouble(strTemp);
                    dbCalibration = dbCalibration + 0.1;
                    dbCalibration = parseDouble(df2Figure.format(dbCalibration));
                    edRangeHigh.setText(Double.toString(dbCalibration));
                    break;
                case R.id.Setup_DF_Range_High_Btn_Down:
                    strTemp = edRangeHigh.getText().toString();
                    if(strTemp.length() == 0) strTemp = "0.00";
                    dbCalibration = parseDouble(strTemp);
                    dbCalibration = dbCalibration - 0.1;
                    dbCalibration = parseDouble(df2Figure.format(dbCalibration));
                    edRangeHigh.setText(Double.toString(dbCalibration));
                    break;
                case R.id.Setup_DF_Range_Low_Btn_Up:
                    strTemp = edRangeLow.getText().toString();
                    if(strTemp.length() == 0) strTemp = "0.00";
                    dbCalibration = parseDouble(strTemp);
                    dbCalibration = dbCalibration + 0.1;
                    dbCalibration = parseDouble(df2Figure.format(dbCalibration));
                    edRangeLow.setText(Double.toString(dbCalibration));
                    break;
                case R.id.Setup_DF_Range_Low_Btn_Down:
                    strTemp = edRangeLow.getText().toString();
                    if(strTemp.length() == 0) strTemp = "0.00";
                    dbCalibration = parseDouble(strTemp);
                    dbCalibration = dbCalibration - 0.1;
                    dbCalibration = parseDouble(df2Figure.format(dbCalibration));
                    edRangeLow.setText(Double.toString(dbCalibration));
                    break;
                case R.id.Setup_DF_Option1_Apply:
                    m_strFilter = edFilter.getText().toString();
                    m_strFilterDeviation = edFilterDeviation.getText().toString();
                    m_strFilterWindow = edFilterWindow.getText().toString();
                    m_strRangeHigh = edRangeHigh.getText().toString();
                    m_strRangeLow = edRangeLow.getText().toString();

                    // 센서 type , Sensor Filter
                    // 01 : pH
                    // 04 : Chlorine
                    // 03 : Turbidity
                    // 02 :   Contacting Conductivity
                    if(rbTu.isChecked()) nID = 3;
                    if(rbCh.isChecked()) nID = 4;
                    if(rbPh.isChecked()) nID = 1;
                    if(rbCo.isChecked()) nID = 2;
                    SavePreferences(nID);
              //      SendProtocol((byte)0x45, 15, String.valueOf(nID), m_strFilter, m_strFilterDeviation, m_strFilterWindow, m_strRangeHigh,m_strRangeLow, m_strMode);
                //    SendProtocol((byte)0x11, 1, "0", m_strMode);
                    FileLogWriter(getString(R.string.multi_setup_datafilter),"Click Apply");
                    Toast.makeText(getActivity(), R.string.multi_applied, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    break;
                case R.id.Setup_DF_Option1_FactoryReset:
                    m_strFilter = "30";
                    m_strFilterDeviation = "10";
                    m_strFilterWindow = "5";
                    m_strRangeHigh = "10";
                    m_strRangeLow = "0";
                    edFilter.setText(m_strFilter);
                    edFilterDeviation.setText(m_strFilterDeviation);
                    edFilterWindow.setText(m_strFilterWindow);
                    edRangeHigh.setText(m_strRangeHigh);
                    edRangeLow.setText(m_strRangeLow);
                    // 센서 type , Sensor Filter
                    // 01 : pH
                    // 04 : Chlorine
                    // 03 : Turbidity
                    // 02 :   Contacting Conductivity
                    if(rbTu.isChecked()) nID = 3;
                    if(rbCh.isChecked()) nID = 4;
                    if(rbPh.isChecked()) nID = 1;
                    if(rbCo.isChecked()) nID = 2;
                    SavePreferences(nID);
                    FileLogWriter(getString(R.string.multi_setup_datafilter),"Click Factory Reset");
                }
            }
    };
}
