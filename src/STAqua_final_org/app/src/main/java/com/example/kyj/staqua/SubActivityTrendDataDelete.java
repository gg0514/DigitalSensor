package com.example.kyj.staqua;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import static com.example.kyj.staqua.MainActivity.SendProtocol_Get;

/**
 * Created by KYJ on 2017-01-16.
 */

public class SubActivityTrendDataDelete extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sub_activity_trend_datadelete, container, false);

        v.findViewById(R.id.Trend_DD_Option1_Delete).setOnClickListener(mClickListener);

        return v;
    }

    public static SubActivityTrendDataDelete newInstance(String text) {

        SubActivityTrendDataDelete f = new SubActivityTrendDataDelete();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    void DeleteDir(String path)
    {
        File file = new File(path);
        File[] childFileList = file.listFiles();
        for(File childFile : childFileList)
        {
            if(childFile.isDirectory()) {
                DeleteDir(childFile.getAbsolutePath());     //하위 디렉토리 루프
            }
            else {
                childFile.delete();    //하위 파일삭제
            }
        }
        file.delete();    //root 삭제
        Toast.makeText(getActivity(), R.string.multi_deleted, Toast.LENGTH_SHORT).show();
    }

    Button.OnClickListener mClickListener  = new View.OnClickListener() {
        public void onClick(View v) {
            //이곳에 버튼 클릭시 일어날 일을 적습니다.
            switch (v.getId()) {
                case R.id.Trend_DD_Option1_Delete:
                    AlertDialog.Builder d = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_TRADITIONAL);
                    d.setMessage(R.string.multi_are_you_sure_delete);
                    d.setPositiveButton(R.string.multi_answer_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                          //  String dirPath = Environment.getExternalStorageDirectory()+"/STAqua/trend/";
                          //  DeleteDir(dirPath);

                            byte[] btData = new byte[1];
                            btData[0] = 0x00;
                            if (!SendProtocol_Get(1,(byte)0xC8, btData)){
                                Toast.makeText(getActivity(), R.string.multi_check_connection, Toast.LENGTH_LONG).show();//토스트메세지를 띄운다.
                                return;
                            }
                            Toast.makeText(getActivity(), R.string.multi_deleted, Toast.LENGTH_LONG).show();

                        }
                    });
                    d.setNegativeButton(R.string.multi_answer_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    d.show();
                    break;
             }
        }
    };
}
