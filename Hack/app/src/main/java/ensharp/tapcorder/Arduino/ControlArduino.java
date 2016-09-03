package ensharp.tapcorder.Arduino;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import ensharp.tapcorder.BT_Preference;
import ensharp.tapcorder.MainActivity;
import ensharp.tapcorder.R;
import ensharp.tapcorder.Record.MakeRecordDir;


public class ControlArduino extends BT_Preference implements TextToSpeech.OnInitListener,View.OnClickListener, MediaPlayer.OnCompletionListener  {
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
    
    //블루투스로부터 들어오는 데이타 처리위한 것들
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;
    
    
    
    
    Button openButton;
    Button closeButton;
    Button sendButton;
    
    Button forwardButton;
    Button leftButton;
    Button rightButton;
    Button backwardButton;
    Button start_movingButton;
    Button stop_movingButton;
    
    
    TextView info_textview;
    EditText myTextbox;
    /////////////////////////////////////
    //대량의 문자열 데이터를 저장할 Arraylist 객체 생성
    ArrayList<String> mDatas = new ArrayList<String>();

    ListView listview; //ListView 참조변수


    //미리 상수 선언
    private static final int REC_STOP = 0;
    private static final int RECORDING = 1;
    private static final int PLAY_STOP = 0;
    private static final int PLAYING = 1;
    private static final int PLAY_PAUSE = 2;

    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private int mRecState = REC_STOP;
    private int mPlayerState = PLAY_STOP;
    private SeekBar mRecProgressBar, mPlayProgressBar;
    private Button mBtnStartRec, mBtnStartPlay;
    private String mFileName = "recordFile1.amr";

    private TextView mTvPlayMaxPoint;

    private int mCurRecTimeMs = 0;
    private int mCurProgressTimeDisplay = 0;

    private String newRecordFile="recordFile1"; //녹음파일이 null일때 파일명 시작값
    private String mFilePath ; //녹음파일 디렉터리 위치
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        
        //이전 화면에서 선택하고 저장한  블루투스 이름 찾기  
        SharedPreferences mPairedSettings;
        mPairedSettings = getSharedPreferences(BT_PREFERENCE, Context.MODE_PRIVATE);
        if (!mPairedSettings.contains(BP_PREFERENCES_PAIRED_DEVICE)) {
          // 선택하지 않았다면  종료 한다. 
          // 사실 선택하지 않으면 이 화면으로 오지 않는다
        }
        devicename = mPairedSettings.getString(BP_PREFERENCES_PAIRED_DEVICE, "");
        
        
        // 스크린이 꺼지면 bluetooth를 자동으로 연결 끊기 위해서 BroadcastReceiver 설정
        // 전화가 온다거나 다른 작업을 할 경우에는 블루투스 연결을 끊도록 하기위해서 
        // 계속 연결 상태를 유지 하려면 이부분을 없애면 됩니다.
        
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON); 
        filter.addAction(Intent.ACTION_SCREEN_OFF); 
        final BroadcastReceiver mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter); 
        
        //택스트를 음성으로 바꾸어주는 기능 사용
        // 이 코드를 실행하면 및에 있는 onInit 함수가 불리워지고 tts(text to speech)가 초기화된다.
        tts = new TextToSpeech(this, this); 
        
        info_textview = 			(TextView) findViewById(R.id.textview_info);

        // SD카드에 디렉토리를 만든다.
        mFilePath = MakeRecordDir.makeDir("progress_recorder");
        Log.i("mFilePath~~??",mFilePath); ///storage/emulated/0/progress_recorder/

