package com.example.kyj.staqua;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;
import static com.example.kyj.staqua.MainActivity.DisplayStatus;
import static com.example.kyj.staqua.MainActivity.FileLogWriter;
import static com.example.kyj.staqua.MainActivity.SendProtocol_Get;
import static com.example.kyj.staqua.MainActivity.SendProtocol_Set;
import static com.example.kyj.staqua.MainActivity.btnConnect;
import static com.example.kyj.staqua.MainActivity.m_bHoldAll;
import static com.example.kyj.staqua.MainActivity.m_strChSerial;
import static com.example.kyj.staqua.MainActivity.m_strCoSerial;
import static com.example.kyj.staqua.MainActivity.m_strEquipmentSerial;
import static com.example.kyj.staqua.MainActivity.m_strMode;
import static com.example.kyj.staqua.MainActivity.m_strPhSerial;
import static com.example.kyj.staqua.MainActivity.m_strStatus;
import static com.example.kyj.staqua.MainActivity.m_strTuSerial;

/**
 * Created by KYJ on 2017-01-16.
 */
public class SubActivitySetupSystem extends Fragment {

    String m_strLanguage ="";
    EditText edSystemLanguage;
    Spinner spLanguage;

    Button btnHold;

    EditText edSerial;
    EditText edSerialLine1;
    EditText edSerialLine2;
    EditText edSerialLine3;
    EditText edSerialLine4;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_setup_system, container, false);

        // 센서 시리얼 번호 가져오기
        edSerial = (EditText)v.findViewById(R.id.System_ProductSerialNumber_Ed);
        edSerialLine1 = (EditText)v.findViewById(R.id.System_Ph_Ed);
        edSerialLine2 = (EditText)v.findViewById(R.id.System_Ch_Ed);
        edSerialLine3 = (EditText)v.findViewById(R.id.System_Tu_Ed);
        edSerialLine4 = (EditText)v.findViewById(R.id.System_Co_Ed);

        byte [] bDummy = new byte[1];
        bDummy[0] = 0x00;
        if (!SendProtocol_Get( 1, (byte)0xA2, bDummy)){
            Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
        }


        edSerial.setText(m_strEquipmentSerial);
        edSerialLine1.setText(m_strPhSerial);
        edSerialLine2.setText(m_strChSerial);
        edSerialLine3.setText(m_strTuSerial);
        edSerialLine4.setText(m_strCoSerial);

        btnHold = (Button) v.findViewById(R.id.System_Hold) ;

        v.findViewById(R.id.System_Option1_USB).setOnClickListener(mClickListener);
        v.findViewById(R.id.System_Option2_Bluetooth).setOnClickListener(mClickListener);
        v.findViewById(R.id.System_Option3_Offline).setOnClickListener(mClickListener);
        v.findViewById(R.id.System_Hold).setOnClickListener(mClickListener);



        v.findViewById(R.id.System_Option1_Load).setOnClickListener(mClickListener);
        v.findViewById(R.id.System_ProductSerialNumber_Registration).setOnClickListener(mClickListener);
        v.findViewById(R.id.System_ProductSerialNumber_Registration_Load).setOnClickListener(mClickListener);

        if (!m_bHoldAll) {
            btnHold.setBackgroundResource(R.drawable.my_button_green);
            btnHold.setText(R.string.multi_on);
        }
        else {
            btnHold.setBackgroundResource(R.drawable.my_button_base);
            btnHold.setText(R.string.multi_off);
        }


//        // 옵션값 불러오기
//        SharedPreferences sf = this.getActivity().getSharedPreferences("system", 0);
//        m_strLanguage = sf.getString("language", ""); // 키값으로 꺼냄
//
//        spLanguage = (Spinner) v.findViewById(R.id.System_Language_Sp);
//       // String text = spLanguage.getSelectedItem().toString();
//        String[] optionLevel = getResources().getStringArray(R.array.language);
//        SubActivitySetupSystem.SpinnerAdapter spALanguage = new SubActivitySetupSystem.SpinnerAdapter(getActivity(),android.R.layout.simple_spinner_item, optionLevel);
//        spLanguage.setAdapter(spALanguage);
//
//        if (m_strLanguage == "KOREAN")
//        {
//            spLanguage.setSelection(0);
//        }
//        else if (m_strLanguage == "ENGLISH")
//        {
//            spLanguage.setSelection(1);
//        }
//        else spLanguage.setSelection(0);
//
//        v.findViewById(R.id.System_Language_Btn).setOnClickListener(mClickListener);

        return v;
    }



    public static SubActivitySetupSystem newInstance(String text) {
        SubActivitySetupSystem f = new SubActivitySetupSystem();
        Bundle b = new Bundle();
        b.putString("msg", text);
        f.setArguments(b);
        return f;
    }

