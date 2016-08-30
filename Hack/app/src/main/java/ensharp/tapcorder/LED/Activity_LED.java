package ensharp.tapcorder.LED;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import ensharp.tapcorder.Arduino.ScreenReceiver;
import ensharp.tapcorder.BT_Preference;
import ensharp.tapcorder.MainActivity;
import ensharp.tapcorder.R;

/**
 * Created by user on 2016-08-29.
 */
public class Activity_LED extends BT_Preference implements TextToSpeech.OnInitListener,View.OnClickListener{
    ArrayList<String> mDatas = new ArrayList<String>();
    ListView listview; //ListView 참조변수

    /////////////////////////////////////////////////

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

    //블루투스로부터 들어오는 데이타 처리위한 것들
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led);


        mDatas.add("LED 1");
        mDatas.add("LED 2");

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mDatas);
        listview = (ListView)findViewById(R.id.listview_led);
        listview.setAdapter(adapter); //위에 만들어진 Adapter를 ListView에 설정 : xml에서 'entries'속성

        //ListView의 아이템 하나가 클릭되는 것을 감지하는 Listener객체 설정 (Button의 OnClickListener와 같은 역할)
        listview.setOnItemClickListener(listener);

        //////////////////////////////////////////////////////

        tts = new TextToSpeech(this, this);

        SharedPreferences mPairedSettings;
        mPairedSettings = getSharedPreferences(BT_PREFERENCE, Context.MODE_PRIVATE);
        if (!mPairedSettings.contains(BP_PREFERENCES_PAIRED_DEVICE)) {
            // 선택하지 않았다면  종료 한다.
            // 사실 선택하지 않으면 이 화면으로 오지 않는다
        }
        devicename = mPairedSettings.getString(BP_PREFERENCES_PAIRED_DEVICE, "");
        info_textview = 			(TextView) findViewById(R.id.textview_info);

        // 스크린이 꺼지면 bluetooth를 자동으로 연결 끊기 위해서 BroadcastReceiver 설정
        // 전화가 온다거나 다른 작업을 할 경우에는 블루투스 연결을 끊도록 하기위해서
        // 계속 연결 상태를 유지 하려면 이부분을 없애면 됩니다.

        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        final BroadcastReceiver mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);
    }

    AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // TODO Auto-generated method stub
            try {
                sendData(String.valueOf(position+20));
            } catch (IOException e) {
                e.printStackTrace();
            }

            //클릭된 아이템의 위치를 이용하여 데이터인 문자열을 Toast로 출력
            Toast.makeText(Activity_LED.this, mDatas.get(position), Toast.LENGTH_SHORT).show();


        }
    };

    public void sendData(String msg) throws IOException
    {
        //String msg = myTextbox.getText().toString();
        msg = msg.trim();
        msg += "\n";
        mmOutputStream.write(msg.getBytes());
        //info_textview.setText("Data Sent");
    }

    //백버튼 사용시 블루투스가 연결 되어있으면 연결 끊고 간다
    @Override
    public void onBackPressed() {
        try
        {
            String msg = "stop";
            msg += "\n";
            mmOutputStream.write(msg.getBytes());
            try {

                Thread.sleep(500);

            } catch (InterruptedException e) { }
            closeBT();
            try {

                Thread.sleep(1000);

            } catch (InterruptedException e) { }





        }
        catch (IOException ex) { }


        super.onBackPressed();
    }

    //tts가 초기화 되고 초기화가 성공하면 블루투스를 찾아서 연결하도록 했다.
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                // btnSpeak.setEnabled(true);
                speakOut("start");

                try
                {
                    findBT();
                    openBT();

                }
                catch (IOException ex) {
                    speakOut("fail to open connection.");
                    startActivity(new Intent(Activity_LED.this, MainActivity.class));

                }
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }

    //텍스트를 음성으로 바꾸는 부분
    private void speakOut(String txt) {

        //String text = txtText.getText().toString();


        tts.speak(txt, TextToSpeech.QUEUE_FLUSH, null);
    }


    @Override
    // 화면이 꺼질때 불려진다
    protected void onPause() {

        if (ScreenReceiver.wasScreenOn) {
            //speakOut("screen off");

            //speakOut("connection closed");
            try
            {
                String msg = "stop";
                msg += "\n";
                mmOutputStream.write(msg.getBytes());
                try {

                    Thread.sleep(1000);

                } catch (InterruptedException e) { }
                closeBT();




            }
            catch (IOException ex) { }

            // this is the case when onPause() is called by the system due to a screen state change
            Log.e("MYAPP", "SCREEN TURNED OFF");
        } else {
            // this is when onPause() is called when the screen state has not changed
        }
        super.onPause();
    }

    // 페어링된 기기중에서 사용자가 선택한 블루투스를 실제로 찾는다.
    void findBT()
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
            speakOut("No bluetooth adapter available");
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals(devicename))
                {
                    mmDevice = device;
                    break;
                }
            }
        }
        //speakOut("Bluetooth Device Found");
    }

    //찾은 블루투스를 안드로이드 폰과 연결한다
    void openBT() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();


        speakOut("Connection Opened");
        is_connected = true;



        String msg = "start";
        msg += "\n";
        mmOutputStream.write(msg.getBytes());

        //msg = "blinkOn13";
        //msg += "\n";
        //mmOutputStream.write(msg.getBytes());
    }


    //블루투스를 안드로이드와 연결 해제한다.
    void closeBT() throws IOException
    {
        String msg = "stop";
        msg += "\n";
        mmOutputStream.write(msg.getBytes());
        try {

            Thread.sleep(100);

        } catch (InterruptedException e) { }






        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        speakOut("Connection Closed");

        is_connected = false;
    }

