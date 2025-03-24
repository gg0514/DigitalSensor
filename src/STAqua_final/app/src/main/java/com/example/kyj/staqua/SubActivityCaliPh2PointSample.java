package com.example.kyj.staqua;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

import static com.example.kyj.staqua.MainActivity.DataCommunicationThread.nTimer;
import static com.example.kyj.staqua.MainActivity.FileCalibrationWriter;
import static com.example.kyj.staqua.MainActivity.FileLogWriter;
import static com.example.kyj.staqua.MainActivity.SendProtocol_Get;
import static com.example.kyj.staqua.MainActivity.m_DataCommunicationThread;
import static com.example.kyj.staqua.MainActivity.m_dPh;
import static com.example.kyj.staqua.MainActivity.m_nCaliStatus;
import static com.example.kyj.staqua.MainActivity.startDataCommunicationThread;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivityCaliPh2PointSample extends Fragment {

    private static SubActivityCaliPh2PointSample.Ph2PointSampleSendThread m_Ph2PointSampleSendThread;
    private static SubActivityCaliPh2PointSample.UpdateCurrentValueThread m_UpdateCurrentValueThreadThread;
    EditText edCurrent;
    Button btApply;
    TextView tvHelpText;
    ImageView ivIcon;
    ProgressBar pgCircle;

    int m_nTime;

    private EditText edCalibration;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_cali_ph_2pointsample, container, false);

        m_nTime = 1;

        ivIcon = (ImageView)v.findViewById(R.id.Cali_Ph_2PS_Help_Icon_Beaker);
        pgCircle = (ProgressBar)v.findViewById(R.id.Cali_Ph_2PS_Help_Progress);
        tvHelpText = (TextView)v.findViewById(R.id.Cali_Ph_2PS_Help_Text1);
        edCurrent = (EditText)v.findViewById(R.id.Cali_Ph_2PS_Current_Ed);
        btApply = (Button)v.findViewById(R.id.Cali_Ph_2PS_Option2_Apply);

        edCalibration = (EditText)v.findViewById(R.id.Cali_Ph_2PS_Calibration_Ed);
        edCalibration.setOnFocusChangeListener(new View.OnFocusChangeListener(){
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

        CalibrationTextWatcher ctWatcher = new CalibrationTextWatcher(edCalibration);
        edCalibration.addTextChangedListener(ctWatcher);

        v.findViewById(R.id.Cali_Ph_2PS_Main).setOnClickListener(mClickListener);
        v.findViewById(R.id.Cali_Ph_2PS_UpDown_Up).setOnClickListener(mClickListener);
        v.findViewById(R.id.Cali_Ph_2PS_UpDown_Down).setOnClickListener(mClickListener);
        v.findViewById(R.id.Cali_Ph_2PS_Option2_Stop).setOnClickListener(mClickListener);
        v.findViewById(R.id.Cali_Ph_2PS_Option2_Apply).setOnClickListener(mClickListener);


        DecimalFormat df2Figure = new DecimalFormat("0.00");
        edCurrent.setText( df2Figure.format(m_dPh));
        edCalibration.setText( df2Figure.format(m_dPh));

        m_Ph2PointSampleSendThread = new Ph2PointSampleSendThread(true);
        m_UpdateCurrentValueThreadThread = new UpdateCurrentValueThread(true);
        m_UpdateCurrentValueThreadThread.start();
        return v;
    }

    @Override
    public void onStop() {
        if (m_UpdateCurrentValueThreadThread != null){
            m_UpdateCurrentValueThreadThread.stopUpdateCurrentValueThread();
        }
        if (m_Ph2PointSampleSendThread.isAlive()){
            m_Ph2PointSampleSendThread.stopPh2PointSampleSendThread();
        }
        super.onStop();
    }

    public static SubActivityCaliPh2PointSample newInstance(String text) {
        SubActivityCaliPh2PointSample f = new SubActivityCaliPh2PointSample();
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

    public void startPh2PointSampleSendThread(){
        if (!m_Ph2PointSampleSendThread.isAlive()){
            m_Ph2PointSampleSendThread = new Ph2PointSampleSendThread(true);
            m_Ph2PointSampleSendThread.start();
        }
    }

    public void DisplayHelpText(int nCaliStatus){
        if (nCaliStatus == 1) {
            ivIcon.setVisibility(View.INVISIBLE);
            pgCircle.setVisibility(View.VISIBLE);
            if (isAdded())tvHelpText.setText(getString(R.string.multi_cal_status_1));
        }
        else {
            pgCircle.setVisibility(View.INVISIBLE);
            ivIcon.setImageResource(R.drawable.ic_exclamation);
            ivIcon.setVisibility(View.VISIBLE);
            if (isAdded()){
                if (nCaliStatus == 0) tvHelpText.setText(getString(R.string.multi_cal_status_0));
                else if (nCaliStatus == 2)
                {
                    if (m_nTime == 1)
                    {
                        tvHelpText.setText(getString(R.string.multi_2buffer_help_line));
                    }
                    else
                    {
                        tvHelpText.setText(getString(R.string.multi_cal_status_2));
                        m_nTime = 1;
                        String strResult = tvHelpText.getText().toString();
                        String strValue = edCalibration.getText().toString();
                        String strLocation = getString(R.string.multi_2point_sample);
                        FileCalibrationWriter("PH_Cali", strLocation, strValue, strResult);
                        return;
                    }
                }
                else if (nCaliStatus == 3) tvHelpText.setText(getString(R.string.multi_cal_status_3));
                else if (nCaliStatus == 4) tvHelpText.setText(getString(R.string.multi_cal_status_4));
                else if (nCaliStatus == 5) tvHelpText.setText(getString(R.string.multi_cal_status_5));
                else if (nCaliStatus == 6) tvHelpText.setText(getString(R.string.multi_cal_status_6));
                else if (nCaliStatus == 7) tvHelpText.setText(getString(R.string.multi_cal_status_7));
                else if (nCaliStatus == 8) tvHelpText.setText(getString(R.string.multi_cal_status_8));
                else if (nCaliStatus == 9) tvHelpText.setText(getString(R.string.multi_cal_status_9));
                else if (nCaliStatus == 10) tvHelpText.setText(getString(R.string.multi_cal_status_10));
                else if (nCaliStatus == 11) tvHelpText.setText(getString(R.string.multi_cal_status_11));
                else if (nCaliStatus == 12) tvHelpText.setText(getString(R.string.multi_cal_status_12));
                else if (nCaliStatus == 13) tvHelpText.setText(getString(R.string.multi_cal_status_13));
                else if (nCaliStatus == 14) tvHelpText.setText(getString(R.string.multi_cal_status_14));
                else if (nCaliStatus == 15) tvHelpText.setText(getString(R.string.multi_cal_status_15));
                else if (nCaliStatus == 16) tvHelpText.setText(getString(R.string.multi_cal_cancel));
                String strResult = tvHelpText.getText().toString();
                String strValue = edCalibration.getText().toString();
                String strLocation = getString(R.string.multi_2point_sample);
                if ((nCaliStatus == 2) && (m_nTime == 1)) strResult = "2nd Step";
                FileCalibrationWriter("PH_Cali", strLocation, strValue, strResult);
            }
        }
        if ((nCaliStatus == 2) && (m_nTime == 1))
        {
            m_nTime++;
        }
        else if (nCaliStatus != 1)
        {
            m_nTime = 1;
        }
    }

    Button.OnClickListener mClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            //이곳에 버튼 클릭시 일어날 일을 적습니다.
            String beforeText = edCalibration.getText().toString();
            if(beforeText.length() == 0) beforeText = "0.00";
            Double dbCalibration = Double.parseDouble(beforeText);
            DecimalFormat df2Figure = new DecimalFormat("0.00");
            switch (v.getId()) {
                case R.id.Cali_Ph_2PS_UpDown_Up:
                    dbCalibration = dbCalibration + 0.01;
                    dbCalibration = Double.parseDouble(df2Figure.format(dbCalibration));
                    edCalibration.setText(Double.toString(dbCalibration));
                    break;
                case R.id.Cali_Ph_2PS_UpDown_Down:
                    dbCalibration = dbCalibration - 0.01;
                    dbCalibration = Double.parseDouble(df2Figure.format(dbCalibration));
                    edCalibration.setText(Double.toString(dbCalibration));
                    break;
                case R.id.Cali_Ph_2PS_Option2_Stop :
                    m_nCaliStatus = 16;
                    FileLogWriter(getString(R.string.multi_cal_ph_1p_sample),"Click Stop");
                    break;
                case R.id.Cali_Ph_2PS_Option2_Apply:
                    m_DataCommunicationThread.stopDCThread();
                    if (m_nTime == 1) // 첫번째 시도시
                    {

                        DecimalFormat df4F = new DecimalFormat("0000. ");
                        DecimalFormat df3F = new DecimalFormat("000.0 ");
                        DecimalFormat df2F = new DecimalFormat("00.00 ");
                        DecimalFormat df1F = new DecimalFormat("0.000 ");
                        Double dbTemp = 0.0;
                        dbTemp = Double.parseDouble(edCalibration.getText().toString());
                        String strPayload1 = "";
                        if (dbTemp >= 1000) strPayload1=df4F.format(dbTemp);
                        else if (dbTemp >= 100) strPayload1=df3F.format(dbTemp);
                        else if (dbTemp >= 10) strPayload1=df2F.format(dbTemp);
                        else strPayload1=df1F.format(dbTemp);

                  //      String strPayload1 = edCalibration.getText().toString()+" ";
                        int nLength = strPayload1.length();
                        byte[] btData = new byte[3+nLength];
                        byte[] btValue = strPayload1.getBytes();
                        btData[0] = 0x01; // 센서타입
                        btData[1] = 0x03; // Cal 타입
                        btData[2] = 0x30; // Value
                        System.arraycopy(btValue, 0, btData, 3, nLength);
                        if (!SendProtocol_Get(4+nLength,(byte)0xA7, btData)){
                            Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                            return;
                        }

                    }
                    else if (m_nTime == 2) // 두번째 시도시
                    {
                        DecimalFormat df4F = new DecimalFormat("0000. ");
                        DecimalFormat df3F = new DecimalFormat("000.0 ");
                        DecimalFormat df2F = new DecimalFormat("00.00 ");
                        DecimalFormat df1F = new DecimalFormat("0.000 ");
                        Double dbTemp = 0.0;
                        dbTemp = Double.parseDouble(edCalibration.getText().toString());
                        String strPayload1 = "";
                        if (dbTemp >= 1000) strPayload1=df4F.format(dbTemp);
                        else if (dbTemp >= 100) strPayload1=df3F.format(dbTemp);
                        else if (dbTemp >= 10) strPayload1=df2F.format(dbTemp);
                        else strPayload1=df1F.format(dbTemp);

                       // String strPayload1 = edCalibration.getText().toString()+" ";
                        int nLength = strPayload1.length();
                        byte[] btData = new byte[3+nLength];
                        byte[] btValue = strPayload1.getBytes();
                        btData[0] = 0x01; // 센서타입
                        btData[1] = 0x03; // Cal 타입
                        btData[2] = 0x31; // Value
                        System.arraycopy(btValue, 0, btData, 3, nLength);
                        if (!SendProtocol_Get(4+nLength,(byte)0xA7, btData)){
                            Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                            return;
                        }
                    }

                    m_nCaliStatus = 1;
                    startPh2PointSampleSendThread();
                    FileLogWriter(getString(R.string.multi_cal_ph_1p_sample),"Click Apply");
                    btApply.setEnabled(FALSE);
                    DisplayHelpText(m_nCaliStatus);
                    break ;
            }
        }
    };

    private class CalibrationTextWatcher implements TextWatcher {
        EditText et;
        String beforeText;

        public CalibrationTextWatcher(EditText et){
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

    class Ph2PointSampleSendThread extends Thread {
        private boolean isPlay = false;
        private int nCount = 0;
        public Ph2PointSampleSendThread(boolean isPlay) {this.isPlay = isPlay;}
        public void stopPh2PointSampleSendThread(){
            isPlay = !isPlay;
            nCount = 0;
            mHandler.sendEmptyMessage(1);
        }
        @Override
        public void run() {
            super.run();
            while (isPlay) {
                try {
                    Thread.sleep(400);
       //             SendProtocol((byte)0x67, 1, "1", m_strMode);
                    if (m_nCaliStatus != 1)
                    {
                        stopPh2PointSampleSendThread();
                    }
                    if (nCount > 300)
                    {
                        m_nCaliStatus = 15;
                    }
                    nCount++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    class UpdateCurrentValueThread extends Thread {
        private boolean isPlay = false;
        public UpdateCurrentValueThread(boolean isPlay) {this.isPlay = isPlay;}
        public void stopUpdateCurrentValueThread(){
            isPlay = !isPlay;
        }
        @Override
        public void run() {
            super.run();
            while (isPlay) {
                try {
                    Thread.sleep(1000);
                    mHandler.sendEmptyMessage(9);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private final Handler mHandler = new Handler() { //핸들러를 통해 UI스레드에 접근한다.
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                nTimer = 1500;
                startDataCommunicationThread();
                btApply.setEnabled(TRUE);
                DisplayHelpText(m_nCaliStatus);
            }
            else if (msg.what == 9)
            {
                DecimalFormat df2Figure = new DecimalFormat("0.00");
                edCurrent.setText( df2Figure.format(m_dPh));
            }
        }
    };

}
