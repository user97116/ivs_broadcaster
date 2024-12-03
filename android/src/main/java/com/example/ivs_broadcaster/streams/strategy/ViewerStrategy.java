package com.example.ivs_broadcaster.streams.strategy;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.amazonaws.ivs.broadcast.JitterBufferConfiguration;
import com.amazonaws.ivs.broadcast.LocalStageStream;
import com.amazonaws.ivs.broadcast.ParticipantInfo;
import com.amazonaws.ivs.broadcast.Stage;
import com.amazonaws.ivs.broadcast.SubscribeConfiguration;

import java.util.Collections;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.P)
public class ViewerStrategy implements Stage.Strategy {
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

    @Override
    public SubscribeConfiguration subscribeConfigrationForParticipant(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
        SubscribeConfiguration config = new SubscribeConfiguration();
        config.jitterBuffer.setMinDelay(JitterBufferConfiguration.JitterBufferDelay.MEDIUM());
        return config;
    }
}
