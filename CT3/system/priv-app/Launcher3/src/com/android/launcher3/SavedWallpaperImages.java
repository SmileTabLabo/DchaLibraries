package com.android.launcher3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import com.android.gallery3d.common.BitmapCropTask;
import com.android.launcher3.WallpaperCropActivity;
import com.android.launcher3.WallpaperPickerActivity;
import com.android.photos.views.TiledImageRenderer;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
/* loaded from: a.zip:com/android/launcher3/SavedWallpaperImages.class */
public class SavedWallpaperImages extends BaseAdapter implements ListAdapter {
    private static String TAG = "Launcher3.SavedWallpaperImages";
    Context mContext;
    private ImageDb mDb;
    ArrayList<SavedWallpaperTile> mImages;
    LayoutInflater mLayoutInflater;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: a.zip:com/android/launcher3/SavedWallpaperImages$ImageDb.class */
    public static class ImageDb extends SQLiteOpenHelper {
        Context mContext;

        public ImageDb(Context context) {
            super(context, context.getDatabasePath("saved_wallpaper_images.db").getPath(), (SQLiteDatabase.CursorFactory) null, 2);
            this.mContext = context;
        }

        public static void moveFromCacheDirectoryIfNecessary(Context context) {
            File file = new File(context.getCacheDir(), "saved_wallpaper_images.db");
            File databasePath = context.getDatabasePath("saved_wallpaper_images.db");
            if (file.exists()) {
                file.renameTo(databasePath);
            }
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS saved_wallpaper_images (id INTEGER NOT NULL, image_thumbnail TEXT NOT NULL, image TEXT NOT NULL, extras TEXT, PRIMARY KEY (id ASC) );");
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            if (i == 1) {
                sQLiteDatabase.execSQL("ALTER TABLE saved_wallpaper_images ADD COLUMN extras TEXT;");
            } else if (i != i2) {
                sQLiteDatabase.execSQL("DELETE FROM saved_wallpaper_images");
                onCreate(sQLiteDatabase);
            }
        }
    }

    /* loaded from: a.zip:com/android/launcher3/SavedWallpaperImages$SavedWallpaperTile.class */
    public static class SavedWallpaperTile extends WallpaperPickerActivity.FileWallpaperInfo {
        private int mDbId;
        private Float[] mExtras;

        public SavedWallpaperTile(int i, File file, Drawable drawable, Float[] fArr) {
            super(file, drawable);
            this.mDbId = i;
            this.mExtras = (fArr == null || fArr.length != 3) ? null : fArr;
        }

        @Override // com.android.launcher3.WallpaperPickerActivity.FileWallpaperInfo
        protected WallpaperCropActivity.CropViewScaleAndOffsetProvider getCropViewScaleAndOffsetProvider() {
            if (this.mExtras != null) {
                return new WallpaperCropActivity.CropViewScaleAndOffsetProvider(this) { // from class: com.android.launcher3.SavedWallpaperImages.SavedWallpaperTile.1
                    final SavedWallpaperTile this$1;

                    {
                        this.this$1 = this;
                    }

                    @Override // com.android.launcher3.WallpaperCropActivity.CropViewScaleAndOffsetProvider
                    public void updateCropView(WallpaperCropActivity wallpaperCropActivity, TiledImageRenderer.TileSource tileSource) {
                        wallpaperCropActivity.mCropView.setScaleAndCenter(this.this$1.mExtras[0].floatValue(), this.this$1.mExtras[1].floatValue(), this.this$1.mExtras[2].floatValue());
                    }
                };
            }
            return null;
        }

        @Override // com.android.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public void onDelete(WallpaperPickerActivity wallpaperPickerActivity) {
            wallpaperPickerActivity.getSavedImages().deleteImage(this.mDbId);
        }

