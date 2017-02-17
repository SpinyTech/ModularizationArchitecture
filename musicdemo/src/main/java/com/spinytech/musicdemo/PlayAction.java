package com.spinytech.musicdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.linked.annotion.Action;
import com.spinytech.macore.MaAction;
import com.spinytech.macore.Song;
import com.spinytech.macore.router.MaActionResult;
import com.spinytech.macore.router.RouterRequest;
import com.spinytech.macore.tools.Logger;

/**
 * Created by wanglei on 2016/12/28.
 */
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
