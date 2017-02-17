package com.spinytech.maindemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.spinytech.macore.router.MaActionResult;
import com.spinytech.macore.MaApplication;
import com.spinytech.macore.router.LocalRouter;
import com.spinytech.macore.router.RouterRequestUtil;
import com.spinytech.macore.Song;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.main_local_sync_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    LocalRouter.getInstance(MaApplication.getMaApplication())
                            .route(MainActivity.this, RouterRequestUtil.obtain(MainActivity.this).provider("main")
                                    .action("sync")
                                    .data("1", "Hello")
                                    .data("2", "World"))
                            .subscribeOn(Schedulers.from(ThreadPool.getThreadPoolSingleton()))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<MaActionResult>() {
                                @Override
                                public void accept(MaActionResult maActionResult) throws Exception {
                                    Toast.makeText(MainActivity.this, maActionResult.getMsg(), Toast.LENGTH_SHORT).show();
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                                }
                            });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.main_local_async_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    LocalRouter.getInstance(MaApplication.getMaApplication())
                            .route(MainActivity.this, RouterRequestUtil.obtain(MainActivity.this).provider("main")
                                    .action("async")
                                    .data("1", "Hello")
                                    .data("2", "World"))
                            .subscribeOn(Schedulers.from(ThreadPool.getThreadPoolSingleton()))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<MaActionResult>() {
                                @Override
                                public void accept(MaActionResult maActionResult) throws Exception {
                                    Toast.makeText(MainActivity.this, maActionResult.getMsg(), Toast.LENGTH_SHORT).show();
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                                }
                            });

                    Toast.makeText(MainActivity.this, "please wait", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        findViewById(R.id.main_play_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    LocalRouter.getInstance(MaApplication.getMaApplication())
                            .route(MainActivity.this, RouterRequestUtil.obtain(MainActivity.this)
                                    .domain("com.spinytech.maindemo:music")
                                    .provider("music")
                                    .action("play")
                                    .reqeustObject(new Song("see you"))
                            )
                            .subscribeOn(Schedulers.from(ThreadPool.getThreadPoolSingleton()))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<MaActionResult>() {
                                @Override
                                public void accept(MaActionResult maActionResult) throws Exception {
                                    Toast.makeText(MainActivity.this, maActionResult.getMsg(), Toast.LENGTH_SHORT).show();
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.main_stop_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    LocalRouter.getInstance(MaApplication.getMaApplication())
                            .route(MainActivity.this, RouterRequestUtil.obtain(MainActivity.this)
                                    .domain("com.spinytech.maindemo:music")
                                    .provider("music")
                                    .action("stop"))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.from(ThreadPool.getThreadPoolSingleton()))
                            .subscribe(new Consumer<MaActionResult>() {
                                @Override
                                public void accept(MaActionResult maActionResult) throws Exception {
                                    Toast.makeText(MainActivity.this, maActionResult.getMsg(), Toast.LENGTH_SHORT).show();
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        findViewById(R.id.main_music_shutdown_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    LocalRouter.getInstance(MaApplication.getMaApplication())
                            .route(MainActivity.this, RouterRequestUtil.obtain(MainActivity.this)
                                    .domain("com.spinytech.maindemo:music")
                                    .provider("music")
                                    .action("shutdown"))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.from(ThreadPool.getThreadPoolSingleton()))
                            .subscribe(new Consumer<MaActionResult>() {
                                @Override
                                public void accept(MaActionResult maActionResult) throws Exception {
                                    Toast.makeText(MainActivity.this, maActionResult.getMsg(), Toast.LENGTH_SHORT).show();
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.main_wide_shutdown_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalRouter.getInstance(MaApplication.getMaApplication()).stopWideRouter();
            }
        });
        findViewById(R.id.main_pic_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    LocalRouter.getInstance(MaApplication.getMaApplication())
                            .route(MainActivity.this, RouterRequestUtil.obtain(MainActivity.this)
                                    .domain("com.spinytech.maindemo:pic")
                                    .provider("pic")
                                    .action("pic")
                                    .data("is_big", "0"))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.from(ThreadPool.getThreadPoolSingleton()))
                            .subscribe(new Consumer<MaActionResult>() {
                                @Override
                                public void accept(MaActionResult maActionResult) throws Exception {
                                    Toast.makeText(MainActivity.this, maActionResult.getMsg(), Toast.LENGTH_SHORT).show();
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.main_big_pic_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    LocalRouter.getInstance(MaApplication.getMaApplication())
                            .route(MainActivity.this, RouterRequestUtil.obtain(MainActivity.this)
                                    .domain("com.spinytech.maindemo:pic")
                                    .provider("pic")
                                    .action("pic")
                                    .data("is_big", "1"))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.from(ThreadPool.getThreadPoolSingleton()))
                            .subscribe(new Consumer<MaActionResult>() {
                                @Override
                                public void accept(MaActionResult maActionResult) throws Exception {
                                    Toast.makeText(MainActivity.this, maActionResult.getMsg(), Toast.LENGTH_SHORT).show();
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.main_web_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    LocalRouter.getInstance(MaApplication.getMaApplication())
                            .route(MainActivity.this, RouterRequestUtil.obtain(MainActivity.this)
                                    .provider("web")
                                    .action("web")
                            );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
