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
import static com.example.kyj.staqua.MainActivity.m_bChAutoWash;
import static com.example.kyj.staqua.MainActivity.m_bChManualWash;
import static com.example.kyj.staqua.MainActivity.m_bChWashChange;
import static com.example.kyj.staqua.MainActivity.m_dChHighWash;
import static com.example.kyj.staqua.MainActivity.m_dChLowWash;
import static com.example.kyj.staqua.MainActivity.m_nChDuration;
import static com.example.kyj.staqua.MainActivity.m_nChInterval;
import static com.example.kyj.staqua.MainActivity.m_nChStabilization;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Double.parseDouble;
/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivityWashCh extends Activity {

    private static SubActivityWashCh.UpdateCheckThread m_UpdateCheckThread;
    private EditText edInteval;
    private EditText edDuration;
    private EditText edStabilization;
    private EditText edHigh;
    private EditText edLow;

    private Button btManual;
  //  private Button btHoldMeasurement;

    private CheckBox rbtAuto;

    private String m_strInterval = "";
    private String m_strDuration = "";
    private String m_strStabilization = "";
    private String m_strHigh = "";
    private String m_strLow = "";

    SharedPreferences prefs;
    DecimalFormat df2Figure = new DecimalFormat("0.00");
    View.OnFocusChangeListener mFocuslistener;
    final Context thisContext = this;



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sub_activity_wash_ch);
        super.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        edInteval = (EditText)findViewById(R.id.Wash_Ch_Interval_Ed);
        edDuration = (EditText)findViewById(R.id.Wash_Ch_Duration_Ed);
        edStabilization = (EditText)findViewById(R.id.Wash_Ch_Stabilization_Ed);
        edHigh = (EditText)findViewById(R.id.Wash_Ch_Auto_Ed_High);
        edLow = (EditText)findViewById(R.id.Wash_Ch_Auto_Ed_Low);

        btManual = (Button)findViewById(R.id.Wash_Ch_Manual_Btn);
       // btHoldMeasurement = (Button)findViewById(R.id.Wash_Ch_Hold_Measurement_Btn);

        rbtAuto = (CheckBox)findViewById(R.id.Wash_Ch_Auto_Btn_Auto);

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

        findViewById(R.id.Wash_Ch_Main).setOnClickListener(mvClickListener);

        findViewById(R.id.Wash_Ch_Manual_Btn).setOnClickListener(mClickListener);
      //  findViewById(R.id.Wash_Ch_Hold_Measurement_Btn).setOnClickListener(mClickListener);
        findViewById(R.id.Wash_Ch_Option1_Apply).setOnClickListener(mClickListener);
        findViewById(R.id.Wash_Ch_Option1_Load).setOnClickListener(mClickListener);

        btManual.setBackgroundResource(R.drawable.my_button_green);
        btManual.setText(R.string.multi_on);
        nTimer = 1500;

        if (!m_bChManualWash) {
            btManual.setBackgroundResource(R.drawable.my_button_green);
            btManual.setText(R.string.multi_on);
        }
        else {
            btManual.setBackgroundResource(R.drawable.my_button_base);
            btManual.setText(R.string.multi_off);
        }


        m_UpdateCheckThread = new UpdateCheckThread(true);
        m_UpdateCheckThread.start();
        FileLogWriter(getString(R.string.multi_wash_ch),"Start Activity");
    }

    @Override
    public void onStop() {

        if (m_UpdateCheckThread.isAlive()){
            m_UpdateCheckThread.stopUpdateCheckThread();
        }

        nTimer = 800;
   //     SendProtocol((byte)0x11, 1, "0", m_strMode);
        FileLogWriter(getString(R.string.multi_wash_ch),"Stop Activity");
        super.onStop();
    }

