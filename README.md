## 说明
从[SpinyTech/ModularizationArchitecture](https://github.com/SpinyTech/ModularizationArchitecture)fork的代码

关于架构说明可以参考
[Android架构思考(模块化、多进程)](http://blog.spinytech.com/2016/12/28/android_modularization/)

关于这个fork版本增加了一些新的特性，方便使用，文档地址：[Android组件化之通信（多模块，多进程）](http://www.jianshu.com/p/1fc5f8a2d703)

## 使用教程
项目地址：[https://github.com/wutongke/ModularizationArchitecture](https://github.com/wutongke/ModularizationArchitecture)
### 1 在项目中集成
1.1 在project的build.gradle中设置maven地址:
```
allprojects {
    repositories {
        jcenter()
        maven{
            url 'https://dl.bintray.com/wutongke/maven'
        }
    }
}
```

dependencies块中支持apt：
```
classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
```
1.2 所有Module中配置apt插件：
```
apply plugin: 'com.neenbedankt.android-apt'
```
dependencies块中设置：
```
 apt 'com.github.wutongke.modularization:compile:1.1.1'
 compile 'com.github.wutongke.modularization:macore:1.1.1'
```
### 2 创建自定义Application
2.1 实际Application
我们知道一个app中只有一个Application，所以在主Module中定义Application，然后在其它模块中根据需要实现逻辑Application即可，然后启动时注册逻辑Application，即可管理其生命周期：
```
public class MyApplication extends MaApplication {

    //多进程中注册各个进程的Router，可以参考第3小节的原理图
    @Override
    public void initializeAllProcessRouter() {
        WideRouter.registerLocalRouter("com.spinytech.maindemo",MainRouterConnectService.class);
        WideRouter.registerLocalRouter("com.spinytech.maindemo:music",MusicRouterConnectService.class);
        WideRouter.registerLocalRouter("com.spinytech.maindemo:pic",PicRouterConnectService.class);
    }

//注册各个模块中的逻辑Application，每个模块中可以注册多个逻辑
//Applicatoin，设置优先级，可以调整模块中多个逻辑Application的
//调用顺序
    @Override
    protected void initializeLogic() {
        registerApplicationLogic("com.spinytech.maindemo",999, MainApplicationLogic.class);
        registerApplicationLogic("com.spinytech.maindemo",998, WebApplicationLogic.class);
        registerApplicationLogic("com.spinytech.maindemo:music",999, MusicApplicationLogic.class);
        registerApplicationLogic("com.spinytech.maindemo:pic",999, PicApplicationLogic.class);
    }

//设置是否支持多进程
    @Override
    public boolean needMultipleProcess() {
        return true;
    }
}
```
当然这个自定义的Application需要注册到manifest文件中。
使用多进程提供服务的模块需要继承```LocalRouterConnectService```，并且在manifest中注册服务:
```
public class MusicRouterConnectService extends LocalRouterConnectService {
    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("MRCS","onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("MRCS","onDestroy");
    }
}
```
```
<service android:name=".MusicRouterConnectService"
            android:process=":music"/>
```


2.2 逻辑Application
逻辑Application通过继承BaseApplicationLogic，实现相应的方法即可被回调。
```
public class BaseApplicationLogic {
    protected MaApplication mApplication;
    public BaseApplicationLogic() {
    }

    public void setApplication(@NonNull MaApplication application) {
        mApplication = application;
    }

    public void onCreate() {
    }

    public void onTerminate() {
    }

    public void onLowMemory() {
    }

    public void onTrimMemory(int level) {
    }

    public void onConfigurationChanged(Configuration newConfig) {
    }
}

//逻辑Application只需要继承BaseApplicationLogic，注册后
//生命周期会被回调
public class MainApplicationLogic extends BaseApplicationLogic {
    @Override
    public void onCreate() {
        super.onCreate();
    }
}
```

### 3 自定义Provider和Action
定义Provider
```
@Provider(processName = "com.spinytech.maindemo:music")
public class MusicProvider extends MaProvider{
    @Override
    protected String getName() {
        return "music";
    }
}
```
定义Action
```
@Action(processName = "com.spinytech.maindemo:music", providerName = "music")
public class PlayAction implements MaAction<Song> {

    @Override
    public boolean isAsync(Context context, RouterRequest<Song> requestData) {
        return false;
    }

    @Override
    public MaActionResult invoke(final Context context, final RouterRequest<Song> requestData) {
        Intent intent = new Intent(context, MusicService.class);
        intent.putExtra("command", "play");
        context.startService(intent);
        MaActionResult result = new MaActionResult.Builder()
                .code(MaActionResult.CODE_SUCCESS)
                .msg("play success")
                .result(new Song("lili"))
                .build();
        Handler handler = new Handler(context.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (requestData != null && requestData.getRequestObject() != null) {
                    Toast.makeText(context, "歌曲名字：" + requestData.getRequestObject().name + "（并不知道）", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Logger.d("com.spinytech", requestData.getRequestObject().name);
        return result;
    }

    @Override
    public String getName() {
        return "play";
    }
}
```
可以看到定义Provider和Action时分别使用了```@Provider``` 和```@Action``` 注解，这样可以在程序编译时完成自动的注册，不需要手动注册到Router了。

其中 ```@Provider```需要设置进程名字，```@Action``` 需要设置进程名字和注册到的Provider名字：
```
@Provider(processName = "com.spinytech.maindemo:music")
@Action(processName = "com.spinytech.maindemo:music", providerName = "music")
```
### 4 调用Action
4.1 建立Action调用
首先需求建立一个请求```RouterRequest```，说明要请求的内容：
```
RouterRequestUtil.obtain(MainActivity.this)
                                    .domain("com.spinytech.maindemo:music")
                                    .provider("music")
                                    .action("play")
                                    .reqeustObject(new Song("see you"))
```
可以通过RouterRequestUtil的obtain方法快速建立请求，上例中请求的Action位于"com.spinytech.maindemo:music"进程，Provider是"music"，Action是"play"，并且传递了相应的参数new Song("see you")。

然后使用Rxjava的方式请求Action：
```
LocalRouter.getInstance(MaApplication.getMaApplication())
                            .rxRoute(MainActivity.this, RouterRequestUtil.obtain(MainActivity.this)
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
```

4.2 处理请求
在music模块中处理刚刚发出的请求，6.3中定义的Provider和Action其实就是处理6.4.1中的请求的，并且返回了```MaActionResult```:
```
MaActionResult result = new MaActionResult.Builder()
                .code(MaActionResult.CODE_SUCCESS)
                .msg("play success")
                .result(new Song("lili"))
                .build();
```
## License


    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

