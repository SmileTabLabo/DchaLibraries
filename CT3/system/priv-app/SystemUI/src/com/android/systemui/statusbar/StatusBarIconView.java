package com.android.systemui.statusbar;

import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewDebug;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import com.android.internal.statusbar.StatusBarIcon;
import java.text.NumberFormat;
/* loaded from: a.zip:com/android/systemui/statusbar/StatusBarIconView.class */
public class StatusBarIconView extends AnimatedImageView {
    private boolean mAlwaysScaleIcon;
    private final boolean mBlocked;
    private int mDensity;
    private StatusBarIcon mIcon;
    private Notification mNotification;
    private Drawable mNumberBackground;
    private Paint mNumberPain;
    private String mNumberText;
    private int mNumberX;
    private int mNumberY;
    @ViewDebug.ExportedProperty
    private String mSlot;

    public StatusBarIconView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mBlocked = false;
        this.mAlwaysScaleIcon = true;
        updateIconScale();
        this.mDensity = context.getResources().getDisplayMetrics().densityDpi;
    }

    public StatusBarIconView(Context context, String str, Notification notification) {
        this(context, str, notification, false);
    }

    public StatusBarIconView(Context context, String str, Notification notification, boolean z) {
        super(context);
        this.mBlocked = z;
        this.mSlot = str;
        this.mNumberPain = new Paint();
        this.mNumberPain.setTextAlign(Paint.Align.CENTER);
        this.mNumberPain.setColor(context.getColor(2130838351));
        this.mNumberPain.setAntiAlias(true);
        setNotification(notification);
        maybeUpdateIconScale();
        setScaleType(ImageView.ScaleType.CENTER);
        this.mDensity = context.getResources().getDisplayMetrics().densityDpi;
    }

    public static String contentDescForNotification(Context context, Notification notification) {
        String str = "";
        try {
            str = Notification.Builder.recoverBuilder(context, notification).loadHeaderAppName();
        } catch (RuntimeException e) {
            Log.e("StatusBarIconView", "Unable to recover builder", e);
            Parcelable parcelable = notification.extras.getParcelable("android.appInfo");
            if (parcelable instanceof ApplicationInfo) {
                str = String.valueOf(((ApplicationInfo) parcelable).loadLabel(context.getPackageManager()));
            }
        }
        CharSequence charSequence = notification.extras.getCharSequence("android.title");
        String str2 = notification.tickerText;
        if (TextUtils.isEmpty(str2)) {
            str2 = !TextUtils.isEmpty(charSequence) ? charSequence : "";
        }
        return context.getString(2131493902, str, str2);
    }

    public static Drawable getIcon(Context context, StatusBarIcon statusBarIcon) {
        int identifier = statusBarIcon.user.getIdentifier();
        int i = identifier;
        if (identifier == -1) {
            i = 0;
        }
        Drawable loadDrawableAsUser = statusBarIcon.icon.loadDrawableAsUser(context, i);
        TypedValue typedValue = new TypedValue();
        context.getResources().getValue(2131689784, typedValue, true);
        float f = typedValue.getFloat();
        return f == 1.0f ? loadDrawableAsUser : new ScalingDrawableWrapper(loadDrawableAsUser, f);
    }

    private Drawable getIcon(StatusBarIcon statusBarIcon) {
        return getIcon(getContext(), statusBarIcon);
    }

    private void maybeUpdateIconScale() {
        if (this.mNotification != null || this.mAlwaysScaleIcon) {
            updateIconScale();
        }
    }

    private void setContentDescription(Notification notification) {
        if (notification != null) {
            String contentDescForNotification = contentDescForNotification(this.mContext, notification);
            if (TextUtils.isEmpty(contentDescForNotification)) {
                return;
            }
            setContentDescription(contentDescForNotification);
        }
    }

    private boolean updateDrawable(boolean z) {
        if (this.mIcon == null) {
            return false;
        }
        Drawable icon = getIcon(this.mIcon);
        if (icon == null) {
            Log.w("StatusBarIconView", "No icon for slot " + this.mSlot);
            return false;
        }
        if (z) {
            setImageDrawable(null);
        }
        setImageDrawable(icon);
        return true;
    }

    private void updateIconScale() {
        Resources resources = this.mContext.getResources();
        float dimensionPixelSize = resources.getDimensionPixelSize(2131689794) / resources.getDimensionPixelSize(2131689776);
        setScaleX(dimensionPixelSize);
        setScaleY(dimensionPixelSize);
    }

    protected void debug(int i) {
        super.debug(i);
        Log.d("View", debugIndent(i) + "slot=" + this.mSlot);
        Log.d("View", debugIndent(i) + "icon=" + this.mIcon);
    }

    public boolean equalIcons(Icon icon, Icon icon2) {
        boolean z = true;
        if (icon == icon2) {
            return true;
        }
        if (icon.getType() != icon2.getType()) {
            return false;
        }
        switch (icon.getType()) {
            case 2:
                if (!icon.getResPackage().equals(icon2.getResPackage()) || icon.getResId() != icon2.getResId()) {
                    z = false;
                }
                return z;
            case 3:
            default:
                return false;
            case 4:
                return icon.getUriString().equals(icon2.getUriString());
        }
    }

    public String getSlot() {
        return this.mSlot;
    }

    public StatusBarIcon getStatusBarIcon() {
        return this.mIcon;
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        int i = configuration.densityDpi;
        if (i != this.mDensity) {
            this.mDensity = i;
            maybeUpdateIconScale();
            updateDrawable();
        }
    }

    @Override // android.widget.ImageView, android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mNumberBackground != null) {
            this.mNumberBackground.draw(canvas);
            canvas.drawText(this.mNumberText, this.mNumberX, this.mNumberY, this.mNumberPain);
        }
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        if (this.mNotification != null) {
            accessibilityEvent.setParcelableData(this.mNotification);
        }
    }

    @Override // android.widget.ImageView, android.view.View
    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        updateDrawable();
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        if (this.mNumberBackground != null) {
            placeNumber();
        }
    }

    void placeNumber() {
        String string = this.mIcon.number > getContext().getResources().getInteger(17694723) ? getContext().getResources().getString(17039383) : NumberFormat.getIntegerInstance().format(this.mIcon.number);
        this.mNumberText = string;
        int width = getWidth();
        int height = getHeight();
        Rect rect = new Rect();
        this.mNumberPain.getTextBounds(string, 0, string.length(), rect);
        int i = rect.right;
        int i2 = rect.left;
        int i3 = rect.bottom - rect.top;
        this.mNumberBackground.getPadding(rect);
        int i4 = rect.left + (i - i2) + rect.right;
        int i5 = i4;
        if (i4 < this.mNumberBackground.getMinimumWidth()) {
            i5 = this.mNumberBackground.getMinimumWidth();
        }
        this.mNumberX = (width - rect.right) - (((i5 - rect.right) - rect.left) / 2);
        int i6 = rect.top + i3 + rect.bottom;
        int i7 = i6;
        if (i6 < this.mNumberBackground.getMinimumWidth()) {
            i7 = this.mNumberBackground.getMinimumWidth();
        }
        this.mNumberY = (height - rect.bottom) - ((((i7 - rect.top) - i3) - rect.bottom) / 2);
        this.mNumberBackground.setBounds(width - i5, height - i7, width, height);
    }

    /* JADX WARN: Code restructure failed: missing block: B:52:0x0101, code lost:
        if (r4.mBlocked != false) goto L48;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public boolean set(StatusBarIcon statusBarIcon) {
        int i;
        boolean equalIcons = this.mIcon != null ? equalIcons(this.mIcon.icon, statusBarIcon.icon) : false;
        boolean z = equalIcons ? this.mIcon.iconLevel == statusBarIcon.iconLevel : false;
        boolean z2 = this.mIcon != null ? this.mIcon.visible == statusBarIcon.visible : false;
        boolean z3 = this.mIcon != null ? this.mIcon.number == statusBarIcon.number : false;
        this.mIcon = statusBarIcon.clone();
        setContentDescription(statusBarIcon.contentDescription);
        if (equalIcons || updateDrawable(false)) {
            if (!z) {
                setImageLevel(statusBarIcon.iconLevel);
            }
            if (!z3) {
                if (statusBarIcon.number <= 0 || !getContext().getResources().getBoolean(2131623946)) {
                    this.mNumberBackground = null;
                    this.mNumberText = null;
                } else {
                    if (this.mNumberBackground == null) {
                        this.mNumberBackground = getContext().getResources().getDrawable(2130837687);
                    }
                    placeNumber();
                }
                invalidate();
            }
            if (z2) {
                return true;
            }
            if (statusBarIcon.visible) {
                i = 0;
            }
            i = 8;
            setVisibility(i);
            return true;
        }
        return false;
    }

    public void setNotification(Notification notification) {
        this.mNotification = notification;
        setContentDescription(notification);
    }

    @Override // android.view.View
    public String toString() {
        return "StatusBarIconView(slot=" + this.mSlot + " icon=" + this.mIcon + " notification=" + this.mNotification + ")";
    }

    public void updateDrawable() {
        updateDrawable(true);
    }
}
