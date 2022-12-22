package com.ulang.nts;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;

public class PlayActivity extends AppCompatActivity {
    //private static byte[] data;
    private WebSocket webSocket;
    private PlayActivity.MyWebSocketListener socketListener;
    private final static String TAG = "NLPService";
    private int mOutput = AudioManager.STREAM_SYSTEM;
    private int mSamplingRate = 16000;
    private final String sttApi = NetRequest.BASE_URL;

    @BindView(R.id.btn_button_startplay)
    Button btn_start;
    @BindView(R.id.btn_button_save)
    Button btn_save;
    @BindView(R.id.btn_button_finish)
    Button btn_finish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        ButterKnife.bind(this);
        socketListener = new PlayActivity.MyWebSocketListener();

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread() {
                    @Override
                    public void run() {
                        start_play();
                    }
                }.start();

            }
        });
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        DownloadSmallFile(sttApi);
                        Looper.prepare();
                        Toast toast=Toast.makeText(getApplicationContext(), "file is downloaded to the folder:"+AudioFileUtils.getDownWavFilePath(), Toast.LENGTH_SHORT);
                        toast.show();
                        Looper.loop();
                    }
                }.start();
            }
        });
        btn_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PlayActivity.this, MainActivity.class));
            }
        });
    }

    public void start_play(){
        OkHttpClient client = NetRequest.getOkHttpClient();
        Request request = new Request.Builder().url(sttApi).build();
        NetRequest.getOkHttpClient().newWebSocket(request, socketListener);
//        try {
//            Log.d(TAG, "audioTrack start ");
//            Log.d(TAG, String.valueOf(data.length));
//            int bufferSizeInBytes = AudioTrack.getMinBufferSize(AudioFileUtils.AUDIO_SAMPLE_RATE,
//                    AudioFileUtils.CHANNEL_CONFIG, AudioFileUtils.AUDIO_FORMAT);
//            AudioTrack audioTrack = new AudioTrack(mOutput, mSamplingRate,
//                    AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
//                    data.length, AudioTrack.MODE_STREAM);
//            audioTrack.write(data, 0, data.length);
//            audioTrack.play();
//            while (audioTrack.getPlaybackHeadPosition() < (data.length / 2)) {
//                Thread.yield();
//            }
//            Log.d(TAG, "audioTrack stop ");
//            audioTrack.stop();
//            audioTrack.release();
//            //MediaPlayer mp = MediaPlayer.create(this, data);
//        } catch (IllegalArgumentException e) {
//            Log.d(TAG, e.getMessage());
//        }
    }

    public boolean DownloadSmallFile(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url+"/wav/"+MainActivity.time1).build();

        try {
            okhttp3.Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                return false;
            }

            ResponseBody body = response.body();
            long contentLength = body.contentLength();
            Log.d(TAG, "contentLength: " + contentLength);
            BufferedSource source = body.source();
            File file = new File(AudioFileUtils.getDownWavFilePath());
            Log.d(TAG, "Path: "+AudioFileUtils.getDownWavFilePath());
            BufferedSink sink = Okio.buffer(Okio.sink(file));
            sink.writeAll(source);
            sink.flush();
            Log.d(TAG, "Finish");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    class MyWebSocketListener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            output("onOpen: " + "webSocket connect success");
            PlayActivity.this.webSocket = webSocket;
            PlayActivity.this.webSocket.send(MainActivity.time1);

        }

        @Override
        public void onMessage(WebSocket webSocket, final String text) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    sttTv.setText("Stt result:" + text);
//                }
//            });
            output("onMessage1: " + text);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            runOnUiThread(new Runnable() {
                @Override
                public void run(){
                    final byte[] data = bytes.toByteArray();
                    try {
                        Log.d(TAG, "audioTrack start ");
                        Log.d(TAG, String.valueOf(data.length));
                        int bufferSizeInBytes = AudioTrack.getMinBufferSize(AudioFileUtils.AUDIO_SAMPLE_RATE,
                                AudioFileUtils.CHANNEL_CONFIG, AudioFileUtils.AUDIO_FORMAT);
                        AudioTrack audioTrack = new AudioTrack(mOutput, mSamplingRate,
                                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                                data.length, AudioTrack.MODE_STREAM);
                        audioTrack.write(data, 0, data.length);
                        audioTrack.play();
                        while (audioTrack.getPlaybackHeadPosition() < (data.length / 2)) {
                            Thread.yield();
                        }
                        Log.d(TAG, "audioTrack stop ");
                        audioTrack.stop();
                        audioTrack.release();
                        //MediaPlayer mp = MediaPlayer.create(this, data);
                    } catch (IllegalArgumentException e) {
                        Log.d(TAG, e.getMessage());

                    }
                    PlayActivity.this.webSocket.send("close");
                }
            });
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
