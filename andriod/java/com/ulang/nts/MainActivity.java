package com.ulang.nts;

import android.content.Intent;
import android.media.AudioRecord;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MainActivity extends AppCompatActivity {
    public static String time1;
    private AudioRecord audioRecord;
    private int bufferSizeInBytes = 0;
    private volatile boolean isRecord = false;// 设置正在录制的状态
    private WebSocket webSocket;
    private MainActivity.MyWebSocketListener socketListener;
    private final String TAG = NetRequest.TAG;
    private final String sttApi = NetRequest.BASE_URL;

//    @BindView(R.id.stt_btn_local)
//    Button sttBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        socketListener = new MainActivity.MyWebSocketListener();
    }

    @OnClick({R.id.ns_red})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ns_red:
                //startActivity(new Intent(this,EndReActivity.class));
                createWebSocket();
                setContentView(R.layout.activity_endre);
                Button buttonBack1=findViewById(R.id.ns_red_end);
                buttonBack1.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        //setContentView(R.layout.activity_main);
                        stopRecordAndFile();
                        Log.d(TAG, "stopRecordAndFile");
                        startActivity(new Intent(MainActivity.this, ProcessActivity.class));
                    }
                });
                break;

        }
    }

    public int startRecordAndFile() {
        Log.d("NLPService", "startRecordAndFile");
        // 判断是否有外部存储设备sdcard
        if (AudioFileUtils.isSdcardExit()) {
            if (isRecord) {
                return ErrorCode.E_STATE_RECODING;
            } else {
                if (audioRecord == null) {
                    createAudioRecord();
                }
                audioRecord.startRecording();
                // 让录制状态为true
                isRecord = true;
                // 开启音频文件写入线程
                new Thread(new MainActivity.AudioRecordThread()).start();
                return ErrorCode.SUCCESS;
            }

        } else {
            return ErrorCode.E_NOSDCARD;
        }

    }

    private void createAudioRecord() {
        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(AudioFileUtils.AUDIO_SAMPLE_RATE,
                AudioFileUtils.CHANNEL_CONFIG, AudioFileUtils.AUDIO_FORMAT);
        //printf(bufferSizeInBytes);
        // 创建AudioRecord对象
        // MONO单声道，STEREO为双声道立体声
        audioRecord = new AudioRecord(AudioFileUtils.AUDIO_INPUT, AudioFileUtils.AUDIO_SAMPLE_RATE,
                AudioFileUtils.CHANNEL_CONFIG, AudioFileUtils.AUDIO_FORMAT, bufferSizeInBytes);
    }

    private void stopRecordAndFile() {
        if (audioRecord != null) {
            isRecord = false;// 停止文件写入
            audioRecord.stop();
            audioRecord.release();// 释放资源
            audioRecord = null;
        }

    }


    class AudioRecordThread implements Runnable {

        @Override
        public void run() {
            ByteBuffer audioBuffer = ByteBuffer.allocateDirect(bufferSizeInBytes * 20).order(ByteOrder.LITTLE_ENDIAN);
            int readSize = 0;
            Log.d(TAG, "isRecord=" + isRecord);
            while (isRecord) {
                readSize = audioRecord.read(audioBuffer, audioBuffer.capacity());
                //readSize = audioRecord.read(audioBuffer, audioBuffer.limit());
                if (readSize == AudioRecord.ERROR_INVALID_OPERATION || readSize == AudioRecord.ERROR_BAD_VALUE) {
                    Log.d("NLPService", "Could not read audio data.");
                    break;
                }
                //System.out.println(audioBuffer.get());
                boolean send = webSocket.send(ByteString.of(audioBuffer));
                Log.d("NLPService", "send=" + send);
                audioBuffer.clear();
            }
            webSocket.send("close");
        }
    }

    private void createWebSocket() {
        Request request = new Request.Builder().url(sttApi).build();
        NetRequest.getOkHttpClient().newWebSocket(request, socketListener);
    }

    class MyWebSocketListener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            output("onOpen: " + "webSocket connect success");
            MainActivity.this.webSocket = webSocket;
            MainActivity.this.webSocket.send("1");
            startRecordAndFile();
        }

        @Override
        public void onMessage(WebSocket webSocket, final String text) {
            time1 = text;
            output("onMessage1: " + text);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            output("onMessage2 byteString: " + bytes);
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            output("onClosing: " + code + "/" + reason);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            output("onClosed: " + code + "/" + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            output("onFailure: " + t.getMessage());
        }

        private void output(String s) {
            Log.d("NLPService", s);
        }

    }
}
