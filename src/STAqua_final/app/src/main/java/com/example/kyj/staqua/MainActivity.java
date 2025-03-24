package com.example.kyj.staqua;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.StrictMode;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kyj.DeviceData;
import com.example.kyj.bluetooth.DeviceConnector;
import com.example.kyj.bluetooth.DeviceListActivity;
import com.example.kyj.staqua.utils.CRC16Modbus;
import com.example.kyj.staqua.utils.FTDriver;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.usbserial.driver.UsbSerialDriver;
import com.usbserial.driver.UsbSerialPort;
import com.usbserial.util.SerialInputOutputManager;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.kyj.staqua.MainActivity.DataCommunicationThread.nTimer;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Double.parseDouble;
import static java.lang.Float.intBitsToFloat;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    // 센서 type : 3031303430373130 : 01 04 07 10
    // 01 : pH
    // 04 : Contacting Conductivity
    // 07 : Turbidity
    // 10 : Chlorine
    private static final boolean DEBUG = true;

    // Wash 관련 변수
    public static final int SENSOR_NONE = 0;
    public static final int SENSOR_PH = 1;
    public static final int SENSOR_CO = 4;
    public static final int SENSOR_TU = 7;
    public static final int SENSOR_CH = 10;

    // TU Wash 변수
    static boolean m_bTuWashChange = false;
    static int m_nTuDuration;
    static int m_nTuInterval;
    static int m_nTuStabilization;
    static double m_dTuHighWash;
    static double m_dTuLowWash;
    static boolean m_bTuAutoWash;
    static boolean m_bTuManualWash;
    // CH Wash 변수
    static boolean m_bChWashChange = false;
    static int m_nChDuration;
    static int m_nChInterval;
    static int m_nChStabilization;
    static double m_dChHighWash;
    static double m_dChLowWash;
    static boolean m_bChAutoWash;
    static boolean m_bChManualWash;
    //public static SensorInfo SInfo [];

//    static int m_nSensor1 = 0;
//    static int m_nSensor2 = 0;
//    static int m_nSensor3 = 0;
//    static int m_nSensor4 = 0;
    private static final String DEVICE_NAME = "DEVICE_NAME";
    private GoogleApiClient client;
    public final  Context context = this;
    // Serial Data 통신 위한 변수
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private static SerialInputOutputManager mSerialIoManager;
    private static UsbSerialPort sPort;
    private FTDriver m_Serial;
    private UsbReceiver m_UsbReceiver;
    private int m_Baudrate;

    //USB 허가를 위한 변수
    private static final String ACTION_USB_PERMISSION = "kr.co.andante.mobiledgs.USB_PERMISSION";
    // 내부 로그 확인을 위한 변수
    private static String TAG = "HDJ";

    // info.txt 파일 저장을 위한 변수
    String strInfo[] = {};

    // 센서 이름 설정
    static String m_strPh= "";
    static String m_strCh= "";
    static String m_strTu= "";
    static String m_strCo= "";
    static String m_strTemp= "";
    static String m_strFlow= "";
    static String m_strFilter= "";
    static String m_strDataDelete= "";
    static String m_strDataDownload= "";
    static String m_strExcel= "";

    // 탁도 검증을 위한 센서 번호
    static String m_strTuStickNo = "";
    // Thread 를 위한 Class
    public static DisplayValueThread m_DisplayValueThread;
    public static DataCommunicationThread m_DataCommunicationThread;
    public static WashTuThread m_WashTuThread;
    public static WashTuFlashThread m_WashTuFlashThread;
    public static WashChThread m_WashChThread;
    public static WashChFlashThread m_WashChFlashThread;
    public static LightTxThread m_LightTxThread;
    public static LightRxThread m_LightRxThread;
    public static EthernetCommThread m_EthernetCommThread;

    public static TrendTreatThread m_TrendTreatThread;

    // Text Display 를 위한 설정
    static TextView    txLane1;
    static TextView    txLane2;
    static TextView    txLane3;
    static TextView    txLane4;
    static TextView    txLane5;
    static TextView    txLane6;

    static Button btnConnect;
    static Button btnDisConnect;

    static TextView txMode;
    static TextView txStatus;

    static String m_strLane1= "";
    static String m_strLane2= "";
    static String m_strLane3= "";
    static String m_strLane4= "";
    static String m_strLane5= "";
    static String m_strLane6= "";

    static double m_dTu;
    static double m_dCh;
    static double m_dPh;
    static double m_dTemp;
    static double m_dCo;

    // 평균값 계산을 위한 Array
    ArrayList<Double> m_ArrayAverage[] = new ArrayList[5];

    static double [] m_dAverage = new double[]{0,0,0,0,0};
    static double [] m_dDownloadAverage = new double[]{0,0,0,0,0};

    double [] m_dCL = new double[]{0,0,0,0,0};
    static double [] m_dCLMax = new double[]{0,0,0,0,0};
    static double [] m_dCLMin = new double[]{0,0,0,0,0};
    static boolean [] m_bCLOnRange = new boolean[]{TRUE,TRUE,TRUE,TRUE,TRUE};

    static double [] m_dPreValue = new double[]{0,0,0,0,0};
    static int [] m_nOnRangeTime = new int[]{0,0,0,0,0};
    static int [] m_nOffRangeTime = new int[]{0,0,0,0,0};
    static boolean [] m_bC1Lane = new boolean[]{FALSE,FALSE,FALSE,FALSE,FALSE};

    boolean m_bTu_IsIncrease;
    boolean m_bCh_IsIncrease;
    boolean m_bPh_IsIncrease;
    boolean m_bTemp_IsIncrease;
    boolean m_bCo_IsIncrease;

    int m_nTu_OnRangeTime;
    int m_nCh_OnRangeTime;
    int m_nPh_OnRangeTime;
    int m_nTemp_OnRangeTime;
    int m_nCo_OnRangeTime;
    int m_nTu_OffRangeTime;
    int m_nCh_OffRangeTime;
    int m_nPh_OffRangeTime;
    int m_nTemp_OffRangeTime;
    int m_nCo_OffRangeTime;

    static double m_dHOCL;
    static double m_dCh_PhFixValue;
    static boolean m_bCh_PhCompIsCurrent;

    static int m_nCurrentTempSensor;

    static int m_nTrendFilter;
    static int m_nTrendCount;
    static double m_dTrendConstant;

    static double m_dMvTu;
    static double m_dMvCh;
    static double m_dMvPh;
    static double m_dMvTemp;
    static double m_dMvCo;

    static int m_nFilterTu;
    static int m_nFilterCh;
    static int m_nFilterPh;
    static int m_nFilterCo;
    static boolean m_bFilterChange = false;

    static boolean m_bSignalTu;
    static boolean m_bSignalCh;
    static boolean m_bSignalPh;
    static boolean m_bSignalTemp;
    static boolean m_bSignalCo;

    static boolean m_bDisplayErrorCheckTu;
    static boolean m_bDisplayErrorCheckCh;
    static boolean m_bDisplayErrorCheckPh;
    static boolean m_bDisplayErrorCheckTemp;
    static boolean m_bDisplayErrorCheckCo;

    static String m_strEquipmentSerial;
    static String m_strTuSerial;
    static String m_strChSerial;
    static String m_strPhSerial;
    static String m_strTempSerial;
    static String m_strCoSerial;

    static String m_strSerialStandard;
    static String m_strBaudRate;
    static String m_strStopBit;
    static String m_strDataBit;
    static String m_strParity;
    static String m_strStation;


    static  String m_strIpAddress = "";
    static  String m_strSubnetMask = "";
    static  String m_strGateway = "";
    static  String m_strPort = "";

    static int m_nTuFilterNumber = 0;
    static int m_nChFilterNumber = 0;
    static int m_nPhFilterNumber = 0;
    static int m_nCoFilterNumber = 0;

    static boolean m_bHoldAll;

    static TextView    txHLane1;
    static TextView    txRLane1;
    static TextView    txC1Lane1;
    static TextView    txC2Lane1;
    static ImageView ivAlarmLane1;
    static TextView    txHLane2;
    static TextView    txRLane2;
    static TextView    txC1Lane2;
    static TextView    txC2Lane2;
    static ImageView ivAlarmLane2;
    static TextView    txHLane3;
    static TextView    txRLane3;
    static TextView    txC1Lane3;
    static TextView    txC2Lane3;
    static ImageView ivAlarmLane3;
    static TextView    txHLane4;
    static TextView    txRLane4;
    static TextView    txC1Lane4;
    static TextView    txC2Lane4;
    static ImageView ivAlarmLane4;
    static TextView    txHLane5;
    static TextView    txRLane5;
    static TextView    txC1Lane5;
    static TextView    txC2Lane5;
    static ImageView ivAlarmLane5;

    static ImageView ivHold;
    static ImageView ivWashT;
    static ImageView ivWashC;

    static ImageView ivTX;
    static ImageView ivRX;


    // 출력->디지털 설정 설정 값
    static double m_dPhHighAlarm;
    static double m_dPhLowAlarm;
    static boolean m_bPhPhaseIsHigh;
    static double m_dPhDeadBand;
    static int m_nPhOnDelay;
    static int m_nPhOffDelay;
    static double m_dPhSetPoint;
    static double m_dChHighAlarm;
    static double m_dChLowAlarm;
    static boolean m_bChPhaseIsHigh;
    static double m_dChDeadBand;
    static int m_nChOnDelay;
    static int m_nChOffDelay;
    static double m_dChSetPoint;

    static double m_dTuHighAlarm;
    static double m_dTuLowAlarm;
    static boolean m_bTuPhaseIsHigh;
    static double m_dTuDeadBand;
    static int m_nTuOnDelay;
    static int m_nTuOffDelay;
    static double m_dTuSetPoint;
    static double m_dCoHighAlarm;
    static double m_dCoLowAlarm;
    static boolean m_bCoPhaseIsHigh;
    static double m_dCoDeadBand;
    static int m_nCoOnDelay;
    static int m_nCoOffDelay;
    static double m_dCoSetPoint;
    static double m_dTempHighAlarm;
    static double m_dTempLowAlarm;
    static boolean m_bTempPhaseIsHigh;
    static double m_dTempDeadBand;
    static int m_nTempOnDelay;
    static int m_nTempOffDelay;
    static double m_dTempSetPoint;

    static boolean m_bPreAlarmLane1 = FALSE;
    static boolean m_bPreAlarmLane2 = FALSE;
    static boolean m_bPreAlarmLane3 = FALSE;
    static boolean m_bPreAlarmLane4 = FALSE;
    static boolean m_bPreAlarmLane5 = FALSE;
    static boolean m_bAlarmLane1;
    static boolean m_bAlarmLane2;
    static boolean m_bAlarmLane3;
    static boolean m_bAlarmLane4;
    static boolean m_bAlarmLane5;

    static boolean m_bMvRead = FALSE;

    // 옵션값 관련 맴버 변수
    public static final String KEY_MODE = "my_mode";
    public static final String KEY_STATUS = "my_status";

    // 통신 관련 멤버 변수
    SharedPreferences prefs;
    static String m_strMode;
    static String m_strStatus;
    static int m_nCaliStatus;

    // 이더넷 통신을 위한 설정 값
    private Handler mTCPSocketHandler;
    static private Socket socket;
    private static DataInputStream networkReader;
    private static DataOutputStream networkWriter;



    //보드에서 응답이 왔는지 확인하는 멤버 변수
    //static byte m_btCaliResponse;


    // Intent 요구 코드 값
    static final int REQUEST_CONNECT_DEVICE = 1;
    static final int REQUEST_ENABLE_BT = 2;
    // Message 타입
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static BluetoothAdapter btAdapter;
    private static DeviceConnector connector;
    public static String m_deviceName;
    private static BluetoothResponseHandler mHandler;
    private static final String SAVED_PENDING_REQUEST_ENABLE_BT = "PENDING_REQUEST_ENABLE_BT";
    boolean pendingRequestEnableBt = false;
    // 블루투스 시작 종료를 위한 값
    private TimerTask m_delayTime;
    private final Handler m_handler_BTReconnect = new Handler();
    int time_sec;


    public static int m_nTrendReadCount = 0;
    public static int m_nTrendProtocolLength = 0;
    public static boolean m_bIsDownloadTrend = false;

    public static int m_nTrendExcelLength = 0;
    public static int m_nTrendExcelCount = 0;



    static ArrayList<Integer> arrayTimeTrend;
    static ArrayList<Double> arrayPhTrend;
    static ArrayList<Double> arrayCoTrend;
    static ArrayList<Double> arrayTuTrend;
    static ArrayList<Double> arrayClTrend;
    static ArrayList<Double> arrayTempTrend;
    static ArrayList<Double> arrayFlowTrend;
