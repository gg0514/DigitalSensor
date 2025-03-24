package com.example.kyj.staqua;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import static com.example.kyj.staqua.MainActivity.m_dCh;
import static com.example.kyj.staqua.MainActivity.m_dCo;
import static com.example.kyj.staqua.MainActivity.m_dMvCh;
import static com.example.kyj.staqua.MainActivity.m_dMvCo;
import static com.example.kyj.staqua.MainActivity.m_dMvPh;
import static com.example.kyj.staqua.MainActivity.m_dMvTu;
import static com.example.kyj.staqua.MainActivity.m_dPh;
import static com.example.kyj.staqua.MainActivity.m_dTemp;
import static com.example.kyj.staqua.MainActivity.m_dTu;
import static com.example.kyj.staqua.MainActivity.m_strCh;
import static com.example.kyj.staqua.MainActivity.m_strCo;
import static com.example.kyj.staqua.MainActivity.m_strPh;
import static com.example.kyj.staqua.MainActivity.m_strTemp;
import static com.example.kyj.staqua.MainActivity.m_strTu;

/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivityDiagnoSensor extends Fragment {
    private SubActivityDiagnoSensor.DiagnoSensorThread m_DiagnoSensorThread;
//    private SubActivityDiagnoSensor.DiagnoSensorSendThread m_DiagnoSensorSendThread;

//    private ArrayList<String> mList;
//    private ListView mListView;;
//    private ArrayAdapter m_Adapter;
//
//    mList = new ArrayList<>();
//    mListView= (ListView) v.findViewById(R.id.Diagno_SD_List_List);
//    m_Adapter =  new ArrayAdapter(getActivity(), Android.R.io.list, mList);
//    mList.add(Double.toString(m_dCo));
//    mListView.setAdapter(m_Adapter);




    TextView    txLane1_Sensor;
    TextView    txLane2_Sensor;
    TextView    txLane3_Sensor;
    TextView    txLane4_Sensor;
    TextView    txLane5_Sensor;

    TextView    txLane1_Thresholds_Value;
    TextView    txLane2_Thresholds_Value;
    TextView    txLane3_Thresholds_Value;
    TextView    txLane4_Thresholds_Value;
    TextView    txLane5_Thresholds_Value;

    TextView    txLane1_Thresholds_Mv;
    TextView    txLane2_Thresholds_Mv;
    TextView    txLane3_Thresholds_Mv;
    TextView    txLane4_Thresholds_Mv;
    TextView    txLane5_Thresholds_Mv;

    TextView    txLane1_Confirmation_Value;
    TextView    txLane2_Confirmation_Value;
    TextView    txLane3_Confirmation_Value;
    TextView    txLane4_Confirmation_Value;
    TextView    txLane5_Confirmation_Value;

    TextView    txLane1_Confirmation_Mv;
    TextView    txLane2_Confirmation_Mv;
    TextView    txLane3_Confirmation_Mv;
    TextView    txLane4_Confirmation_Mv;
    TextView    txLane5_Confirmation_Mv;

    TextView    txLane1_Diagnosis;
    TextView    txLane2_Diagnosis;
    TextView    txLane3_Diagnosis;
    TextView    txLane4_Diagnosis;
    TextView    txLane5_Diagnosis;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_diagno_sensordiagnosis, container, false);

        v.findViewById(R.id.Diagno_SD_Lane_DetailDiagnosis).setOnClickListener(mClickListener);

        txLane1_Sensor= (TextView)v.findViewById(R.id.Diagno_SD_Lane1_Sensor);
        txLane2_Sensor= (TextView)v.findViewById(R.id.Diagno_SD_Lane2_Sensor);
        txLane3_Sensor= (TextView)v.findViewById(R.id.Diagno_SD_Lane3_Sensor);
        txLane4_Sensor= (TextView)v.findViewById(R.id.Diagno_SD_Lane4_Sensor);
        txLane5_Sensor= (TextView)v.findViewById(R.id.Diagno_SD_Lane5_Sensor);

        txLane1_Thresholds_Value = (TextView)v.findViewById(R.id.Diagno_SD_Lane1_Thresholds_Value);
        txLane2_Thresholds_Value= (TextView)v.findViewById(R.id.Diagno_SD_Lane2_Thresholds_Value);
        txLane3_Thresholds_Value= (TextView)v.findViewById(R.id.Diagno_SD_Lane3_Thresholds_Value);
        txLane4_Thresholds_Value= (TextView)v.findViewById(R.id.Diagno_SD_Lane4_Thresholds_Value);
        txLane5_Thresholds_Value= (TextView)v.findViewById(R.id.Diagno_SD_Lane5_Thresholds_Value);

        txLane1_Thresholds_Mv = (TextView)v.findViewById(R.id.Diagno_SD_Lane1_Thresholds_Mv);
        txLane2_Thresholds_Mv = (TextView)v.findViewById(R.id.Diagno_SD_Lane2_Thresholds_Mv);
        txLane3_Thresholds_Mv = (TextView)v.findViewById(R.id.Diagno_SD_Lane3_Thresholds_Mv);
        txLane4_Thresholds_Mv= (TextView)v.findViewById(R.id.Diagno_SD_Lane4_Thresholds_Mv);
        txLane5_Thresholds_Mv= (TextView)v.findViewById(R.id.Diagno_SD_Lane5_Thresholds_Mv);

        txLane1_Confirmation_Value = (TextView)v.findViewById(R.id.Diagno_SD_Lane1_Confirmation_Value);
        txLane2_Confirmation_Value = (TextView)v.findViewById(R.id.Diagno_SD_Lane2_Confirmation_Value);
        txLane3_Confirmation_Value = (TextView)v.findViewById(R.id.Diagno_SD_Lane3_Confirmation_Value);
        txLane4_Confirmation_Value = (TextView)v.findViewById(R.id.Diagno_SD_Lane4_Confirmation_Value);
        txLane5_Confirmation_Value = (TextView)v.findViewById(R.id.Diagno_SD_Lane5_Confirmation_Value);

        txLane1_Confirmation_Mv = (TextView)v.findViewById(R.id.Diagno_SD_Lane1_Confirmation_Mv);
        txLane2_Confirmation_Mv = (TextView)v.findViewById(R.id.Diagno_SD_Lane2_Confirmation_Mv);
        txLane3_Confirmation_Mv = (TextView)v.findViewById(R.id.Diagno_SD_Lane3_Confirmation_Mv);
        txLane4_Confirmation_Mv = (TextView)v.findViewById(R.id.Diagno_SD_Lane4_Confirmation_Mv);
        txLane5_Confirmation_Mv = (TextView)v.findViewById(R.id.Diagno_SD_Lane5_Confirmation_Mv);

        txLane1_Diagnosis = (TextView)v.findViewById(R.id.Diagno_SD_Lane1_Diagnosis);
        txLane2_Diagnosis = (TextView)v.findViewById(R.id.Diagno_SD_Lane2_Diagnosis);
        txLane3_Diagnosis = (TextView)v.findViewById(R.id.Diagno_SD_Lane3_Diagnosis);
        txLane4_Diagnosis = (TextView)v.findViewById(R.id.Diagno_SD_Lane4_Diagnosis);
        txLane5_Diagnosis = (TextView)v.findViewById(R.id.Diagno_SD_Lane5_Diagnosis);

      //  SendProtocol((byte)0x01, 1, "0", m_strMode);
      //  SystemClock.sleep(200);

    //    m_DiagnoSensorSendThread = new DiagnoSensorSendThread(true);
    //    m_DiagnoSensorSendThread.start();
        m_DiagnoSensorThread = new DiagnoSensorThread(true);
        m_DiagnoSensorThread.start();
        return v;
    }