        @Override // com.android.launcher3.WallpaperPickerActivity.FileWallpaperInfo, com.android.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public void onSave(WallpaperPickerActivity wallpaperPickerActivity) {
            if (this.mExtras == null) {
                super.onSave(wallpaperPickerActivity);
            } else {
                wallpaperPickerActivity.cropImageAndSetWallpaper(Uri.fromFile(this.mFile), (BitmapCropTask.OnBitmapCroppedHandler) null, true, wallpaperPickerActivity.getWallpaperParallaxOffset() == 0.0f);
            }
        }
    }

    public SavedWallpaperImages(Context context) {
        ImageDb.moveFromCacheDirectoryIfNecessary(context);
        this.mDb = new ImageDb(context);
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    private Pair<String, String> getImageFilenames(int i) {
        Cursor query = this.mDb.getReadableDatabase().query("saved_wallpaper_images", new String[]{"image_thumbnail", "image"}, "id = ?", new String[]{Integer.toString(i)}, null, null, null, null);
        if (query.getCount() > 0) {
            query.moveToFirst();
            String string = query.getString(0);
            String string2 = query.getString(1);
            query.close();
            return new Pair<>(string, string2);
        }
        return null;
    }

    private void writeImage(Bitmap bitmap, InputStream inputStream, Float[] fArr) throws IOException {
        File createTempFile = File.createTempFile("wallpaper", "", this.mContext.getFilesDir());
        FileOutputStream openFileOutput = this.mContext.openFileOutput(createTempFile.getName(), 0);
        byte[] bArr = new byte[4096];
        while (true) {
            int read = inputStream.read(bArr);
            if (read <= 0) {
                break;
            }
            openFileOutput.write(bArr, 0, read);
        }
        openFileOutput.close();
        inputStream.close();
        File createTempFile2 = File.createTempFile("wallpaperthumb", "", this.mContext.getFilesDir());
        FileOutputStream openFileOutput2 = this.mContext.openFileOutput(createTempFile2.getName(), 0);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, openFileOutput2);
        openFileOutput2.close();
        SQLiteDatabase writableDatabase = this.mDb.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("image_thumbnail", createTempFile2.getName());
        contentValues.put("image", createTempFile.getName());
        if (fArr != null) {
            contentValues.put("extras", TextUtils.join(",", fArr));
        }
        writableDatabase.insert("saved_wallpaper_images", null, contentValues);
    }

    public void deleteImage(int i) {
        Pair<String, String> imageFilenames = getImageFilenames(i);
        new File(this.mContext.getFilesDir(), (String) imageFilenames.first).delete();
        new File(this.mContext.getFilesDir(), (String) imageFilenames.second).delete();
        this.mDb.getWritableDatabase().delete("saved_wallpaper_images", "id = ?", new String[]{Integer.toString(i)});
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.mImages.size();
    }

    @Override // android.widget.Adapter
    public SavedWallpaperTile getItem(int i) {
        return this.mImages.get(i);
    }

    @Override // android.widget.Adapter
    public long getItemId(int i) {
        return i;
    }

    @Override // android.widget.Adapter
    public View getView(int i, View view, ViewGroup viewGroup) {
        Drawable drawable = this.mImages.get(i).mThumb;
        if (drawable == null) {
            Log.e(TAG, "Error decoding thumbnail for wallpaper #" + i);
        }
        return WallpaperPickerActivity.createImageTileView(this.mLayoutInflater, view, viewGroup, drawable);
    }

    public void loadThumbnailsAndImageIdList() {
        this.mImages = new ArrayList<>();
        Cursor query = this.mDb.getReadableDatabase().query("saved_wallpaper_images", new String[]{"id", "image_thumbnail", "image", "extras"}, null, null, null, null, "id DESC", null);
        while (query.moveToNext()) {
            Bitmap decodeFile = BitmapFactory.decodeFile(new File(this.mContext.getFilesDir(), query.getString(1)).getAbsolutePath());
            if (decodeFile != null) {
                Float[] fArr = null;
                String string = query.getString(3);
                if (string != null) {
                    String[] split = string.split(",");
                    Float[] fArr2 = new Float[split.length];
                    int i = 0;
                    while (true) {
                        fArr = fArr2;
                        if (i >= split.length) {
                            break;
                        }
                        try {
                            fArr2[i] = Float.valueOf(Float.parseFloat(split[i]));
                            i++;
                        } catch (Exception e) {
                            fArr = null;
                        }
                    }
                }
                this.mImages.add(new SavedWallpaperTile(query.getInt(0), new File(this.mContext.getFilesDir(), query.getString(2)), new BitmapDrawable(decodeFile), fArr));
            }
        }
        query.close();
    }

    public void writeImage(Bitmap bitmap, Uri uri, Float[] fArr) {
        try {
            writeImage(bitmap, this.mContext.getContentResolver().openInputStream(uri), fArr);
        } catch (IOException e) {
            Log.e(TAG, "Failed writing images to storage " + e);
        }
    }

    public void writeImage(Bitmap bitmap, byte[] bArr) {
        try {
            writeImage(bitmap, new ByteArrayInputStream(bArr), (Float[]) null);
        } catch (IOException e) {
            Log.e(TAG, "Failed writing images to storage " + e);
        }
    }
}
