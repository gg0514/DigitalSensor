package com.example.kyj.staqua;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.example.kyj.staqua.MainActivity.SendProtocol_Get;


/**
 * Created by KYJ on 2017-01-16.
 */


public class SubActivitySetupItemsReplacement extends Fragment {

    SharedPreferences prefs;
    SimpleDateFormat sdCurrent;
    SimpleDateFormat sdCurrentDirect;

    static TextView tvPhSoltBridge;
    static TextView tvChMambraneCap;
    static TextView tvTuLamp;
    static TextView tvPhSensor;
    static TextView tvChSensor;
    private TextView tvPhSoltBridge1;
    private TextView tvChMambraneCap1;

    private static Button btnLoad;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_setup_itemsreplacement, container, false);
    //    mainactivity = (MainActivity)getActivity();

        sdCurrentDirect = new SimpleDateFormat("yyyyMMdd");

        Locale mLocale = getResources().getConfiguration().locale;
        String language = mLocale.getLanguage();
        if(language.contains("ko")) {
            sdCurrent = new SimpleDateFormat("yyyy년MM월dd일");
        }
        else{
            sdCurrent = new SimpleDateFormat("MM/dd/yyyy");
        }

        tvPhSoltBridge = (TextView)v.findViewById(R.id.Setup_IR_Ph_SaltBridge_Text);
        tvChMambraneCap = (TextView)v.findViewById(R.id.Setup_IR_Ch_MembraneCap_Text);
        tvTuLamp = (TextView)v.findViewById(R.id.Setup_IR_Tu_Lamp_Text);
        tvPhSensor = (TextView)v.findViewById(R.id.Setup_IR_Ph_Sensor_Text);
        tvChSensor = (TextView)v.findViewById(R.id.Setup_IR_Ch_Sensor_Text);
        tvPhSoltBridge1 = (TextView)v.findViewById(R.id.Setup_IR_Ph_SaltBridge1_Text);
        tvChMambraneCap1 = (TextView)v.findViewById(R.id.Setup_IR_Ch_MembraneCap1_Text);

        btnLoad = (Button) v.findViewById(R.id.Serial_IR_Option1_Load) ;
        btnLoad.setOnClickListener(onClickListener) ;
        v.findViewById(R.id.Setup_IR_Ph_SaltBridge_Button).setOnClickListener(mClickListener);
        v.findViewById(R.id.Setup_IR_Ch_MembraneCap_Button).setOnClickListener(mClickListener);
        v.findViewById(R.id.Setup_IR_Tu_Lamp_Button).setOnClickListener(mClickListener);
        v.findViewById(R.id.Setup_IR_Ph_Sensor_Button).setOnClickListener(mClickListener);
        v.findViewById(R.id.Setup_IR_Ch_Sensor_Button).setOnClickListener(mClickListener);
        v.findViewById(R.id.Setup_IR_Ph_SaltBridge_Button1).setOnClickListener(mClickListener);
        v.findViewById(R.id.Setup_IR_Ch_MembraneCap_Button1).setOnClickListener(mClickListener);
        v.findViewById(R.id.Setup_IR_Tu_Lamp_Button1).setOnClickListener(mClickListener);
        v.findViewById(R.id.Setup_IR_Ph_Sensor_Button1).setOnClickListener(mClickListener);
        v.findViewById(R.id.Setup_IR_Ch_Sensor_Button1).setOnClickListener(mClickListener);
    //    DisplayTime();
        return v;
    }

    public void DisplayTime(int nSensor){
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        SimpleDateFormat dt = new SimpleDateFormat("yyyyyMMdd");
        SimpleDateFormat fm = new SimpleDateFormat("yyyy년MM월dd일");
        if (nSensor == 0) {
            cal.add(Calendar.MONTH, 6);
            String strExDate = fm.format(cal.getTime());
            tvPhSoltBridge.setText(strExDate);
        }
        if (nSensor == 1) {
            cal.add(Calendar.MONTH, 6);
            String strExDate = fm.format(cal.getTime());
            tvChMambraneCap.setText(strExDate);
        }
        if (nSensor == 2) {
            cal.add(Calendar.YEAR, 2);
            String strExDate = fm.format(cal.getTime());
            tvTuLamp.setText(strExDate);
        }
        if (nSensor == 3) {
            cal.add(Calendar.YEAR, 2);
            String strExDate = fm.format(cal.getTime());
            tvPhSensor.setText(strExDate);
        }
        if (nSensor == 4) {
            cal.add(Calendar.YEAR, 2);
            String strExDate = fm.format(cal.getTime());
            tvChSensor.setText(strExDate);
        }



//        String strTime =  sdCurrent.format(cal.getTime());
//        prefs = getActivity().getSharedPreferences("Item_Replacement", MODE_PRIVATE);
//        strTempary = prefs.getString("PhSoltBridge", "");
//        if (strTempary != "") tvPhSoltBridge.setText(getResources().getString(R.string.multi_recommended_date) +"\n"+ strTempary);
//        else  tvPhSoltBridge.setText(getResources().getString(R.string.multi_recommended_date) +"\n"+ strTime);
//        strTempary = prefs.getString("ChManbraneCap", "");
//        if (strTempary != "") tvChMambraneCap.setText(getResources().getString(R.string.multi_recommended_date) +"\n"+ strTempary);
//        else  tvChMambraneCap.setText(getResources().getString(R.string.multi_recommended_date) +"\n"+ strTime);
//        strTempary = prefs.getString("TuLamp", "");
//        if (strTempary != "") tvTuLamp.setText(getResources().getString(R.string.multi_recommended_date)+"\n"+ strTempary);
//        else  tvTuLamp.setText(getResources().getString(R.string.multi_recommended_date) +"\n"+ strTime);
//        strTempary = prefs.getString("PhSensor", "");
//        if (strTempary != "") tvPhSensor.setText(getResources().getString(R.string.multi_recommended_date) +"\n"+ strTempary);
//        else  tvPhSensor.setText(getResources().getString(R.string.multi_recommended_date) +"\n"+ strTime);
//        strTempary = prefs.getString("ChSensor", "");
//        if (strTempary != "") tvChSensor.setText(getResources().getString(R.string.multi_recommended_date) +"\n"+ strTempary);
//        else  tvChSensor.setText(getResources().getString(R.string.multi_recommended_date) +"\n"+ strTime);
//        strTempary = prefs.getString("PhSoltBridge", "");
//        if (strTempary != "") tvPhSoltBridge1.setText(getResources().getString(R.string.multi_notification_date)+"\n"+ strTempary);
//        else  tvPhSoltBridge1.setText(getResources().getString(R.string.multi_notification_date) +"\n"+ strTime);
//        strTempary = prefs.getString("ChManbraneCap", "");
//        if (strTempary != "") tvChMambraneCap1.setText(getResources().getString(R.string.multi_notification_date)+"\n"+ strTempary);
//        else  tvChMambraneCap1.setText(getResources().getString(R.string.multi_notification_date) +"\n"+ strTime);
    }


    public static SubActivitySetupItemsReplacement newInstance(String text) {
        SubActivitySetupItemsReplacement f = new SubActivitySetupItemsReplacement();
        Bundle b = new Bundle();
        b.putString("msg", text);
        f.setArguments(b);
        return f;
    }

    static void getExpoireDate(int nSensor, String strDate) {
        try {
            SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat fm = new SimpleDateFormat("yyyy년MM월dd일");
            Date date = dt.parse(strDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            if (nSensor == 0) {
                cal.add(Calendar.MONTH, 6);
                String strExDate = fm.format(cal.getTime());
                tvPhSoltBridge.setText(strExDate);
            }
            if (nSensor == 1) {
                cal.add(Calendar.MONTH, 6);
                String strExDate = fm.format(cal.getTime());
                tvChMambraneCap.setText(strExDate);
            }
            if (nSensor == 2) {
                cal.add(Calendar.YEAR, 2);
                String strExDate = fm.format(cal.getTime());
                tvTuLamp.setText(strExDate);
            }
            if (nSensor == 3) {
                cal.add(Calendar.YEAR, 2);
                String strExDate = fm.format(cal.getTime());
                tvPhSensor.setText(strExDate);
            }
            if (nSensor == 4) {
                cal.add(Calendar.YEAR, 2);
                String strExDate = fm.format(cal.getTime());
                tvChSensor.setText(strExDate);
            }


        }
        catch (Exception e) {
            return;
        }
    }


    Button.OnClickListener mClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            //이곳에 버튼 클릭시 일어날 일을 적습니다.
            Date date = new Date();
            final String strTime = sdCurrent.format(date);
            AlertDialog.Builder d = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_TRADITIONAL);

