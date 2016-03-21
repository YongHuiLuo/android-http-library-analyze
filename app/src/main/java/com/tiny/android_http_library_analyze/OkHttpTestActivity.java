package com.tiny.android_http_library_analyze;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created JackLuo
 * 实现主要功能：
 * 创建时间： on 2016/3/3.
 * 修改者： 修改日期： 修改内容：
 */
public class OkHttpTestActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView txtResult;
    private Button btn_get, btn_post, btn_sync, btn_async;
    private String URL = "http://fanyi.youdao.com/openapi.do?keyfrom=tiny123456com&key=1021954366&type=data&doctype=json&version=1.1&q=good";
    private int maxConnections = 20;
    private volatile int maxRequestCount = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ok_http);
        txtResult = (TextView) findViewById(R.id.txt_result);
        btn_get = (Button) findViewById(R.id.btn_get);
        btn_post = (Button) findViewById(R.id.btn_post);
        btn_sync = (Button) findViewById(R.id.btn_sync);
        btn_async = (Button) findViewById(R.id.btn_async);

        setListener();
    }

    private void setListener() {
        btn_get.setOnClickListener(this);
        btn_post.setOnClickListener(this);
        btn_sync.setOnClickListener(this);
        btn_async.setOnClickListener(this);
    }

    //========================================== Get URL Start=================================

    int mTotalCount = 0;
    int mErrorCount = 0;
    int mSuccessCount = 0;
    double beginTime;
    OkHttpClient client = new OkHttpClient();

    private void getAsyncUrl() {
        beginTime = System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < maxRequestCount; i++) {
                        String response = asyncGet("http://www.baidu.com");
                        Log.d("tiny", "i --" + i);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getSyncUrl() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                beginTime = System.currentTimeMillis();
                for (int i = 0; i < maxRequestCount; i++) {
                    String response = null;
                    try {
                        response = syncGet("http://www.baidu.com");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d("tiny", "response --" + response);
                }
                Log.d("tiny", "response -- 完成 -- avg --"  + (System.currentTimeMillis() - beginTime)/maxRequestCount);
            }
        }).start();

    }

    private String asyncGet(String url) throws IOException {
        final String[] result = new String[1];
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("tiny", "onFailure");
                mErrorCount++;
                mTotalCount++;
                if (mTotalCount == maxRequestCount) {
                    Log.d("tiny", "mTotalCount --" + mTotalCount + ";mSuccessCount --" + mSuccessCount + ";mErrorCount--" + mErrorCount + ";avg --" + (System.currentTimeMillis() - beginTime) / mTotalCount);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("tiny", "onResponse" + response);
                mSuccessCount++;
                mTotalCount++;
                result[0] = response.body().string();
                if (mTotalCount == maxRequestCount) {
                    Log.d("tiny", "mTotalCount --" + mTotalCount + ";mSuccessCount --" + mSuccessCount + ";mErrorCount--" + mErrorCount + ";avg --" + (System.currentTimeMillis() - beginTime) / mTotalCount);
                }
            }
        });
        return result[0];
    }

    private String syncGet(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    //========================================== Get URL End =================================

    //========================================== Post To A Server Start=================================

    private final static MediaType Json = MediaType.parse("application/json; charset=utf-8");

    private void postToServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 100; i++) {
                        String response = post("http://www.baidu.com","");
                        Log.d("tiny", "post response --" + response);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(Json, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();

        return response.body().string();
    }

    String createJson(int groupId, int postId) {
        JSONObject json = null;
        try {
            json = new JSONObject();
            json.put("gid", groupId);
            json.put("pid", postId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json.toString();
    }

    //==========================================  Post To A Server End =================================

    private void AsyncHttpRequestTest() {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sync:
                getSyncUrl();
                break;
            case R.id.btn_async:
                getAsyncUrl();
                break;
            case R.id.btn_get:
                break;
            case R.id.btn_post:
                postToServer();
                break;
        }
    }
}
