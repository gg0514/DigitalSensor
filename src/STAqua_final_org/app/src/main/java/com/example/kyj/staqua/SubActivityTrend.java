package com.example.kyj.staqua;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import static com.example.kyj.staqua.MainActivity.DataCommunicationThread.nTimer;
import static com.example.kyj.staqua.MainActivity.FileLogWriter;
import static com.example.kyj.staqua.MainActivity.m_DataCommunicationThread;
import static com.example.kyj.staqua.MainActivity.m_bIsDownloadTrend;
import static com.example.kyj.staqua.MainActivity.m_strCh;
import static com.example.kyj.staqua.MainActivity.m_strCo;
import static com.example.kyj.staqua.MainActivity.m_strDataDelete;
import static com.example.kyj.staqua.MainActivity.m_strDataDownload;
import static com.example.kyj.staqua.MainActivity.m_strExcel;
import static com.example.kyj.staqua.MainActivity.m_strFilter;
import static com.example.kyj.staqua.MainActivity.m_strFlow;
import static com.example.kyj.staqua.MainActivity.m_strPh;
import static com.example.kyj.staqua.MainActivity.m_strTemp;
import static com.example.kyj.staqua.MainActivity.m_strTu;
import static com.example.kyj.staqua.MainActivity.startDataCommunicationThread;
import static com.example.kyj.staqua.MainActivity.startDisplayValueThread;

