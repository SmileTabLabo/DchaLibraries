package com.android.browser;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
/* loaded from: b.zip:com/android/browser/UploadHandler.class */
public class UploadHandler {

    /* renamed from: -assertionsDisabled  reason: not valid java name */
    static final boolean f5assertionsDisabled;
    private Uri mCapturedMedia;
    private Controller mController;
    private boolean mHandled;
    private WebChromeClient.FileChooserParams mParams;
    private ValueCallback<Uri[]> mUploadMessage;

    static {
        f5assertionsDisabled = !UploadHandler.class.desiredAssertionStatus();
    }

    public UploadHandler(Controller controller) {
        this.mController = controller;
    }

    private Intent createCamcorderIntent() {
        return new Intent("android.media.action.VIDEO_CAPTURE");
    }

    private Intent createCameraIntent(Uri uri) {
        if (uri == null) {
            throw new IllegalArgumentException();
        }
        this.mCapturedMedia = uri;
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.setFlags(3);
        intent.putExtra("output", this.mCapturedMedia);
        intent.setClipData(ClipData.newUri(this.mController.getActivity().getContentResolver(), "com.android.browser-classic.file", this.mCapturedMedia));
        return intent;
    }

    private Intent[] createCaptureIntent() {
        String[] acceptTypes = this.mParams.getAcceptTypes();
        String str = "*/*";
        if (acceptTypes != null) {
            str = "*/*";
            if (acceptTypes.length > 0) {
                str = acceptTypes[0];
            }
        }
        return str.equals("image/*") ? new Intent[]{createCameraIntent(createTempFileContentUri(".jpg"))} : str.equals("video/*") ? new Intent[]{createCamcorderIntent()} : str.equals("audio/*") ? new Intent[]{createSoundRecorderIntent()} : new Intent[]{createCameraIntent(createTempFileContentUri(".jpg")), createCamcorderIntent(), createSoundRecorderIntent()};
    }

    private Intent createSoundRecorderIntent() {
        return new Intent("android.provider.MediaStore.RECORD_SOUND");
    }

    private Uri createTempFileContentUri(String str) {
        try {
            File file = new File(this.mController.getActivity().getFilesDir(), "captured_media");
            if (file.exists() || file.mkdir()) {
                return FileProvider.getUriForFile(this.mController.getActivity(), "com.android.browser-classic.file", File.createTempFile(String.valueOf(System.currentTimeMillis()), str, file));
            }
            throw new RuntimeException("Folder cannot be created.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Uri[] parseResult(int i, Intent intent) {
        if (i == 0) {
            return null;
        }
        Uri uri = null;
        if (intent != null) {
            uri = i != -1 ? null : intent.getData();
        }
        Uri uri2 = uri;
        if (uri == null) {
            uri2 = uri;
            if (intent == null) {
                uri2 = uri;
                if (i == -1) {
                    uri2 = uri;
                    if (this.mCapturedMedia != null) {
                        uri2 = this.mCapturedMedia;
                    }
                }
            }
        }
        Uri[] uriArr = null;
        if (uri2 != null) {
            uriArr = new Uri[]{uri2};
        }
        return uriArr;
    }

    private void startActivity(Intent intent) {
        try {
            this.mController.getActivity().startActivityForResult(intent, 4);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this.mController.getActivity(), 2131492947, 1).show();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean handled() {
        return this.mHandled;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onResult(int i, Intent intent) {
        this.mUploadMessage.onReceiveValue(parseResult(i, intent));
        this.mHandled = true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void openFileChooser(ValueCallback<Uri[]> valueCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        Intent intent;
        if (this.mUploadMessage != null) {
            return;
        }
        this.mUploadMessage = valueCallback;
        this.mParams = fileChooserParams;
        Intent[] createCaptureIntent = createCaptureIntent();
        if (!f5assertionsDisabled) {
            if (!(createCaptureIntent != null && createCaptureIntent.length > 0)) {
                throw new AssertionError();
            }
        }
        if (fileChooserParams.isCaptureEnabled() && createCaptureIntent.length == 1) {
            intent = createCaptureIntent[0];
        } else {
            intent = new Intent("android.intent.action.CHOOSER");
            intent.putExtra("android.intent.extra.INITIAL_INTENTS", createCaptureIntent);
            intent.putExtra("android.intent.extra.INTENT", fileChooserParams.createIntent());
        }
        startActivity(intent);
    }
}
