package com.example.ivs_broadcaster.streams.strategy;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.amazonaws.ivs.broadcast.AudioLocalStageStream;
import com.amazonaws.ivs.broadcast.BroadcastConfiguration;
import com.amazonaws.ivs.broadcast.Device;
import com.amazonaws.ivs.broadcast.DeviceDiscovery;
import com.amazonaws.ivs.broadcast.ImageLocalStageStream;
import com.amazonaws.ivs.broadcast.ImagePreviewSurfaceView;
import com.amazonaws.ivs.broadcast.JitterBufferConfiguration;
import com.amazonaws.ivs.broadcast.LocalStageStream;
import com.amazonaws.ivs.broadcast.ParticipantInfo;
import com.amazonaws.ivs.broadcast.Stage;
import com.amazonaws.ivs.broadcast.StageAudioConfiguration;
import com.amazonaws.ivs.broadcast.StageVideoConfiguration;
import com.amazonaws.ivs.broadcast.SubscribeConfiguration;
import com.example.ivs_broadcaster.views.RemoteViewFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.flutter.plugin.common.EventChannel;

@RequiresApi(api = Build.VERSION_CODES.P)
public class PublishStrategy implements Stage.Strategy {
    Context context;

    public PublishStrategy(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public List<LocalStageStream> stageStreamsToPublishForParticipant(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
        DeviceDiscovery deviceDiscovery = new DeviceDiscovery(context);
        List<Device> devices = deviceDiscovery.listLocalDevices();
        List<LocalStageStream> localStageStreams = new ArrayList<LocalStageStream>();

        Device frontCamera = null;
        Device microphone = null;

        // Create streams using the front camera, first microphone
        for (Device device : devices) {
            Device.Descriptor descriptor = device.getDescriptor();
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
                Log.d("PublishStrategy", "camera accessed " + cameraStream.toString());
            }
            if (microphone == null && descriptor.type == Device.Descriptor.DeviceType.MICROPHONE) {
                microphone = device;
                AudioLocalStageStream microphoneStream = new AudioLocalStageStream(microphone);
                final StageAudioConfiguration audioConfiguration = new StageAudioConfiguration();
                audioConfiguration.enableEchoCancellation(true);
                microphoneStream.setAudioConfiguration(audioConfiguration);
                localStageStreams.add(microphoneStream);
                Log.d("PublishStrategy", "mic accessed " + microphoneStream.toString());
            }
        }
        return localStageStreams;
    }

    @Override
    public boolean shouldPublishFromParticipant(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
        return true;
    }

    @Override
    public Stage.SubscribeType shouldSubscribeToParticipant(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
        return Stage.SubscribeType.AUDIO_VIDEO;
    }

    @Override
    public SubscribeConfiguration subscribeConfigrationForParticipant(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
        SubscribeConfiguration config = new SubscribeConfiguration();
        config.jitterBuffer.setMinDelay(JitterBufferConfiguration.JitterBufferDelay.MEDIUM());
        return config;
    }
}