//            Calendar cal = Calendar.getInstance();
//            cal.setTime(date);
//            cal.add(Calendar.YEAR, 2);
//            final String strTime2year =  sdCurrent.format(cal.getTime());
//            final String strTimeDirect =  sdCurrentDirect.format(date);
//            final SharedPreferences.Editor editor = prefs.edit();
            byte [] bDummy = new byte[1];
            switch (v.getId()) {
                case R.id.Setup_IR_Ph_SaltBridge_Button:
                    d.setMessage(getResources().getString(R.string.multi_complete_replacement)+"\n" +strTime );
                    d.setPositiveButton(R.string.multi_answer_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                   //           FileReplacementHistoryWriter(getResources().getString(R.string.multi_setup_ph_salt_bridge), m_strPhSerial);
//                            editor.putString("PhSoltBridge", strTime2year);
//                            editor.commit();
                            byte [] bDummy = new byte[1];
                            bDummy[0] = 0x00;
                            if (!SendProtocol_Get( 2, (byte)0xB3, bDummy)){
                                Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                            }
                            DisplayTime(0);
                        }
                    });
                    d.setNegativeButton(R.string.multi_answer_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    d.show();
                    break;
                case R.id.Setup_IR_Ch_MembraneCap_Button:
                    d.setMessage(getResources().getString(R.string.multi_complete_replacement)+"\n" +strTime );
                    d.setPositiveButton(R.string.multi_answer_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            byte [] bDummy = new byte[1];
                            bDummy[0] = 0x01;
                            if (!SendProtocol_Get( 2, (byte)0xB3, bDummy)){
                                Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                            }
                            DisplayTime(1);
                        }
                    });
                    d.setNegativeButton(R.string.multi_answer_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    d.show();
                    break;
                case R.id.Setup_IR_Tu_Lamp_Button:
                    d.setMessage(getResources().getString(R.string.multi_complete_replacement)+"\n" +strTime );
                    d.setPositiveButton(R.string.multi_answer_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            byte [] bDummy = new byte[1];
                            bDummy[0] = 0x02;
                            if (!SendProtocol_Get( 2, (byte)0xB3, bDummy)){
                                Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                            }
                            DisplayTime(2);
                        }
                    });
                    d.setNegativeButton(R.string.multi_answer_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    d.show();
                    break;

                case R.id.Setup_IR_Ph_Sensor_Button:
                    d.setMessage(getResources().getString(R.string.multi_complete_replacement)+"\n" +strTime );
                    d.setPositiveButton(R.string.multi_answer_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            byte [] bDummy = new byte[1];
                            bDummy[0] = 0x03;
                            if (!SendProtocol_Get( 2, (byte)0xB3, bDummy)){
                                Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                            }
                            DisplayTime(3);
                        }
                    });
                    d.setNegativeButton(R.string.multi_answer_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    d.show();

                    break;

                case R.id.Setup_IR_Ch_Sensor_Button:
                    d.setMessage(getResources().getString(R.string.multi_complete_replacement)+"\n" +strTime );
                    d.setPositiveButton(R.string.multi_answer_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            byte [] bDummy = new byte[1];
                            bDummy[0] = 0x04;
                            if (!SendProtocol_Get( 2, (byte)0xB3, bDummy)){
                                Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                            }
                            DisplayTime(4);
                        }
                    });
                    d.setNegativeButton(R.string.multi_answer_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    d.show();
                    break;
                case R.id.Setup_IR_Ph_SaltBridge_Button1:
                    bDummy[0] = 0x00;
                    if (!SendProtocol_Get( 2, (byte)0xB4, bDummy)){
                        Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    }
                    break;
                case R.id.Setup_IR_Ch_MembraneCap_Button1:
                    bDummy[0] = 0x01;
                    if (!SendProtocol_Get( 2, (byte)0xB4, bDummy)){
                        Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    }
                    break;
                case R.id.Setup_IR_Tu_Lamp_Button1:
                    bDummy[0] = 0x02;
                    if (!SendProtocol_Get( 2, (byte)0xB4, bDummy)){
                        Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    }
                    break;
                case R.id.Setup_IR_Ph_Sensor_Button1:
                    bDummy[0] = 0x03;
                    if (!SendProtocol_Get( 2, (byte)0xB4, bDummy)){
                        Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    }
                    break;
                case R.id.Setup_IR_Ch_Sensor_Button1:
                    bDummy[0] = 0x04;
                    if (!SendProtocol_Get( 2, (byte)0xB4, bDummy)){
                        Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    }
                    break;
            }
        }
    };

    Button.OnClickListener onClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            AlertDialog.Builder d = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_TRADITIONAL);
            switch (view.getId()) {
                case R.id.Serial_IR_Option1_Load :
                    break;
            }
        }
    } ;

    private void CreateFile(String FilePath) {
        try {
            int nLast = FilePath.lastIndexOf("/");
            String strDir = FilePath.substring(0, nLast);
            String strFile = FilePath.substring(nLast + 1, FilePath.length());

            File dirFolder = new File(strDir);
            dirFolder.mkdirs();
            File f = new File(dirFolder, strFile);
            f.createNewFile();
        } catch (Exception ex) {

        }
    }

