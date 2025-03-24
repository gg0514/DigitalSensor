package com.example.kyj.staqua;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.text.DecimalFormat;

import static com.example.kyj.staqua.MainActivity.DataCommunicationThread.nTimer;
import static com.example.kyj.staqua.MainActivity.FileLogWriter;
import static com.example.kyj.staqua.MainActivity.SendProtocol_Get;
import static com.example.kyj.staqua.MainActivity.SendProtocol_Set;
import static com.example.kyj.staqua.MainActivity.m_bTuAutoWash;
import static com.example.kyj.staqua.MainActivity.m_bTuManualWash;
import static com.example.kyj.staqua.MainActivity.m_bTuWashChange;
import static com.example.kyj.staqua.MainActivity.m_dTuHighWash;
import static com.example.kyj.staqua.MainActivity.m_dTuLowWash;
import static com.example.kyj.staqua.MainActivity.m_nTuDuration;
import static com.example.kyj.staqua.MainActivity.m_nTuInterval;
import static com.example.kyj.staqua.MainActivity.m_nTuStabilization;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Double.parseDouble;
/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivityWashTu extends Activity {

    private static SubActivityWashTu.UpdateCheckThread m_UpdateCheckThread;

    private EditText edInteval;
    private EditText edDuration;
    private EditText edStabilization;
    private EditText edHigh;
    private EditText edLow;

    private Button btManual;
   // private Button btHoldMeasurement;

    private CheckBox rbtAuto;

    private String m_strInterval = "";
    private String m_strDuration = "";
    private String m_strStabilization = "";
    private String m_strHigh = "";
    private String m_strLow = "";


    DecimalFormat df2Figure = new DecimalFormat("0.00");
    View.OnFocusChangeListener mFocuslistener;
    final Context thisContext = this;



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sub_activity_wash_tu);
        super.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        edInteval = (EditText)findViewById(R.id.Wash_Tu_Interval_Ed);
        edDuration = (EditText)findViewById(R.id.Wash_Tu_Duration_Ed);
        edStabilization = (EditText)findViewById(R.id.Wash_Tu_Stabilization_Ed);
        edHigh = (EditText)findViewById(R.id.Wash_Tu_Auto_Ed_High);
        edLow = (EditText)findViewById(R.id.Wash_Tu_Auto_Ed_Low);

        btManual = (Button)findViewById(R.id.Wash_Tu_Manual_Btn);
       // btHoldMeasurement = (Button)findViewById(R.id.Wash_Tu_Hold_Measurement_Btn);

        rbtAuto = (CheckBox)findViewById(R.id.Wash_Tu_Auto_Btn_Auto);

        edInteval.setOnFocusChangeListener(meClickListener);
        edDuration.setOnFocusChangeListener(meClickListener);
        edStabilization.setOnFocusChangeListener(meClickListener);
        edHigh.setOnFocusChangeListener(meClickListener);
        edLow.setOnFocusChangeListener(meClickListener);

        WashTextWatcher wIntevalWatcher = new WashTextWatcher(edInteval);
        edInteval.addTextChangedListener(wIntevalWatcher);
        WashTextWatcher wDurationWatcher = new WashTextWatcher(edDuration);
        edDuration.addTextChangedListener(wDurationWatcher);
        WashTextWatcher wStabilizationWatcher = new WashTextWatcher(edStabilization);
        edStabilization.addTextChangedListener(wStabilizationWatcher);
        WashTextWatcher wHighWatcher = new WashTextWatcher(edHigh);
        edHigh.addTextChangedListener(wHighWatcher);
        WashTextWatcher wLowWatcher = new WashTextWatcher(edLow);
        edLow.addTextChangedListener(wLowWatcher);

        findViewById(R.id.Wash_Tu_Main).setOnClickListener(mvClickListener);

        findViewById(R.id.Wash_Tu_Manual_Btn).setOnClickListener(mClickListener);
      //  findViewById(R.id.Wash_Tu_Hold_Measurement_Btn).setOnClickListener(mClickListener);
        findViewById(R.id.Wash_Tu_Option1_Apply).setOnClickListener(mClickListener);
        findViewById(R.id.Wash_Tu_Option1_Load).setOnClickListener(mClickListener);

        if (m_bTuManualWash){
            btManual.setBackgroundResource(R.drawable.my_button_green);
            btManual.setText(R.string.multi_on);
        }
        else {
            btManual.setBackgroundResource(R.drawable.my_button_base);
            btManual.setText(R.string.multi_off);
        }
        if (!m_bTuManualWash) {
            btManual.setBackgroundResource(R.drawable.my_button_green);
            btManual.setText(R.string.multi_on);
        }
        else {
            btManual.setBackgroundResource(R.drawable.my_button_base);
            btManual.setText(R.string.multi_off);
        }

        nTimer = 1500;
    //    SendProtocol((byte)0x11, 1, "0", m_strMode);

        m_UpdateCheckThread = new UpdateCheckThread(true);
        m_UpdateCheckThread.start();

        FileLogWriter(getString(R.string.multi_wash_tu),"Start Activity");
    }

    @Override
    public void onStop() {

        if (m_UpdateCheckThread.isAlive()){
            m_UpdateCheckThread.stopUpdateCheckThread();
        }

        nTimer = 800;
    //    SendProtocol((byte)0x11, 1, "0", m_strMode);
        FileLogWriter(getString(R.string.multi_wash_tu),"Stop Activity");
        super.onStop();
    }

