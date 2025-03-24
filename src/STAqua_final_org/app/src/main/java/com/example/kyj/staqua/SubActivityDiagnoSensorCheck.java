package com.example.kyj.staqua;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static com.example.kyj.staqua.MainActivity.m_bMvRead;
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
import static com.example.kyj.staqua.MainActivity.m_strChSerial;
import static com.example.kyj.staqua.MainActivity.m_strCo;
import static com.example.kyj.staqua.MainActivity.m_strCoSerial;
import static com.example.kyj.staqua.MainActivity.m_strPh;
import static com.example.kyj.staqua.MainActivity.m_strPhSerial;
import static com.example.kyj.staqua.MainActivity.m_strTemp;
import static com.example.kyj.staqua.MainActivity.m_strTempSerial;
import static com.example.kyj.staqua.MainActivity.m_strTu;
import static com.example.kyj.staqua.MainActivity.m_strTuSerial;

/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivityDiagnoSensorCheck extends Fragment {
    private SubActivityDiagnoSensorCheck.DiagnoSensorCheckThread m_DiagnoSensorThread;
 //   private SubActivityDiagnoSensorCheck.DiagnoSensorCheckSendThread m_DiagnoSensorSendThread;

//    private ArrayList<String> mList;
//    private ListView mListView;;
//    private ArrayAdapter m_Adapter;
//
//    mList = new ArrayList<>();
//    mListView= (ListView) v.findViewById(R.id.Diagno_SD_List_List);
//    m_Adapter =  new ArrayAdapter(getActivity(), Android.R.io.list, mList);
//    mList.add(Double.toString(m_dCo));
//    mListView.setAdapter(m_Adapter);


    TextView    txLane1_No;
    TextView    txLane2_No;
    TextView    txLane3_No;
    TextView    txLane4_No;
    TextView    txLane5_No;

    TextView    txLane1_Sensor;
    TextView    txLane2_Sensor;
    TextView    txLane3_Sensor;
    TextView    txLane4_Sensor;
    TextView    txLane5_Sensor;

    TextView    txLane1_Value;
    TextView    txLane2_Value;
    TextView    txLane3_Value;
    TextView    txLane4_Value;
    TextView    txLane5_Value;

    TextView    txLane1_Mv;
    TextView    txLane2_Mv;
    TextView    txLane3_Mv;
    TextView    txLane4_Mv;
    TextView    txLane5_Mv;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_diagno_sensorcheck, container, false);

        txLane1_No = (TextView)v.findViewById(R.id.Diagno_SC_Lane1_No);
        txLane2_No = (TextView)v.findViewById(R.id.Diagno_SC_Lane2_No);
        txLane3_No = (TextView)v.findViewById(R.id.Diagno_SC_Lane3_No);
        txLane4_No = (TextView)v.findViewById(R.id.Diagno_SC_Lane4_No);
        txLane5_No = (TextView)v.findViewById(R.id.Diagno_SC_Lane5_No);

        txLane1_Sensor= (TextView)v.findViewById(R.id.Diagno_SC_Lane1_Sensor);
        txLane2_Sensor= (TextView)v.findViewById(R.id.Diagno_SC_Lane2_Sensor);
        txLane3_Sensor= (TextView)v.findViewById(R.id.Diagno_SC_Lane3_Sensor);
        txLane4_Sensor= (TextView)v.findViewById(R.id.Diagno_SC_Lane4_Sensor);
        txLane5_Sensor= (TextView)v.findViewById(R.id.Diagno_SC_Lane5_Sensor);

        txLane1_Value= (TextView)v.findViewById(R.id.Diagno_SC_Lane1_Value);
        txLane2_Value= (TextView)v.findViewById(R.id.Diagno_SC_Lane2_Value);
        txLane3_Value= (TextView)v.findViewById(R.id.Diagno_SC_Lane3_Value);
        txLane4_Value= (TextView)v.findViewById(R.id.Diagno_SC_Lane4_Value);
        txLane5_Value= (TextView)v.findViewById(R.id.Diagno_SC_Lane5_Value);

        txLane1_Mv= (TextView)v.findViewById(R.id.Diagno_SC_Lane1_Mv);
        txLane2_Mv= (TextView)v.findViewById(R.id.Diagno_SC_Lane2_Mv);
        txLane3_Mv= (TextView)v.findViewById(R.id.Diagno_SC_Lane3_Mv);
        txLane4_Mv= (TextView)v.findViewById(R.id.Diagno_SC_Lane4_Mv);
        txLane5_Mv= (TextView)v.findViewById(R.id.Diagno_SC_Lane5_Mv);

      //  SendProtocol((byte)0x01, 1, "0", m_strMode);
      //  SystemClock.sleep(200);

     //   m_DiagnoSensorSendThread = new DiagnoSensorCheckSendThread(true);
     //   m_DiagnoSensorSendThread.start();
        m_DiagnoSensorThread = new DiagnoSensorCheckThread(true);
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
            m_DiagnoSensorThread.stopDiagnoSensorCheckThread();
        }
        m_bMvRead = false;
