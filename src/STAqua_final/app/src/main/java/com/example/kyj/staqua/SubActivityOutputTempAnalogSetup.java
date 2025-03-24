package com.example.kyj.staqua;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.DecimalFormat;

import static android.content.Context.MODE_PRIVATE;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivityOutputTempAnalogSetup extends Fragment {

    private EditText edRange4mAChange;
    private EditText edRange20mAChange;
    private EditText edAnalog4mAChange;
    private EditText edAnalog20mAChange;
    private EditText edRange4mACurrent;
    private EditText edRange20mACurrent;
    private EditText edAnalog4mACurrent;
    private EditText edAnalog20mACurrent;

    private DecimalFormat df2Figure = new DecimalFormat("0.00");

    private SharedPreferences prefs;

    private String m_strRange4mA = "";
    private String m_strRange20mA = "";
    private String m_strAnalog4mA = "";
    private String m_strAnalog20mA = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_output_temp_analogsetup, container, false);

        edRange4mAChange = (EditText)v.findViewById(R.id.Output_Temp_AS_RangeSetting_4ma_Ed_Change);
        edRange4mACurrent = (EditText)v.findViewById(R.id.Output_Temp_AS_RangeSetting_4ma_Ed_Current);
        edRange20mAChange = (EditText)v.findViewById(R.id.Output_Temp_AS_RangeSetting_20ma_Ed_Change);
        edRange20mACurrent = (EditText)v.findViewById(R.id.Output_Temp_AS_RangeSetting_20ma_Ed_Current);
        edAnalog4mAChange = (EditText)v.findViewById(R.id.Output_Temp_AS_AnalogOutput_4ma_Ed_Change);
        edAnalog4mACurrent = (EditText)v.findViewById(R.id.Output_Temp_AS_AnalogOutput_4ma_Ed_Current);
        edAnalog20mAChange = (EditText)v.findViewById(R.id.Output_Temp_AS_AnalogOutput_20ma_Ed_Change);
        edAnalog20mACurrent = (EditText)v.findViewById(R.id.Output_Temp_AS_AnalogOutput_20ma_Ed_Current);
        edRange4mAChange.setOnFocusChangeListener(meClickListener);
        edRange20mAChange.setOnFocusChangeListener(meClickListener);
        edAnalog4mAChange.setOnFocusChangeListener(meClickListener);
        edAnalog20mAChange.setOnFocusChangeListener(meClickListener);

        AnalogTextWatcher atRange4mAWatcher = new AnalogTextWatcher(edRange4mAChange);
        edRange4mAChange.addTextChangedListener(atRange4mAWatcher);
        AnalogTextWatcher atRange20mAWatcher = new AnalogTextWatcher(edRange20mAChange);
        edRange20mAChange.addTextChangedListener(atRange20mAWatcher);
        AnalogTextWatcher atAnalog4mAWatcher = new AnalogTextWatcher(edAnalog4mAChange);
        edAnalog4mAChange.addTextChangedListener(atAnalog4mAWatcher);
        AnalogTextWatcher atAnalog20mAWatcher = new AnalogTextWatcher(edAnalog20mAChange);
        edAnalog20mAChange.addTextChangedListener(atAnalog20mAWatcher);

        v.findViewById(R.id.Output_Temp_AS_Main).setOnClickListener(mvClickListener);
        v.findViewById(R.id.Output_Temp_AS_RangeSetting_4ma_Btn_Up).setOnClickListener(mClickListener);
        v.findViewById(R.id.Output_Temp_AS_RangeSetting_4ma_Btn_Down).setOnClickListener(mClickListener);
        v.findViewById(R.id.Output_Temp_AS_RangeSetting_20ma_Btn_Up).setOnClickListener(mClickListener);
        v.findViewById(R.id.Output_Temp_AS_RangeSetting_20ma_Btn_Down).setOnClickListener(mClickListener);
        v.findViewById(R.id.Output_Temp_AS_AnalogOutput_4ma_Btn_Up).setOnClickListener(mClickListener);
        v.findViewById(R.id.Output_Temp_AS_AnalogOutput_4ma_Btn_Down).setOnClickListener(mClickListener);
        v.findViewById(R.id.Output_Temp_AS_AnalogOutput_20ma_Btn_Up).setOnClickListener(mClickListener);
        v.findViewById(R.id.Output_Temp_AS_AnalogOutput_20ma_Btn_Down).setOnClickListener(mClickListener);

        v.findViewById(R.id.Output_Temp_AS_Option1_Hold).setOnClickListener(mClickListener);
        v.findViewById(R.id.Output_Temp_AS_Option2_Apply).setOnClickListener(mClickListener);
        v.findViewById(R.id.Output_Temp_AS_Option2_FactoryReset).setOnClickListener(mClickListener);


        prefs = getActivity().getSharedPreferences("Temp_Analog", MODE_PRIVATE);
        m_strRange4mA = prefs.getString("Range_4mA", "");
        if (m_strRange4mA != ""){
            edRange4mACurrent.setText(m_strRange4mA);
            edRange4mAChange.setText(m_strRange4mA);
        }
        m_strRange20mA = prefs.getString("Range_20mA", "");
        if (m_strRange20mA != ""){
            edRange20mACurrent.setText(m_strRange20mA);
            edRange20mAChange.setText(m_strRange20mA);
        }
        m_strAnalog4mA = prefs.getString("Analog_4mA", "");
        if (m_strAnalog4mA != ""){
            edAnalog4mACurrent.setText(m_strAnalog4mA);
            edAnalog4mAChange.setText(m_strAnalog4mA);
        }
        m_strAnalog20mA = prefs.getString("Analog_20mA", "");
        if (m_strAnalog20mA != ""){
            edAnalog20mACurrent.setText(m_strAnalog20mA);
            edAnalog20mAChange.setText(m_strAnalog20mA);
        }

        return v;
    }

    public static SubActivityOutputTempAnalogSetup newInstance(String text) {

        SubActivityOutputTempAnalogSetup f = new SubActivityOutputTempAnalogSetup();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    public static boolean isStringDouble(String s) {
        try {
            parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    EditText.OnFocusChangeListener meClickListener  = new View.OnFocusChangeListener(){
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus)
            {
                switch(v.getId()){
                    case R.id.Output_Temp_AS_RangeSetting_4ma_Ed_Change:
                        edRange4mAChange.setText("");
                        break;
                    case R.id.Output_Temp_AS_RangeSetting_20ma_Ed_Change:
                        edRange20mAChange.setText("");
                        break;
                    case R.id.Output_Temp_AS_AnalogOutput_4ma_Ed_Change:
                        edAnalog4mAChange.setText("");
                        break;
                    case R.id.Output_Temp_AS_AnalogOutput_20ma_Ed_Change:
                        edAnalog20mAChange.setText("");
                        break;
                }
            }
        }
    };

    View.OnClickListener mvClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.Output_Temp_AS_Main:
                    InputMethodManager imm =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    break;
            }
        }
    };


    Button.OnClickListener mClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            String beforeText;
            Double dbCalibration = 0.0d;
            Integer nCalibration = 0;
            SharedPreferences.Editor editor = prefs.edit();
            switch (v.getId()) {
                case R.id.Output_Temp_AS_RangeSetting_4ma_Btn_Up:
                    beforeText = edRange4mAChange.getText().toString();
                    if(beforeText.length() == 0) beforeText = "0.00";
                    dbCalibration = parseDouble(beforeText);
                    dbCalibration = dbCalibration + 0.01;
                    dbCalibration = parseDouble(df2Figure.format(dbCalibration));
                    edRange4mAChange.setText(Double.toString(dbCalibration));
                    break;
                case R.id.Output_Temp_AS_RangeSetting_4ma_Btn_Down:
                    beforeText = edRange4mAChange.getText().toString();
                    if(beforeText.length() == 0) beforeText = "0.00";
                    dbCalibration = parseDouble(beforeText);
                    dbCalibration = dbCalibration - 0.01;
                    dbCalibration = parseDouble(df2Figure.format(dbCalibration));
                    edRange4mAChange.setText(Double.toString(dbCalibration));
                    break;
                case R.id.Output_Temp_AS_RangeSetting_20ma_Btn_Up:
                    beforeText = edRange20mAChange.getText().toString();
                    if(beforeText.length() == 0) beforeText = "0.00";
                    dbCalibration = parseDouble(beforeText);
                    dbCalibration = dbCalibration + 0.01;
                    dbCalibration = parseDouble(df2Figure.format(dbCalibration));
                    edRange20mAChange.setText(Double.toString(dbCalibration));
                    break;
                case R.id.Output_Temp_AS_RangeSetting_20ma_Btn_Down:
                    beforeText = edRange20mAChange.getText().toString();
                    if(beforeText.length() == 0) beforeText = "0.00";
                    dbCalibration = parseDouble(beforeText);
                    dbCalibration = dbCalibration - 0.01;
                    dbCalibration = parseDouble(df2Figure.format(dbCalibration));
                    edRange20mAChange.setText(Double.toString(dbCalibration));
                    break;
                case R.id.Output_Temp_AS_AnalogOutput_4ma_Btn_Up:
                    beforeText = edAnalog4mAChange.getText().toString();
                    if(beforeText.length() == 0) beforeText = "0";
                    nCalibration = parseInt(beforeText);
                    nCalibration = nCalibration + 1;
                    edAnalog4mAChange.setText(Integer.toString(nCalibration));
                    break;
                case R.id.Output_Temp_AS_AnalogOutput_4ma_Btn_Down:
                    beforeText = edAnalog4mAChange.getText().toString();
                    if(beforeText.length() == 0) beforeText = "0";
                    nCalibration = parseInt(beforeText);
                    nCalibration = nCalibration - 1;
                    edAnalog4mAChange.setText(Integer.toString(nCalibration));
                    break;
                case R.id.Output_Temp_AS_AnalogOutput_20ma_Btn_Up:
                    beforeText = edAnalog20mAChange.getText().toString();
                    if(beforeText.length() == 0) beforeText = "0";
                    nCalibration = parseInt(beforeText);
                    nCalibration = nCalibration + 1;
                    edAnalog20mAChange.setText(Integer.toString(nCalibration));
                    break;
                case R.id.Output_Temp_AS_AnalogOutput_20ma_Btn_Down:
                    beforeText = edAnalog20mAChange.getText().toString();
                    if(beforeText.length() == 0) beforeText = "0";
                    nCalibration = parseInt(beforeText);
                    nCalibration = nCalibration - 1;
                    edAnalog20mAChange.setText(Integer.toString(nCalibration));
                    break;
                case R.id.Output_Temp_AS_Option2_Apply:
                    m_strRange4mA = edRange4mAChange.getText().toString();
                    m_strRange20mA = edRange20mAChange.getText().toString();
                    m_strAnalog4mA = edAnalog4mAChange.getText().toString();
                    m_strAnalog20mA = edAnalog20mAChange.getText().toString();
                    edRange4mACurrent.setText(m_strRange4mA);
                    edRange20mACurrent.setText(m_strRange20mA);
                    edAnalog4mACurrent.setText(m_strAnalog4mA);
                    edAnalog20mACurrent.setText(m_strAnalog20mA);
                    editor.putString("Range_4mA", m_strRange4mA);
                    editor.putString("Range_20mA", m_strRange20mA);
                    editor.putString("Analog_4mA", m_strAnalog4mA);
                    editor.putString("Analog_20mA", m_strAnalog20mA);
                    editor.commit();
                    Toast.makeText(getActivity(), R.string.multi_applied, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    break;
                case R.id.Output_Temp_AS_Option2_FactoryReset:
                    m_strRange4mA = "0.00";
                    m_strRange20mA = "14.00";
                    m_strAnalog4mA = "4800";
                    m_strAnalog20mA = "24200";
                    edRange4mACurrent.setText(m_strRange4mA);
                    edRange4mAChange.setText(m_strRange4mA);
                    edRange20mACurrent.setText(m_strRange20mA);
                    edRange20mAChange.setText(m_strRange20mA);
                    edAnalog4mACurrent.setText(m_strAnalog4mA);
                    edAnalog4mAChange.setText(m_strAnalog4mA);
                    edAnalog20mACurrent.setText(m_strAnalog20mA);
                    edAnalog20mAChange.setText(m_strAnalog20mA);
                    editor.putString("Range_4mA", m_strRange4mA);
                    editor.putString("Range_20mA", m_strRange20mA);
                    editor.putString("Analog_4mA", m_strAnalog4mA);
                    editor.putString("Analog_20mA", m_strAnalog20mA);
                    editor.commit();
                    break;
            }
        }
    };

    private class AnalogTextWatcher implements TextWatcher {
        EditText et;
        String beforeText;
        public AnalogTextWatcher(EditText et){
            this.et = et;
        }
        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            if (!isStringDouble(s.toString())) return;
            if(s.toString().length() > 0){
                if ((et.getId() == R.id.Output_Temp_AS_RangeSetting_4ma_Ed_Change) || (et.getId() == R.id.Output_Temp_AS_RangeSetting_20ma_Ed_Change))
                {
                    if((parseDouble(s.toString()) > 14) || (parseDouble(s.toString()) < 0)){
                        et.setText(beforeText);
                        Toast.makeText(getActivity(), R.string.multi_temp_analog_validation, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    }
                }
                else if ((et.getId() == R.id.Output_Temp_AS_AnalogOutput_4ma_Ed_Change) || (et.getId() == R.id.Output_Temp_AS_AnalogOutput_20ma_Ed_Change))
                {
                    if((parseInt(s.toString()) > 65535) || (parseInt(s.toString()) < 0)){
                        et.setText(beforeText);
                        Toast.makeText(getActivity(), R.string.multi_analog_output_validation, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    }
                }
            }
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            beforeText = s.toString();
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}
