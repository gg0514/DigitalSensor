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

import static com.example.kyj.staqua.MainActivity.arrayTimeTrend;
import static com.example.kyj.staqua.MainActivity.m_nTrendExcelCount;
import static com.example.kyj.staqua.MainActivity.m_nTrendExcelLength;
import static com.example.kyj.staqua.MainActivity.m_nTrendProtocolLength;
import static com.example.kyj.staqua.MainActivity.m_nTrendReadCount;

/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivityTrendExcel extends Fragment {

    public Fragment fra = this;
    public ProgressBar pb;
    public TextView tvMainNotice;
    public TextView tvStatus1;
    public TextView tvStatus2;
    public TextView tvStatus3;
    public TextView tvTitle;
    public Button btApply;
    public Button btStop;

    int nPreTrendRealCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_trend_excel, container, false);

        v.findViewById(R.id.Trend_EX_Option1_Apply).setOnClickListener(mClickListener);
        v.findViewById(R.id.Trend_EX_Option1_Stop).setOnClickListener(mClickListener);

        pb = (ProgressBar) v.findViewById(R.id.Trend_EX_Title1_ProgressBar);
        tvMainNotice = (TextView) v.findViewById(R.id.Trend_EX_Content_Text);
        tvStatus1 = (TextView) v.findViewById(R.id.Trend_EX_Status_Text1);
        tvStatus2 = (TextView) v.findViewById(R.id.Trend_EX_Status_Text2);
        tvStatus3 = (TextView) v.findViewById(R.id.Trend_EX_Status_Text3);
        tvTitle = (TextView) v.findViewById(R.id.Trend_EX_Title1_Text);
        btApply = (Button) v.findViewById(R.id.Trend_EX_Option1_Apply);
        btStop = (Button) v.findViewById(R.id.Trend_EX_Option1_Stop);

        tvTitle.setVisibility(View.VISIBLE);
        pb.setVisibility(View.INVISIBLE);
        tvStatus1.setVisibility(View.VISIBLE);
        tvStatus2.setVisibility(View.VISIBLE);
        tvStatus3.setVisibility(View.VISIBLE);
        btApply.setVisibility(View.VISIBLE);
        btStop.setVisibility(View.INVISIBLE);
       // m_nTrendExcelLength = 20000;
        m_nTrendExcelLength = arrayTimeTrend.size();
        if (m_nTrendExcelLength > 65530) m_nTrendExcelLength = 65530;
        tvStatus3.setText(Integer.toString(m_nTrendExcelLength));

        return v;
    }

