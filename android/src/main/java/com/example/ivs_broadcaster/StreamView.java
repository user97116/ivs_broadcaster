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
    private static final String TAG = "StreamView";

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

//        player.setLogLevel(Player.LogLevel.DEBUG);

        statusChannel = new EventChannel(messenger, "ivs_player_status_stream");
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
        if (surfaceView != null) {
            surfaceView.getHolder().removeCallback(this);
        }
    }

    @Override
    public void dispose() {
        onClose();
    }

    private void onClose() {
        // Remove listener and release player resources if player is not null
        if (player != null) {
            player.removeListener(this);
            player.release();
            Log.d(TAG, "Player disposed.");
        }

        // Ensure surface is cleaned up properly
        if (surface != null) {
            surface.release();  // Optional: depending on how the surface is managed.
        }
    }


    @Override
    public void onMethodCall(MethodCall methodCall, @NonNull MethodChannel.Result result) {
        switch (methodCall.method) {
            case "load":
                String uriString = (String) methodCall.arguments;
                if (uriString != null && !uriString.isEmpty()) {
                    player.load(Uri.parse(uriString));
                    result.success("Loading");
                } else {
                    result.error("INVALID_URL", "Provided URL is invalid", null);
                }
                break;

            case "play":
                player.play();
                result.success("Playing");
                break;

            case "close":
                onClose();
                result.success("Closed");
                break;

            case "pause":
                player.pause();
                result.success("Paused");
                break;

            default:
                result.notImplemented();
        }
    }

    @Override
    public void onCue(@NonNull Cue cue) {
        // Handle cue events if necessary
        Log.d(TAG, "Cue received: " + cue.toString());
    }

    @Override
    public void onDurationChanged(long duration) {
        Log.d(TAG, "Duration changed: " + duration);
    }

    @Override
    public void onStateChanged(@NonNull Player.State state) {
        Log.d(TAG, "Player state changed: " + state.name());

        switch (state) {
            case BUFFERING:
                statusSink.success("BUFFERING");
                break;

            case READY:
                player.setLooping(true);
                player.play();
                statusSink.success("READY");
                break;

            case IDLE:
                statusSink.success("IDLE");
                break;

            case PLAYING:
                statusSink.success("PLAYING");
                break;

            case ENDED:
                statusSink.success("ENDED");
                break;
        }
    }

    @Override
    public void onError(@NonNull PlayerException e) {
        Log.e(TAG, "Player error: " + e.getMessage());
        Log.e(TAG, "Error details: " + e.getCause());
        if (statusSink != null) {
            statusSink.success("ERROR: " + e.getMessage());
        }
    }


    @Override
    public void onRebuffering() {
        Log.d(TAG, "Rebuffering...");
    }

    @Override
    public void onSeekCompleted(long position) {
        Log.d(TAG, "Seek completed at position: " + position);
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
        Log.d(TAG, "Video size changed: " + width + "x" + height);
    }

    @Override
    public void onQualityChanged(@NonNull Quality quality) {
        Log.d(TAG, "Quality changed: " + quality.toString());
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        this.surface = surfaceHolder.getSurface();
        if (player != null) {
            player.setSurface(this.surface);
            Log.d(TAG, "Surface created and player surface set.");
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int format, int width, int height) {
        // Handle surface changes if necessary
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        this.surface = null;
        if (player != null) {
            player.setSurface(null);
            Log.d(TAG, "Surface destroyed and player surface cleared.");
        }
    }
}