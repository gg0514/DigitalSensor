package com.example.kyj.staqua;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kyj.staqua.utils.AppUtils;
import com.example.kyj.staqua.utils.BlurView;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.example.kyj.staqua.MainActivity.FileLogWriter;
import static com.example.kyj.staqua.MainActivity.SendProtocol_Get;
import static com.example.kyj.staqua.MainActivity.SendProtocol_Set;
import static com.example.kyj.staqua.MainActivity.m_bHoldAll;
import static com.example.kyj.staqua.MainActivity.m_strBaudRate;
import static com.example.kyj.staqua.MainActivity.m_strDataBit;
import static com.example.kyj.staqua.MainActivity.m_strParity;
import static com.example.kyj.staqua.MainActivity.m_strSerialStandard;
import static com.example.kyj.staqua.MainActivity.m_strStation;
import static com.example.kyj.staqua.MainActivity.m_strStopBit;

/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivitySetupSerial extends Fragment {


    SharedPreferences prefs;

    private AlertDialog alert;
    private ArrayList<String> mList;
    private ListView mListView;;
    private ArrayAdapter mAdapter;

    private static Button btnHold;

    Spinner spSerial;
    Spinner spBaudRate;
    Spinner spStopBit;
    Spinner spParity;
    Spinner spDataBit;
 //   Spinner spProtocol ;
    EditText edStationNo;

//    private String m_strSerialStandard = "";
//    private String m_strBaudRate = "";
//    private String m_strStopBit = "";
//    private String m_strParity = "";
//    private String m_strStationNo = "";
//    private String m_strDataBit = "";
 //   private String m_strProtocol = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_setup_serial, container, false);

        edStationNo = (EditText)v.findViewById(R.id.Serial_StationNumber_Ed);

        spSerial = (Spinner) v.findViewById(R.id.Serial_SerialStandard_Sp);
        String[] optionLevel = getResources().getStringArray(R.array.serial_standard);
        SpinnerAdapter spASerial = new SpinnerAdapter(getActivity(),android.R.layout.simple_spinner_item, optionLevel);
        spSerial.setAdapter(spASerial);

        spBaudRate = (Spinner) v.findViewById(R.id.Serial_BaudRate_Sp);
        String[] optionLevel1 = getResources().getStringArray(R.array.baud_rate);
        SpinnerAdapter spABaudRate = new SpinnerAdapter(getActivity(),android.R.layout.simple_spinner_item, optionLevel1);
        spBaudRate.setAdapter(spABaudRate);

        spStopBit = (Spinner) v.findViewById(R.id.Serial_StopBit_Sp);
        String[] optionLevel3 = getResources().getStringArray(R.array.stop_bit);
        SpinnerAdapter spAStopBit= new SpinnerAdapter(getActivity(),android.R.layout.simple_spinner_item, optionLevel3);
        spStopBit.setAdapter(spAStopBit);

        spParity = (Spinner) v.findViewById(R.id.Serial_Parity_Sp);
        String[] optionLevel4 = getResources().getStringArray(R.array.parity);
        SpinnerAdapter spAParity= new SpinnerAdapter(getActivity(),android.R.layout.simple_spinner_item, optionLevel4);
        spParity.setAdapter(spAParity);

        spDataBit = (Spinner) v.findViewById(R.id.Serial_DataBit_Sp);
        String[] optionLevel5 = getResources().getStringArray(R.array.data_bit);
        SpinnerAdapter spADataBit= new SpinnerAdapter(getActivity(),android.R.layout.simple_spinner_item, optionLevel5);
        spDataBit.setAdapter(spADataBit);

        RefreshData();

        Button.OnClickListener onClickListener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
               final SharedPreferences.Editor editor = prefs.edit();
                AlertDialog.Builder d = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_TRADITIONAL);
                switch (view.getId()) {
//                    case R.id.Serial_Option1_Hold :
//                        if (m_bHoldAll)
//                        {
//                            d.setMessage(R.string.multi_start_measurement);
//                            d.setPositiveButton(R.string.multi_answer_cancel, new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.cancel();
//                                }
//                            });
//                            d.setNegativeButton(R.string.multi_answer_confirm, new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    // 각 센서 시리얼 번호 요청
//                              //      String strPayload1 = edSerial.getText().toString()+" ";
//                              //      int nLength = strPayload1.length();
//                                    byte[] btValue = new byte[1];
//                                    btValue[0] = (byte)0x00;
//                                    if (!SendProtocol_Set( 2, (byte)0xC0, btValue)){
//                                        Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
//                                        return;
//                                    }
//                                    m_bHoldAll = false;
//                                    btnHold.setBackgroundResource(R.drawable.my_button_base);
//                                    DisplayStatus();
//                                }
//                            });
//                            d.show();
//                        }
//                        else
//                        {
//                            d.setMessage(R.string.multi_stop_measurement);
//                            d.setPositiveButton(R.string.multi_answer_cancel, new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.cancel();
//                                }
//                            });
//                            d.setNegativeButton(R.string.multi_answer_confirm, new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    byte[] btValue = new byte[1];
//                                    btValue[0] = (byte)0x01;
//                                    if (!SendProtocol_Set( 2, (byte)0xC0, btValue)){
//                                        Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
//                                        return;
//                                    }
//                                    m_bHoldAll = true;
//                                    btnHold.setBackgroundResource(R.drawable.my_button_yellowgreen);
//                                    DisplayStatus();
//                                }
//                            });
//                            d.show();
//                        }
//                        FileLogWriter(getString(R.string.multi_setup_bluetooth),"Click Hold");
//                        break;
                    case R.id.Serial_Option2_Load :
                        byte [] bDummy = new byte[1];
                        bDummy[0] = 0x00;
                        if (!SendProtocol_Get( 1, (byte)0xB0, bDummy)){
                            Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                        }
                        RefreshData();
                        break;
                    case R.id.Serial_Option2_Apply:
                        d.setMessage(R.string.multi_change_communication);
                        d.setPositiveButton(R.string.multi_answer_yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                int nBaudRate =  Integer.parseInt(spBaudRate.getSelectedItem().toString());
                                String strBaudrate = String.format("%06d", nBaudRate);

                                String strParity ="";
                                if (spParity.getSelectedItem().toString().equals("None")) strParity = "0";
                                else if (spParity.getSelectedItem().toString().equals("Odd")) strParity = "1";
                                else if (spParity.getSelectedItem().toString().equals("Even")) strParity = "2";
                                else strParity = "0";
                                String strPayload1 = strBaudrate+" "+spStopBit.getSelectedItem().toString()+" "+strParity+" "+spDataBit.getSelectedItem().toString()+" "+edStationNo.getText().toString()+" ";
                                int nLength = strPayload1.length();
                                byte[] btValue = strPayload1.getBytes();
                                if (!SendProtocol_Set( nLength+1, (byte)0xC1, btValue)){
                                    Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                                    return;
                                }


                            }
                        });
                        d.setNegativeButton(R.string.multi_answer_no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        d.show();
                        break ;
                    case R.id.Serial_Option1_FactoryReset :
                        m_strSerialStandard = "SERIAL";
                        m_strBaudRate = "115200";
                        m_strStopBit = "1";
                        m_strParity = "None";
                        m_strStation = "1";
                        m_strDataBit = "8";
                    //    m_strProtocol = "FLOAT";
                        spSerial.setSelection(0);
                        spBaudRate.setSelection(0);
                        spStopBit.setSelection(0);
                        spParity.setSelection(0);
                        edStationNo.setText(m_strStation);
                        spDataBit.setSelection(0);
                  //      spProtocol.setSelection(0);
                        editor.putString("Serial", m_strSerialStandard);
                        editor.putString("Baud", m_strBaudRate);
                        editor.putString("Stop", m_strStopBit);
                        editor.putString("Parity", m_strParity);
                        editor.putString("Station", m_strStation);
                        editor.putString("Data", m_strDataBit);
                   //     editor.putString("Protocol", m_strProtocol);
                        editor.commit();
                        Toast.makeText(getActivity(), R.string.multi_factory_reset, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                        FileLogWriter(getString(R.string.multi_setup_serial),"Click Factory Reset");
                        break;

                }
            }
        } ;
