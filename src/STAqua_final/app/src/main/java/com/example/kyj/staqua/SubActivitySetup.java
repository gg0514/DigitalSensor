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

/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivitySetup extends AppCompatActivity implements ActionBar.TabListener {

    AppSectionsPagerAdapter mAppSectionsPagerAdapter;

    static String m_strSerial = "";
    static String m_strEthernet = "";
    static String m_strBluetooth = "";
    static String m_strSystem = "";
    static String m_strAddition= "";
    static String m_strItemReplacement="";
    static String m_strReplacementHistory ="";
    static String m_strDataFilter = "";
    static String m_strTemperatureElectrode = "";
    static String m_strSystemTime ="";

    //ViewPager에는 한번에 하나의 섹션만 보여진다.
    static ViewPager mViewPager;

//    // Intent 요구 코드 값
//    static final int REQUEST_CONNECT_DEVICE = 1;
//    static final int REQUEST_ENABLE_BT = 2;
//    // Message 타입
//    public static final int MESSAGE_STATE_CHANGE = 1;
//    public static final int MESSAGE_READ = 2;
//    public static final int MESSAGE_WRITE = 3;
//    public static final int MESSAGE_DEVICE_NAME = 4;
//    public static final int MESSAGE_TOAST = 5;
    // 블루투스 아답터
//    BluetoothAdapter btAdapter;
//    private static DeviceConnector connector;
//    private String m_deviceName;
//    private static BluetoothResponseHandler mBTHandler;
//    private static final String SAVED_PENDING_REQUEST_ENABLE_BT = "PENDING_REQUEST_ENABLE_BT";
//    private static final String DEVICE_NAME = "DEVICE_NAME";
//    boolean pendingRequestEnableBt = false;
//    // 블루투스 시작 종료를 위한 값
//    private TimerTask m_delayTime;
//    private final Handler m_handler_BTReconnect = new Handler();
//    int time_sec;




    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sub_activity_setup);
        super.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        m_strSerial = getResources().getString(R.string.multi_serial);
        m_strEthernet = getResources().getString(R.string.multi_ethernet);
        m_strBluetooth = getResources().getString(R.string.multi_bluetooth);
        m_strSystem = getResources().getString(R.string.multi_system);
        m_strAddition = getResources().getString(R.string.multi_additional_function);
        m_strItemReplacement=getResources().getString(R.string.multi_item_replacement);
        m_strReplacementHistory =getResources().getString(R.string.multi_replacement_history);
        m_strDataFilter = getResources().getString(R.string.multi_data_filter);
        m_strTemperatureElectrode = getResources().getString(R.string.multi_temperature_select);
        m_strSystemTime = getResources().getString(R.string.multi_systemtime);
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

//        SpannableString s = new SpannableString(getResources().getString(R.string.app_name));
//        s.setSpan(new TypefaceSpan("normal"), 0, s.length(),
//                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        actionBar.setTitle(s);


        //ViewPager를 정의하고
        mViewPager = (ViewPager) findViewById(R.id.sub_activity_setup);
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