//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.sub_activity_wash_tu, container, false);
//
//        return v;
//    }

    public static SubActivityWashTu newInstance(String text) {
        SubActivityWashTu f = new SubActivityWashTu();
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

    private class WashTextWatcher implements TextWatcher {
        EditText et;
        String beforeText;
        public WashTextWatcher(EditText et){
            this.et = et;
        }
        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            if (!isStringDouble(s.toString())) return;
            if(s.toString().length() > 0){
                if ((et.getId() == R.id.Wash_Tu_Interval_Ed) || (et.getId() == R.id.Wash_Tu_Duration_Ed) || (et.getId() == R.id.Wash_Tu_Stabilization_Ed))
                {
                    if((parseDouble(s.toString()) > 50000) || (parseDouble(s.toString()) < 0)){
                        et.setText(beforeText);
                        Toast.makeText(thisContext, R.string.multi_wash_time_validation, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    }
                }
                else if ((et.getId() == R.id.Wash_Tu_Auto_Ed_High) || (et.getId() == R.id.Wash_Tu_Auto_Ed_Low))
                {
                    if((parseDouble(s.toString()) > 20) || (parseDouble(s.toString()) < 0)){
                        et.setText(beforeText);
                        Toast.makeText(thisContext, R.string.multi_tu_validation, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
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

    EditText.OnFocusChangeListener meClickListener  = new View.OnFocusChangeListener(){
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus)
            {
                switch(v.getId()){
                    case R.id.Wash_Tu_Duration_Ed:
                        edDuration.setText("");
                        break;
                    case R.id.Wash_Tu_Interval_Ed:
                        edInteval.setText("");
                        break;
                    case R.id.Wash_Tu_Stabilization_Ed:
                        edStabilization.setText("");
                        break;
                    case R.id.Wash_Tu_Auto_Ed_High:
                        edHigh.setText("");
                        break;
                    case R.id.Wash_Tu_Auto_Ed_Low:
                        edLow.setText("");
                        break;
                }
            }
        }
    };

    View.OnClickListener mvClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.Wash_Tu_Main:
                    InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    break;
            }
        }
    };


    Button.OnClickListener mClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
              SharedPreferences prefs;
            prefs = getSharedPreferences("Tu_Wash", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            switch (v.getId()) {
//                case R.id.Wash_Tu_Hold_Measurement_Btn:
//                    if (m_bHoldAll)
//                    {
//                        btHoldMeasurement.setBackgroundResource(R.drawable.my_button_base);
//                        btHoldMeasurement.setText(R.string.multi_off);
//                        m_bHoldAll = FALSE;
//                    }
//                    else
//                    {
//                        btHoldMeasurement.setBackgroundResource(R.drawable.my_button_green);
//                        btHoldMeasurement.setText(R.string.multi_on);
//                        m_bHoldAll = TRUE;
//                    }
//                    DisplayStatus();
//                    break;
                case R.id.Wash_Tu_Manual_Btn:
                    if (!m_bTuManualWash)
                    {
                        byte[] btData = new byte[2];
                        btData[0] = 0x02; // 센서타입
                        btData[1] = 0x01; // On
                        if (!SendProtocol_Get(3,(byte)0xA8, btData)){
                            Toast.makeText(thisContext, R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                            return;
                        }
                      //  SendProtocol((byte)0x54, 1, "0", m_strMode);
//                        SendWash(2);
//                        SystemClock.sleep(1000);
//                        SendWash(2);
                        btManual.setBackgroundResource(R.drawable.my_button_base);
                        btManual.setText(R.string.multi_off);
                        m_bTuManualWash = true;
                        editor.putString("ManualWash", "FALSE");
                        editor.commit();
                    }
                    else
                    {
                        byte[] btData = new byte[2];
                        btData[0] = 0x02; // 센서타입
                        btData[1] = 0x00; // Off
                        if (!SendProtocol_Get(3,(byte)0xA8, btData)){
                            Toast.makeText(thisContext, R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                            return;
                        }
                      //  SendProtocol((byte)0x53, 1, "0", m_strMode);
//                        SendWash(3);
//                        SystemClock.sleep(1000);
//                        SendWash(3);
                        btManual.setBackgroundResource(R.drawable.my_button_green);
                        btManual.setText(R.string.multi_on);
                        m_bTuManualWash = false;
                        editor.putString("ManualWash", "TRUE");
                        editor.commit();
                    }
                    break;
                case R.id.Wash_Tu_Option1_Apply:
                    m_nTuDuration = Integer.parseInt(edDuration.getText().toString());
                    m_nTuInterval = Integer.parseInt(edInteval.getText().toString());
                    m_nTuStabilization = Integer.parseInt(edStabilization.getText().toString());
                    m_dTuHighWash = Double.parseDouble(edHigh.getText().toString());
                    m_dTuLowWash = Double.parseDouble(edLow.getText().toString());
                    if (rbtAuto.isChecked())  {
                        m_bTuAutoWash = TRUE;
                        editor.putString("Auto", "TRUE");
                    }
                    else {
                        m_bTuAutoWash = FALSE;
                        editor.putString("Auto","FALSE");
                    }
                    editor.putString("Duration", edDuration.getText().toString());
                    editor.putString("Interval", edInteval.getText().toString());
                    editor.putString("Stabilization", edStabilization.getText().toString());
                    editor.putString("High", edHigh.getText().toString());
                    editor.putString("Low", edLow.getText().toString());
                    editor.commit();



                    DecimalFormat df2Figure = new DecimalFormat("00.00");
                    DecimalFormat df3Figure = new DecimalFormat("0.000");
                    String strTuHighWash = "";
                    String strTuLowWash = "";
                    if (m_dTuHighWash >= 10) strTuHighWash = df2Figure.format( m_dTuHighWash );
                    else strTuHighWash = df3Figure.format( m_dTuHighWash );
                    if (m_dTuLowWash >= 10)  strTuLowWash = df3Figure.format( m_dTuLowWash );
                    else strTuLowWash = df3Figure.format( m_dTuLowWash );

                    String strPayload1 = strTuHighWash+" "+strTuLowWash+" ";
                    int nLength = strPayload1.length();
                    byte[] btValue = strPayload1.getBytes();

                    byte[] btData = new byte[8+nLength];
                    byte[] btTuDuration = new byte[2];
                    byte[] btTuInterval = new byte[2];
                    byte[] btTuStabilization = new byte[2];

                    btTuDuration=IntTo2Byte(m_nTuDuration);
                    btTuInterval=IntTo2Byte(m_nTuInterval);
                    btTuStabilization=IntTo2Byte(m_nTuStabilization);

                    btData[0] = 0x01; // 센서타입
                    btData[1] = btTuDuration[0];
                    btData[2] = btTuDuration[1];
                    btData[3] = btTuInterval[0];
                    btData[4] = btTuInterval[1];
                    btData[5] = btTuStabilization[0];
                    btData[6] = btTuStabilization[1];
                    if (m_bTuAutoWash) btData[7] =  0x01;
                    else btData[7] =  0x00;
                    System.arraycopy(btValue, 0, btData, 8, nLength);

                    if (!SendProtocol_Set( nLength+9, (byte)0xC4, btData)){
                        Toast.makeText(thisContext, R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                        return;
                    }
                    Toast.makeText(thisContext, R.string.multi_applied, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.

//                    String strPayload1 = edDuration.getText().toString()+" "+edInteval.getText().toString()+" "+edStabilization.getText().toString()+" "+strAutoWash+" "+edHigh.getText().toString()+" "+edLow.getText().toString()+" ";
//                    int nLength = strPayload1.length();
//                    byte[] btValue = strPayload1.getBytes();
//                    if (!SendProtocol_Set( nLength+1, (byte)0xC6, btValue)){
//                        Toast.makeText(thisContext, R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
//                        return;
//                    }
                    break;

                case R.id.Wash_Tu_Option1_Load:
                    byte[] btData1 = new byte[2];
                    btData1[0] = 0x01; // 센서타입
                    if (!SendProtocol_Get(2,(byte)0xAE, btData1)){
                        Toast.makeText(thisContext, R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                        return;
                    }
                    break;

            }
        }
    };

    public static byte[] IntTo2Byte(int value) {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte)(value >> 8);
        byteArray[1] = (byte)(value);
        return byteArray;
    }

    class UpdateCheckThread extends Thread {
        private boolean isPlay = false;
        public UpdateCheckThread(boolean isPlay) {this.isPlay = isPlay;}
        public void stopUpdateCheckThread(){
            isPlay = !isPlay;
        }
        @Override
        public void run() {
            super.run();
            while (isPlay) {
                try {
                    Thread.sleep(1000);
                    if (m_bTuWashChange == true) {
                        m_bTuWashChange = false;
                        mHandler.sendEmptyMessage(1);
                    }
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
            if (msg.what == 1)
            {
                edDuration.setText(String.valueOf(m_nTuDuration));
                edInteval.setText(String.valueOf(m_nTuInterval));
                edStabilization.setText(String.valueOf(m_nTuStabilization));
                edHigh.setText(String.valueOf(m_dTuHighWash));
                edLow.setText(String.valueOf(m_dTuLowWash));
                if (m_bTuAutoWash) rbtAuto.setChecked(TRUE);
                else  rbtAuto.setChecked(FALSE);
            }
        }
    };

}
