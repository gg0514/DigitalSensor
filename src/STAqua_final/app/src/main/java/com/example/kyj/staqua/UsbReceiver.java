package com.example.kyj.staqua;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.kyj.staqua.utils.FTDriver;
import com.example.kyj.staqua.utils.FTDriverUtil;

/**
 * Created by 공영준 on 2016-07-10.
 */
public class UsbReceiver extends BroadcastReceiver {
    private Boolean SHOW_DEBUG = false;
    private String TAG = "HDJ";

    private Context mContext;
    private Activity mActivity;
    private FTDriver mSerial;
    private Handler mHandler = new Handler();
    private StringBuilder mText;

    private static final String ACTION_USB_PERMISSION = "kr.co.andante.mobiledgs.USB_PERMISSION";

    private boolean mStop = false;
    private boolean mRunningMainLoop = false;

    private static final int TEXT_MAX_SIZE = 8192;

    // Default settings
    private int mTextFontSize       = 10;
    private Typeface mTextTypeface  = Typeface.MONOSPACE;
    private int mDisplayType        = FTDriverUtil.DISP_CHAR;
    private int mBaudrate           = FTDriver.BAUD115200;
    private int mDataBits           = FTDriver.FTDI_SET_DATA_BITS_8;
    private int mParity             = FTDriver.FTDI_SET_DATA_PARITY_NONE;
    private int mStopBits           = FTDriver.FTDI_SET_DATA_STOP_BITS_1;
    private int mFlowControl        = FTDriver.FTDI_SET_FLOW_CTRL_NONE;
    private int mBreak              = FTDriver.FTDI_SET_NOBREAK;


    private boolean m_bSTX = false;

    public UsbReceiver(Activity activity, FTDriver serial)
    {
        mActivity = activity;
        mContext = activity.getBaseContext();
        mSerial = serial;
    }

    public int GetTextFontSize()
    {
        return mTextFontSize;
    }

    public int GetBaudrate()
    {
        return mBaudrate;
    }

