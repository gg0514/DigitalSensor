package com.example.kyj.staqua;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import static android.content.ContentValues.TAG;
import static com.example.kyj.staqua.MainActivity.arrayClTrend;
import static com.example.kyj.staqua.MainActivity.arrayCoTrend;
import static com.example.kyj.staqua.MainActivity.arrayFlowTrend;
import static com.example.kyj.staqua.MainActivity.arrayPhTrend;
import static com.example.kyj.staqua.MainActivity.arrayTempTrend;
import static com.example.kyj.staqua.MainActivity.arrayTimeTrend;
import static com.example.kyj.staqua.MainActivity.arrayTuTrend;
import static com.example.kyj.staqua.MainActivity.m_nTrendExcelCount;


public class ExcelExporter {
    public static ExcelExportThread m_ExcelExportThread;

    public static void  startExcelExportThread(){
        if (m_ExcelExportThread == null){
            m_ExcelExportThread = new ExcelExportThread(true);
            m_ExcelExportThread.start();
        }
    }
//    public static void  stopExcelExportThread(){
//        if (m_ExcelExportThread == null){
//            m_ExcelExportThread = new ExcelExportThread(true);
//            m_ExcelExportThread.start();
//        }
//    }
    public static void onStop() {
        if (m_ExcelExportThread != null){
            m_ExcelExportThread.stopExcelExportThread();
        }
    }

    private static class ExcelExportThread extends Thread {
        private boolean isPlay = false;
        private int nTimerFlash = 200;
        public ExcelExportThread(boolean isPlay) {this.isPlay = isPlay;}
        public void stopExcelExportThread(){
            isPlay = !isPlay;
            m_ExcelExportThread = null;
        }
        @Override
        public void run() {
            super.run();
            while (isPlay) {
                try {
                    export();
                    stopExcelExportThread();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static String export() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String strDate = sdf.format(d);
        String dirPath = Environment.getExternalStorageDirectory()+"/STAqua/excel/";
        File directory = new File(dirPath);
        String csvFile = "TrendResult_"+strDate+".xls";

        //create directory if not exist
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }
        try {
            //file path
            File file = new File(directory, csvFile);
            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale(Locale.US.getLanguage(), Locale.US.getCountry()));
            WritableWorkbook workbook = Workbook.createWorkbook(file, wbSettings);
            int nCount = arrayTimeTrend.size();
          //  int nCount = 20000;
            if (nCount > 65530) nCount = 65530;

            WritableSheet sheetA =  workbook.createSheet("TREND"+0, 0);
            int nS = 0;
            for (int i = 0; i < nCount; i++) {
                Log.d(TAG, "[index / nS] : " + i +"|"+nS);
                // 시작시
                if (i == 0) {
            //        sheetA = workbook.getSheet(0);
                    sheetA.addCell(new Label(1, 0, "Ph"));
                    sheetA.addCell(new Label(2, 0, "Cl(mg/L)"));
                    sheetA.addCell(new Label(3, 0, "Tu(NTU)"));
                    sheetA.addCell(new Label(4, 0, "Ec(?s/cm)"));
                    sheetA.addCell(new Label(5, 0, "Temp(℃)"));
                    sheetA.addCell(new Label(6, 0, "Flow"));
                }
                else  if (i < 65530) {

//                    sheetA.addCell(new Label(1, 0, "Ph"));
//                    sheetA.addCell(new Label(2, 0, "Cl(mg/L)"));
//                    sheetA.addCell(new Label(3, 0, "Tu(NTU)"));
//                    sheetA.addCell(new Label(4, 0, "Ec(?s/cm)"));
//                    sheetA.addCell(new Label(5, 0, "Temp(℃)"));
//                    sheetA.addCell(new Label(6, 0, "Flow"));

                    sheetA.addCell(new Label(0, i, TransferToTime(arrayTimeTrend.get(i-1))));
                    sheetA.addCell(new Label(1, i, Double.toString(arrayPhTrend.get(i-1))));
                    sheetA.addCell(new Label(2, i, Double.toString(arrayClTrend.get(i-1))));
                    sheetA.addCell(new Label(3, i, Double.toString(arrayTuTrend.get(i-1))));
                    sheetA.addCell(new Label(4, i, Double.toString(arrayCoTrend.get(i-1))));
                    sheetA.addCell(new Label(5, i, Double.toString(arrayTempTrend.get(i-1))));
                    sheetA.addCell(new Label(6, i, Double.toString(arrayFlowTrend.get(i-1))));
                }
                m_nTrendExcelCount = i;

//                else if (i == 65000) {
//                    sheetA = workbook.createSheet("TREND"+1, 1);
//                    sheetA = workbook.getSheet(1);
//                    sheetA.addCell(new Label(1, 0, "Ph"));
//                    sheetA.addCell(new Label(2, 0, "Cl(mg/L)"));
//                    sheetA.addCell(new Label(3, 0, "Tu(NTU)"));
//                    sheetA.addCell(new Label(4, 0, "Ec(?s/cm)"));
//                    sheetA.addCell(new Label(5, 0, "Temp(℃)"));
//                    sheetA.addCell(new Label(6, 0, "Flow"));
//
//                }
//                else if (i < 130000) {
//                    sheetA.addCell(new Label(0, i - 65000, "sheet B 1"));
//                    sheetA.addCell(new Label(1, i- 65000 , "sheet B 2"));
//                    sheetA.addCell(new Label(2, i- 65000 , "sheet B 3"));
//                    sheetA.addCell(new Label(3, i- 65000 , "sheet B 4"));
//                    sheetA.addCell(new Label(4, i- 65000 , "sheet B 5"));
//                    sheetA.addCell(new Label(5, i- 65000 , "sheet B 6"));
//                    sheetA.addCell(new Label(6, i- 65000 , "sheet B 7"));
//                }

            }
            workbook.write();
            workbook.close();
            return dirPath+csvFile;

        } catch (Exception e) {
            e.printStackTrace();
            return "";
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

}