//    private void  FileReplacementValueUpdate (String strType)
//    {
//        Date d = new Date();
//        SimpleDateFormat sdCurrent = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
//        String strTime = sdCurrent.format(d);
//        String dirPath = Environment.getExternalStorageDirectory()+"/STAqua/History/Replacement.txt";
//
//        File file = new File(dirPath);
//        if (!file.exists()){
//            CreateFile(dirPath);
//        }
//
//        String strText = "["+strType+"]"+strTime+"\n";
//        String findStr = "(?i).*" + strType + ".*";
//
//        try{
//            // BufferedWriter 와 FileWriter를 조합하여 사용 (속도 향상)
//            BufferedReader br = new BufferedReader(new FileReader(file));
//          //  BufferedWriter fw = new BufferedWriter(new FileWriter(file));
//            FileWriter fw = new FileWriter(file);
//            BufferedWriter fw1 = new BufferedWriter(new FileWriter(dirPath, true));
//
//            String orgStr="";
//            String changeStr ="";
//            String strReadline ="";
//            boolean bResult = false;
//            while ((strReadline = br.readLine()) != null) {
//                if (strReadline.matches(findStr)) {
//                 //   strReadline = strReadline.replaceAll(strReadline, strText);
//                    changeStr += strText;
//                    bResult = true;
//                }
//                else
//                {
//                    changeStr += strReadline;
//                }
//            }
//            if (bResult) fw.write(changeStr);
//            else {
//                fw1.write(strText);
//                fw1.flush();
//            }
//
//
//            // 객체 닫기
//            br.close();
//            fw.close();
//            fw1.close();
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//    }



    private  void  FileReplacementHistoryWriter (String strType, String strNo)
    {
        Date d = new Date();
        SimpleDateFormat sdCurrent = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        String strTime = sdCurrent.format(d);
        String dirPath = Environment.getExternalStorageDirectory()+"/STAqua/History/Replacement_History.txt";

        File file = new File(dirPath);
        if (!file.exists()){
            CreateFile(dirPath);
        }
    //    String strWFile = dirPath+"/"+strFileName+".txt";
        String strText = strType+" | "+strNo+" | "+strTime+"\n";
        try{
            // BufferedWriter 와 FileWriter를 조합하여 사용 (속도 향상)
            BufferedWriter fw = new BufferedWriter(new FileWriter(dirPath, true));
            // 파일안에 문자열 쓰기
            fw.write(strText);
            fw.flush();
            // 객체 닫기
            fw.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
