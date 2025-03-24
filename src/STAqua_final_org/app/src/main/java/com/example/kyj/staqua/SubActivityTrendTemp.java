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

import static com.example.kyj.staqua.MainActivity.arrayTempTrend;
import static com.example.kyj.staqua.MainActivity.m_bCLOnRange;
import static com.example.kyj.staqua.MainActivity.m_dAverage;
import static com.example.kyj.staqua.MainActivity.m_dCLMax;
import static com.example.kyj.staqua.MainActivity.m_dCLMin;
import static com.example.kyj.staqua.MainActivity.m_dTemp;
import static com.example.kyj.staqua.MainActivity.m_nTrendFilter;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivityTrendTemp extends Fragment {

    // 그래프 그리기
    private static XYMultipleSeriesDataset tempMultipleSeriesDataset;
    private static XYMultipleSeriesRenderer tempMultipleSeriesRenderer;
    private static XYSeriesRenderer tempSeriesRenderer;
    private static XYSeries tempSeries;
    private static GraphicalView tempGv;
    private static View v;

    private SubActivityTrendTemp.TempTrendThread m_TempTrendThread;
    TextView txTempCurrentValue;
    TextView txTempAverageTitle;
    TextView txTempAverageValue;
    TextView txTempConfidenceTitle;
    TextView txTempConfidenceValue;
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

    ArrayList<Double> arrayTempValue;

    String m_strXAxis = "";
    double m_dPreTempValue = 0;

    boolean m_bIsHistory = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup tempntainer, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_trend_temp, tempntainer, false);

        final Button btnHistory = (Button) v.findViewById(R.id.Trend_Temp_Content_Btn_History);
        final Button btnXIn = (Button) v.findViewById(R.id.Trend_Temp_X_Btn_In) ;
        final Button btnXOut = (Button) v.findViewById(R.id.Trend_Temp_X_Btn_Out) ;
        Button.OnClickListener onClickListener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date d = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String strDate = sdf.format(d);
                switch (view.getId()) {
                    case R.id.Trend_Temp_X_Btn_In :
                        m_dXMax= m_dXMax - m_nXLength / 2;
                        if (m_dXMax <= 10) m_dXMax = 10;
                        if (m_TempTrendThread != null) {
                            m_TempTrendThread.stopTrendThread();
                            m_nX = 0;
                        }
                        ll.removeView(tempGv);
                        arrayTempValue.clear();
                        showTempChart();
                        tempGetFileData(strDate);
                        m_TempTrendThread = new SubActivityTrendTemp.TempTrendThread(true);
                        m_TempTrendThread.start();
                        break ;
                    case R.id.Trend_Temp_X_Btn_Out :
                        m_dXMax= m_dXMax + m_nXLength;
                        if ((m_bIsSecond) && (m_dXMax > 3600)) m_dXMax = 3600;
                        if ((!m_bIsSecond) && (m_dXMax > 1440)) m_dXMax = 1440;
                        if (m_TempTrendThread != null) {
                            m_TempTrendThread.stopTrendThread();
                            m_nX = 0;
                        }
                        ll.removeView(tempGv);
                        arrayTempValue.clear();
                        showTempChart();
                        tempGetFileData(strDate);
                        m_TempTrendThread = new SubActivityTrendTemp.TempTrendThread(true);
                        m_TempTrendThread.start();
                        break ;
                    case R.id.Trend_Temp_Previous_Btn_Left:
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

                            if (m_TempTrendThread != null) {
                                m_TempTrendThread.stopTrendThread();
                                m_nX = 0;
                            }
                            ll.removeView(tempGv);
                            arrayTempValue.clear();
                            showTempChart();
                            tempGetFileData(strDate);
                            m_TempTrendThread = new SubActivityTrendTemp.TempTrendThread(true);
                            m_TempTrendThread.start();
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
                            ll.removeView(tempGv);
                            arrayTempValue.clear();

                            showTempChart();
                            tempChartAddHistory();
                        }

                        break ;
                    case R.id.Trend_Temp_Previous_Btn_Right:
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

                            if (m_TempTrendThread != null) {
                                m_TempTrendThread.stopTrendThread();
                                m_nX = 0;
                            }
                            ll.removeView(tempGv);
                            arrayTempValue.clear();
                            showTempChart();
                            tempGetFileData(strDate);
                            m_TempTrendThread = new SubActivityTrendTemp.TempTrendThread(true);
                            m_TempTrendThread.start();
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
                            ll.removeView(tempGv);
                            arrayTempValue.clear();

                            showTempChart();
                            tempChartAddHistory();
                        }

                        break ;
                    case R.id.Trend_Temp_Content_Btn_History:
                        m_bIsHistory = true;
                        m_bIsSecond = FALSE;
                        m_dXMin = 0;
                        m_dXMax = 720;
                        m_strXAxis = getResources().getString(R.string.multi_hour_previous);

                        if (m_TempTrendThread != null) {
                            m_TempTrendThread.stopTrendThread();
                            SystemClock.sleep(1000);
                            m_nX = 0;
                        }
                        //   m_PhTrendThread.stopTrendThread();
                        ll.removeView(tempGv);
                        arrayTempValue.clear();

                        showTempChart();
                        tempChartAddHistory();
                        break;
                }
            }
        } ;
        btnXIn.setOnClickListener(onClickListener) ;
        btnXOut.setOnClickListener(onClickListener) ;
        Button btnLeft = (Button) v.findViewById(R.id.Trend_Temp_Previous_Btn_Left) ;
        btnLeft.setOnClickListener(onClickListener) ;
        Button btnRight = (Button) v.findViewById(R.id.Trend_Temp_Previous_Btn_Right) ;
        btnRight.setOnClickListener(onClickListener) ;

        btnHistory.setOnClickListener(onClickListener) ;

        txTempCurrentValue = (TextView)v.findViewById(R.id.Trend_Temp_Data_Text_Value);
        txTempAverageTitle = (TextView)v.findViewById(R.id.Trend_Temp_Data_Average_Title);
        txTempAverageValue = (TextView)v.findViewById(R.id.Trend_Temp_Data_Average_Value);
        txTempConfidenceTitle = (TextView)v.findViewById(R.id.Trend_Temp_Confidence_Title);
        txTempConfidenceValue = (TextView)v.findViewById(R.id.Trend_Temp_Confidence_Value);
        // progressCircle = (ProgressBar)v.findViewById(R.id.Trend_Temp_Progress);
        ll = (LinearLayout) v.findViewById(R.id.Trend_Temp_Chart);
        arrayTempValue = new ArrayList();
        btnXIn.setVisibility(View.INVISIBLE);
        btnXOut.setVisibility(View.INVISIBLE);
        m_bIsHistory = true;
        m_bIsSecond = FALSE;
        m_dXMin = 0;
        m_dXMax = 720;
        m_strXAxis = getResources().getString(R.string.multi_hour_previous);
        ll.removeView(tempGv);
        arrayTempValue.clear();
        showTempChart();
        tempChartAddHistory();

        if (m_nTrendFilter == 1) {
            txTempAverageTitle.setVisibility(View.INVISIBLE);
            txTempAverageValue.setVisibility(View.INVISIBLE);
            txTempConfidenceTitle.setVisibility(View.INVISIBLE);
            txTempConfidenceValue.setVisibility(View.INVISIBLE);
        }
        else if (m_nTrendFilter == 2) {
            txTempAverageTitle.setVisibility(View.VISIBLE);
            txTempAverageTitle.setText(R.string.multi_trend_moving_average);
            txTempAverageValue.setVisibility(View.VISIBLE);
            txTempConfidenceTitle.setVisibility(View.INVISIBLE);
            txTempConfidenceValue.setVisibility(View.INVISIBLE);
        }
        else if (m_nTrendFilter == 3) {
            txTempAverageTitle.setVisibility(View.VISIBLE);
            txTempAverageTitle.setText(R.string.multi_trend_on_range);
            txTempAverageValue.setVisibility(View.VISIBLE);
            txTempConfidenceTitle.setVisibility(View.VISIBLE);
            txTempConfidenceValue.setVisibility(View.VISIBLE);
        }
        return v;
    }

    @Override
    public void onStop() {
        if (m_TempTrendThread != null) {
            m_TempTrendThread.stopTrendThread();
        }
        m_nX = 0;
        m_nIndex = 0;
        super.onStop();
    }

    public static SubActivityTrendTemp newInstance(String text) {

        SubActivityTrendTemp f = new SubActivityTrendTemp();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    public  void tempGetFileData(String strDate){
        String dirPath = Environment.getExternalStorageDirectory()+"/STAqua/trend/"+strDate+"/lane4.txt";
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
                    arrayTempValue.add(Double.parseDouble(strValue));
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
//                arrayTempValue.add(Double.parseDouble(strValue));
//                pos = pos - strTemp.length() - 1;
//                i++;
//            }
            file.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public  void tempChartAdd(){
        tempChartClear();
        for (int i = 0; i < m_dXMax; i++) {
            if (i >= arrayTempValue.size()) tempSeries.add(i,0);
            else tempSeries.add(i,arrayTempValue.get(i));
        }
        if (tempGv != null){
            tempGv.repaint();
        }
        else{
            tempSeries.add(0, arrayTempValue.get(0));
            tempMultipleSeriesDataset.addSeries(tempSeries);
            tempGv =  ChartFactory.getLineChartView(getActivity().getBaseContext(), tempMultipleSeriesDataset, tempMultipleSeriesRenderer);
            ll.addView(tempGv);
        }
    }

    public  void tempChartAddHistory(){
        tempChartClear();
        for (int i = 0; i < m_dXMax; i++) {
            if (i >= arrayTempTrend.size()) tempSeries.add(i,0);
            else tempSeries.add(i,arrayTempTrend.get(i));
        }
        if (tempGv != null){
            tempGv.repaint();
        }
        else{
            tempSeries.add(0, arrayTempTrend.get(0));
            tempMultipleSeriesDataset.addSeries(tempSeries);
            tempGv =  ChartFactory.getLineChartView(getActivity().getBaseContext(), tempMultipleSeriesDataset, tempMultipleSeriesRenderer);
            ll.addView(tempGv);
        }
    }

    public  void tempChartClear(){
        tempSeries.clear();
    }


    public  void showTempChart() {

        tempMultipleSeriesDataset = new XYMultipleSeriesDataset();
        tempMultipleSeriesRenderer = new XYMultipleSeriesRenderer();
        tempSeriesRenderer = new XYSeriesRenderer();
        //tempSeries = new XYSeries("Temperature");
        tempSeries = new XYSeries(getResources().getString(R.string.multi_temp1));

        // Adding Intempme Series to the dataset
        tempMultipleSeriesDataset.addSeries(tempSeries);
        // Creating XYSeriesRenderer to customize intempmeSeries
        tempSeriesRenderer.setColor(Color.WHITE);
        tempSeriesRenderer.setPointStyle(PointStyle.POINT);
        tempSeriesRenderer.setFillPoints(true);
        tempSeriesRenderer.setLineWidth(2);
        tempSeriesRenderer.setDisplayChartValues(false);

        int [] nMargin = new int[]{80, 60, 100, 40};
        tempMultipleSeriesRenderer.setMargins(nMargin);
        tempMultipleSeriesRenderer.setChartTitleTextSize(0);
        // tempMultipleSeriesRenderer.setTextTypeface("TEST",2);
        tempMultipleSeriesRenderer.setAxisTitleTextSize(30);
        tempMultipleSeriesRenderer.setLabelsTextSize(20);
        tempMultipleSeriesRenderer.setXAxisMin(m_dXMin);
        tempMultipleSeriesRenderer.setXAxisMax(m_dXMax);

        // X축의 개수를 구함
        m_nXLength = (int)m_dXMax - (int)m_dXMin;

        tempMultipleSeriesRenderer.setYAxisMin(0);
        tempMultipleSeriesRenderer.setYAxisMax(50);
        tempMultipleSeriesRenderer.setLegendTextSize(50);
        tempMultipleSeriesRenderer.setLegendHeight(100);
        if (m_bIsHistory) {
            tempMultipleSeriesRenderer.setXLabels(0);
            for (int i = 0; i < 6; i++ ) {
                tempMultipleSeriesRenderer.addXTextLabel(m_dXMin + (i * 120), String.format("-%.0fH", (m_dXMin + (i * 120))/60));
            }
            tempMultipleSeriesRenderer.addXTextLabel(m_dXMin + (6 * 120), String.format("-%.0fH", (m_dXMin + (6 * 120))/60));
        }
        else {
            tempMultipleSeriesRenderer.setXLabels(0);
            if (m_bIsSecond) {
                for (int i = 0; i < 6; i++ ) {
                    tempMultipleSeriesRenderer.addXTextLabel(m_dXMin + (m_nXLength / 6) * i, String.format("-%.0fS", m_dXMin + (m_nXLength / 6) * i));
                }
                tempMultipleSeriesRenderer.addXTextLabel(m_dXMin + (m_nXLength / 6) * 6, String.format("-%.0fS", m_dXMin + (m_nXLength / 6) * 6));
            }
            else {
                for (int i = 0; i < 6; i++ ) {
                    tempMultipleSeriesRenderer.addXTextLabel(m_dXMin + (m_nXLength / 6) * i, String.format("-%.0fM", m_dXMin + (m_nXLength / 6) * i));
                }
                tempMultipleSeriesRenderer.addXTextLabel(m_dXMin + (m_nXLength / 6) * 6, String.format("-%.0fM", m_dXMin + (m_nXLength / 6) * 6));
            }
        }
        tempMultipleSeriesRenderer.setYLabels(7);
        tempMultipleSeriesRenderer.setXTitle(m_strXAxis);
        // tempMultipleSeriesRenderer.setXLabelsAlign(Paint.Align.RIGHT);
        tempMultipleSeriesRenderer.setYLabelsAlign(Paint.Align.RIGHT);
        tempMultipleSeriesRenderer.setYLabelsPadding(5.0f);
        tempMultipleSeriesRenderer.setYTitle("");


        // X,Y축 라인 색상
        tempMultipleSeriesRenderer.setAxesColor(Color.WHITE);
        // 상단제목, X,Y축 제목, 수치값의 글자 색상
        tempMultipleSeriesRenderer.setLabelsColor(Color.CYAN);

        // X,Y축 스크롤 여부 ON/OFF
        tempMultipleSeriesRenderer.setPanEnabled(false, true);
        // ZOOM기능 ON/OFF
        tempMultipleSeriesRenderer.setZoomEnabled(false, true);
        // ZOOM 비율
        tempMultipleSeriesRenderer.setZoomRate(1.0f);
        tempMultipleSeriesRenderer.addSeriesRenderer(tempSeriesRenderer);
        // 그래프 객체 생성
        tempGv = ChartFactory.getLineChartView(getActivity().getBaseContext(), tempMultipleSeriesDataset, tempMultipleSeriesRenderer);



        ll.addView(tempGv);
        //  tempGv.repaint();
    }



    class TempTrendThread extends Thread {
        private boolean isPlay = false;
        public TempTrendThread(boolean isPlay) {this.isPlay = isPlay;}
        public void stopTrendThread(){
            isPlay = !isPlay;
            tempChartClear();
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
                if (m_nTrendFilter == 1 )  arrayTempValue.add(0, m_dTemp);
                else if (m_nTrendFilter == 2) arrayTempValue.add(0, m_dAverage[3]);
                else if (m_nTrendFilter == 3) {
                    if (m_bCLOnRange[3]) arrayTempValue.add(0, m_dTemp);
                    else    arrayTempValue.add(0, m_dPreTempValue);
                }
                arrayTempValue.remove(m_dXMax-1);
               // arrayTempValue.remove(arrayTempValue.size()-1);
                tempChartAdd();
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
                String strValue = df3Figure.format( m_dTemp );
                String strPreValue = df3Figure.format(m_dPreTempValue);
                String strAverage = df3Figure.format(m_dAverage[3]);
                if (m_nTrendFilter == 1 )  txTempCurrentValue.setText(strValue);
                else if (m_nTrendFilter == 2) {
                    txTempCurrentValue.setText(strValue);
                    txTempAverageValue.setText(strAverage);
                }
                else if (m_nTrendFilter == 3) {
                    if (m_bCLOnRange[3]) {
                        txTempCurrentValue.setText(strValue);
                        txTempAverageValue.setText("YES");
                        m_dPreTempValue = m_dTemp;
                    }
                    else    {
                        txTempCurrentValue.setText(strPreValue);
                        txTempAverageValue.setText("NO");
                    }
                    String strMax = df3Figure.format(m_dCLMax[3]);
                    String strMin = df3Figure.format(m_dCLMin[3]);
                    txTempConfidenceValue.setText(strMin + " ~ " + strMax);
                }
            }
            else if (msg.what == 2) {
                //  txLane2.setText(m_strLane2);
            }
        }
    };


}
