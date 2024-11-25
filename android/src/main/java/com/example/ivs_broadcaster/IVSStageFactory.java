package com.example.ivs_broadcaster;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;
import io.flutter.plugin.platform.PlatformViewRegistry;

@RequiresApi(api = Build.VERSION_CODES.P)
public class IVSStageFactory extends PlatformViewFactory {
    private final PlatformViewRegistry platformViewRegistry;
    private final BinaryMessenger messenger;

    public IVSStageFactory(PlatformViewRegistry platformViewRegistry, BinaryMessenger messenger) {
        super(StandardMessageCodec.INSTANCE);
        this.platformViewRegistry = platformViewRegistry;
        this.messenger = messenger;
    }

    @NonNull
    @Override
    public PlatformView create(Context context, int id, Object o) {
        return (PlatformView) new IVSStagePlayerView(platformViewRegistry, context, messenger);
    }
}