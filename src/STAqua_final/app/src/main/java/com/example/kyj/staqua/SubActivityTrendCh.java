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

import static com.example.kyj.staqua.MainActivity.arrayClTrend;
import static com.example.kyj.staqua.MainActivity.m_bCLOnRange;
import static com.example.kyj.staqua.MainActivity.m_dAverage;
import static com.example.kyj.staqua.MainActivity.m_dCLMax;
import static com.example.kyj.staqua.MainActivity.m_dCLMin;
import static com.example.kyj.staqua.MainActivity.m_dCh;
import static com.example.kyj.staqua.MainActivity.m_nTrendFilter;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivityTrendCh extends Fragment {

    // 그래프 그리기
    private static XYMultipleSeriesDataset chMultipleSeriesDataset;
    private static XYMultipleSeriesRenderer chMultipleSeriesRenderer;
    private static XYSeriesRenderer chSeriesRenderer;
    private static XYSeries chSeries;
    private static GraphicalView chGv;
    private static View v;

    private SubActivityTrendCh.ChTrendThread m_ChTrendThread;
    TextView txChCurrentValue;
    TextView txChAverageTitle;
    TextView txChAverageValue;
    TextView txChConfidenceTitle;
    TextView txChConfidenceValue;

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

    ArrayList<Double> arrayChValue;

    String m_strXAxis = "";
    double m_dPreChValue = 0;

    boolean m_bIsHistory = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_trend_ch, container, false);

        final Button btnHistory = (Button) v.findViewById(R.id.Trend_Ch_Content_Btn_History);
        final Button btnXIn = (Button) v.findViewById(R.id.Trend_Ch_X_Btn_In) ;
        final Button btnXOut = (Button) v.findViewById(R.id.Trend_Ch_X_Btn_Out) ;

        Button.OnClickListener onClickListener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date d = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String strDate = sdf.format(d);
                switch (view.getId()) {
                    case R.id.Trend_Ch_X_Btn_In :
                        m_dXMax= m_dXMax - m_nXLength / 2;
                        if (m_dXMax <= 10) m_dXMax = 10;
                        if (m_ChTrendThread != null) {
                            m_ChTrendThread.stopTrendThread();
                            m_nX = 0;
                        }
                        ll.removeView(chGv);
                        arrayChValue.clear();
                        showChChart();
                        chGetFileData(strDate);
                        m_ChTrendThread = new SubActivityTrendCh.ChTrendThread(true);
                        m_ChTrendThread.start();
                        break ;
                    case R.id.Trend_Ch_X_Btn_Out :
                        m_dXMax= m_dXMax + m_nXLength;
                        if ((m_bIsSecond) && (m_dXMax > 3600)) m_dXMax = 3600;
                        if ((!m_bIsSecond) && (m_dXMax > 1440)) m_dXMax = 1440;
                        if (m_ChTrendThread != null) {
                            m_ChTrendThread.stopTrendThread();
                            m_nX = 0;
                        }
                        ll.removeView(chGv);
                        arrayChValue.clear();
                        showChChart();
                        chGetFileData(strDate);
                        m_ChTrendThread = new SubActivityTrendCh.ChTrendThread(true);
                        m_ChTrendThread.start();
                        break ;
                    case R.id.Trend_Ch_Previous_Btn_Left:
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

                            if (m_ChTrendThread != null) {
                                m_ChTrendThread.stopTrendThread();
                                m_nX = 0;
                            }
                            ll.removeView(chGv);
                            arrayChValue.clear();
                            showChChart();
                            chGetFileData(strDate);
                            m_ChTrendThread = new SubActivityTrendCh.ChTrendThread(true);
                            m_ChTrendThread.start();
                        }
                        else if (m_bIsHistory) {
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
                            ll.removeView(chGv);
                            arrayChValue.clear();

                            showChChart();
                            chChartAddHistory();
                        }

                        break ;
                    case R.id.Trend_Ch_Previous_Btn_Right:
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

                            if (m_ChTrendThread != null) {
                                m_ChTrendThread.stopTrendThread();
                                m_nX = 0;
                            }
                            ll.removeView(chGv);
                            arrayChValue.clear();
                            showChChart();
                            chGetFileData(strDate);
                            m_ChTrendThread = new SubActivityTrendCh.ChTrendThread(true);
                            m_ChTrendThread.start();
                        }
                        else if (m_bIsHistory) {
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
                            ll.removeView(chGv);
                            arrayChValue.clear();

                            showChChart();
                            chChartAddHistory();
                        }
                        break ;
                    case R.id.Trend_Ch_Content_Btn_History:
                        // 히스토리 선택시

                        m_bIsHistory = true;
                        m_bIsSecond = FALSE;
                        m_dXMin = 0;
                        m_dXMax = 720;
                        m_strXAxis = getResources().getString(R.string.multi_hour_previous);

                        if (m_ChTrendThread != null) {
                            m_ChTrendThread.stopTrendThread();
                            SystemClock.sleep(1000);
                            m_nX = 0;
                        }
                        //   m_PhTrendThread.stopTrendThread();
                        ll.removeView(chGv);
                        arrayChValue.clear();

                        showChChart();
                        chChartAddHistory();

                        break;
                }
            }
        } ;

        btnXIn.setOnClickListener(onClickListener) ;
        btnXOut.setOnClickListener(onClickListener) ;
        Button btnLeft = (Button) v.findViewById(R.id.Trend_Ch_Previous_Btn_Left) ;
        btnLeft.setOnClickListener(onClickListener) ;
        Button btnRight = (Button) v.findViewById(R.id.Trend_Ch_Previous_Btn_Right) ;
        btnRight.setOnClickListener(onClickListener) ;

        btnHistory.setOnClickListener(onClickListener) ;

        txChCurrentValue = (TextView)v.findViewById(R.id.Trend_Ch_Data_Text_Value);
        txChAverageTitle = (TextView)v.findViewById(R.id.Trend_Ch_Data_Average_Title);
        txChAverageValue = (TextView)v.findViewById(R.id.Trend_Ch_Data_Average_Value);
        txChConfidenceTitle = (TextView)v.findViewById(R.id.Trend_Ch_Confidence_Title);
        txChConfidenceValue = (TextView)v.findViewById(R.id.Trend_Ch_Confidence_Value);

       // progressCircle = (ProgressBar)v.findViewById(R.id.Trend_Ch_Progress);
        ll = (LinearLayout) v.findViewById(R.id.Trend_Ch_Chart);

        m_strXAxis = getResources().getString(R.string.multi_second_previous);

        arrayChValue = new ArrayList();
        btnXIn.setVisibility(View.INVISIBLE);
        btnXOut.setVisibility(View.INVISIBLE);
        m_bIsHistory = true;
        m_bIsSecond = FALSE;
        m_dXMin = 0;
        m_dXMax = 720;
        m_strXAxis = getResources().getString(R.string.multi_hour_previous);
        ll.removeView(chGv);
        arrayChValue.clear();
        showChChart();
        chChartAddHistory();


        if (m_nTrendFilter == 1) {
            txChAverageTitle.setVisibility(View.INVISIBLE);
            txChAverageValue.setVisibility(View.INVISIBLE);
            txChConfidenceTitle.setVisibility(View.INVISIBLE);
            txChConfidenceValue.setVisibility(View.INVISIBLE);
        }
        else if (m_nTrendFilter == 2) {
            txChAverageTitle.setVisibility(View.VISIBLE);
            txChAverageTitle.setText(R.string.multi_trend_moving_average);
            txChAverageValue.setVisibility(View.VISIBLE);
            txChConfidenceTitle.setVisibility(View.INVISIBLE);
            txChConfidenceValue.setVisibility(View.INVISIBLE);
        }
        else if (m_nTrendFilter == 3) {
            txChAverageTitle.setVisibility(View.VISIBLE);
            txChAverageTitle.setText(R.string.multi_trend_on_range);
            txChAverageValue.setVisibility(View.VISIBLE);
            txChConfidenceTitle.setVisibility(View.VISIBLE);
            txChConfidenceValue.setVisibility(View.VISIBLE);
        }
        return v;
    }

    @Override
    public void onStop() {
        if (m_ChTrendThread != null) {
            m_ChTrendThread.stopTrendThread();
        }
        m_nX = 0;
        m_nIndex = 0;
        super.onStop();
    }

    public static SubActivityTrendCh newInstance(String text) {

        SubActivityTrendCh f = new SubActivityTrendCh();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    public  void chGetFileData(String strDate){
        String dirPath = "";
        if (m_bIsSecond == TRUE) dirPath = Environment.getExternalStorageDirectory()+"/STAqua/trend/"+strDate+"/lane2.txt";
        else dirPath = Environment.getExternalStorageDirectory()+"/STAqua/trend/"+strDate+"/lane2_min.txt";
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
                    arrayChValue.add(Double.parseDouble(strValue));
                    pos = pos - nIndexEnd - 2;
                    i++;
                }
                else pos--;
            }