//        // 블루투스를 위한 기본 설정
//        if (mBTHandler == null) mBTHandler = new BluetoothResponseHandler(this);
//        else mBTHandler.setTarget(this);
//        if (isConnected() && (savedInstanceState != null)) {
//            setDeviceName(savedInstanceState.getString(DEVICE_NAME));
//        } else setDeviceName("");
//        if (savedInstanceState != null) {
//            pendingRequestEnableBt = savedInstanceState.getBoolean(SAVED_PENDING_REQUEST_ENABLE_BT);
//        }
//        btAdapter = BluetoothAdapter.getDefaultAdapter();

        nTimer = 1500;
     //   SendProtocol((byte)0x11, 1, "0", m_strMode);
        FileLogWriter(getString(R.string.multi_setup),"Start Activity");
    }

    @Override
    public void onStop() {
        nTimer = 800;
   //     SendProtocol((byte)0x11, 1, "0", m_strMode);
        FileLogWriter(getString(R.string.multi_setup),"Stop Activity");
        super.onStop();
    }

    @Override //액션바의 탭 선택시 호출됨
    public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
        //액션바에서 선택된 탭에 대응되는 페이지를 뷰페이지에서 현재 보여지는 페이지로 변경한다.
        mViewPager.setCurrentItem(tab.getPosition());
        Log.d(this.getClass().getSimpleName(), "onTabSelected()");

        int position = tab.getPosition();
        switch( position ){
//            case 0: // 첫번째 Tab
//                SubActivitySetupSerial.CheckBtnStatus();
//                break;
//            case 1: //두번째 Tab
//                SubActivitySetupEthernet.CheckBtnStatus();
//                break;
//            case 1: //세번째 Tab
//                SubActivitySetupBluetooth.CheckBtnStatus();
//                break;
        }

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
        Log.d(this.getClass().getSimpleName(), "onTabUnselected()");
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
        Log.d(this.getClass().getSimpleName(), "onTabReselected()");
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
                case 0: return SubActivitySetupSerial.newInstance("Serial, Instance 1");
                case 1: return SubActivitySetupEthernet.newInstance("Ethernet, Instance 1");
    //            case 1: return SubActivitySetupBluetooth.newInstance("Bluetooth, Instance 1");
                case 2: return SubActivitySetupSystem.newInstance("System, Instance 1");
    //            case 2: return SubActivitySetupAddition.newInstance("Additional, Instance 1");
                case 3: return SubActivitySetupItemsReplacement.newInstance("Items Replacement, Instance 1");
                case 4: return SubActivitySetupReplacementHistory.newInstance("Replacement History, Instance 1");
    //            case 4: return SubActivitySetupDatafilter.newInstance("DataFilter, Instance 1");
                case 5: return SubActivitySetupTemperatureelectrode.newInstance("Temperature Electrode, Instance 1");
                case 6: return SubActivitySetupSystemTime.newInstance("System Time, Instance 1");
                default: return SubActivitySetupBluetooth.newInstance("ThirdFragment, Default");
            }
        }

        //프래그먼트를 최대 5개를 생성할 것임
        @Override
        public int getCount() {
            return 7;
        }

        //탭의 제목으로 사용되는 문자열 생성
        @Override
        public CharSequence getPageTitle(int position) {
            //프래그먼트의 인스턴스를 생성한다.

            switch(position) {
                case 0: return m_strSerial;
                case 1: return m_strEthernet;
        //        case 1: return m_strBluetooth;
                case 2: return m_strSystem;
        //        case 2: return m_strAddition;
                case 3: return m_strItemReplacement;
                case 4: return m_strReplacementHistory;
        //        case 4: return m_strDataFilter;
                case 5: return m_strTemperatureElectrode;
                case 6: return m_strSystemTime;
                default: return "";
            }
//            return "Section " + (position + 1);
        }
    }
}





//    public void ClickListButton() {
//        if (isAdapterReady()) {
//            if (isConnected()) stopConnection();
//            else startDeviceListActivity();
//        } else {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        }
 //   }
//
//    public void ClickDisableButton() {
//        DisableBlueTooth ();
//    }
//
//    public void ClickEnableButton() {
//        EnableBlueTooth ();
//    }
//
//    public void DisableBlueTooth () {
//        time_sec = 0;
//        m_delayTime = new TimerTask() {
//            @Override
//            public void run() {
//                if(btAdapter.getState() == BluetoothAdapter.STATE_TURNING_ON ||
//                        btAdapter.getState() == btAdapter.STATE_ON) {
//                    btAdapter.disable();
//                }
//             EnableBlueTooth();
//                time_sec++;
//            }
//        };
     //   Timer timer = new Timer();
      //  timer.schedule(m_delayTime, 2000);

//        if(btAdapter.getState() == BluetoothAdapter.STATE_TURNING_ON ||
//                btAdapter.getState() == btAdapter.STATE_ON) {
//            btAdapter.disable();
//        }

 //   protected  void EnableBlueTooth(){
//        Runnable update = new Runnable() {
//            @Override
//            public void run() {
//                btAdapter.enable();
//            }
//        };
//        m_handler_BTReconnect.post(update);
//    }

