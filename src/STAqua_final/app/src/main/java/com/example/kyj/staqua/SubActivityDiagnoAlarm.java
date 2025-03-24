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

/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivityDiagnoAlarm extends Fragment {

    static ArrayAdapter adapter;
    static ListView listview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_diagno_alarmhistory, container, false);
        adapter = new ArrayAdapter(getActivity(), R.layout.result_item) ;
        listview = (ListView) v.findViewById(R.id.Diagno_AH_List_List) ;
        v.findViewById(R.id.Diagno_AH_Option1_DeleteAll).setOnClickListener(mClickListener);
        v.findViewById(R.id.Diagno_AH_Option1_Load).setOnClickListener(mClickListener);
        return v;
    }

    @Override
    public void onStop() {
        super.onStop();
    }
    public static void GetAlarmData(){
        String dirPath = Environment.getExternalStorageDirectory()+"/STAqua/Calibration/Alarm_History.txt";
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


    public static void ClearAlarmData(){
        adapter.clear();
    }


    public static SubActivityDiagnoAlarm newInstance(String text) {

        SubActivityDiagnoAlarm f = new SubActivityDiagnoAlarm();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }


    Button.OnClickListener mClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            //이곳에 버튼 클릭시 일어날 일을 적습니다.
            switch (v.getId()) {
                case R.id.Diagno_AH_Option1_Load:
                    break;
                case R.id.Diagno_AH_Option1_DeleteAll:
                    AlertDialog.Builder d = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_TRADITIONAL);
                    d.setMessage(R.string.multi_are_you_sure_delete);
                    d.setPositiveButton(R.string.multi_answer_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String dirPath = Environment.getExternalStorageDirectory()+"/STAqua/Calibration/Alarm_History.txt";
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
