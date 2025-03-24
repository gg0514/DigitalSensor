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

import static com.example.kyj.staqua.MainActivity.arrayCoTrend;
import static com.example.kyj.staqua.MainActivity.m_bCLOnRange;
import static com.example.kyj.staqua.MainActivity.m_dAverage;
import static com.example.kyj.staqua.MainActivity.m_dCLMax;
import static com.example.kyj.staqua.MainActivity.m_dCLMin;
import static com.example.kyj.staqua.MainActivity.m_dCo;
import static com.example.kyj.staqua.MainActivity.m_nTrendFilter;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivityTrendCo extends Fragment {

    // 그래프 그리기
    private static XYMultipleSeriesDataset coMultipleSeriesDataset;
    private static XYMultipleSeriesRenderer coMultipleSeriesRenderer;
    private static XYSeriesRenderer coSeriesRenderer;
    private static XYSeries coSeries;
    private static GraphicalView coGv;
    private static View v;

    private SubActivityTrendCo.CoTrendThread m_CoTrendThread;
    TextView txCoCurrentValue;

    TextView txCoAverageTitle;
    TextView txCoAverageValue;
    TextView txCoConfidenceTitle;
    TextView txCoConfidenceValue;

    double m_dXMax = 600;
    double m_dXMin = 0;
    int m_nXLength = 0;

    int m_nX = 0;
    int m_nIndex = 0;

    // 3600 초가 넘어갈 경우 초단위에서 분단위로 변경 Flag
    boolean m_bIsSecond = TRUE;
    int m_bThreadTime = 1000;

    LinearLayout ll;

    ArrayList<Double> arrayCoValue;
    String m_strXAxis = "";

    double m_dPreCoValue = 0;

    boolean m_bIsHistory = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_trend_co, container, false);

        final Button btnHistory = (Button) v.findViewById(R.id.Trend_Co_Content_Btn_History);
        final Button btnXIn = (Button) v.findViewById(R.id.Trend_Co_X_Btn_In) ;
        final Button btnXOut = (Button) v.findViewById(R.id.Trend_Co_X_Btn_Out) ;
        Button.OnClickListener onClickListener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date d = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String strDate = sdf.format(d);
                switch (view.getId()) {
                    case R.id.Trend_Co_X_Btn_In :
                        m_dXMax= m_dXMax - m_nXLength / 2;
                        if (m_dXMax <= 10) m_dXMax = 10;
                        m_nXLength = (int)m_dXMax - (int)m_dXMin;
                        if (m_CoTrendThread != null) {
                            m_CoTrendThread.stopTrendThread();
                            m_nX = 0;
                        }
                        ll.removeView(coGv);
                        arrayCoValue.clear();
                        showCoChart();
                        coGetFileData(strDate);
                        m_CoTrendThread = new SubActivityTrendCo.CoTrendThread(true);
                        m_CoTrendThread.start();
                        break ;
                    case R.id.Trend_Co_X_Btn_Out :
                        m_dXMax= m_dXMax + m_nXLength;
                        if ((m_bIsSecond) && (m_dXMax > 3600)) m_dXMax = 3600;
                        if ((!m_bIsSecond) && (m_dXMax > 1440)) m_dXMax = 1440;
                        m_nXLength = (int)m_dXMax - (int)m_dXMin;
                        if (m_CoTrendThread != null) {
                            m_CoTrendThread.stopTrendThread();
                            m_nX = 0;
                        }
                        ll.removeView(coGv);
                        arrayCoValue.clear();
                        showCoChart();
                        coGetFileData(strDate);
                        m_CoTrendThread = new SubActivityTrendCo.CoTrendThread(true);
                        m_CoTrendThread.start();
                        break ;
                    case R.id.Trend_Co_Previous_Btn_Left:
                        if (!m_bIsHistory) {
                            m_dXMin = m_dXMin - m_nXLength;
                            if ((m_bIsSecond) && (m_dXMin < 0)) // 초단위에서 0 이하로 이동시 경고화면 후 복귀
                            {
                                m_dXMin = m_dXMin + m_nXLength;
                                Toast.makeText(getActivity(), R.string.multi_x_value_is_less_than, Toast.LENGTH_SHORT).show();
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
                            if (m_CoTrendThread != null) {
                                m_CoTrendThread.stopTrendThread();
                                m_nX = 0;
                            }
                            ll.removeView(coGv);
                            arrayCoValue.clear();
                            showCoChart();
                            coGetFileData(strDate);
                            m_CoTrendThread = new SubActivityTrendCo.CoTrendThread(true);
                            m_CoTrendThread.start();
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
                            ll.removeView(coGv);
                            arrayCoValue.clear();

                            showCoChart();
                            coChartAddHistory();
                        }

                        break ;
                    case R.id.Trend_Co_Previous_Btn_Right:
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
                            if (m_CoTrendThread != null) {
                                m_CoTrendThread.stopTrendThread();
                                m_nX = 0;
                            }
                            ll.removeView(coGv);
                            arrayCoValue.clear();
                            showCoChart();
                            coGetFileData(strDate);
                            m_CoTrendThread = new SubActivityTrendCo.CoTrendThread(true);
                            m_CoTrendThread.start();
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
                            ll.removeView(coGv);
                            arrayCoValue.clear();

                            showCoChart();
                            coChartAddHistory();
                        }
                        break ;
                    case R.id.Trend_Co_Content_Btn_History:

                        m_bIsHistory = true;
                        m_bIsSecond = FALSE;
                        m_dXMin = 0;
                        m_dXMax = 720;
                        m_strXAxis = getResources().getString(R.string.multi_hour_previous);

                        if (m_CoTrendThread != null) {
                            m_CoTrendThread.stopTrendThread();
                            SystemClock.sleep(1000);
                            m_nX = 0;
                        }
                        //   m_PhTrendThread.stopTrendThread();
                        ll.removeView(coGv);
                        arrayCoValue.clear();

                        showCoChart();
                        coChartAddHistory();

                        break;
                }
            }
        } ;
        btnXIn.setOnClickListener(onClickListener) ;
        btnXOut.setOnClickListener(onClickListener) ;
        Button btnLeft = (Button) v.findViewById(R.id.Trend_Co_Previous_Btn_Left) ;
        btnLeft.setOnClickListener(onClickListener) ;
        Button btnRight = (Button) v.findViewById(R.id.Trend_Co_Previous_Btn_Right) ;
        btnRight.setOnClickListener(onClickListener) ;

        btnHistory.setOnClickListener(onClickListener) ;

        txCoCurrentValue = (TextView)v.findViewById(R.id.Trend_Co_Data_Text_Value);
        txCoAverageTitle = (TextView)v.findViewById(R.id.Trend_Co_Data_Average_Title);
        txCoAverageValue = (TextView)v.findViewById(R.id.Trend_Co_Data_Average_Value);
        txCoConfidenceTitle = (TextView)v.findViewById(R.id.Trend_Co_Confidence_Title);
        txCoConfidenceValue = (TextView)v.findViewById(R.id.Trend_Co_Confidence_Value);

        // progressCircle = (ProgressBar)v.findViewById(R.id.Trend_Co_Progress);
        ll = (LinearLayout) v.findViewById(R.id.Trend_Co_Chart);
        arrayCoValue = new ArrayList();
        btnXIn.setVisibility(View.INVISIBLE);
        btnXOut.setVisibility(View.INVISIBLE);
        m_bIsHistory = true;
        m_bIsSecond = FALSE;
        m_dXMin = 0;
        m_dXMax = 720;
        m_strXAxis = getResources().getString(R.string.multi_hour_previous);
        ll.removeView(coGv);
        arrayCoValue.clear();
        showCoChart();
        coChartAddHistory();

        if (m_nTrendFilter == 1) {
            txCoAverageTitle.setVisibility(View.INVISIBLE);
            txCoAverageValue.setVisibility(View.INVISIBLE);
            txCoConfidenceTitle.setVisibility(View.INVISIBLE);
            txCoConfidenceValue.setVisibility(View.INVISIBLE);
        }
        else if (m_nTrendFilter == 2) {
            txCoAverageTitle.setVisibility(View.VISIBLE);
            txCoAverageTitle.setText(R.string.multi_trend_moving_average);
            txCoAverageValue.setVisibility(View.VISIBLE);
            txCoConfidenceTitle.setVisibility(View.INVISIBLE);
            txCoConfidenceValue.setVisibility(View.INVISIBLE);
        }
        else if (m_nTrendFilter == 3) {
            txCoAverageTitle.setVisibility(View.VISIBLE);
            txCoAverageTitle.setText(R.string.multi_trend_on_range);
            txCoAverageValue.setVisibility(View.VISIBLE);
            txCoConfidenceTitle.setVisibility(View.VISIBLE);
            txCoConfidenceValue.setVisibility(View.VISIBLE);
        }

        return v;
    }

    @Override
    public void onStop() {
        if (m_CoTrendThread != null) {
            m_CoTrendThread.stopTrendThread();
        }
        m_nX = 0;
        m_nIndex = 0;
        super.onStop();
    }

    public static SubActivityTrendCo newInstance(String text) {

        SubActivityTrendCo f = new SubActivityTrendCo();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    public  void coGetFileData(String strDate){
        String dirPath = "";
        if (m_bIsSecond == TRUE) dirPath = Environment.getExternalStorageDirectory()+"/STAqua/trend/"+strDate+"/lane5.txt";
        else dirPath = Environment.getExternalStorageDirectory()+"/STAqua/trend/"+strDate+"/lane5_min.txt";
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
                pos --;
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
                    arrayCoValue.add(Double.parseDouble(strValue));
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

    public  void coChartAdd(){
        coChartClear();
        for (int i = 0; i < m_dXMax; i++) {
            if (i >= arrayCoValue.size()) coSeries.add(i,0);
            else coSeries.add(i,arrayCoValue.get(i));
        }
        if (coGv != null){
            coGv.repaint();
        }
        else{
            coSeries.add(0, arrayCoValue.get(0));
            coMultipleSeriesDataset.addSeries(coSeries);
            coGv =  ChartFactory.getLineChartView(getActivity().getBaseContext(), coMultipleSeriesDataset, coMultipleSeriesRenderer);
            ll.addView(coGv);
        }
    }
    public  void coChartAddHistory(){
        coChartClear();
        for (int i = 0; i < m_dXMax; i++) {
            if (i >= arrayCoTrend.size()) coSeries.add(i,0);
            else coSeries.add(i,arrayCoTrend.get(i));
        }
        if (coGv != null){
            coGv.repaint();
        }
        else{
            coSeries.add(0, arrayCoTrend.get(0));
            coMultipleSeriesDataset.addSeries(coSeries);
            coGv =  ChartFactory.getLineChartView(getActivity().getBaseContext(), coMultipleSeriesDataset, coMultipleSeriesRenderer);
            ll.addView(coGv);
        }
    }
    public  void coChartClear(){
        coSeries.clear();
    }


    public  void showCoChart() {

        coMultipleSeriesDataset = new XYMultipleSeriesDataset();
        coMultipleSeriesRenderer = new XYMultipleSeriesRenderer();
        coSeriesRenderer = new XYSeriesRenderer();
        //coSeries = new XYSeries("Conductivity");
        coSeries = new XYSeries(getResources().getString(R.string.multi_co1));

        // Adding Income Series to the dataset
        coMultipleSeriesDataset.addSeries(coSeries);
        // Creating XYSeriesRenderer to customize incomeSeries
        coSeriesRenderer.setColor(Color.GREEN);
        coSeriesRenderer.setPointStyle(PointStyle.POINT);
        coSeriesRenderer.setFillPoints(true);
        coSeriesRenderer.setLineWidth(2);
        coSeriesRenderer.setDisplayChartValues(false);

        int [] nMargin = new int[]{80, 60, 100, 40};
        coMultipleSeriesRenderer.setMargins(nMargin);
        coMultipleSeriesRenderer.setChartTitleTextSize(0);
        // coMultipleSeriesRenderer.setTextTypeface("TEST",2);
        coMultipleSeriesRenderer.setAxisTitleTextSize(30);
        coMultipleSeriesRenderer.setLabelsTextSize(20);
        coMultipleSeriesRenderer.setXAxisMin(m_dXMin);
        coMultipleSeriesRenderer.setXAxisMax(m_dXMax);

        // X축의 개수를 구함
        m_nXLength = (int)m_dXMax - (int)m_dXMin;

        coMultipleSeriesRenderer.setYAxisMin(0);
        coMultipleSeriesRenderer.setYAxisMax(2000);
        coMultipleSeriesRenderer.setLegendTextSize(50);
        coMultipleSeriesRenderer.setLegendHeight(100);

        if (m_bIsHistory) {
            coMultipleSeriesRenderer.setXLabels(0);
            for (int i = 0; i < 6; i++ ) {
                coMultipleSeriesRenderer.addXTextLabel(m_dXMin + (i * 120), String.format("-%.0fH", (m_dXMin + (i * 120))/60));
            }
            coMultipleSeriesRenderer.addXTextLabel(m_dXMin + (6 * 120), String.format("-%.0fH", (m_dXMin + (6 * 120))/60));
        }
        else {
            coMultipleSeriesRenderer.setXLabels(0);
            if (m_bIsSecond) {
                for (int i = 0; i < 6; i++ ) {
                    coMultipleSeriesRenderer.addXTextLabel(m_dXMin + (m_nXLength / 6) * i, String.format("-%.0fS", m_dXMin + (m_nXLength / 6) * i));
                }
                coMultipleSeriesRenderer.addXTextLabel(m_dXMin + (m_nXLength / 6) * 6, String.format("-%.0fS", m_dXMin + (m_nXLength / 6) * 6));
            }
            else {
                for (int i = 0; i < 6; i++ ) {
                    coMultipleSeriesRenderer.addXTextLabel(m_dXMin + (m_nXLength / 6) * i, String.format("-%.0fM", m_dXMin + (m_nXLength / 6) * i));
                }
                coMultipleSeriesRenderer.addXTextLabel(m_dXMin + (m_nXLength / 6) * 6, String.format("-%.0fM", m_dXMin + (m_nXLength / 6) * 6));
            }
        }
        coMultipleSeriesRenderer.setYLabels(7);
        coMultipleSeriesRenderer.setXTitle(m_strXAxis);
        // coMultipleSeriesRenderer.setXLabelsAlign(Paint.Align.RIGHT);
        //coMultipleSeriesRenderer.setYLabelsAlign(Paint.Align.RIGHT);
        coMultipleSeriesRenderer.setYLabelsAlign(Paint.Align.RIGHT);
        coMultipleSeriesRenderer.setYLabelsPadding(5.0f);
        coMultipleSeriesRenderer.setYTitle("");



        // X,Y축 라인 색상
        coMultipleSeriesRenderer.setAxesColor(Color.WHITE);
        // 상단제목, X,Y축 제목, 수치값의 글자 색상
        coMultipleSeriesRenderer.setLabelsColor(Color.CYAN);

        // X,Y축 스크롤 여부 ON/OFF
        coMultipleSeriesRenderer.setPanEnabled(false, true);
        // ZOOM기능 ON/OFF
        coMultipleSeriesRenderer.setZoomEnabled(false, true);
        // ZOOM 비율
        coMultipleSeriesRenderer.setZoomRate(1.0f);
        coMultipleSeriesRenderer.addSeriesRenderer(coSeriesRenderer);
        // 그래프 객체 생성
        coGv = ChartFactory.getLineChartView(getActivity().getBaseContext(), coMultipleSeriesDataset, coMultipleSeriesRenderer);
        ll.addView(coGv);
        //  coGv.repaint();
    }



    class CoTrendThread extends Thread {
        private boolean isPlay = false;
        public CoTrendThread(boolean isPlay) {this.isPlay = isPlay;}
        public void stopTrendThread(){
            isPlay = !isPlay;
            coChartClear();
        }
        @Override
        public void run() {
            super.run();
            while (isPlay) {
                try {
                    if (!m_bIsSecond) coChartAdd();
                    Thread.sleep(m_bThreadTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (m_nTrendFilter == 1 )  arrayCoValue.add(0, m_dCo);
                else if (m_nTrendFilter == 2) arrayCoValue.add(0, m_dAverage[4]);
                else if (m_nTrendFilter == 3) {
                    if (m_bCLOnRange[4]) arrayCoValue.add(0, m_dCo);
                    else    arrayCoValue.add(0, m_dPreCoValue);
                }
                arrayCoValue.remove(m_dXMax-1);
              //  arrayCoValue.remove(arrayCoValue.size()-1);
                coChartAdd();
                mHandler.sendEmptyMessage(1);
            }
        }
    }

    private final Handler mHandler = new Handler() { //핸들러를 통해 UI스레드에 접근한다.
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                DecimalFormat df3Figure = new DecimalFormat("0.0");
                String strValue = df3Figure.format( m_dCo );
                String strPreValue = df3Figure.format(m_dPreCoValue);
                String strAverage = df3Figure.format(m_dAverage[4]);
                if (m_nTrendFilter == 1 )  txCoCurrentValue.setText(strValue);
                else if (m_nTrendFilter == 2) {
                    txCoCurrentValue.setText(strValue);
                    txCoAverageValue.setText(strAverage);
                }
                else if (m_nTrendFilter == 3) {
                    if (m_bCLOnRange[4]) {
                        txCoCurrentValue.setText(strValue);
                        txCoAverageValue.setText("YES");
                        m_dPreCoValue = m_dCo;
                    }
                    else    {
                        txCoCurrentValue.setText(strPreValue);
                        txCoAverageValue.setText("NO");
                    }
                    String strMax = df3Figure.format(m_dCLMax[4]);
                    String strMin = df3Figure.format(m_dCLMin[4]);
                    txCoConfidenceValue.setText(strMin + " ~ " + strMax);
                }
            }
            else if (msg.what == 2) {
                //  txLane2.setText(m_strLane2);
            }
        }
    };


}
