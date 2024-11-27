package com.example.ivs_broadcaster;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

import com.amazonaws.ivs.broadcast.AudioLocalStageStream;
import com.amazonaws.ivs.broadcast.BroadcastConfiguration;
import com.amazonaws.ivs.broadcast.BroadcastException;
import com.amazonaws.ivs.broadcast.Device;
import com.amazonaws.ivs.broadcast.DeviceDiscovery;
import com.amazonaws.ivs.broadcast.ImageLocalStageStream;
import com.amazonaws.ivs.broadcast.JitterBufferConfiguration;
import com.amazonaws.ivs.broadcast.LocalStageStream;
import com.amazonaws.ivs.broadcast.ParticipantInfo;
import com.amazonaws.ivs.broadcast.StageAudioConfiguration;
import com.amazonaws.ivs.broadcast.StageRenderer;
import com.amazonaws.ivs.broadcast.StageStream;
import com.amazonaws.ivs.broadcast.StageVideoConfiguration;
import com.amazonaws.ivs.broadcast.SubscribeConfiguration;
import com.amazonaws.ivs.broadcast.Stage;
import com.example.ivs_broadcaster.views.RemoteViewFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@SuppressLint("NewApi")
public class IvsBroadcasterPlugin implements FlutterPlugin, MethodChannel.MethodCallHandler, Stage.Strategy, StageRenderer {
    MethodChannel ivsStageMethod;
    FlutterPluginBinding binding;
    Stage stage;
    HashMap stageMap = new HashMap();
    EventChannel stageEventChannel;
    EventChannel.EventSink stageSink;
    ArrayList<String> views = new ArrayList<>();
    ArrayList<String> joined = new ArrayList<>();
    ArrayList<String> allViews = new ArrayList<>();

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        this.binding = binding;
        binding.getPlatformViewRegistry().registerViewFactory("ivs_player", new IVSPlayerFactory(binding.getBinaryMessenger()));
        ivsStageMethod = new MethodChannel(binding.getBinaryMessenger(), "gb_ivs_stage_method");
        ivsStageMethod.setMethodCallHandler(this);
        stageEventChannel = new EventChannel(binding.getBinaryMessenger(), "gb_ivs_stage_event");
        stageEventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object o, EventChannel.EventSink event) {
                stageSink = event;
            }

            @Override
            public void onCancel(Object o) {
                stageSink = null;
            }
        });
        binding.getPlatformViewRegistry().registerViewFactory("ivs_stage_player", new IVSStageFactory(binding.getPlatformViewRegistry(), binding.getBinaryMessenger()));
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        Log.d("Stage", "onDetachedFromEngine");
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        switch (call.method) {
            case "join":
                initializeWithJoin(call, result);
                break;
            case "leave":
                leave(call, result);
                break;
            default:
                result.notImplemented();
        }
    }

    private void initializeWithJoin(MethodCall methodCall, @NonNull MethodChannel.Result result) {
        views.clear();
        joined.clear();
        String token = (String) methodCall.argument("token");
        boolean shouldPublish = (boolean) methodCall.argument("shouldPublish");
        Log.d("Stage", token);
        Log.d("Stage", String.valueOf(shouldPublish));
        if (shouldPublish) {
            stage = new Stage(binding.getApplicationContext(), token, this);
        } else {
            stage = new Stage(binding.getApplicationContext(), token, new Stage.Strategy() {
                @NonNull
                @Override
                public List<LocalStageStream> stageStreamsToPublishForParticipant(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
                    return Collections.emptyList();
                }

                @Override
                public boolean shouldPublishFromParticipant(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
                    return false;
                }

                @Override
                public Stage.SubscribeType shouldSubscribeToParticipant(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
                    return Stage.SubscribeType.AUDIO_VIDEO;
                }
            });
        }
        stage.addRenderer(this);
        try {
            stage.join();
            stageMap.put("is_joined", true);
            if (stageSink != null) stageSink.success(stageMap);
        } catch (Exception e) {
            Log.d("Stage joined failed", e.getMessage());
            stageMap.put("is_joined", false);
            if (stageSink != null) stageSink.success(stageMap);
        }
        result.success("Success");
    }

    private void leave(MethodCall methodCall, @NonNull MethodChannel.Result result) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            stage.leave();
            stageMap.put("is_joined", false);
            if (stageSink != null) stageSink.success(stageMap);
            result.success("Success");
            return;
        }
        if (stageSink != null) result.success("Failed to leave stage");
    }

    // Local view
    List<LocalStageStream> getLocalStageStreams() {
        DeviceDiscovery deviceDiscovery = new DeviceDiscovery(binding.getApplicationContext());
        List<Device> devices = deviceDiscovery.listLocalDevices();
        List<LocalStageStream> localStageStreams = new ArrayList<LocalStageStream>();

        Device frontCamera = null;
        Device microphone = null;

        // Create streams using the front camera, first microphone
        for (Device device : devices) {
            Log.d("amar_live", device.getTag().toString());
            Device.Descriptor descriptor = device.getDescriptor();
            Log.d("amar_live 1", String.valueOf(descriptor.position));

            if (frontCamera == null && descriptor.type == Device.Descriptor.DeviceType.CAMERA && descriptor.position == Device.Descriptor.Position.FRONT) {
                frontCamera = device;
                ImageLocalStageStream cameraStream = new ImageLocalStageStream(frontCamera);
                StageVideoConfiguration videoConfiguration = new StageVideoConfiguration();
                BroadcastConfiguration.Vec2 size = new BroadcastConfiguration.Vec2(1280f, 720f);
                videoConfiguration.setSize(size);
                videoConfiguration.setCameraCaptureQuality(30, size);
                videoConfiguration.simulcast.setEnabled(false);
                videoConfiguration.setDegradationPreference(StageVideoConfiguration.DegradationPreference.MAINTAIN_RESOLUTION);
                cameraStream.setVideoConfiguration(videoConfiguration);
                localStageStreams.add(cameraStream);
                Log.d("amar_live", "Stage stream attahed");

            }
            if (microphone == null && descriptor.type == Device.Descriptor.DeviceType.MICROPHONE) {
                microphone = device;
                AudioLocalStageStream microphoneStream = new AudioLocalStageStream(microphone);
                final StageAudioConfiguration audioConfiguration = new StageAudioConfiguration();
                audioConfiguration.enableEchoCancellation(true);
                microphoneStream.setAudioConfiguration(audioConfiguration);
                localStageStreams.add(microphoneStream);
            }
        }
        return localStageStreams;
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
        Log.d("Stage 2", participantInfo.isLocal ? "local" : "no local");
//        return Collections.emptyList();//
        return getLocalStageStreams();
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

    // Render
    @Override
    public void onError(@NonNull BroadcastException exception) {
        StageRenderer.super.onError(exception);
        Log.d("Stage onError", exception.toString());
        stageMap.put("error", exception.getMessage());
        if (stageSink != null) stageSink.success(stageMap);
    }

    @Override
    public void onConnectionStateChanged(@NonNull Stage stage, @NonNull Stage.ConnectionState state, @Nullable BroadcastException exception) {
        StageRenderer.super.onConnectionStateChanged(stage, state, exception);
        Log.d("Stage onCtateChanged", state.toString());
        stageMap.put("state_changed", state.toString());
        if (stageSink != null) stageSink.success(stageMap);
    }

    @Override
    public void onParticipantJoined(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
        StageRenderer.super.onParticipantJoined(stage, participantInfo);
        Log.d("Stage onPaJoined", participantInfo.participantId);
        Log.d("Stage onPaJoined", participantInfo.attributes.toString());
        Log.d("Stage onPaJoined", participantInfo.capabilities.toString());
        Log.d("Stage onPaJoined", participantInfo.userInfo.toString());
        Log.d("Stage onPaJoined", participantInfo.userId.toString());
        joined.add(participantInfo.participantId);
        stageMap.put("joined", joined);

        if (stageSink != null) stageSink.success(stageMap);
//        participantInfo.capabilities.add(ParticipantInfo.Capabilities.SUBSCRIBE);
    }

    @Override
    public void onParticipantLeft(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
        StageRenderer.super.onParticipantLeft(stage, participantInfo);
        Log.d("Stage onPartLeft", participantInfo.toString());
        stageMap.put("left", participantInfo.participantId);
        joined.remove(participantInfo.participantId);
        if (stageSink != null) stageSink.success(stageMap);
    }

    @Override
    public void onParticipantPublishStateChanged(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo, @NonNull Stage.PublishState publishState) {
        StageRenderer.super.onParticipantPublishStateChanged(stage, participantInfo, publishState);
        Log.d("Stage publish status c", publishState.name());
        Log.d("Stage publish status c", publishState.name());
        stageMap.put("publish_changed", participantInfo.participantId);
        if (stageSink != null) stageSink.success(stageMap);
    }

    @Override
    public void onParticipantSubscribeStateChanged(@NonNull Stage stage, @NonNull ParticipantInfo publishingParticipantInfo, @NonNull Stage.SubscribeState subscribeState) {
        StageRenderer.super.onParticipantSubscribeStateChanged(stage, publishingParticipantInfo, subscribeState);
        Log.d("Stage subscribe statte", subscribeState.name());
        stageMap.put("subscribe_changed", publishingParticipantInfo.participantId);
        if (stageSink != null) stageSink.success(stageMap);
    }

    @Override
    public void onStreamsAdded(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo, @NonNull List<StageStream> streams) {
        StageRenderer.super.onStreamsAdded(stage, participantInfo, streams);
        Log.d("Stage onStreamsAdded", streams.toString());
        for (int i = 0; i < streams.size(); i++) {
            Log.d("Stage onStreamsAddeqd", streams.get(i).getDevice().getDescriptor().urn);
            if (streams.get(i).getStreamType() == StageStream.Type.VIDEO) {
                String deviceId = String.valueOf(allViews.size()) + "_" + streams.get(i).getDevice().getDescriptor().deviceId;
                views.removeIf(item -> item.contains(deviceId));
                binding.getPlatformViewRegistry().registerViewFactory(deviceId, new RemoteViewFactory(streams.get(i).getPreviewSurfaceView()));
                views.add(deviceId);
                allViews.add(streams.get(i).getDevice().getDescriptor().deviceId);
                stageMap.put("views", views);
                Log.d("Stage", "preview added");
                Log.d("remove_views a", String.valueOf(allViews.size()));
            }
        }
        stageMap.put("stream_added", streams.size());
        if (stageSink != null) stageSink.success(stageMap);
    }

    @Override
    public void onStreamsRemoved(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo, @NonNull List<StageStream> streams) {
        StageRenderer.super.onStreamsRemoved(stage, participantInfo, streams);
        Log.d("Stage onStreamsRemoved", participantInfo.toString());
        Log.d("Stage onStreamsRemoved", participantInfo.userInfo.toString());
        Log.d("Stage onStreamsRemoved", String.valueOf(participantInfo.isLocal));
        stageMap.put("stream_removed", streams.size());

        for (int i = 0; i < streams.size(); i++) {
            StageStream stream = streams.get(i);
            if (stream.getStreamType() == StageStream.Type.VIDEO) {
                String deviceId = streams.get(i).getDevice().getDescriptor().deviceId;
                views.removeIf(item -> item.contains(deviceId));
            }
        }

        if (stageSink != null) stageSink.success(stageMap);
    }

    @Override
    public void onStreamsMutedChanged(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo, @NonNull List<StageStream> streams) {
        StageRenderer.super.onStreamsMutedChanged(stage, participantInfo, streams);
        Log.d("Stage onSdChanged", participantInfo.toString());
        stageMap.put("stream_mute", streams.size());
        if (stageSink != null) stageSink.success(stageMap);
    }
}
