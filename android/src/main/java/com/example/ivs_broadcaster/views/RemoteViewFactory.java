
package com.example.ivs_broadcaster.views;

import android.content.Context;
import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

@RequiresApi(api = Build.VERSION_CODES.P)
public class RemoteViewFactory extends PlatformViewFactory {
    private final View view;

    public RemoteViewFactory(View view) {
        super(StandardMessageCodec.INSTANCE);
        this.view = view;
    }

    @NonNull
    @Override
    public PlatformView create(Context context, int id, Object o) {
        return (PlatformView) new RemoteView(view);
    }
}