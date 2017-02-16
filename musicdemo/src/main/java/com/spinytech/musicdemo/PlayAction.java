package com.spinytech.musicdemo;

import android.content.Context;
import android.content.Intent;

import com.linked.annotion.Action;
import com.spinytech.macore.MaAction;
import com.spinytech.macore.MaActionResult;

import java.util.HashMap;

/**
 * Created by wanglei on 2016/12/28.
 */
@Action(processName = "com.spinytech.maindemo:music", providerName = "music")
public class PlayAction extends MaAction {

    @Override
    public boolean isAsync(Context context, HashMap<String, String> requestData) {
        return false;
    }

    @Override
    public MaActionResult invoke(Context context, HashMap<String, String> requestData) {
        Intent intent = new Intent(context,MusicService.class);
        intent.putExtra("command","play");
        context.startService(intent);
        MaActionResult result = new MaActionResult.Builder()
                .code(MaActionResult.CODE_SUCCESS)
                .msg("play success")
                .data("lll")
                .result(new Person("lili"))
                .build();
        return result;
    }

    @Override
    public String getName() {
        return "play";
    }
}
