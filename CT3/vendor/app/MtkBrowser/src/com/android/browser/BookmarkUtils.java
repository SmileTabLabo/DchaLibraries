package com.android.browser;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.net.Uri;
import android.os.Message;
import com.android.browser.provider.BrowserContract;
/* loaded from: b.zip:com/android/browser/BookmarkUtils.class */
public class BookmarkUtils {

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: b.zip:com/android/browser/BookmarkUtils$BookmarkIconType.class */
    public enum BookmarkIconType {
        ICON_INSTALLABLE_WEB_APP,
        ICON_HOME_SHORTCUT,
        ICON_WIDGET;

        /* renamed from: values  reason: to resolve conflict with enum method */
        public static BookmarkIconType[] valuesCustom() {
            return values();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Intent createAddToHomeIntent(Context context, String str, String str2, Bitmap bitmap, Bitmap bitmap2) {
        Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        intent.putExtra("android.intent.extra.shortcut.INTENT", createShortcutIntent(str));
        intent.putExtra("android.intent.extra.shortcut.NAME", str2);
        intent.putExtra("android.intent.extra.shortcut.ICON", createIcon(context, bitmap, bitmap2, BookmarkIconType.ICON_HOME_SHORTCUT));
        intent.putExtra("duplicate", false);
        return intent;
    }

    static Bitmap createIcon(Context context, Bitmap bitmap, Bitmap bitmap2, BookmarkIconType bookmarkIconType) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
        return createIcon(context, bitmap, bitmap2, bookmarkIconType, activityManager.getLauncherLargeIconSize(), activityManager.getLauncherLargeIconDensity());
    }