//    public void setLocale(String char_select)
//    {
//        Locale locale = new Locale(char_select);
//        Locale.setDefault(locale);
//        Configuration c =  this.getActivity().getBaseContext().getResources().getConfiguration();
//        c.locale = locale;
//        this.getActivity().getBaseContext().getResources().updateConfiguration(c, this.getActivity().getBaseContext().getResources().getDisplayMetrics());
//
//        SharedPreferences sf = this.getActivity().getSharedPreferences("system", 0);
//        SharedPreferences.Editor editor = sf.edit();//저장하려면 editor가 필요
//        editor.putString("language", char_select); // 입력
//        editor.commit(); // 파일에 최종 반영함
//    }


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

    Button.OnClickListener mClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            //이곳에 버튼 클릭시 일어날 일을 적습니다.
            AlertDialog.Builder d = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_TRADITIONAL);
            switch (v.getId()) {
                case R.id.System_ProductSerialNumber_Registration_Load:
                    byte [] bDummy1 = new byte[1];
                    bDummy1[0] = 0x00;
                    if (!SendProtocol_Get( 1, (byte)0xAF, bDummy1)){
                        Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    }
                    edSerial.setText(m_strEquipmentSerial);
                    break;
                case R.id.System_ProductSerialNumber_Registration:
                    // 각 센서 시리얼 번호 요청
                    String strPayload1 = edSerial.getText().toString()+" ";
                    int nLength = strPayload1.length();
                    byte[] btValue = strPayload1.getBytes();
                    if (!SendProtocol_Set( nLength+1, (byte)0xC2, btValue)){
                        Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    }
                    break;
                case R.id.System_Option1_Load:
                    // 각 센서 시리얼 번호 요청
                    byte [] bDummy = new byte[1];
                    bDummy[0] = 0x00;
                    if (!SendProtocol_Get( 1, (byte)0xA2, bDummy)){
                        Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    }
                    edSerialLine1.setText(m_strPhSerial);
                    edSerialLine2.setText(m_strChSerial);
                    edSerialLine3.setText(m_strTuSerial);
                    edSerialLine4.setText(m_strCoSerial);

                break;
                case R.id.System_Option1_USB:
                    d.setMessage(R.string.multi_change_communication);
                    d.setPositiveButton(R.string.multi_answer_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            SharedPreferences prefs;
                            m_strMode = "USB";
                            m_strStatus = "Disconnected";
                            prefs = getActivity().getSharedPreferences("PrefName", MODE_PRIVATE);
                            SharedPreferences.Editor editor1 = prefs.edit();
                            editor1.putString("my_mode", m_strMode);
                            editor1.putString("my_status", m_strStatus);
                            editor1.commit();

                            Toast.makeText(getActivity(), R.string.multi_applied, Toast.LENGTH_LONG).show();
                            DisplayStatus();
                            btnConnect.setVisibility(View.VISIBLE);
                            getActivity().finish();
                        }
                    });
                    d.setNegativeButton(R.string.multi_answer_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    d.show();
                    FileLogWriter(getString(R.string.multi_setup_serial),"Click Apply");
                    break ;

                case R.id.System_Option2_Bluetooth:
                    d.setMessage(R.string.multi_change_communication);
                    d.setPositiveButton(R.string.multi_answer_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            m_strMode = "BlueTooth";
                            m_strStatus = "Disconnected";

                            SharedPreferences prefs;
                            prefs = getActivity().getSharedPreferences("PrefName", MODE_PRIVATE);
                            SharedPreferences.Editor editor1 = prefs.edit();
                            editor1.putString("my_mode", m_strMode);
                            editor1.putString("my_status", m_strStatus);
                            editor1.commit();

                            DisplayStatus();
                            btnConnect.setVisibility(View.VISIBLE);
                            getActivity().finish();
                        }
                    });
                    d.setNegativeButton(R.string.multi_answer_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    d.show();
                    FileLogWriter(getString(R.string.multi_setup_bluetooth),"Click Offline");
                    break;
                case R.id.System_Option3_Offline:
                    d.setMessage(R.string.multi_change_communication);
                    d.setPositiveButton(R.string.multi_answer_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                          //  m_strMode = "Offline";
                            m_strMode = "Online";
                            m_strStatus = "Connected";

                            SharedPreferences prefs;
                            prefs = getActivity().getSharedPreferences("PrefName", MODE_PRIVATE);
                            SharedPreferences.Editor editor1 = prefs.edit();
                            editor1.putString("my_mode", m_strMode);
                            editor1.putString("my_status", m_strStatus);
                            editor1.commit();

                            DisplayStatus();
                            getActivity().finish();
                        }
                    });
                    d.setNegativeButton(R.string.multi_answer_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    d.show();
                    FileLogWriter(getString(R.string.multi_setup_bluetooth),"Click Offline");
                    break;
                case R.id.System_Hold:

                    if (!m_bHoldAll)
                    {
                        d.setMessage(R.string.multi_stop_measurement);
                        d.setPositiveButton(R.string.multi_answer_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        d.setNegativeButton(R.string.multi_answer_confirm, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                byte[] btData = new byte[1];
                                btData[0] = 0x01; // On
                                if (!SendProtocol_Get(2,(byte)0xC0, btData)) {
                                    Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                                    return;
                                }
                                //m_bHoldAll = true;
                                btnHold.setBackgroundResource(R.drawable.my_button_base);
                                btnHold.setText(R.string.multi_off);
                            }
                        });
                        d.show();
                    }
                    else
                    {
                        d.setMessage(R.string.multi_start_measurement);
                        d.setPositiveButton(R.string.multi_answer_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        d.setNegativeButton(R.string.multi_answer_confirm, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                byte[] btData = new byte[1];
                                btData[0] = 0x00; // Off
                                if (!SendProtocol_Get(2,(byte)0xC0, btData)) {
                                    Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                                    return;
                                }
                               // m_bHoldAll = false;
                                btnHold.setBackgroundResource(R.drawable.my_button_green);
                                btnHold.setText(R.string.multi_on);
                            }
                        });
                        d.show();
                    }
                    FileLogWriter(getString(R.string.multi_setup_bluetooth),"Click Hold");
                    break;
            }
        }
    };
}
