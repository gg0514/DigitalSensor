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

import static com.example.kyj.staqua.MainActivity.arrayPhTrend;
import static com.example.kyj.staqua.MainActivity.m_bCLOnRange;
import static com.example.kyj.staqua.MainActivity.m_dAverage;
import static com.example.kyj.staqua.MainActivity.m_dCLMax;
import static com.example.kyj.staqua.MainActivity.m_dCLMin;
import static com.example.kyj.staqua.MainActivity.m_dPh;
import static com.example.kyj.staqua.MainActivity.m_nTrendFilter;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Double.parseDouble;

/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivityTrendPh extends Fragment {

    // 그래프 그리기
    private static XYMultipleSeriesDataset phMultipleSeriesDataset;
    private static XYMultipleSeriesRenderer phMultipleSeriesRenderer;
    private static XYSeriesRenderer phSeriesRenderer;
    private static XYSeries phSeries;
    private static GraphicalView phGv;
    private static View v;

    private  PhTrendThread m_PhTrendThread;

    TextView   txPhCurrentValue;
    TextView txPhAverageTitle;
    TextView txPhAverageValue;
    TextView txPhConfidenceTitle;
    TextView txPhConfidenceValue;

    TextView txPhXTextTitle;

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

    ArrayList<Double> arrayPhValue;

    String m_strXAxis = "";
    double m_dPrePhValue = 0;

    boolean m_bIsHistory ;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.sub_activity_trend_ph, container, false);

        final Button  btnHistory = (Button) v.findViewById(R.id.Trend_Ph_Content_Btn_History);
        final Button  btnXIn = (Button) v.findViewById(R.id.Trend_Ph_X_Btn_In) ;
        final Button  btnXOut = (Button) v.findViewById(R.id.Trend_Ph_X_Btn_Out) ;

        Button.OnClickListener onClickListener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date d = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String strDate = sdf.format(d);
                switch (view.getId()) {
                    case R.id.Trend_Ph_X_Btn_In :
                        m_dXMax= m_dXMax - m_nXLength / 2;
                        if (m_dXMax <= 10) m_dXMax = 10;
                        if (m_PhTrendThread != null) {
                            m_PhTrendThread.stopTrendThread();
                            m_nX = 0;
                        }
                        ll.removeView(phGv);
                        arrayPhValue.clear();
                        showPhChart();
                        phGetFileData(strDate);
                        m_PhTrendThread = new PhTrendThread(true);
                        m_PhTrendThread.start();
                        break ;
                    case R.id.Trend_Ph_X_Btn_Out :
                        m_dXMax= m_dXMax + m_nXLength;
                        if ((m_bIsSecond) && (m_dXMax > 3600)) m_dXMax = 3600;
                        if ((!m_bIsSecond) && (m_dXMax > 1440)) m_dXMax = 1440;
                        if (m_PhTrendThread != null) {
                            m_PhTrendThread.stopTrendThread();
                            m_nX = 0;
                        }
                        ll.removeView(phGv);
                        arrayPhValue.clear();
                        showPhChart();
                        phGetFileData(strDate);
                        m_PhTrendThread = new PhTrendThread(true);
                        m_PhTrendThread.start();
                        break ;
                    case R.id.Trend_Ph_Previous_Btn_Left:
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
                            if (m_PhTrendThread != null) {
                                m_PhTrendThread.stopTrendThread();
                                m_nX = 0;
                            }
                            ll.removeView(phGv);
                            arrayPhValue.clear();
                            showPhChart();
                            phGetFileData(strDate);
                            m_PhTrendThread = new PhTrendThread(true);
                            m_PhTrendThread.start();
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
                            ll.removeView(phGv);
                            arrayPhValue.clear();

                            showPhChart();
                            phChartAddHistory();
                        }
                        break ;
                    case R.id.Trend_Ph_Previous_Btn_Right:
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
                            if (m_PhTrendThread != null) {
                                m_PhTrendThread.stopTrendThread();
                                m_nX = 0;
                            }
                            ll.removeView(phGv);
                            arrayPhValue.clear();
                            showPhChart();
                            phGetFileData(strDate);
                            m_PhTrendThread = new PhTrendThread(true);
                            m_PhTrendThread.start();
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
                            ll.removeView(phGv);
                            arrayPhValue.clear();

                            showPhChart();
                            phChartAddHistory();
                        }
                        break;
                    case R.id.Trend_Ph_Content_Btn_History:
                        m_bIsHistory = true;
                        m_bIsSecond = FALSE;
                        m_dXMin = 0;
                        m_dXMax = 720;
                        m_strXAxis = getResources().getString(R.string.multi_hour_previous);

                        if (m_PhTrendThread != null) {
                            m_PhTrendThread.stopTrendThread();
                            SystemClock.sleep(1000);
                            m_nX = 0;
                        }
                        //   m_PhTrendThread.stopTrendThread();
                        ll.removeView(phGv);
                        arrayPhValue.clear();

                        showPhChart();
                        phChartAddHistory();



                        // 히스토리 선택시