//    static ArrayList<Float> arrayPhTrend;
//    static ArrayList<Float> arrayCoTrend;
//    static ArrayList<Float> arrayTuTrend;
//    static ArrayList<Float> arrayClTrend;
//    static ArrayList<Float> arrayTempTrend;
//    ArrayList<Float> arrayFlowTrend;
    static byte[] m_bDownLoadData = new byte[4063232];

    static boolean m_bisFirstConnect = true;

    private Messenger mServiceMessenger = null;


    static boolean m_bTXRX = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        super.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View view = toolbar.getChildAt(i);
            if (view instanceof TextView) {
                TextView tv = (TextView) view;
                if (tv.getText().equals(getTitle())) {
                    tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                    break;
                }
            }
        }

        arrayTimeTrend = new ArrayList();
        arrayPhTrend = new ArrayList();
        arrayCoTrend = new ArrayList();
        arrayTuTrend = new ArrayList();
        arrayClTrend = new ArrayList();
        arrayTempTrend = new ArrayList();
        arrayFlowTrend = new ArrayList();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

   //     setTitle(getString(R.string.multi_menu));

        // Text Display 를 위한 설정
        txLane1 = (TextView)findViewById(R.id.Lane1_Box_Value);
        txLane2 = (TextView)findViewById(R.id.Lane2_Box_Value);
        txLane3 = (TextView)findViewById(R.id.Lane3_Box_Value);
        txLane4 = (TextView)findViewById(R.id.Lane4_Box_Value);
        txLane5 = (TextView)findViewById(R.id.Lane5_Box_Value);
       // txLane6 = (TextView)findViewById(R.id.Lane6_Box_Value);
        txMode = (TextView)findViewById(R.id.Info_Mode);
        txStatus = (TextView)findViewById(R.id.Info_Status);
        btnConnect = (Button) findViewById(R.id.Info_Button) ;
       // btnDisConnect = (Button) findViewById(R.id.Info_Button1);
        findViewById(R.id.Info_Button).setOnClickListener(mClickListener);
        txMode.setText(m_strMode);
        txStatus.setText(m_strStatus);

        // 알림표시 Display 를 설정
        txHLane1 = (TextView)findViewById(R.id.Lane1_Box_H);
        txRLane1 = (TextView)findViewById(R.id.Lane1_Box_R);
        txC1Lane1 = (TextView)findViewById(R.id.Lane1_Box_C1);
        txC2Lane1 = (TextView)findViewById(R.id.Lane1_Box_C2);
        ivAlarmLane1 = (ImageView)findViewById(R.id.Lane1_Box_Alarm);
        txHLane2 = (TextView)findViewById(R.id.Lane2_Box_H);
        txRLane2 = (TextView)findViewById(R.id.Lane2_Box_R);
        txC1Lane2 = (TextView)findViewById(R.id.Lane2_Box_C1);
        txC2Lane2 = (TextView)findViewById(R.id.Lane2_Box_C2);
        ivAlarmLane2 = (ImageView)findViewById(R.id.Lane2_Box_Alarm);
        txHLane3 = (TextView)findViewById(R.id.Lane3_Box_H);
        txRLane3 = (TextView)findViewById(R.id.Lane3_Box_R);
        txC1Lane3 = (TextView)findViewById(R.id.Lane3_Box_C1);
        txC2Lane3 = (TextView)findViewById(R.id.Lane3_Box_C2);
        ivAlarmLane3 = (ImageView)findViewById(R.id.Lane3_Box_Alarm);
        txHLane4 = (TextView)findViewById(R.id.Lane4_Box_H);
        txRLane4 = (TextView)findViewById(R.id.Lane4_Box_R);
        txC1Lane4 = (TextView)findViewById(R.id.Lane4_Box_C1);
        txC2Lane4 = (TextView)findViewById(R.id.Lane4_Box_C2);
        ivAlarmLane4 = (ImageView)findViewById(R.id.Lane4_Box_Alarm);
        txHLane5 = (TextView)findViewById(R.id.Lane5_Box_H);
        txRLane5 = (TextView)findViewById(R.id.Lane5_Box_R);
        txC1Lane5 = (TextView)findViewById(R.id.Lane5_Box_C1);
        txC2Lane5 = (TextView)findViewById(R.id.Lane5_Box_C2);
        ivAlarmLane5 = (ImageView)findViewById(R.id.Lane5_Box_Alarm);

        // 액션바 색상 변경
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(0xFF21222A));
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        //actionBar.hide();
        // USB 권한 설정
        m_Serial = new FTDriver((UsbManager) getSystemService(Context.USB_SERVICE));
        m_UsbReceiver = new UsbReceiver(this, m_Serial);
        IntentFilter filter = new IntentFilter();
        filter.addAction (UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction (UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver (m_UsbReceiver, filter);
        m_Baudrate = m_UsbReceiver.loadDefaultBaudrate();

        // USB 허가를 위한 시작
        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        m_Serial.setPermissionIntent(permissionIntent);
        Log.d(TAG, "FTDriver beginning");

//        askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE, READ_EXIST);
//        askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXST);
//        ExcelExporter.export();

        // 파일 권한을 위한 시작
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        // 블루투스 허가를 위한 시작
        // 블루투스
        if (mHandler == null) mHandler = new BluetoothResponseHandler(this);
        else mHandler.setTarget(this);
        if (isConnected() && (savedInstanceState != null)) {
            setDeviceName(savedInstanceState.getString(DEVICE_NAME));
        } else setDeviceName("");
        if (savedInstanceState != null) {
            pendingRequestEnableBt = savedInstanceState.getBoolean(SAVED_PENDING_REQUEST_ENABLE_BT);
        }
        btAdapter = BluetoothAdapter.getDefaultAdapter();


        // 옵션값 파일로 불러오기
        prefs = getSharedPreferences("PrefName", MODE_PRIVATE);
        m_strMode = prefs.getString(KEY_MODE, "");
        m_strStatus = prefs.getString(KEY_STATUS, "");
        if (m_strMode== "") {
            m_strMode = "BlueTooth";
        }
        if (m_strStatus == "") m_strStatus = "Disconnected";
        m_nCaliStatus = 0;

        // 부과기능의 체크 상태를 가져옵니다
        prefs = getSharedPreferences("ErrorDisplay", MODE_PRIVATE);
        m_bDisplayErrorCheckTu = prefs.getBoolean("CheckTu", false);
        m_bDisplayErrorCheckCh = prefs.getBoolean("CheckCh", false);
        m_bDisplayErrorCheckPh = prefs.getBoolean("CheckPh", false);
        m_bDisplayErrorCheckCo = prefs.getBoolean("CheckCo", false);
        m_bDisplayErrorCheckTemp = prefs.getBoolean("CheckTemp", false);

        // 시리얼 통신 설정값을 가져옵니다
//        prefs = getSharedPreferences("Setup_Ethernet", MODE_PRIVATE);
//        m_strIpAddress = prefs.getString("Ip", "");
//        if (m_strIpAddress == "")     m_strIpAddress = "192.168.000.180";
//        m_strSubnetMask = prefs.getString("Subnet", "");
//        if (m_strSubnetMask == "")  m_strSubnetMask = "255.255.255.000";
//        m_strGateway = prefs.getString("Gateway", "");
//        if (m_strGateway == "")  m_strGateway = "192.168.000.001";
//        m_strPort = prefs.getString("Port", "");
//        if (m_strPort == "")  m_strPort = "9000";

        // 잔류염소의 PH 보상 상태 및 값을 가져옵니다
        prefs = getSharedPreferences("Ch_Comp", MODE_PRIVATE);
        String strFixValue  = prefs.getString("FixValue", "");
        String strCompStatus = prefs.getString("CompStatus","");
        if (strFixValue =="") m_dCh_PhFixValue = 7.00;
        else m_dCh_PhFixValue = parseDouble(strFixValue);
        if (strCompStatus =="") m_bCh_PhCompIsCurrent = TRUE;
        else if (strCompStatus =="CURRENT") m_bCh_PhCompIsCurrent = TRUE;
        else if (strCompStatus =="FIX") m_bCh_PhCompIsCurrent = FALSE;

        // 온도 전극 선택 값을 가져옵니다 (값이 없을때 1번 PH로 설정)
        prefs = getSharedPreferences("Setup_TE", MODE_PRIVATE);
        String strCurrentSensor  = prefs.getString("CurrentSensor", "");
        if (strCurrentSensor =="") m_nCurrentTempSensor = 1;
        else m_nCurrentTempSensor = parseInt(strCurrentSensor);

        // Trend 의 Filter 종류 및 설정 값을 가져옵니다
        prefs = getSharedPreferences("Trend_Filter", MODE_PRIVATE);
        String strTemp  = prefs.getString("Filter", "");
        if (strTemp =="") m_nTrendFilter = 1;
        else m_nTrendFilter = parseInt(strTemp);
        strTemp  = prefs.getString("TrendCount", "");
        if (strTemp =="") m_nTrendCount = 10;
        else m_nTrendCount = parseInt(strTemp);
        strTemp  = prefs.getString("TrendConstant", "");
        if (strTemp =="") m_dTrendConstant = 3.25;
        else m_dTrendConstant = parseDouble(strTemp);

        for (int i = 0; i < 5; i++){
            m_ArrayAverage[i] = new ArrayList<Double>();
        }


        m_strPh = getResources().getString(R.string.multi_ph);
        m_strCo = getResources().getString(R.string.multi_conductivity);
        m_strTemp = getResources().getString(R.string.multi_temperature);
        m_strCh = getResources().getString(R.string.multi_chlorine);
        m_strTu = getResources().getString(R.string.multi_turbidity);

        // 센서 동작 유무 초기화
        m_bSignalTu = TRUE;
        m_bSignalCh = TRUE;
        m_bSignalPh = TRUE;
        m_bSignalTemp = TRUE;
        m_bSignalCo = TRUE;

        // 데이터 값 초기화
        m_dTu = 0;
        m_dCh= 0;
        m_dPh= 0;
        m_dTemp= 0;
        m_dCo = 0;
        m_bTu_IsIncrease = FALSE;
        m_bCh_IsIncrease = FALSE;
        m_bPh_IsIncrease = FALSE;
        m_bTemp_IsIncrease = FALSE;
        m_bCo_IsIncrease = FALSE;
        m_nTu_OnRangeTime = 0;
        m_nCh_OnRangeTime = 0;
        m_nPh_OnRangeTime = 0;
        m_nTemp_OnRangeTime = 0;
        m_nCo_OnRangeTime = 0;
        m_nTu_OffRangeTime = 0;
        m_nCh_OffRangeTime = 0;
        m_nPh_OffRangeTime = 0;
        m_nTemp_OffRangeTime = 0;
        m_nCo_OffRangeTime = 0;


        // Hold 알람등 옵션 값 초기화
        for (int i = 0; i < 5; i++)
       {
            DisplayOption(i+1, false, false, false, false, false);
       }
        // 상태값 초기화
        ivHold = (ImageView)findViewById(R.id.Info_Hold);
        ivWashC = (ImageView)findViewById(R.id.Info_Wash_C);
        ivWashT = (ImageView)findViewById(R.id.Info_Wash_T);
        ivTX = (ImageView)findViewById(R.id.Info_TX);
        ivRX = (ImageView)findViewById(R.id.Info_RX);
        m_bHoldAll = false;

        // 디지털 설정 가져오기
        GetDigitalSettingPreferences();
        // Wash 정보 가져오기
        m_bChManualWash = false;
        m_bTuManualWash = false;
     //   GetTuWashSettingPreferences();
     //   GetClWashSettingPreferences();

        // 센서 정보에대한 생성
//        SInfo = new SensorInfo[6];
//        for (int i = 0; i < 6; i++)
//        {
//            int nIndex = i + 1;
//            SInfo[i] = new SensorInfo(nIndex, 0, 0.0d, 0.0d, "", "");
//        }

        DisplayStatus();

        // Thread 통해 실시간 데이터 수집을 실시 합니다
        m_DisplayValueThread = new DisplayValueThread(true);
        m_DisplayValueThread.start();

        // Thread 통해 데이터 전송을 실시 합니다
        m_DataCommunicationThread = new DataCommunicationThread(true);
        nTimer = 1000;
        m_DataCommunicationThread.start();


        // Thread 를 통해 T_Flush 화면깜빡임을 준비합니다
       // m_WashTuFlashThread = new WashTuFlashThread(true);
        // Thread 를 통해 T_Flush 컨트롤을 준비 합니다
       // m_WashTuThread = new WashTuThread(true);
        // Thread 를 통해 C_Flush 화면깜빡임을 준비합니다
       // m_WashChFlashThread = new WashChFlashThread(true);
        // Thread 를 통해 C_Flush 컨트롤을 준비 합니다
      //  m_WashChThread = new WashChThread(true);

        m_LightTxThread = new LightTxThread(true);
        m_LightRxThread = new LightRxThread(true);

        m_TrendTreatThread = new TrendTreatThread(true);

      //  m_EthernetCommThread = new EthernetCommThread(true);

        // 저장되어 있는 모드를 가지고 통신 연결 시도
//        if (m_strMode.equals("BlueTooth"))
//        {
//            ClickListButton();
//        }
//        else if (m_strMode.equals("Serial"))
//        {
//            ClickSerialConnection();
//        }
//        else if (m_strMode.equals("Ethernet"))
//        {
//            ClickEthernetConnection();
//        }

        FileLogWriter(getString(R.string.multi_main),"Application Start");
    }


    Button.OnClickListener mClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            //이곳에 버튼 클릭시 일어날 일을 적습니다.
            switch (v.getId()) {
                case R.id.Info_Button:
                    if (m_strMode.equals("BlueTooth"))  {
                        if (m_strStatus.equals("Connected")) {
                            byte [] bConnect = new byte[1];
                            bConnect[0] = 0x00;
                            SendProtocol_Set(2, (byte)0xC5, bConnect);
                            stopConnection();
                         //   DisableBlueTooth();
                            m_strStatus = "Disconnected";
                            SetStatus();
                            m_bisFirstConnect = true;
                        }
                        else  ClickListButton();
                    }
                    else if (m_strMode.equals("USB")) {
                        if (m_strStatus.equals("Connected")) {
                            byte [] bConnect = new byte[1];
                            bConnect[0] = 0x00;
                            SendProtocol_Set(2, (byte)0xC5, bConnect);
                            stopConnection();
                            stopIoManager();
                            if (sPort != null) {
                                try { sPort.close();
                                } catch (IOException e) {
                                }
                                sPort = null;
                            }
                            m_strStatus = "Disconnected";
                            SetStatus();
                            m_bisFirstConnect = true;
                        }
                        else ClickSerialConnection();
                    }
                    else if (m_strMode.equals("Ethernet")) {
                        if (m_strStatus.equals("Connected")) {
                            StopEthernet();
                            m_strStatus = "Disconnected";
                            SetStatus();
                            m_bisFirstConnect = true;
                        }
                        else ClickEthernetConnection();
                    }
                    break;
//                case R.id.Info_Button1:
//                    if (m_strMode.equals("BlueTooth"))  {
//                        stopConnection();
//                    }
//                    else if (m_strMode.equals("Serial")) {
//                        if (m_strStatus.equals("Connected")) {
//                            stopIoManager();
//                            if (sPort != null) {
//                                try { sPort.close();
//                                } catch (IOException e) {
//                                }
//                                sPort = null;
//                            }
//                            m_strStatus = "Disconnected";
//                            SetStatus();
//                        }
//                    }
//                    break;
            }
        }
    };

    static public void DisplayOption(int nSensor, boolean bH, boolean bR, boolean bC1, boolean bC2, boolean bAlarm){
        if (nSensor == 1) // 탁도
        {
            if (bH) txHLane1.setText("H");
            else  txHLane1.setText("");
            if (bR) txRLane1.setText("R");
            else  txRLane1.setText("");
            if (bC1) txC1Lane1.setText("C1");
            else  txC1Lane1.setText("");
            if (bC2) txC2Lane1.setText("C2");
            else  txC2Lane1.setText("");
            if (bAlarm) ivAlarmLane1.setVisibility(View.VISIBLE);
            else  ivAlarmLane1.setVisibility(View.INVISIBLE);
        }
        else if (nSensor == 2) // 잔류염소
        {
            if (bH) txHLane2.setText("H");
            else  txHLane2.setText("");
            if (bR) txRLane2.setText("R");
            else  txRLane2.setText("");
            if (bC1) txC1Lane2.setText("C1");
            else  txC1Lane2.setText("");
            if (bC2) txC2Lane2.setText("C2");
            else  txC2Lane2.setText("");
            if (bAlarm) ivAlarmLane2.setVisibility(View.VISIBLE);
            else  ivAlarmLane2.setVisibility(View.INVISIBLE);
        }
        else if (nSensor == 3) // ph
        {
            if (bH) txHLane3.setText("H");
            else  txHLane3.setText("");
            if (bR) txRLane3.setText("R");
            else  txRLane3.setText("");
            if (bC1) txC1Lane3.setText("C1");
            else  txC1Lane3.setText("");
            if (bC2) txC2Lane3.setText("C2");
            else  txC2Lane3.setText("");
            if (bAlarm) ivAlarmLane3.setVisibility(View.VISIBLE);
            else  ivAlarmLane3.setVisibility(View.INVISIBLE);
        }
        else if (nSensor == 4) // 수온
        {
            if (bH) txHLane4.setText("H");
            else  txHLane4.setText("");
            if (bR) txRLane4.setText("R");
            else  txRLane4.setText("");
            if (bC1) txC1Lane4.setText("C1");
            else  txC1Lane4.setText("");
            if (bC2) txC2Lane4.setText("C2");
            else  txC2Lane4.setText("");
            if (bAlarm) ivAlarmLane4.setVisibility(View.VISIBLE);
            else  ivAlarmLane4.setVisibility(View.INVISIBLE);
        }
        else if (nSensor == 5) // 전기전도도
        {
            if (bH) txHLane5.setText("H");
            else  txHLane5.setText("");
            if (bR) txRLane5.setText("R");
            else  txRLane5.setText("");
            if (bC1) txC1Lane5.setText("C1");
            else  txC1Lane5.setText("");
            if (bC2) txC2Lane5.setText("C2");
            else  txC2Lane5.setText("");
            if (bAlarm) ivAlarmLane5.setVisibility(View.VISIBLE);
            else  ivAlarmLane5.setVisibility(View.INVISIBLE);
        }
    }
    public static void DisplayAlarm(boolean bLane1, boolean bLane2, boolean bLane3, boolean bLane4, boolean bLane5){
        String strSensorStatus="";
        if (bLane1)
        {
            ivAlarmLane1.setVisibility(View.VISIBLE);
            if (!m_bPreAlarmLane1) {
                if (m_bSignalTu)  strSensorStatus = Resources.getSystem().getString(R.string.multi_connect);
                else  strSensorStatus = Resources.getSystem().getString(R.string.multi_disconnect);
                FileCalibrationWriter("Alarm_History", Resources.getSystem().getString(R.string.multi_turbidity), strSensorStatus, Resources.getSystem().getString(R.string.multi_alarm_start));
            }
        }
        else
        {
            ivAlarmLane1.setVisibility(View.INVISIBLE);
            if (m_bPreAlarmLane1) {
                if (m_bSignalTu)  strSensorStatus = Resources.getSystem().getString(R.string.multi_connect);
                else  strSensorStatus = Resources.getSystem().getString(R.string.multi_disconnect);
                FileCalibrationWriter("Alarm_History", Resources.getSystem().getString(R.string.multi_turbidity), strSensorStatus, Resources.getSystem().getString(R.string.multi_alarm_end));
            }
        }
        if (bLane2)
        {
            ivAlarmLane2.setVisibility(View.VISIBLE);
            if (!m_bPreAlarmLane2) {
                if (m_bSignalCh)  strSensorStatus = Resources.getSystem().getString(R.string.multi_connect);
                else  strSensorStatus = Resources.getSystem().getString(R.string.multi_disconnect);
                FileCalibrationWriter("Alarm_History", Resources.getSystem().getString(R.string.multi_chlorine), strSensorStatus, Resources.getSystem().getString(R.string.multi_alarm_start));
            }
        }
        else
        {
            ivAlarmLane2.setVisibility(View.INVISIBLE);
            if (m_bPreAlarmLane2) {
                if (m_bSignalCh)  strSensorStatus = Resources.getSystem().getString(R.string.multi_connect);
                else  strSensorStatus = Resources.getSystem().getString(R.string.multi_disconnect);
                FileCalibrationWriter("Alarm_History", Resources.getSystem().getString(R.string.multi_chlorine), strSensorStatus, Resources.getSystem().getString(R.string.multi_alarm_end));
            }
        }
        if (bLane3)
        {
            ivAlarmLane3.setVisibility(View.VISIBLE);
            if (!m_bPreAlarmLane3) {
                if (m_bSignalPh)  strSensorStatus = Resources.getSystem().getString(R.string.multi_connect);
                else  strSensorStatus = Resources.getSystem().getString(R.string.multi_disconnect);
                FileCalibrationWriter("Alarm_History", Resources.getSystem().getString(R.string.multi_ph), strSensorStatus, Resources.getSystem().getString(R.string.multi_alarm_start));
            }
        }
        else
        {
            ivAlarmLane3.setVisibility(View.INVISIBLE);
            if (m_bPreAlarmLane3) {
                if (m_bSignalPh)  strSensorStatus = Resources.getSystem().getString(R.string.multi_connect);
                else  strSensorStatus = Resources.getSystem().getString(R.string.multi_disconnect);
                FileCalibrationWriter("Alarm_History", Resources.getSystem().getString(R.string.multi_ph), strSensorStatus, Resources.getSystem().getString(R.string.multi_alarm_end));
            }
        }
        if (bLane4)
        {
            ivAlarmLane4.setVisibility(View.VISIBLE);
            if (!m_bPreAlarmLane4) {
                if (m_bSignalTemp)  strSensorStatus = Resources.getSystem().getString(R.string.multi_connect);
                else  strSensorStatus = Resources.getSystem().getString(R.string.multi_disconnect);
                FileCalibrationWriter("Alarm_History", Resources.getSystem().getString(R.string.multi_temperature), strSensorStatus, Resources.getSystem().getString(R.string.multi_alarm_start));
            }
        }
        else
        {
            ivAlarmLane4.setVisibility(View.INVISIBLE);
            if (m_bPreAlarmLane4) {
                if (m_bSignalTemp)  strSensorStatus = Resources.getSystem().getString(R.string.multi_connect);
                else  strSensorStatus = Resources.getSystem().getString(R.string.multi_disconnect);
                FileCalibrationWriter("Alarm_History", Resources.getSystem().getString(R.string.multi_temperature), strSensorStatus, Resources.getSystem().getString(R.string.multi_alarm_end));
            }
        }
        if (bLane5)
        {
            ivAlarmLane5.setVisibility(View.VISIBLE);
            if (!m_bPreAlarmLane5) {
                if (m_bSignalCo)  strSensorStatus = Resources.getSystem().getString(R.string.multi_connect);
                else  strSensorStatus = Resources.getSystem().getString(R.string.multi_disconnect);
                FileCalibrationWriter("Alarm_History", Resources.getSystem().getString(R.string.multi_conductivity), strSensorStatus, Resources.getSystem().getString(R.string.multi_alarm_start));
            }
        }
        else
        {
            ivAlarmLane5.setVisibility(View.INVISIBLE);
            if (m_bPreAlarmLane5) {
                if (m_bSignalCo)  strSensorStatus = Resources.getSystem().getString(R.string.multi_connect);
                else  strSensorStatus = Resources.getSystem().getString(R.string.multi_disconnect);
                FileCalibrationWriter("Alarm_History", Resources.getSystem().getString(R.string.multi_conductivity), strSensorStatus, Resources.getSystem().getString(R.string.multi_alarm_end));
            }
        }

        m_bPreAlarmLane1 = bLane1;
        m_bPreAlarmLane2 = bLane2;
        m_bPreAlarmLane3 = bLane3;
        m_bPreAlarmLane4 = bLane4;
        m_bPreAlarmLane5 = bLane5;

    }
    public static void DisplayC1(boolean bLane1, boolean bLane2, boolean bLane3, boolean bLane4, boolean bLane5){
        if (bLane1) txC1Lane1.setText("C1");
        else  txC1Lane1.setText("");
        if (bLane2) txC1Lane2.setText("C1");
        else  txC1Lane2.setText("");
        if (bLane3) txC1Lane3.setText("C1");
        else  txC1Lane3.setText("");
        if (bLane4) txC1Lane4.setText("C1");
        else  txC1Lane4.setText("");
        if (bLane5) txC1Lane5.setText("C1");
        else  txC1Lane5.setText("");
    }
    // 접속상태 변경시 보여주는 화면
    public void SetStatus () {
        if (m_strMode.equals("BlueTooth")) {
            if (m_strStatus.equals("Connected")) {
            //    SendStartSignal();
                m_strStatus = "Connected";
                byte [] bConnect = new byte[1];
                bConnect[0] = 0x01;
                SendProtocol_Set(2, (byte)0xC5, bConnect);
                Toast.makeText(context, R.string.connected, Toast.LENGTH_SHORT).show();
                DisplayStatus();
                btnConnect.setText(R.string.multi_disconnect);
            //    btnConnect.setVisibility(View.INVISIBLE);

            }
            else if (m_strStatus.equals("OnConnecting")) {
                m_strStatus = "OnConnecting";
                Toast.makeText(context, R.string.on_connecting, Toast.LENGTH_SHORT).show();
                DisplayStatus();
            //    btnConnect.setVisibility(View.VISIBLE);
                btnConnect.setText(R.string.on_connecting);
            }
            else if (m_strStatus.equals("Disconnected")) {
                m_strStatus = "Disconnected";
                Toast.makeText(context, R.string.disconnected, Toast.LENGTH_SHORT).show();
                DisplayStatus();
            //    btnConnect.setVisibility(View.VISIBLE);
                btnConnect.setText(R.string.multi_connect);
            }
        }
       else  if (m_strMode.equals("Online")) {
            m_strStatus = "Connected";
            Toast.makeText(context, R.string.disconnected, Toast.LENGTH_SHORT).show();
            DisplayStatus();
        //   btnConnect.setVisibility(View.INVISIBLE);
        }
       else if (m_strMode.equals("USB")) {
            if (m_strStatus.equals("Connected")) {
                m_strStatus = "Connected";
                byte [] bConnect = new byte[1];
                bConnect[0] = 0x02;
                SendProtocol_Set(2, (byte)0xC5, bConnect);
                Toast.makeText(context, R.string.connect_success, Toast.LENGTH_SHORT).show();
                DisplayStatus();
             //   btnConnect.setVisibility(View.INVISIBLE);
                btnConnect.setText(R.string.multi_disconnect);
            }
            else if (m_strStatus.equals("Disconnected")) {
                m_strStatus = "Disconnected";
                Toast.makeText(context, R.string.disconnected, Toast.LENGTH_SHORT).show();
                DisplayStatus();
            //    btnConnect.setVisibility(View.VISIBLE);
                btnConnect.setText(R.string.multi_connect);
            }
        }
        else if (m_strMode.equals("Ethernet")) {
            if (m_strStatus.equals("Connected")) {
            //    SendStartSignal();
                m_strStatus = "Connected";
                Toast.makeText(context, R.string.connect_success, Toast.LENGTH_SHORT).show();
                DisplayStatus();
            //    btnConnect.setVisibility(View.INVISIBLE);
                btnConnect.setText(R.string.multi_disconnect);
            }
            else if (m_strStatus.equals("Disconnected")) {
                m_strStatus = "Disconnected";
                Toast.makeText(context, R.string.disconnected, Toast.LENGTH_SHORT).show();
                DisplayStatus();
             //   btnConnect.setVisibility(View.VISIBLE);
                btnConnect.setText(R.string.multi_connect);
            }
        }
    }
    // 접속되면 관련 데이터들 요청한다
    private static void SendStartSignal()
    {

        // 센서타입 종류 요청
        byte [] bDummy = new byte[1];
        bDummy[0] = 0x00;

//        SendProtocol_Get( 1, (byte)0xA6, bDummy);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
      //   센서 타입 종류 요청
//        SendProtocol_Get(1,(byte)0xA1, bDummy);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        // 각 센서 시리얼 번호 요청
        SendProtocol_Get(1,(byte)0xA2, bDummy);
        try {
            Thread.sleep(500);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//
        // 각 센서 스틱 시리얼 번호
//        SendProtocol_Get(1,(byte)0xA3, bDummy);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        // 각 센서 설정된 데이터 필터
        SendProtocol_Get(1,(byte)0xA4, bDummy);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 시리얼 번호
        SendProtocol_Get(1,(byte)0xAF, bDummy);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SendProtocol_Get(1,(byte)0xB0, bDummy);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SendProtocol_Get(1,(byte)0xB6, bDummy);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 각 센서 전극 mV 데이터
//        SendProtocol_Get(2,(byte)0xA5, bDummy);
//        try {
//            Thread.sleep(200);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    static public void DisplayStatus(){
   //     if (m_bHoldAll) ivHold.setImageResource(R.mipmap.hold_on);
   //     else if (!m_bHoldAll) ivHold.setImageResource(R.mipmap.hold_off);
    //    CleanSensorValue();
        txMode.setText(m_strMode);
        txStatus.setText(m_strStatus);

        if (m_strMode.equals("Online")) btnConnect.setVisibility(View.INVISIBLE);
    }

    static public void CleanSensorValue(){
        txLane1.setText("");
        txLane2.setText("");
        txLane3.setText("");
        txLane4.setText("");
        txLane5.setText("");
        txLane5.setText("");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder d = new AlertDialog.Builder(this, AlertDialog.THEME_TRADITIONAL);
            d.setMessage(R.string.multi_are_you_sure);
            d.setPositiveButton(R.string.multi_answer_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // process전체 종료
                    finish();
                }
            });
            d.setNegativeButton(R.string.multi_answer_no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            d.show();
        }
    }

    // 옵션 메뉴 숨기는 곳
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        MenuItem item= menu.findItem(R.id.action_settings);
//        item.setVisible(false);
//        super.onPrepareOptionsMenu(menu);
//        return true;
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        //return true;
        ActionBar actionBar = getSupportActionBar();

        // Custom Actionbar를 사용하기 위해 CustomEnabled을 true 시키고 필요 없는 것은 false 시킨다
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);            //액션바 아이콘을 업 네비게이션 형태로 표시합니다.
        actionBar.setDisplayShowTitleEnabled(false);        //액션바에 표시되는 제목의 표시유무를 설정합니다.
        actionBar.setDisplayShowHomeEnabled(false);            //홈 아이콘을 숨김처리합니다.


        //layout을 가지고 와서 actionbar에 포팅을 시킵니다.
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View actionbar = inflater.inflate(R.layout.custom_actionbar, null);

        actionBar.setCustomView(actionbar);

        //액션바 양쪽 공백 없애기
        Toolbar parent = (Toolbar)actionbar.getParent();
        parent.setContentInsetsAbsolute(0,0);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
   //     noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
         //   ClickListButton();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_setup) {
            Intent intent = new Intent(
                    getApplicationContext(), // 현재 화면의 제어권자
                    SubActivitySetup.class); // 다음 넘어갈 클래스 지정
            startActivity(intent); // 다음 화면으로 넘어간다
        } else if (id == R.id.nav_cal) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_TRADITIONAL);
            builder.setTitle(R.string.multi_please_select_sensor);
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item);
            adapter.add(m_strPh);
            adapter.add(m_strCh);
            adapter.add(m_strTu);
            adapter.add(m_strCo);
            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (id == 0) {
                        Intent intent = new Intent(
                                getApplicationContext(), // 현재 화면의 제어권자
                                SubActivityCaliPh.class); // 다음 넘어갈 클래스 지정
                        startActivity(intent); // 다음 화면으로 넘어간다
                    } else if (id == 1) {
                        Intent intent = new Intent(
                                getApplicationContext(), // 현재 화면의 제어권자
                                SubActivityCaliCh.class); // 다음 넘어갈 클래스 지정
                        startActivity(intent); // 다음 화면으로 넘어간다
                    } else if (id == 2) {
                        Intent intent = new Intent(
                                getApplicationContext(), // 현재 화면의 제어권자
                                SubActivityCaliTu.class); // 다음 넘어갈 클래스 지정
                        startActivity(intent); // 다음 화면으로 넘어간다
                    } else if (id == 3) {
                        Intent intent = new Intent(
                                getApplicationContext(), // 현재 화면의 제어권자
                                SubActivityCaliCo.class); // 다음 넘어갈 클래스 지정
                        startActivity(intent); // 다음 화면으로 넘어간다
                    }
                }
            });
            builder.show();
        }
