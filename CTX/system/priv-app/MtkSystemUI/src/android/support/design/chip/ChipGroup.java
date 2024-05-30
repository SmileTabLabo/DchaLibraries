package android.support.design.chip;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.design.internal.ThemeEnforcement;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import com.google.android.flexbox.FlexboxLayout;
/* loaded from: classes.dex */
public class ChipGroup extends FlexboxLayout {
    private int checkedId;
    private final CheckedStateTracker checkedStateTracker;
    private int chipSpacingHorizontal;
    private int chipSpacingVertical;
    private OnCheckedChangeListener onCheckedChangeListener;
    private PassThroughHierarchyChangeListener passThroughListener;
    private boolean protectFromCheckedChange;
    private boolean singleLine;
    private boolean singleSelection;
    private final SpacingDrawable spacingDrawable;

    /* loaded from: classes.dex */
    public interface OnCheckedChangeListener {
        void onCheckedChanged(ChipGroup chipGroup, int i);
    }

    public ChipGroup(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.chipGroupStyle);
    }

    public ChipGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.spacingDrawable = new SpacingDrawable();
        this.checkedStateTracker = new CheckedStateTracker();
        this.passThroughListener = new PassThroughHierarchyChangeListener();
        this.checkedId = -1;
        this.protectFromCheckedChange = false;
        TypedArray a = ThemeEnforcement.obtainStyledAttributes(context, attrs, R.styleable.ChipGroup, defStyleAttr, R.style.Widget_MaterialComponents_ChipGroup);
        int chipSpacing = a.getDimensionPixelOffset(R.styleable.ChipGroup_chipSpacing, 0);
        setChipSpacingHorizontal(a.getDimensionPixelOffset(R.styleable.ChipGroup_chipSpacingHorizontal, chipSpacing));
        setChipSpacingVertical(a.getDimensionPixelOffset(R.styleable.ChipGroup_chipSpacingVertical, chipSpacing));
        setSingleLine(a.getBoolean(R.styleable.ChipGroup_singleLine, false));
        setSingleSelection(a.getBoolean(R.styleable.ChipGroup_singleSelection, false));
        int checkedChip = a.getResourceId(R.styleable.ChipGroup_checkedChip, -1);
        if (checkedChip != -1) {
            this.checkedId = checkedChip;
        }
        a.recycle();
        setDividerDrawable(this.spacingDrawable);
        setShowDivider(2);
        setWillNotDraw(true);
        super.setOnHierarchyChangeListener(this.passThroughListener);
    }

    @Override // android.view.ViewGroup
    public void setOnHierarchyChangeListener(ViewGroup.OnHierarchyChangeListener listener) {
        this.passThroughListener.onHierarchyChangeListener = listener;
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (this.checkedId != -1) {
            setCheckedStateForView(this.checkedId, true);
            setCheckedId(this.checkedId);
        }
    }

    @Override // com.google.android.flexbox.FlexboxLayout, android.view.ViewGroup
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof Chip) {
            Chip chip = (Chip) child;
            if (chip.isChecked()) {
                if (this.checkedId != -1 && this.singleSelection) {
                    setCheckedStateForView(this.checkedId, false);
                }
                setCheckedId(chip.getId());
            }
        }
        super.addView(child, index, params);
    }

    @Override // com.google.android.flexbox.FlexboxLayout
    public void setDividerDrawableHorizontal(Drawable divider) {
        if (divider != this.spacingDrawable) {
            throw new UnsupportedOperationException("Changing divider drawables not allowed. ChipGroup uses divider drawables as spacing.");
        }
        super.setDividerDrawableHorizontal(divider);
    }

    @Override // com.google.android.flexbox.FlexboxLayout
    public void setDividerDrawableVertical(Drawable divider) {
        if (divider != this.spacingDrawable) {
            throw new UnsupportedOperationException("Changing divider drawables not allowed. ChipGroup uses divider drawables as spacing.");
        }
        super.setDividerDrawableVertical(divider);
    }

    @Override // com.google.android.flexbox.FlexboxLayout
    public void setShowDividerHorizontal(int dividerMode) {
        if (dividerMode != 2) {
            throw new UnsupportedOperationException("Changing divider modes not allowed. ChipGroup uses divider drawables as spacing.");
        }
        super.setShowDividerHorizontal(dividerMode);
    }

    @Override // com.google.android.flexbox.FlexboxLayout
    public void setShowDividerVertical(int dividerMode) {
        if (dividerMode != 2) {
            throw new UnsupportedOperationException("Changing divider modes not allowed. ChipGroup uses divider drawables as spacing.");
        }
        super.setShowDividerVertical(dividerMode);
    }

    @Override // com.google.android.flexbox.FlexboxLayout
    public void setFlexWrap(int flexWrap) {
        throw new UnsupportedOperationException("Changing flex wrap not allowed. ChipGroup exposes a singleLine attribute instead.");
    }

    public void clearCheck() {
        this.protectFromCheckedChange = true;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof Chip) {
                ((Chip) child).setChecked(false);
            }
        }
        this.protectFromCheckedChange = false;
        setCheckedId(-1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setCheckedId(int checkedId) {
        this.checkedId = checkedId;
        if (this.onCheckedChangeListener != null && this.singleSelection) {
            this.onCheckedChangeListener.onCheckedChanged(this, checkedId);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setCheckedStateForView(int viewId, boolean checked) {
        View checkedView = findViewById(viewId);
        if (checkedView instanceof Chip) {
            this.protectFromCheckedChange = true;
            ((Chip) checkedView).setChecked(checked);
            this.protectFromCheckedChange = false;
        }
    }

    public void setChipSpacingHorizontal(int chipSpacingHorizontal) {
        if (this.chipSpacingHorizontal != chipSpacingHorizontal) {
            this.chipSpacingHorizontal = chipSpacingHorizontal;
            requestLayout();
        }
    }

    public void setChipSpacingVertical(int chipSpacingVertical) {
        if (this.chipSpacingVertical != chipSpacingVertical) {
            this.chipSpacingVertical = chipSpacingVertical;
            requestLayout();
        }
    }

    public void setSingleLine(boolean singleLine) {
        this.singleLine = singleLine;
        super.setFlexWrap(!singleLine ? 1 : 0);
    }

    public void setSingleSelection(boolean singleSelection) {
        if (this.singleSelection != singleSelection) {
            this.singleSelection = singleSelection;
            clearCheck();
        }
    }

    /* loaded from: classes.dex */
    private class SpacingDrawable extends Drawable {
        private SpacingDrawable() {
        }

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicWidth() {
            return ChipGroup.this.chipSpacingHorizontal;
        }

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicHeight() {
            return ChipGroup.this.chipSpacingVertical;
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int alpha) {
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -2;
        }
    }

    /* loaded from: classes.dex */
    private class CheckedStateTracker implements CompoundButton.OnCheckedChangeListener {
        private CheckedStateTracker() {
        }

        @Override // android.widget.CompoundButton.OnCheckedChangeListener
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (ChipGroup.this.protectFromCheckedChange) {
                return;
            }
            int id = buttonView.getId();
            if (!isChecked) {
                if (ChipGroup.this.checkedId == id) {
                    ChipGroup.this.setCheckedId(-1);
                    return;
                }
                return;
            }
            if (ChipGroup.this.checkedId != -1 && ChipGroup.this.checkedId != id && ChipGroup.this.singleSelection) {
                ChipGroup.this.setCheckedStateForView(ChipGroup.this.checkedId, false);
            }
            ChipGroup.this.setCheckedId(id);
        }
    }

    /* loaded from: classes.dex */
    private class PassThroughHierarchyChangeListener implements ViewGroup.OnHierarchyChangeListener {
        private ViewGroup.OnHierarchyChangeListener onHierarchyChangeListener;

        private PassThroughHierarchyChangeListener() {
        }

        @Override // android.view.ViewGroup.OnHierarchyChangeListener
        public void onChildViewAdded(View parent, View child) {
            int id;
            if (parent == ChipGroup.this && (child instanceof Chip)) {
                int id2 = child.getId();
                if (id2 == -1) {
                    if (Build.VERSION.SDK_INT >= 17) {
                        id = View.generateViewId();
                    } else {
                        id = child.hashCode();
                    }
                    child.setId(id);
                }
                ((Chip) child).setOnCheckedChangeListenerInternal(ChipGroup.this.checkedStateTracker);
            }
            if (this.onHierarchyChangeListener != null) {
                this.onHierarchyChangeListener.onChildViewAdded(parent, child);
            }
        }

        @Override // android.view.ViewGroup.OnHierarchyChangeListener
        public void onChildViewRemoved(View parent, View child) {
            if (parent == ChipGroup.this && (child instanceof Chip)) {
                ((Chip) child).setOnCheckedChangeListenerInternal(null);
            }
            if (this.onHierarchyChangeListener != null) {
                this.onHierarchyChangeListener.onChildViewRemoved(parent, child);
            }
        }
    }
}
