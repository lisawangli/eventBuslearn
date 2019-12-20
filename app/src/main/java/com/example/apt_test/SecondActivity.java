package com.example.apt_test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class SecondActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        findViewById(R.id.getmes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
                        EventBus.getDefault().post("hello");
                        Log.e("SecondActivity",Thread.currentThread().getName());

//                    }
//                }).start();
            }
        });
    }
}