//    @Override
//    public void onPause() {
//        if (m_DiagnoSensorThread != null) {
//            m_DiagnoSensorThread.stopDiagnoSensorThread();
//        }
//        if (m_DiagnoSensorSendThread != null){
//            m_DiagnoSensorSendThread.stopDiagnoSensorSendThread();
//        }
//        nTimer = 800;
//        super.onPause();
//    }

    @Override
    public void onStop() {
        if (m_DiagnoSensorThread != null) {
            m_DiagnoSensorThread.stopDiagnoSensorThread();
        }
//        if (m_DiagnoSensorSendThread != null){
//            m_DiagnoSensorSendThread.stopDiagnoSensorSendThread();
//        }
       // nTimer = 800;
        super.onStop();
    }


    public static SubActivityDiagnoSensor newInstance(String text) {

        SubActivityDiagnoSensor f = new SubActivityDiagnoSensor();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    class DiagnoSensorThread extends Thread {
        private boolean isPlay = false;
        public DiagnoSensorThread(boolean isPlay) {this.isPlay = isPlay;}
        public void stopDiagnoSensorThread(){
            isPlay = !isPlay;
        }
        @Override
        public void run() {
            super.run();
            while (isPlay) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandler.sendEmptyMessage(1);
            }
        }
    }

    private final Handler mHandler = new Handler() { //핸들러를 통해 UI스레드에 접근한다.
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String strNo ="";
            String strSensor="";
            String strValue="";
            String strMv="";
            if (msg.what == 1) {
                strSensor = m_strPh;
                txLane1_Sensor.setText(strSensor);
                strValue =Double.toString(m_dPh);
                txLane1_Confirmation_Value.setText(strValue);
                txLane1_Thresholds_Value.setText(strValue);
                strMv =Double.toString(m_dMvPh);
                txLane1_Confirmation_Mv.setText(strMv);
                txLane1_Thresholds_Mv.setText(strMv);

                strSensor = m_strCh;
                txLane2_Sensor.setText(strSensor);
                strValue =Double.toString(m_dCh);
                txLane2_Confirmation_Value.setText(strValue);
                txLane2_Thresholds_Value.setText(strValue);
                strMv =Double.toString(m_dMvCh);
                txLane2_Confirmation_Mv.setText(strMv);
                txLane2_Thresholds_Mv.setText(strMv);

                strSensor = m_strTu;
                txLane3_Sensor.setText(strSensor);
                strValue =Double.toString(m_dTu);
                txLane3_Confirmation_Value.setText(strValue);
                txLane3_Thresholds_Value.setText(strValue);
                strMv =Double.toString(m_dMvTu);
                txLane3_Confirmation_Mv.setText(strMv);
                txLane3_Thresholds_Mv.setText(strMv);

                strSensor = m_strCo;
                txLane4_Sensor.setText(strSensor);
                strValue =Double.toString(m_dCo);
                txLane4_Confirmation_Value.setText(strValue);
                txLane4_Thresholds_Value.setText(strValue);
                strMv =Double.toString(m_dMvCo);
                txLane4_Confirmation_Mv.setText(strMv);
                txLane4_Thresholds_Mv.setText(strMv);

                strSensor = m_strTemp;
                txLane5_Sensor.setText(strSensor);
                strValue =Double.toString(m_dTemp);
                txLane5_Confirmation_Value.setText(strValue);
                txLane5_Thresholds_Value.setText(strValue);
               // strMv =Double.toString(m_dMvTemp);
//                txLane5_Confirmation_Value.setText(strValue);
//                txLane5_Thresholds_Value.setText(strValue);

            }
            else if (msg.what == 2) {
                //  txLane2.setText(m_strLane2);
            }
        }
    };

    Button.OnClickListener mClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.Diagno_SD_Lane_DetailDiagnosis:
                    Intent intent = new Intent(getActivity(), SubActivityDiagnoSensorDetail.class); // 다음 넘어갈 클래스 지정
                    startActivity(intent); // 다음 화면으로 넘어간다
                    break;

            }
        }
    };



//    class DiagnoSensorSendThread extends Thread {
//        private boolean isPlay = false;
//        public DiagnoSensorSendThread(boolean isPlay) {this.isPlay = isPlay;}
//        public void stopDiagnoSensorSendThread(){
//            isPlay = !isPlay;
//        }
//        @Override
//        public void run() {
//            super.run();
//            while (isPlay) {
//                try {
//                    Thread.sleep(3000);
//                    byte [] bDummy = new byte[1];
//                    bDummy[0] = 0x00;
//                    SendProtocol_Get(1,(byte)0xA5, bDummy);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }
//    }


}
