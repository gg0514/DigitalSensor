package com.example.kyj.staqua;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;
import static com.example.kyj.staqua.MainActivity.SendProtocol_Get;
import static com.example.kyj.staqua.MainActivity.SendProtocol_Set;
import static com.example.kyj.staqua.MainActivity.m_bHoldAll;
import static com.example.kyj.staqua.MainActivity.m_strGateway;
import static com.example.kyj.staqua.MainActivity.m_strIpAddress;
import static com.example.kyj.staqua.MainActivity.m_strPort;
import static com.example.kyj.staqua.MainActivity.m_strSubnetMask;

/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivitySetupEthernet extends Fragment {

    SharedPreferences prefs;

    static private Button btnLoad;
    EditText edIpAddress;
    EditText edSubnetMask;
    EditText edGateway;
    EditText edPort;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_setup_ethernet, container, false);

        btnLoad = (Button) v.findViewById(R.id.Ethernet_Option1_Load) ;

        edIpAddress = (EditText)v.findViewById(R.id.Ethernet_IPAddress_Ed);
        edSubnetMask = (EditText)v.findViewById(R.id.Ethernet_SubnetMask_Ed);
        edGateway = (EditText)v.findViewById(R.id.Ethernet_Gateway_Ed);
        edPort = (EditText)v.findViewById(R.id.Ethernet_Port_Ed);

        prefs = getActivity().getSharedPreferences("Setup_Ethernet", MODE_PRIVATE);

        edIpAddress.setText(m_strIpAddress);
        edSubnetMask.setText(m_strSubnetMask);
        edGateway.setText(m_strGateway);
        edPort.setText(m_strPort);

        v.findViewById(R.id.Ethernet_Main).setOnClickListener(mvClickListener);
        v.findViewById(R.id.Ethernet_IPAddress_Ed).setOnClickListener(mClickListener);
        v.findViewById(R.id.Ethernet_SubnetMask_Ed).setOnClickListener(mClickListener);
        v.findViewById(R.id.Ethernet_Gateway_Ed).setOnClickListener(mClickListener);
        v.findViewById(R.id.Ethernet_Port_Ed).setOnClickListener(mClickListener);

        v.findViewById(R.id.Ethernet_Option1_Load).setOnClickListener(mClickListener);
      //  v.findViewById(R.id.Ethernet_Option1_Hold).setOnClickListener(mClickListener);
        v.findViewById(R.id.Ethernet_Option2_Apply).setOnClickListener(mClickListener);

        RefreshData();

        return v;
    }

    public static void CheckBtnStatus() {
        // 메인의 Hold 값을 확인 한다 hold 상태면 초록 아니면 회색
        if (m_bHoldAll) btnLoad.setBackgroundResource(R.drawable.my_button_yellowgreen);
        else btnLoad.setBackgroundResource(R.drawable.my_button_base);
    }

    public static SubActivitySetupEthernet newInstance(String text) {

        SubActivitySetupEthernet f = new SubActivitySetupEthernet();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }


    EditText.OnFocusChangeListener meClickListener  = new View.OnFocusChangeListener(){
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus)
            {
                switch(v.getId()){
                    case R.id.Ethernet_IPAddress_Ed:
                        edIpAddress.setText("");
                        break;
                    case R.id.Ethernet_SubnetMask_Ed:
                        edSubnetMask.setText("");
                        break;
                    case R.id.Ethernet_Gateway_Ed:
                        edGateway.setText("");
                        break;
                    case R.id.Ethernet_Port_Ed:
                        edPort.setText("");
                        break;
                }
            }
        }
    };

    View.OnClickListener mvClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.Ethernet_Main:
                    InputMethodManager imm =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    break;
            }
        }
    };

    private void RefreshData() {
        SharedPreferences prefs;
        prefs = getActivity().getSharedPreferences("Setup_Ethernet", MODE_PRIVATE);
        m_strIpAddress = prefs.getString("IpAddress", "");
        if (m_strIpAddress != "") {
            edIpAddress.setText(m_strIpAddress);
        }
        m_strSubnetMask = prefs.getString("SubnetMask", "");
        if (m_strSubnetMask != "") {
            edSubnetMask.setText(m_strSubnetMask);
        }
        m_strGateway = prefs.getString("Gateway", "");
        if (m_strGateway != "") {
            edGateway.setText(m_strGateway);
        }
        m_strPort = prefs.getString("Port", "");
        if (m_strPort != "") {
            edPort.setText(m_strPort);
        }
    }

    Button.OnClickListener mClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            final SharedPreferences.Editor editor = prefs.edit();
            AlertDialog.Builder d = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_TRADITIONAL);
            switch (v.getId()) {
                case R.id.Ethernet_Option1_Load:
                    byte [] bDummy = new byte[1];
                    bDummy[0] = 0x00;
                    if (!SendProtocol_Get( 1, (byte)0xB6, bDummy)){
                        Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    }
                    RefreshData();
                    break;
//                case R.id.Ethernet_Option1_FactoryReset :
//                    m_strIpAddress = "192.168.000.180";
//                    m_strSubnetMask = "255.255.255.000";
//                    m_strGateway = "192.168.000.001";
//                    m_strPort = "9000";
//                    edIpAddress.setText(m_strIpAddress);
//                    edSubnetMask.setText(m_strSubnetMask);
//                    edGateway.setText(m_strGateway);
//                    edPort.setText(m_strPort);
//                    editor.putString("Ip", m_strIpAddress);
//                    editor.putString("Subnet", m_strSubnetMask);
//                    editor.putString("Gateway", m_strGateway);
//                    editor.putString("Port", m_strPort);
//                    editor.commit();
//                    Toast.makeText(getActivity(), R.string.multi_factory_reset, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
//                    FileLogWriter(getString(R.string.multi_setup_ethernet),"Click Factory Reset");
//                    break;
                case R.id.Ethernet_Option2_Apply:
                    d.setMessage(R.string.multi_change_communication);
                    d.setPositiveButton(R.string.multi_answer_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String strIP = "";
                            String strGW = "";
                            String strSubNet = "";
                            String strPort = "";
                            try {
                                strIP = edIpAddress.getText().toString();
                                String[] strSplit = strIP.split("\\.");
                                int [] nValue = new int[4];
                                for (int i = 0; i < 4; i++) {
                                    nValue[i] = Integer.parseInt(strSplit[i]);
                                    if ((nValue[i] > 255) || (nValue[i] < 0)) {
                                        Toast.makeText(getActivity(), R.string.multi_setup_ethernet_value, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                                        return;
                                    }
                                    strSplit[i] = String.format("%03d", nValue[i]);
                                }
                                strIP =  strSplit[0]+"."+strSplit[1]+"."+strSplit[2]+"."+strSplit[3];
                                strSubNet = edSubnetMask.getText().toString();
                                strSplit = strSubNet.split("\\.");
                                for (int i = 0; i < 4; i++) {
                                    nValue[i] = Integer.parseInt(strSplit[i]);
                                    if ((nValue[i] > 255) || (nValue[i] < 0)) {
                                        Toast.makeText(getActivity(), R.string.multi_setup_ethernet_value, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                                        return;
                                    }
                                    strSplit[i] = String.format("%03d", nValue[i]);
                                }
                                strSubNet =  strSplit[0]+"."+strSplit[1]+"."+strSplit[2]+"."+strSplit[3];
                                strGW = edGateway.getText().toString();
                                strSplit = strGW.split("\\.");
                                for (int i = 0; i < 4; i++) {
                                    nValue[i] = Integer.parseInt(strSplit[i]);
                                    if ((nValue[i] > 255) || (nValue[i] < 0)) {
                                        Toast.makeText(getActivity(), R.string.multi_setup_ethernet_value, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                                        return;
                                    }
                                    strSplit[i] = String.format("%03d", nValue[i]);
                                }
                                strGW =  strSplit[0]+"."+strSplit[1]+"."+strSplit[2]+"."+strSplit[3];
                                strPort = edPort.getText().toString();
                                int nPort = Integer.parseInt(strPort);
                                if ((nPort > 99999) || (nPort < 0)) {
                                    Toast.makeText(getActivity(), R.string.multi_setup_ethernet_port_value, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                                    return;
                                }
                            }
                            catch (Exception e) {
                                Toast.makeText(getActivity(), R.string.multi_setup_ethernet_format, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                            }

                            String strPayload1 = strIP+" "+strSubNet+" "+strGW+" "+strPort+" ";
                            int nLength = strPayload1.length();
                            byte[] btValue = strPayload1.getBytes();
                            if (!SendProtocol_Set( nLength+1, (byte)0xC9, btValue)){
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

//                    d.setMessage(R.string.multi_change_communication);
//                    d.setPositiveButton(R.string.multi_answer_yes, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            m_strIpAddress = edIpAddress.getText().toString();
//                            m_strSubnetMask = edSubnetMask.getText().toString();
//                            m_strGateway = edGateway.getText().toString();
//                            m_strPort = edPort.getText().toString();
//                            editor.putString("Ip", m_strIpAddress);
//                            editor.putString("Subnet", m_strSubnetMask);
//                            editor.putString("Gateway", m_strGateway);
//                            editor.putString("Port", m_strPort);
//                            editor.commit();
//                            m_strMode = "Ethernet";
//                            m_strStatus = "Disconnected";
//
//                            prefs = getActivity().getSharedPreferences("PrefName", MODE_PRIVATE);
//                            SharedPreferences.Editor editor1 = prefs.edit();
//                            editor1.putString("my_mode", m_strMode);
//                            editor1.putString("my_status", m_strStatus);
//                            editor1.commit();
//
//                            Toast.makeText(getActivity(), R.string.multi_applied, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
//                            DisplayStatus();
//                    //        btnConnect.setVisibility(View.VISIBLE);
//                            getActivity().finish();
//
//                        }
//                    });
//                    d.setNegativeButton(R.string.multi_answer_no, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.cancel();
//                        }
//                    });
//                    d.show();
//                    FileLogWriter(getString(R.string.multi_setup_ethernet),"Click Apply");
//                    break ;

            }
        }
    };
}
