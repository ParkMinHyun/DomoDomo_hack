package ensharp.tapcorder;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.widget.TextView;

import java.io.InputStream;
import java.io.OutputStream;

import ensharp.tapcorder.Record.ProgressRecorder;

public class AlarmCheck extends AsyncTask<Void, Void, Void> {

    private TextToSpeech tts;

    BluetoothAdapter mBluetoothAdapter;
    //블루투스 오픈 사용
    BluetoothSocket mmSocket;
    //블루투스 기기
    BluetoothDevice mmDevice;
    //블루투스로 전송시 사용
    OutputStream mmOutputStream;
    //블루투스로부터 데이트 받을때 사용
    InputStream mmInputStream;

    boolean is_connected = false;
    String devicename;
    TextView info_textview;

    volatile boolean stopWorker;
    ////////////////////////////////////////

    CountDownTimer timer;

    int alarmHours, alarmMinutes, totalNowSeconds, totalAlarm;
    int target;
    public static int check;

    protected void onPreExecute() {
        super.onPreExecute();


//        SharedPreferences mPairedSettings;
//        mPairedSettings = getSharedPreferences(BT_PREFERENCE, Context.MODE_PRIVATE);
//        if (!mPairedSettings.contains(BP_PREFERENCES_PAIRED_DEVICE)) {
//            // 선택하지 않았다면  종료 한다.
//            // 사실 선택하지 않으면 이 화면으로 오지 않는다
//        }
//        devicename = mPairedSettings.getString(BP_PREFERENCES_PAIRED_DEVICE, "");
//
//        tts = new TextToSpeech(this, this);
//        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
//        filter.addAction(Intent.ACTION_SCREEN_OFF);
//        final BroadcastReceiver mReceiver = new ScreenReceiver();
//        registerReceiver(mReceiver, filter);
        ////////////////////////////////////////////////////////////////////////////



        alarmHours = ProgressRecorder.hours;
        alarmMinutes = ProgressRecorder.minutes;

        totalAlarm = (alarmHours*60*60) + (alarmMinutes*60);
        totalNowSeconds = (ProgressRecorder.nowHours*60*60) + (ProgressRecorder.nowMinutes*60) + ProgressRecorder.nowSeconds;

        if(totalNowSeconds>totalAlarm)
        {
            target = (24*60*60-totalNowSeconds)+totalAlarm;
        }
        if(totalAlarm>totalNowSeconds)
        {
            target = totalAlarm - totalNowSeconds;
        }


        timer = new CountDownTimer(target*1000,1000) {
            @Override
            public void onTick(long l) {

                if((l>500)&&(l<2000))
                {
                    check = 1;
                }
            }

            @Override
            public void onFinish() {
                check = 1;
                startIntent();
            }
        }.start();
    }
    @Override
    protected Void doInBackground(Void... voids) {
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
    }
    public void startIntent(){
        Intent intent = new Intent(MainActivity.context, MainActivity.class);
        //intent.putExtra("check", check);
        MainActivity.context.startActivity(intent);

    }
}
