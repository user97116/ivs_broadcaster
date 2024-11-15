package com.example.ivs_broadcaster;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

@RequiresApi(api = Build.VERSION_CODES.P)
public class IVSStageFactory extends PlatformViewFactory {
    private final BinaryMessenger messenger;

    public IVSStageFactory(BinaryMessenger messenger) {
        super(StandardMessageCodec.INSTANCE);
        this.messenger = messenger;
    }

    @NonNull
    @Override
    public PlatformView create(Context context, int id, Object o) {
        return (PlatformView) new IVSStagePlayerView(context, messenger);
    }
}