//        if (m_DiagnoSensorSendThread != null){
//            m_DiagnoSensorSendThread.stopDiagnoSensorCheckSendThread();
//        }
       // nTimer = 800;
        super.onStop();
    }


    public static SubActivityDiagnoSensorCheck newInstance(String text) {

        SubActivityDiagnoSensorCheck f = new SubActivityDiagnoSensorCheck();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    class DiagnoSensorCheckThread extends Thread {
        private boolean isPlay = false;
        public DiagnoSensorCheckThread(boolean isPlay) {this.isPlay = isPlay;}
        public void stopDiagnoSensorCheckThread(){
            isPlay = !isPlay;
        }
        @Override
        public void run() {
            super.run();
            while (isPlay) {
                try {
                    Thread.sleep(2000);
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
                strNo = m_strPhSerial;
                txLane1_No.setText(strNo);
                strSensor = m_strPh;
                txLane1_Sensor.setText(strSensor);
                strValue =Double.toString(m_dPh);
                txLane1_Value.setText(strValue);
                strMv =Double.toString(m_dMvPh);
                txLane1_Mv.setText(strMv);

                strNo = m_strChSerial;
                txLane2_No.setText(strNo);
                strSensor = m_strCh;
                txLane2_Sensor.setText(strSensor);
                strValue =Double.toString(m_dCh);
                txLane2_Value.setText(strValue);
                strMv =Double.toString(m_dMvCh);
                txLane2_Mv.setText(strMv);

                strNo = m_strTuSerial;
                txLane3_No.setText(strNo);
                strSensor = m_strTu;
                txLane3_Sensor.setText(strSensor);
                strValue =Double.toString(m_dTu);
                txLane3_Value.setText(strValue);
                strMv =Double.toString(m_dMvTu);
                txLane3_Mv.setText(strMv);

                strNo = m_strCoSerial;
                txLane4_No.setText(strNo);
                strSensor = m_strCo;
                txLane4_Sensor.setText(strSensor);
                strValue =Double.toString(m_dCo);
                txLane4_Value.setText(strValue);
                strMv =Double.toString(m_dMvCo);
                txLane4_Mv.setText(strMv);

                strNo = m_strTempSerial;
                txLane5_No.setText(strNo);
                strSensor = m_strTemp;
                txLane5_Sensor.setText(strSensor);
                strValue =Double.toString(m_dTemp);
                txLane5_Value.setText(strValue);
               // strMv =Double.toString(m_dMvTemp);
                txLane5_Mv.setText("");

            }
            else if (msg.what == 2) {
                //  txLane2.setText(m_strLane2);
            }
        }
    };


//    class DiagnoSensorCheckSendThread extends Thread {
//        private boolean isPlay = false;
//        public DiagnoSensorCheckSendThread(boolean isPlay) {this.isPlay = isPlay;}
//        public void stopDiagnoSensorCheckSendThread(){
//            isPlay = !isPlay;
//        }
//        @Override
//        public void run() {
//            super.run();
//            while (isPlay) {
//                try {
//                    Thread.sleep(2000);
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
