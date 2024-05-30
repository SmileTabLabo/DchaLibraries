package com.android.settings.users;

import android.app.Fragment;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.os.UserHandle;
import android.provider.ContactsContract;
import android.support.v4.content.FileProvider;
import android.util.EventLog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.drawable.CircleFramedDrawable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class EditUserPhotoController {
    private final Context mContext;
    private final Uri mCropPictureUri;
    private final Fragment mFragment;
    private final ImageView mImageView;
    private Bitmap mNewUserPhotoBitmap;
    private Drawable mNewUserPhotoDrawable;
    private final int mPhotoSize;
    private final Uri mTakePictureUri;

    static /* synthetic */ Context access$300(EditUserPhotoController editUserPhotoController) {
        return editUserPhotoController.mContext;
    }

    static /* synthetic */ Uri access$400(EditUserPhotoController editUserPhotoController) {
        return editUserPhotoController.mTakePictureUri;
    }

    public EditUserPhotoController(Fragment fragment, ImageView imageView, Bitmap bitmap, Drawable drawable, boolean z) {
        this.mContext = imageView.getContext();
        this.mFragment = fragment;
        this.mImageView = imageView;
        this.mCropPictureUri = createTempImageUri(this.mContext, "CropEditUserPhoto.jpg", !z);
        this.mTakePictureUri = createTempImageUri(this.mContext, "TakeEditUserPhoto2.jpg", !z);
        this.mPhotoSize = getPhotoSize(this.mContext);
        this.mImageView.setOnClickListener(new View.OnClickListener() { // from class: com.android.settings.users.EditUserPhotoController.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                EditUserPhotoController.this.showUpdatePhotoPopup();
            }
        });
        this.mNewUserPhotoBitmap = bitmap;
        this.mNewUserPhotoDrawable = drawable;
    }

    public boolean onActivityResult(int i, int i2, Intent intent) {
        if (i2 != -1) {
            return false;
        }
        Uri data = (intent == null || intent.getData() == null) ? this.mTakePictureUri : intent.getData();
        if (!"content".equals(data.getScheme())) {
            Log.e("EditUserPhotoController", "Invalid pictureUri scheme: " + data.getScheme());
            EventLog.writeEvent(1397638484, "172939189", -1, data.getPath());
            return false;
        }
        switch (i) {
            case 1001:
            case 1002:
                if (this.mTakePictureUri.equals(data)) {
                    cropPhoto();
                } else {
                    copyAndCropPhoto(data);
                }
                return true;
            case 1003:
                onPhotoCropped(data, true);
                return true;
            default:
                return false;
        }
    }

    public Bitmap getNewUserPhotoBitmap() {
        return this.mNewUserPhotoBitmap;
    }

    public Drawable getNewUserPhotoDrawable() {
        return this.mNewUserPhotoDrawable;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showUpdatePhotoPopup() {
        boolean canTakePhoto = canTakePhoto();
        boolean canChoosePhoto = canChoosePhoto();
        if (!canTakePhoto && !canChoosePhoto) {
            return;
        }
        Context context = this.mImageView.getContext();
        ArrayList arrayList = new ArrayList();
        if (canTakePhoto) {
            arrayList.add(new RestrictedMenuItem(context, context.getString(R.string.user_image_take_photo), "no_set_user_icon", new Runnable() { // from class: com.android.settings.users.EditUserPhotoController.2
                @Override // java.lang.Runnable
                public void run() {
                    EditUserPhotoController.this.takePhoto();
                }
            }));
        }
        if (canChoosePhoto) {
            arrayList.add(new RestrictedMenuItem(context, context.getString(R.string.user_image_choose_photo), "no_set_user_icon", new Runnable() { // from class: com.android.settings.users.EditUserPhotoController.3
                @Override // java.lang.Runnable
                public void run() {
                    EditUserPhotoController.this.choosePhoto();
                }
            }));
        }
        final ListPopupWindow listPopupWindow = new ListPopupWindow(context);
        listPopupWindow.setAnchorView(this.mImageView);
        listPopupWindow.setModal(true);
        listPopupWindow.setInputMethodMode(2);
        listPopupWindow.setAdapter(new RestrictedPopupMenuAdapter(context, arrayList));
        listPopupWindow.setWidth(Math.max(this.mImageView.getWidth(), context.getResources().getDimensionPixelSize(R.dimen.update_user_photo_popup_min_width)));
        listPopupWindow.setDropDownGravity(8388611);
        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.android.settings.users.EditUserPhotoController.4
            /* JADX WARN: Type inference failed for: r1v1, types: [android.widget.Adapter] */
            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
                listPopupWindow.dismiss();
                ((RestrictedMenuItem) adapterView.getAdapter().getItem(i)).doAction();
            }
        });
        listPopupWindow.show();
    }

    private boolean canTakePhoto() {
        return this.mImageView.getContext().getPackageManager().queryIntentActivities(new Intent("android.media.action.IMAGE_CAPTURE"), 65536).size() > 0;
    }

    private boolean canChoosePhoto() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        return this.mImageView.getContext().getPackageManager().queryIntentActivities(intent, 0).size() > 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void takePhoto() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        appendOutputExtra(intent, this.mTakePictureUri);
        this.mFragment.startActivityForResult(intent, 1002);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void choosePhoto() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT", (Uri) null);
        intent.setType("image/*");
        appendOutputExtra(intent, this.mTakePictureUri);
        this.mFragment.startActivityForResult(intent, 1001);
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.settings.users.EditUserPhotoController$5] */
    private void copyAndCropPhoto(final Uri uri) {
        new AsyncTask<Void, Void, Void>() { // from class: com.android.settings.users.EditUserPhotoController.5
            /*  JADX ERROR: JadxRuntimeException in pass: BlockProcessor
                jadx.core.utils.exceptions.JadxRuntimeException: Found unreachable blocks
                	at jadx.core.dex.visitors.blocks.DominatorTree.sortBlocks(DominatorTree.java:35)
                	at jadx.core.dex.visitors.blocks.DominatorTree.compute(DominatorTree.java:25)
                	at jadx.core.dex.visitors.blocks.BlockProcessor.computeDominators(BlockProcessor.java:202)
                	at jadx.core.dex.visitors.blocks.BlockProcessor.processBlocksTree(BlockProcessor.java:45)
                	at jadx.core.dex.visitors.blocks.BlockProcessor.visit(BlockProcessor.java:39)
                */
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public java.lang.Void doInBackground(java.lang.Void... r6) {
                /*
                    r5 = this;
                    com.android.settings.users.EditUserPhotoController r6 = com.android.settings.users.EditUserPhotoController.this
                    android.content.Context r6 = com.android.settings.users.EditUserPhotoController.access$300(r6)
                    android.content.ContentResolver r6 = r6.getContentResolver()
                    r0 = 0
                    android.net.Uri r1 = r2     // Catch: java.io.IOException -> L48
                    java.io.InputStream r1 = r6.openInputStream(r1)     // Catch: java.io.IOException -> L48
                    com.android.settings.users.EditUserPhotoController r2 = com.android.settings.users.EditUserPhotoController.this     // Catch: java.lang.Throwable -> L3c
                    android.net.Uri r2 = com.android.settings.users.EditUserPhotoController.access$400(r2)     // Catch: java.lang.Throwable -> L3c
                    java.io.OutputStream r6 = r6.openOutputStream(r2)     // Catch: java.lang.Throwable -> L3c
                    libcore.io.Streams.copy(r1, r6)     // Catch: java.lang.Throwable -> L2d
                    if (r6 == 0) goto L24
                    $closeResource(r0, r6)     // Catch: java.lang.Throwable -> L3c
                L24:
                    if (r1 == 0) goto L29
                    $closeResource(r0, r1)     // Catch: java.io.IOException -> L48
                L29:
                    goto L50
                L2a:
                    r2 = move-exception
                    r3 = r0
                    goto L33
                L2d:
                    r2 = move-exception
                    throw r2     // Catch: java.lang.Throwable -> L2f
                L2f:
                    r3 = move-exception
                    r4 = r3
                    r3 = r2
                    r2 = r4
                L33:
                    if (r6 == 0) goto L38
                    $closeResource(r3, r6)     // Catch: java.lang.Throwable -> L3c
                L38:
                    throw r2     // Catch: java.lang.Throwable -> L3c
                L39:
                    r6 = move-exception
                    r2 = r0
                    goto L42
                L3c:
                    r6 = move-exception
                    throw r6     // Catch: java.lang.Throwable -> L3e
                L3e:
                    r2 = move-exception
                    r4 = r2
                    r2 = r6
                    r6 = r4
                L42:
                    if (r1 == 0) goto L47
                    $closeResource(r2, r1)     // Catch: java.io.IOException -> L48
                L47:
                    throw r6     // Catch: java.io.IOException -> L48
                L48:
                    r6 = move-exception
                    java.lang.String r1 = "EditUserPhotoController"
                    java.lang.String r2 = "Failed to copy photo"
                    android.util.Log.w(r1, r2, r6)
                L50:
                    return r0
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.settings.users.EditUserPhotoController.AnonymousClass5.doInBackground(java.lang.Void[]):java.lang.Void");
            }

            private static /* synthetic */ void $closeResource(Throwable th, AutoCloseable autoCloseable) {
                if (th == null) {
                    autoCloseable.close();
                    return;
                }
                try {
                    autoCloseable.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(Void r1) {
                if (EditUserPhotoController.this.mFragment.isAdded()) {
                    EditUserPhotoController.this.cropPhoto();
                }
            }
        }.execute(new Void[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void cropPhoto() {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(this.mTakePictureUri, "image/*");
        appendOutputExtra(intent, this.mCropPictureUri);
        appendCropExtras(intent);
        if (intent.resolveActivity(this.mContext.getPackageManager()) != null) {
            try {
                StrictMode.disableDeathOnFileUriExposure();
                this.mFragment.startActivityForResult(intent, 1003);
                return;
            } finally {
                StrictMode.enableDeathOnFileUriExposure();
            }
        }
        onPhotoCropped(this.mTakePictureUri, false);
    }

    private void appendOutputExtra(Intent intent, Uri uri) {
        intent.putExtra("output", uri);
        intent.addFlags(3);
        intent.setClipData(ClipData.newRawUri("output", uri));
    }

    private void appendCropExtras(Intent intent) {
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", this.mPhotoSize);
        intent.putExtra("outputY", this.mPhotoSize);
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.settings.users.EditUserPhotoController$6] */
    private void onPhotoCropped(final Uri uri, final boolean z) {
        new AsyncTask<Void, Void, Bitmap>() { // from class: com.android.settings.users.EditUserPhotoController.6
            /* JADX INFO: Access modifiers changed from: protected */
            /* JADX WARN: Type inference failed for: r9v1, types: [boolean] */
            @Override // android.os.AsyncTask
            public Bitmap doInBackground(Void... voidArr) {
                Throwable th;
                InputStream inputStream;
                ?? r9 = z;
                InputStream inputStream2 = null;
                try {
                    if (r9 == 0) {
                        Bitmap createBitmap = Bitmap.createBitmap(EditUserPhotoController.this.mPhotoSize, EditUserPhotoController.this.mPhotoSize, Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(createBitmap);
                        try {
                            Bitmap decodeStream = BitmapFactory.decodeStream(EditUserPhotoController.this.mContext.getContentResolver().openInputStream(uri));
                            if (decodeStream != null) {
                                int min = Math.min(decodeStream.getWidth(), decodeStream.getHeight());
                                int width = (decodeStream.getWidth() - min) / 2;
                                int height = (decodeStream.getHeight() - min) / 2;
                                canvas.drawBitmap(decodeStream, new Rect(width, height, width + min, min + height), new Rect(0, 0, EditUserPhotoController.this.mPhotoSize, EditUserPhotoController.this.mPhotoSize), new Paint());
                                return createBitmap;
                            }
                            return null;
                        } catch (FileNotFoundException e) {
                            return null;
                        }
                    }
                    try {
                        inputStream = EditUserPhotoController.this.mContext.getContentResolver().openInputStream(uri);
                        try {
                            Bitmap decodeStream2 = BitmapFactory.decodeStream(inputStream);
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e2) {
                                    Log.w("EditUserPhotoController", "Cannot close image stream", e2);
                                }
                            }
                            return decodeStream2;
                        } catch (FileNotFoundException e3) {
                            e = e3;
                            Log.w("EditUserPhotoController", "Cannot find image file", e);
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e4) {
                                    Log.w("EditUserPhotoController", "Cannot close image stream", e4);
                                }
                            }
                            return null;
                        }
                    } catch (FileNotFoundException e5) {
                        e = e5;
                        inputStream = null;
                    } catch (Throwable th2) {
                        th = th2;
                        if (inputStream2 != null) {
                            try {
                                inputStream2.close();
                            } catch (IOException e6) {
                                Log.w("EditUserPhotoController", "Cannot close image stream", e6);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    inputStream2 = r9;
                    th = th3;
                }
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    EditUserPhotoController.this.mNewUserPhotoBitmap = bitmap;
                    EditUserPhotoController.this.mNewUserPhotoDrawable = CircleFramedDrawable.getInstance(EditUserPhotoController.this.mImageView.getContext(), EditUserPhotoController.this.mNewUserPhotoBitmap);
                    EditUserPhotoController.this.mImageView.setImageDrawable(EditUserPhotoController.this.mNewUserPhotoDrawable);
                }
                new File(EditUserPhotoController.this.mContext.getCacheDir(), "TakeEditUserPhoto2.jpg").delete();
                new File(EditUserPhotoController.this.mContext.getCacheDir(), "CropEditUserPhoto.jpg").delete();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
    }

    private static int getPhotoSize(Context context) {
        Cursor query = context.getContentResolver().query(ContactsContract.DisplayPhoto.CONTENT_MAX_DIMENSIONS_URI, new String[]{"display_max_dim"}, null, null, null);
        try {
            query.moveToFirst();
            return query.getInt(0);
        } finally {
            query.close();
        }
    }

    private Uri createTempImageUri(Context context, String str, boolean z) {
        File cacheDir = context.getCacheDir();
        cacheDir.mkdirs();
        File file = new File(cacheDir, str);
        if (z) {
            file.delete();
        }
        return FileProvider.getUriForFile(context, "com.android.settings.files", file);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public File saveNewUserPhotoBitmap() {
        if (this.mNewUserPhotoBitmap == null) {
            return null;
        }
        try {
            File file = new File(this.mContext.getCacheDir(), "NewUserPhoto.png");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            this.mNewUserPhotoBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            return file;
        } catch (IOException e) {
            Log.e("EditUserPhotoController", "Cannot create temp file", e);
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Bitmap loadNewUserPhotoBitmap(File file) {
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void removeNewUserPhotoBitmapFile() {
        new File(this.mContext.getCacheDir(), "NewUserPhoto.png").delete();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class RestrictedMenuItem {
        private final Runnable mAction;
        private final RestrictedLockUtils.EnforcedAdmin mAdmin;
        private final Context mContext;
        private final boolean mIsRestrictedByBase;
        private final String mTitle;

        public RestrictedMenuItem(Context context, String str, String str2, Runnable runnable) {
            this.mContext = context;
            this.mTitle = str;
            this.mAction = runnable;
            int myUserId = UserHandle.myUserId();
            this.mAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(context, str2, myUserId);
            this.mIsRestrictedByBase = RestrictedLockUtils.hasBaseUserRestriction(this.mContext, str2, myUserId);
        }

        public String toString() {
            return this.mTitle;
        }

        final void doAction() {
            if (isRestrictedByBase()) {
                return;
            }
            if (isRestrictedByAdmin()) {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(this.mContext, this.mAdmin);
            } else {
                this.mAction.run();
            }
        }

        final boolean isRestrictedByAdmin() {
            return this.mAdmin != null;
        }

        final boolean isRestrictedByBase() {
            return this.mIsRestrictedByBase;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class RestrictedPopupMenuAdapter extends ArrayAdapter<RestrictedMenuItem> {
        public RestrictedPopupMenuAdapter(Context context, List<RestrictedMenuItem> list) {
            super(context, (int) R.layout.restricted_popup_menu_item, (int) R.id.text, list);
        }

        @Override // android.widget.ArrayAdapter, android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            View view2 = super.getView(i, view, viewGroup);
            RestrictedMenuItem item = getItem(i);
            TextView textView = (TextView) view2.findViewById(R.id.text);
            ImageView imageView = (ImageView) view2.findViewById(R.id.restricted_icon);
            int i2 = 0;
            textView.setEnabled((item.isRestrictedByAdmin() || item.isRestrictedByBase()) ? false : true);
            imageView.setVisibility((!item.isRestrictedByAdmin() || item.isRestrictedByBase()) ? 8 : 8);
            return view2;
        }
    }
}
