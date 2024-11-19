package com.example.ivs_broadcaster;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.amazonaws.ivs.broadcast.BroadcastException;
import com.amazonaws.ivs.broadcast.ImagePreviewView;
import com.amazonaws.ivs.broadcast.JitterBufferConfiguration;
import com.amazonaws.ivs.broadcast.LocalStageStream;
import com.amazonaws.ivs.broadcast.ParticipantInfo;
import com.amazonaws.ivs.broadcast.Stage;
import com.amazonaws.ivs.broadcast.StageRenderer;
import com.amazonaws.ivs.broadcast.StageStream;
import com.amazonaws.ivs.broadcast.SubscribeConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;

@RequiresApi(api = Build.VERSION_CODES.P)
public class IVSStagePlayerView implements PlatformView, MethodChannel.MethodCallHandler, SurfaceHolder.Callback, StageRenderer, Stage.Strategy {
    private static final String TAG = "StageView";

    private final SurfaceView surfaceView;
    private Surface surface;
    private EventChannel renderStreamChannel;
    private EventChannel.EventSink renderStreamSink;
    private ImagePreviewView previewView;

    //
    private Stage stage = null;
    private Context context = null;

    //
    HashMap renderEventMap = new HashMap();


    IVSStagePlayerView(Context context, BinaryMessenger messenger) {
        surfaceView = new SurfaceView(context);
        MethodChannel methodChannel = new MethodChannel(messenger, "ivs_stage_method");
        renderStreamChannel = new EventChannel(messenger, "ivs_stage_event");
        methodChannel.setMethodCallHandler(this);
        this.context = context;
    }

    @Override
    public View getView() {
        return surfaceView;
    }

    @Override
    public void onFlutterViewAttached(@NonNull View flutterView) {
        PlatformView.super.onFlutterViewAttached(flutterView);
        surfaceView.getHolder().addCallback(this);
        renderStreamChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object o, EventChannel.EventSink event) {
                renderStreamSink = event;
            }

            @Override
            public void onCancel(Object o) {
                renderStreamSink = null;
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
        destroy();
    }


    @Override
    public void onMethodCall(MethodCall methodCall, @NonNull MethodChannel.Result result) {
        switch (methodCall.method) {
            case "join":
                initializeWithJoin(methodCall, result);
                break;
            case "leave":
                leave(methodCall, result);
                break;
            default:
                result.notImplemented();
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        this.surface = surfaceHolder.getSurface();
        Log.d(TAG, "Surface created and player surface set.");
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        this.surface = null;
        Log.d(TAG, "Surface destroyed and player surface cleared.");
    }

    // Flutter
    private void initializeWithJoin(MethodCall methodCall, @NonNull MethodChannel.Result result) {
        String token = (String) methodCall.arguments;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && token != null) {
            stage = new Stage(context, token, this);
            stage.addRenderer(this);
            try {
                stage.join();
                renderEventMap.put("is_joined", true);
                if (renderStreamSink != null)
                    renderStreamSink.success(renderEventMap);
            } catch (Exception e) {
                Log.d("Stage joined failed", e.getMessage());
                renderEventMap.put("is_joined", false);
                if (renderStreamSink != null)
                    renderStreamSink.success(renderEventMap);
                ;
            }

            result.success("Success");
            return;
        }
        result.success("Failed to init stage");
    }

