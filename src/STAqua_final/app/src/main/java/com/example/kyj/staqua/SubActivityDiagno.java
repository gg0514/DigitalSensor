package com.example.kyj.staqua;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import static com.example.kyj.staqua.MainActivity.DataCommunicationThread.nTimer;
import static com.example.kyj.staqua.MainActivity.FileLogWriter;
import static com.example.kyj.staqua.MainActivity.m_DataCommunicationThread;
import static com.example.kyj.staqua.MainActivity.m_bMvRead;
import static com.example.kyj.staqua.MainActivity.startDataCommunicationThread;
import static com.example.kyj.staqua.SubActivityDiagnoAlarm.ClearAlarmData;
import static com.example.kyj.staqua.SubActivityDiagnoAlarm.GetAlarmData;

public class SubActivityDiagno extends AppCompatActivity implements ActionBar.TabListener {

    AppSectionsPagerAdapter mAppSectionsPagerAdapter;

    static String m_strSensorDiag = "";
    static String m_strAlarmHist = "";
    static String m_strTuDiag = "";
    static String m_strSensorDiagCheck = "";
    //ViewPager에는 한번에 하나의 섹션만 보여진다.
    static ViewPager mViewPager;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sub_activity_diagno);
        super.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        m_strSensorDiag = getResources().getString(R.string.multi_sensor_diagnosis);
        m_strAlarmHist = getResources().getString(R.string.multi_alarm_history);
    //    m_strTuDiag = getResources().getString(R.string.multi_turbidity_diagnosis);
        m_strSensorDiagCheck = getResources().getString(R.string.multi_sensor_check);

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
        mViewPager = (ViewPager) findViewById(R.id.sub_activity_diagno);
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
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            //어댑터에서 정의한 페이지 제목을 탭에 보이는 문자열로 사용한다.
                            .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                            //TabListener 인터페이스를 구현할 액티비티 오브젝트도 지정한다.
                            .setTabListener(this));

        }
        nTimer = 1000;
      //  SendProtocol((byte)0x13, 1, "0", m_strMode);
        FileLogWriter(getString(R.string.multi_diagnosis),"Start Activity");
    }

    @Override
    public void onStop() {
     //   SendProtocol((byte)0x11, 1, "0", m_strMode);
        startDataCommunicationThread();
        nTimer = 800;
        FileLogWriter(getString(R.string.multi_cal_ch),"Stop Activity");
        super.onStop();
    }

    @Override //액션바의 탭 선택시 호출됨
    public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
        //액션바에서 선택된 탭에 대응되는 페이지를 뷰페이지에서 현재 보여지는 페이지로 변경한다.
        mViewPager.setCurrentItem(tab.getPosition());
        int position = tab.getPosition();
        if (position == 0)
        {
            m_bMvRead = true;
        //    m_DataCommunicationThread.stopDCThread();
        //    nTimer = 1000;
        }
        else if (position == 1)
        {
            nTimer = 1500;
            GetAlarmData();
        }
        else if (position == 2)
        {
            m_DataCommunicationThread.stopDCThread();
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
        int position = tab.getPosition();
        if (position == 0)
        {
            m_bMvRead = false;   //    startDataCommunicationThread();
        }
        else if (position == 1)
        {
            ClearAlarmData();
        }
        else if (position == 2)
        {
            startDataCommunicationThread();
        }
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
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
                case 0:
                {
                    return SubActivityDiagnoSensorCheck.newInstance("Sensor Check, Instance 1");
                }
                case 1:
                {
                    return SubActivityDiagnoAlarm.newInstance("Alarm History, Instance 1");
                }
                case 2:
                {
                    return SubActivityDiagnoSensor.newInstance("Sensor Diagnosis, Instance 1");
                  //  return SubActivityDiagnoTurbidity.newInstance("Turbidity Diagnosis, Instance 1");
                }
                default: return SubActivityDiagnoSensor.newInstance("Sensor Diagnosis, Default");
            }
        }

        //프래그먼트를 최대 3개를 생성할 것임
        @Override
        public int getCount() {
            return 1;
        }

        //탭의 제목으로 사용되는 문자열 생성
        @Override
        public CharSequence getPageTitle(int position) {
            //프래그먼트의 인스턴스를 생성한다.
            switch(position) {
                case 0: return m_strSensorDiagCheck;
             //   case 1: return m_strAlarmHist;
             //   case 2: return m_strSensorDiag;
            //    case 2: return m_strTuDiag;
                default: return "";
            }
//
//            return "Section " + (position + 1);
        }
    }
}
