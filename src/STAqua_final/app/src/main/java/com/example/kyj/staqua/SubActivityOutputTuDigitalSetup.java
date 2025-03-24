package com.example.kyj.staqua;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

import static android.content.Context.MODE_PRIVATE;
import static com.example.kyj.staqua.MainActivity.m_bTuPhaseIsHigh;
import static com.example.kyj.staqua.MainActivity.m_dTuDeadBand;
import static com.example.kyj.staqua.MainActivity.m_dTuHighAlarm;
import static com.example.kyj.staqua.MainActivity.m_dTuLowAlarm;
import static com.example.kyj.staqua.MainActivity.m_dTuSetPoint;
import static com.example.kyj.staqua.MainActivity.m_nTuOffDelay;
import static com.example.kyj.staqua.MainActivity.m_nTuOnDelay;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivityOutputTuDigitalSetup extends Fragment {

    private EditText edHighAlarmCurrent;
    private EditText edHighAlarmChange;
    private EditText edLowAlarmCurrent;
    private EditText edLowAlarmChange;
    private EditText edRelaySelectionCurrent;
    private EditText edPhaseCurrent;
    private EditText edDeadBandCurrent;
    private EditText edDeadBandChange;
    private EditText edOnDelayCurrent;
    private EditText edOnDelayChange;
    private EditText edOffDelayCurrent;
    private EditText edOffDelayChange;
    private EditText edSetPointCurrent;
    private EditText edSetPointChange;
    private EditText edRelayCurrent;

    DecimalFormat df2Figure = new DecimalFormat("0.00");

    SharedPreferences prefs;

//    String m_strHighAlarm = "";
//    String m_strLowAlarm = "";
//    String m_strRelaySelection = "";
//    String m_strPhase = "";
//    String m_strDeadBand = "";
//    String m_strOnDelay = "";
//    String m_strOffDelay = "";
//    String m_strSetPoint = "";
//    String m_strRelay = "";

    Spinner spRelaySelection;
    Spinner spPhase;
    Spinner spRelay;

    View.OnFocusChangeListener mFocuslistener;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_output_tu_digitalsetup, container, false);