    private void leave(MethodCall methodCall, @NonNull MethodChannel.Result result) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            stage.leave();
            renderEventMap.put("is_joined", false);
            if (renderStreamSink != null)
                renderStreamSink.success(renderEventMap);
            result.success("Success");
            return;
        }
        if (renderStreamSink != null)
            result.success("Failed to leave stage");
    }

    // Flutter dispose
    private void destroy() {
        stage.leave();
        stage.release();
        stage.removeRenderer(this);
        if (surface != null) {
            surface.release();
        }
    }

    // Render
    @Override
    public void onError(@NonNull BroadcastException exception) {
        StageRenderer.super.onError(exception);
        Log.d("Stage onError", exception.toString());
        renderEventMap.put("error", exception.getMessage());
        if (renderStreamSink != null)
            renderStreamSink.success(renderEventMap);
    }

    @Override
    public void onConnectionStateChanged(@NonNull Stage stage, @NonNull Stage.ConnectionState state, @Nullable BroadcastException exception) {
        StageRenderer.super.onConnectionStateChanged(stage, state, exception);
        Log.d("Stage onCtateChanged", state.toString());
        renderEventMap.put("state_changed", state.toString());
        if (renderStreamSink != null)
            renderStreamSink.success(renderEventMap);
    }

    @Override
    public void onParticipantJoined(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
        StageRenderer.super.onParticipantJoined(stage, participantInfo);
        Log.d("Stage onPaJoined", participantInfo.participantId);
        renderEventMap.put("joined", participantInfo.participantId);
        if (renderStreamSink != null)
            renderStreamSink.success(renderEventMap);
    }

    @Override
    public void onParticipantLeft(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
        StageRenderer.super.onParticipantLeft(stage, participantInfo);
        Log.d("Stage onPartLeft", participantInfo.toString());
        renderEventMap.put("left", participantInfo.participantId);
        if (renderStreamSink != null)
            renderStreamSink.success(renderEventMap);
    }

    @Override
    public void onParticipantPublishStateChanged(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo, @NonNull Stage.PublishState publishState) {
        StageRenderer.super.onParticipantPublishStateChanged(stage, participantInfo, publishState);
        Log.d("Stage publish status c", publishState.name());
        renderEventMap.put("publish_changed", participantInfo.participantId);
        if (renderStreamSink != null)
            renderStreamSink.success(renderEventMap);
    }

    @Override
    public void onParticipantSubscribeStateChanged(@NonNull Stage stage, @NonNull ParticipantInfo publishingParticipantInfo, @NonNull Stage.SubscribeState subscribeState) {
        StageRenderer.super.onParticipantSubscribeStateChanged(stage, publishingParticipantInfo, subscribeState);
        Log.d("Stage subscribe statte", subscribeState.name());
        renderEventMap.put("subscribe_changed", publishingParticipantInfo.participantId);
        if (renderStreamSink != null)
            renderStreamSink.success(renderEventMap);
    }

    @Override
    public void onStreamsAdded(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo, @NonNull List<StageStream> streams) {
        StageRenderer.super.onStreamsAdded(stage, participantInfo, streams);
        Log.d("Stage onStreamsAdded", streams.toString());
        for (int i = 0; i < streams.size(); i++) {
            if (streams.get(i).getStreamType() == StageStream.Type.VIDEO) {
                surfaceView.setVisibility(View.GONE);
                ((ViewGroup) surfaceView.getParent()).addView(streams.get(i).getPreviewSurfaceView());
                surfaceView.setVisibility(View.VISIBLE);
                Log.d("Stage", "preview setted");
            }
        }

        renderEventMap.put("stream_added", streams.size());
        if (renderStreamSink != null)
            renderStreamSink.success(renderEventMap);
    }

    @Override
    public void onStreamsRemoved(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo, @NonNull List<StageStream> streams) {
        StageRenderer.super.onStreamsRemoved(stage, participantInfo, streams);
        Log.d("Stage onStreamsRemoved", participantInfo.toString());
        renderEventMap.put("stream_removed", streams.size());
        if (renderStreamSink != null)
            renderStreamSink.success(renderEventMap);
    }

    @Override
    public void onStreamsMutedChanged(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo, @NonNull List<StageStream> streams) {
        StageRenderer.super.onStreamsMutedChanged(stage, participantInfo, streams);
        Log.d("Stage onSdChanged", participantInfo.toString());
        renderEventMap.put("stream_mute", streams.size());
        if (renderStreamSink != null)
            renderStreamSink.success(renderEventMap);
    }

    // Strategy
    @Override
    public SubscribeConfiguration subscribeConfigrationForParticipant(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
        Log.d("Stage", "subscribeConfigrationForParticipant");
        SubscribeConfiguration config = new SubscribeConfiguration();
        config.jitterBuffer.setMinDelay(JitterBufferConfiguration.JitterBufferDelay.MEDIUM());
        return config;
    }

    @NonNull
    @Override
    public List<LocalStageStream> stageStreamsToPublishForParticipant(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
        Log.d("Stage", "stageStreamsToPublishForParticipant");
        return Collections.emptyList();
    }

    @Override
    public boolean shouldPublishFromParticipant(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
        Log.d("Stage", "shouldPublishFromParticipant");
        return true;
    }

    @Override
    public Stage.SubscribeType shouldSubscribeToParticipant(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
        Log.d("Stage", "shouldSubscribeToParticipant");
        return Stage.SubscribeType.AUDIO_VIDEO;
    }
}