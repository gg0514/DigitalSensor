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
import android.widget.RadioButton;
import android.widget.Toast;

import java.text.DecimalFormat;

import static android.content.Context.MODE_PRIVATE;
import static com.example.kyj.staqua.MainActivity.FileLogWriter;
import static com.example.kyj.staqua.MainActivity.SendProtocol_Get;
import static com.example.kyj.staqua.MainActivity.m_bCh_PhCompIsCurrent;
import static com.example.kyj.staqua.MainActivity.m_dCh_PhFixValue;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivityCaliChPhCompensation extends Fragment {

    private EditText edCalibration;
    RadioButton rbCurrent;
    RadioButton rbFixed;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_cali_ch_phcompensation, container, false);
        edCalibration = (EditText)v.findViewById(R.id.Cali_Ch_PC_Edit_Ed);
        edCalibration.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edCalibration.getWindowToken(), 0);
                }
                else {
                    edCalibration.setText("");
                }
            }
        });

        CompensationTextWatcher ftWatcher = new CompensationTextWatcher(edCalibration);
        edCalibration.addTextChangedListener(ftWatcher);
        v.findViewById(R.id.Cali_Ch_PC_Main).setOnClickListener(mClickListener);
        v.findViewById(R.id.Cali_Ch_PC_Option2_Apply).setOnClickListener(mClickListener);

        rbFixed = (RadioButton)v.findViewById(R.id.Cali_Ch_PC_Fixed_Radio);
        rbCurrent = (RadioButton)v.findViewById(R.id.Cali_Ch_PC_Current_Radio);

        if (m_bCh_PhCompIsCurrent)   rbCurrent.setChecked(true);
        else if (!m_bCh_PhCompIsCurrent)  rbFixed.setChecked(true);
        edCalibration.setText(Double.toString(m_dCh_PhFixValue));

        return v;
    }

    public static SubActivityCaliChPhCompensation newInstance(String text) {

        SubActivityCaliChPhCompensation f = new SubActivityCaliChPhCompensation();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    public static boolean isStringDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    Button.OnClickListener mClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            //이곳에 버튼 클릭시 일어날 일을 적습니다.
            switch (v.getId()) {
                case R.id.Cali_Ch_PC_Option2_Apply:
                    SharedPreferences prefs;
                    prefs = getActivity().getSharedPreferences("Ch_Comp", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    String strFilter = edCalibration.getText().toString();
                    if (rbCurrent.isChecked())
                    {
                        m_bCh_PhCompIsCurrent = TRUE;
                        editor.putString("FixValue", strFilter);
                        editor.putString("SetPoint", "CURRENT");

                        byte [] bDummy = new byte[1];
                        bDummy[0] = 0x01; // 보상 Off
                        if (!SendProtocol_Get( 2, (byte)0xA9, bDummy)){
                            Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                        }

                    }
                    else if (rbFixed.isChecked())
                    {
                        m_bCh_PhCompIsCurrent = FALSE;
                        m_dCh_PhFixValue = Double.parseDouble(strFilter);
                        editor.putString("FixValue", strFilter);
                        editor.putString("SetPoint", "FIX");

                        DecimalFormat df3Figure = new DecimalFormat("0.000");
                        DecimalFormat df2Figure = new DecimalFormat("00.00");
                        String strPhFixValue = "";
                        if (m_dCh_PhFixValue >= 10) strPhFixValue  = df2Figure.format( m_dCh_PhFixValue );
                        else strPhFixValue  = df3Figure.format( m_dCh_PhFixValue );

                     //   String strPayload = strPhFixValue+" ";
                     //   int nLength = strPhFixValue.length();
                        byte[] btValue = strPhFixValue.getBytes();

                        byte[] btData = new byte[7];
                        btData[0] = 0x00; // 보상 On
                        System.arraycopy(btValue, 0, btData, 1, 5);
                        btData[6] = 0x20;

                        if (!SendProtocol_Get( 8, (byte)0xA9, btData)){
                            Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                            return;
                        }
                    }
                    editor.commit();
                    FileLogWriter(getString(R.string.multi_cal_ch_filter),"Click Apply");
                    Toast.makeText(getActivity(), R.string.multi_applied, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    break ;
            }
        }
    };

    private class CompensationTextWatcher implements TextWatcher {
        EditText et;
        String beforeText;

        public CompensationTextWatcher(EditText et){
            this.et = et;
        }
        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            if (!isStringDouble(s.toString())) return;
            if(s.toString().length() > 0){
                if((Double.parseDouble(s.toString()) > 14) || (Double.parseDouble(s.toString()) < 0)){
                    et.setText(beforeText);
                    Toast.makeText(getActivity(), R.string.multi_ph_validation, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
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
