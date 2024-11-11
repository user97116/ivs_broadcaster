package com.example.ivs_broadcaster;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

import static io.flutter.plugin.common.MethodChannel.MethodCallHandler;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.platform.PlatformView;

import com.amazonaws.ivs.player.Cue;
import com.amazonaws.ivs.player.Player;
import com.amazonaws.ivs.player.PlayerException;
import com.amazonaws.ivs.player.PlayerView;
import com.amazonaws.ivs.player.Quality;

import androidx.annotation.NonNull;

public class StreamView extends Player.Listener implements PlatformView, MethodCallHandler, SurfaceHolder.Callback {
    private final Player player;
    private final SurfaceView surfaceView;
    private Surface surface;
    private final EventChannel statusChannel;
    private EventChannel.EventSink statusSink;

    StreamView(Context context, BinaryMessenger messenger) {
        PlayerView playerView = new PlayerView(context);
        player = playerView.getPlayer();
        player.addListener(this);
        surfaceView = new SurfaceView(context);
        MethodChannel methodChannel = new MethodChannel(messenger, "ivs_player_channel");
        methodChannel.setMethodCallHandler(this);
        player.setLogLevel(Player.LogLevel.ERROR);
        statusChannel = new EventChannel(messenger, "statusStream");
    }

    @Override
    public View getView() {
        return surfaceView;
    }

    @Override
    public void onFlutterViewAttached(@NonNull View flutterView) {
        PlatformView.super.onFlutterViewAttached(flutterView);
        surfaceView.getHolder().addCallback(this);
        statusChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object o, EventChannel.EventSink event) {
                statusSink = event;
            }

            @Override
            public void onCancel(Object o) {
                statusSink = null;
            }
        });

    }

    @Override
    public void onFlutterViewDetached() {
        PlatformView.super.onFlutterViewDetached();
    }

    @Override
    public void dispose() {
        player.removeListener(this);
        player.release();
        Log.d("amar", "dispose");
    }

    @Override
    public void onMethodCall(MethodCall methodCall, @NonNull MethodChannel.Result result) {
        if (methodCall.method.equals("load")) {
            player.load(Uri.parse((String) methodCall.arguments));
            result.success("Loading");
        } else if (methodCall.method.equals("play")) {
            player.play();
            result.success("Playing");
        } else {
            result.notImplemented();
        }
    }


    @Override
    public void onCue(@NonNull Cue cue) {

    }

    @Override
    public void onDurationChanged(long l) {

    }

    @Override
    public void onStateChanged(@NonNull Player.State state) {
        switch (state) {
            case BUFFERING:
                statusSink.success("BUFFERING");
                break;
            case READY:
                player.play();
                statusSink.success("READY");
                break;
            case IDLE:
                statusSink.success("IDLE");
                break;
            case PLAYING:
                statusSink.success("PLAYING");
                break;
        }
    }

    @Override
    public void onError(@NonNull PlayerException e) {

    }

    @Override
    public void onRebuffering() {

    }

    @Override
    public void onSeekCompleted(long l) {

    }

    @Override
    public void onVideoSizeChanged(int i, int i1) {

    }

    @Override
    public void onQualityChanged(@NonNull Quality quality) {

    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        this.surface = surfaceHolder.getSurface();
        if (player != null) {
            player.setSurface(this.surface);
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        this.surface = null;
        if (player != null) {
            player.setSurface(null);
        }
    }

}
