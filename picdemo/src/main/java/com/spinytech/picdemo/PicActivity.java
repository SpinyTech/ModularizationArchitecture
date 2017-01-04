package com.spinytech.picdemo;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PicActivity extends Activity {

    private static List<Bitmap> oomList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_pic);
        showPic();
    }
    private void showPic(){
        Intent intent = getIntent();
        if(intent!=null){
            boolean isBig = intent.getBooleanExtra("is_big",false);
            if(!isBig){
                ((ImageView)findViewById(R.id.pic_iv)).setImageBitmap(getImageFromAssetsFile("messi.png"));
            }
            //Do something crazy.... like OOM
            else{
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for(int i = 0;i<100;i++){
                            oomList.add(getImageFromAssetsFile("messi.png"));
                        }
                    }
                }).start();
            }
        }
    }

    private Bitmap getImageFromAssetsFile(String fileName)
    {
        Bitmap image = null;
        AssetManager am = getResources().getAssets();
        try
        {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return image;
    }
}