//        else if (id == R.id.nav_out) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_TRADITIONAL);
//            builder.setTitle(R.string.multi_please_select_sensor);
//            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item);
//            adapter.add(m_strPh);
//            adapter.add(m_strCh);
//            adapter.add(m_strTu);
//            adapter.add(m_strCo);
//            adapter.add(m_strTemp);
//            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int id) {
//                    if (id == 0) {
//                        Intent intent = new Intent(
//                                getApplicationContext(), // 현재 화면의 제어권자
//                                SubActivityOutputPh.class); // 다음 넘어갈 클래스 지정
//                        startActivity(intent); // 다음 화면으로 넘어간다
//                    } else if (id == 1) {
//                        Intent intent = new Intent(
//                                getApplicationContext(), // 현재 화면의 제어권자
//                                SubActivityOutputCh.class); // 다음 넘어갈 클래스 지정
//                        startActivity(intent); // 다음 화면으로 넘어간다
//                    } else if (id == 2) {
//                        Intent intent = new Intent(
//                                getApplicationContext(), // 현재 화면의 제어권자
//                                SubActivityOutputTu.class); // 다음 넘어갈 클래스 지정
//                        startActivity(intent); // 다음 화면으로 넘어간다
//                    } else if (id == 3) {
//                        Intent intent = new Intent(
//                                getApplicationContext(), // 현재 화면의 제어권자
//                                SubActivityOutputCo.class); // 다음 넘어갈 클래스 지정
//                        startActivity(intent); // 다음 화면으로 넘어간다
//                    } else if (id == 4) {
//                        Intent intent = new Intent(
//                                getApplicationContext(), // 현재 화면의 제어권자
//                                SubActivityOutputTemp.class); // 다음 넘어갈 클래스 지정
//                        startActivity(intent); // 다음 화면으로 넘어간다
//                    }
//                }
//            });
//            builder.show();
//        }
        else if (id == R.id.nav_wash) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_TRADITIONAL);
            builder.setTitle(R.string.multi_please_select_sensor);
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item);
            adapter.add(m_strCh);
            adapter.add(m_strTu);
            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (id == 0) {
                        Intent intent = new Intent(
                                getApplicationContext(), // 현재 화면의 제어권자
                                SubActivityWashCh.class); // 다음 넘어갈 클래스 지정
                        startActivity(intent); // 다음 화면으로 넘어간다
                    } else if (id == 1) {
                        Intent intent = new Intent(
                                getApplicationContext(), // 현재 화면의 제어권자
                                SubActivityWashTu.class); // 다음 넘어갈 클래스 지정
                        startActivity(intent); // 다음 화면으로 넘어간다
                    }
                }
            });
            builder.show();
        } else if (id == R.id.nav_trend) {
            if (m_DisplayValueThread != null) {
                m_DisplayValueThread.stopThread();
            }
            Intent intent = new Intent(
                    getApplicationContext(), // 현재 화면의 제어권자
                    SubActivityTrend.class); // 다음 넘어갈 클래스 지정
            startActivity(intent); // 다음 화면으로 넘어간다

        } else if (id == R.id.nav_dia) {
            Intent intent = new Intent(
                    getApplicationContext(), // 현재 화면의 제어권자
                    SubActivityDiagno.class); // 다음 넘어갈 클래스 지정
            startActivity(intent); // 다음 화면으로 넘어간다

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }


    @Override
    public void onDestroy() {
        FileLogWriter(getString(R.string.multi_main),"Application Finish");
        if (m_DisplayValueThread != null) {
            m_DisplayValueThread.stopThread();
        }
        if (m_DataCommunicationThread != null) {
            m_DataCommunicationThread.stopDCThread();
        }
        if (m_WashTuThread != null) {
            m_WashTuThread.stopWashTuThread();
        }
        if (m_WashTuFlashThread != null) {
            m_WashTuFlashThread.stopWashTuFlashThread();
        }
        if (m_WashChThread != null) {
            m_WashChThread.stopWashChThread();
        }
        if (m_WashChFlashThread != null) {
            m_WashChFlashThread.stopWashChFlashThread();
        }
        if (m_LightTxThread  != null) {
            m_LightTxThread.stopLightTxThread();
        }
        if (m_LightRxThread != null) {
            m_LightRxThread.stopLightRxThread();
        }
        if (m_TrendTreatThread != null) {
            m_TrendTreatThread.stopTrendTreatThread();
        }
        m_UsbReceiver.closeUsbSerial();
        unregisterReceiver(m_UsbReceiver);

        stopConnection();



   //     System.exit(0);
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }

    public static double randomRange(double d1, double d2) {
        return (Math.random() * (d2 - d1)) + d1;
    }

    public static void startDataCommunicationThread(){
        if (!m_DataCommunicationThread.isAlive()){
            m_DataCommunicationThread = new DataCommunicationThread(true);
            m_DataCommunicationThread.start();
        }
    }

    public static class DataCommunicationThread extends Thread {
        private boolean isPlay = false;
        public static int nTimer = 800;
        public static int nCommand = 3;
        public static String strSensor = "0";
        public DataCommunicationThread(boolean isPlay) {this.isPlay = isPlay;}
        public void stopDCThread(){
            isPlay = !isPlay;
        }
        @Override
        public void run() {
            super.run();
            while (isPlay) {
                try {
                    Thread.sleep(nTimer);
                    if (m_strStatus.equals("Connected"))
                    {
                        if (m_bisFirstConnect)
                        {
                            SendStartSignal();
                            m_bisFirstConnect = false;
                        }
                        byte [] bDummy = new byte[1];
                        bDummy[0] = 0x00;
                        SendProtocol_Get( 1, (byte)0xA6, bDummy);

                        if (m_bMvRead)
                        {
                            Thread.sleep(1000);
                            SendProtocol_Get( 1, (byte)0xA5, bDummy);

                            Thread.sleep(2000);
                        }

                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        }
    }

    private void startWashTuFlashThread(){
        if (!m_WashTuFlashThread.isAlive()){
            m_WashTuFlashThread = new WashTuFlashThread(true);
            m_WashTuFlashThread.start();
        }
    }
    private class WashTuFlashThread extends Thread {
        private boolean isPlay = false;
        private int nTimerFlash = 200;
        public WashTuFlashThread(boolean isPlay) {this.isPlay = isPlay;}
        public void stopWashTuFlashThread(){
            isPlay = !isPlay;
            msHandler.sendEmptyMessage(12);
        }
        @Override
        public void run() {
            super.run();
            while (isPlay) {
                try {
                    msHandler.sendEmptyMessage(11);
                    Thread.sleep(nTimerFlash);
                    msHandler.sendEmptyMessage(12);
                    Thread.sleep(nTimerFlash);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void startWashTuThread(){
        if (!m_WashTuThread.isAlive()){
            m_WashTuThread = new WashTuThread(true);
            m_WashTuThread.start();
        }
    }
    private class WashTuThread extends Thread {
        private boolean isPlay = false;
        private int nTimer = 1000;
        private int nDuration = 0;
        private int nInterval = 0;
        private int nStabilization = 0;
        private boolean bWashOpen = FALSE;
        private boolean bStabilization = FALSE;
        public WashTuThread(boolean isPlay) {this.isPlay = isPlay;}
        public void stopWashTuThread(){
            if (m_WashTuFlashThread.isAlive()) {
                m_WashTuFlashThread.stopWashTuFlashThread();
            }
            isPlay = !isPlay;
        }
        @Override
        public void run() {
            super.run();
            while (isPlay) {
                try {
                    Thread.sleep(nTimer);
                    if (m_strStatus.equals("Connected")) {
                        if (!bStabilization) {
                            if (nDuration == 0 && !bWashOpen) {
                                startWashTuFlashThread();
                               // SendProtocol((byte)0x53, 1, "0", m_strMode);
                                bWashOpen = TRUE;
                            }
                            if (nDuration >= m_nTuDuration && bWashOpen) {
                              //  SendProtocol((byte)0x54, 1, "0", m_strMode);
                                bWashOpen = FALSE;
                                bStabilization = TRUE;
                            }
                            nDuration++;
                            nInterval++;
                        }
                        else if (bStabilization) {
                            nDuration++;
                            nInterval++;
                            nStabilization++;
                            if (nStabilization >= m_nTuStabilization) {
                                if (m_WashTuFlashThread.isAlive()) {
                                    m_WashTuFlashThread.stopWashTuFlashThread();
                                }
                                if (nInterval >= m_nTuInterval * 60) {
                                    bStabilization = FALSE;
                                    nDuration = 0;
                                    nInterval = 0;
                                    nStabilization = 0;
                                }
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void startWashChFlashThread(){
        if (!m_WashChFlashThread.isAlive()){
            m_WashChFlashThread = new WashChFlashThread(true);
            m_WashChFlashThread.start();
        }
    }
    private class WashChFlashThread extends Thread {
        private boolean isPlay = false;
        private int nTimerFlash = 200;
        public WashChFlashThread(boolean isPlay) {this.isPlay = isPlay;}
        public void stopWashChFlashThread(){
            isPlay = !isPlay;
            msHandler.sendEmptyMessage(14);
        }
        @Override
        public void run() {
            super.run();
            while (isPlay) {
                try {
                    msHandler.sendEmptyMessage(13);
                    Thread.sleep(nTimerFlash);
                    msHandler.sendEmptyMessage(14);
                    Thread.sleep(nTimerFlash);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private static class TrendTreatThread extends Thread {
        private boolean isPlay = false;
        private int nTimerFlash = 200;
        public TrendTreatThread(boolean isPlay) {this.isPlay = isPlay;}
        public void stopTrendTreatThread(){
            isPlay = !isPlay;
        }
        @Override
        public void run() {
            super.run();
            while (isPlay) {
                            try {
                                arrayTimeTrend.clear();
                                arrayPhTrend.clear();
                                arrayCoTrend.clear();
                                arrayTuTrend.clear();
                                arrayClTrend.clear();
                                arrayTempTrend.clear();
                                arrayFlowTrend.clear();

                                byte btMainFlag;
                                byte btSubFlag;
                               // byte[] btTrendCount = new byte[4];

                                byte[] btTrendTime = new byte[4];
                                byte[] btTrendPh = new byte[4];
                                byte[] btTrendCo = new byte[4];
                                byte[] btTrendTu = new byte[4];
                                byte[] btTrendCl = new byte[4];
                                byte[] btTrendTemp = new byte[4];
                                byte[] btTrendFlow = new byte[4];

                                int nTrendTime = 0;
                                int nTrendPh = 0;
                                int nTrendCo = 0;
                                int nTrendTu = 0;
                                int nTrendCl = 0;
                                int nTrendTemp = 0;
                                int nTrendFlow = 0;
                                double dTrendPh = 0;
                                double dTrendCo = 0;
                                double dTrendTu = 0;
                                double dTrendCl = 0;
                                double dTrendTemp = 0;
                                double dTrendFlow = 0;

                                for (int i = 0; i < m_nTrendReadCount; i++) {
                                    if ((m_bDownLoadData[i] == (byte)0x02) && (m_bDownLoadData[i+29] == (byte)0x03)) {

                                        System.arraycopy(m_bDownLoadData, i+1, btTrendTime, 0, 4);
                                        System.arraycopy(m_bDownLoadData, i+5, btTrendPh, 0, 4);
                                        System.arraycopy(m_bDownLoadData, i+9, btTrendCl, 0, 4);
                                        System.arraycopy(m_bDownLoadData, i+13, btTrendTu, 0, 4);
                                        System.arraycopy(m_bDownLoadData, i+17, btTrendCo, 0, 4);
                                        System.arraycopy(m_bDownLoadData, i+21, btTrendTemp, 0, 4);
                                        System.arraycopy(m_bDownLoadData, i+25, btTrendFlow, 0, 4);

                                        nTrendTime = byteToint1(btTrendTime);
                                        nTrendPh = byteToint1(btTrendPh);
                                        nTrendCl = byteToint1(btTrendCl);
                                        nTrendTu = byteToint1(btTrendTu);
                                        nTrendCo = byteToint1(btTrendCo);
                                        nTrendTemp = byteToint1(btTrendTemp);
                                        nTrendFlow = byteToint1(btTrendFlow);

                                       // if ((nTrendTime > 0) && (nTrendPh > 0) && (nTrendCo > 0)) {
                                            if ((nTrendTime > 0)) {
                                            arrayTimeTrend.add(nTrendTime);
                                            arrayPhTrend.add((double)nTrendPh / 100);
                                            arrayClTrend.add((double)nTrendCl / 1000);
                                            arrayTuTrend.add((double)nTrendTu / 1000);
                                            arrayCoTrend.add((double)nTrendCo / 1000);
                                            arrayTempTrend.add((double)nTrendTemp / 100);
                                            arrayFlowTrend.add((double)nTrendFlow);
                                        }
                                        i += 29;
                                    }


                                }
                                Thread.sleep(nTimerFlash);
                                m_nTrendReadCount = 0;
                                stopTrendTreatThread();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
            }
        }
    }



    private static void  startLightTxThread(){
        if (!m_LightTxThread.isAlive()){
            m_LightTxThread = new LightTxThread(true);
            m_LightTxThread.start();
        }
    }
    private static class LightTxThread extends Thread {
        private boolean isPlay = false;
        private int nTimerFlash = 200;
        public LightTxThread(boolean isPlay) {this.isPlay = isPlay;}
        public void stopLightTxThread(){
            isPlay = !isPlay;
            msMainHandler.sendEmptyMessage(18);
        }
        @Override
        public void run() {
            super.run();
            while (isPlay) {
                try {
                    msMainHandler.sendEmptyMessage(17);
                    Thread.sleep(nTimerFlash);
                    stopLightTxThread();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private static void startLightRxThread(){
        if (!m_LightRxThread.isAlive()){
            m_LightRxThread = new LightRxThread(true);
            m_LightRxThread.start();
        }
    }
    private static class LightRxThread extends Thread {
        private boolean isPlay = false;
        private int nTimerFlash = 200;
        public LightRxThread(boolean isPlay) {this.isPlay = isPlay;}
        public void stopLightRxThread(){
            isPlay = !isPlay;
            msHandler.sendEmptyMessage(16);
        }
        @Override
        public void run() {
            super.run();
            while (isPlay) {
                try {
                    msHandler.sendEmptyMessage(15);
                    Thread.sleep(nTimerFlash);
                    stopLightRxThread();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void startWashChThread(){
        if (!m_WashChThread.isAlive()){
            m_WashChThread = new WashChThread(true);
            m_WashChThread.start();
        }
    }
    private class WashChThread extends Thread {
        private boolean isPlay = false;
        private int nTimer = 1000;
        private int nDuration = 0;
        private int nInterval = 0;
        private int nStabilization = 0;
        private boolean bWashOpen = FALSE;
        private boolean bStabilization = FALSE;
        public WashChThread(boolean isPlay) {this.isPlay = isPlay;}
        public void stopWashChThread(){
            if (m_WashChFlashThread.isAlive()) {
                m_WashChFlashThread.stopWashChFlashThread();
            }
            isPlay = !isPlay;
        }
        @Override
        public void run() {
            super.run();
            while (isPlay) {
                try {
                    Thread.sleep(nTimer);
                    if (m_strStatus.equals("Connected")) {
                        if (!bStabilization) {
                            if (nDuration == 0 && !bWashOpen) {
                                startWashChFlashThread();
                              //  SendProtocol((byte)0x55, 1, "0", m_strMode);
                                bWashOpen = TRUE;
                            }
                            if (nDuration >= m_nChDuration && bWashOpen) {
                              //  SendProtocol((byte)0x56, 1, "0", m_strMode);
                                bWashOpen = FALSE;
                                bStabilization = TRUE;
                            }
                            nDuration++;
                            nInterval++;
                        }
                        else if (bStabilization) {
                            nDuration++;
                            nInterval++;
                            nStabilization++;
                            if (nStabilization >= m_nChStabilization) {
                                if (m_WashChFlashThread.isAlive()) {
                                    m_WashChFlashThread.stopWashChFlashThread();
                                }
                                if (nInterval >= m_nChInterval * 60) {
                                    bStabilization = FALSE;
                                    nDuration = 0;
                                    nInterval = 0;
                                    nStabilization = 0;
                                }
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }


    public static void startDisplayValueThread(){
        if (!m_DisplayValueThread.isAlive()){
            m_DisplayValueThread = new DisplayValueThread(true);
            m_DisplayValueThread.start();
        }
    }

    // 1초단위로 전송된 데이터 값을 보여주는 Thread
    public static class DisplayValueThread extends Thread {
        private boolean isPlay = false;
        public  DisplayValueThread(boolean isPlay) {this.isPlay = isPlay;}
        public void stopThread(){
            isPlay = !isPlay;
        }
        @Override
        public void run() {
            super.run();
            while (isPlay) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (m_strStatus.equals("Disconnected"))
                {
                    m_dTu = 0.0d;
                    m_dCh = 0.0d;
                    m_dPh = 0.0d;
                    m_dTemp = 0.0d;
                    m_dCo = 0.0d;
                }

                if (m_strMode.equals("Online")) {
//                    double dRandomTu = randomRange(0.0300, 0.0650);
//                    double dRandomCh = randomRange(0.023, 0.030);
//                    double dRandomPh = randomRange(6.80, 7.10);
//                    double dRandomTemp = randomRange(20, 22);
//                    double dRandomCo = randomRange(0.231, 0.235);

                    double dRandomTu = randomRange(0.050, 0.050);
                    double dRandomCh = randomRange(0.200, 0.200);
                    double dRandomPh = randomRange(7.00, 7.00);
                    double dRandomTemp = randomRange(15.6, 15.6);
                    double dRandomCo = randomRange(250.0, 250.0);

                    m_dTu = dRandomTu;
                    m_dCh = dRandomCh;
                    m_dPh = dRandomPh;
                    m_dTemp = dRandomTemp;
                    m_dCo = dRandomCo;

                    if (m_bTXRX) {
                        m_bTXRX = false;
                        startLightTxThread();
                    } else {
                        m_bTXRX = true;
                        startLightRxThread();
                    }
                }

                // 자리수 표기를 위한 방법
                DecimalFormat df0Figure = new DecimalFormat("0");
                DecimalFormat df1Figure = new DecimalFormat("0.0");
                DecimalFormat df2Figure = new DecimalFormat("0.00");
                DecimalFormat df3Figure = new DecimalFormat("0.000");
                DecimalFormat df4Figure = new DecimalFormat("0.0000");
                // 소수점 이하 버림
                m_dTu = Math.floor(m_dTu*1000d) / 1000d;
                m_dCh = Math.floor(m_dCh*1000d) / 1000d;
                m_dHOCL = Math.floor(m_dHOCL*1000d) / 1000d;
                m_dPh = Math.floor(m_dPh*100d) / 100d;
                m_dTemp = Math.floor(m_dTemp*10d) / 10d;
                m_dCo = Math.floor(m_dCo*1000d) / 1000d;

                // 센서 값 증가 or 감소 인지 확인
//                if (m_dTu > m_dTu_Pre) m_bTu_IsIncrease = TRUE;
//                else m_bTu_IsIncrease = FALSE;
//                if (m_dCh > m_dCh_Pre) m_bCh_IsIncrease = TRUE;
//                else m_bCh_IsIncrease = FALSE;
//                if (m_dPh > m_dPh_Pre) m_bPh_IsIncrease = TRUE;
//                else m_bPh_IsIncrease = FALSE;
//                if (m_dTemp > m_dTu_Pre) m_bTemp_IsIncrease = TRUE;
//                else m_bTemp_IsIncrease = FALSE;
//                if (m_dCo > m_dCo_Pre) m_bCo_IsIncrease = TRUE;
//                else m_bCo_IsIncrease = FALSE;

                m_dTu = CalculateSetRange(0, m_dTu, m_bTuPhaseIsHigh, m_dTuSetPoint, m_dTuDeadBand, m_nTuOnDelay, m_nTuOffDelay );
                m_dCh = CalculateSetRange(1, m_dCh, m_bChPhaseIsHigh, m_dChSetPoint, m_dChDeadBand, m_nChOnDelay, m_nChOffDelay );
                m_dPh = CalculateSetRange(2, m_dPh, m_bPhPhaseIsHigh, m_dPhSetPoint, m_dPhDeadBand, m_nPhOnDelay, m_nPhOffDelay );
                m_dTemp = CalculateSetRange(3, m_dTemp, m_bTempPhaseIsHigh, m_dTempSetPoint, m_dTempDeadBand, m_nTempOnDelay, m_nTempOffDelay );
                m_dCo = CalculateSetRange(4, m_dCo, m_bCoPhaseIsHigh, m_dCoSetPoint, m_dCoDeadBand, m_nCoOnDelay, m_nCoOffDelay );
                msHandler.sendEmptyMessage(0);

                // 알람 설정값 체크 및 표시
//                if (m_dTu > m_dTuHighAlarm || m_dTu < m_dTuLowAlarm) m_bAlarmLane1 = TRUE;
//                else m_bAlarmLane1 = FALSE;
//                if (m_dCh > m_dChHighAlarm || m_dCh < m_dChLowAlarm) m_bAlarmLane2 = TRUE;
//                else m_bAlarmLane2 = FALSE;
//                if (m_dPh > m_dPhHighAlarm || m_dPh < m_dPhLowAlarm) m_bAlarmLane3 = TRUE;
//                else m_bAlarmLane3 = FALSE;
//                if (m_dTemp > m_dTempHighAlarm || m_dTemp < m_dTempLowAlarm) m_bAlarmLane4 = TRUE;
//                else m_bAlarmLane4 = FALSE;
//                if (m_dCo > m_dCoHighAlarm || m_dCo < m_dCoLowAlarm) m_bAlarmLane5 = TRUE;
//                else m_bAlarmLane5 = FALSE;




//                m_strLane1 = String.format("%.3f", m_dTu);
//                m_strLane2 = String.format("%.3f", m_dCh);
//                m_strLane3 = String.format("%.2f", m_dPh);
//                m_strLane4 = String.format("%.1f", m_dTemp);
//                m_strLane5 = String.format("%.1f", m_dCo);
//                m_strLane6 = "Normal Flow";

                // 세정 설정값 체크 및 표시
//                if ((m_dTu > m_dTuHighWash || m_dTu < m_dTuLowWash) && m_bTuAutoWash) startWashTuThread();
//                else m_WashTuThread.stopWashTuThread();
//                if ((m_dCh > m_dChHighWash || m_dCh < m_dChLowWash) && m_bChAutoWash) startWashChThread();
//                else m_WashChThread.stopWashChThread();

                // Trend Filter 적용
                // Moving Average
//                if (m_nTrendFilter == 2) {
//                    m_dAverage[0] = CalcMovingAvg(0, m_dTu);
//                    m_dAverage[1] = CalcMovingAvg(1, m_dCh);
//                    m_dAverage[2] = CalcMovingAvg(2, m_dPh);
//                    m_dAverage[3] = CalcMovingAvg(3, m_dTemp);
//                    m_dAverage[4] = CalcMovingAvg(4, m_dCo);
//                }
//                // CL 99의 경우
//                else if (m_nTrendFilter == 3) {
//                    m_dAverage[0] = CalcMovingAvg(0, m_dTu);
//                    m_dAverage[1] = CalcMovingAvg(1, m_dCh);
//                    m_dAverage[2] = CalcMovingAvg(2, m_dPh);
//                    m_dAverage[3] = CalcMovingAvg(3, m_dTemp);
//                    m_dAverage[4] = CalcMovingAvg(4, m_dCo);
//
//                    m_dTu = CalcConstantLevel (0, m_dTu, m_dAverage[0] );
//                    m_dCh = CalcConstantLevel (1, m_dCh, m_dAverage[1]);
//                    m_dPh = CalcConstantLevel (2, m_dPh,  m_dAverage[2]);
//                    m_dTemp = CalcConstantLevel (3, m_dTemp, m_dAverage[3]);
//                    m_dCo = CalcConstantLevel (4, m_dCo, m_dAverage[4]);
//                }




              //  mHandler.sendEmptyMessage(6);

                Date dCurrentTime = new Date();
                SimpleDateFormat sdCurrent = new SimpleDateFormat("HH:mm:ss");
                String strDate = sdCurrent.format(dCurrentTime);
                String strWrite;
                String strTemp;

                // 화면 소수점 보이기
                m_strLane1 = df3Figure.format(m_dTu);
                m_strLane2 = df3Figure.format(m_dCh);
                m_strLane3 = df2Figure.format(m_dPh);
                m_strLane4 = df1Figure.format(m_dTemp);
                if (m_dCo >= 1000)  m_strLane5 = df1Figure.format(m_dCo);
                else if (m_dCo >= 100) m_strLane5 = df2Figure.format(m_dCo);
                else if (m_dCo >= 0) m_strLane5 = df3Figure.format(m_dCo);
                m_strLane6 = "Normal Flow";

                if (!m_bHoldAll)
                {
                    msHandler.sendEmptyMessage(1);
                    msHandler.sendEmptyMessage(2);
                    msHandler.sendEmptyMessage(3);
                    msHandler.sendEmptyMessage(4);
                    msHandler.sendEmptyMessage(5);
                }

                if (!m_bSignalTu) {
                    m_strLane1 = "Error";
                    m_dTu = 0;
                }
                if (!m_bSignalCh) {
                    m_strLane2 = "Error";
                    m_dCh = 0;
                }
                if (!m_bSignalPh) {
                    m_strLane3 = "Error";
                    m_dPh = 0;
                }
                if (!m_bSignalCo) {
                    m_strLane5 = "Error";
                    m_dCo = 0;
                }
                if (!m_bSignalTemp) {
                    m_strLane4 = "Error";
                    m_dTemp = 0;
                }
                m_strLane6 = "Normal Flow";


                double dSumofValue = m_dTu + m_dCh + m_dPh + m_dTemp + m_dCo;
                if (dSumofValue > 0){
                    int nSecond = dCurrentTime.getSeconds();
                    if (nSecond == 0)
                    {
                        //strTemp = Double.toString(m_dTu);
                        strTemp = df3Figure.format(m_dTu);
                        strWrite = strDate+"|"+strTemp+"\n";
                        FileWriter("lane1.txt", strWrite);
                        strWrite = strDate+"|"+strTemp+"\n";
                        FileWriter("lane1_min.txt", strWrite);

                       // strTemp = Double.toString(m_dCh);
                        strTemp = df3Figure.format(m_dCh);
                        strWrite = strDate+"|"+strTemp+"\n";
                        FileWriter("lane2.txt", strWrite);
                        strWrite = strDate+"|"+strTemp+"\n";
                        FileWriter("lane2_min.txt", strWrite);

                      //  strTemp = Double.toString(m_dPh);
                        strTemp = df2Figure.format(m_dPh);
                        strWrite = strDate+"|"+strTemp+"\n";
                        FileWriter("lane3.txt", strWrite);
                        strWrite = strDate+"|"+strTemp+"\n";
                        FileWriter("lane3_min.txt", strWrite);

                     //   strTemp = Double.toString(m_dTemp);
                        strTemp = df1Figure.format(m_dTemp);
                        strWrite = strDate+"|"+strTemp+"\n";
                        FileWriter("lane4.txt", strWrite);
                        strWrite = strDate+"|"+strTemp+"\n";
                        FileWriter("lane4_min.txt", strWrite);

                       // strTemp = Double.toString(m_dCo);
                        strTemp =  df3Figure.format(m_dCo);
                        strWrite = strDate+"|"+strTemp+"\n";
                        FileWriter("lane5.txt", strWrite);
                        strWrite = strDate+"|"+strTemp+"\n";
                        FileWriter("lane5_min.txt", strWrite);

                        if (m_strLane6 == "Normal Flow") strTemp = "1";
                        else if (m_strLane6 == "Check Flow") strTemp = "0";
                        strWrite = strDate+"|"+strTemp+"\n";
                        FileWriter("lane6.txt", strWrite);
                        strWrite = strDate+"|"+strTemp+"\n";
                        FileWriter("lane6_min.txt", strWrite);
                    }
                    else
                    {
                        strTemp = df3Figure.format(m_dTu);
                        strWrite = strDate+"|"+strTemp+"\n";
                        FileWriter("lane1.txt", strWrite);
                        strTemp = df3Figure.format(m_dCh);
                        strWrite = strDate+"|"+strTemp+"\n";
                        FileWriter("lane2.txt", strWrite);
                        strTemp = df2Figure.format(m_dPh);
                        strWrite = strDate+"|"+strTemp+"\n";
                        FileWriter("lane3.txt", strWrite);
                        strTemp = df1Figure.format(m_dTemp);
                        strWrite = strDate+"|"+strTemp+"\n";
                        FileWriter("lane4.txt", strWrite);
                        strTemp =  df3Figure.format(m_dCo);
                        strWrite = strDate+"|"+strTemp+"\n";
                        FileWriter("lane5.txt", strWrite);
                        if (m_strLane6 == "Normal Flow") strTemp = "1";
                        else if (m_strLane6 == "Check Flow") strTemp = "0";
                        strWrite = strDate+"|"+strTemp+"\n";
                        FileWriter("lane6.txt", strWrite);
                    }
                }





                // hocl 을 fcl 로 변경
//                m_dHOCL = 0.0d;
//                m_dHOCL = m_dCh;
//                // PH 보상일 경우
//                if (m_bCh_PhCompIsCurrent){
//                    m_dCh = m_dCh * (1+(((0.056714 * Math.pow(10.0, -8)*m_dTemp) + (1.476 * Math.pow(10.0,-8)))/Math.pow(10.0, -m_dPh)));
//                }
//                // PH 고정일 경우
//                else {
//                    m_dCh = m_dCh * (1+(((0.056714 * Math.pow(10.0, -8)*m_dTemp) + (1.476 * Math.pow(10.0,-8)))/Math.pow(10.0, -(m_dCh_PhFixValue))));
//                }


            }
        }
    }


    double CalcMovingAvg (int nSensor, double dValue) {
        double dResult = 0.0f;
        double dSum = 0.0f;
        m_ArrayAverage[nSensor].add(0,dValue);
        int nCount = 0;
        if ( m_ArrayAverage[nSensor].size() > m_nTrendCount) {
            nCount = m_nTrendCount;
            m_ArrayAverage[nSensor].remove(nCount);
        }
        else nCount =  m_ArrayAverage[nSensor].size();
        for (int i = 0; i < nCount; i++ ) {
            dSum +=  m_ArrayAverage[nSensor].get(i);
        }
        dResult = dSum / nCount;
        return dResult;
    }

    double CalcConstantLevel (int nSensor, double dValue, double dAverage) {
        double dResult = 0.0f;
        double dSD = 0.0f;
        double dDiff = 0.0f;
        double dDSum = 0.0f;
        int nCount = m_ArrayAverage[nSensor].size();
        if ( nCount< 2) {
            return Double.NaN;
        }
        else {
            for (int i = 0; i < nCount; i++) {
                dDiff = m_ArrayAverage[nSensor].get(i) - dAverage;
                dDSum += dDiff * dDiff;
            }
        }
        dSD = Math.sqrt(dDSum / (nCount -1));
        m_dCL[nSensor] = dSD * m_dTrendConstant / Math.sqrt(10);
        m_dCLMax[nSensor] =  m_dAverage[nSensor] + m_dCL[nSensor];
        m_dCLMin[nSensor] =  m_dAverage[nSensor] - m_dCL[nSensor];

        if (dValue >= m_dCLMin[nSensor] && dValue <= m_dCLMax[nSensor]) {
            dResult = dValue;
            m_bCLOnRange[nSensor] = TRUE;
        }
        else {
            m_bCLOnRange[nSensor] = FALSE;
            dResult =  m_dPreValue[nSensor];
        }
        return dResult;
    }

//    private  final Handler msHandler = new Handler()
    private static final Handler msHandler = new Handler() { //핸들러를 통해 UI스레드에 접근한다.
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 0){
                DisplayAlarm(m_bAlarmLane1,m_bAlarmLane2,m_bAlarmLane3,m_bAlarmLane4,m_bAlarmLane5);
                DisplayC1(m_bC1Lane[0],m_bC1Lane[1],m_bC1Lane[2],m_bC1Lane[3],m_bC1Lane[4]);
            }
            else if (msg.what == 1) {
                txLane1.setText(m_strLane1);
            }
            else  if (msg.what == 2) {
                txLane2.setText(m_strLane2);
            }
            else  if (msg.what == 3) {
                txLane3.setText(m_strLane3);
            }
            else  if (msg.what == 4) {
                txLane4.setText(m_strLane4);
            }
            else  if (msg.what == 5) {
                txLane5.setText(m_strLane5);
            }
            else if (msg.what == 6)
            {
//                Date dCurrentTime = new Date();
//                SimpleDateFormat sdCurrentTime = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
//                String strCurrentDate = sdCurrentTime.format(dCurrentTime);
//                setTitle(strCurrentDate);
            }
            else if (msg.what == 7)
            {
               // Toast.makeText(context, "Mode Normal", Toast.LENGTH_SHORT);
            }
            else if (msg.what == 11)
            {
                ivWashT.setImageResource(R.mipmap.wash_t_on);
            }
            else if (msg.what == 12)
            {
                ivWashT.setImageResource(R.mipmap.wash_t_off);
            }
            else if (msg.what == 13)
            {
                ivWashC.setImageResource(R.mipmap.wash_c_on);
            }
            else if (msg.what == 14)
            {
                ivWashC.setImageResource(R.mipmap.wash_c_off);
            }
            else if (msg.what == 15)
            {
                ivRX.setImageResource(R.mipmap.rx_on);
            }
            else if (msg.what == 16)
            {
                ivRX.setImageResource(R.mipmap.rx_off);
            }
            else if (msg.what == 17)
            {
                ivHold.setImageResource(R.mipmap.hold_on);
            }
            else if (msg.what == 18)
            {
                ivHold.setImageResource(R.mipmap.hold_off);
            }
//            else if (msg.what == 20)
//            {
//                Toast.makeText(context, "Loading..... i5 information", Toast.LENGTH_LONG).show();
//            }

//            else if (msg.what == 17)
//            {
//                ivTX.setImageResource(R.mipmap.tx_on);
//            }
//            else if (msg.what == 18)
//            {
//                ivTX.setImageResource(R.mipmap.tx_off);
//            }
        }
    };

    private static final Handler msMainHandler = new Handler() { //핸들러를 통해 UI스레드에 접근한다.
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            if (msg.what == 15)
//            {
//                ivRX.setImageResource(R.mipmap.rx_on);
//            }
//            else if (msg.what == 16)
//            {
//                ivRX.setImageResource(R.mipmap.rx_off);
//            }
            if (msg.what == 17)
            {
                ivTX.setImageResource(R.mipmap.tx_on);
            }
            else if (msg.what == 18)
            {
                ivTX.setImageResource(R.mipmap.tx_off);
            }
        }
    };


    private static  final Handler msStaticHandler = new Handler() { //핸들러를 통해 UI스레드에 접근한다.
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                txStatus.setText(m_strStatus);
             //   btnConnect.setVisibility(View.VISIBLE);
            }
        }
    };

    // 트렌드 데이터값을 파일 저장
    public static void  FileWriter (String strFile, String strText)
    {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String strDate = sdf.format(d);
        String dirPath = Environment.getExternalStorageDirectory()+"/STAqua/trend/"+strDate;
        File file = new File(dirPath);
        if (!file.exists()){
            file.mkdirs();
        }
        String strWFile = dirPath+"/"+strFile;
        try{
            // BufferedWriter 와 FileWriter를 조합하여 사용 (속도 향상)
            BufferedWriter fw = new BufferedWriter(new FileWriter(strWFile, true));
            // 파일안에 문자열 쓰기
            fw.write(strText);
            fw.flush();
            // 객체 닫기
            fw.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // 로그를 파일에 저장하는 함수
    public static void  FileLogWriter (String strLocation, String strAction )
    {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sdCurrent = new SimpleDateFormat("HH:mm:ss");
        String strTime = sdCurrent.format(d);
        String strDate = sdf.format(d);
        String dirPath = Environment.getExternalStorageDirectory()+"/STAqua/log";
        File file = new File(dirPath);
        if (!file.exists()){
            file.mkdirs();
        }
        String strWFile = dirPath+"/"+strDate+"log.txt";
        strAction = strTime+"|"+strLocation+"|"+strAction+"\n";

        try{
            // BufferedWriter 와 FileWriter를 조합하여 사용 (속도 향상)
            BufferedWriter fw = new BufferedWriter(new FileWriter(strWFile, true));
            // 파일안에 문자열 쓰기
            fw.write(strAction);
            fw.flush();
            // 객체 닫기
            fw.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // 교정내용 파일에 저장하는 함수
    public static void  FileCalibrationWriter (String strFileName, String strCaliLocation, String strCaliValue , String strCaliResult )
    {
        Date d = new Date();
        SimpleDateFormat sdCurrent = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        String strTime = sdCurrent.format(d);
        String dirPath = Environment.getExternalStorageDirectory()+"/STAqua/Calibration";
        File file = new File(dirPath);
        if (!file.exists()){
            file.mkdirs();
        }
        String strWFile = dirPath+"/"+strFileName+".txt";
        String strText = strTime+" | "+strCaliLocation+" | "+strCaliValue+" | "+strCaliResult+"\n";
        try{
            // BufferedWriter 와 FileWriter를 조합하여 사용 (속도 향상)
            BufferedWriter fw = new BufferedWriter(new FileWriter(strWFile, true));
            // 파일안에 문자열 쓰기
            fw.write(strText);
            fw.flush();
            // 객체 닫기
            fw.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // 바이트 값을 float 로 변경
    static public float byteTofloat(byte[] b)
    {
        String hexText = new java.math.BigInteger(b).toString(16);
        Long i = parseLong(hexText, 16);
        float myFloat = intBitsToFloat(i.intValue());
        return myFloat;
    }


    // 바이트 값을 int 로 변경
    static public int byteToint(byte[] b)
    {
        String hexText = new java.math.BigInteger(b).toString(16);
        Long i = parseLong(hexText, 16);
        int myInt = (int) (long) i;
        return myInt;
    }

    static public int byteToint1(byte[] b)
    {
        return (b[3] & 0xff)<<24 | (b[2] & 0xff)<<16 | (b[1] & 0xff)<<8 | (b[0] & 0xff);
    }
//    public  int byteArrayToInt(byte bytes[]) {
//        return ((((int)bytes[0] & 0xff) << 24) |
//                (((int)bytes[1] & 0xff) << 16) |
//                (((int)bytes[2] & 0xff) << 8) |
//                (((int)bytes[3] & 0xff)));
//    }
//
//    public float byteArrayToFloat(byte bytes[]) {
//        int value =  byteArrayToInt(bytes);
//        return Float.intBitsToFloat(value);
//    }


    // float 를 byte 로 변경
    public static byte[] floatTobyte(float f) {
        byte newbytes[] = new byte[4];
        int bits = Float.floatToIntBits(f);
        newbytes[0] = (byte)(bits & 0xff);
        newbytes[1] = (byte)((bits >> 8) & 0xff);
        newbytes[2] = (byte)((bits >> 16) & 0xff);
        newbytes[3] = (byte)((bits >> 24) & 0xff);

        return newbytes;
    }
    // byte 16진수를 정수로 변환
    private int hexToInteger(byte output) {
        StringBuffer sb = new StringBuffer(2);
        String hexaDecimal;
        hexaDecimal = "0"+Integer.toHexString(0xff&output);
        sb.append(hexaDecimal.substring(0));
        return parseInt(sb.toString(),16);
    }

    //Serial 통신 관련
    public void ClickSerialConnection()
    {
        // USB 드라이버 검색 및 설정
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = com.usbserial.driver.UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            Toast.makeText(this, "Check USB Connection", Toast.LENGTH_SHORT).show();
            m_strStatus = "Disconnected";
            txStatus.setText(m_strStatus);
            FileLogWriter("Main","Check USB Connection");
            return;
        }
        // USB 드라이버 권한 설정
        com.usbserial.driver.UsbSerialDriver driver = availableDrivers.get(0);
        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        manager.requestPermission(driver.getDevice(),permissionIntent);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            Toast.makeText(this, "Check USB authority", Toast.LENGTH_SHORT).show();
            m_strStatus = "Check USB authority";
            txStatus.setText(m_strStatus);
            FileLogWriter("Main","Check USB Connection");
            return;
        }
        sPort = driver.getPorts().get(0);

        // USB 시리얼 포트 연결을 위한 셋팅
        try {
            sPort.open(connection);
            // 설정된 baudrate 값을 설정

//            prefs = getSharedPreferences("Setup_Serial", MODE_PRIVATE);
//            String strBaudRate = prefs.getString("Baud", "");
//            String strData = prefs.getString("Data","");
//            String strStop = prefs.getString("Stop", "");
//            String strParity = prefs.getString("Parity", "");
//            if (strBaudRate =="") strBaudRate = "115200";
//            if (strData =="")strData = "8";
//            if (strStop =="")strStop = "1";
//            if (strParity =="")strParity = "None";
//            int nBaudRate = parseInt(strBaudRate);
//            int nData = parseInt(strData);
//            int nStop = parseInt(strStop);
//            int nParity = 0;
//            if(strParity=="None") nParity = 0;
//            else if (strParity =="Odd") nParity = 1;
//            else if (strParity =="Even") nParity = 2;
            sPort.setParameters(115200, 8, 1, 0);
        } catch (IOException e) {
            Toast.makeText(this, "Error Setting up Device", Toast.LENGTH_SHORT).show();
            m_strStatus = "Error Setup";
            txStatus.setText(m_strStatus);
            FileLogWriter("Main","Error Setting up Device");
            try {
                sPort.close();
            } catch (IOException e2) {
                // Ignore.
            }
            sPort = null;
            return;
        }
        // 접속이 되면 통신을 위한 설정
        stopIoManager();
        startIoManager();

        m_strStatus = "Connected";
        SetStatus();
        FileLogWriter("Main","Serial Connected");
    }

    private static void stopIoManager() {
        if (mSerialIoManager != null) {
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }
    private void startIoManager() {
        if (sPort != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(sPort, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    // Serial 통신 관련
    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {
                byte[] btTemp  = new byte [1024*16];
                int nCount = 0;
                boolean bHasStart = FALSE;
                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG, "Runner stopped.");
                }
                // 통신 결과값을 받는 Listener 값 (여기서 처리)
                // 동기화 방법에 대한 고민
                // FC 를 Start 로 두고 2byte 뒤의 값이 길이값으로 두고 잘라서 전송
                @Override
                public void onNewData(final byte[] data) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                          //  MainActivity.this.updateReceivedData(data);
                            int nLen = data.length;
                            // 정상적으로 데이터 들어오는 경우
                            if (nLen > 0) {
                                startLightRxThread();
                                if((data[0] == (byte)0xFC) && (data[1] != (byte)0xF1) && !m_bIsDownloadTrend) {
                                    byte [] btLength = new byte [4];
                                    btLength[0] = data[1];
                                    btLength[1] = data[2];
                                    btLength[2] = data[3];
                                    btLength[3] = data[4];
                                    int nPayloadLen = byteToint(btLength);
                                    if (nLen == nPayloadLen + 7){
                                        SplitResponseProtocol_Get(data, nLen);
                                    }
                                    else {
                                        Log.d(TAG, "[Receive Wrong]");
                                    }
                                }
                                else if (m_bIsDownloadTrend) {
                                    for (int i = 0; i < nLen; i++) {
                                        m_bDownLoadData[m_nTrendReadCount+i] = data[i];
                                    }
                                    m_nTrendReadCount = m_nTrendReadCount+nLen;
//                                    if (m_nTrendReadCount >= m_nTrendProtocolLength * 30) {
//                                        Log.d(TAG, "[Receive Trend End]" + m_nTrendReadCount);
//                                        if (!m_TrendTreatThread.isAlive()){
//                                            m_TrendTreatThread = new TrendTreatThread(true);
//                                            m_TrendTreatThread.start();
//                                            m_bIsDownloadTrend = false;
//                                        }
//                                    }
                                    Log.d(TAG, "[Receive Trend Ing]" + m_nTrendReadCount);
                                }
                                else {
                                    Log.d(TAG, "[Receive Exp]" + nLen);
                                    m_nTrendReadCount = 0;
                                }
                            }
                        }
                    });
                }
            };

    public static void StartTrendProcess() {
        Log.d(TAG, "[Receive Trend End]" + m_nTrendReadCount);
        if (!m_TrendTreatThread.isAlive()){
            m_TrendTreatThread = new TrendTreatThread(true);
            m_TrendTreatThread.start();
            m_bIsDownloadTrend = false;
        }
    }


    // 블루투스 관련
    public void ClickListButton() {
        if (isAdapterReady()) {
            if (isConnected()) stopConnection();
            else startDeviceListActivity();
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public void DisableBlueTooth () {
        time_sec = 0;
        m_delayTime = new TimerTask() {
            @Override
            public void run() {
                if(btAdapter.getState() == BluetoothAdapter.STATE_TURNING_ON ||
                        btAdapter.getState() == btAdapter.STATE_ON) {
                    btAdapter.disable();
                }
                EnableBlueTooth();
                time_sec++;
            }
        };
        Timer timer = new Timer();
        timer.schedule(m_delayTime, 2000);
    }

    protected  void EnableBlueTooth(){
        Runnable update = new Runnable() {
            @Override
            public void run() {
                btAdapter.enable();
            }
        };
        m_handler_BTReconnect.post(update);
    }


    boolean isAdapterReady() {
        return (btAdapter != null) && (btAdapter.isEnabled());
    }
    private static boolean isConnected() {
        return (connector != null) && (connector.getState() == DeviceConnector.STATE_CONNECTED);
    }
    private void startDeviceListActivity() {
        stopConnection();
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }
    static private void stopConnection() {
        if (connector != null) {
            connector.stop();
            connector = null;
            m_deviceName = null;
        }
    }
    void setDeviceName(String deviceName) {
        m_deviceName = deviceName;
        //txHw.setText(m_deviceName);
        //getActionBar().setSubtitle(deviceName);
    }

    private void setupConnector(BluetoothDevice connectedDevice) {
        stopConnection();
        try {
            String emptyName = getString(R.string.empty_device_name);
            DeviceData data = new DeviceData(connectedDevice, emptyName);
            connector = new DeviceConnector(data, mHandler);
            connector.connect();
        } catch (IllegalArgumentException e) {
          //  Utils.log("setupConnector failed: " + e.getMessage());
            Log.d(TAG, "setupConnector failed: " + e.getMessage());
        }
    }

    private class BluetoothResponseHandler extends Handler {
        private WeakReference<MainActivity> mActivity;

        public BluetoothResponseHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        public void setTarget(MainActivity target) {
            mActivity.clear();
            mActivity = new WeakReference<MainActivity>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case MESSAGE_STATE_CHANGE:
                        //Utils.log("MESSAGE_STATE_CHANGE: " + msg.arg1);
                        final android.app.ActionBar bar = activity.getActionBar();
                        switch (msg.arg1) {
                            case DeviceConnector.STATE_CONNECTED:
                                m_strStatus = "Connected";
                                break;
                            case DeviceConnector.STATE_CONNECTING:
                                m_strStatus = "OnConnecting";
                                break;
                            case DeviceConnector.STATE_NONE:
                                m_strStatus = "Disconnected";
                                break;
                        }
                        activity.SetStatus ();
                        activity.invalidateOptionsMenu();
                        break;

                    case MESSAGE_READ:
                        int nLength = msg.arg1;
                        int nFlag = msg.arg2;
                        byte[] btBuffer;
                        btBuffer = (byte[])msg.obj;

                       // int nLength = btBuffer.length;

                        if ( ( btBuffer[0] == (byte)0xFC) && !m_bIsDownloadTrend && nLength > 6)  {
                            startLightRxThread();
                            byte[] bLength = new byte[4];
                            bLength[0] = btBuffer[1];
                            bLength[1] = btBuffer[2];
                            bLength[2] = btBuffer[3];
                            bLength[3] = btBuffer[4];
                            int nPayloadLen = byteToint(bLength);
                            if (nLength == nPayloadLen + 7){
                                SplitResponseProtocol_Get(btBuffer, nLength);
                            }
                            else {
                                Log.d(TAG, "[Receive Wrong]");
                            }
                        }
                        else  if (m_bIsDownloadTrend) {
                            for (int i = 0; i < nLength; i++) {
                                m_bDownLoadData[m_nTrendReadCount+i] = btBuffer[i];
                            }
                            m_nTrendReadCount = m_nTrendReadCount+nLength;
                     //       Log.d(TAG, "[Receive Trend Ing]" + m_nTrendReadCount);
//                            if (m_nTrendReadCount >= m_nTrendProtocolLength * 30) {
//                                if (!m_TrendTreatThread.isAlive()){
//                                    m_TrendTreatThread = new TrendTreatThread(true);
//                                    m_TrendTreatThread.start();
//                                    m_bIsDownloadTrend = false;
//                                }
//                            }
                        }
                        else {
                            Log.d(TAG, "[Receive Wrong]");
                        }

                        break;

                    case MESSAGE_DEVICE_NAME:
                   //     activity.txHw.setText((String) msg.obj);
                        break;

                    case MESSAGE_WRITE:
                        // stub
                        break;

                    case MESSAGE_TOAST:
                        // stub
                        break;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = btAdapter.getRemoteDevice(address);
                    if (isAdapterReady() && (connector == null)) setupConnector(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                pendingRequestEnableBt = false;
                if (resultCode != Activity.RESULT_OK) {
                    // Utils.log("BT not enabled");
                }
                break;
        }
    }

    //TCPIP 통신 관련
    public void ClickEthernetConnection() {
        if (socket != null) return;
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());
        mTCPSocketHandler = new Handler();
        try {
           if ( setSocket(m_strIpAddress, parseInt(m_strPort))){
        //       startEthernetCommThread();
               m_strStatus = "Connected";
               SetStatus();
           }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
    static public void StopEthernet() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore.
            }
            socket = null;
        }
    }
    public boolean  setSocket(String ip, int port) throws IOException {
        try {
            socket = new Socket();
            SocketAddress serverAddress = new InetSocketAddress(ip, port);
            socket.connect(serverAddress, 5000); // 연결시도
            networkWriter = new DataOutputStream(socket.getOutputStream());
            networkReader = new DataInputStream(socket.getInputStream());
            return true;
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
            m_EthernetCommThread.stopWashTuFlashThread();
            socket.close();
            socket = null;
            Toast.makeText(this, "Time out : Check Server Connection", Toast.LENGTH_SHORT).show();
            m_strStatus = "Check Server";
            txStatus.setText(m_strStatus);
            FileLogWriter("Main","Time out : Check Server Connection");
            return false;
        }
    }
    private void startEthernetCommThread(){
        if (!m_EthernetCommThread.isAlive()){
            m_EthernetCommThread = new EthernetCommThread(true);
            m_EthernetCommThread.start();
        }
    }
    private class EthernetCommThread extends Thread {
        byte[] buff = new byte[102400];
        int n_read;
        private boolean isPlay = false;
        public EthernetCommThread(boolean isPlay) {this.isPlay = isPlay;}
        public void stopWashTuFlashThread(){
            isPlay = !isPlay;
        }
        @Override
        public void run() {
            super.run();
                try {
                    while ((n_read = networkReader.read(buff)) > 0 && isPlay) {
                        if (buff[0] == (byte) 0xFC) {
                            int nPayloadLen = hexToInteger(buff[2]);
                            if (n_read == nPayloadLen + 5) {
                                SplitResponseProtocol_Get(buff, n_read);
                            } else {
                                SplitResponseProtocol_Get(buff, nPayloadLen + 7);
                            }
                        }
                        else {
                        }
                }
            } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
//    private Thread checkUpdate = new Thread() {
//        byte[] buff = new byte[1024];
//        int n_read;
//        public void run() {
//            try {
//                while ((n_read = networkReader.read(buff)) > 0 && !isInterrupted())  {
//                    if (buff[0] == (byte) 0xFC) {
//                        int nPayloadLen = hexToInteger(buff[2]);
//                        if (n_read == nPayloadLen + 5) {
//                            SplitResponseProtocol(buff, n_read);
//                        } else {
//                            SplitResponseProtocol(buff, nPayloadLen + 5);
//                        }
//                    }
//                }
//            } catch (Exception e) {
//
//            }
//        }
//    };
    public static class SensorInfo
    {
        public int nIndex;
        public int nType;
        public double dValue;
        public double dValueMv;
        public String strSensor;
        public String strSerialNo;


        public SensorInfo(int nIndex)
        {
            this.nIndex = nIndex;
        }

        public SensorInfo(int nIndex, int nType,  double dValue, double dValueMv, String strSensor , String strSerialNo )
        {
            this.nIndex = nIndex;
            this.nType = nType;
            this.dValue = dValue;
            this.dValueMv = dValueMv;
            this.strSensor = strSensor;
            this.strSerialNo = strSerialNo;
        }
    }

    boolean isSet(byte value, int bit){
        return (value&(1<<bit))!=0;
    }


    public void SplitResponseProtocol_Get(byte[] btProtocol, int nLength) {

        // CRC 체크
        CRC16Modbus crc = new CRC16Modbus();
        boolean bResult = crc.checkCRC(btProtocol, nLength);
        if (!bResult) return;

        byte btCommand = btProtocol[5];
        byte[] btRealData = new byte [nLength - 8];
        System.arraycopy(btProtocol, 6, btRealData, 0, nLength-8);
        if (btCommand == (byte)0xA6) {
            String strValue = new String(btRealData);
            StringTokenizer tokens = new StringTokenizer(strValue);
            String strCo = tokens.nextToken(" ");
            String strPH = tokens.nextToken(" ");
            String strTU = tokens.nextToken(" ");
            String strCH = tokens.nextToken(" ");
            String strTemp = tokens.nextToken(" ");
            String strFlow = tokens.nextToken(" ");
            String strImage = tokens.nextToken(" ");

            float fCO = Float.parseFloat(strCo);
            float fPh = Float.parseFloat(strPH);
            float fTU = Float.parseFloat(strTU);
            float fCH = Float.parseFloat(strCH);
            float fTEMP = Float.parseFloat(strTemp);

            DecimalFormat df1Figure = new DecimalFormat("0.0");
            DecimalFormat df2Figure = new DecimalFormat("0.00");
            DecimalFormat df3Figure = new DecimalFormat("0.000");

            m_dPh = parseDouble(df2Figure.format(fPh));
            m_dCo = parseDouble(df3Figure.format(fCO));
            m_dTu = parseDouble(df3Figure.format(fTU));
            m_dCh = parseDouble(df3Figure.format(fCH));
            m_dTemp = parseDouble(df1Figure.format(fTEMP));

            byte[] btImage = new byte[2];
            btImage[0] = btProtocol[nLength - 4];
            btImage[1] = btProtocol[nLength - 3];

            // 온도 에러
            if (isSet(btImage[0], 3)) { m_bSignalTemp = false;}
            else { m_bSignalTemp = true;}
            // 염소 에러
            if (isSet(btImage[0], 4)) { m_bSignalCh = false;}
            else { m_bSignalCh = true;}
            // 탁도 에러
            if (isSet(btImage[0], 5)) { m_bSignalTu = false;}
            else { m_bSignalTu = true;}
            // ph 에러
            if (isSet(btImage[0], 6)) { m_bSignalPh = false;}
            else { m_bSignalPh = true;}
            // 전도도 에러
            if (isSet(btImage[0], 7)) { m_bSignalCo = false;}
            else { m_bSignalCo = true;}


            // 232,485 테블릿이랑 관련 없음
            if (isSet(btImage[1], 0)) { }
            // 램프 (아직 화면 미구성)
            if (isSet(btImage[1], 1)) { }
            else { }
            // 에러 표시 없음
            if (isSet(btImage[1], 2)) { }
            //알람 표시 없음
            if (isSet(btImage[1], 3)) { }
            // 추가 벨브 표시 없음
            if (isSet(btImage[1], 4)) { }
            // 염소 벨브 표시
            if (isSet(btImage[1], 5)) {
                m_bChManualWash = true;
                msHandler.sendEmptyMessage(13);
            }
            else {
                m_bChManualWash = false;
                msHandler.sendEmptyMessage(14);
            }
            //탁도 벨브 표시
            if (isSet(btImage[1], 6)) {
                m_bTuManualWash = true;
                msHandler.sendEmptyMessage(11);
            }
            else {
                m_bTuManualWash = false;
                msHandler.sendEmptyMessage(12);
            }
            //홀드 버튼 표시
            if (isSet(btImage[1], 7)) {
                m_bHoldAll = true;
                msHandler.sendEmptyMessage(17);
            }
            else {
                m_bHoldAll = false;
                msHandler.sendEmptyMessage(18);
            }

        }

        else if (btCommand == (byte)0xA2) {
            String strValue = new String(btRealData);
            StringTokenizer tokens = new StringTokenizer(strValue);
            String strSensor1No = tokens.nextToken(" ");
            String strSensor2No = tokens.nextToken(" ");
            String strSensor3No = tokens.nextToken(" ");
            String strSensor4No = tokens.nextToken(" ");


//            String strSensor1No = strValue.substring(0,6);
//            String strSensor2No = strValue.substring(6,12);
//            String strSensor3No = strValue.substring(12,18);
//            String strSensor4No = strValue.substring(18,24);

            m_strCoSerial = strSensor1No;
//            EditText edSerialLine1 = (EditText)findViewById(R.id.System_Ph_Ed);
//            edSerialLine1.setText(m_strCoSerial);
            m_strPhSerial = strSensor2No;
            m_strTuSerial = strSensor3No;
            m_strChSerial = strSensor4No;
        }

        else if (btCommand == (byte)0xA4) {
            String strValue = new String(btRealData);
            StringTokenizer tokens = new StringTokenizer(strValue);
            String strCo = tokens.nextToken(" ");
            String strPH = tokens.nextToken(" ");
            String strTU = tokens.nextToken(" ");
            String strCH = tokens.nextToken(" ");

            m_nFilterCo = Integer.parseInt(strCo);
            m_nFilterPh = Integer.parseInt(strPH);
            m_nFilterTu = Integer.parseInt(strTU);
            m_nFilterCh = Integer.parseInt(strCH);

            m_bFilterChange = true;
        }

        else if (btCommand == (byte)0xA5) {
            String strValue = new String(btRealData);
            StringTokenizer tokens = new StringTokenizer(strValue);
            String strCo = tokens.nextToken(" ");
            String strPH = tokens.nextToken(" ");
            String strTU = tokens.nextToken(" ");
            String strCH = tokens.nextToken(" ");

            float fCO = Float.parseFloat(strCo);
            float fPh = Float.parseFloat(strPH);
            float fTU = Float.parseFloat(strTU);
            float fCH = Float.parseFloat(strCH);

            DecimalFormat df3Figure = new DecimalFormat("0.000");

            m_dMvPh = parseDouble(df3Figure.format(fPh));
            m_dMvCo = parseDouble(df3Figure.format(fCO));
            m_dMvTu = parseDouble(df3Figure.format(fTU));
            m_dMvCh = parseDouble(df3Figure.format(fCH));
        }
        else if (btCommand == (byte)0xAF) {
            String strValue = new String(btRealData);
            StringTokenizer tokens = new StringTokenizer(strValue);
            String strSensorSerial = tokens.nextToken(" ");
            m_strEquipmentSerial = strSensorSerial;
        }
        else if (btCommand == (byte)0xA0) {
            String strValue = new String(btRealData);
            StringTokenizer tokens = new StringTokenizer(strValue);
            String strTrendTotalCount = tokens.nextToken(" ");
            m_nTrendProtocolLength = Integer.parseInt(strTrendTotalCount);
            Log.d(TAG, "[Receive Trend Start]" + m_nTrendProtocolLength);
            m_nTrendReadCount = 0;
            m_bIsDownloadTrend = true;
            byte [] bDummy = new byte[1];
            bDummy[0] = 0x00;
            if (!SendProtocol_Get(1,(byte)0x88, bDummy)){
                return;
            }
        }
        else if (btCommand == (byte)0xAD) {
            String strValue = new String(btRealData);
            StringTokenizer tokens = new StringTokenizer(strValue);
            String strSystemTime = tokens.nextToken(" ");
            String strDate = TransferToTime(Integer.parseInt(strSystemTime));
            Toast.makeText(context, strDate, Toast.LENGTH_LONG).show();
        }
        else if (btCommand == (byte)0xB0) {
            String strValue = new String(btRealData);
            StringTokenizer tokens = new StringTokenizer(strValue);
            m_strBaudRate = tokens.nextToken(" ");
            m_strStopBit = tokens.nextToken(" ");
            m_strDataBit = tokens.nextToken(" ");
            m_strParity = tokens.nextToken(" ");
            m_strStation = tokens.nextToken(" ");

            SharedPreferences prefs;
            prefs = getSharedPreferences("Setup_Serial", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("Serial", m_strSerialStandard);
            editor.putString("Baud", m_strBaudRate);
            editor.putString("Stop", m_strStopBit);
            editor.putString("Parity", m_strParity);
            editor.putString("Station", m_strStation);
            editor.putString("Data", m_strDataBit);
            editor.commit();
        }
        else if (btCommand == (byte)0xB1) {
            String strValue = new String(btRealData);
            StringTokenizer tokens = new StringTokenizer(strValue);
            String strVersion = tokens.nextToken(" ");
            Toast.makeText(context, strVersion, Toast.LENGTH_LONG).show();
        }
        else if (btCommand == (byte)0xB2) {
            byte[] btLogData = new byte [nLength - 10];
            System.arraycopy(btRealData, 2, btLogData, 0, nLength-10);
            String strMessage = new String(btLogData);
            try{
                if (btRealData[0] == 0x30) {
                    SubActivityCaliCoCalibrationHistory.getHistory(strMessage);
                }
                else if (btRealData[0] == 0x31) {
                    SubActivityCaliPhCalibrationHistory.getHistory(strMessage);
                }
                else if (btRealData[0] == 0x32) {
                    SubActivityCaliTuCalibrationHistory.getHistory(strMessage);
                }
                else if (btRealData[0] == 0x33) {
                    SubActivityCaliChCalibrationHistory.getHistory(strMessage);
                }
            }
            catch (Exception e) {
                return;
            }
        }

        else if (btCommand == (byte)0xB4) {
            try{
                String strValue = new String(btRealData);
                int nSensor = 0;
                StringTokenizer tokens = new StringTokenizer( strValue, "," );
                for( int i = 0; tokens.hasMoreElements(); i++ ) {
                    String strTemp = tokens.nextToken();
                    if (i == 0) {
                        nSensor = parseInt(strTemp);
                    }
                    else if (i == 1) {
                        SubActivitySetupItemsReplacement.getExpoireDate(nSensor, strTemp);
                    }
                }
            }
            catch (Exception e) {
                return;
            }
        }


        else if (btCommand == (byte)0xB5) {
            String strValue = new String(btRealData);
            try{
                SubActivitySetupReplacementHistory.getHistory(strValue);
            }
            catch (Exception e) {
                return;
            }
        }

        else if (btCommand == (byte)0xB6) {
            String strValue = new String(btRealData);
            StringTokenizer tokens = new StringTokenizer(strValue);
            m_strIpAddress = tokens.nextToken(" ");
            m_strSubnetMask = tokens.nextToken(" ");
            m_strGateway = tokens.nextToken(" ");
            m_strPort = tokens.nextToken(" ");

            SharedPreferences prefs;
            prefs = getSharedPreferences("Setup_Ethernet", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("IpAddress", m_strIpAddress);
            editor.putString("SubnetMask", m_strSubnetMask);
            editor.putString("Gateway", m_strGateway);
            editor.putString("Port", m_strPort);
            editor.commit();

//            prefs = getSharedPreferences("Setup_Ethernet", MODE_PRIVATE);
//            m_strIpAddress = prefs.getString("Ip", "");
//            if (m_strIpAddress == "")     m_strIpAddress = "192.168.000.180";
//            m_strSubnetMask = prefs.getString("Subnet", "");
//            if (m_strSubnetMask == "")  m_strSubnetMask = "255.255.255.000";
//            m_strGateway = prefs.getString("Gateway", "");
//            if (m_strGateway == "")  m_strGateway = "192.168.000.001";
//            m_strPort = prefs.getString("Port", "");
//            if (m_strPort == "")  m_strPort = "9000";
        }


        else if (btCommand == (byte)0xAE) {
            String strValue = new String(btRealData);
            StringTokenizer tokens = new StringTokenizer(strValue);

            String strType = tokens.nextToken(" ");
            String strDuration = tokens.nextToken(" ");
            String strInterval = tokens.nextToken(" ");
            String strStabilization = tokens.nextToken(" ");
            String strHigh = tokens.nextToken(" ");
            String strLow = tokens.nextToken(" ");
            String strAuto = tokens.nextToken(" ");

            int nType = Integer.parseInt(strType);

            //CL
            if (nType == 0) {
                m_nChDuration = Integer.parseInt(strDuration);
                m_nChInterval = Integer.parseInt(strInterval);
                m_nChStabilization = Integer.parseInt(strStabilization);
                m_dChHighWash = Double.parseDouble(strHigh);
                m_dChLowWash = Double.parseDouble(strLow);
                m_bChAutoWash = Boolean.valueOf(strAuto);
                m_bChWashChange = true;
            }
            else if (nType == 1) {
                m_nTuDuration = Integer.parseInt(strDuration);
                m_nTuInterval = Integer.parseInt(strInterval);
                m_nTuStabilization = Integer.parseInt(strStabilization);
                m_dTuHighWash = Double.parseDouble(strHigh);
                m_dTuLowWash = Double.parseDouble(strLow);
                m_bTuAutoWash = Boolean.valueOf(strAuto);
                m_bTuWashChange = true;
            }
        }
        else if (btCommand == (byte)0xA7)
        {
            // Get Temperature Sensor
            // Status 0 : OFF, 1 : On
            byte bStatus = btRealData[0];
            if (bStatus == (byte)0x00) {
                Log.d(TAG, "No Sensor Calibration");
                m_nCaliStatus = 0;
            }
            else if (bStatus == (byte)0x01) {
                Log.d(TAG, "Cal in Progress");
                m_nCaliStatus = 1;
            }
            else if (bStatus == (byte)0x02) {
                Log.d(TAG, "Cal OK");
                m_nCaliStatus = 2;
            }
            else if (bStatus == (byte)0x03) {
                Log.d(TAG, "Fail - Not Stable");
                m_nCaliStatus = 3;
            }
            else if (bStatus == (byte)0x04) {
                Log.d(TAG, "Fail - Buffer not found");
                m_nCaliStatus = 4;
            }
            else if (bStatus == (byte)0x05) {
                Log.d(TAG, "Fail - First Buffer not found");
                m_nCaliStatus = 5;
            }
            else if (bStatus == (byte)0x06) {
                Log.d(TAG, "Fail - Second Buffer not found");
                m_nCaliStatus = 6;
            }
            else if (bStatus == (byte)0x07) {
                Log.d(TAG, "Fail - Value too low");
                m_nCaliStatus = 7;
            }
            else if (bStatus == (byte)0x08) {
                Log.d(TAG, "Fail – Value too high");
                m_nCaliStatus = 8;
            }
            else if (bStatus == (byte)0x09) {
                Log.d(TAG, "Fail – Slope too low");
                m_nCaliStatus = 9;
            }
            else if (bStatus == (byte)0x0A) {
                Log.d(TAG, "Fail – Slope too high");
                m_nCaliStatus = 10;
            }
            else if (bStatus == (byte)0x0B) {
                Log.d(TAG, "Offset too low");
                m_nCaliStatus = 11;
            }
            else if (bStatus == (byte)0x0C) {
                Log.d(TAG, "Offset too high");
                m_nCaliStatus = 12;
            }
            else if (bStatus == (byte)0x0D) {
                Log.d(TAG, "Points too close");
                m_nCaliStatus = 13;
            }
            else if (bStatus == (byte)0x0E) {
                Log.d(TAG, "General Cal Fail(zero or sample)");
                m_nCaliStatus = 14;
            }
        }
        else {
            if( btRealData[0] == 0x01) {
                Log.d(TAG, "Send OK");
            }
        }

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

//    public void SplitResponseProtocol(byte[] btProtocol, int nLength)
//    {
//        CRC16Modbus crc = new CRC16Modbus();
//        boolean bResult = crc.checkCRC(btProtocol, nLength);
//        if (!bResult) return;
//        // Start Byte 체크
//        if (btProtocol[0] != (byte)0xFC) return;
//        // Command Byte 체크
//        // Get Sensor Info
//        if (btProtocol[1] == (byte)0x81)
//        {
//            // FC8120303130343037313042323234303342323930383042343736363142323937303829EC
//            // FC / 81 / 20 : Size 32
//            // 센서 type : 3031303430373130 : 01 04 07 10
//            // 01 : pH
//            // 04 : Contacting Conductivity
//            // 07 : Turbidity
//            // 10 : Chlorine
//
//            // 시리얼 번호 PH : 423232343033 : B22403
//            // 시리얼 번호 Conductivity : 423239303830 : B29080
//            // 시리얼 번호 TU : 423437363631 : B47661
//            // 시리얼 번호 CL : 423239373038 : B29708
//
//            byte[] btSensor1 = new byte[2];
//            byte[] btSensor2 = new byte[2];
//            byte[] btSensor3 = new byte[2];
//            byte[] btSensor4 = new byte[2];
//
//            for (int i = 0; i < 2 ;i++)
//            {
//                btSensor1[i] =  btProtocol[3+i];
//                btSensor2[i] =  btProtocol[5+i];
//                btSensor3[i] =  btProtocol[7+i];
//                btSensor4[i] =  btProtocol[9+i];
//            }
//
//            byte[] btSensor1No = new byte[6];
//            byte[] btSensor2No = new byte[6];
//            byte[] btSensor3No = new byte[6];
//            byte[] btSensor4No = new byte[6];
//            for (int i = 0; i < 6 ;i++)
//            {
//                btSensor1No[i] =  btProtocol[11+i];
//                btSensor2No[i] =  btProtocol[17+i];
//                btSensor3No[i] =  btProtocol[23+i];
//                btSensor4No[i] =  btProtocol[29+i];
//            }
//            String strSensor1No = new String(btSensor1No);
//            String strSensor2No = new String(btSensor2No);
//            String strSensor3No = new String(btSensor3No);
//            String strSensor4No = new String(btSensor4No);
//            m_strPhSerial = strSensor1No;
//            m_strCoSerial = strSensor2No;
//            m_strTuSerial = strSensor3No;
//            m_strChSerial = strSensor4No;
//
//        }
//        if (btProtocol[1] == (byte)0x83)
//        {
//            // FC83180A90EB40BF74133CEA920441000000003333974100000000725F
//            // 18 : Size 24
//            // 0A90EB40 PH 값// BF74133C Conductivity 값// EA920441 TU 값// 00000000 CL 값// 33339741 TEMP 값
//            // 00000000 센서 STATUS
//            byte[] btPH = new byte[4];
//            byte[] btCO = new byte[4];
//            byte[] btTU = new byte[4];
//            byte[] btCH = new byte[4];
//            byte[] btTEMP = new byte[4];
//
//            byte[] btPH1 = new byte[4];
//            byte[] btCO1 = new byte[4];
//            byte[] btTU1 = new byte[4];
//            byte[] btCH1 = new byte[4];
//            byte[] btTEMP1 = new byte[4];
////            for (int i = 0; i < 4 ;i++)
////            {
////                btPH[i] =  btProtocol[6-i];
////                btCO[i] =  btProtocol[10-i];
////                btTU[i] =  btProtocol[14-i];
////                btCH[i] =  btProtocol[18-i];
////                btTEMP[i] =  btProtocol[22-i];
////            }
//
////            btPH[1] =  btProtocol[6];
////            btPH[0] =  btProtocol[5];
////            btPH[2] =  btProtocol[4];
////            btPH[3] =  btProtocol[3];
////
////            btCH[1] =  btProtocol[10];
////            btCH[0] =  btProtocol[9];
////            btCH[2] =  btProtocol[8];
////            btCH[3] =  btProtocol[7];
////
////            btTU[1] =  btProtocol[14];
////            btTU[0] =  btProtocol[13];
////            btTU[2] =  btProtocol[12];
////            btTU[3] =  btProtocol[11];
////
////            btCO[1] =  btProtocol[18];
////            btCO[0] =  btProtocol[17];
////            btCO[2] =  btProtocol[16];
////            btCO[3] =  btProtocol[15];
////
////            btTEMP[1] =  btProtocol[22];
////            btTEMP[0] =  btProtocol[21];
////            btTEMP[2] =  btProtocol[20];
////            btTEMP[3] =  btProtocol[19];
//
//
//            btPH1[1] =  btProtocol[3];
//            btPH1[0] =  btProtocol[4];
//            btPH1[3] =  btProtocol[5];
//            btPH1[2] =  btProtocol[6];
//
//            btCH1[1] =  btProtocol[7];
//            btCH1[0] =  btProtocol[8];
//            btCH1[3] =  btProtocol[9];
//            btCH1[2] =  btProtocol[10];
//
//            btTU1[1] =  btProtocol[11];
//            btTU1[0] =  btProtocol[12];
//            btTU1[3] =  btProtocol[13];
//            btTU1[2] =  btProtocol[14];
//
//            btCO1[1] =  btProtocol[15];
//            btCO1[0] =  btProtocol[16];
//            btCO1[3] =  btProtocol[17];
//            btCO1[2] =  btProtocol[18];
//
//            btTEMP1[1] =  btProtocol[19];
//            btTEMP1[0] =  btProtocol[20];
//            btTEMP1[3] =  btProtocol[21];
//            btTEMP1[2] =  btProtocol[22];
//
////
////            float fPh = byteArrayToFloat(btPH);
////            float fCO = byteArrayToFloat(btCO);
////            float fTU = byteArrayToFloat(btTU);
////            float fCH = byteArrayToFloat(btCH);
////            float fTEMP = byteArrayToFloat(btTEMP);
//
//
//            float fPh1 = ByteBuffer.wrap(btPH1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
//            float fCO1 = ByteBuffer.wrap(btCO1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
//            float fTU1 = ByteBuffer.wrap(btTU1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
//            float fCH1 = ByteBuffer.wrap(btCH1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
//            float fTEMP1 = ByteBuffer.wrap(btTEMP1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
//
////            float fPh = byteTofloat(btPH);
////            float fCO = byteTofloat(btCO);
////            float fTU = byteTofloat(btTU);
////            float fCH = byteTofloat(btCH);
////            float fTEMP = byteTofloat(btTEMP);
//
//            m_dPh = fPh1;
//            m_dCo = fCO1;
//            m_dTu = fTU1;
//            m_dCh = fCH1;
//            m_dTemp = fTEMP1;
//
//
////            DecimalFormat df1Figure = new DecimalFormat("0.0");
////            DecimalFormat df2Figure = new DecimalFormat("0.00");
////            DecimalFormat df3Figure = new DecimalFormat("0.000");
////            m_dPh = parseDouble(df2Figure.format(fPh));
////            m_dCo = parseDouble(df1Figure.format(fCO));
////            m_dTu = parseDouble(df3Figure.format(fTU));
////            m_dCh = parseDouble(df3Figure.format(fCH));
////            m_dTemp = parseDouble(df1Figure.format(fTEMP));
//
//
//            if (btProtocol[23] == (byte)0x00)
//            {
//                m_bSignalPh = TRUE;
//                m_bSignalTemp = TRUE;
//            }
//            else if (btProtocol[23] == (byte)0x01)
//            {
//                m_bSignalPh = FALSE;
//                m_bSignalTemp = FALSE;
//            }
//            if (btProtocol[24] == (byte)0x00) m_bSignalCo = TRUE;
//            else if (btProtocol[24] == (byte)0x01) m_bSignalCo = FALSE;
//            if (btProtocol[25] == (byte)0x00) m_bSignalTu = TRUE;
//            else if (btProtocol[25] == (byte)0x01) m_bSignalTu = FALSE;
//            if (btProtocol[26] == (byte)0x00) m_bSignalCh = TRUE;
//            else if (btProtocol[26] == (byte)0x01) m_bSignalCh = FALSE;
//       //     if (DEBUG) Log.d(TAG, "*** Get Sensor Value");
//        }
//
//        if (btProtocol[1] == (byte)0x84)
//        {
//            // FC8414F6A8A1C23393AD431F85D2C2AE47C13F000000003879
//            // FC / 84 / 14 : Size 20
//            // mv PH : F6A8A1C2 : -80.830002
//            // mv Conductivity : 3393AD43 : 347.1499994
//            // mv TU : 1F85D2C2 : -105.260002
//            // mv CL : AE47C13F : 1.150000
//            // mv TEMP : 00000000 : 0.0000000
//            // CRC check : 3879
//            byte[] btMvPH = new byte[4];
//            byte[] btMvCO = new byte[4];
//            byte[] btMvTU = new byte[4];
//            byte[] btMvCH = new byte[4];
//            byte[] btMvTEMP = new byte[4];
//            for (int i = 0; i < 4 ;i++)
//            {
//                btMvPH[i] =  btProtocol[6-i];
//                btMvCO[i] =  btProtocol[10-i];
//                btMvTU[i] =  btProtocol[14-i];
//                btMvCH[i] =  btProtocol[18-i];
//                btMvTEMP[i] =  btProtocol[22-i];
//            }
//            float fPh = byteTofloat(btMvPH);
//            float fCO = byteTofloat(btMvCO);
//            float fTU = byteTofloat(btMvTU);
//            float fCH = byteTofloat(btMvCH);
//            float fTEMP = byteTofloat(btMvTEMP);
//            DecimalFormat df3Figure = new DecimalFormat("0.000");
//            m_dMvPh = parseDouble(df3Figure.format(fPh));
//            m_dMvCo = parseDouble(df3Figure.format(fCO));
//            m_dMvTu = parseDouble(df3Figure.format(fTU));
//            m_dMvCh = parseDouble(df3Figure.format(fCH));
//            m_dMvTemp = parseDouble(df3Figure.format(fTEMP));
//        //    if (DEBUG) Log.d(TAG, "*** Get Sensor Mv");
//        }
//
//        if (btProtocol[1] == (byte)0x85)
//        {
//            // Get Lamp Status
//            // Status 0 : OFF, 1 : On
//            byte bLamStatus = btProtocol[4];
//            if (bLamStatus == (byte)0x00)  Log.d(TAG, "*** Get Lamp Status : Off");
//            else if (bLamStatus == (byte)0x01)  Log.d(TAG, "*** Get Lamp Status : On");
//            else  Log.d(TAG, "*** Get Lamp Status : Wrong");
//        }
//
//        if (btProtocol[1] == (byte)0x86)
//        {
//            // Get Valve
//            // Status 0 : OFF, 1 : On
//            byte bValveStatus = btProtocol[4];
//            if (bValveStatus == (byte)0x00)
//            {
//                Log.d(TAG, "Get Valve Status : Off");
//            }
//            else if (bValveStatus == (byte)0x01)
//            {
//                Log.d(TAG, "Get Valve Status : On");
//            }
//            else
//            {
//                Log.d(TAG, "Get Valve Status : Wrong");
//            }
//        }
//        if (btProtocol[1] == (byte)0x87)
//        {
//            // Get Sensor Filter
//            // 센서 type , Sensor Filter
//            // 01 : pH
//            // 04 : Chlorine
//            // 03 : Turbidity
//            // 02 :   Contacting Conductivity
//            byte bSensor = btProtocol[3];
//            byte bFilterNumber = btProtocol[4];
//            if (bSensor == (byte)0x01) {
//                m_nPhFilterNumber = hexToInteger(bFilterNumber);
//            }
//            if (bSensor == (byte)0x04) {
//                m_nChFilterNumber = hexToInteger(bFilterNumber);
//            }
//            if (bSensor == (byte)0x03) {
//                m_nTuFilterNumber = hexToInteger(bFilterNumber);
//            }
//            if (bSensor == (byte)0x02) {
//                m_nCoFilterNumber = hexToInteger(bFilterNumber);
//            }
//
//            Log.d(TAG, "Get Sensor Filter");
//        }
//        if (btProtocol[1] == (byte)0x91)
//        {
//            Log.d(TAG, "Mode Normal");
//        }
//        if (btProtocol[1] == (byte)0x92)
//        {
//            Log.d(TAG, "Mode Calibration");
//        }
//        if (btProtocol[1] == (byte)0x93)
//        {
//            Log.d(TAG, "Mode Value MV");
//        }
//        if (btProtocol[1] == (byte)0x94)
//        {
//            Log.d(TAG, "Mode Find Sensor");
//        }
//        if (btProtocol[1] == (byte)0x95)
//        {
//            Log.d(TAG, "Mode Diagnosis Sensor");
//        }
//        if (btProtocol[1] == (byte)0xC3)
//        {
//            Log.d(TAG, "Set Sensor Filter");
//        }
//        if (btProtocol[1] == (byte)0xC5)
//        {
//            Log.d(TAG, "Set Moving Average Data Count");
//        }
//        if (btProtocol[1] == (byte)0xC6)
//        {
//            Log.d(TAG, "Set Moving Average Deviation");
//        }
//        if (btProtocol[1] == (byte)0xC7)
//        {
//            Log.d(TAG, "Set Moving Average Filter Count");
//        }
//        if (btProtocol[1] == (byte)0xC8)
//        {
//            Log.d(TAG, "Set Sensor High Limit");
//        }
//        if (btProtocol[1] == (byte)0xC9)
//        {
//            Log.d(TAG, "Set Sensor Low Limit");
//        }
//        if (btProtocol[1] == (byte)0xCA)
//        {
//            Log.d(TAG, "Set Temperature Sensor");
//        }
//        if (btProtocol[1] == (byte)0xD1) {
//        Log.d(TAG, "On Lamp");
//    }
//        if (btProtocol[1] == (byte)0xD2) {
//            Log.d(TAG, "Off Lamp");
//        }
//        if (btProtocol[1] == (byte)0xD3) {
//            Log.d(TAG, "Open Value1");
//        }
//        if (btProtocol[1] == (byte)0xD4) {
//            Log.d(TAG, "Close Value1 ");
//        }
//        if (btProtocol[1] == (byte)0xD5) {
//            Log.d(TAG, "Open Value2");
//        }
//        if (btProtocol[1] == (byte)0xD6) {
//            Log.d(TAG, "Close Value2 ");
//        }
//        if (btProtocol[1] == (byte)0xE1) {
//            Log.d(TAG, "Calibrate Zero");
//        }
//        if (btProtocol[1] == (byte)0xE2) {
//            Log.d(TAG, "Calibrate 1Point Buffer");
//        }
//        if (btProtocol[1] == (byte)0xE3) {
//            Log.d(TAG, "Calibrate 2point Buffer");
//        }
//        if (btProtocol[1] == (byte)0xE4) {
//            Log.d(TAG, "Calibrate 1point Sample");
//        }
//        if (btProtocol[1] == (byte)0xE5) {
//            Log.d(TAG, "Calibrate 2point Sample");
//        }
//        if (btProtocol[1] == (byte)0xE6) {
//            Log.d(TAG, "Calibrate Temperature ");
//        }
//
//        if (btProtocol[1] == (byte)0xE7)
//        {
//            // Get Temperature Sensor
//            // Status 0 : OFF, 1 : On
//            byte bStatus = btProtocol[3];
//            if (bStatus == (byte)0x00) {
//                Log.d(TAG, "No Sensor Calibration");
//                m_nCaliStatus = 0;
//            }
//            else if (bStatus == (byte)0x01) {
//                Log.d(TAG, "Cal in Progress");
//                m_nCaliStatus = 1;
//            }
//            else if (bStatus == (byte)0x02) {
//                Log.d(TAG, "Cal OK");
//                m_nCaliStatus = 2;
//            }
//            else if (bStatus == (byte)0x03) {
//                Log.d(TAG, "Fail - Not Stable");
//                m_nCaliStatus = 3;
//            }
//            else if (bStatus == (byte)0x04) {
//                Log.d(TAG, "Fail - Buffer not found");
//                m_nCaliStatus = 4;
//            }
//            else if (bStatus == (byte)0x05) {
//                Log.d(TAG, "Fail - First Buffer not found");
//                m_nCaliStatus = 5;
//            }
//            else if (bStatus == (byte)0x06) {
//                Log.d(TAG, "Fail - Second Buffer not found");
//                m_nCaliStatus = 6;
//            }
//            else if (bStatus == (byte)0x07) {
//                Log.d(TAG, "Fail - Value too low");
//                m_nCaliStatus = 7;
//            }
//            else if (bStatus == (byte)0x08) {
//                Log.d(TAG, "Fail – Value too high");
//                m_nCaliStatus = 8;
//            }
//            else if (bStatus == (byte)0x09) {
//                Log.d(TAG, "Fail – Slope too low");
//                m_nCaliStatus = 9;
//            }
//            else if (bStatus == (byte)0x0A) {
//                Log.d(TAG, "Fail – Slope too high");
//                m_nCaliStatus = 10;
//            }
//            else if (bStatus == (byte)0x0B) {
//                Log.d(TAG, "Offset too low");
//                m_nCaliStatus = 11;
//            }
//            else if (bStatus == (byte)0x0C) {
//                Log.d(TAG, "Offset too high");
//                m_nCaliStatus = 12;
//            }
//            else if (bStatus == (byte)0x0D) {
//                Log.d(TAG, "Points too close");
//                m_nCaliStatus = 13;
//            }
//            else if (bStatus == (byte)0x0E) {
//                Log.d(TAG, "General Cal Fail(zero or sample)");
//                m_nCaliStatus = 14;
//            }
//     //       m_btCaliResponse = (byte)0xE7;
//
//        }
//        if (btProtocol[1] == (byte)0xE8) {
//            Log.d(TAG, "Diagnosis Sensor");
//        }
//        if (btProtocol[1] == (byte)0xE9) {
//            Log.d(TAG, "Search Sensor");
//        }
//        if (btProtocol[1] == (byte)0xEA) {
//            Log.d(TAG, "Search Sensor Status");
//        }
//    }

    public synchronized static boolean SendToCommunication(byte bSend[]){
      //  ivTX.setImageResource(R.mipmap.tx_on);
        startLightTxThread();
        if (m_strMode.equals("BlueTooth")) // Bluetooth
        {
            if(isConnected()) connector.write(bSend);
            else {
                stopConnection();
                m_strStatus = "Disconnected";
                msStaticHandler.sendEmptyMessage(0);
                return false;
            }
        }
        else if (m_strMode.equals("USB"))
        {
            if (sPort != null) {
                mSerialIoManager.writeAsync(bSend);
            }
            else {
                stopIoManager();
                m_strStatus = "Disconnected";
                msStaticHandler.sendEmptyMessage(0);
                return false;
            }
        }
        else if (m_strMode.equals("Ethernet"))
        {
            if (socket != null) {
                try {
                    networkWriter.write(bSend, 0, bSend.length);
                } catch (IOException e) {
                    //      m_DataCommunicationThread.stopDCThread();
                    e.printStackTrace();
                    StopEthernet();
                    m_strStatus = "Disconnected";
                    msStaticHandler.sendEmptyMessage(0);
                    return false;
                }
            }
            else {
                StopEthernet();
                m_strStatus = "Disconnected";
                msStaticHandler.sendEmptyMessage(0);
                return false;
            }
        }
        else return false;

        return true;
       // ivTX.setImageResource(R.mipmap.tx_off);
    }

//    public synchronized static void SendWash(int nMode) {
//
//        // FC5000003065
//        // FC500001F1A5
//        // FC5010003065
//        // FC501001F1A5
//
//        int nTotalLength = 6;
//        byte [] bSend = new byte [nTotalLength];
//        bSend[0] = (byte)0xFC;
//        bSend[1] = (byte)0x50;
//       if (nMode == 0) {
//           bSend[2] = (byte)0x00;
//           bSend[3] = (byte)0x01;
//       }
//       else if (nMode == 1) {
//           bSend[2] = (byte)0x00;
//           bSend[3] = (byte)0x00;
//       }
//       else if (nMode == 2) {
//           bSend[2] = (byte)0x01;
//           bSend[3] = (byte)0x01;
//       }
//       else if (nMode == 3) {
//           bSend[2] = (byte)0x01;
//           bSend[3] = (byte)0x00;
//       }
//
//        // CRC 추가
//        CRC16Modbus crc = new CRC16Modbus();
//        byte [] bCRC = crc.generateCRC(bSend, 4);
//        bSend[nTotalLength-2] = bCRC[0];
//        bSend[nTotalLength-1] = bCRC[1];
//
//        SendToCommunication(bSend);
//    }


    public synchronized static boolean SendProtocol_Get(int nLength, byte btCommand, byte[] btArrayData) {
        int nTotalLength = nLength + 7;
        byte [] bSend = new byte [nTotalLength];
        byte [] bLength = new byte [4];
        bLength = IntTo4Byte(nLength);
    //    byte bLength =  Byte.parseByte(Integer.toHexString(nLength),16);
        bSend[0] = (byte)0xFC;
        bSend[1] = bLength[0];
        bSend[2] = bLength[1];
        bSend[3] = bLength[2];
        bSend[4] = bLength[3];
        bSend[5] = btCommand;
        for (int i = 0; i < nLength-1; i++)
        {
            bSend[6+i] = btArrayData[i];
        }

        // CRC 추가
        CRC16Modbus crc = new CRC16Modbus();
        byte [] bCRC = crc.generateCRC(bSend, nLength+5);
        bSend[nTotalLength-2] = bCRC[0];
        bSend[nTotalLength-1] = bCRC[1];

        boolean bResult = false ;
        bResult = SendToCommunication(bSend);
        return bResult;

    }


    public synchronized static boolean SendProtocol_Set(int nLength, byte btCommand, byte[] btArrayData) {
        int nTotalLength = nLength + 7;
        byte [] bSend = new byte [nTotalLength];
        byte [] bLength = new byte [4];
        bLength = IntTo4Byte(nLength);
        //    byte bLength =  Byte.parseByte(Integer.toHexString(nLength),16);
        bSend[0] = (byte)0xFC;
        bSend[1] = bLength[0];
        bSend[2] = bLength[1];
        bSend[3] = bLength[2];
        bSend[4] = bLength[3];
        bSend[5] = btCommand;
        for (int i = 0; i < nLength-1; i++)
        {
            bSend[6+i] = btArrayData[i];
        }

        // CRC 추가
        CRC16Modbus crc = new CRC16Modbus();
        byte [] bCRC = crc.generateCRC(bSend, nLength+5);
        bSend[nTotalLength-2] = bCRC[0];
        bSend[nTotalLength-1] = bCRC[1];

        boolean bResult = false ;
        bResult = SendToCommunication(bSend);
        return bResult;
    }




    public static byte[] IntTo4Byte(int value) {
        byte[] byteArray = new byte[4];
        byteArray[0] = (byte)(value >> 24);
        byteArray[1] = (byte)(value >> 16);
        byteArray[2] = (byte)(value >> 8);
        byteArray[3] = (byte)(value);
        return byteArray;
    }


//    public synchronized static void SendProtocol(byte bCommand, int nLength, String strPayload, String strMode) {
//        int nTotalLength = nLength + 5;
//        byte [] bSend = new byte [nTotalLength];
//        byte bLength =  Byte.parseByte(Integer.toHexString(nLength),16);
//        bSend[0] = (byte)0xFC;
//        bSend[1] = bCommand;
//        bSend[2] = bLength;
//        bSend[3] = Byte.parseByte(Integer.toHexString(parseInt(strPayload)), 16);
//
//        // CRC 추가
//        CRC16Modbus crc = new CRC16Modbus();
//        byte [] bCRC = crc.generateCRC(bSend, nLength+3);
//        bSend[nTotalLength-2] = bCRC[0];
//        bSend[nTotalLength-1] = bCRC[1];
//
//        SendToCommunication(bSend);
//    }
//
//
//
//
//
//    public synchronized static void SendProtocol(byte bCommand, int nLength, String strPayload1, String strPayload2, String strMode) {
//        int nTotalLength = nLength + 5;
//        byte [] bSend = new byte [nTotalLength];
//        byte bLength =  Byte.parseByte(Integer.toHexString(nLength),16);
//
//        // Start Byte
//        bSend[0] = (byte)0xFC;
//        // Command
//        bSend[1] = bCommand;
//        bSend[2] = bLength;
//
//        if ((bCommand == (byte)0x43) || (bCommand == (byte)0x63))// Filter, 2포인트 Buffer
//        {
//            bSend[3] = Byte.parseByte(Integer.toHexString(parseInt(strPayload1)), 16);
//            bSend[4] = Byte.parseByte(Integer.toHexString(parseInt(strPayload2)), 16);
//        }
//        else if ((bCommand == (byte)0x64) || (bCommand == (byte)0x66))// 1포인트 sample ,  온도전극교정
//        {
//            bSend[3] = Byte.parseByte(Integer.toHexString(parseInt(strPayload1)), 16);
//            float fSensorValue = Float.parseFloat(strPayload2);
//            byte[] bSensorValue = new byte[4];
//            bSensorValue = floatTobyte(fSensorValue);
//            for (int i = 0; i < 4; i++) {
//                bSend[i + 4] = bSensorValue[i];
//            }
//        }
//        else
//        {
//            // Payload 연결
//            for (int i = 0; i < nLength; i++)
//            {
//                bSend[i+3] = Byte.parseByte(strPayload1+i,16);
//            }
//        }
//
//        // CRC 추가
//        CRC16Modbus crc = new CRC16Modbus();
//        byte [] bCRC = crc.generateCRC(bSend, nLength+3);
//        bSend[nTotalLength-2] = bCRC[0];
//        bSend[nTotalLength-1] = bCRC[1];
//
//        SendToCommunication(bSend);
//    }
//
//    public synchronized static void SendProtocol(byte bCommand, int nLength, String strPayload1, String strPayload2, String strPayload3, String strMode) {
//        int nTotalLength = nLength + 5;
//        byte [] bSend = new byte [nTotalLength];
//        byte bLength =  Byte.parseByte(Integer.toHexString(nLength),16);
//
//        // Start Byte
//        bSend[0] = (byte)0xFC;
//        // Command
//        bSend[1] = bCommand;
//        bSend[2] = bLength;
//
//        if (bCommand == (byte)0x65) // 2포인트 sample
//        {
//            bSend[3] = Byte.parseByte(Integer.toHexString(parseInt(strPayload1)), 16);
//            bSend[4] = Byte.parseByte(Integer.toHexString(parseInt(strPayload2)), 16);
//            float fSensorValue = Float.parseFloat(strPayload3);
//            byte [] bSensorValue1 = new byte [4];
//            bSensorValue1 = floatTobyte(fSensorValue);
//            for (int i = 0; i < 4; i++)
//            {
//                bSend[i+5] = bSensorValue1[i];
//            }
//        }
//        else
//        {
//            // Payload 연결
//            for (int i = 0; i < nLength; i++)
//            {
//                bSend[i+3] = Byte.parseByte(strPayload1+i,16);
//            }
//        }
//
//        // CRC 추가
//        CRC16Modbus crc = new CRC16Modbus();
//        byte [] bCRC = crc.generateCRC(bSend, nLength+3);
//        bSend[nTotalLength-2] = bCRC[0];
//        bSend[nTotalLength-1] = bCRC[1];
//
//        SendToCommunication(bSend);
//    }
//
//    public synchronized static void SendProtocol(byte bCommand, int nLength, String strPayload1, String strPayload2, String strPayload3, String strPayload4, String strPayload5, String strPayload6, String strMode) {
//        int nTotalLength = nLength + 5;
//        byte [] bSend = new byte [nTotalLength];
//        byte bLength =  Byte.parseByte(Integer.toHexString(nLength),16);
//        // Start Byte
//        bSend[0] = (byte)0xFC;
//        // Command
//        bSend[1] = bCommand;
//        bSend[2] = bLength;
//        if (bCommand == (byte)0x45)
//        {
//            bSend[3] = Byte.parseByte(Integer.toHexString(parseInt(strPayload1)), 16); // 센서 종류
//            bSend[4] = Byte.parseByte(Integer.toHexString(parseInt(strPayload2)), 16); // 필터
//            bSend[9] = Byte.parseByte(Integer.toHexString(parseInt(strPayload4)), 16); // 필터윈도우
//            float fFilterDeviation = Float.parseFloat(strPayload3);
//            float fRangeHigh = Float.parseFloat(strPayload5);
//            float fRangeLow = Float.parseFloat(strPayload6);
//            byte [] bFilterDeviation = new byte [4];
//            byte [] bRangeHigh = new byte [4];
//            byte [] bRangeLow = new byte [4];
//            bFilterDeviation = floatTobyte(fFilterDeviation);
//            bRangeHigh = floatTobyte(fRangeHigh);
//            bRangeLow = floatTobyte(fRangeLow);
//            for (int i = 0; i < 4; i++)
//            {
//                bSend[i+5] = bFilterDeviation[i];
//                bSend[i+10] = bRangeHigh[i];
//                bSend[i+14] = bRangeLow[i];
//            }
//        }
//        else
//        {
//            return;
//        }
//
//        // CRC 추가
//        CRC16Modbus crc = new CRC16Modbus();
//        byte [] bCRC = crc.generateCRC(bSend, nLength+3);
//        bSend[nTotalLength-2] = bCRC[0];
//        bSend[nTotalLength-1] = bCRC[1];
//
//        SendToCommunication(bSend);
//    }

    private void GetDigitalSettingPreferences()
    {
        // 출력->디지털 설정 설정 값가져오기
        String strTemp = "";
        prefs = getSharedPreferences("Tu_Digital", MODE_PRIVATE);
        strTemp = prefs.getString("HighAlarm", "");
        if (strTemp =="") m_dTuHighAlarm = 20;
        else m_dTuHighAlarm = parseDouble(strTemp);
        strTemp = prefs.getString("LowAlarm", "");
        if (strTemp =="") m_dTuLowAlarm = 0;
        else m_dTuLowAlarm = parseDouble(strTemp);
        strTemp = prefs.getString("Phase", "");
        if (strTemp =="") m_bTuPhaseIsHigh = TRUE;
        else  if (strTemp.equals("High"))  m_bTuPhaseIsHigh = TRUE;
        else if  (strTemp.equals("Low"))  m_bTuPhaseIsHigh = FALSE;
        strTemp = prefs.getString("DeadBand", "");
        if (strTemp =="") m_dTuDeadBand = 0;
        else m_dTuDeadBand = parseDouble(strTemp);
        strTemp = prefs.getString("OnDelay", "");
        if (strTemp =="") m_nTuOnDelay = 0;
        else m_nTuOnDelay = parseInt(strTemp);
        strTemp = prefs.getString("OffDelay", "");
        if (strTemp =="") m_nTuOffDelay = 0;
        else m_nTuOffDelay = parseInt(strTemp);
        strTemp = prefs.getString("SetPoint", "");
        if (strTemp =="") m_dTuSetPoint = 20;
        else m_dTuSetPoint = parseDouble(strTemp);

        prefs = getSharedPreferences("Ch_Digital", MODE_PRIVATE);
        strTemp = prefs.getString("HighAlarm", "");
        if (strTemp =="") m_dChHighAlarm = 2;
        else m_dChHighAlarm = parseDouble(strTemp);
        strTemp = prefs.getString("LowAlarm", "");
        if (strTemp =="") m_dChLowAlarm = 0;
        else m_dChLowAlarm = parseDouble(strTemp);
        strTemp = prefs.getString("Phase", "");
        if (strTemp =="") m_bChPhaseIsHigh = TRUE;
        else  if (strTemp.equals("High"))  m_bChPhaseIsHigh = TRUE;
        else if  (strTemp.equals("Low"))  m_bChPhaseIsHigh = FALSE;
        strTemp = prefs.getString("DeadBand", "");
        if (strTemp =="") m_dChDeadBand = 0;
        else m_dChDeadBand = parseDouble(strTemp);
        strTemp = prefs.getString("OnDelay", "");
        if (strTemp =="") m_nChOnDelay = 0;
        else m_nChOnDelay = parseInt(strTemp);
        strTemp = prefs.getString("OffDelay", "");
        if (strTemp =="") m_nChOffDelay = 0;
        else m_nChOffDelay = parseInt(strTemp);
        strTemp = prefs.getString("SetPoint", "");
        if (strTemp =="") m_dChSetPoint = 20;
        else m_dChSetPoint = parseDouble(strTemp);

        prefs = getSharedPreferences("Ph_Digital", MODE_PRIVATE);
        strTemp = prefs.getString("HighAlarm", "");
        if (strTemp =="") m_dPhHighAlarm = 14;
        else m_dPhHighAlarm = parseDouble(strTemp);
        strTemp = prefs.getString("LowAlarm", "");
        if (strTemp =="") m_dPhLowAlarm = 0;
        else m_dPhLowAlarm = parseDouble(strTemp);
        strTemp = prefs.getString("Phase", "");
        if (strTemp =="") m_bPhPhaseIsHigh = TRUE;
        else  if (strTemp.equals("High"))  m_bPhPhaseIsHigh = TRUE;
        else if  (strTemp.equals("Low"))  m_bPhPhaseIsHigh = FALSE;
        strTemp = prefs.getString("DeadBand", "");
        if (strTemp =="") m_dPhDeadBand = 0;
        else m_dPhDeadBand = parseDouble(strTemp);
        strTemp = prefs.getString("OnDelay", "");
        if (strTemp =="") m_nPhOnDelay = 0;
        else m_nPhOnDelay = parseInt(strTemp);
        strTemp = prefs.getString("OffDelay", "");
        if (strTemp =="") m_nPhOffDelay = 0;
        else m_nPhOffDelay = parseInt(strTemp);
        strTemp = prefs.getString("SetPoint", "");
        if (strTemp =="") m_dPhSetPoint = 20;
        else m_dPhSetPoint = parseDouble(strTemp);

        prefs = getSharedPreferences("Temp_Digital", MODE_PRIVATE);
        strTemp = prefs.getString("HighAlarm", "");
        if (strTemp =="") m_dTempHighAlarm = 90;
        else m_dTempHighAlarm = parseDouble(strTemp);
        strTemp = prefs.getString("LowAlarm", "");
        if (strTemp =="") m_dTempLowAlarm = 0;
        else m_dTempLowAlarm = parseDouble(strTemp);
        strTemp = prefs.getString("Phase", "");
        if (strTemp =="") m_bTempPhaseIsHigh = TRUE;
        else  if (strTemp.equals("High"))  m_bTempPhaseIsHigh = TRUE;
        else if  (strTemp.equals("Low"))  m_bTempPhaseIsHigh = FALSE;
        strTemp = prefs.getString("DeadBand", "");
        if (strTemp =="") m_dTempDeadBand = 0;
        else m_dTempDeadBand = parseDouble(strTemp);
        strTemp = prefs.getString("OnDelay", "");
        if (strTemp =="") m_nTempOnDelay = 0;
        else m_nTempOnDelay = parseInt(strTemp);
        strTemp = prefs.getString("OffDelay", "");
        if (strTemp =="") m_nTempOffDelay = 0;
        else m_nTempOffDelay = parseInt(strTemp);
        strTemp = prefs.getString("SetPoint", "");
        if (strTemp =="") m_dTempSetPoint = 90;
        else m_dTempSetPoint = parseDouble(strTemp);

        prefs = getSharedPreferences("Co_Digital", MODE_PRIVATE);
        strTemp = prefs.getString("HighAlarm", "");
        if (strTemp =="") m_dCoHighAlarm = 500;
        else m_dCoHighAlarm = parseDouble(strTemp);
        strTemp = prefs.getString("LowAlarm", "");
        if (strTemp =="") m_dCoLowAlarm = 0;
        else m_dCoLowAlarm = parseDouble(strTemp);
        strTemp = prefs.getString("Phase", "");
        if (strTemp =="") m_bCoPhaseIsHigh = TRUE;
        else  if (strTemp.equals("High"))  m_bCoPhaseIsHigh = TRUE;
        else if  (strTemp.equals("Low"))  m_bCoPhaseIsHigh = FALSE;
        strTemp = prefs.getString("DeadBand", "");
        if (strTemp =="") m_dCoDeadBand = 0;
        else m_dCoDeadBand = parseDouble(strTemp);
        strTemp = prefs.getString("OnDelay", "");
        if (strTemp =="") m_nCoOnDelay = 0;
        else m_nCoOnDelay = parseInt(strTemp);
        strTemp = prefs.getString("OffDelay", "");
        if (strTemp =="") m_nCoOffDelay = 0;
        else m_nCoOffDelay = parseInt(strTemp);
        strTemp = prefs.getString("SetPoint", "");
        if (strTemp =="") m_dCoSetPoint = 500;
        else m_dCoSetPoint = parseDouble(strTemp);
    }

    private void GetTuWashSettingPreferences(){
        String strTemp = "";
        prefs = getSharedPreferences("Tu_Wash", MODE_PRIVATE);
        strTemp = prefs.getString("Duration", "");
        if (strTemp =="") m_nTuDuration = 60;
        else  m_nTuDuration = parseInt(strTemp);
        strTemp = prefs.getString("Interval", "");
        if (strTemp =="") m_nTuInterval = 600;
        else m_nTuInterval = parseInt(strTemp);
        strTemp = prefs.getString("Stabilization", "");
        if (strTemp =="") m_nTuStabilization = 200;
        else m_nTuStabilization = parseInt(strTemp);
        strTemp = prefs.getString("High", "");
        if (strTemp =="") m_dTuHighWash = 20;
        else m_dTuHighWash = parseDouble(strTemp);
        strTemp = prefs.getString("Low", "");
        if (strTemp =="") m_dTuLowWash = 0;
        else m_dTuLowWash = parseDouble(strTemp);
        strTemp = prefs.getString("Auto", "");
        if (strTemp =="TRUE") m_bTuAutoWash = TRUE;
        else if (strTemp =="FALSE") m_bTuAutoWash = FALSE;
        else  m_bTuAutoWash = FALSE;
        strTemp = prefs.getString("ManualWash", "");
        if (strTemp =="TRUE") m_bTuManualWash = TRUE;
        else if (strTemp =="FALSE") m_bTuManualWash = FALSE;
        else  m_bTuManualWash = FALSE;
    }

    private void GetClWashSettingPreferences(){
        String strTemp = "";
        prefs = getSharedPreferences("Ch_Wash", MODE_PRIVATE);
        strTemp = prefs.getString("Duration", "");
        if (strTemp =="") m_nChDuration = 60;
        else m_nChDuration = parseInt(strTemp);
        strTemp = prefs.getString("Interval", "");
        if (strTemp =="") m_nChInterval = 600;
        else m_nChInterval = parseInt(strTemp);
        strTemp = prefs.getString("Stabilization", "");
        if (strTemp =="") m_nChStabilization = 200;
        else m_nChStabilization = parseInt(strTemp);
        strTemp = prefs.getString("High", "");
        if (strTemp =="") m_dChHighWash = 2;
        else m_dChHighWash = parseDouble(strTemp);
        strTemp = prefs.getString("Low", "");
        if (strTemp =="") m_dChLowWash = 0;
        else m_dChLowWash = parseDouble(strTemp);
        strTemp = prefs.getString("Auto", "");
        if (strTemp =="TRUE") m_bChAutoWash = TRUE;
        else if (strTemp =="FALSE") m_bChAutoWash = FALSE;
        else  m_bChAutoWash = FALSE;
        strTemp = prefs.getString("ManualWash", "");
        if (strTemp =="TRUE") m_bChManualWash = TRUE;
        else if (strTemp =="FALSE") m_bChManualWash = FALSE;
        else  m_bChManualWash = FALSE;
    }




    private static double CalculateSetRange(int nSensor, double dValue, boolean bPhaseIsHigh, double dSetPoint, double dDeadBand, int nOnDelay, int nOffDelay )
    {
        // SetPoint 및 Dead Band 에 대한 컨트롤
        // High 설정시
        double dSetPointHigh = 0;
        double dSetPointLow = 0;
        double dResultValue = 0;
        dResultValue = dValue;

        if (bPhaseIsHigh) {
            dSetPointHigh = dSetPoint;
            dSetPointLow  = dSetPoint - dDeadBand;
        }
        else {
            dSetPointHigh = dSetPoint + dDeadBand;
            dSetPointLow  = dSetPoint;
        }

        // 데이터가 설정 범위에 들어오는 경우
        if ((dValue >= dSetPointLow) && (dValue <= dSetPointHigh)) {
            if (nOnDelay == 0)  {
                m_dPreValue[nSensor] = dValue;
            }
            // On Delay 시작
            else if (m_nOnRangeTime[nSensor] < nOnDelay) {
                if (m_nOnRangeTime[nSensor] == 0)  {
                    if (DEBUG) Log.d(TAG, "On Delay Start");
                    m_dPreValue[nSensor] = dValue;
                }else {
                    dResultValue = m_dPreValue[nSensor];
                }
                if (DEBUG) Log.d(TAG, "m_nOnRangeTime" + Integer.toString(nSensor)+Integer.toString(m_nOnRangeTime[nSensor]));
            }
            // On Delay 종료
            else if (m_nOnRangeTime[nSensor] >= nOnDelay) {
                if (m_nOnRangeTime[nSensor] == nOnDelay)   {
                    if (DEBUG) Log.d(TAG, "On Delay End");
                }
                m_dPreValue[nSensor] = dValue;
                m_bC1Lane[nSensor] = TRUE;
            }
            m_nOffRangeTime[nSensor] = 0;
            m_nOnRangeTime[nSensor]++;
            if (m_nOnRangeTime[nSensor] > 1000000) m_nOnRangeTime[nSensor] = 1000000;
        }
        // 데이터가 설정 범위에 없는 경우
        else {
            if (!m_bC1Lane[nSensor])  {
                m_dPreValue[nSensor] = dValue;
                m_bC1Lane[nSensor] = FALSE;
            }
            // On -> Off 로 전환시 Off Delay 시작
            else {
                if (nOffDelay == 0)  {
                    m_dPreValue[nSensor] = dValue;
                }
                // Off Delay 시작
                else if (m_nOffRangeTime[nSensor] < nOffDelay) {
                    if (m_nOffRangeTime[nSensor] == 0){
                        if (DEBUG) Log.d(TAG, "Off Delay Start");
                        m_dPreValue[nSensor] = dValue;
                    }
                    else dResultValue = m_dPreValue[nSensor];
                    if (DEBUG) Log.d(TAG, "m_nTu_OffRangeTime"+ Integer.toString(nSensor) + Integer.toString(m_nOffRangeTime[nSensor]));
                }
                // Off Delay 종료
                else if (m_nOffRangeTime[nSensor] >= nOffDelay) {
                    if (m_nOffRangeTime[nSensor] == nOffDelay)   {
                        if (DEBUG) Log.d(TAG, "Off Delay End");
                    }
                    m_dPreValue[nSensor] = dValue;
                    m_bC1Lane[nSensor] = FALSE;
                }
            }
            m_nOnRangeTime[nSensor] = 0;
            m_nOffRangeTime[nSensor]++;

            if (m_nOffRangeTime[nSensor] > 1000000) m_nOffRangeTime[nSensor] = 1000000;
        }
        return dResultValue;
    }
}


