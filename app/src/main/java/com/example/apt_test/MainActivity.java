package com.example.apt_test;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.apt_annotation.BindView;
import com.example.apt_annotation.onClick;
import com.example.apt_library.BindViewTools;

import java.util.Map;


public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv1)
    TextView tv1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BindViewTools.bind(this);
        tv1.setText("注解获取成功");
        EventBus.getDefault().register(this);

    }

    @onClick(R.id.tv1)
    public void onClick(){
        startActivity(new Intent(this,SecondActivity.class));
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void getMessage(String s){
        Log.e("MainActivity",Thread.currentThread().getName()+"====s====="+s);

    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unreister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
