package ensharp.tapcorder;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import java.util.Set;

import ensharp.tapcorder.LED.Activity_LED;
import ensharp.tapcorder.Music.Activity_Music;
import ensharp.tapcorder.Record.ProgressRecorder;


public class MainActivity extends BT_Preference {

    BluetoothAdapter mBluetoothAdapter;
	BluetoothDevice mmDevice;
    BluetoothSocket mmSocket;

    Button btn_record;
    Button btn_musicFiles;
    Button btn_LED;
    Button btn_downMusic;

    public static Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        context = this;

        Intent intent=new Intent(this,Splash.class);
        this.startActivity(intent);

        Typeface tf_menu = Typeface.createFromAsset(getAssets(), "BMJUA_ttf.ttf");

        btn_record = (Button)findViewById(R.id.btn_Record);
        btn_record.setTypeface(tf_menu);
        btn_record.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this,ProgressRecorder.class);
                intent.putExtra("check",0);
                startActivity(intent);
            }
        });
//        if(AlarmCheck.check==1)
//        {
//            Intent intent_A = new Intent(MainActivity.this,ProgressRecorder.class);
//            intent_A.putExtra("check",1);
//            startActivity(intent_A);
//        }
        btn_musicFiles = (Button)findViewById(R.id.btn_Music);
        btn_musicFiles.setTypeface(tf_menu);
        btn_musicFiles.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(MainActivity.this, Activity_Music.class));
            }
        });
        btn_LED = (Button)findViewById(R.id.btn_LED);
        btn_LED.setTypeface(tf_menu);
        btn_LED.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(MainActivity.this, Activity_LED.class));
            }
        });
        btn_downMusic =(Button)findViewById(R.id.btn_Downmusic);
        btn_downMusic.setTypeface(tf_menu);
        btn_downMusic.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(MainActivity.this, ServerActivity.class));
            }
        });

        //페어링 되어 있는 블루투스 찾아서 나열하기
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        SharedPreferences mPairedSettings;
        mPairedSettings = getSharedPreferences(BT_PREFERENCE, Context.MODE_PRIVATE);
        Editor editor = mPairedSettings.edit();
        editor.putString(BP_PREFERENCES_PAIRED_DEVICE, "Safix");
        editor.commit();

        if(pairedDevices.contains("Safix"))
        {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