//                        if (!m_bIsHistory) {
//                            btnHistory.setText(R.string.multi_realtime);
//                            txPhCurrentValue.setText("");
//                            btnXIn.setVisibility(View.INVISIBLE);
//                            btnXOut.setVisibility(View.INVISIBLE);
//                            textXTitle.setVisibility(View.INVISIBLE);
//
//                            m_bIsHistory = true;
//                            m_bIsSecond = FALSE;
//                            m_dXMin = 0;
//                            m_dXMax = 720;
//                            m_strXAxis = getResources().getString(R.string.multi_hour_previous);
//
//                            if (m_PhTrendThread != null) {
//                                m_PhTrendThread.stopTrendThread();
//                                SystemClock.sleep(1000);
//                                m_nX = 0;
//                            }
//                            //   m_PhTrendThread.stopTrendThread();
//                            ll.removeView(phGv);
//                            arrayPhValue.clear();
//
//                            showPhChart();
//                            phChartAddHistory();
//
//                        }
//                        // 실시간 선택시
//                        else if (m_bIsHistory) {
//                            btnHistory.setText(R.string.multi_history);
//                            btnXIn.setVisibility(View.VISIBLE);
//                            btnXOut.setVisibility(View.VISIBLE);
//                            m_bIsHistory = false;
//
//                            m_bIsSecond = true;
//                            m_bThreadTime = 1000;
//                            m_dXMin = 0;
//                            m_dXMax = 600;
//                            m_strXAxis = getResources().getString(R.string.multi_second_previous);
//
//                            if (m_PhTrendThread != null) {
//                                m_PhTrendThread.stopTrendThread();
//                                m_nX = 0;
//                            }
//                            ll.removeView(phGv);
//                            arrayPhValue.clear();
//                            showPhChart();
//                            phGetFileData(strDate);
//                            m_PhTrendThread = new PhTrendThread(true);
//                            m_PhTrendThread.start();
//                        }
                        break;
                }
            }
        } ;

        btnXIn.setOnClickListener(onClickListener) ;
        btnXOut.setOnClickListener(onClickListener) ;
        Button btnLeft = (Button) v.findViewById(R.id.Trend_Ph_Previous_Btn_Left) ;
        btnLeft.setOnClickListener(onClickListener) ;
        Button btnRight = (Button) v.findViewById(R.id.Trend_Ph_Previous_Btn_Right) ;
        btnRight.setOnClickListener(onClickListener) ;
        btnHistory.setOnClickListener(onClickListener) ;

        txPhCurrentValue = (TextView)v.findViewById(R.id.Trend_Ph_Data_Text_Value);
        txPhAverageTitle = (TextView)v.findViewById(R.id.Trend_Ph_Data_Average_Title);
        txPhAverageValue = (TextView)v.findViewById(R.id.Trend_Ph_Data_Average_Value);
        txPhConfidenceTitle = (TextView)v.findViewById(R.id.Trend_Ph_Confidence_Title);
        txPhConfidenceValue = (TextView)v.findViewById(R.id.Trend_Ph_Confidence_Value);

        progressCircle = (ProgressBar)v.findViewById(R.id.Trend_Ph_Progress);
        ll = (LinearLayout) v.findViewById(R.id.Trend_Ph_Chart);
        arrayPhValue = new ArrayList();