//    private void saveExcel(){
//
//        Date d = new Date();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//        String strDate = sdf.format(d);
//        String dirPath = Environment.getExternalStorageDirectory()+"/STAqua/excel/";
//        File directory = new File(dirPath);
//        String csvFile = "TrendResult_"+strDate+".xls";
//        File file = new File(directory, csvFile);
//
//        XSSFWorkbook workbook = new XSSFWorkbook(file);
//        XSSFSheet sheet = workbook.createSheet("TREND") ;
//        XSSFRow row = null;
//        XSSFCell cell = null;
//
//        int nCount = 120000;
//        for(int i=0; i<=nCount; i++) {
//            //data의 크기만큼 로우를 생성합니다.
//            row=sheet.createRow((short)i);
//            for(int k=0; k<7; k++) {
//                cell=row.createCell(k);
//                if(i==0) {
////                    CellStyle style = workbook.createCellStyle();
////                    style.setFillForegroundColor("셀color 세팅");
////                    style.setFillPattern("셀의 패턴을 세팅");
////                    style.setAlignment("셀데이터의 정렬조건 세팅");
////                    cell.setCellStyle(style);
//                    //headerList의 데이터를 세팅
//                    cell = row.createCell(0); // 1번 셀 생성
//                    cell.setCellValue("Time"); // 1번 셀 값 입력
//                    cell = row.createCell(1); // 2번 셀 생성
//                    cell.setCellValue("Ph"); // 2번 셀 값 입력
//                    cell = row.createCell(2); // 3번 셀 생성
//                    cell.setCellValue("Cl(mg/L)"); // 3번 셀 값 입력
//                    cell = row.createCell(3); // 4번 셀 생성
//                    cell.setCellValue("Tu(NTU)"); // 3번 셀 값 입력
//                    cell = row.createCell(4); // 5번 셀 생성
//                    cell.setCellValue("Ec(us/cm)"); // 4번 셀 값 입력
//                    cell = row.createCell(5); // 6번 셀 생성
//                    cell.setCellValue("Temp(℃)"); // 5번 셀 값 입력
//                    cell = row.createCell(6); // 7번 셀 생성
//                    cell.setCellValue("Flow"); // 6번 셀 값 입력
//                }
//                //엑셀파일에 넣을 데이터를 세팅합니다.
//                else {
//                    cell = row.createCell(0); // 1번 셀 생성
//                    cell.setCellValue("Time"); // 1번 셀 값 입력
//                    cell = row.createCell(1); // 2번 셀 생성
//                    cell.setCellValue("Ph"); // 2번 셀 값 입력
//                    cell = row.createCell(2); // 3번 셀 생성
//                    cell.setCellValue("Cl(mg/L)"); // 3번 셀 값 입력
//                    cell = row.createCell(3); // 4번 셀 생성
//                    cell.setCellValue("Tu(NTU)"); // 3번 셀 값 입력
//                    cell = row.createCell(4); // 5번 셀 생성
//                    cell.setCellValue("Ec(us/cm)"); // 4번 셀 값 입력
//                    cell = row.createCell(5); // 6번 셀 생성
//                    cell.setCellValue("Temp(℃)"); // 5번 셀 값 입력
//                    cell = row.createCell(6); // 7번 셀 생성
//                    cell.setCellValue("Flow"); // 6번 셀 값 입력
//                }
//            }
//        }
//
//
//
//        //create directory if not exist
//        if (!directory.isDirectory()) {
//            directory.mkdirs();
//        }
//
//        try {
//            FileOutputStream os = new FileOutputStream(csvFile);
//            workbook.write(os); // 외부 저장소에 엑셀 파일 생성
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//        Toast.makeText(getActivity(), directory+csvFile, Toast.LENGTH_SHORT).show();
//    }


    public static SubActivityTrendExcel newInstance(String text) {

        SubActivityTrendExcel f = new SubActivityTrendExcel();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }


    Button.OnClickListener mClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            //이곳에 버튼 클릭시 일어날 일을 적습니다.
            switch (v.getId()) {
                case R.id.Trend_EX_Option1_Apply:
                  //  saveExcel();
              //      String strFileLocation = "";
                    startProgressBarThread();
                    ExcelExporter.startExcelExportThread();
             //       Toast.makeText(getActivity(), strFileLocation, Toast.LENGTH_LONG).show();
                    break;
                case R.id.Trend_EX_Option1_Stop:
                    //ExcelExporter.export();
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
        }

    }
    public synchronized  void stopProgressBarThread(int nResult) {
        if (theProgressBarThread != null) {
            nPreTrendRealCount = 0;
            // m_nTrendProtocolLength = 0;
            // m_nTrendReadCount = 0;
//            Thread tmpThread = theProgressBarThread;
//            theProgressBarThread = null;
//            tmpThread.interrupt();

            theProgressBarThread.interrupt();
            theProgressBarThread = null;
        }
        tvTitle.setVisibility(View.VISIBLE);
        pb.setVisibility(View.INVISIBLE);
        tvStatus1.setVisibility(View.INVISIBLE);
        tvStatus2.setVisibility(View.INVISIBLE);
        tvStatus3.setVisibility(View.INVISIBLE);

        btStop.setVisibility(View.INVISIBLE);

        if (nResult == 1) {
            tvTitle.setText(R.string.multi_download_success);
            btApply.setVisibility(View.INVISIBLE);
            Toast.makeText(getActivity(), R.string.multi_download_success, Toast.LENGTH_SHORT).show();
            m_nTrendExcelCount = 0;
        }
        else if (nResult == 2) {
            tvTitle.setText(R.string.multi_download_failed);
            btApply.setVisibility(View.VISIBLE);
            m_nTrendExcelCount = 0;
        }
   //     ExcelExporter.onStop();
    }

    private Runnable backgroundThread = new Runnable() {
        int nTimeout = 0;
        public void run() {
//            if (Thread.currentThread() == theProgressBarThread) {
//
//            }
            nTimeout = 0;
            nPreTrendRealCount = 0;
            m_nTrendReadCount = 0;
            while (!Thread.currentThread().isInterrupted()) {
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

        Handler progressBarHandle = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (m_nTrendExcelLength >= 0) {
                    int nProcess = 0;
                    if (m_nTrendExcelLength > 0 ) {
                        nProcess = 100 * (m_nTrendExcelCount) / (m_nTrendExcelLength);
                        pb.setProgress(nProcess);
                    }
                    tvStatus1.setText("" + nProcess + "%");
                    tvStatus3.setText(m_nTrendExcelCount + "/" + m_nTrendExcelLength);

                    if (nPreTrendRealCount == m_nTrendExcelCount) nTimeout++;
                    else nTimeout = 0;
                    if (m_nTrendExcelLength > 0 ) {
                        if(m_nTrendExcelCount >= m_nTrendExcelLength) stopProgressBarThread(1);
                    }

                    if (nTimeout > 300) {
                        if (nProcess > 95) {
                            tvStatus1.setText("" + 100 + "%");
                            tvStatus3.setText(m_nTrendProtocolLength  + "/" + m_nTrendProtocolLength );
                            stopProgressBarThread(1);
                            nTimeout = 0;
                        }
                        else  {
                            stopProgressBarThread(2);
                            nTimeout = 0;
                        }
                    }

                    nPreTrendRealCount = m_nTrendExcelCount;
                }

            }
        };
    };
}
