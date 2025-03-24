package com.example.kyj.staqua;

import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kyj.staqua.utils.AppUtils;
import com.example.kyj.staqua.utils.BlurView;

import java.util.ArrayList;

import static com.example.kyj.staqua.MainActivity.SendProtocol_Get;

/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivitySetupSystemTime extends Fragment {


    SharedPreferences prefs;

    private AlertDialog alert;
    private ArrayList<String> mList;
    private ListView mListView;;
    private ArrayAdapter mAdapter;

//    private static Button btnApply;
//    private static Button btnLoad;
//    private static Button btnVersionLoad;

    Spinner spYear;
    Spinner spMonth;
    Spinner spDay;
    Spinner spHour;
    Spinner spMinute;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_setup_systemtime, container, false);

        spYear = (Spinner) v.findViewById(R.id.SystemTime_Year_Sp);
        String[] optionLevel1 = getResources().getStringArray(R.array.systemdate_year);
        SpinnerAdapter spAYear = new SpinnerAdapter(getActivity(),android.R.layout.simple_spinner_item, optionLevel1);
        spYear.setAdapter(spAYear);

        spMonth = (Spinner) v.findViewById(R.id.SystemTime_Month_Sp);
        String[] optionLevel2 = getResources().getStringArray(R.array.systemdate_month);
        SpinnerAdapter spAMonth = new SpinnerAdapter(getActivity(),android.R.layout.simple_spinner_item, optionLevel2);
        spMonth.setAdapter(spAMonth);

        spDay = (Spinner) v.findViewById(R.id.SystemTime_Day_Sp);
        String[] optionLevel3 = getResources().getStringArray(R.array.systemdate_day);
        SpinnerAdapter spADay= new SpinnerAdapter(getActivity(),android.R.layout.simple_spinner_item, optionLevel3);
        spDay.setAdapter(spADay);

        spHour = (Spinner) v.findViewById(R.id.SystemTime_Hour_Sp);
        String[] optionLevel4 = getResources().getStringArray(R.array.systemdate_hour);
        SpinnerAdapter spAHour= new SpinnerAdapter(getActivity(),android.R.layout.simple_spinner_item, optionLevel4);
        spHour.setAdapter(spAHour);

        spMinute = (Spinner) v.findViewById(R.id.SystemTime_Minute_Sp);
        String[] optionLevel5 = getResources().getStringArray(R.array.systemdate_minute);
        SpinnerAdapter spAMinute= new SpinnerAdapter(getActivity(),android.R.layout.simple_spinner_item, optionLevel5);
        spMinute.setAdapter(spAMinute);

//        spParity = (Spinner) v.findViewById(R.id.Serial_Parity_Sp);
//        String[] optionLevel4 = getResources().getStringArray(R.array.parity);
//        SpinnerAdapter spAParity= new SpinnerAdapter(getActivity(),android.R.layout.simple_spinner_item, optionLevel4);
//        spParity.setAdapter(spAParity);
//
//        spDataBit = (Spinner) v.findViewById(R.id.Serial_DataBit_Sp);
//        String[] optionLevel5 = getResources().getStringArray(R.array.data_bit);
//        SpinnerAdapter spADataBit= new SpinnerAdapter(getActivity(),android.R.layout.simple_spinner_item, optionLevel5);
//        spDataBit.setAdapter(spADataBit);


        Button.OnClickListener onClickListener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder d = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_TRADITIONAL);
                switch (view.getId()) {
                    case R.id.SystemTime_Option1_Load :
                        byte [] bDummy = new byte[1];
                        bDummy[0] = 0x00;
                        if (!SendProtocol_Get( 1, (byte)0xAD, bDummy)){
                            Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                        }
                        break;
                    case R.id.SystemTime_Version1_Load:
                        byte [] bDummy2 = new byte[1];
                        bDummy2[0] = 0x00;
                        if (!SendProtocol_Get( 1, (byte)0xB1, bDummy2)){
                            Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                        }
                        break;
                    case R.id.SystemTime_Option2_Apply:
                        int nYear = Integer.parseInt(spYear.getSelectedItem().toString());
                        int nMonth = Integer.parseInt(spMonth.getSelectedItem().toString());
                        int nDay = Integer.parseInt(spDay.getSelectedItem().toString());
                        int nHour = Integer.parseInt(spHour.getSelectedItem().toString());
                        int nMinute = Integer.parseInt(spMinute.getSelectedItem().toString());
                        int nSecond = 0;
                        long ulNowDate = date_to_count(nYear, nMonth, nDay, nHour, nMinute, nSecond);

                        byte [] bDummy1 = new byte[5];
                        //   byte[] btNowDate = String.valueOf(ulNowDate).getBytes();
                        bDummy1[0] = (byte)ulNowDate;
                        bDummy1[1] = (byte)(ulNowDate >> 8);
                        bDummy1[2] = (byte)(ulNowDate >> 16);
                        bDummy1[3] = (byte)(ulNowDate >> 24);
                        bDummy1[4] = 0x20;
                        if (!SendProtocol_Get( 5, (byte)0xC7, bDummy1)){
                            Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                        }
                        else
                        {
                            Toast.makeText(getActivity(), R.string.multi_applied, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                        }
                        break;

                }
            }
        } ;

        Button btnApply = (Button) v.findViewById(R.id.SystemTime_Option2_Apply) ;
        btnApply.setOnClickListener(onClickListener) ;
        Button btnLoad = (Button) v.findViewById(R.id.SystemTime_Option1_Load) ;
        btnLoad.setOnClickListener(onClickListener) ;
        Button btnVersionLoad = (Button) v.findViewById(R.id.SystemTime_Version1_Load) ;
        btnVersionLoad.setOnClickListener(onClickListener) ;
        return v;
    }


    long  date_to_count(int nYear, int nMonth, int nDay, int nHour, int nMinute, int nSecond)
    {
        long ulRtcCount;
        if (nYear < 2000) return 0;

        ulRtcCount = DMYToDate(nDay, nMonth, nYear);
        ulRtcCount -= 36525;  // 2000년 기준으로 변경.

        ulRtcCount *= 86400;  // 초로 변환..

        ulRtcCount +=(long)(nHour * 3600 + nMinute * 60 + nSecond); // 시간 더함.

        return ulRtcCount;
    }

    long DMYToDate(int nDay, int nMonth, int nYear)
    {
        int nSerialDate =
                ((1461 * (nYear + 4800 + ((nMonth - 14) / 12))) / 4) +
                        ((367 * (nMonth - 2 - 12 * ((nMonth - 14) / 12))) / 12) -
                        ((3 * (((nYear + 4900 + ((nMonth - 14) / 12)) / 100))) / 4) +
                        nDay - 2415019 - 32075;

        return (long)nSerialDate;
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



    public static SubActivitySetupSystemTime newInstance(String text) {

        SubActivitySetupSystemTime f = new SubActivitySetupSystemTime();
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

