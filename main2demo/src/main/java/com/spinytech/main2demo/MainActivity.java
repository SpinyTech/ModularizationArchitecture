package com.spinytech.main2demo;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.spinytech.macore.MaApplication;
import com.spinytech.macore.router.LocalRouter;
import com.spinytech.macore.router.RouterRequest;
import com.spinytech.macore.router.RouterResponse;

public class MainActivity extends AppCompatActivity {

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.main2_play_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final long startTime = System.currentTimeMillis();
                final RouterRequest request = new RouterRequest.Builder(getApplicationContext())
                        .domain("com.spinytech.maindemo:music")
                        .provider("music")
                        .action("play")
                        .build();
                try {
                    final RouterResponse response = LocalRouter.getInstance((MaApplication) getApplication())
                            .route(MainActivity.this, request);
                    response.isAsync();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final String temp = response.getData();
                                final long time = System.currentTimeMillis() - startTime;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Toast.makeText(MainActivity.this, "async:" + response.isAsync() + " cost:" + time + " response:" + response.get(), Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.main2_stop_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final long startTime = System.currentTimeMillis();
                RouterRequest request = new RouterRequest.Builder(getApplicationContext())
                        .domain("com.spinytech.maindemo:music")
                        .provider("music")
                        .action("stop")
                        .build();
                try {
                    final RouterResponse response = LocalRouter.getInstance((MaApplication) getApplication())
                            .route(MainActivity.this, request);
                    response.isAsync();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final String temp = response.getData();
                                final long time = System.currentTimeMillis() - startTime;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Toast.makeText(MainActivity.this, "async:" + response.isAsync() + " cost:" + time + " response:" + response.get(), Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