//        LinearLayout layout = (LinearLayout) v.findViewById(R.id.Output_Tu_DS_Setpoint);
//        layout.setVisibility(v.GONE);

        edHighAlarmCurrent = (EditText)v.findViewById(R.id.Output_Tu_DS_HighAlarm_Ed_Current);
        edHighAlarmChange = (EditText)v.findViewById(R.id.Output_Tu_DS_HighAlarm_Ed_Change);
        edLowAlarmCurrent = (EditText)v.findViewById(R.id.Output_Tu_DS_LowAlarm_Ed_Current);
        edLowAlarmChange = (EditText)v.findViewById(R.id.Output_Tu_DS_LowAlarm_Ed_Change);
        edRelaySelectionCurrent = (EditText)v.findViewById(R.id.Output_Tu_DS_RelaySelection_Ed_Current);
        edPhaseCurrent = (EditText)v.findViewById(R.id.Output_Tu_DS_Phase_Ed_Current);
        edDeadBandCurrent = (EditText)v.findViewById(R.id.Output_Tu_DS_Deadband_Ed_Current);
        edDeadBandChange = (EditText)v.findViewById(R.id.Output_Tu_DS_Deadband_Ed_Change);
        edOnDelayCurrent = (EditText)v.findViewById(R.id.Output_Tu_DS_OnDelay_Ed_Current);
        edOnDelayChange = (EditText)v.findViewById(R.id.Output_Tu_DS_OnDelay_Ed_Change);
        edOffDelayCurrent = (EditText)v.findViewById(R.id.Output_Tu_DS_OffDelay_Ed_Current);
        edOffDelayChange = (EditText)v.findViewById(R.id.Output_Tu_DS_OffDelay_Ed_Change);
        edSetPointCurrent = (EditText)v.findViewById(R.id.Output_Tu_DS_Setpoint_Ed_Current);
        edSetPointChange = (EditText)v.findViewById(R.id.Output_Tu_DS_Setpoint_Ed_Change);
        edRelayCurrent = (EditText)v.findViewById(R.id.Output_Tu_DS_Relay_Ed_Current);
        edHighAlarmChange.setOnFocusChangeListener(meClickListener);
        edLowAlarmChange.setOnFocusChangeListener(meClickListener);
        edDeadBandChange.setOnFocusChangeListener(meClickListener);
        edOnDelayChange.setOnFocusChangeListener(meClickListener);
        edOffDelayChange.setOnFocusChangeListener(meClickListener);
        edSetPointChange.setOnFocusChangeListener(meClickListener);

        DigitalTextWatcher dtHighAlarmWatcher = new DigitalTextWatcher(edHighAlarmChange);
        edHighAlarmChange.addTextChangedListener(dtHighAlarmWatcher);
        DigitalTextWatcher dtLowAlarmWatcher = new DigitalTextWatcher(edLowAlarmChange);
        edLowAlarmChange.addTextChangedListener(dtLowAlarmWatcher);
        DigitalTextWatcher dtDeadBandWatcher = new DigitalTextWatcher(edDeadBandChange);
        edDeadBandChange.addTextChangedListener(dtDeadBandWatcher);
        DigitalTextWatcher dtOnDelayWatcher = new DigitalTextWatcher(edOnDelayChange);
        edOnDelayChange.addTextChangedListener(dtOnDelayWatcher);
        DigitalTextWatcher dtOffDelayWatcher = new DigitalTextWatcher(edOffDelayChange);
        edOffDelayChange.addTextChangedListener(dtOffDelayWatcher);
        DigitalTextWatcher dtSetPointWatcher = new DigitalTextWatcher(edSetPointChange);
        edSetPointChange.addTextChangedListener(dtSetPointWatcher);

        spRelaySelection = (Spinner) v.findViewById(R.id.Output_Tu_DS_RelaySelection_Sp_Change);
        String[] optionLevel = getResources().getStringArray(R.array.relay);
        SpinnerAdapter spARelaySelection = new SpinnerAdapter(getActivity(),android.R.layout.simple_spinner_item, optionLevel);
        spRelaySelection.setAdapter(spARelaySelection);

        spPhase = (Spinner) v.findViewById(R.id.Output_Tu_DS_Phase_Sp_Change);
        String[] optionLevel1 = getResources().getStringArray(R.array.phase);
        SpinnerAdapter spAPhase = new SpinnerAdapter(getActivity(),android.R.layout.simple_spinner_item, optionLevel1);
        spPhase.setAdapter(spAPhase);

        spRelay = (Spinner) v.findViewById(R.id.Output_Tu_DS_Relay_Sp_Change);
        String[] optionLevel2 = getResources().getStringArray(R.array.relay);
        SpinnerAdapter spARelay = new SpinnerAdapter(getActivity(),android.R.layout.simple_spinner_item, optionLevel2);
        spRelay.setAdapter(spARelay);

        v.findViewById(R.id.Output_Tu_DS_Main).setOnClickListener(mvClickListener);
        v.findViewById(R.id.Output_Tu_DS_Option1_Apply).setOnClickListener(mClickListener);
        v.findViewById(R.id.Output_Tu_DS_Option1_FactoryReset).setOnClickListener(mClickListener);

        prefs = getActivity().getSharedPreferences("Tu_Digital", MODE_PRIVATE);

        edHighAlarmCurrent.setText(Double.toString(m_dTuHighAlarm));
        edHighAlarmChange.setText(Double.toString(m_dTuHighAlarm));
        edLowAlarmCurrent.setText(Double.toString(m_dTuLowAlarm));
        edLowAlarmChange.setText(Double.toString(m_dTuLowAlarm));
        if (m_bTuPhaseIsHigh) spPhase.setSelection(0);
        else spPhase.setSelection(1);
        edDeadBandCurrent.setText(Double.toString(m_dTuDeadBand));
        edDeadBandChange.setText(Double.toString(m_dTuDeadBand));
        edOnDelayCurrent.setText(Integer.toString(m_nTuOnDelay));
        edOnDelayChange.setText(Integer.toString(m_nTuOnDelay));
        edOffDelayCurrent.setText(Integer.toString(m_nTuOffDelay));
        edOffDelayChange.setText(Integer.toString(m_nTuOffDelay));
        edSetPointCurrent.setText(Double.toString(m_dTuSetPoint));
        edSetPointChange.setText(Double.toString(m_dTuSetPoint));


