package com.example.kyj.staqua;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class SubActivityCaliPh1PointBuffer extends Fragment {
    private static SubActivityCaliPh1PointBuffer.Ph1PointBufferSendThread m_Ph1PointBufferSendThread;
    private static SubActivityCaliPh1PointBuffer.UpdateCurrentValueThread m_UpdateCurrentValueThreadThread;

    EditText edCurrent;
    Button btApply;
    TextView tvHelpText;
    ImageView ivIcon;
    ProgressBar pgCircle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_cali_ph_1pointbuffer, container, false);

        ivIcon = (ImageView)v.findViewById(R.id.Cali_Ph_1PB_Help_Icon_Beaker);
        pgCircle = (ProgressBar)v.findViewById(R.id.Cali_Ph_1PB_Help_Progress);
        tvHelpText = (TextView)v.findViewById(R.id.Cali_Ph_1PB_Help_Text1);
        edCurrent = (EditText)v.findViewById(R.id.Cali_Ph_1PB_Current_Ed);
        btApply = (Button)v.findViewById(R.id.Cali_Ph_1PB_Option2_Apply);
        v.findViewById(R.id.Cali_Ph_1PB_Option2_Stop).setOnClickListener(mClickListener);
        v.findViewById(R.id.Cali_Ph_1PB_Option2_Apply).setOnClickListener(mClickListener);

        DecimalFormat df2Figure = new DecimalFormat("0.00");
        edCurrent.setText( df2Figure.format(m_dPh));

        m_Ph1PointBufferSendThread = new Ph1PointBufferSendThread(true);
        m_UpdateCurrentValueThreadThread = new UpdateCurrentValueThread(true);
        m_UpdateCurrentValueThreadThread.start();
        return v;
    }

    public static SubActivityCaliPh1PointBuffer newInstance(String text) {
        SubActivityCaliPh1PointBuffer f = new SubActivityCaliPh1PointBuffer();
        Bundle b = new Bundle();
        b.putString("msg", text);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onStop() {
        if (m_Ph1PointBufferSendThread != null){
            m_Ph1PointBufferSendThread.stopPh1PointBufferSendThread();
        }
        super.onStop();
    }

    public void startPh1PointBufferSendThread(){
        if (!m_Ph1PointBufferSendThread.isAlive()){
            m_Ph1PointBufferSendThread = new Ph1PointBufferSendThread(true);
            m_Ph1PointBufferSendThread.start();
        }
        if (m_UpdateCurrentValueThreadThread.isAlive()){
            m_UpdateCurrentValueThreadThread.stopUpdateCurrentValueThread();
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
              else if (nCaliStatus == 2) tvHelpText.setText(getString(R.string.multi_cal_status_2));
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
              String strValue = edCurrent.getText().toString();
              String strLocation = getString(R.string.multi_1point_buffer);
              FileCalibrationWriter("PH_Cali", strLocation, strValue, strResult);
          }
      }
    }
    Button.OnClickListener mClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.Cali_Ph_1PB_Option2_Stop :
                     m_nCaliStatus = 16;
                     FileLogWriter(getString(R.string.multi_cal_ph_1p_buffer),"Click Stop");
                    break;
                case R.id.Cali_Ph_1PB_Option2_Apply:
                      m_DataCommunicationThread.stopDCThread();

                      byte[] btData = new byte[3];
                      btData[0] = 0x01; // 센서타입
                      btData[1] = 0x00; // Cal 타입
                      btData[2] = 0x30; // Value
                      if (!SendProtocol_Get(4,(byte)0xA7, btData)){
                        Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                        return;
                      }

                      m_nCaliStatus = 1;
                      startPh1PointBufferSendThread();
                      btApply.setEnabled(FALSE);
                      DisplayHelpText(m_nCaliStatus);
                      FileLogWriter(getString(R.string.multi_cal_ph_1p_buffer),"Click Apply");
                    break ;
            }
        }
    };


    class Ph1PointBufferSendThread extends Thread {
        private boolean isPlay = false;
        private int nCount = 0;
        public Ph1PointBufferSendThread(boolean isPlay) {this.isPlay = isPlay;}
        public void stopPh1PointBufferSendThread(){
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
            //        SendProtocol((byte)0x67, 1, "4", m_strMode);
                    if (m_nCaliStatus != 1)
                    {
                        stopPh1PointBufferSendThread();
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
