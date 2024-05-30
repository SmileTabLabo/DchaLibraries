package com.android.browser;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
/* loaded from: classes.dex */
public class UploadHandler {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private ArrayList<String> mAuthority;
    private Uri mCapturedMedia;
    private Controller mController;
    private boolean mHandled;
    private boolean mIsLowMemory;
    private WebChromeClient.FileChooserParams mParams;
    private ValueCallback<Uri[]> mUploadMessage;

    public UploadHandler(Controller controller) {
        this.mIsLowMemory = false;
        this.mAuthority = null;
        this.mController = controller;
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ((ActivityManager) this.mController.getContext().getSystemService("activity")).getMemoryInfo(memoryInfo);
        if ((memoryInfo.totalMem / 1024.0d) / 1024.0d < 512.0d) {
            this.mIsLowMemory = true;
            this.mAuthority = new ArrayList<>();
            this.mAuthority.add("com.android.externalstorage.documents");
            this.mAuthority.add("com.android.providers.downloads.documents");
            return;
        }
        this.mIsLowMemory = false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean handled() {
        return this.mHandled;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onResult(int i, Intent intent) {
        Log.d("browser", "onResult: " + i + " " + intent);
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
        if (this.mIsLowMemory) {
            String str = "*/*";
            String[] acceptTypes = this.mParams.getAcceptTypes();
            if (acceptTypes != null && acceptTypes.length > 0) {
                str = acceptTypes[0];
            }
            str = (str == null || str.equals("")) ? "*/*" : "*/*";
            intent = new Intent("android.intent.action.GET_CONTENT");
            intent.addCategory("android.intent.category.OPENABLE");
            intent.setType(str);
            intent.putStringArrayListExtra("user-assigned-authorities", this.mAuthority);
            Log.d("browser", "MIME TYPE: " + str);
        } else if (fileChooserParams.isCaptureEnabled() && createCaptureIntent.length == 1) {
            intent = createCaptureIntent[0];
        } else {
            Intent intent2 = new Intent("android.intent.action.CHOOSER");
            intent2.putExtra("android.intent.extra.INITIAL_INTENTS", createCaptureIntent);
            intent2.putExtra("android.intent.extra.INTENT", fileChooserParams.createIntent());
            intent = intent2;
        }
        startActivity(intent);
    }

    private Uri[] parseResult(int i, Intent intent) {
        Uri uri;
        if (i == 0) {
            return null;
        }
        if (intent != null && i == -1) {
            uri = intent.getData();
        } else {
            uri = null;
        }
        if (uri == null && intent == null && i == -1 && this.mCapturedMedia != null) {
            uri = this.mCapturedMedia;
        }
        if (uri != null) {
            return new Uri[]{uri};
        }
        return null;
    }

    private void startActivity(Intent intent) {
        try {
            this.mController.getActivity().startActivityForResult(intent, 4);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this.mController.getActivity(), (int) R.string.uploads_disabled, 1).show();
        }
    }

    private Intent[] createCaptureIntent() {
        String str = "*/*";
        String[] acceptTypes = this.mParams.getAcceptTypes();
        if (acceptTypes != null && acceptTypes.length > 0) {
            str = acceptTypes[0];
        }
        return str.equals("image/*") ? new Intent[]{createCameraIntent(createTempFileContentUri(".jpg"))} : str.equals("video/*") ? new Intent[]{createCamcorderIntent()} : str.equals("audio/*") ? new Intent[]{createSoundRecorderIntent()} : new Intent[]{createCameraIntent(createTempFileContentUri(".jpg")), createCamcorderIntent(), createSoundRecorderIntent()};
    }

    private Uri createTempFileContentUri(String str) {
        try {
            File file = new File(this.mController.getActivity().getFilesDir(), "captured_media");
            if (!file.exists() && !file.mkdir()) {
                throw new RuntimeException("Folder cannot be created.");
            }
            return FileProvider.getUriForFile(this.mController.getActivity(), "com.android.browser-classic.file", File.createTempFile(String.valueOf(System.currentTimeMillis()), str, file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    private Intent createCamcorderIntent() {
        return new Intent("android.media.action.VIDEO_CAPTURE");
    }

    private Intent createSoundRecorderIntent() {
        return new Intent("android.provider.MediaStore.RECORD_SOUND");
    }
}