//    void beginListenForData()
//    {
//        final Handler handler = new Handler();
//        final byte delimiter = 10; //This is the ASCII code for a newline character
//
//        stopWorker = false;
//        readBufferPosition = 0;
//        readBuffer = new byte[1024];
//        workerThread = new Thread(new Runnable()
//        {
//            public void run()
//            {
//                while(!Thread.currentThread().isInterrupted() && !stopWorker)
//                {
//
//                    if(!is_connected)
//                    {
//                        continue;
//                    }
//
//
//                    try
//                    {
//                        int bytesAvailable = mmInputStream.available();
//                        if(bytesAvailable > 0)
//                        {
//                            byte[] packetBytes = new byte[bytesAvailable];
//                            mmInputStream.read(packetBytes);
//                            for(int i=0;i<bytesAvailable;i++)
//                            {
//                                byte b = packetBytes[i];
//                                if(b == delimiter)
//                                {
//                                    byte[] encodedBytes = new byte[readBufferPosition];
//                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
//                                    final String data = new String(encodedBytes, "EUC-KR");
//                                    readBufferPosition = 0;
//
//                                    handler.post(new Runnable()
//                                    {
//                                        public void run()
//                                        {
//
//                                            info_textview.setText(data);
//
//                                            String tmp2 = (String)data;
//
//
//                                            String tmp = (String)info_textview.getText();
//
//                                            info_textview.setText(tmp2);
//
//                                            if( tmp.indexOf("blocked") != -1)
//                                            {
//                                                speakOut("blocked");
//
//                                            }
//                                            else if( tmp.indexOf("cleared") != -1)
//                                            {
//                                                speakOut("cleared");
//
//                                            }
//                                        }
//                                    });
//                                }
//                                else
//                                {
//                                    readBuffer[readBufferPosition++] = b;
//                                }
//                            }
//                        }
//                    }
//                    catch (IOException ex)
//                    {
//                        stopWorker = true;
//                    }
//
//                    try {
//
//                        Thread.sleep(100);
//
//                    } catch (InterruptedException e) { }
//                }
//            }
//        });
//
//        workerThread.start();
//    }
    public void onClick(View v) {
    }
}