//        Button btnHelp = (Button) v.findViewById(R.id.Serial_Option1_Help) ;
//        btnHelp.setOnClickListener(onClickListener) ;
        Button btnFactoryReset = (Button) v.findViewById(R.id.Serial_Option1_FactoryReset) ;
        btnFactoryReset.setOnClickListener(onClickListener) ;
        Button btnApply = (Button) v.findViewById(R.id.Serial_Option2_Apply) ;
        btnApply.setOnClickListener(onClickListener) ;
       // btnHold = (Button) v.findViewById(R.id.Serial_Option1_Hold) ;
       // btnHold.setOnClickListener(onClickListener) ;
        Button btnLoad = (Button) v.findViewById(R.id.Serial_Option2_Load) ;
        btnLoad.setOnClickListener(onClickListener) ;

        CheckBtnStatus();

        return v;
    }

    private void RefreshData() {
        prefs = getActivity().getSharedPreferences("Setup_Serial", MODE_PRIVATE);
        m_strSerialStandard = prefs.getString("Serial", "");
        if (m_strSerialStandard != "") {
            if (m_strSerialStandard.equals("Serial"))  spSerial.setSelection(0);
            else spSerial.setSelection(0);
        }
        else spSerial.setSelection(0);
        m_strBaudRate = prefs.getString("Baud", "");
        if (m_strBaudRate != "") {
            if (m_strBaudRate.equals("9600"))  spBaudRate.setSelection(0);
            else if (m_strBaudRate.equals("19200"))  spBaudRate.setSelection(1);
            else if (m_strBaudRate.equals("57600"))  spBaudRate.setSelection(2);
            else if (m_strBaudRate.equals("115200"))  spBaudRate.setSelection(3);
        }
        else spBaudRate.setSelection(3);
        m_strStopBit = prefs.getString("Stop", "");
        if (m_strStopBit != "") {
            if (m_strStopBit.equals("1"))  spStopBit.setSelection(0);
            else if (m_strStopBit.equals("2"))  spStopBit.setSelection(1);
        }
        else spStopBit.setSelection(0);
        m_strParity = prefs.getString("Parity", "");
        if (m_strParity != "") {
            if (m_strParity.equals("None"))  spParity.setSelection(0);
            else if (m_strParity.equals("Odd"))  spParity.setSelection(1);
            else if (m_strParity.equals("Even"))  spParity.setSelection(2);
        }
        else spParity.setSelection(0);
        m_strStation = prefs.getString("Station", "");
        if (m_strStation != "") {
            edStationNo.setText(m_strStation);
        }
        m_strDataBit = prefs.getString("Data", "");
        if (m_strDataBit != "") {
            if (m_strDataBit.equals("8"))  spDataBit.setSelection(0);
            else if (m_strDataBit.equals("7"))  spDataBit.setSelection(1);
        }
        else spDataBit.setSelection(0);
    }

    public static void CheckBtnStatus() {
        // 메인의 Hold 값을 확인 한다 hold 상태면 초록 아니면 회색
        if (btnHold == null) return;
        if (m_bHoldAll) btnHold.setBackgroundResource(R.drawable.my_button_yellowgreen);
        else btnHold.setBackgroundResource(R.drawable.my_button_base);
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        Log.d(this.getClass().getSimpleName(), "onResume()");
        super.onResume();
    }

    public static byte[] IntTo4Byte(int value) {
        byte[] byteArray = new byte[4];
        byteArray[0] = (byte)(value >> 24);
        byteArray[1] = (byte)(value >> 16);
        byteArray[2] = (byte)(value >> 8);
        byteArray[3] = (byte)(value);
        return byteArray;
    }



    public static SubActivitySetupSerial newInstance(String text) {

        SubActivitySetupSerial f = new SubActivitySetupSerial();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    class BlurAsyncTask extends AsyncTask<Void, Integer, Bitmap> {
        private  final String TAG = BlurAsyncTask.class.getName();
        protected Bitmap doInBackground(Void...arg0) {
            Bitmap map  = AppUtils.takeScreenShot(getActivity());
            Bitmap fast = new BlurView().fastBlur(map, 10);
            return fast;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null){
                final Drawable draw=new BitmapDrawable(getResources(),result);
                Window window = alert.getWindow();
                window.setBackgroundDrawable(draw);
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                window.setGravity(Gravity.CENTER);
                alert.show();
                TextView textView = (TextView) alert.findViewById(android.R.id.title);
                if (textView != null)
                    textView.setGravity(Gravity.CENTER);
            }

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
}


//                    case R.id.Serial_Option1_Help :
//                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_TRADITIONAL);
//                        builder.setTitle("Code2Concept - Blur");
//                        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_item);
//                        adapter.add("pH");
//                        adapter.add("Chlorine");
//                        adapter.add("Turbidity");
//                        adapter.add("Conductivity");
//                        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                if (id == 0) {
//
//                                } else if (id == 1) {
//
//                                } else if (id == 2) {
//
//                                } else if (id == 3) {
//                                }
//                            }
//                        });
//
//                        alert = builder.create();
//                        new BlurAsyncTask().execute();
//                        break ;