    private static Bitmap createIcon(Context context, Bitmap bitmap, Bitmap bitmap2, BookmarkIconType bookmarkIconType, int i, int i2) {
        Bitmap createBitmap = Bitmap.createBitmap(i, i, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        Rect rect = new Rect(0, 0, createBitmap.getWidth(), createBitmap.getHeight());
        if (bitmap != null) {
            drawTouchIconToCanvas(bitmap, canvas, rect);
        } else {
            Bitmap iconBackground = getIconBackground(context, bookmarkIconType, i2);
            if (iconBackground != null) {
                canvas.drawBitmap(iconBackground, (Rect) null, rect, new Paint(3));
            }
            if (bitmap2 != null) {
                drawFaviconToCanvas(context, bitmap2, canvas, rect, bookmarkIconType);
            }
        }
        canvas.setBitmap(null);
        return createBitmap;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Drawable createListFaviconBackground(Context context) {
        PaintDrawable paintDrawable = new PaintDrawable();
        Resources resources = context.getResources();
        int dimensionPixelSize = resources.getDimensionPixelSize(2131427359);
        paintDrawable.setPadding(dimensionPixelSize, dimensionPixelSize, dimensionPixelSize, dimensionPixelSize);
        paintDrawable.getPaint().setColor(context.getResources().getColor(2131361799));
        paintDrawable.setCornerRadius(resources.getDimension(2131427360));
        return paintDrawable;
    }

    static Intent createShortcutIntent(String str) {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(str));
        intent.putExtra("com.android.browser.application_id", Long.toString((str.hashCode() << 32) | intent.hashCode()));
        return intent;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void displayRemoveBookmarkDialog(long j, String str, Context context, Message message) {
        new AlertDialog.Builder(context).setIconAttribute(16843605).setMessage(context.getString(2131493020, str)).setPositiveButton(2131492963, new DialogInterface.OnClickListener(message, j, context) { // from class: com.android.browser.BookmarkUtils.1
            final Context val$context;
            final long val$id;
            final Message val$msg;

            {
                this.val$msg = message;
                this.val$id = j;
                this.val$context = context;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                if (this.val$msg != null) {
                    this.val$msg.sendToTarget();
                }
                new Thread(new Runnable(this, this.val$id, this.val$context) { // from class: com.android.browser.BookmarkUtils.1.1
                    final AnonymousClass1 this$1;
                    final Context val$context;
                    final long val$id;

                    {
                        this.this$1 = this;
                        this.val$id = r6;
                        this.val$context = r8;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.val$context.getContentResolver().delete(ContentUris.withAppendedId(BrowserContract.Bookmarks.CONTENT_URI, this.val$id), null, null);
                    }
                }).start();
            }
        }).setNegativeButton(2131492962, (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void displayRemoveFolderDialog(long j, String str, Context context, Message message) {
        new AlertDialog.Builder(context).setIcon(17301543).setMessage(context.getString(2131492865, str)).setPositiveButton(2131492963, new DialogInterface.OnClickListener(message, j, context) { // from class: com.android.browser.BookmarkUtils.2
            final Context val$context;
            final long val$id;
            final Message val$msg;

            {
                this.val$msg = message;
                this.val$id = j;
                this.val$context = context;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                if (this.val$msg != null) {
                    this.val$msg.sendToTarget();
                }
                new Thread(new Runnable(this, this.val$id, this.val$context) { // from class: com.android.browser.BookmarkUtils.2.1
                    final AnonymousClass2 this$1;
                    final Context val$context;
                    final long val$id;

                    {
                        this.this$1 = this;
                        this.val$id = r6;
                        this.val$context = r8;
                    }

                    private void deleteBookmarkById(long j2) {
                        this.val$context.getContentResolver().delete(ContentUris.withAppendedId(BrowserContract.Bookmarks.CONTENT_URI, j2), null, null);
                    }

                    private void deleteFoldBookmarks(long j2) {
                        Cursor query = this.val$context.getContentResolver().query(BookmarkUtils.getBookmarksUri(this.val$context), new String[]{"_id"}, "parent = ? AND deleted = ?", new String[]{j2 + "", "0"}, null);
                        deleteBookmarkById(j2);
                        while (query.moveToNext()) {
                            deleteFoldBookmarks(query.getInt(0));
                        }
                        query.close();
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        deleteFoldBookmarks(this.val$id);
                    }
                }).start();
            }
        }).setNegativeButton(2131492962, (DialogInterface.OnClickListener) null).show();
    }

    private static void drawFaviconToCanvas(Context context, Bitmap bitmap, Canvas canvas, Rect rect, BookmarkIconType bookmarkIconType) {
        Paint paint = new Paint(3);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (bookmarkIconType == BookmarkIconType.ICON_WIDGET) {
            paint.setColor(context.getResources().getColor(2131361798));
        } else {
            paint.setColor(-1);
        }
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(2131427341);
        int width = bookmarkIconType == BookmarkIconType.ICON_WIDGET ? canvas.getWidth() : context.getResources().getDimensionPixelSize(2131427342);
        float f = (width - dimensionPixelSize) / 2;
        float exactCenterX = rect.exactCenterX() - (width / 2);
        float exactCenterY = rect.exactCenterY() - (width / 2);
        float f2 = exactCenterY;
        if (bookmarkIconType != BookmarkIconType.ICON_WIDGET) {
            f2 = exactCenterY - f;
        }
        RectF rectF = new RectF(exactCenterX, f2, width + exactCenterX, width + f2);
        canvas.drawRoundRect(rectF, 3.0f, 3.0f, paint);
        rectF.inset(f, f);
        canvas.drawBitmap(bitmap, (Rect) null, rectF, (Paint) null);
    }

    private static void drawTouchIconToCanvas(Bitmap bitmap, Canvas canvas, Rect rect) {
        Rect rect2 = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Paint paint = new Paint(1);
        paint.setFilterBitmap(true);
        canvas.drawBitmap(bitmap, rect2, rect, paint);
        Path path = new Path();
        path.setFillType(Path.FillType.INVERSE_WINDING);
        RectF rectF = new RectF(rect);
        rectF.inset(1.0f, 1.0f);
        path.addRoundRect(rectF, 8.0f, 8.0f, Path.Direction.CW);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPath(path, paint);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Uri getBookmarksUri(Context context) {
        return BrowserContract.Bookmarks.CONTENT_URI;
    }

    private static Bitmap getIconBackground(Context context, BookmarkIconType bookmarkIconType, int i) {
        if (bookmarkIconType == BookmarkIconType.ICON_HOME_SHORTCUT) {
            Drawable drawableForDensity = context.getResources().getDrawableForDensity(2130903041, i);
            if (drawableForDensity instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawableForDensity).getBitmap();
            }
            return null;
        } else if (bookmarkIconType == BookmarkIconType.ICON_INSTALLABLE_WEB_APP) {
            Drawable drawableForDensity2 = context.getResources().getDrawableForDensity(2130903040, i);
            if (drawableForDensity2 instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawableForDensity2).getBitmap();
            }
            return null;
        } else {
            return null;
        }
    }
}