//        m_strHighAlarm = prefs.getString("HighAlarm", "");
//        if (m_strHighAlarm != "") {
//            edHighAlarmCurrent.setText(m_strHighAlarm);
//            edHighAlarmChange.setText(m_strHighAlarm);
//        }
//        m_strLowAlarm = prefs.getString("LowAlarm", "");
//        if (m_strLowAlarm != "") {
//            edLowAlarmCurrent.setText(m_strLowAlarm);
//            edLowAlarmChange.setText(m_strLowAlarm);
//        }
//        m_strRelaySelection = prefs.getString("RelaySelection", "");
//        if (m_strRelaySelection != ""){
//            edRelaySelectionCurrent.setText(m_strRelaySelection);
//            if (m_strRelaySelection.equals(getResources().getText(R.string.multi_disable).toString()))  spRelaySelection.setSelection(0);
//            else spRelaySelection.setSelection(Integer.parseInt(m_strRelaySelection));
//        }
//        else{
//            edRelaySelectionCurrent.setText(getResources().getText(R.string.multi_disable).toString());
//            spRelaySelection.setSelection(0);
//        }
//
//        m_strPhase = prefs.getString("Phase", "");
//        if (m_strPhase != ""){
//            edPhaseCurrent.setText(m_strPhase);
//            if (m_strPhase.equals("High")) spPhase.setSelection(0);
//            else if  (m_strPhase.equals("Low")) spPhase.setSelection(1);
//        }
//        else{
//            edPhaseCurrent.setText("High");
//            spPhase.setSelection(0);
//        }
//        m_strDeadBand = prefs.getString("DeadBand", "");
//        if (m_strDeadBand != ""){
//            edDeadBandCurrent.setText(m_strDeadBand);
//            edDeadBandChange.setText(m_strDeadBand);
//        }
//        m_strOnDelay = prefs.getString("OnDelay", "");
//        if (m_strOnDelay != ""){
//            edOnDelayCurrent.setText(m_strOnDelay);
//            edOnDelayChange.setText(m_strOnDelay);
//        }
//        m_strOffDelay = prefs.getString("OffDelay", "");
//        if (m_strOffDelay != ""){
//            edOffDelayCurrent.setText(m_strOffDelay);
//            edOffDelayChange.setText(m_strOffDelay);
//        }
//        m_strSetPoint = prefs.getString("SetPoint", "");
//        if (m_strSetPoint != ""){
//            edSetPointCurrent.setText(m_strSetPoint);
//            edSetPointChange.setText(m_strSetPoint);
//        }
//        m_strRelay = prefs.getString("Relay", "");
//        if (m_strRelay != ""){
//            edRelayCurrent.setText(m_strRelay);
//            if (m_strRelay.equals(getResources().getText(R.string.multi_disable).toString()))  spRelay.setSelection(0);
//            else spRelay.setSelection(Integer.parseInt(m_strRelay));
//        }
//        else {
//            edRelayCurrent.setText(getResources().getText(R.string.multi_disable).toString());
//            spRelay.setSelection(0);
//        }
        return v;
    }

    public static SubActivityOutputTuDigitalSetup newInstance(String text) {

        SubActivityOutputTuDigitalSetup f = new SubActivityOutputTuDigitalSetup();
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

    private class DigitalTextWatcher implements TextWatcher {
        EditText et;
        String beforeText;
        public DigitalTextWatcher(EditText et){
            this.et = et;
        }
        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            if (!isStringDouble(s.toString())) return;
            if(s.toString().length() > 0){
                if ((et.getId() == R.id.Output_Tu_DS_HighAlarm_Ed_Change) || (et.getId() == R.id.Output_Tu_DS_LowAlarm_Ed_Change) || (et.getId() == R.id.Output_Tu_DS_Deadband_Ed_Change)|| (et.getId() == R.id.Output_Tu_DS_Setpoint_Ed_Change))
                {
                    if((parseDouble(s.toString()) > 20) || (parseDouble(s.toString()) < 0)){
                        et.setText(beforeText);
                        Toast.makeText(getActivity(), R.string.multi_tu_validation, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    }
                }
                else if ((et.getId() == R.id.Output_Tu_DS_OnDelay_Ed_Change) || (et.getId() == R.id.Output_Tu_DS_OffDelay_Ed_Change))
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


    public class SpinnerAdapter extends ArrayAdapter<String> {
        Context context;
        String[] items = new String[] {};
        public SpinnerAdapter(final Context context, final int textViewResourceId, final String[] objects) {
            super(context, textViewResourceId, objects);
            this.items = objects;
            this.context = context;
        }
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            }
            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
            tv.setText(items[position]);
            tv.setTextColor(Color.parseColor("#38C6C8"));
//            tv.setTextSize(12);
//            tv.setHeight(50);
            return convertView;
        }
        /**
         * 기본 스피너 View 정의
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
            }
            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
            tv.setText(items[position]);
            tv.setTextColor(Color.parseColor("#38C6C8"));
            // tv.setTextSize(12);
            return convertView;
        }
    }

    EditText.OnFocusChangeListener meClickListener  = new View.OnFocusChangeListener(){
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus)
            {
                switch(v.getId()){
                    case R.id.Output_Tu_DS_HighAlarm_Ed_Change:
                        edHighAlarmChange.setText("");
                        break;
                    case R.id.Output_Tu_DS_LowAlarm_Ed_Change:
                        edLowAlarmChange.setText("");
                        break;
                    case R.id.Output_Tu_DS_Deadband_Ed_Change:
                        edDeadBandChange.setText("");
                        break;
                    case R.id.Output_Tu_DS_OnDelay_Ed_Change:
                        edOnDelayChange.setText("");
                        break;
                    case R.id.Output_Tu_DS_OffDelay_Ed_Change:
                        edOffDelayChange.setText("");
                        break;
                    case R.id.Output_Tu_DS_Setpoint_Ed_Change:
                        edSetPointChange.setText("");
                        break;
                }
            }
        }
    };

    View.OnClickListener mvClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.Output_Tu_DS_Main:
                    InputMethodManager imm =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    break;
            }
        }
    };


    Button.OnClickListener mClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            SharedPreferences.Editor editor = prefs.edit();
            switch (v.getId()) {
                case R.id.Output_Tu_DS_Option1_Apply:
                    edHighAlarmCurrent.setText(edHighAlarmChange.getText().toString());
                    edLowAlarmCurrent.setText(edLowAlarmChange.getText().toString());
                    edRelaySelectionCurrent.setText(spRelaySelection.getSelectedItem().toString());
                    edPhaseCurrent.setText(spPhase.getSelectedItem().toString());
                    edDeadBandCurrent.setText(edDeadBandChange.getText().toString());
                    edOnDelayCurrent.setText(edOnDelayChange.getText().toString());
                    edOffDelayCurrent.setText(edOffDelayChange.getText().toString());
                    edSetPointCurrent.setText(edSetPointChange.getText().toString());
                    edRelayCurrent.setText(spRelay.getSelectedItem().toString());

                    // Main 에 알람 상하값 저장
                    m_dTuHighAlarm = Double.parseDouble(edHighAlarmChange.getText().toString());
                    m_dTuLowAlarm = Double.parseDouble(edLowAlarmChange.getText().toString());
                    if (spPhase.getSelectedItem().toString().equals("High")) m_bTuPhaseIsHigh = TRUE;
                    else if  (spPhase.getSelectedItem().toString().equals("Low")) m_bTuPhaseIsHigh = FALSE;
                    m_dTuDeadBand = Double.parseDouble(edDeadBandChange.getText().toString());
                    m_nTuOnDelay = Integer.parseInt(edOnDelayChange.getText().toString());
                    m_nTuOffDelay = Integer.parseInt(edOffDelayChange.getText().toString());
                    m_dTuSetPoint = Double.parseDouble(edSetPointChange.getText().toString());

                    editor.putString("HighAlarm", edHighAlarmChange.getText().toString());
                    editor.putString("LowAlarm", edLowAlarmChange.getText().toString());
                    editor.putString("RelaySelection", spRelaySelection.getSelectedItem().toString());
                    editor.putString("Phase", spPhase.getSelectedItem().toString());
                    editor.putString("DeadBand", edDeadBandChange.getText().toString());
                    editor.putString("OnDelay", edOnDelayChange.getText().toString());
                    editor.putString("OffDelay", edOffDelayChange.getText().toString());
                    editor.putString("SetPoint", edSetPointChange.getText().toString());
                    editor.putString("Relay", spRelay.getSelectedItem().toString());
                    editor.commit();
                    Toast.makeText(getActivity(), R.string.multi_applied, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    break;
                case R.id.Output_Tu_DS_Option1_FactoryReset:
                    String strHighAlarm = "20.00";
                    String strLowAlarm = "0.00";
                    String strRelaySelection = "Disable";
                    String strPhase = "High";
                    String strDeadBand = "0";
                    String strOnDelay = "300";
                    String strOffDelay = "30";
                    String strSetPoint = "20";
                    String strRelay = "Disable";

                    edHighAlarmCurrent.setText(strHighAlarm);
                    edHighAlarmChange.setText(strHighAlarm);
                    edLowAlarmCurrent.setText(strLowAlarm);
                    edLowAlarmChange.setText(strLowAlarm);
                    edRelaySelectionCurrent.setText(strRelaySelection);
                    spRelaySelection.setSelection(0);
                    edPhaseCurrent.setText(strPhase);
                    spPhase.setSelection(0);
                    edDeadBandCurrent.setText(strDeadBand);
                    edDeadBandChange.setText(strDeadBand);
                    edOnDelayCurrent.setText(strOnDelay);
                    edOnDelayChange.setText(strOnDelay);
                    edOffDelayCurrent.setText(strOffDelay);
                    edOffDelayChange.setText(strOffDelay);
                    edSetPointCurrent.setText(strSetPoint);
                    edSetPointChange.setText(strSetPoint);
                    edRelayCurrent.setText(strRelay);
                    spRelay.setSelection(0);

                    editor.putString("HighAlarm", strHighAlarm);
                    editor.putString("LowAlarm", strLowAlarm);
                    editor.putString("RelaySelection", strRelaySelection);
                    editor.putString("Phase", strPhase);
                    editor.putString("DeadBand", strDeadBand);
                    editor.putString("OnDelay", strOnDelay);
                    editor.putString("OffDelay", strOffDelay);
                    editor.putString("SetPoint", strSetPoint);
                    editor.putString("Relay", strRelay);
                    editor.commit();

                    Toast.makeText(getActivity(), R.string.multi_applied, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    break;
            }
        }
    };
}