//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.sub_activity_wash_ch, container, false);
//
//        return v;
//    }

    public static SubActivityWashCh newInstance(String text) {
        SubActivityWashCh f = new SubActivityWashCh();
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
                if ((et.getId() == R.id.Wash_Ch_Interval_Ed) || (et.getId() == R.id.Wash_Ch_Duration_Ed) || (et.getId() == R.id.Wash_Ch_Stabilization_Ed))
                {
                    if((parseDouble(s.toString()) > 50000) || (parseDouble(s.toString()) < 0)){
                        et.setText(beforeText);
                        Toast.makeText(thisContext, R.string.multi_wash_time_validation, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    }
                }
                else if ((et.getId() == R.id.Wash_Ch_Auto_Ed_High) || (et.getId() == R.id.Wash_Ch_Auto_Ed_Low))
                {
                    if((parseDouble(s.toString()) > 2) || (parseDouble(s.toString()) < 0)){
                        et.setText(beforeText);
                        Toast.makeText(thisContext, R.string.multi_ch_validation, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
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
                    case R.id.Wash_Ch_Duration_Ed:
                        edDuration.setText("");
                        break;
                    case R.id.Wash_Ch_Interval_Ed:
                        edInteval.setText("");
                        break;
                    case R.id.Wash_Ch_Stabilization_Ed:
                        edStabilization.setText("");
                        break;
                    case R.id.Wash_Ch_Auto_Ed_High:
                        edHigh.setText("");
                        break;
                    case R.id.Wash_Ch_Auto_Ed_Low:
                        edLow.setText("");
                        break;
                }
            }
        }
    };

    View.OnClickListener mvClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.Wash_Ch_Main:
                    InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    break;
            }
        }
    };


    Button.OnClickListener mClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            SharedPreferences prefs;
            prefs = getSharedPreferences("Ch_Wash", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            switch (v.getId()) {
//                case R.id.Wash_Ch_Hold_Measurement_Btn:
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
                case R.id.Wash_Ch_Manual_Btn:
                    if (!m_bChManualWash)
                    {
                        byte[] btData = new byte[2];
                        btData[0] = 0x03; // 센서타입
                        btData[1] = 0x01; // On
                        if (!SendProtocol_Get(3,(byte)0xA8, btData)){
                            Toast.makeText(thisContext, R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                            return;
                        }

                      //  SendProtocol((byte)0x56, 1, "0", m_strMode);
//                        SendWash(0);
//                        SystemClock.sleep(1000);
//                        SendWash(0);
                        btManual.setBackgroundResource(R.drawable.my_button_base);
                        btManual.setText(R.string.multi_off);
                        m_bChManualWash = true;
                        editor.putString("ManualWash", "FALSE");
                        editor.commit();
                    }
                    else
                    {
                        byte[] btData = new byte[2];
                        btData[0] = 0x03; // 센서타입
                        btData[1] = 0x00; // OFF
                        if (!SendProtocol_Get(3,(byte)0xA8, btData)){
                            Toast.makeText(thisContext, R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                            return;
                        }
                      //  SendProtocol((byte)0x55, 1, "0", m_strMode);
//                        SendWash(1);
//                        SystemClock.sleep(1000);
//                        SendWash(1);
                        btManual.setBackgroundResource(R.drawable.my_button_green);
                        btManual.setText(R.string.multi_on);
                        m_bChManualWash = false;
                        editor.putString("ManualWash", "TRUE");
                        editor.commit();
                    }
                    break;
                case R.id.Wash_Ch_Option1_Apply:
                    m_nChDuration = Integer.parseInt(edDuration.getText().toString());
                    m_nChInterval = Integer.parseInt(edInteval.getText().toString());
                    m_nChStabilization = Integer.parseInt(edStabilization.getText().toString());
                    m_dChHighWash = Double.parseDouble(edHigh.getText().toString());
                    m_dChLowWash = Double.parseDouble(edLow.getText().toString());
                    if (rbtAuto.isChecked())  {
                        m_bChAutoWash = TRUE;
                        editor.putString("Auto", "TRUE");
                    }
                    else {
                        m_bChAutoWash = FALSE;
                        editor.putString("Auto","FALSE");
                    }
                    editor.putString("Duration", edDuration.getText().toString());
                    editor.putString("Interval", edInteval.getText().toString());
                    editor.putString("Stabilization", edStabilization.getText().toString());
                    editor.putString("High", edHigh.getText().toString());
                    editor.putString("Low", edLow.getText().toString());
                    editor.commit();



                    DecimalFormat df3Figure = new DecimalFormat("0.000");
                    DecimalFormat df2Figure = new DecimalFormat("00.00");
                    String strChHighWash = "";
                    if (m_dChHighWash >= 10) strChHighWash  = df2Figure.format( m_dChHighWash );
                    else strChHighWash  = df3Figure.format( m_dChHighWash );

                    String strChLowWash = "";
                    if (m_dChHighWash >= 10) strChLowWash  = df2Figure.format( m_dChLowWash );
                    else strChLowWash  = df3Figure.format( m_dChLowWash );

                    String strPayload1 = strChHighWash+" "+strChLowWash+" ";
                    int nLength = strPayload1.length();
                    byte[] btValue = strPayload1.getBytes();

                    byte[] btData = new byte[8+nLength];
                    byte[] btChDuration = new byte[2];
                    byte[] btChInterval = new byte[2];
                    byte[] btChStabilization = new byte[2];

                    btChDuration=IntTo2Byte(m_nChDuration);
                    btChInterval=IntTo2Byte(m_nChInterval);
                    btChStabilization=IntTo2Byte(m_nChStabilization);


                    btData[0] = 0x00; // 센서타입
                    btData[1] = btChDuration[0];
                    btData[2] = btChDuration[1];
                    btData[3] = btChInterval[0];
                    btData[4] = btChInterval[1];
                    btData[5] = btChStabilization[0];
                    btData[6] = btChStabilization[1];
                    if (m_bChAutoWash) btData[7] =  0x01;
                    else btData[7] =  0x00;
                    System.arraycopy(btValue, 0, btData, 8, nLength);

                    if (!SendProtocol_Set( nLength+9, (byte)0xC4, btData)){
                        Toast.makeText(thisContext, R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                        return;
                    }
                    Toast.makeText(thisContext, R.string.multi_applied, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.

                    break;
                case R.id.Wash_Ch_Option1_Load:
                    byte[] btData1 = new byte[2];
                    btData1[0] = 0x00; // 센서타입
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
                    if (m_bChWashChange == true) {
                        m_bChWashChange = false;
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
                edDuration.setText(String.valueOf(m_nChDuration));
                edInteval.setText(String.valueOf(m_nChInterval));
                edStabilization.setText(String.valueOf(m_nChStabilization));
                edHigh.setText(String.valueOf(m_dChHighWash));
                edLow.setText(String.valueOf(m_dChLowWash));
                if (m_bChAutoWash) rbtAuto.setChecked(TRUE);
                else  rbtAuto.setChecked(FALSE);
            }
        }
    };

}
