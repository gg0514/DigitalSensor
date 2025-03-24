package com.example.kyj.staqua;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.kyj.staqua.MainActivity.SendProtocol_Get;
import static com.example.kyj.staqua.MainActivity.StartTrendProcess;
import static com.example.kyj.staqua.MainActivity.m_bIsDownloadTrend;
import static com.example.kyj.staqua.MainActivity.m_nTrendProtocolLength;
import static com.example.kyj.staqua.MainActivity.m_nTrendReadCount;
/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivityTrendDownload extends Fragment {

    public Fragment fra = this;
    public ProgressBar pb;
    public TextView tvMainNotice;
    public TextView tvStatus1;
    public TextView tvStatus2;
    public TextView tvTitle;
    public Button btApply;
    public Button btStop;


    int nPreTrendRealCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_trend_download, container, false);

        v.findViewById(R.id.Trend_DL_Option1_Apply).setOnClickListener(mClickListener);
        v.findViewById(R.id.Trend_DL_Option1_Stop).setOnClickListener(mClickListener);

        pb = (ProgressBar) v.findViewById(R.id.Trend_DL_Title1_ProgressBar);
        tvMainNotice = (TextView) v.findViewById(R.id.Trend_DL_Content_Text);
        tvStatus1 = (TextView) v.findViewById(R.id.Trend_DL_Status_Text1);
        tvStatus2 = (TextView) v.findViewById(R.id.Trend_DL_Status_Text2);
        tvTitle = (TextView) v.findViewById(R.id.Trend_DL_Title1_Text);
        btApply = (Button) v.findViewById(R.id.Trend_DL_Option1_Apply);
        btStop = (Button) v.findViewById(R.id.Trend_DL_Option1_Stop);

        tvTitle.setVisibility(View.VISIBLE);
        pb.setVisibility(View.INVISIBLE);
        tvStatus1.setVisibility(View.INVISIBLE);
        tvStatus2.setVisibility(View.INVISIBLE);
        btApply.setVisibility(View.VISIBLE);
        btStop.setVisibility(View.INVISIBLE);

        return v;
    }

    public static SubActivityTrendDownload newInstance(String text) {

        SubActivityTrendDownload f = new SubActivityTrendDownload();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }


    Button.OnClickListener mClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            //이곳에 버튼 클릭시 일어날 일을 적습니다.
            switch (v.getId()) {
                case R.id.Trend_DL_Option1_Apply:

                    byte[] btData = new byte[1];
                    btData[0] = 0x00;
                    if (!SendProtocol_Get(1,(byte)0xA0, btData)){
                        Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                        return;
                    }
                    startProgressBarThread();
                    break;
                case R.id.Trend_DL_Option1_Stop:
                //    ExcelExporter.export();
                //    stopProgressBarThread(2);
                    break;
             }
        }
    };

    private volatile  Thread theProgressBarThread;

    public synchronized  void startProgressBarThread() {
        if (theProgressBarThread == null) {
            theProgressBarThread = new Thread(null, backgroundThread, "startProgressBarThread");
            theProgressBarThread.start();

            tvTitle.setVisibility(View.INVISIBLE);
            pb.setVisibility(View.VISIBLE);
            tvStatus1.setVisibility(View.VISIBLE);
            tvStatus2.setVisibility(View.VISIBLE);
            btApply.setVisibility(View.INVISIBLE);
            btStop.setVisibility(View.INVISIBLE);

            pb.setProgress(0);

            tvStatus1.setText("" + 0 + "%");
            tvStatus2.setText(0 + "/" + 0);
        }

    }
    public synchronized  void stopProgressBarThread(int nResult) {
        if (theProgressBarThread != null) {
            nPreTrendRealCount = 0;
           // m_nTrendProtocolLength = 0;
           // m_nTrendReadCount = 0;
            m_bIsDownloadTrend = false;
            Thread tmpThread = theProgressBarThread;
            theProgressBarThread = null;
            tmpThread.interrupt();
        }
        tvTitle.setVisibility(View.VISIBLE);
        pb.setVisibility(View.INVISIBLE);
        tvStatus1.setVisibility(View.INVISIBLE);
        tvStatus2.setVisibility(View.INVISIBLE);

        btStop.setVisibility(View.INVISIBLE);

        if (nResult == 1) {
            tvTitle.setText(R.string.multi_download_success);
            btApply.setVisibility(View.INVISIBLE);
            Toast.makeText(getActivity(), R.string.multi_download_success, Toast.LENGTH_SHORT).show();
            StartTrendProcess();
         //   startDataCommunicationThread();
           // getActivity().finish();
        }
        else if (nResult == 2) {
            tvTitle.setText(R.string.multi_download_failed);
         //   startDataCommunicationThread();
            btApply.setVisibility(View.VISIBLE);
        }
        m_bIsDownloadTrend = false;
    }

    private Runnable backgroundThread = new Runnable() {
        int nTimeout = 0;
        public void run() {
            if (Thread.currentThread() == theProgressBarThread) {
                nTimeout = 0;
                nPreTrendRealCount = 0;
                m_nTrendReadCount = 0;
                while (true) {
                    try {
                        progressBarHandle.sendMessage(progressBarHandle.obtainMessage());
                        Thread.sleep(10);
                    } catch (final InterruptedException e) {
                        return;
                    } catch (final Exception e) {
                        return;
                    }
                }
            }
        }

        Handler progressBarHandle = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (m_nTrendProtocolLength >= 0) {
                    int nProcess = 0;
                    if (m_nTrendProtocolLength > 0 ) {
                        nProcess = 100 * (m_nTrendReadCount) / (m_nTrendProtocolLength * 30);
                        pb.setProgress(nProcess);
                    }
                    tvStatus1.setText("" + nProcess + "%");
                    tvStatus2.setText(m_nTrendReadCount + "/" + m_nTrendProtocolLength * 30);

                    if (nPreTrendRealCount == m_nTrendReadCount) nTimeout++;
                    else nTimeout = 0;
                    if (m_nTrendProtocolLength > 0 ) {
                        if(m_nTrendReadCount >= m_nTrendProtocolLength * 30) stopProgressBarThread(1);
                    }

                    if (nTimeout > 300) {
                        if (nProcess > 80) {
                            tvStatus1.setText("" + 100 + "%");
                            tvStatus2.setText(m_nTrendProtocolLength * 30 + "/" + m_nTrendProtocolLength * 30);
                            stopProgressBarThread(1);
                        }
                        else stopProgressBarThread(2);
                    }

                    nPreTrendRealCount = m_nTrendReadCount;
                }

            }
        };
    };
}
