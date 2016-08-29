package ensharp.tapcorder.Music;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import ensharp.tapcorder.BT_Preference;
import ensharp.tapcorder.R;

/**
 * Created by user on 2016-08-29.
 */
public class Activity_Music extends BT_Preference {

    ArrayList<String> mDatas = new ArrayList<String>();
    ListView listview; //ListView 참조변수

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        mDatas.add("음악파일01");
        mDatas.add("음악파일02");
        mDatas.add("음악파일03");

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mDatas);
        listview = (ListView)findViewById(R.id.listview_music);
        listview.setAdapter(adapter); //위에 만들어진 Adapter를 ListView에 설정 : xml에서 'entries'속성

        //ListView의 아이템 하나가 클릭되는 것을 감지하는 Listener객체 설정 (Button의 OnClickListener와 같은 역할)
        listview.setOnItemClickListener(listener);
    }


    //ListView의 아이템 하나가 클릭되는 것을 감지하는 Listener객체 생성 (Button의 OnClickListener와 같은 역할)
    AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // TODO Auto-generated method stub

            //클릭된 아이템의 위치를 이용하여 데이터인 문자열을 Toast로 출력
            Toast.makeText(Activity_Music.this, mDatas.get(position), Toast.LENGTH_SHORT).show();


        }
    };


}