/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivityTrend extends AppCompatActivity implements ActionBar.TabListener {

    AppSectionsPagerAdapter mAppSectionsPagerAdapter;

//    static String m_strPh= "";
//    static String m_strCh= "";
//    static String m_strTu= "";
//    static String m_strCo= "";
//    static String m_strTemp= "";
//    static String m_strFlow= "";
//    static String m_strDataDelete= "";
    //ViewPager에는 한번에 하나의 섹션만 보여진다.
    static ViewPager mViewPager;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.sub_activity_trend);
        m_strPh = getResources().getString(R.string.multi_ph1);
        m_strCh = getResources().getString(R.string.multi_ch1);
        m_strTu = getResources().getString(R.string.multi_tu1);
        m_strCo = getResources().getString(R.string.multi_co1);
        m_strTemp = getResources().getString(R.string.multi_temp1);
        m_strFlow = getResources().getString(R.string.multi_flow);
        m_strFilter = getResources().getString(R.string.multi_filter);
        m_strDataDelete = getResources().getString(R.string.multi_delete);
        m_strDataDownload = getResources().getString(R.string.multi_download);
        m_strExcel = getResources().getString(R.string.multi_excel);
        //어댑터를 생성한다. 섹션마다 프래그먼트를 생성하여 리턴해준다.
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());
        //액션바를 설정한다.
        final ActionBar actionBar = getSupportActionBar();

        // 액션바 색상 변경
        actionBar.setBackgroundDrawable(new ColorDrawable(0xFF21222A));
        //액션바 코너에 있는 Home버튼을 비활성화 한다.

        //탭을 액션바에 보여줄 것이라고 지정한다.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeButtonEnabled(true);


        //ViewPager를 정의하고
        mViewPager = (ViewPager) findViewById(R.id.sub_activity_trend);
        //ViewPager에 어댑터를 연결한다.
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        //사용자가 섹션사이를 스와이프할때 발생하는 이벤트에 대한 리스너를 설정한다.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override //스와이프로 페이지 이동시 호출됨
            public void onPageSelected(int position) {
                //swipe - 손가락을 화면에 댄 후, 일직선으로 드래그했다가 손을 떼는 동작이다.
                //화면을 좌우로 스와이핑하여 섹션 사이를 이동할 때, 현재 선택된 탭의 위치이다.

                //액션바의 탭위치를 페이지 위치에 맞춘다.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        //각각의 섹션을 위한 탭을 액션바에 추가한다.
        for (int i = 0; i <mAppSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            //어댑터에서 정의한 페이지 제목을 탭에 보이는 문자열로 사용한다.
                            .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                            //TabListener 인터페이스를 구현할 액티비티 오브젝트도 지정한다.
                            .setTabListener(this));

        }
        nTimer = 1500;
     //   SendProtocol((byte)0x11, 1, "0", m_strMode);
        FileLogWriter(getString(R.string.multi_trend),"Start Activity");

    }

    @Override
    public void onStop() {

        startDataCommunicationThread();
        startDisplayValueThread();
        m_bIsDownloadTrend = false;
        m_DataCommunicationThread.nCommand = 3;
        m_DataCommunicationThread.strSensor = "0";
        nTimer = 800;
    //    SendProtocol((byte)0x11, 1, "0", m_strMode);
        FileLogWriter(getString(R.string.multi_trend),"Stop Activity");
        super.onStop();
    }
    @Override //액션바의 탭 선택시 호출됨
    public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
        //액션바에서 선택된 탭에 대응되는 페이지를 뷰페이지에서 현재 보여지는 페이지로 변경한다.
        mViewPager.setCurrentItem(tab.getPosition());
        int position = tab.getPosition();
        if (position == 0)
        {
            m_DataCommunicationThread.stopDCThread();
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
        Log.d(this.getClass().getSimpleName(), "onTabSelected()");
        int position = tab.getPosition();
        if (position == 0)
        {
            startDataCommunicationThread();
            m_DataCommunicationThread.nCommand = 3;
            m_DataCommunicationThread.strSensor = "0";
            nTimer = 800;
        }
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
        int position = tab.getPosition();
        if (position == 0)
        {
        }
        else if (position == 1)
        {
        }
        else if (position == 2)
        {
        }
    }

    //세션에 대응되는 프래그먼트를 리턴한다
    //FragmentPagerAdapter는 메모리에 프래그먼트를 로드한 상태로 유지하지만(3개 프래그먼트 유지하는게 적당함)
    //FragmentStatePagerAdapter는 화면에 보이지 않는 프래그먼트는 메모리에서 제거한다.
    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {
        private FragmentManager fm;

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fm = fm;
        }

        @Override
        public Fragment getItem(int pos) {
            //태그로 프래그먼트를 찾는다.
            Fragment fragment = fm.findFragmentByTag("android:switcher:" + mViewPager.getId() + ":" + getItemId(pos));

            //프래그먼트가 이미 생성되어 있는 경우에는 리턴
            if (fragment != null) {
                return fragment;
            }

            //프래그먼트의 인스턴스를 생성한다.
            switch(pos) {
                case 0: return SubActivityTrendDownload.newInstance("Download, Instance 1");
                case 1: return SubActivityTrendPh.newInstance("pH, Instance 1");
                case 2: return SubActivityTrendCh.newInstance("Chlorine, Instance 1");
                case 3: return SubActivityTrendTu.newInstance("Turbidity, Instance 1");
                case 4: return SubActivityTrendCo.newInstance("Conductivity, Instance 1");
                case 5: return SubActivityTrendTemp.newInstance("Temperature, Instance 1");
                case 6: return SubActivityTrendExcel.newInstance("Excel, Instance 1");
                case 7: return SubActivityTrendDataDelete.newInstance("Data Delete, Instance 1");
            //    case 5: return SubActivityTrendFlow.newInstance("Flow, Instance 1");
             //   case 6: return SubActivityTrendFilter.newInstance("Filter, Instance 1");
             //
             //   case 7: return SubActivityTrendDownload.newInstance("Download, Instance 1");
                default: return SubActivityTrendPh.newInstance("pH, Default");
            }
        }

        //프래그먼트를 최대 3개를 생성할 것임
        @Override
        public int getCount() {
            return 8;
        }

        //탭의 제목으로 사용되는 문자열 생성
        @Override
        public CharSequence getPageTitle(int position) {
            //프래그먼트의 인스턴스를 생성한다.
            switch(position) {
                case 0: return m_strDataDownload;
                case 1: return m_strPh;
                case 2: return m_strCh;
                case 3: return m_strTu;
                case 4: return m_strCo;
                case 5: return m_strTemp;
                case 6: return m_strExcel;
                case 7: return m_strDataDelete;
          //      case 6: return m_strFilter;
          //      case 7: return m_strDataDelete;
         //       case 5: return m_strFlow;

         //      case 7: return m_strDataDownload;
                default: return "";
            }
//
//            return "Section " + (position + 1);
        }
    }
}
