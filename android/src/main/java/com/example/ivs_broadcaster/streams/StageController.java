package com.example.ivs_broadcaster.streams;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.amazonaws.ivs.broadcast.Stage;
import com.example.ivs_broadcaster.chats.StageChat;
import com.example.ivs_broadcaster.streams.strategy.PublishStrategy;
import com.example.ivs_broadcaster.streams.strategy.ViewerStrategy;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

@SuppressLint("NewApi")
public class StageController {
    Stage stage;
    FlutterPlugin.FlutterPluginBinding binding;
    EventChannel.EventSink stageSink;
    StageChat stageChat;

    public StageController(FlutterPlugin.FlutterPluginBinding binding, EventChannel.EventSink stageSink, StageChat stageChat) {
        this.binding = binding;
        this.stageSink = stageSink;
        this.stageChat = stageChat;
    }

    public void joinStage(MethodCall methodCall, @NonNull MethodChannel.Result result) {
        String token = (String) methodCall.argument("token");
        boolean shouldPublish = (boolean) methodCall.argument("shouldPublish");
        Log.d("StageController", token);
        Log.d("StageController", String.valueOf(shouldPublish));
        if (shouldPublish) {
            stage = new Stage(binding.getApplicationContext(), token, new PublishStrategy(binding.getApplicationContext()));
        } else {
            stage = new Stage(binding.getApplicationContext(), token, new ViewerStrategy());
        }
        stage.addRenderer(new StageListener(binding, stageSink, stageChat));
        try {
            stage.join();
            Log.d("StageController", "Stage is joined");
        } catch (Exception e) {
            Log.d("StageController", e.getMessage());
        }
        result.success("Success");
    }

    public void leaveStage(MethodCall methodCall, @NonNull MethodChannel.Result result) {
        stage.leave();
        stage.release();
        result.success("StageController is leaving...");
    }
}