//        mBtnStartRec = (Button) findViewById(R.id.btnStartRec);
//        mBtnStartPlay = (Button) findViewById(R.id.btnStartPlay);
//        mRecProgressBar = (SeekBar) findViewById(R.id.recProgressBar);
//        mPlayProgressBar = (SeekBar) findViewById(R.id.playProgressBar);
//        mTvPlayMaxPoint = (TextView) findViewById(R.id.tvPlayMaxPoint);

        mBtnStartRec.setOnClickListener(this);
        mBtnStartPlay.setOnClickListener(this);


        Log.i("~~progrRecorder~~mRoot",mFilePath);
        String[] fileList = getFileList(mFilePath);
        for(int i=0; i < fileList.length; i++)
        {
            Log.d("~~~~fileList[i]~~~", fileList[i]);
            mDatas.add(fileList[i]); //리스트뷰에 디렉터리에 있는 파일띄우기
        }
        //fileList다음으로 등록할 recordFile명 지정
        if(fileList.length!=0){
            newRecordFile="recordFile"+String.valueOf(fileList.length+1);
            Log.d("~~~~newrecordFile명::",newRecordFile);
        }

        //ListView가 보여줄 뷰를 만들어내는 Adapter 객체 생성
        //ArrayAdapter : 문자열 데이터들을 적절한 iew로 1:1로 만들어서 List형태로 ListView에 제공하는 객체
        //첫번째 파라미터 : Contextf객체 ->MainActivity가 Context를 상속했기 때문에 this로 제공 가능
        //두번째 파라미터 : 문자열 데이터를 보여줄 뷰. ListView에 나열되는 하나의 아이템 단위의 뷰 모양
        //세번째 파라미터 : adapter가 뷰로 만들어줄 대량의 데이터들
        //본 예제에서는 문자열만 하나씩 보여주면 되기 때문에 두번째 파라미터의 뷰 모먕은 Android 시스템에서 제공하는
        //기본 Layout xml 파일을 사용함.
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mDatas);
        listview = (ListView) findViewById(R.id.listview_rec);
        listview.setAdapter(adapter); //위에 만들어진 Adapter를 ListView에 설정 : xml에서 'entries'속성

        //ListView의 아이템 하나가 클릭되는 것을 감지하는 Listener객체 설정 (Button의 OnClickListener와 같은 역할)
        listview.setOnItemClickListener(listener);


    }

    // 특정 폴더의 파일 목록을 구해서 반환
    public String[] getFileList(String strPath) {
        // 폴더 경로를 지정해서 File 객체 생성
        File fileRoot = new File(strPath);
        // 해당 경로가 폴더가 아니라면 함수 탈출
        if( fileRoot.isDirectory() == false ) {
            Log.i("getFileList~~","해당 경로가 폴더가 아닙니다");
            return null;
        }
        // 파일 목록을 구한다
        String[] fileList = fileRoot.list();
        Log.i("~~~getfileList~~Count","fileList의 갯수는 "+ fileList.length);
        return fileList;
    }

//    public void Thread() {
//        Runnable task = new Runnable() {
//            public void run() {
//
//                while (mPlayer.isPlaying()) {
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//
//                }
//            }
//        };
//        Thread thread = new Thread(task);
//        thread.start();
//    }

    //ListView의 아이템 하나가 클릭되는 것을 감지하는 Listener객체 생성 (Button의 OnClickListener와 같은 역할)
    AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {

        //ListView의 아이템 중 하나가 클릭될 때 호출되는 메소드
        //첫번째 파라미터 : 클릭된 아이템을 보여주고 있는 AdapterView 객체(여기서는 ListView객체)
        //두번째 파라미터 : 클릭된 아이템 뷰
        //세번째 파라미터 : 클릭된 아이템의 위치(ListView이 첫번째 아이템(가장위쪽)부터 차례대로 0,1,2,3.....)
        //네번재 파리미터 : 클릭된 아이템의 아이디(특별한 설정이 없다면 세번째 파라이터인 position과 같은 값)
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // TODO Auto-generated method stub

            //재생되는지 테스팅
            try {
                mBtnStartPlayOnClick(mDatas.get(position));
                sendData(String.valueOf(position));

            } catch (IOException e) {
                e.printStackTrace();
            }
            //클릭된 아이템의 위치를 이용하여 데이터인 문자열을 Toast로 출력
            Toast.makeText(ControlArduino.this, mDatas.get(position), Toast.LENGTH_SHORT).show();

//            switch (position) {
//                case 0:
//                    //재생되는지 테스팅
//                    mBtnStartPlayOnClick(mFileName);
//                    //클릭된 아이템의 위치를 이용하여 데이터인 문자열을 Toast로 출력
//                    Toast.makeText(ProgressRecorder.this, mDatas.get(position), Toast.LENGTH_SHORT).show();
//                    break;

//                case 1:
//                    //재생되는지 테스팅
//                    mBtnStartPlayOnClick(mFileTemp);
//                    //클릭된 아이템의 위치를 이용하여 데이터인 문자열을 Toast로 출력
//                    Toast.makeText(ProgressRecorder.this, mDatas.get(position), Toast.LENGTH_SHORT).show();
//                    break;

//            }
        }
    };


    // 버튼의 OnClick 이벤트 리스너
    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.btnStartRec:
