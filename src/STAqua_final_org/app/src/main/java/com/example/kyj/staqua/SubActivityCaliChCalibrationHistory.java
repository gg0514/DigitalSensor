package com.example.kyj.staqua;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import static com.example.kyj.staqua.MainActivity.SendProtocol_Get;
import static java.lang.Integer.parseInt;

/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivityCaliChCalibrationHistory extends Fragment {

    static ArrayAdapter adapter;
    static ListView listview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_cali_ch_calibrationhistory, container, false);
        adapter = new ArrayAdapter(getActivity(), R.layout.result_item) ;
        listview = (ListView) v.findViewById(R.id.Cali_Ch_Cal_List_List) ;
        v.findViewById(R.id.Cali_Ch_Cal_Option1_DeleteAll).setOnClickListener(mClickListener);
        v.findViewById(R.id.Cali_Ch_Cal_Option1_Load).setOnClickListener(mClickListener);
        return v;
    }

    public static void GetCalibtationData(){
        String dirPath = Environment.getExternalStorageDirectory()+"/STAqua/Calibration/CH_Cali.txt";
        try{

            FileInputStream fis = new FileInputStream(dirPath);
            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(fis));
            String strTemp="";
            while( (strTemp = bufferReader.readLine()) != null ) {
                adapter.add(strTemp);
            }
            fis.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        listview.setAdapter(adapter) ;
    }

    public static void ClearCalibtationData(){
        adapter.clear();
    }

    public static SubActivityCaliChCalibrationHistory newInstance(String text) {

        SubActivityCaliChCalibrationHistory f = new SubActivityCaliChCalibrationHistory();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    private static String TransferToTime(int nDate) {
        int nDayCount = nDate / 86400;
        int nSecCount = nDate % 86400;

        nDayCount += 36525;// 1900년 기준으로 변경.

        int l = nDayCount + 68569 + 2415019;
        int n = ((4 * l) / 146097);
        l = l - ((146097 * n + 3) / 4);
        int i = ((4000 * (l + 1)) / 1461001);
        l = l - ((1461 * i) / 4) + 31;
        int j = ((80 * l) / 2447);
        int nDay = l - ((2447 * j) / 80);
        l = (j / 11);
        int nMonth = j + 2 - (12 * l);
        int nYear = 100 * (n - 49) + i + l;

        int nHour = nSecCount / 3600;
        int nMinute = (nSecCount % 3600) / 60;

        String strResult = Integer.toString(nYear)+ "-" +Integer.toString(nMonth)+ "-" +Integer.toString(nDay)+ " " + Integer.toString(nHour)+ ":" + Integer.toString(nMinute);

        return  strResult;
    }


    static void getHistory(String strHis) {
        try {
            adapter.clear();
            StringTokenizer tokens = new StringTokenizer( strHis, " " );
            for( int x = 0; tokens.hasMoreElements(); x++ ){
                String strHistory = tokens.nextToken();
                StringTokenizer tokens1 = new StringTokenizer( strHistory, "," );
                String strLog = "";
                for( int y = 0; tokens1.hasMoreElements(); y++ ){
                    String strTmp = tokens1.nextToken();
                    if (y == 0) {
                        strLog += TransferToTime(parseInt(strTmp)) +"|";
                    }
                    else if (y == 1) {
                        int nCalType = parseInt(strTmp);
                        if (nCalType == 0) strLog += "1Point Buffer|";
                        else if (nCalType == 1) strLog += "2Point Buffer|";
                        else if (nCalType == 2) strLog += "1Point Sample|";
                        else if (nCalType == 3) strLog += "2Point Sample|";
                        else if (nCalType == 4) strLog += "Zero|";
                        else if (nCalType == 5) strLog += "Temperature|";
                        else if (nCalType == 15) strLog += "Cancel|";
                        else                       strLog +=" WrongType|";
                    }
                    else if (y == 2) {

                        int nCalResult = parseInt(strTmp);
                        if (nCalResult == 0) strLog += "No Sensor Calibration|";
                        else if (nCalResult == 1) strLog += "Cal in progress|";
                        else if (nCalResult == 2) strLog += "Cal OK|";
                        else if (nCalResult == 3) strLog += "Fail - Not Stable|";
                        else if (nCalResult == 4) strLog += "Fail - Buffer not found|";
                        else if (nCalResult == 5) strLog += "Fail - 1st buffer not found|";
                        else if (nCalResult == 6) strLog += "Fail - 2nd buffer not found|";
                        else if (nCalResult == 7) strLog += "Fail - Value too low|";
                        else if (nCalResult == 8) strLog += "Fail - Value too high|";
                        else if (nCalResult == 9) strLog += "Fail - Slope too low|";
                        else if (nCalResult == 10) strLog += "Fail - Slope too high|";
                        else if (nCalResult == 11) strLog += "Offset too low|";
                        else if (nCalResult == 12) strLog += "Offset too high|";
                        else if (nCalResult == 13) strLog += "Points too close|";
                        else if (nCalResult == 14) strLog += "General Cal Fail(zero or sample)|";
                        else if (nCalResult == 15) strLog += "TimeOut|";
                        else if (nCalResult == 16) strLog += "Calibration Canceled|";
                        else strLog +=" WrongResult";
                    }
                    else if (y == 3) {
                        int nCalResult = parseInt(strTmp);
                        float fCalResult = (float)(nCalResult / 1000);
                        String strValue =  String.valueOf(fCalResult );
                        strLog += strValue;
                    }

                }
                adapter.add(strLog);
            }
            listview.setAdapter(adapter);
        }
        catch (Exception e) {
            return;
        }
    }

    Button.OnClickListener mClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            //이곳에 버튼 클릭시 일어날 일을 적습니다.
            switch (v.getId()) {
                case R.id.Cali_Ch_Cal_Option1_Load:
                    byte [] bDummy = new byte[1];
                    bDummy[0] = 0x03;
                    if (!SendProtocol_Get( 2, (byte)0xB2, bDummy)){
                        Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    }
                    break;
                case R.id.Cali_Ch_Cal_Option1_DeleteAll:
                    AlertDialog.Builder d = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_TRADITIONAL);
                    d.setMessage(R.string.multi_are_you_sure_delete);
                    d.setPositiveButton(R.string.multi_answer_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String dirPath = Environment.getExternalStorageDirectory()+"/STAqua/Calibration/CH_Cali.txt";
                            File file = new File(dirPath);
                            if (file.exists())
                            {
                                file.delete();    //파일삭제
                                adapter.clear();
                                Toast.makeText(getActivity(), R.string.multi_deleted, Toast.LENGTH_SHORT).show();
                            }
                            else Toast.makeText(getActivity(), R.string.multi_delete_failed, Toast.LENGTH_SHORT).show();

                        }
                    });
                    d.setNegativeButton(R.string.multi_answer_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    d.show();
                    break;
            }
        }
    };
}
