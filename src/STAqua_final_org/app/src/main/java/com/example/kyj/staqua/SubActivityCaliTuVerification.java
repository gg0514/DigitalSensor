package com.example.kyj.staqua;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;
import static com.example.kyj.staqua.MainActivity.FileCalibrationWriter;
import static com.example.kyj.staqua.MainActivity.m_dTu;
import static com.example.kyj.staqua.MainActivity.m_strTuStickNo;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivityCaliTuVerification extends Fragment {

    private EditText edCalibration;

    private EditText edDate;
    private EditText edStandard;
    private EditText edMeasured;
    private EditText edRate;
    private TextView txHelp;
    SharedPreferences prefs;

    String m_strDate = "";
    String m_strStandard = "";
    String m_strMeasured = "";

    DecimalFormat df2Figure;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_cali_tu_verification, container, false);

        txHelp = (TextView)v.findViewById(R.id.Cali_Tu_Ver_Help_Text1);
        edCalibration = (EditText)v.findViewById(R.id.Cali_Tu_Ver_Stick_Ed);

        edDate = (EditText)v.findViewById(R.id.Cali_Tu_Ver_Date_Ed);
        edStandard = (EditText)v.findViewById(R.id.Cali_Tu_Ver_Standard_Ed);
        edMeasured = (EditText)v.findViewById(R.id.Cali_Tu_Ver_Measured_Ed);
        edRate = (EditText)v.findViewById(R.id.Cali_Tu_Ver_Rate_Ed);

        df2Figure = new DecimalFormat("00.00");

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
        VerificationTextWatcher ctWatcher = new VerificationTextWatcher(edCalibration);
        edCalibration.addTextChangedListener(ctWatcher);

        v.findViewById(R.id.Cali_Tu_Ver_Main).setOnClickListener(mClickListener);
        v.findViewById(R.id.Cali_Tu_Ver_Stick_Btn).setOnClickListener(mClickListener);
        v.findViewById(R.id.Cali_Tu_Ver_Measured_Btn).setOnClickListener(mClickListener);
        v.findViewById(R.id.Cali_Tu_Ver_Standard_Btn).setOnClickListener(mClickListener);

        prefs = getActivity().getSharedPreferences("TU1_StickNo", MODE_PRIVATE);
        m_strTuStickNo = prefs.getString("No", "");
        if (m_strTuStickNo =="")  txHelp.setText(R.string.multi_verify_new_help_line);
        else  {
            m_strDate = prefs.getString("Date", "");
            m_strStandard = prefs.getString("Standard", "");
            m_strMeasured = prefs.getString("Measured", "");
            txHelp.setText(R.string.multi_verify_saved_help_line);
            edCalibration.setText(m_strTuStickNo);
            edDate.setText(m_strDate);
            edStandard.setText(m_strStandard);
            edMeasured.setText(m_strMeasured);
            Double dRate = calcRate(parseDouble(m_strStandard), parseDouble(m_strMeasured));
            dRate = parseDouble(df2Figure.format(dRate));
            edRate.setText(Double.toString(dRate));
        }
        return v;
    }

    public static SubActivityCaliTuVerification newInstance(String text) {

        SubActivityCaliTuVerification f = new SubActivityCaliTuVerification();
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


    public static double calcRate(Double d1, Double d2) {
        double dBase = d1 * 0.001;
        double dCurrent = d2 * 0.001;
        double dResult = 0.0d;
        dCurrent = dCurrent - dBase;
        dCurrent = Math.abs(dCurrent);
        dResult = (dCurrent/dBase) * 100;
        return dResult;
    }

    Button.OnClickListener mClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
//                case R.id.Cali_Tu_Ver_Stick_Btn:
//                    String strStickNo = edCalibration.getText().toString();
//                    if (!strStickNo.equals(m_strTuStickNo))
//                    {
//                        m_strDate = "";
//                        m_strStandard = "0.000";
//                        m_strMeasured = "0.000";
//                        edDate.setText(m_strDate);
//                        edStandard.setText(m_strStandard);
//                        edMeasured.setText(m_strMeasured);
//                        edRate.setText("");
//                    }
//                    else
//                    {
//                        m_strDate = prefs.getString("Date", "");
//                        m_strStandard = prefs.getString("Standard", "");
//                        m_strMeasured = prefs.getString("Measured", "");
//                        edDate.setText(m_strDate);
//                        edStandard.setText(m_strStandard);
//                        edMeasured.setText(m_strMeasured);
//                        edRate.setText("");
//                    }
//                    break;
                case R.id.Cali_Tu_Ver_Standard_Btn:
                    int nStickNumber = parseInt(edCalibration.getText().toString());
                    Double dMeasured = Double.parseDouble(edMeasured.getText().toString());
                    // 스틱번호가 0보다 크고 현재값이 0보다 큰 경우 다이얼로그 생성
                    if (nStickNumber > 0 && dMeasured > 0)
                    {
                        AlertDialog.Builder d = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_TRADITIONAL);
                        d.setMessage(R.string.multi_verification_has_record);
                        d.setPositiveButton(R.string.multi_answer_yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                m_strTuStickNo ="0";
                                m_strDate = "";
                                m_strStandard = "0.000";
                                m_strMeasured = "0.000";
                                edCalibration.setText(m_strTuStickNo);
                                edDate.setText(m_strDate);
                                edStandard.setText(m_strStandard);
                                edMeasured.setText(m_strMeasured);
                                edRate.setText("");
                                // 초기값 어플에 저장
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("No", edCalibration.getText().toString());
                                editor.putString("Date", edDate.getText().toString());
                                editor.putString("Standard", edStandard.getText().toString());
                                editor.putString("Measured", edMeasured.getText().toString());
                                editor.commit();
                                // Help 화면 남기기
                                txHelp.setText(R.string.multi_verify_new_help_line);
                            }
                        });
                        d.setNegativeButton(R.string.multi_answer_no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                m_strDate = "";
                                m_strStandard = "0.000";
                                m_strMeasured = "0.000";
                                edDate.setText(m_strDate);
                                edStandard.setText(m_strStandard);
                                edMeasured.setText(m_strMeasured);
                                edRate.setText("");
                                dialog.cancel();
                                txHelp.setText(R.string.multi_verify_saved_help_line);
                            }
                        });
                        d.show();
                    }
                    else if (nStickNumber == 0)
                    {
                        Toast.makeText(getActivity(), R.string.multi_verification_check_stick, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    }
                    else
                    {
                        SystemClock.sleep(200);
                        DecimalFormat df3Figure = new DecimalFormat("0.000");
                        edStandard.setText( df3Figure.format(m_dTu));
                        txHelp.setText(R.string.multi_verification_success);
                    }
                    break;
                case R.id.Cali_Tu_Ver_Measured_Btn:
                    if (parseInt(edCalibration.getText().toString()) == 0)
                    {
                        Toast.makeText(getActivity(), R.string.multi_verification_check_stick, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                        return;
                    }
                    if (parseDouble(edStandard.getText().toString()) > 0)
                    {
                        SystemClock.sleep(200);
                        DecimalFormat df3Figure = new DecimalFormat("0.000");
                        edMeasured.setText( df3Figure.format(m_dTu));
                        // 편차율 계산
                        m_strStandard = edStandard.getText().toString();
                        m_strMeasured = edMeasured.getText().toString();
                        Double dRate = calcRate(parseDouble(m_strStandard), parseDouble(m_strMeasured));
                        dRate = parseDouble(df2Figure.format(dRate));
                        edRate.setText(Double.toString(dRate));
                        // 입력일자 표시
                        SimpleDateFormat sdCurrentTime;
                        Date dCurrentTime = new Date();
                        sdCurrentTime = new SimpleDateFormat(getResources().getText(R.string.multi_date_format).toString());
                        String strCurrentDate = sdCurrentTime.format(dCurrentTime);
                        edDate.setText(strCurrentDate);
                        // 저장값 어플에 저장
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("No", edCalibration.getText().toString());
                        editor.putString("Date", edDate.getText().toString());
                        editor.putString("Standard", edStandard.getText().toString());
                        editor.putString("Measured", edMeasured.getText().toString());
                        editor.commit();
                        // 스틱값 저장
                        m_strTuStickNo = edCalibration.getText().toString();
                        // 교정이력에 남기기
                        String strValue = "Stick:"+edCalibration.getText().toString();
                        String strResult = edStandard.getText().toString()+"->"+edMeasured.getText().toString()+"["+edRate.getText().toString()+"]";
                        String strLocation = getString(R.string.multi_verification);
                        FileCalibrationWriter("TU_Cali", strLocation, strValue, strResult);
                        // 성공화면 남기기
                        txHelp.setText(R.string.multi_verification_success);
                    }
                    else
                    {
                        Toast.makeText(getActivity(), R.string.multi_verification_check_standard, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    }
                    break;

            }
        }
    };

    private class VerificationTextWatcher implements TextWatcher {
        EditText et;
        String beforeText;
        public VerificationTextWatcher(EditText et){
            this.et = et;
        }
        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            if (!isStringDouble(s.toString())) return;
            if(s.toString().length() > 0){
                if((parseDouble(s.toString()) > 9999) || (parseDouble(s.toString()) < 0)){
                    et.setText(beforeText);
                    Toast.makeText(getActivity(), R.string.multi_verification_validation, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
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