    // Load default baud rate
    int loadDefaultBaudrate() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String res = pref.getString("baudrate_list", Integer.toString(FTDriver.BAUD115200));
        return Integer.valueOf(res);
    }

    void loadDefaultSettingValues() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String res = pref.getString("display_list", Integer.toString(FTDriverUtil.DISP_CHAR));
        mDisplayType = Integer.valueOf(res);

        res = pref.getString("fontsize_list", Integer.toString(12));
        mTextFontSize = Integer.valueOf(res);

        res = pref.getString("typeface_list", Integer.toString(3));
        switch(Integer.valueOf(res)){
            case 0:
                mTextTypeface = Typeface.DEFAULT;
                break;
            case 1:
                mTextTypeface = Typeface.SANS_SERIF;
                break;
            case 2:
                mTextTypeface = Typeface.SERIF;
                break;
            case 3:
                mTextTypeface = Typeface.MONOSPACE;
                break;
        }

        res = pref.getString("readlinefeedcode_list", Integer.toString(FTDriverUtil.LINEFEED_CODE_CRLF));
        FTDriverUtil.mReadLinefeedCode = Integer.valueOf(res);

        res = pref.getString("writelinefeedcode_list", Integer.toString(FTDriverUtil.LINEFEED_CODE_CRLF));
        FTDriverUtil.mWriteLinefeedCode = Integer.valueOf(res);

        res = pref.getString("email_edittext", "@gmail.com");

        res = pref.getString("databits_list", Integer.toString(FTDriver.FTDI_SET_DATA_BITS_8));
        mDataBits = Integer.valueOf(res);
        mSerial.setSerialPropertyDataBit(mDataBits, FTDriver.CH_A);

        res = pref.getString("parity_list", Integer.toString(FTDriver.FTDI_SET_DATA_PARITY_NONE));
        mParity = Integer.valueOf(res) << 8; // parity_list's number is 0 to 4
        mSerial.setSerialPropertyParity(mParity, FTDriver.CH_A);

        res = pref.getString("stopbits_list", Integer.toString(FTDriver.FTDI_SET_DATA_STOP_BITS_1));
        mStopBits = Integer.valueOf(res) << 11; // stopbits_list's number is 0 to 2
        mSerial.setSerialPropertyStopBits(mStopBits, FTDriver.CH_A);

        res = pref.getString("flowcontrol_list", Integer.toString(FTDriver.FTDI_SET_FLOW_CTRL_NONE));
        mFlowControl = Integer.valueOf(res) << 8;
        mSerial.setFlowControl(FTDriver.CH_A, mFlowControl);

        res = pref.getString("break_list", Integer.toString(FTDriver.FTDI_SET_NOBREAK));
        mBreak = Integer.valueOf(res) << 14;
        mSerial.setSerialPropertyBreak(mBreak, FTDriver.CH_A);

        mSerial.setSerialPropertyToChip(FTDriver.CH_A);
    }

    public void openUsbSerial() {
        if (!mSerial.isConnected()) {
            if (SHOW_DEBUG) {
                Log.d(TAG, "onNewIntent begin");
            }
            mBaudrate = loadDefaultBaudrate();
            if (!mSerial.begin(mBaudrate)) {
                Toast.makeText(mContext, "cannot open", Toast.LENGTH_SHORT).show();
                return;
            } else {
                Toast.makeText(mContext, "connected", Toast.LENGTH_SHORT).show();
            }
        }

        if (!mRunningMainLoop) {
            mainloop();
        }

    }

    public void closeUsbSerial() {
        detachedUi();
        mStop = true;
        mSerial.end();
    }

    public boolean writeDataToSerial() {
    //public void writeDataToSerial(String strWrite) {
        // MEASURE A 전송

        boolean bResult;

        byte [] bSend = new byte [11];
        bSend[0] = 0x02;
        bSend[1] = 0x4D;
        bSend[2] = 0x45;
        bSend[3] = 0x41;
        bSend[4] = 0x53;
        bSend[5] = 0x55;
        bSend[6] = 0x52;
        bSend[7] = 0x45;
        bSend[8] = 0x20;
        bSend[9] = 0x41;
        bSend[10] = 0x03;
        //strWrite = FTDriverUtil.changeLinefeedcode(strWrite);
        if(mSerial.isConnected()){
            final int nVal = mSerial.write(bSend, 11);
            bResult = true;
        }
        else{
            Toast.makeText(mContext, "Usb is disconnection", Toast.LENGTH_SHORT).show();
            bResult = false;
        }
        return bResult;
    }

    public boolean writeStartValidationData() {

        // ANALOG SFT_B 전송
        boolean bResult;
        byte [] bSend = new byte [14];
        bSend[0] = 0x02;
        bSend[1] = 0x41;
        bSend[2] = 0x4E;
        bSend[3] = 0x41;
        bSend[4] = 0x4C;
        bSend[5] = 0x4F;
        bSend[6] = 0x47;
        bSend[7] = 0x20;
        bSend[8] = 0x53;
        bSend[9] = 0x46;
        bSend[10] = 0x54;
        bSend[11] = 0x5F;
        bSend[12] = 0x42;
        bSend[13] = 0x03;

        if(mSerial.isConnected()){
            final int nVal = mSerial.write(bSend, 14);
            bResult = true;
        }
        else{
        //    Toast.makeText(mContext, "Usb is disconnection", Toast.LENGTH_SHORT).show();
            bResult = false;
        }
        return bResult;
    }

    public void mainloop() {
        mStop = false;
        mRunningMainLoop = true;
       // Toast.makeText(mContext, "connected", Toast.LENGTH_SHORT).show();
        if (SHOW_DEBUG) {
            Log.d(TAG, "start mainloop");
        }
        new Thread(mLoop).start();
    }

    private Runnable mLoop = new Runnable() {
        byte[] sendbuf = new byte[1024];
        byte[] sendEmpty = new byte[1024];
        int nCount = 0;
        int nLen = 0;
        @Override
        public void run() {
            byte[] rbuf = new byte[1024];
            for (;;) {
                nLen = mSerial.read(rbuf);
                if (nLen > 0) {
                    rbuf[nLen] = 0x0D;
                    byte[] tempbuf = rbuf;
                    for (int i = 0; i < nLen; i++) {
                        if (tempbuf[i] == 0x0D) {
                            mText = new StringBuilder();
                            System.arraycopy(sendEmpty, 0, sendbuf, 0, nLen);
                            nCount = 0;
                        }
                        else if (tempbuf[i] == 0x02) {
                            nCount = 0;
                            System.arraycopy(sendEmpty, 0, sendbuf, 0, nLen);
                        }
                        else if (tempbuf[i] == 0x03) {
                            mText = new StringBuilder();
//                            OnReadMessage(sendbuf, nCount);
//                            mHandler.post(new Runnable() {
//                            public void run() {
//                                    ((MainActivity) mActivity).onSetText(mText.toString());
//                            }
//                            });
                            System.arraycopy(sendEmpty, 0, sendbuf, 0, nLen);
                            nCount = 0;
                        }
                        else {
                            sendbuf[nCount] = tempbuf[i];
                            nCount++;
                        }
                    }
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (mStop) {
                    mRunningMainLoop = false;
                    return;
                }
            }
        }
    };

    private void OnReadMessage(byte[] rbuf, int len)
    {
        switch (mDisplayType) {
            case FTDriverUtil.DISP_CHAR:
                FTDriverUtil.setSerialDataToTextView(mText, mDisplayType, rbuf, len, "", "");
                break;
            case FTDriverUtil.DISP_DEC:
                FTDriverUtil.setSerialDataToTextView(mText, mDisplayType, rbuf, len, "013", "010");
                break;
            case FTDriverUtil.DISP_HEX:
                FTDriverUtil.setSerialDataToTextView(mText, mDisplayType, rbuf, len, "0d", "0a");
                break;
        }

        if (SHOW_DEBUG) {
            Log.d(TAG, "Read Length : " + len +"/" +mText);
        }
    }

    private void detachedUi() {
        if(mSerial.isConnected()) {
            Toast.makeText(mContext, "disconnect", Toast.LENGTH_SHORT).show();
          //  txStartValue.setText(strStart);
        }
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            if (SHOW_DEBUG) {
                Log.d(TAG, "Device attached");
//                mHandler.post(new Runnable() {
//                    public void run() {
//                        ((MainActivity) mActivity).onStstusText("접속");
//                    }
//                });
                Toast.makeText(mContext, "Device attached", Toast.LENGTH_SHORT).show();
            }
            if (!mSerial.isConnected()) {
                if (SHOW_DEBUG) {
                    Log.d(TAG, "Device attached begin");
                    Toast.makeText(mContext, "Device attached begin", Toast.LENGTH_SHORT).show();
                }
                mBaudrate = loadDefaultBaudrate();
                mSerial.begin(mBaudrate);
                loadDefaultSettingValues();
            }
            if (!mRunningMainLoop) {
                if (SHOW_DEBUG) {
                    Log.d(TAG, "Device attached mainloop");
                    Toast.makeText(mContext, "Device attached mainloop", Toast.LENGTH_SHORT).show();
                }
                mainloop();
            }
        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            if (SHOW_DEBUG) {
                Log.d(TAG, "Device detached");
                Toast.makeText(mContext, "Device detached", Toast.LENGTH_SHORT).show();
            }
            mStop = true;
            detachedUi();
//            mHandler.post(new Runnable() {
//                public void run() {
//                    ((MainActivity) mActivity).onStstusText("해제");
//                }
//            });
            mSerial.usbDetached(intent);
            mSerial.end();
        } else if (ACTION_USB_PERMISSION.equals(action)) {
            if (SHOW_DEBUG) {
                Log.d(TAG, "Request permission");
                Toast.makeText(mContext, "Request permission", Toast.LENGTH_SHORT).show();
            }
            synchronized (this) {
                if (!mSerial.isConnected()) {
                    if (SHOW_DEBUG) {
                        Log.d(TAG, "Request permission begin");
                        Toast.makeText(mContext, "Request permission begin", Toast.LENGTH_SHORT).show();
                    }
                    mBaudrate = loadDefaultBaudrate();
                    mSerial.begin(mBaudrate);
                    loadDefaultSettingValues();
                }
            }
            if (!mRunningMainLoop) {
                if (SHOW_DEBUG) {
                    Log.d(TAG, "Request permission mainloop");
                    Toast.makeText(mContext, "Request permission mainloop", Toast.LENGTH_SHORT).show();
                }
                mainloop();
            }
        }
    }
}
