package com.example.kyj.staqua;

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
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import static com.example.kyj.staqua.MainActivity.SendProtocol_Get;
import static java.lang.Integer.parseInt;

/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivitySetupReplacementHistory extends Fragment {

    static ArrayAdapter adapter;
    static ListView listview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_setup_replancementhistory, container, false);
        adapter = new ArrayAdapter(getActivity(), R.layout.result_item) ;
        listview = (ListView) v.findViewById(R.id.Setup_RH_List_List) ;
        v.findViewById(R.id.Setup_RH_Option1_Load).setOnClickListener(mClickListener);
        //GetReplaceHistoryData();
        return v;
    }

    public static void GetReplaceHistoryData(){
        String dirPath = Environment.getExternalStorageDirectory()+"/STAqua/History/Replacement_History.txt";
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
    public static SubActivitySetupReplacementHistory newInstance(String text) {

        SubActivitySetupReplacementHistory f = new SubActivitySetupReplacementHistory();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
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
                        strLog += strTmp+"|";
                    }
                    else if (y == 1) {
                        int nCalType = parseInt(strTmp);
                        if (nCalType == 0) strLog += "PH SoltBridge|";
                        else if (nCalType == 1) strLog += "Ch ManbraneCap|";
                        else if (nCalType == 2) strLog += "Tu Lamp|";
                        else if (nCalType == 3) strLog += "Ph Sensor|";
                        else if (nCalType == 4) strLog += "Ch Sensor|";
                        else                       strLog +=" WrongType|";
                    }
                    else if (y == 2) {
                        int nCalResult = parseInt(strTmp);
                        if (nCalResult == 0) strLog += "[Serial No]No Serial";
                        else if (nCalResult == 1) strLog += "[Serial No]No Serial";
                        else if (nCalResult == 2) strLog += "[Serial No]No Serial";
                        else strLog +=" [Serial No]"+ Integer.toString(nCalResult);
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

            switch (v.getId()) {
                case R.id.Setup_RH_Option1_Load:
                    byte [] bDummy = new byte[1];
                    bDummy[0] = 0x00;
                    if (!SendProtocol_Get( 2, (byte)0xB5, bDummy)){
                        Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                    }

                    break;

            }
        }
    };

}