//
//            while ((i < m_dXMax ) && (pos > 0))
//            {
//                file.seek(pos+1);
//                String strTemp = file.readLine();
//                StringTokenizer tokens = new StringTokenizer(strTemp);
//                String strTime = tokens.nextToken("|");
//                String strValue = tokens.nextToken("|");
//                arrayChValue.add(Double.parseDouble(strValue));
//                pos = pos - strTemp.length() - 1;
//                i++;
//            }
            file.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public  void chChartAdd(){
        chChartClear();
        for (int i = 0; i < m_dXMax; i++) {
            if (i >= arrayChValue.size()) chSeries.add(i,0);
            else chSeries.add(i,arrayChValue.get(i));
        }
        if (chGv != null){
            chGv.repaint();
        }
        else{
            chSeries.add(0, arrayChValue.get(0));
            chMultipleSeriesDataset.addSeries(chSeries);
            chGv =  ChartFactory.getLineChartView(getActivity().getBaseContext(), chMultipleSeriesDataset, chMultipleSeriesRenderer);
            ll.addView(chGv);
        }
    }

    public  void chChartAddHistory(){
        chChartClear();
        for (int i = 0; i < m_dXMax; i++) {
            if (i >= arrayClTrend.size()) chSeries.add(i,0);
            else chSeries.add(i,arrayClTrend.get(i));
        }
        if (chGv != null){
            chGv.repaint();
        }
        else{
            chSeries.add(0, arrayClTrend.get(0));
            chMultipleSeriesDataset.addSeries(chSeries);
            chGv =  ChartFactory.getLineChartView(getActivity().getBaseContext(), chMultipleSeriesDataset, chMultipleSeriesRenderer);
            ll.addView(chGv);
        }
    }

    public  void chChartClear(){
        chSeries.clear();
    }


    public  void showChChart() {

        chMultipleSeriesDataset = new XYMultipleSeriesDataset();
        chMultipleSeriesRenderer = new XYMultipleSeriesRenderer();
        chSeriesRenderer = new XYSeriesRenderer();
       // chSeries = new XYSeries("Chlorine");
        chSeries = new XYSeries(getResources().getString(R.string.multi_ch1));

        // Adding Income Series to the dataset
        chMultipleSeriesDataset.addSeries(chSeries);
        // Creating XYSeriesRenderer to customize incomeSeries
        chSeriesRenderer.setColor(Color.CYAN);
        chSeriesRenderer.setPointStyle(PointStyle.POINT);
        chSeriesRenderer.setFillPoints(true);
        chSeriesRenderer.setLineWidth(2);
        chSeriesRenderer.setDisplayChartValues(false);

        int [] nMargin = new int[]{80, 40, 100, 40};
        chMultipleSeriesRenderer.setMargins(nMargin);
        chMultipleSeriesRenderer.setChartTitleTextSize(0);
        // chMultipleSeriesRenderer.setTextTypeface("TEST",2);
        chMultipleSeriesRenderer.setAxisTitleTextSize(30);
        chMultipleSeriesRenderer.setLabelsTextSize(20);
        chMultipleSeriesRenderer.setXAxisMin(m_dXMin);
        chMultipleSeriesRenderer.setXAxisMax(m_dXMax);

        // X축의 개수를 구함
        m_nXLength = (int)m_dXMax - (int)m_dXMin;

        chMultipleSeriesRenderer.setYAxisMin(0.00);
        chMultipleSeriesRenderer.setYAxisMax(2.00);
        chMultipleSeriesRenderer.setLegendTextSize(50);
        chMultipleSeriesRenderer.setLegendHeight(100);

        if (m_bIsHistory) {
            chMultipleSeriesRenderer.setXLabels(0);
            for (int i = 0; i < 6; i++ ) {
                chMultipleSeriesRenderer.addXTextLabel(m_dXMin + (i * 120), String.format("-%.0fH", (m_dXMin + (i * 120))/60));
            }
            chMultipleSeriesRenderer.addXTextLabel(m_dXMin + (6 * 120), String.format("-%.0fH", (m_dXMin + (6 * 120))/60));
        }
        else {
            chMultipleSeriesRenderer.setXLabels(0);
            if (m_bIsSecond) {
                for (int i = 0; i < 6; i++ ) {
                    chMultipleSeriesRenderer.addXTextLabel(m_dXMin + (m_nXLength / 6) * i, String.format("-%.0fS", m_dXMin + (m_nXLength / 6) * i));
                }
                chMultipleSeriesRenderer.addXTextLabel(m_dXMin + (m_nXLength / 6) * 6, String.format("-%.0fS", m_dXMin + (m_nXLength / 6) * 6));
            }
            else {
                for (int i = 0; i < 6; i++ ) {
                    chMultipleSeriesRenderer.addXTextLabel(m_dXMin + (m_nXLength / 6) * i, String.format("-%.0fM", m_dXMin + (m_nXLength / 6) * i));
                }
                chMultipleSeriesRenderer.addXTextLabel(m_dXMin + (m_nXLength / 6) * 6, String.format("-%.0fM", m_dXMin + (m_nXLength / 6) * 6));
            }
        }

        chMultipleSeriesRenderer.setYLabels(7);
        chMultipleSeriesRenderer.setXTitle(m_strXAxis);
        // chMultipleSeriesRenderer.setXLabelsAlign(Paint.Align.RIGHT);
        chMultipleSeriesRenderer.setYLabelsAlign(Paint.Align.RIGHT);
        chMultipleSeriesRenderer.setYLabelsPadding(5.0f);
        chMultipleSeriesRenderer.setYTitle("");


        // X,Y축 라인 색상
        chMultipleSeriesRenderer.setAxesColor(Color.WHITE);
        // 상단제목, X,Y축 제목, 수치값의 글자 색상
        chMultipleSeriesRenderer.setLabelsColor(Color.CYAN);

        // X,Y축 스크롤 여부 ON/OFF
        chMultipleSeriesRenderer.setPanEnabled(false, true);
        // ZOOM기능 ON/OFF
        chMultipleSeriesRenderer.setZoomEnabled(false, true);
        // ZOOM 비율
        chMultipleSeriesRenderer.setZoomRate(1.0f);
        chMultipleSeriesRenderer.addSeriesRenderer(chSeriesRenderer);
        // 그래프 객체 생성
        chGv = ChartFactory.getLineChartView(getActivity().getBaseContext(), chMultipleSeriesDataset, chMultipleSeriesRenderer);
        ll.addView(chGv);
        //  chGv.repaint();
    }



    class ChTrendThread extends Thread {
        private boolean isPlay = false;
        public ChTrendThread(boolean isPlay) {this.isPlay = isPlay;}
        public void stopTrendThread(){
            isPlay = !isPlay;
            chChartClear();
        }
        @Override
        public void run() {
            super.run();
            while (isPlay) {
                try {
                    if (!m_bIsSecond) chChartAdd();
                    Thread.sleep(m_bThreadTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (m_nTrendFilter == 1 )  arrayChValue.add(0, m_dCh);
                else if (m_nTrendFilter == 2) arrayChValue.add(0, m_dAverage[1]);
                else if (m_nTrendFilter == 3) {
                    if (m_bCLOnRange[1]) arrayChValue.add(0, m_dCh);
                    else    arrayChValue.add(0, m_dPreChValue);
                }
                arrayChValue.remove(m_dXMax-1);
              //  arrayChValue.remove(arrayChValue.size()-1);
                chChartAdd();
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
                String strValue = df3Figure.format( m_dCh );
                String strPreValue = df3Figure.format(m_dPreChValue);
                String strAverage = df3Figure.format(m_dAverage[1]);
                if (m_nTrendFilter == 1 )  txChCurrentValue.setText(strValue);
                else if (m_nTrendFilter == 2) {
                    txChCurrentValue.setText(strValue);
                    txChAverageValue.setText(strAverage);
                }
                else if (m_nTrendFilter == 3) {
                    if (m_bCLOnRange[1]) {
                        txChCurrentValue.setText(strValue);
                        txChAverageValue.setText("YES");
                        m_dPreChValue = m_dCh;
                    }
                    else    {
                        txChCurrentValue.setText(strPreValue);
                        txChAverageValue.setText("NO");
                    }
                    String strMax = df3Figure.format(m_dCLMax[1]);
                    String strMin = df3Figure.format(m_dCLMin[1]);
                    txChConfidenceValue.setText(strMin + " ~ " + strMax);
                }
            }
            else if (msg.what == 2) {
                //  txLane2.setText(m_strLane2);
            }
        }
    };


}
