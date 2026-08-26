package com.nanbeiyule.game;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

final class AvatarEditorDialog extends Dialog {
    interface Listener {
        void onChooseRequested();

        void onSaveRequested();
    }

    private final AvatarEditorView editorView;

    AvatarEditorDialog(
            Context context,
            Bitmap currentAvatar,
            int membershipLevel,
            Listener listener) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        editorView = new AvatarEditorView(context, currentAvatar, membershipLevel);
        editorView.setListener(
                new AvatarEditorView.Listener() {
                    @Override
                    public void onChooseRequested() {
                        listener.onChooseRequested();
                    }

                    @Override
                    public void onSaveRequested() {
                        listener.onSaveRequested();
                    }

                    @Override
                    public void onCloseRequested() {
                        dismiss();
                    }
                });
        setContentView(
                editorView,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Window window = getWindow();
        if (window != null) {
            window.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            window.getDecorView()
                    .setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    void setSelectedAvatar(Bitmap bitmap) {
        editorView.setSelectedAvatar(bitmap);
    }

    void setUploading(boolean uploading) {
        editorView.setUploading(uploading);
    }
}
