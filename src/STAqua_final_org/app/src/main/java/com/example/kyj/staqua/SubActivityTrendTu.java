package com.example.kyj.staqua;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.kyj.staqua.MainActivity.arrayTuTrend;
import static com.example.kyj.staqua.MainActivity.m_bCLOnRange;
import static com.example.kyj.staqua.MainActivity.m_dAverage;
import static com.example.kyj.staqua.MainActivity.m_dCLMax;
import static com.example.kyj.staqua.MainActivity.m_dCLMin;
import static com.example.kyj.staqua.MainActivity.m_dTu;
import static com.example.kyj.staqua.MainActivity.m_nTrendFilter;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivityTrendTu extends Fragment {

    // 그래프 그리기
    private static XYMultipleSeriesDataset tuMultipleSeriesDataset;
    private static XYMultipleSeriesRenderer tuMultipleSeriesRenderer;
    private static XYSeriesRenderer tuSeriesRenderer;
    private static XYSeries tuSeries;
    private static GraphicalView tuGv;
    private static View v;

    private SubActivityTrendTu.TuTrendThread m_TuTrendThread;
    TextView txTuCurrentValue;

    TextView txTuAverageTitle;
    TextView txTuAverageValue;
    TextView txTuConfidenceTitle;
    TextView txTuConfidenceValue;

    double m_dXMax = 600;
    double m_dXMin = 0;
    int m_nXLength = 0;

    int m_nX = 0;
    int m_nIndex = 0;

    // 3600 초가 넘어갈 경우 초단위에서 분단위로 변경 Flag
    boolean m_bIsSecond = TRUE;
    int m_bThreadTime = 1000;

    ProgressBar progressCircle;
    LinearLayout ll;

    ArrayList<Double> arrayTuValue;

    String m_strXAxis = "";
    double m_dPreTuValue = 0;

    boolean m_bIsHistory = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_trend_tu, container, false);

        final Button btnHistory = (Button) v.findViewById(R.id.Trend_Tu_Content_Btn_History);
        final Button btnXIn = (Button) v.findViewById(R.id.Trend_Tu_X_Btn_In) ;
        final Button btnXOut = (Button) v.findViewById(R.id.Trend_Tu_X_Btn_Out) ;
        Button.OnClickListener onClickListener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date d = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String strDate = sdf.format(d);
                switch (view.getId()) {
                    case R.id.Trend_Tu_X_Btn_In :
                        m_dXMax= m_dXMax - m_nXLength / 2;
                        if (m_dXMax <= 10) m_dXMax = 10;
                        m_nXLength = (int)m_dXMax - (int)m_dXMin;
                        if (m_TuTrendThread != null) {
                            m_TuTrendThread.stopTrendThread();
                            m_nX = 0;
                        }
                        ll.removeView(tuGv);
                        showTuChart();
                        tuGetFileData(strDate);
                        m_TuTrendThread = new SubActivityTrendTu.TuTrendThread(true);
                        m_TuTrendThread.start();
                        break ;
                    case R.id.Trend_Tu_X_Btn_Out :
                        m_dXMax= m_dXMax + m_nXLength;
                        if ((m_bIsSecond) && (m_dXMax > 3600)) m_dXMax = 3600;
                        if ((!m_bIsSecond) && (m_dXMax > 1440)) m_dXMax = 1440;
                        if (m_TuTrendThread != null) {
                            m_TuTrendThread.stopTrendThread();
                            m_nX = 0;
                        }
                        ll.removeView(tuGv);
                        showTuChart();
                        tuGetFileData(strDate);
                        m_TuTrendThread = new SubActivityTrendTu.TuTrendThread(true);
                        m_TuTrendThread.start();
                        break ;
                    case R.id.Trend_Tu_Previous_Btn_Left:
                        if (!m_bIsHistory) {
                            m_dXMin = m_dXMin - m_nXLength;
                            if ((m_bIsSecond) && (m_dXMin < 0)) // 초단위에서 0 이하로 이동시 경고화면 후 복귀
                            {
                                m_dXMin = m_dXMin + m_nXLength;
                                Toast.makeText(getActivity(), "X value is less than 0", Toast.LENGTH_SHORT).show();
                                break;
                            }
                            else if ((!m_bIsSecond) && (m_dXMin < 0)) // 분단위에서 초단위로 이동시
                            {
                                m_bIsSecond = TRUE;
                                m_bThreadTime = 1000;
                                m_dXMin = 0;
                                m_dXMax = 600;
                                m_strXAxis = getResources().getString(R.string.multi_second_previous);
                            }
                            else if ((!m_bIsSecond) && (m_dXMax > 1440)) // 분단위에서의 값 감소시
                            {
                                m_dXMax = m_dXMax - m_nXLength;
                            }
                            else
                            {
                                m_dXMax = m_dXMax - m_nXLength;
                            }

                            if (m_TuTrendThread != null) {
                                m_TuTrendThread.stopTrendThread();
                                m_nX = 0;
                            }
                            ll.removeView(tuGv);
                            arrayTuValue.clear();
                            showTuChart();
                            tuGetFileData(strDate);
                            m_TuTrendThread = new SubActivityTrendTu.TuTrendThread(true);
                            m_TuTrendThread.start();
                        }
                        else {
                            m_dXMin = m_dXMin - m_nXLength;
                            if (m_dXMin < 0) // 초단위에서 0 이하로 이동시 경고화면 후 복귀
                            {
                                m_dXMin = m_dXMin + m_nXLength;
                                Toast.makeText(getActivity(), R.string.multi_x_value_is_less_than, Toast.LENGTH_SHORT).show();
                                break;
                            }
                            else
                            {
                                m_dXMax = m_dXMax - m_nXLength;
                            }
                            ll.removeView(tuGv);
                            arrayTuValue.clear();

                            showTuChart();
                            tuChartAddHistory();
                        }

                        break ;
                    case R.id.Trend_Tu_Previous_Btn_Right:
                        if (!m_bIsHistory) {
                            m_dXMax = m_dXMax + m_nXLength;
                            if ( (m_bIsSecond) && (m_dXMax > 3600)) // 10분이 넘어가는 경우 분단위의 차트를 보여준다
                            {
                                m_bIsSecond = FALSE;
                                m_bThreadTime = 60000;
                                m_dXMin = 0;
                                m_dXMax = 180;
                                m_strXAxis = getResources().getString(R.string.multi_minute_previous);
                            }
                            else if ((!m_bIsSecond) && (m_dXMax <= 1440)) // 분단위에서의 값 증가시
                            {
                                m_dXMin = m_dXMin + m_nXLength;
                            }
                            else if ((!m_bIsSecond) && (m_dXMax > 1440)) // 분단위에서의 하루이상 초과시 경고 화면 후 복귀
                            {
                                m_dXMax = m_dXMax - m_nXLength;
                                Toast.makeText(getActivity(), R.string.multi_x_value_is_more_than, Toast.LENGTH_SHORT).show();
                                break;
                            }
                            else
                            {
                                m_dXMin = m_dXMin + m_nXLength;
                            }

                            if (m_TuTrendThread != null) {
                                m_TuTrendThread.stopTrendThread();
                                m_nX = 0;
                            }
                            ll.removeView(tuGv);
                            arrayTuValue.clear();
                            showTuChart();
                            tuGetFileData(strDate);
                            m_TuTrendThread = new SubActivityTrendTu.TuTrendThread(true);
                            m_TuTrendThread.start();
                        }
                        else {
                            m_dXMax = m_dXMax + m_nXLength;
                            if ((m_dXMax > 129600))
                            {
                                m_dXMax = m_dXMax - m_nXLength;
                                //      Toast.makeText(getActivity(), R.string.multi_x_value_is_more_than, Toast.LENGTH_SHORT).show();
                                break;
                            }
                            else
                            {
                                m_dXMin = m_dXMin + m_nXLength;
                            }
                            ll.removeView(tuGv);
                            arrayTuValue.clear();

                            showTuChart();
                            tuChartAddHistory();
                        }
                        break ;
                    case R.id.Trend_Tu_Content_Btn_History:
                        m_bIsHistory = true;
                        m_bIsSecond = FALSE;
                        m_dXMin = 0;
                        m_dXMax = 720;
                        m_strXAxis = getResources().getString(R.string.multi_hour_previous);

                        if (m_TuTrendThread != null) {
                            m_TuTrendThread.stopTrendThread();
                            SystemClock.sleep(1000);
                            m_nX = 0;
                        }
                        //   m_PhTrendThread.stopTrendThread();
                        ll.removeView(tuGv);
                        arrayTuValue.clear();

                        showTuChart();
                        tuChartAddHistory();
                        break;
                }
            }
        } ;
        btnXIn.setOnClickListener(onClickListener) ;
        btnXOut.setOnClickListener(onClickListener) ;
        Button btnLeft = (Button) v.findViewById(R.id.Trend_Tu_Previous_Btn_Left) ;
        btnLeft.setOnClickListener(onClickListener) ;
        Button btnRight = (Button) v.findViewById(R.id.Trend_Tu_Previous_Btn_Right) ;
        btnRight.setOnClickListener(onClickListener) ;

        btnHistory.setOnClickListener(onClickListener) ;

        txTuCurrentValue = (TextView)v.findViewById(R.id.Trend_Tu_Data_Text_Value);
        txTuAverageTitle = (TextView)v.findViewById(R.id.Trend_Tu_Data_Average_Title);
        txTuAverageValue = (TextView)v.findViewById(R.id.Trend_Tu_Data_Average_Value);
        txTuConfidenceTitle = (TextView)v.findViewById(R.id.Trend_Tu_Confidence_Title);
        txTuConfidenceValue = (TextView)v.findViewById(R.id.Trend_Tu_Confidence_Value);


        // progressCircle = (ProgressBar)v.findViewById(R.id.Trend_Tu_Progress);
        ll = (LinearLayout) v.findViewById(R.id.Trend_Tu_Chart);
        arrayTuValue = new ArrayList();

        btnXIn.setVisibility(View.INVISIBLE);
        btnXOut.setVisibility(View.INVISIBLE);
        m_bIsHistory = true;
        m_bIsSecond = FALSE;
        m_dXMin = 0;
        m_dXMax = 720;
        m_strXAxis = getResources().getString(R.string.multi_hour_previous);
        ll.removeView(tuGv);
        arrayTuValue.clear();
        showTuChart();
        tuChartAddHistory();

        if (m_nTrendFilter == 1) {
            txTuAverageTitle.setVisibility(View.INVISIBLE);
            txTuAverageValue.setVisibility(View.INVISIBLE);
            txTuConfidenceTitle.setVisibility(View.INVISIBLE);
            txTuConfidenceValue.setVisibility(View.INVISIBLE);
        }
        else if (m_nTrendFilter == 2) {
            txTuAverageTitle.setVisibility(View.VISIBLE);
            txTuAverageTitle.setText(R.string.multi_trend_moving_average);
            txTuAverageValue.setVisibility(View.VISIBLE);
            txTuConfidenceTitle.setVisibility(View.INVISIBLE);
            txTuConfidenceValue.setVisibility(View.INVISIBLE);
        }
        else if (m_nTrendFilter == 3) {
            txTuAverageTitle.setVisibility(View.VISIBLE);
            txTuAverageTitle.setText(R.string.multi_trend_on_range);
            txTuAverageValue.setVisibility(View.VISIBLE);
            txTuConfidenceTitle.setVisibility(View.VISIBLE);
            txTuConfidenceValue.setVisibility(View.VISIBLE);
        }

 //       m_TuTrendThread = new TuTrendThread(true);
 //       m_TuTrendThread.start();
        return v;
    }

    @Override
    public void onStop() {
        if (m_TuTrendThread != null) {
            m_TuTrendThread.stopTrendThread();
        }
        m_nX = 0;
        m_nIndex = 0;
        super.onStop();
    }

    public static SubActivityTrendTu newInstance(String text) {

        SubActivityTrendTu f = new SubActivityTrendTu();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    public  void tuGetFileData(String strDate){
        String dirPath = "";
        if (m_bIsSecond == TRUE) dirPath = Environment.getExternalStorageDirectory()+"/STAqua/trend/"+strDate+"/lane1.txt";
        else dirPath = Environment.getExternalStorageDirectory()+"/STAqua/trend/"+strDate+"/lane1_min.txt";

        try{
            RandomAccessFile file = new RandomAccessFile(dirPath, "r");
            long fileSize = file.length();
            long pos = fileSize - 2;
            int i = 0;
            byte[] buff = new byte [20];
            while (true) {
                file.seek(pos);
                if (file.readByte() == 0xA)
                {
                    break;
                }
                else pos --;
            }

            while ((i < m_dXMax ) && (pos > 0))
            {
                file.seek(pos);
                if (file.readByte()== 0xA){
                    pos++;
                    file.seek(pos);
                    file.read(buff);
                    String strTemp = new String(buff,0,20);
                    int nIndexTime = strTemp.indexOf("|");
                    int nIndexEnd = strTemp.indexOf("\n");
                    String strValue = strTemp.substring(nIndexTime+1, nIndexEnd);
                    arrayTuValue.add(Double.parseDouble(strValue));
                    pos = pos - nIndexEnd - 2;
                    i++;
                }
                else pos--;
            }
            file.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public  void tuChartAdd(){
        tuChartClear();
        for (int i = 0; i < m_dXMax; i++) {
            if (i >= arrayTuValue.size()) tuSeries.add(i,0);
            else tuSeries.add(i,arrayTuValue.get(i));
        }
        if (tuGv != null){
            tuGv.repaint();
        }
        else{
            tuSeries.add(0, arrayTuValue.get(0));
            tuMultipleSeriesDataset.addSeries(tuSeries);
            tuGv =  ChartFactory.getLineChartView(getActivity().getBaseContext(), tuMultipleSeriesDataset, tuMultipleSeriesRenderer);
            ll.addView(tuGv);
        }
    }
    public  void tuChartAddHistory(){
        tuChartClear();
        for (int i = 0; i < m_dXMax; i++) {
            if (i >= arrayTuTrend.size()) tuSeries.add(i,0);
            else tuSeries.add(i,arrayTuTrend.get(i));
        }
        if (tuGv != null){
            tuGv.repaint();
        }
        else{
            tuSeries.add(0, arrayTuTrend.get(0));
            tuMultipleSeriesDataset.addSeries(tuSeries);
            tuGv =  ChartFactory.getLineChartView(getActivity().getBaseContext(), tuMultipleSeriesDataset, tuMultipleSeriesRenderer);
            ll.addView(tuGv);
        }
    }

    public  void tuChartClear(){
        tuSeries.clear();
    }


    public  void showTuChart() {

        tuMultipleSeriesDataset = new XYMultipleSeriesDataset();
        tuMultipleSeriesRenderer = new XYMultipleSeriesRenderer();
        tuSeriesRenderer = new XYSeriesRenderer();
        //tuSeries = new XYSeries("Turbidity");
        tuSeries = new XYSeries(getResources().getString(R.string.multi_tu1));

        // Adding Income Series to the dataset
        tuMultipleSeriesDataset.addSeries(tuSeries);
        // Creating XYSeriesRenderer to customize incomeSeries
        tuSeriesRenderer.setColor(Color.YELLOW);
        tuSeriesRenderer.setPointStyle(PointStyle.POINT);
        tuSeriesRenderer.setFillPoints(true);
        tuSeriesRenderer.setLineWidth(2);
        tuSeriesRenderer.setDisplayChartValues(false);

        int [] nMargin = new int[]{80, 40, 100, 40};
        tuMultipleSeriesRenderer.setMargins(nMargin);
        tuMultipleSeriesRenderer.setChartTitleTextSize(0);
        // tuMultipleSeriesRenderer.setTextTypeface("TEST",2);
        tuMultipleSeriesRenderer.setAxisTitleTextSize(30);
        tuMultipleSeriesRenderer.setLabelsTextSize(20);
        tuMultipleSeriesRenderer.setXAxisMin(m_dXMin);
        tuMultipleSeriesRenderer.setXAxisMax(m_dXMax);

        // X축의 개수를 구함
        m_nXLength = (int)m_dXMax - (int)m_dXMin;

        tuMultipleSeriesRenderer.setYAxisMin(0.00);
        tuMultipleSeriesRenderer.setYAxisMax(2.00);
        tuMultipleSeriesRenderer.setLegendTextSize(50);
        tuMultipleSeriesRenderer.setLegendHeight(100);

        if (m_bIsHistory) {
            tuMultipleSeriesRenderer.setXLabels(0);
            for (int i = 0; i < 6; i++ ) {
                tuMultipleSeriesRenderer.addXTextLabel(m_dXMin + (i * 120), String.format("-%.0fH", (m_dXMin + (i * 120))/60));
            }
            tuMultipleSeriesRenderer.addXTextLabel(m_dXMin + (6 * 120), String.format("-%.0fH", (m_dXMin + (6 * 120))/60));
        }
        else {
            tuMultipleSeriesRenderer.setXLabels(0);
            if (m_bIsSecond) {
                for (int i = 0; i < 6; i++ ) {
                    tuMultipleSeriesRenderer.addXTextLabel(m_dXMin + (m_nXLength / 6) * i, String.format("-%.0fS", m_dXMin + (m_nXLength / 6) * i));
                }
                tuMultipleSeriesRenderer.addXTextLabel(m_dXMin + (m_nXLength / 6) * 6, String.format("-%.0fS", m_dXMin + (m_nXLength / 6) * 6));
            }
            else {
                for (int i = 0; i < 6; i++ ) {
                    tuMultipleSeriesRenderer.addXTextLabel(m_dXMin + (m_nXLength / 6) * i, String.format("-%.0fM", m_dXMin + (m_nXLength / 6) * i));
                }
                tuMultipleSeriesRenderer.addXTextLabel(m_dXMin + (m_nXLength / 6) * 6, String.format("-%.0fM", m_dXMin + (m_nXLength / 6) * 6));
            }
        }

        tuMultipleSeriesRenderer.setYLabels(7);
        tuMultipleSeriesRenderer.setXTitle(m_strXAxis);
        // tuMultipleSeriesRenderer.setXLabelsAlign(Paint.Align.RIGHT);
        tuMultipleSeriesRenderer.setYLabelsAlign(Paint.Align.RIGHT);
        tuMultipleSeriesRenderer.setYLabelsPadding(5.0f);
        tuMultipleSeriesRenderer.setYTitle("");

        // X,Y축 라인 색상
        tuMultipleSeriesRenderer.setAxesColor(Color.WHITE);
        // 상단제목, X,Y축 제목, 수치값의 글자 색상
        tuMultipleSeriesRenderer.setLabelsColor(Color.CYAN);

        // X,Y축 스크롤 여부 ON/OFF
        tuMultipleSeriesRenderer.setPanEnabled(false, true);
        // ZOOM기능 ON/OFF
        tuMultipleSeriesRenderer.setZoomEnabled(false, true);
        // ZOOM 비율
        tuMultipleSeriesRenderer.setZoomRate(1.0f);
        tuMultipleSeriesRenderer.addSeriesRenderer(tuSeriesRenderer);
        // 그래프 객체 생성
        tuGv = ChartFactory.getLineChartView(getActivity().getBaseContext(), tuMultipleSeriesDataset, tuMultipleSeriesRenderer);

        ll.addView(tuGv);
        //  tuGv.repaint();
    }



    class TuTrendThread extends Thread {
        private boolean isPlay = false;
        public TuTrendThread(boolean isPlay) {this.isPlay = isPlay;}
        public void stopTrendThread(){
            isPlay = !isPlay;
            tuChartClear();
        }
        @Override
        public void run() {
            super.run();
            while (isPlay) {
                try {
                    if (!m_bIsSecond) tuChartAdd();
                    Thread.sleep(m_bThreadTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (m_nTrendFilter == 1 )  arrayTuValue.add(0, m_dTu);
                else if (m_nTrendFilter == 2) arrayTuValue.add(0, m_dAverage[0]);
                else if (m_nTrendFilter == 3) {
                    if (m_bCLOnRange[0]) arrayTuValue.add(0, m_dTu);
                    else    arrayTuValue.add(0, m_dPreTuValue);
                }
                arrayTuValue.remove(m_dXMax-1);
               // arrayTuValue.remove(arrayTuValue.size()-1);
                tuChartAdd();
                mHandler.sendEmptyMessage(1);
            }
        }
    }

    private final Handler mHandler = new Handler() { //핸들러를 통해 UI스레드에 접근한다.
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                DecimalFormat df3Figure = new DecimalFormat("0.000");
                String strValue = df3Figure.format( m_dTu );
                String strPreValue = df3Figure.format(m_dPreTuValue);
                String strAverage = df3Figure.format(m_dAverage[0]);
                if (m_nTrendFilter == 1 )  txTuCurrentValue.setText(strValue);
                else if (m_nTrendFilter == 2) {
                    txTuCurrentValue.setText(strValue);
                    txTuAverageValue.setText(strAverage);
                }
                else if (m_nTrendFilter == 3) {
                    if (m_bCLOnRange[0]) {
                        txTuCurrentValue.setText(strValue);
                        txTuAverageValue.setText("YES");
                        m_dPreTuValue = m_dTu;
                    }
                    else    {
                        txTuCurrentValue.setText(strPreValue);
                        txTuAverageValue.setText("NO");
                    }
                    String strMax = df3Figure.format(m_dCLMax[0]);
                    String strMin = df3Figure.format(m_dCLMin[0]);
                    txTuConfidenceValue.setText(strMin + " ~ " + strMax);
                }

            }
            else if (msg.what == 2) {
                //  txLane2.setText(m_strLane2);
            }
        }
    };


}