//    boolean isAdapterReady() {
//        return (btAdapter != null) && (btAdapter.isEnabled());
//    }
//    private boolean isConnected() {
//        return (connector != null) && (connector.getState() == DeviceConnector.STATE_CONNECTED);
//    }
//    private void startDeviceListActivity() {
//        stopConnection();
//        Intent serverIntent = new Intent(this, DeviceListActivity.class);
//        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
//    }
//    private void stopConnection() {
//        if (connector != null) {
//            connector.stop();
//            connector = null;
//            m_deviceName = null;
//        }
//    }
//    void setDeviceName(String deviceName) {
//        m_deviceName = deviceName;
//        //txHw.setText(m_deviceName);
//        //getActionBar().setSubtitle(deviceName);
//    }
//
//    private void setupConnector(BluetoothDevice connectedDevice) {
//        stopConnection();
//        try {
//               String emptyName = getString(R.string.empty_device_name);
//               DeviceData data = new DeviceData(connectedDevice, emptyName);
//               connector = new DeviceConnector(data, mBTHandler);
//               connector.connect();
//        } catch (IllegalArgumentException e) {
//            // Utils.log("setupConnector failed: " + e.getMessage());
//        }
//    }
//
//    private static class BluetoothResponseHandler extends Handler {
//        private WeakReference<SubActivitySetup> mActivity;
//        public BluetoothResponseHandler(SubActivitySetup activity) {
//            mActivity = new WeakReference<SubActivitySetup>(activity);
//        }
//        public void setTarget(SubActivitySetup target) {
//            mActivity.clear();
//            mActivity = new WeakReference<SubActivitySetup>(target);
//        }
//        @Override
//        public void handleMessage(Message msg) {
//            SubActivitySetup activity = mActivity.get();
//            if (activity != null) {
//                switch (msg.what) {
//                    case MESSAGE_STATE_CHANGE:
//                        final android.app.ActionBar bar = activity.getActionBar();
//                        switch (msg.arg1) {
//                            case DeviceConnector.STATE_CONNECTED:
//                                m_strStatus = activity.getText(R.string.connected).toString();
//                                Toast.makeText(activity, R.string.connect_success, Toast.LENGTH_SHORT).show();
//                                break;
//                            case DeviceConnector.STATE_CONNECTING:
//                                //activity.txHw.setText(activity.m_deviceName);
//                                //      activity.txStatus.setText("Connecting ...");
////                                activity.btnList.setText("Pair/Connect");
////                                activity.AddLog("Connecting ....");
//                                m_strStatus = activity.getText(R.string.on_connecting).toString();
//                                Toast.makeText(activity, R.string.on_connecting, Toast.LENGTH_SHORT).show();
//                                break;
//                            case DeviceConnector.STATE_NONE:
//                                //activity.txHw.setText("");
//                                //      activity.txStatus.setText("Disconnected");
////                                activity.btnList.setText("Pair/Connect");
////                                activity.AddLog("Disconnected");
//                                m_strStatus = activity.getText(R.string.disconnected).toString();
//                                Toast.makeText(activity, R.string.connect_failure, Toast.LENGTH_SHORT).show();
//                                break;
//                        }
//                        activity.invalidateOptionsMenu();
//                        break;
//                    case MESSAGE_READ:
//                        int nLength = msg.arg1;
//                        byte[] btBuffer;
//                        btBuffer = (byte[])msg.obj;
//                        if (nLength > 1) SplitResponseProtocol(btBuffer, nLength);
//                        break;
//                    case MESSAGE_DEVICE_NAME:
//                        // activity.txHw.setText((String) msg.obj);
//                        break;
//                    case MESSAGE_WRITE:
//                        // stub
//                        break;
//                    case MESSAGE_TOAST:
//                        // stub
//                        break;
//                }
//            }
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case REQUEST_CONNECT_DEVICE:
//                // When DeviceListActivity returns with a device to connect
//                if (resultCode == Activity.RESULT_OK) {
//                    String address = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
//                    BluetoothDevice device = btAdapter.getRemoteDevice(address);
//                    if (isAdapterReady() && (connector == null)) setupConnector(device);
//                }
//                break;
//            case REQUEST_ENABLE_BT:
//                // When the request to enable Bluetooth returns
//                pendingRequestEnableBt = false;
//                if (resultCode != Activity.RESULT_OK) {
//                    // Utils.log("BT not enabled");
//                }
//                break;
//        }
//    }

