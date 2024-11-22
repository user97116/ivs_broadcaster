package com.example.ivs_broadcaster;

import static com.amazonaws.ivs.broadcast.BroadcastConfiguration.*;

import android.content.Context;
import android.content.pm.ActivityInfo;
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

import com.amazonaws.ivs.broadcast.AudioLocalStageStream;
import com.amazonaws.ivs.broadcast.AudioStageStream;
import com.amazonaws.ivs.broadcast.BroadcastConfiguration;
import com.amazonaws.ivs.broadcast.BroadcastException;
import com.amazonaws.ivs.broadcast.CameraSource;
import com.amazonaws.ivs.broadcast.Device;
import com.amazonaws.ivs.broadcast.DeviceDiscovery;
import com.amazonaws.ivs.broadcast.ImageDevice;
import com.amazonaws.ivs.broadcast.ImageLocalStageStream;
import com.amazonaws.ivs.broadcast.ImagePreviewView;
import com.amazonaws.ivs.broadcast.ImageStageStream;
import com.amazonaws.ivs.broadcast.JitterBufferConfiguration;
import com.amazonaws.ivs.broadcast.LocalStageStream;
import com.amazonaws.ivs.broadcast.ParticipantInfo;
import com.amazonaws.ivs.broadcast.Stage;
import com.amazonaws.ivs.broadcast.StageAudioConfiguration;
import com.amazonaws.ivs.broadcast.StageRenderer;
import com.amazonaws.ivs.broadcast.StageStream;
import com.amazonaws.ivs.broadcast.StageVideoConfiguration;
import com.amazonaws.ivs.broadcast.SubscribeConfiguration;

import java.util.ArrayList;
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
    List<LocalStageStream> publishStreams = new ArrayList<LocalStageStream>();

    // IVS Stage Broadcaster
   void publishIvsStage(MethodCall methodCall, @NonNull MethodChannel.Result result) {
        if(stage == null) {
            publishStreams.clear();
        }
        publishStreams.clear();

        DeviceDiscovery deviceDiscovery = new DeviceDiscovery(context);
        List<Device> devices = deviceDiscovery.listLocalDevices();

        Device frontCamera = null;
        Device microphone = null;
        // Create streams using the front camera, first microphone
        for (Device device : devices) {
            Log.d("amar_live", device.getTag().toString());
            Device.Descriptor descriptor = device.getDescriptor();
            if (frontCamera == null && descriptor.type == Device.Descriptor.DeviceType.CAMERA && descriptor.position == Device.Descriptor.Position.BACK) {
                frontCamera = device;
                ImageLocalStageStream cameraStream = new ImageLocalStageStream(frontCamera);
                StageVideoConfiguration videoConfiguration = new StageVideoConfiguration();
                videoConfiguration.setSize(new BroadcastConfiguration.Vec2(1080f, 720f));
                videoConfiguration.setCameraCaptureQuality(30, new BroadcastConfiguration.Vec2(1080f, 720f));
                videoConfiguration.simulcast.setEnabled(false);
                videoConfiguration.setDegradationPreference(StageVideoConfiguration.DegradationPreference.BALANCED);
                cameraStream.setVideoConfiguration(videoConfiguration);
                publishStreams.add(cameraStream);
            }
            if (microphone == null && descriptor.type == Device.Descriptor.DeviceType.MICROPHONE) {
                microphone = device;
                AudioLocalStageStream microphoneStream = new AudioLocalStageStream(microphone);
                final StageAudioConfiguration audioConfiguration = new StageAudioConfiguration();
                audioConfiguration.enableEchoCancellation(true);
                microphoneStream.setAudioConfiguration(audioConfiguration);
                publishStreams.add(microphoneStream);
            }
        }
        Log.d("amar_live", String.valueOf(publishStreams.size()));
        result.success("Exited");
    }
//

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
            case "publish":
                publishIvsStage(methodCall, result);
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
//        String token = (String) methodCall.arguments;
        String token = "eyJhbGciOiJLTVMiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjE3MzIyMjM2MTIsImlhdCI6MTczMjE4MDQxMiwianRpIjoibGtPT3JVSmx3UVhRIiwicmVzb3VyY2UiOiJhcm46YXdzOml2czphcC1zb3V0aC0xOjI5ODYzOTcxMjAzMjpzdGFnZS9EWVcxcjd4M20xSGQiLCJ0b3BpYyI6IkRZVzFyN3gzbTFIZCIsImV2ZW50c191cmwiOiJ3c3M6Ly9nbG9iYWwuZXZlbnRzLmxpdmUtdmlkZW8ubmV0Iiwid2hpcF91cmwiOiJodHRwczovLzdkNzdlNDI1NDVkYy5nbG9iYWwtYm0ud2hpcC5saXZlLXZpZGVvLm5ldCIsInVzZXJfaWQiOiJhbWFyIiwiY2FwYWJpbGl0aWVzIjp7ImFsbG93X3B1Ymxpc2giOnRydWUsImFsbG93X3N1YnNjcmliZSI6dHJ1ZX0sInZlcnNpb24iOiIwLjAifQ.MGYCMQDSfSnWugF-nEBreMdwoomEXEwa5OIo9pQ3D5efiye-7qrFN9nM3ySjl3nvEFuxcw8CMQC5hGX2OJsv3Okaya3MmU9HCl46FFTbAo80pBhtCgYhZtRqLgZriCoaABqKZB7V1-Y";
        Log.d("Stage", token);
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

        participantInfo.capabilities.add(ParticipantInfo.Capabilities.SUBSCRIBE);
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
//        return Collections.emptyList();
        return publishStreams;
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