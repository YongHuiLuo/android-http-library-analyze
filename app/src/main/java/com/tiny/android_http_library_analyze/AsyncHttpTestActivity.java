package com.tiny.android_http_library_analyze;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.SyncHttpClient;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import cz.msebera.android.httpclient.Header;

/**
 * Created JackLuo
 * 实现主要功能：
 * 创建时间： on 2016/1/19.
 * 修改者： 修改日期： 修改内容：
 */
public class AsyncHttpTestActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView txtResult;
    private Button btn_sync, btn_async;
    //private String URL = "http://fanyi.youdao.com/openapi.do?keyfrom=tiny123456com&key=1021954366&type=data&doctype=json&version=1.1&q=good";
    private String URL = "http://baidu.com";
    private int maxConnections = 20;
    private volatile int maxRequestCount = 10000;
    private volatile int requestSuccess = 0;

    private volatile int mSyncSuccess = 0;
    private volatile int mSyncError = 0;
    private int mErrorCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_async_http);
        txtResult = (TextView) findViewById(R.id.txt_result);
        btn_sync = (Button) findViewById(R.id.btn_sync);
        btn_async = (Button) findViewById(R.id.btn_async);

        setListener();
    }

    private void setListener() {
        btn_sync.setOnClickListener(this);
        btn_async.setOnClickListener(this);
    }


    //================================= 同步请求代码 开始 =================================

    private long beginTime;

    /**
     * 同步HttpClient请求
     */
    private void SyncHttpRequestTest() {
        final SyncHttpClient httpClient = new SyncHttpClient();
        //httpClient.setMaxConnections(maxConnections);

        beginTime = System.currentTimeMillis();

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < maxRequestCount; i++) {
                    executeSyncThread(httpClient, i);
                }
            }
        }).start();


//        while (requestSuccess < maxRequestCount) {
//            try {
//                Thread.sleep(1);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        Log.d("tiny", "requestSuccess = " + requestSuccess + "; avg " + (System.currentTimeMillis() - beginTime) / requestSuccess);
    }

    private void executeSyncThread(final AsyncHttpClient client, final int position) {
        if (client instanceof AsyncHttpClient) {
            Log.i("tiny", "syncHttpClient position =" + position + ";currentThread = " + Thread.currentThread());
            client.get(getApplicationContext(), URL, syncHandler);
        }
    }

    int mSyncTotal = 0;

    AsyncHttpResponseHandler syncHandler = new AsyncHttpResponseHandler() {

        @Override
        public void onStart() {
            super.onStart();
            Log.d("tiny", "onStart");
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            Log.d("tiny", "onSuccess");
            mSyncSuccess++;
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Log.d("tiny", "onFailure");
            mSyncError++;
        }

        @Override
        public void onRetry(int retryNo) {
            super.onRetry(retryNo);
            Log.d("tiny", "onRetry");
        }

        @Override
        public void onFinish() {
            super.onFinish();
            mSyncTotal++;
            if (mSyncTotal >= maxRequestCount) {
                //txtResult.setText("同步请求：成功 " + mSyncSuccess + "个，失败 " + mSyncError + "个，avg" + (System.currentTimeMillis() - beginTime) / mSyncSuccess);
                Log.d("tiny", "mSyncSuccess = " + mSyncSuccess + ";mSyncError" + mSyncError + "; avg " + (System.currentTimeMillis() - beginTime) / mSyncSuccess);
            }
        }
    };

    //================================= 同步请求代码 结束 =================================

    //================================= 异步请求代码块 开始 =================================

    /**
     * 异步HttpClient请求
     */
    private void AsyncHttpRequestTest() {
        beginTime = System.currentTimeMillis();
        final AsyncHttpClient client = new AsyncHttpClient();
        client.setThreadPool(executor);

        for (int i = 0; i < maxRequestCount; i++) {
            Log.d("tiny", "i --" + i);
            executeAsyncThread(client, i);
        }
    }

    ExecutorService executor = Executors.newSingleThreadExecutor();
    private int countTotal = 0;

    private void executeAsyncThread(final AsyncHttpClient client, final int position) {
        final AsyncHttpResponseHandler asyncHandler = new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d("tiny", " onSuccess response --" + responseBody);
                requestSuccess++;
                txtResult.setText("成功返回");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("tiny", "onFailure response --" + responseBody);
                mErrorCount++;
            }

            @Override
            public void onFinish() {
                super.onFinish();
                countTotal++;
                Log.d("tiny", "onFinish");
                if (countTotal >= maxRequestCount)
                    Log.d("tiny", "requestSuccess = " + requestSuccess + "; avg " + (System.currentTimeMillis() - beginTime) / requestSuccess);
            }
        };

        FutureTask<RequestHandle> futureTask = new FutureTask<RequestHandle>(new Callable<RequestHandle>() {
            @Override
            public RequestHandle call() throws Exception {
                Log.d("tiny", "Executing Get request on background position" + position);
                return client.get(URL, asyncHandler);
            }
        });

        executor.submit(futureTask);

        RequestHandle handle;
        try {
            handle = futureTask.get(100, TimeUnit.SECONDS);
            if (handle == null || handle.isCancelled()) {
                Log.d("tiny", "task run error >>" + position);
                mErrorCount++;
            } else {
                Log.d("tiny", "task run success >>" + position);
            }
        } catch (InterruptedException e) {
            Log.d("tiny", "Exception");
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    //================================= 异步请求代码块 结束 =================================

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sync:
                SyncHttpRequestTest();
                break;
            case R.id.btn_async:
                AsyncHttpRequestTest();
                break;
        }
    }
}