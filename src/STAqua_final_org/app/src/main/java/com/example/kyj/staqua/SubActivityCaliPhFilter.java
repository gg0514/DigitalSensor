package com.example.kyj.staqua;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.Toast;

import static com.example.kyj.staqua.MainActivity.FileLogWriter;
import static com.example.kyj.staqua.MainActivity.SendProtocol_Get;
import static com.example.kyj.staqua.MainActivity.SendProtocol_Set;
import static com.example.kyj.staqua.MainActivity.m_bFilterChange;
import static com.example.kyj.staqua.MainActivity.m_nFilterPh;
import static com.example.kyj.staqua.MainActivity.m_nPhFilterNumber;


public class SubActivityCaliPhFilter extends Fragment {

    private static SubActivityCaliPhFilter.UpdateCheckThread m_UpdateCheckThread;
    private EditText edCalibration;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_cali_ph_filter, container, false);

        edCalibration = (EditText)v.findViewById(R.id.Cali_Ph_Filter_DataFilter_Ed);
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

        FilterTextWatcher ftWatcher = new FilterTextWatcher(edCalibration);
        edCalibration.addTextChangedListener(ftWatcher);
        v.findViewById(R.id.Cali_Ph_Filter_Main).setOnClickListener(mClickListener);

    //    SendProtocol((byte)0x07, 1, "1", m_strMode);
        SystemClock.sleep(200);
        edCalibration.setText(Integer.toString(m_nPhFilterNumber));

        v.findViewById(R.id.Cali_Ph_Filter_Option1_Stop).setOnClickListener(mClickListener);
        v.findViewById(R.id.Cali_Ph_Filter_Option2_Apply).setOnClickListener(mClickListener);

        m_UpdateCheckThread = new UpdateCheckThread(true);
        m_UpdateCheckThread.start();


        return v;
    }

    public void onStop() {
        if (m_UpdateCheckThread.isAlive()){
            m_UpdateCheckThread.stopUpdateCheckThread();
        }
        super.onStop();
    }

    public static SubActivityCaliPhFilter newInstance(String text) {

        SubActivityCaliPhFilter f = new SubActivityCaliPhFilter();
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
                case R.id.Cali_Ph_Filter_Option1_Stop:
                    byte[] btData1 = new byte[1];
                    btData1[0] = 0x00; // 센서타입
                    if (!SendProtocol_Get(1,(byte)0xA4, btData1)){
                        Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                        return;
                    }
                    Toast.makeText(getActivity(), R.string.multi_applied, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    break ;
                case R.id.Cali_Ph_Filter_Option2_Apply:
                    int nFilter =  Integer.parseInt(edCalibration.getText().toString());
                    String strFilter = String.format("%03d", nFilter)+" ";
                    int nLength = strFilter.length();
                    byte[] btData = new byte[1+nLength];
                    byte[] btValue = strFilter.getBytes();
                    btData[0] = 0x01; // 센서타입
                    System.arraycopy(btValue, 0, btData, 1, nLength);
                    if (!SendProtocol_Set(6,(byte)0xC6, btData)){
                        Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                        return;
                    }
                    FileLogWriter(getString(R.string.multi_cal_ph_filter),"Click Apply");
                    Toast.makeText(getActivity(), R.string.multi_applied, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    break ;
            }
        }
    };

    private class FilterTextWatcher implements TextWatcher {
        EditText et;
        String beforeText;

        public FilterTextWatcher(EditText et){
            this.et = et;
        }
        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            if (!isStringDouble(s.toString())) return;
            if(s.toString().length() > 0){
                if((Integer.parseInt(s.toString()) > 127) || (Integer.parseInt(s.toString()) < 0)){
                    et.setText(beforeText);
                    Toast.makeText(getActivity(), R.string.multi_filter_validation, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
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
                    if (m_bFilterChange == true) {
                        m_bFilterChange = false;
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
                edCalibration.setText(String.valueOf(m_nFilterPh));
            }
        }
    };
}
