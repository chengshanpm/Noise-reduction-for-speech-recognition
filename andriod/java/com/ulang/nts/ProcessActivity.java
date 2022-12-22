package com.ulang.nts;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class ProcessActivity extends AppCompatActivity {
    private WebSocket webSocket;
    private ProcessActivity.MyWebSocketListener socketListener;
    private final String TAG = NetRequest.TAG;
    private final String sttApi = NetRequest.BASE_URL;

    @BindView(R.id.tw_process)
    TextView tw;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);
        ButterKnife.bind(this);
        socketListener = new ProcessActivity.MyWebSocketListener();

        createWebSocket();

    }

    private void createWebSocket() {
        Request request = new Request.Builder().url(sttApi).build();
        NetRequest.getOkHttpClient().newWebSocket(request, socketListener);
    }

    class MyWebSocketListener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            output("onOpen: " + "webSocket connect success");
            ProcessActivity.this.webSocket = webSocket;
            ProcessActivity.this.webSocket.send("2");
            ProcessActivity.this.webSocket.send(MainActivity.time1);
        }

        @Override
        public void onMessage(WebSocket webSocket, final String text) {
            output("onMessage1: " + text);
            ProcessActivity.this.webSocket.send("close");
            startActivity(new Intent(ProcessActivity.this, PlayActivity.class));
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
