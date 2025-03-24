package com.example.kyj.staqua;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import static android.content.Context.MODE_PRIVATE;
import static com.example.kyj.staqua.MainActivity.DisplayStatus;
import static com.example.kyj.staqua.MainActivity.FileLogWriter;
import static com.example.kyj.staqua.MainActivity.btAdapter;
import static com.example.kyj.staqua.MainActivity.btnConnect;
import static com.example.kyj.staqua.MainActivity.m_bHoldAll;
import static com.example.kyj.staqua.MainActivity.m_strMode;
import static com.example.kyj.staqua.MainActivity.m_strStatus;


/**
 * Created by KYJ on 2017-01-16.
 */


public class SubActivitySetupBluetooth extends Fragment {


    static private Button btnHold;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_setup_bluetooth, container, false);
    //    mainactivity = (MainActivity)getActivity();

       // v.findViewById(R.id.Bluetooth_PairList_Btn).setOnClickListener(mClickListener);

        btnHold = (Button) v.findViewById(R.id.Bluetooth_Option1_Hold) ;

        v.findViewById(R.id.Bluetooth_Connect_Btn_Connect).setOnClickListener(mClickListener);
        v.findViewById(R.id.Bluetooth_Connect_Btn_DisConnect).setOnClickListener(mClickListener);
        v.findViewById(R.id.Bluetooth_Option1_Hold).setOnClickListener(mClickListener);
        v.findViewById(R.id.Bluetooth_Option2_Apply).setOnClickListener(mClickListener);
      //  v.findViewById(R.id.Bluetooth_Option3_Offline).setOnClickListener(mClickListener);
        return v;
    }

    public static void CheckBtnStatus() {
        // 메인의 Hold 값을 확인 한다 hold 상태면 초록 아니면 회색
        if (m_bHoldAll) btnHold.setBackgroundResource(R.drawable.my_button_yellowgreen);
        else btnHold.setBackgroundResource(R.drawable.my_button_base);
    }

    public static SubActivitySetupBluetooth newInstance(String text) {
        SubActivitySetupBluetooth f = new SubActivitySetupBluetooth();
        Bundle b = new Bundle();
        b.putString("msg", text);
        f.setArguments(b);
        return f;
    }
    Button.OnClickListener mClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            //이곳에 버튼 클릭시 일어날 일을 적습니다.
            AlertDialog.Builder d = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_TRADITIONAL);
            switch (v.getId()) {
//                case R.id.Bluetooth_PairList_Btn:
//                  //  ClickListButton();
//                    break;
                case R.id.Bluetooth_Connect_Btn_Connect:
                    btAdapter.enable();
                    FileLogWriter(getString(R.string.multi_setup_bluetooth),"Click Enable");
                    break;
                case R.id.Bluetooth_Connect_Btn_DisConnect:
                    btAdapter.disable();
                    FileLogWriter(getString(R.string.multi_setup_bluetooth),"Click Disable");
                    break;
                case R.id.Bluetooth_Option1_Hold:
                    if (m_bHoldAll)
                    {
                        d.setMessage(R.string.multi_start_measurement);
                        d.setPositiveButton(R.string.multi_answer_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        d.setNegativeButton(R.string.multi_answer_confirm, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                m_bHoldAll = false;
                                btnHold.setBackgroundResource(R.drawable.my_button_base);
                                DisplayStatus();
                            }
                        });
                        d.show();
                    }
                    else
                    {
                        d.setMessage(R.string.multi_stop_measurement);
                        d.setPositiveButton(R.string.multi_answer_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        d.setNegativeButton(R.string.multi_answer_confirm, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                m_bHoldAll = true;
                                btnHold.setBackgroundResource(R.drawable.my_button_yellowgreen);
                                DisplayStatus();
                            }
                        });
                        d.show();
                    }
                    FileLogWriter(getString(R.string.multi_setup_bluetooth),"Click Hold");
                    break;
                case R.id.Bluetooth_Option2_Apply:
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
                    FileLogWriter(getString(R.string.multi_setup_bluetooth),"Click Apply");

                    break;
            }
        }
    };
}