//
//                mBtnStartRecOnClick();
//                break;
//            case R.id.btnStartPlay:
//                try {
//                    mBtnStartPlayOnClick(mFileName);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                break;
//            default:
//                break;
//        }
    }

    //REC버튼 눌렀을때 녹음 시작 및 종료 메서드(리스트뷰에 리스트 추가)
    private void mBtnStartRecOnClick() {
        //녹음 시작
        if (mRecState == REC_STOP) {
            mRecState = RECORDING;
            startRec();
            updateUI();


            //리스트뷰 문자열 데이터 ArrayList에 추가
            mDatas.add(newRecordFile+".amr");
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mDatas);
            listview = (ListView) findViewById(R.id.listview_rec);
            listview.setAdapter(adapter); //위에 만들어진 Adapter를 ListView에 설정 : xml에서 'entries'속성
            //ListView의 아이템 하나가 클릭되는 것을 감지하는 Listener객체 설정 (Button의 OnClickListener와 같은 역할)
            listview.setOnItemClickListener(listener);


        }
        //녹음 종료
        else if (mRecState == RECORDING) {


            mRecState = REC_STOP;
            stopRec();
            updateUI();
        }
    }


    // 녹음 시작 메서드
    private void startRec() {
        mCurRecTimeMs = 0;
        mCurProgressTimeDisplay = 0;

        // SeekBar의 상태를 0.1초후 체크 시작
//        mProgressHandler.sendEmptyMessageDelayed(0, 100);

        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.reset();
        } else {
            mRecorder.reset();
        }

        try {
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mRecorder.setOutputFile(mFilePath+newRecordFile+".amr"); //newRecordFile명의 음성파일에 음성 녹음.
            Log.d("setoutputFile문제??",newRecordFile+".amr");
            mRecorder.prepare();
            mRecorder.start();
        } catch (IllegalStateException e) {
            //Toast.makeText(this, "IllegalStateException", 1).show();
        } catch (IOException e) {
            //Toast.makeText(this, "IOException이뭔데", 1).show();
        }
    }

    // 녹음정지
    private void stopRec() {
        try {
            mRecorder.stop();
        } catch (Exception e) {
        } finally {
            mRecorder.release();
            mRecorder = null;
        }

        mCurRecTimeMs = -999;
        // SeekBar의 상태를 즉시 체크
//        mProgressHandler.sendEmptyMessageDelayed(0, 0);
    }

    private void mBtnStartPlayOnClick(String mFileName) throws IOException{
        if (mPlayerState == PLAY_STOP) {
            mPlayerState = PLAYING;
            startPlay(mFileName);
            updateUI();

        } else if (mPlayerState == PLAYING) {
            mPlayerState = PLAY_STOP;
            stopPlay();
            updateUI();
        }
    }

    // 재생 시작
    private void startPlay(String mFileName) {
        // 미디어 플레이어 생성
        if (mPlayer == null)
            mPlayer = new MediaPlayer();
        else
            mPlayer.reset();

        mPlayer.setOnCompletionListener(this);

        String fullFilePath = mFilePath + mFileName;
        Log.v("ProgressRecorder", "녹음파일명 ==========> " + fullFilePath);

        try {
            mPlayer.setDataSource(fullFilePath);
            mPlayer.prepare();
            int point = mPlayer.getDuration();
            mPlayProgressBar.setMax(point);

            int maxMinPoint = point / 1000 / 60;
            int maxSecPoint = (point / 1000) % 60;
            String maxMinPointStr = "";
            String maxSecPointStr = "";

            if (maxMinPoint < 10)
                maxMinPointStr = "0" + maxMinPoint + ":";
            else
                maxMinPointStr = maxMinPoint + ":";

            if (maxSecPoint < 10)
                maxSecPointStr = "0" + maxSecPoint;
            else
                maxSecPointStr = String.valueOf(maxSecPoint);

            mTvPlayMaxPoint.setText(maxMinPointStr + maxSecPointStr);
        } catch (Exception e) {
            Log.v("ProgressRecorder", "미디어 플레이어 Prepare Error ==========> " + e);
        }

        if (mPlayerState == PLAYING) {
            mPlayProgressBar.setProgress(0);

            try {
                // SeekBar의 상태를 0.1초마다 체크
//                mProgressHandler2.sendEmptyMessageDelayed(0, 100);
                mPlayer.start();
            } catch (Exception e) {
                //Toast.makeText(this, "error : " + e.getMessage(), 0).show();
            }
        }
    }

    //재생 중지
    private void stopPlay() {
        // 재생을 중지하고
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
        mPlayProgressBar.setProgress(0);

        // 즉시 SeekBar 메세지 핸들러를 호출한다.
//        mProgressHandler2.sendEmptyMessageDelayed(0, 0);
    }

    public void onCompletion(MediaPlayer mp) {
        mPlayerState = PLAY_STOP; // 재생이 종료됨

        // 재생이 종료되면 즉시 SeekBar 메세지 핸들러를 호출한다.
//        mProgressHandler2.sendEmptyMessageDelayed(0, 0);
    }

    private void updateUI() {

        if (mRecState == REC_STOP) {
            mBtnStartRec.setText("Rec");
            mRecProgressBar.setProgress(0);
        } else if (mRecState == RECORDING)
            mBtnStartRec.setText("Stop");

        if (mPlayerState == PLAY_STOP) {
            mBtnStartPlay.setText("Play");
            mPlayProgressBar.setProgress(0);
        } else if (mPlayerState == PLAYING)
            mBtnStartPlay.setText("Stop");

    }
    
    void sendCommand(String msg) throws IOException
    {
        msg += "\n";
        mmOutputStream.write(msg.getBytes());
        //info_textview.setText("Data Sent");
    }
    
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
                	startActivity(new Intent(ControlArduino.this, MainActivity.class));
                    
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
        
        beginListenForData();
        
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
    
    void beginListenForData()
    {
        final Handler handler = new Handler(); 
        final byte delimiter = 10; //This is the ASCII code for a newline character
        
        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {                
               while(!Thread.currentThread().isInterrupted() && !stopWorker)
               {
            	  
            	   if(!is_connected)
            	   {
            		   continue;
            	   }
            		   

                    try 
                    {
                        int bytesAvailable = mmInputStream.available();                        
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "EUC-KR");
                                    readBufferPosition = 0;
                                    
                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                        	
                                        	info_textview.setText(data);

                                        	String tmp2 = (String)data;
                                        	
                                        	
                                        	String tmp = (String)info_textview.getText();
                                        	
                                        	info_textview.setText(tmp2);
                                        	
                                        	if( tmp.indexOf("blocked") != -1) 
                                        	{
                                        		 speakOut("blocked"); 
                                        	
                                        	}
                                        	else if( tmp.indexOf("cleared") != -1) 
                                        	{
                                        		 speakOut("cleared"); 
                                        	
                                        	}
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } 
                    catch (IOException ex) 
                    {
                        stopWorker = true;
                    }
                    
                    try {

          		      Thread.sleep(100);

          		} catch (InterruptedException e) { }
               }
            }
        });

        workerThread.start();
    }

}