//        m_strXAxis = getResources().getString(R.string.multi_second_previous);

//        showPhChart();
//        Date d = new Date();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//        String strDate = sdf.format(d);
//        phGetFileData(strDate);
//        m_PhTrendThread = new PhTrendThread(true);
//        m_PhTrendThread.start();

        btnXIn.setVisibility(View.INVISIBLE);
        btnXOut.setVisibility(View.INVISIBLE);
        m_bIsHistory = true;
        m_bIsSecond = FALSE;
        m_dXMin = 0;
        m_dXMax = 720;
        m_strXAxis = getResources().getString(R.string.multi_hour_previous);
        ll.removeView(phGv);
        arrayPhValue.clear();
        showPhChart();
        phChartAddHistory();

        if (m_nTrendFilter == 1) {
            txPhAverageTitle.setVisibility(View.INVISIBLE);
            txPhAverageValue.setVisibility(View.INVISIBLE);
            txPhConfidenceTitle.setVisibility(View.INVISIBLE);
            txPhConfidenceValue.setVisibility(View.INVISIBLE);
        }
        else if (m_nTrendFilter == 2) {
            txPhAverageTitle.setVisibility(View.VISIBLE);
            txPhAverageTitle.setText(R.string.multi_trend_moving_average);
            txPhAverageValue.setVisibility(View.VISIBLE);
            txPhConfidenceTitle.setVisibility(View.INVISIBLE);
            txPhConfidenceValue.setVisibility(View.INVISIBLE);
        }
        else if (m_nTrendFilter == 3) {
            txPhAverageTitle.setVisibility(View.VISIBLE);
            txPhAverageTitle.setText(R.string.multi_trend_on_range);
            txPhAverageValue.setVisibility(View.VISIBLE);
            txPhConfidenceTitle.setVisibility(View.VISIBLE);
            txPhConfidenceValue.setVisibility(View.VISIBLE);
        }
        return v;
    }


    @Override
    public void onStop() {
        if (m_PhTrendThread != null) {
            m_PhTrendThread.stopTrendThread();
        }
        m_nX = 0;
        m_nIndex = 0;
        super.onStop();
    }


