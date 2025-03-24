/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.kyj.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.kyj.DeviceData;
import com.example.kyj.staqua.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

import static com.example.kyj.staqua.MainActivity.m_bIsDownloadTrend;
import static java.lang.Long.parseLong;


public class DeviceConnector {
    private static final String TAG = "DeviceConnector";
    private static final boolean D = false;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_CONNECTING = 1; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 2;  // now connected to a remote device

    private int mState;

    private final BluetoothAdapter btAdapter;
    private final BluetoothDevice connectedDevice;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private final Handler mHandler;
    private final String deviceName;
    private Queue<byte[]> bufferQueue = new LinkedList<byte[]>();


    // ==========================================================================

    //

    public DeviceConnector(DeviceData deviceData, Handler handler) {
        mHandler = handler;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        connectedDevice = btAdapter.getRemoteDevice(deviceData.getAddress());
        deviceName = (deviceData.getName() == null) ? deviceData.getAddress() : deviceData.getName();
        mState = STATE_NONE;
    }
    // ==========================================================================


    public synchronized void connect() {
        if (D) Log.d(TAG, "connect to: " + connectedDevice);

        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                if (D) Log.d(TAG, "cancel mConnectThread");
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        if (mConnectedThread != null) {
            if (D) Log.d(TAG, "cancel mConnectedThread");
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(connectedDevice);
        mConnectThread.start();
       setState(STATE_CONNECTING);
    }
    // ==========================================================================

    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");

        if (mConnectThread != null) {
            if (D) Log.d(TAG, "cancel mConnectThread");
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            if (D) Log.d(TAG, "cancel mConnectedThread");
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }
    // ==========================================================================

    private synchronized void setState(int state) {
      //  if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
       mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }
    // ==========================================================================


    public synchronized int getState() {
        return mState;
    }
    // ==========================================================================


    public synchronized void connected(BluetoothSocket socket) {
     //   if (D) Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            if (D) Log.d(TAG, "cancel mConnectThread");
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            if (D) Log.d(TAG, "cancel mConnectedThread");
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_CONNECTED);

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME, deviceName);
        mHandler.sendMessage(msg);

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
    }
    // ==========================================================================


    public void write(byte[] data) {
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }

        // Perform the write unsynchronized
        if (data.length == 1) r.write(data[0]);
        else r.writeData(data);
    }
    // ==========================================================================


    private void connectionFailed() {
       // if (D) Log.d(TAG, "connectionFailed");

        // Send a failure message back to the Activity
//        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
//        Bundle bundle = new Bundle();
//        msg.setData(bundle);
//        mHandler.sendMessage(msg);
        setState(STATE_NONE);

    }
    // ==========================================================================


    private void connectionLost() {
        // Send a failure message back to the Activity
//        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
//        Bundle bundle = new Bundle();
//        msg.setData(bundle);
//        mHandler.sendMessage(msg);
        setState(STATE_NONE);
    }
    // ==========================================================================

    private class ConnectThread extends Thread {
        private static final String TAG = "ConnectThread";
        private static final boolean D = false;

        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;



        public ConnectThread(BluetoothDevice device) {
            if (D) Log.d(TAG, "create ConnectThread");
            mmDevice = device;
            mmSocket = BluetoothUtils.createRfcommSocket(mmDevice);
        }
        // ==========================================================================

        public void run() {
            if (D) Log.d(TAG, "ConnectThread run");
            btAdapter.cancelDiscovery();
            if (mmSocket == null) {
                if (D) Log.d(TAG, "unable to connect to device, socket isn't created");
                connectionFailed();
                return;
            }

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    if (D) Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (DeviceConnector.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket);
        }

        public void cancel() {
            if (D) Log.d(TAG, "ConnectThread cancel");

            if (mmSocket == null) {
                if (D) Log.d(TAG, "unable to close null socket");
                return;
            }
            try {
                mmSocket.close();
            } catch (IOException e) {
                if (D) Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    static public int byteToint(byte[] b)
    {
        String hexText = new java.math.BigInteger(b).toString(16);
        Long i = parseLong(hexText, 16);
        int myInt = (int) (long) i;
        return myInt;
    }


    private class ConnectedThread extends Thread {
        private static final String TAG = "ConnectedThread";
        private static final boolean D = false;

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        int nTempCount = 0;
        byte[] btTemp = new byte[1024];

        int nStartFlag = 0;


        public ConnectedThread(BluetoothSocket socket) {
            if (D) Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
               tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                if (D) Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
        //    BufferedReader Buffer_in = new BufferedReader(new InputStreamReader(mmInStream));
            byte[] buffer = new byte[1024];
            byte[] btEmpty = new byte[1024];
            int bytes ;
           while (true) {
                try {
                    if (m_bIsDownloadTrend) {
                        bytes = mmInStream.read(buffer, 0, 1024);
                        bufferQueue.offer(new byte[bytes]);
                        System.arraycopy(buffer, 0, bufferQueue.peek(), 0, bytes);
                        mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, bufferQueue.poll()).sendToTarget();
                    }
                    else {
                        bytes = mmInStream.read(buffer, 0, 1024);
                        System.arraycopy(buffer, 0, btTemp, nTempCount, bytes);
                        nTempCount += bytes;
                        if ( ( btTemp[0] == (byte)0xFC)  && nTempCount > 6)  {
                            byte[] bLength = new byte[4];
                            bLength[0] = btTemp[1];
                            bLength[1] = btTemp[2];
                            bLength[2] = btTemp[3];
                            bLength[3] = btTemp[4];
                            int nPayloadLen = byteToint(bLength);
                            if (nTempCount >= nPayloadLen + 7){
                                mHandler.obtainMessage(MainActivity.MESSAGE_READ, nTempCount, -1, btTemp).sendToTarget();
                                nTempCount = 0;
                            }
                        }
                        else {
                            nTempCount = 0;
                        }
                    }
                    System.arraycopy(btEmpty, 0, buffer, 0, buffer.length);

                }
                 catch (IOException e) {
                    if (D) Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        public void writeData(byte[] chunk) {
            try {
                mmOutStream.write(chunk);
                mmOutStream.flush();
                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(MainActivity.MESSAGE_WRITE, -1, -1, chunk).sendToTarget();
            } catch (IOException e) {
                if (D) Log.e(TAG, "Exception during write", e);
            }
        }
        public void write(byte command) {
            byte[] buffer = new byte[1];
            buffer[0] = command;

            try {
                mmOutStream.write(buffer);
                mHandler.obtainMessage(MainActivity.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException e) {
                if (D) Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                if (D) Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}




//                    if (!m_bIsDownloadTrend) {
//                        bytes = mmInStream.read(btBuffer); // 핸들러에 buffer를 그냥 넘기고있다.
//                        mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, btBuffer).sendToTarget();
//                    }
//                    else{
//
//                    }



//                    if (m_nTrendProtocolLength > 0) {
//                        nTotalBytes = nTotalBytes + bytes;
//                        Log.d(TAG, "[Receive Trend Step1]" + nTotalBytes);
//                    }

//                    if (m_bIsDownloadTrend) {
//                        for (int i = 0; i < bytes; i++) {
//                            bTrendData[m_nTrendReadCount+i] = btBuffer[i];
//                        }
//                        m_nTrendReadCount = m_nTrendReadCount + bytes;
//                        Log.d(TAG, "[Receive Trend Ing]" + m_nTrendReadCount);
//                        if (m_nTrendReadCount >= m_nTrendProtocolLength * 30) {
//                            Log.d(TAG, "[Receive Trend End]" + m_nTrendReadCount);
//                            m_nTrendReadCount = 0;
//                            m_bIsDownloadTrend = false;
//                        }
//                    }


//                    for (int i = 0; i < bytes; i++)
//                    {
//                        btSubBuffer[nRealCount + i] = btBuffer[i];
//                    }
//                    nRealCount = nRealCount + bytes;
//
//
//                    if (nRealCount > 6) {
//                        if (!m_bIsDownloadTrend) {
//                            if ((btSubBuffer[0] == (byte)0xFC) && (btSubBuffer[1] != (byte)0xF1) ) {
//                                int nLength = (int) btSubBuffer[2];
//                                // nLength = nLength + 5;
//                               // m_bIsDownloadTrend = false;
//                                mHandler.obtainMessage(MainActivity.MESSAGE_READ, nLength+5, 0, btSubBuffer).sendToTarget();
//                            }
//                            nRealCount = 0;
//                            if ((btSubBuffer[0] == (byte)0xFC) && (btSubBuffer[1] == (byte)0xF1)) {
//                                nTrendCount =0;
//                                byte[] bLength = new byte[4];
//                                bLength[0] = btSubBuffer[2];
//                                bLength[1] = btSubBuffer[3];
//                                bLength[2] = btSubBuffer[4];
//                                bLength[3] = btSubBuffer[5];
//                                nTotalBytes = byteToint(bLength);
//                                if ((nTotalBytes < 0) || (nTotalBytes > 10000000)) nTotalBytes = 10000;
//                                nTotalBytes = nTotalBytes / 5;
//                                mHandler.obtainMessage(MainActivity.MESSAGE_READ, nTotalBytes, 1, btSubBuffer).sendToTarget();
//                                m_bIsDownloadTrend = true;
//                                mHandler.obtainMessage(MainActivity.MESSAGE_READ, nTrendCount, 2, btSubBuffer).sendToTarget();
//                                for (int i = 0; i < nTrendCount; i++) {
//                                    bTrendData[i] = btSubBuffer[i];
//                                }
//                            }
//                        }
//                    else if (m_bIsDownloadTrend) {
//                        mHandler.obtainMessage(MainActivity.MESSAGE_READ, nTrendCount, 3, bTrendData).sendToTarget();
//                        for (int i = 0; i < bytes; i++) {
//                            bTrendData[nTrendCount+i] = btBuffer[i];
//                        }
//                        nTrendCount = nTrendCount + bytes;
//                        if (nTrendCount >= nTotalBytes) {
//                            mHandler.obtainMessage(MainActivity.MESSAGE_READ, nTrendCount, 4, bTrendData).sendToTarget();
//                            mHandler.obtainMessage(MainActivity.MESSAGE_READ, nTrendCount, 4, bTrendData).sendToTarget();
//                            nTrendCount = 0;
//                            m_bIsDownloadTrend = false;
//                        }
//                        nRealCount = 0;
//                    }

//                    }