//    @Override
//    public void onDestroy() {
//        // mbWriteData = false;
//        if (m_PhTrendThread != null) {
//            m_PhTrendThread.stopThread();
//        }
//        m_nX = 0;
//
//        super.onDestroy();
//    }

    public static SubActivityTrendPh newInstance(String text) {
        SubActivityTrendPh f = new SubActivityTrendPh();
        Bundle b = new Bundle();
        b.putString("msg", text);
        f.setArguments(b);
        return f;
    }

    // 파일에 있는 데이터를 가져오는 함수
    public  void phGetFileData(String strDate){
        String dirPath = "";
        if (m_bIsSecond == TRUE) dirPath = Environment.getExternalStorageDirectory()+"/STAqua/trend/"+strDate+"/lane3.txt";
        else dirPath = Environment.getExternalStorageDirectory()+"/STAqua/trend/"+strDate+"/lane3_min.txt";
        try{
            RandomAccessFile file = new RandomAccessFile(dirPath, "r");
            long fileSize = file.length();
            long pos = fileSize - 2;
            int i = 0;
            byte[] buff = new byte [20];

            while (true) {
                file.seek(pos);
          //      byte bText = file.readByte();
                if (file.readByte() == 0xA)
                {
                    break;
                }
                else  pos --;
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
                    arrayPhValue.add(parseDouble(strValue));
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


    public  void phChartAdd(){
        phChartClear();
        for (int i = 0; i < m_dXMax; i++) {
            if (i >= arrayPhValue.size()) phSeries.add(i,0);
            else phSeries.add(i,arrayPhValue.get(i));
        }
        if (phGv != null){
            phGv.repaint();
        }
        else{
            phSeries.add(0, arrayPhValue.get(0));
            phMultipleSeriesDataset.addSeries(phSeries);
            phGv =  ChartFactory.getLineChartView(getActivity().getBaseContext(), phMultipleSeriesDataset, phMultipleSeriesRenderer);
            ll.addView(phGv);
        }
    }

    public  void phChartAddHistory(){
        phChartClear();
        for (int i = 0; i < m_dXMax; i++) {
            if (i >= arrayPhTrend.size()) phSeries.add(i,0);
            else phSeries.add(i,arrayPhTrend.get(i));
        }
        if (phGv != null){
            phGv.repaint();
        }
        else{
            phSeries.add(0, arrayPhTrend.get(0));
            phMultipleSeriesDataset.addSeries(phSeries);
            phGv =  ChartFactory.getLineChartView(getActivity().getBaseContext(), phMultipleSeriesDataset, phMultipleSeriesRenderer);
            ll.addView(phGv);
        }
    }

    public  void phChartClear(){
        phSeries.clear();
    }


    public  void showPhChart() {

        phMultipleSeriesDataset = new XYMultipleSeriesDataset();
        phMultipleSeriesRenderer = new XYMultipleSeriesRenderer();
        phSeriesRenderer = new XYSeriesRenderer();
        phSeries = new XYSeries(getResources().getString(R.string.multi_ph1));

        // Adding Income Series to the dataset
        phMultipleSeriesDataset.addSeries(phSeries);
        // Creating XYSeriesRenderer to customize incomeSeries
        phSeriesRenderer.setColor(Color.RED);
        phSeriesRenderer.setPointStyle(PointStyle.POINT);
        phSeriesRenderer.setFillPoints(true);
        phSeriesRenderer.setLineWidth(2);
        phSeriesRenderer.setDisplayChartValues(false);

        int [] nMargin = new int[]{80, 40, 100, 40};
        phMultipleSeriesRenderer.setMargins(nMargin);
        phMultipleSeriesRenderer.setChartTitleTextSize(0);
       // phMultipleSeriesRenderer.setTextTypeface("TEST",2);
        phMultipleSeriesRenderer.setAxisTitleTextSize(30);
        phMultipleSeriesRenderer.setLabelsTextSize(20);
        phMultipleSeriesRenderer.setXAxisMin(m_dXMin);
        phMultipleSeriesRenderer.setXAxisMax(m_dXMax);

        // X축의 개수를 구함
        m_nXLength = (int)m_dXMax - (int)m_dXMin;

        phMultipleSeriesRenderer.setYAxisMin(0);
        phMultipleSeriesRenderer.setYAxisMax(14);
        phMultipleSeriesRenderer.setLegendTextSize(50);
        phMultipleSeriesRenderer.setLegendHeight(100);
        if (m_bIsHistory) {
            phMultipleSeriesRenderer.setXLabels(0);
            for (int i = 0; i < 6; i++ ) {
                phMultipleSeriesRenderer.addXTextLabel(m_dXMin + (i * 120), String.format("-%.0fH", (m_dXMin + (i * 120))/60));
            }
            phMultipleSeriesRenderer.addXTextLabel(m_dXMin + (6 * 120), String.format("-%.0fH", (m_dXMin + (6 * 120))/60));
        }
        else {
            phMultipleSeriesRenderer.setXLabels(0);
            if (m_bIsSecond) {
                for (int i = 0; i < 6; i++ ) {
                    phMultipleSeriesRenderer.addXTextLabel(m_dXMin + (m_nXLength / 6) * i, String.format("-%.0fS", m_dXMin + (m_nXLength / 6) * i));
                }
                phMultipleSeriesRenderer.addXTextLabel(m_dXMin + (m_nXLength / 6) * 6, String.format("-%.0fS", m_dXMin + (m_nXLength / 6) * 6));
            }
            else {
                for (int i = 0; i < 6; i++ ) {
                    phMultipleSeriesRenderer.addXTextLabel(m_dXMin + (m_nXLength / 6) * i, String.format("-%.0fM", m_dXMin + (m_nXLength / 6) * i));
                }
                phMultipleSeriesRenderer.addXTextLabel(m_dXMin + (m_nXLength / 6) * 6, String.format("-%.0fM", m_dXMin + (m_nXLength / 6) * 6));
            }
        }
        phMultipleSeriesRenderer.setYLabels(7);
        phMultipleSeriesRenderer.setXTitle(m_strXAxis);
       // phMultipleSeriesRenderer.setXLabelsAlign(Paint.Align.RIGHT);
        phMultipleSeriesRenderer.setYLabelsAlign(Paint.Align.RIGHT);
        phMultipleSeriesRenderer.setYLabelsPadding(5.0f);
        phMultipleSeriesRenderer.setYTitle("");




        // X,Y축 라인 색상
        phMultipleSeriesRenderer.setAxesColor(Color.WHITE);
        // 상단제목, X,Y축 제목, 수치값의 글자 색상
        phMultipleSeriesRenderer.setLabelsColor(Color.CYAN);

        // X,Y축 스크롤 여부 ON/OFF
         phMultipleSeriesRenderer.setPanEnabled(false, true);
        // ZOOM기능 ON/OFF
     //   if (m_bIsHistory) phMultipleSeriesRenderer.setZoomEnabled(true, true);
        phMultipleSeriesRenderer.setZoomEnabled(false, true);

        // ZOOM 비율
        phMultipleSeriesRenderer.setZoomRate(1.0f);
        phMultipleSeriesRenderer.addSeriesRenderer(phSeriesRenderer);
        // 그래프 객체 생성
        phGv = ChartFactory.getLineChartView(getActivity().getBaseContext(), phMultipleSeriesDataset, phMultipleSeriesRenderer);



        ll.addView(phGv);
      //  phGv.repaint();
    }



    class PhTrendThread extends Thread {
        private boolean isPlay = false;
        public PhTrendThread(boolean isPlay) {this.isPlay = isPlay;}
        public void stopTrendThread(){
            isPlay = !isPlay;
            phChartClear();
        }
        @Override
        public void run() {
            super.run();
            while (isPlay) {
                try {
               //     if (m_bIsHistory) return;
                    if (!m_bIsSecond) phChartAdd();
                    Thread.sleep(m_bThreadTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (m_nTrendFilter == 1 )  arrayPhValue.add(0, m_dPh);
                else if (m_nTrendFilter == 2) arrayPhValue.add(0, m_dAverage[2]);
                else if (m_nTrendFilter == 3) {
                    if (m_bCLOnRange[2]) arrayPhValue.add(0, m_dPh);
                    else    arrayPhValue.add(0, m_dPrePhValue);
                }
               // arrayPhValue.remove(arrayPhValue.size()-1);
                arrayPhValue.remove(m_dXMax-1);
                phChartAdd();
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
                String strValue = df3Figure.format( m_dPh );
                String strPreValue = df3Figure.format(m_dPrePhValue);
                String strAverage = df3Figure.format(m_dAverage[2]);
                if (m_nTrendFilter == 1 )  txPhCurrentValue.setText(strValue);
                else if (m_nTrendFilter == 2) {
                    txPhCurrentValue.setText(strValue);
                    txPhAverageValue.setText(strAverage);
                }
                else if (m_nTrendFilter == 3) {
                    if (m_bCLOnRange[2]) {
                        txPhCurrentValue.setText(strValue);
                        txPhAverageValue.setText("YES");
                        m_dPrePhValue = m_dPh;
                    }
                    else    {
                        txPhCurrentValue.setText(strPreValue);
                        txPhAverageValue.setText("NO");
                    }
                    String strMax = df3Figure.format(m_dCLMax[2]);
                    String strMin = df3Figure.format(m_dCLMin[2]);
                    txPhConfidenceValue.setText(strMin + " ~ " + strMax);
                }
            }
            else if (msg.what == 2) {
              //  txLane2.setText(m_strLane2);
            }
        }
    };
}





//            BufferedReader reader  = new BufferedReader(new FileReader(dirPath));
//            m_dValue = new double[m_nXLength];
//            // 파일안 문자열 읽기
//            int i = 0;
//            String strTemp = "";
//            while((strTemp = reader.readLine()) != null)
//            {
//                if (i > m_nXLength) break;
//                else{
//                    StringTokenizer tokens = new StringTokenizer(strTemp);
//                    String strTime = tokens.nextToken("|");
//                    String strValue = tokens.nextToken("|");
//                    m_dValue[i++] = Double.parseDouble(strValue);
//                }
//            }
//            reader.close();