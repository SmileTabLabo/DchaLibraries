package com.android.ex.editstyledtext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.text.Editable;
import android.text.NoCopySpan;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.ParagraphStyle;
import android.text.style.QuoteSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
/* loaded from: classes.dex */
public class EditStyledText extends EditText {
    private static final NoCopySpan.Concrete SELECTING = new NoCopySpan.Concrete();
    private static CharSequence STR_CLEARSTYLES;
    private static CharSequence STR_HORIZONTALLINE;
    private static CharSequence STR_PASTE;
    private Drawable mDefaultBackground;
    private ArrayList<EditStyledTextNotifier> mESTNotifiers;
    private InputConnection mInputConnection;
    private EditorManager mManager;

    /* loaded from: classes.dex */
    public interface EditStyledTextNotifier {
        boolean isButtonsFocused();

        void onStateChanged(int i, int i2);

        boolean sendOnTouchEvent(MotionEvent motionEvent);
    }

    @Override // android.widget.TextView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean onTouchEvent;
        if (motionEvent.getAction() == 1) {
            cancelLongPress();
            boolean isEditting = isEditting();
            if (!isEditting) {
                onStartEdit();
            }
            int selectionStart = Selection.getSelectionStart(getText());
            int selectionEnd = Selection.getSelectionEnd(getText());
            onTouchEvent = super.onTouchEvent(motionEvent);
            if (isFocused() && getSelectState() == 0) {
                if (isEditting) {
                    this.mManager.showSoftKey(Selection.getSelectionStart(getText()), Selection.getSelectionEnd(getText()));
                } else {
                    this.mManager.showSoftKey(selectionStart, selectionEnd);
                }
            }
            this.mManager.onCursorMoved();
            this.mManager.unsetTextComposingMask();
        } else {
            onTouchEvent = super.onTouchEvent(motionEvent);
        }
        sendOnTouchEvent(motionEvent);
        return onTouchEvent;
    }

    @Override // android.widget.TextView, android.view.View
    public Parcelable onSaveInstanceState() {
        SavedStyledTextState savedStyledTextState = new SavedStyledTextState(super.onSaveInstanceState());
        savedStyledTextState.mBackgroundColor = this.mManager.getBackgroundColor();
        return savedStyledTextState;
    }

    @Override // android.widget.TextView, android.view.View
    public void onRestoreInstanceState(Parcelable parcelable) {
        if (!(parcelable instanceof SavedStyledTextState)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        SavedStyledTextState savedStyledTextState = (SavedStyledTextState) parcelable;
        super.onRestoreInstanceState(savedStyledTextState.getSuperState());
        setBackgroundColor(savedStyledTextState.mBackgroundColor);
    }

    @Override // android.widget.TextView, android.view.View
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (this.mManager != null) {
            this.mManager.onRefreshStyles();
        }
    }

    @Override // android.widget.TextView
    public boolean onTextContextMenuItem(int i) {
        boolean z = getSelectionStart() != getSelectionEnd();
        switch (i) {
            case 16776961:
                onInsertHorizontalLine();
                return true;
            case 16776962:
                onClearStyles();
                return true;
            case 16776963:
                onStartEdit();
                return true;
            case 16776964:
                onEndEdit();
                return true;
            default:
                switch (i) {
                    case 16908319:
                        onStartSelectAll();
                        return true;
                    case 16908320:
                        if (z) {
                            onStartCut();
                        } else {
                            this.mManager.onStartSelectAll(false);
                            onStartCut();
                        }
                        return true;
                    case 16908321:
                        if (z) {
                            onStartCopy();
                        } else {
                            this.mManager.onStartSelectAll(false);
                            onStartCopy();
                        }
                        return true;
                    case 16908322:
                        onStartPaste();
                        return true;
                    default:
                        switch (i) {
                            case 16908328:
                                onStartSelect();
                                this.mManager.blockSoftKey();
                                break;
                            case 16908329:
                                onFixSelectedItem();
                                break;
                        }
                        return super.onTextContextMenuItem(i);
                }
        }
    }

    @Override // android.widget.TextView, android.view.View
    protected void onCreateContextMenu(ContextMenu contextMenu) {
        super.onCreateContextMenu(contextMenu);
        MenuHandler menuHandler = new MenuHandler();
        if (STR_HORIZONTALLINE != null) {
            contextMenu.add(0, 16776961, 0, STR_HORIZONTALLINE).setOnMenuItemClickListener(menuHandler);
        }
        if (isStyledText() && STR_CLEARSTYLES != null) {
            contextMenu.add(0, 16776962, 0, STR_CLEARSTYLES).setOnMenuItemClickListener(menuHandler);
        }
        if (this.mManager.canPaste()) {
            contextMenu.add(0, 16908322, 0, STR_PASTE).setOnMenuItemClickListener(menuHandler).setAlphabeticShortcut('v');
        }
    }

    @Override // android.widget.TextView
    protected void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        if (this.mManager != null) {
            this.mManager.updateSpanNextToCursor(getText(), i, i2, i3);
            this.mManager.updateSpanPreviousFromCursor(getText(), i, i2, i3);
            if (i3 > i2) {
                this.mManager.setTextComposingMask(i, i + i3);
            } else if (i2 < i3) {
                this.mManager.unsetTextComposingMask();
            }
            if (this.mManager.isWaitInput()) {
                if (i3 > i2) {
                    this.mManager.onCursorMoved();
                    onFixSelectedItem();
                } else if (i3 < i2) {
                    this.mManager.onAction(22);
                }
            }
        }
        super.onTextChanged(charSequence, i, i2, i3);
    }

    @Override // android.widget.TextView, android.view.View
    public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
        this.mInputConnection = new StyledTextInputConnection(super.onCreateInputConnection(editorInfo), this);
        return this.mInputConnection;
    }

    @Override // android.widget.TextView, android.view.View
    protected void onFocusChanged(boolean z, int i, Rect rect) {
        super.onFocusChanged(z, i, rect);
        if (z) {
            onStartEdit();
        } else if (!isButtonsFocused()) {
            onEndEdit();
        }
    }

    private void sendOnTouchEvent(MotionEvent motionEvent) {
        if (this.mESTNotifiers != null) {
            Iterator<EditStyledTextNotifier> it = this.mESTNotifiers.iterator();
            while (it.hasNext()) {
                it.next().sendOnTouchEvent(motionEvent);
            }
        }
    }

    public boolean isButtonsFocused() {
        boolean z = false;
        if (this.mESTNotifiers != null) {
            Iterator<EditStyledTextNotifier> it = this.mESTNotifiers.iterator();
            while (it.hasNext()) {
                z |= it.next().isButtonsFocused();
            }
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyStateChanged(int i, int i2) {
        if (this.mESTNotifiers != null) {
            Iterator<EditStyledTextNotifier> it = this.mESTNotifiers.iterator();
            while (it.hasNext()) {
                it.next().onStateChanged(i, i2);
            }
        }
    }

    public void onStartEdit() {
        this.mManager.onAction(20);
    }

    public void onEndEdit() {
        this.mManager.onAction(21);
    }

    public void onStartCopy() {
        this.mManager.onAction(1);
    }

    public void onStartCut() {
        this.mManager.onAction(7);
    }

    public void onStartPaste() {
        this.mManager.onAction(2);
    }

    public void onStartSelect() {
        this.mManager.onStartSelect(true);
    }

    public void onStartSelectAll() {
        this.mManager.onStartSelectAll(true);
    }

    public void onFixSelectedItem() {
        this.mManager.onFixSelectedItem();
    }

    public void onInsertHorizontalLine() {
        this.mManager.onAction(12);
    }

    public void onClearStyles() {
        this.mManager.onClearStyles();
    }

    private void onRefreshStyles() {
        this.mManager.onRefreshStyles();
    }

    @Override // android.view.View
    public void setBackgroundColor(int i) {
        if (i != 16777215) {
            super.setBackgroundColor(i);
        } else {
            setBackgroundDrawable(this.mDefaultBackground);
        }
        this.mManager.setBackgroundColor(i);
        onRefreshStyles();
    }

    public boolean isEditting() {
        return this.mManager.isEditting();
    }

    public boolean isStyledText() {
        return this.mManager.isStyledText();
    }

    public boolean isSoftKeyBlocked() {
        return this.mManager.isSoftKeyBlocked();
    }

    public int getSelectState() {
        return this.mManager.getSelectState();
    }

    public int getBackgroundColor() {
        return this.mManager.getBackgroundColor();
    }

    public int getForegroundColor(int i) {
        if (i < 0 || i > getText().length()) {
            return -16777216;
        }
        ForegroundColorSpan[] foregroundColorSpanArr = (ForegroundColorSpan[]) getText().getSpans(i, i, ForegroundColorSpan.class);
        if (foregroundColorSpanArr.length <= 0) {
            return -16777216;
        }
        return foregroundColorSpanArr[0].getForegroundColor();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void stopSelecting(View view, Spannable spannable) {
        spannable.removeSpan(SELECTING);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class EditorManager {
        private EditModeActions mActions;
        private int mBackgroundColor;
        private int mColorWaitInput;
        private BackgroundColorSpan mComposingTextMask;
        private SpannableStringBuilder mCopyBuffer;
        private int mCurEnd;
        private int mCurStart;
        private EditStyledText mEST;
        private boolean mEditFlag;
        private boolean mKeepNonLineSpan;
        private int mMode;
        private int mSizeWaitInput;
        private SoftKeyReceiver mSkr;
        private boolean mSoftKeyBlockFlag;
        private int mState;
        private boolean mTextIsFinishedFlag;
        private boolean mWaitInputFlag;
        final /* synthetic */ EditStyledText this$0;

        public void onAction(int i) {
            onAction(i, true);
        }

        public void onAction(int i, boolean z) {
            this.mActions.onAction(i);
            if (z) {
                this.mEST.notifyStateChanged(this.mMode, this.mState);
            }
        }

        public void onStartSelect(boolean z) {
            Log.d("EditStyledText.EditorManager", "--- onClickSelect");
            this.mMode = 5;
            if (this.mState == 0) {
                this.mActions.onSelectAction();
            } else {
                unsetSelect();
                this.mActions.onSelectAction();
            }
            if (z) {
                this.mEST.notifyStateChanged(this.mMode, this.mState);
            }
        }

        public void onCursorMoved() {
            Log.d("EditStyledText.EditorManager", "--- onClickView");
            if (this.mState == 1 || this.mState == 2) {
                this.mActions.onSelectAction();
                this.mEST.notifyStateChanged(this.mMode, this.mState);
            }
        }

        public void onStartSelectAll(boolean z) {
            Log.d("EditStyledText.EditorManager", "--- onClickSelectAll");
            handleSelectAll();
            if (z) {
                this.mEST.notifyStateChanged(this.mMode, this.mState);
            }
        }

        public void onFixSelectedItem() {
            Log.d("EditStyledText.EditorManager", "--- onFixSelectedItem");
            fixSelectionAndDoNextAction();
            this.mEST.notifyStateChanged(this.mMode, this.mState);
        }

        public void onClearStyles() {
            this.mActions.onAction(14);
        }

        public void onRefreshStyles() {
            Log.d("EditStyledText.EditorManager", "--- onRefreshStyles");
            Editable text = this.mEST.getText();
            int length = text.length();
            int width = this.mEST.getWidth();
            EditStyledTextSpans.HorizontalLineSpan[] horizontalLineSpanArr = (EditStyledTextSpans.HorizontalLineSpan[]) text.getSpans(0, length, EditStyledTextSpans.HorizontalLineSpan.class);
            for (EditStyledTextSpans.HorizontalLineSpan horizontalLineSpan : horizontalLineSpanArr) {
                horizontalLineSpan.resetWidth(width);
            }
            for (EditStyledTextSpans.MarqueeSpan marqueeSpan : (EditStyledTextSpans.MarqueeSpan[]) text.getSpans(0, length, EditStyledTextSpans.MarqueeSpan.class)) {
                marqueeSpan.resetColor(this.mEST.getBackgroundColor());
            }
            if (horizontalLineSpanArr.length > 0) {
                text.replace(0, 1, "" + text.charAt(0));
            }
        }

        public void setBackgroundColor(int i) {
            this.mBackgroundColor = i;
        }

        public void setTextComposingMask(int i, int i2) {
            int foregroundColor;
            Log.d("EditStyledText", "--- setTextComposingMask:" + i + "," + i2);
            int min = Math.min(i, i2);
            int max = Math.max(i, i2);
            if (isWaitInput() && this.mColorWaitInput != 16777215) {
                foregroundColor = this.mColorWaitInput;
            } else {
                foregroundColor = this.mEST.getForegroundColor(min);
            }
            int backgroundColor = this.mEST.getBackgroundColor();
            Log.d("EditStyledText", "--- fg:" + Integer.toHexString(foregroundColor) + ",bg:" + Integer.toHexString(backgroundColor) + "," + isWaitInput() + ",," + this.mMode);
            if (foregroundColor == backgroundColor) {
                int i3 = Integer.MIN_VALUE | ((backgroundColor | (-16777216)) ^ (-1));
                if (this.mComposingTextMask == null || this.mComposingTextMask.getBackgroundColor() != i3) {
                    this.mComposingTextMask = new BackgroundColorSpan(i3);
                }
                this.mEST.getText().setSpan(this.mComposingTextMask, min, max, 33);
            }
        }

        public void unsetTextComposingMask() {
            Log.d("EditStyledText", "--- unsetTextComposingMask");
            if (this.mComposingTextMask != null) {
                this.mEST.getText().removeSpan(this.mComposingTextMask);
                this.mComposingTextMask = null;
            }
        }

        public boolean isEditting() {
            return this.mEditFlag;
        }

        public boolean isStyledText() {
            Editable text = this.mEST.getText();
            int length = text.length();
            return ((ParagraphStyle[]) text.getSpans(0, length, ParagraphStyle.class)).length > 0 || ((QuoteSpan[]) text.getSpans(0, length, QuoteSpan.class)).length > 0 || ((CharacterStyle[]) text.getSpans(0, length, CharacterStyle.class)).length > 0 || this.mBackgroundColor != 16777215;
        }

        public boolean isSoftKeyBlocked() {
            return this.mSoftKeyBlockFlag;
        }

        public boolean isWaitInput() {
            return this.mWaitInputFlag;
        }

        public int getBackgroundColor() {
            return this.mBackgroundColor;
        }

        public int getSelectState() {
            return this.mState;
        }

        public void updateSpanPreviousFromCursor(Editable editable, int i, int i2, int i3) {
            Object[] spans;
            int findLineEnd;
            Log.d("EditStyledText.EditorManager", "updateSpanPrevious:" + i + "," + i2 + "," + i3);
            int i4 = i + i3;
            int min = Math.min(i, i4);
            int max = Math.max(i, i4);
            for (Object obj : editable.getSpans(min, min, Object.class)) {
                if ((obj instanceof ForegroundColorSpan) || (obj instanceof AbsoluteSizeSpan) || (obj instanceof EditStyledTextSpans.MarqueeSpan) || (obj instanceof AlignmentSpan)) {
                    int spanStart = editable.getSpanStart(obj);
                    int spanEnd = editable.getSpanEnd(obj);
                    Log.d("EditStyledText.EditorManager", "spantype:" + obj.getClass() + "," + spanStart);
                    if ((obj instanceof EditStyledTextSpans.MarqueeSpan) || (obj instanceof AlignmentSpan)) {
                        findLineEnd = findLineEnd(this.mEST.getText(), max);
                    } else {
                        findLineEnd = this.mKeepNonLineSpan ? spanEnd : max;
                    }
                    if (spanEnd < findLineEnd) {
                        Log.d("EditStyledText.EditorManager", "updateSpanPrevious: extend span");
                        editable.setSpan(obj, spanStart, findLineEnd, 33);
                    }
                } else if (obj instanceof EditStyledTextSpans.HorizontalLineSpan) {
                    int spanStart2 = editable.getSpanStart(obj);
                    int spanEnd2 = editable.getSpanEnd(obj);
                    if (i2 > i3) {
                        editable.replace(spanStart2, spanEnd2, "");
                        editable.removeSpan(obj);
                    } else if (spanEnd2 == i4 && i4 < editable.length() && this.mEST.getText().charAt(i4) != '\n') {
                        this.mEST.getText().insert(i4, "\n");
                    }
                }
            }
        }

        public void updateSpanNextToCursor(Editable editable, int i, int i2, int i3) {
            Object[] spans;
            int findLineStart;
            Log.d("EditStyledText.EditorManager", "updateSpanNext:" + i + "," + i2 + "," + i3);
            int i4 = i + i3;
            int min = Math.min(i, i4);
            int max = Math.max(i, i4);
            for (Object obj : editable.getSpans(max, max, Object.class)) {
                boolean z = obj instanceof EditStyledTextSpans.MarqueeSpan;
                if (z || (obj instanceof AlignmentSpan)) {
                    int spanStart = editable.getSpanStart(obj);
                    int spanEnd = editable.getSpanEnd(obj);
                    Log.d("EditStyledText.EditorManager", "spantype:" + obj.getClass() + "," + spanEnd);
                    if (z || (obj instanceof AlignmentSpan)) {
                        findLineStart = findLineStart(this.mEST.getText(), min);
                    } else {
                        findLineStart = min;
                    }
                    if (findLineStart < spanStart && i2 > i3) {
                        editable.removeSpan(obj);
                    } else if (spanStart > min) {
                        editable.setSpan(obj, min, spanEnd, 33);
                    }
                } else if ((obj instanceof EditStyledTextSpans.HorizontalLineSpan) && editable.getSpanStart(obj) == i4 && i4 > 0 && this.mEST.getText().charAt(i4 - 1) != '\n') {
                    this.mEST.getText().insert(i4, "\n");
                    this.mEST.setSelection(i4);
                }
            }
        }

        public boolean canPaste() {
            return this.mCopyBuffer != null && this.mCopyBuffer.length() > 0 && removeImageChar(this.mCopyBuffer).length() == 0;
        }

        private void endEdit() {
            Log.d("EditStyledText.EditorManager", "--- handleCancel");
            this.mMode = 0;
            this.mState = 0;
            this.mEditFlag = false;
            this.mColorWaitInput = 16777215;
            this.mSizeWaitInput = 0;
            this.mWaitInputFlag = false;
            this.mSoftKeyBlockFlag = false;
            this.mKeepNonLineSpan = false;
            this.mTextIsFinishedFlag = false;
            unsetSelect();
            this.mEST.setOnClickListener(null);
            unblockSoftKey();
        }

        private void fixSelectionAndDoNextAction() {
            Log.d("EditStyledText.EditorManager", "--- handleComplete:" + this.mCurStart + "," + this.mCurEnd);
            if (!this.mEditFlag) {
                return;
            }
            if (this.mCurStart == this.mCurEnd) {
                Log.d("EditStyledText.EditorManager", "--- cancel handle complete:" + this.mCurStart);
                resetEdit();
                return;
            }
            if (this.mState == 2) {
                this.mState = 3;
            }
            this.mActions.doNext(this.mMode);
            EditStyledText.stopSelecting(this.mEST, this.mEST.getText());
        }

        private SpannableStringBuilder removeImageChar(SpannableStringBuilder spannableStringBuilder) {
            DynamicDrawableSpan[] dynamicDrawableSpanArr;
            SpannableStringBuilder spannableStringBuilder2 = new SpannableStringBuilder(spannableStringBuilder);
            for (DynamicDrawableSpan dynamicDrawableSpan : (DynamicDrawableSpan[]) spannableStringBuilder2.getSpans(0, spannableStringBuilder2.length(), DynamicDrawableSpan.class)) {
                if ((dynamicDrawableSpan instanceof EditStyledTextSpans.HorizontalLineSpan) || (dynamicDrawableSpan instanceof EditStyledTextSpans.RescalableImageSpan)) {
                    spannableStringBuilder2.replace(spannableStringBuilder2.getSpanStart(dynamicDrawableSpan), spannableStringBuilder2.getSpanEnd(dynamicDrawableSpan), (CharSequence) "");
                }
            }
            return spannableStringBuilder2;
        }

        private void handleSelectAll() {
            if (!this.mEditFlag) {
                return;
            }
            this.mActions.onAction(11);
        }

        private void resetEdit() {
            endEdit();
            this.mEditFlag = true;
            this.mEST.notifyStateChanged(this.mMode, this.mState);
        }

        private void unsetSelect() {
            Log.d("EditStyledText.EditorManager", "--- offSelect");
            EditStyledText.stopSelecting(this.mEST, this.mEST.getText());
            int selectionStart = this.mEST.getSelectionStart();
            this.mEST.setSelection(selectionStart, selectionStart);
            this.mState = 0;
        }

        private int findLineStart(Editable editable, int i) {
            int i2 = i;
            while (i2 > 0 && editable.charAt(i2 - 1) != '\n') {
                i2--;
            }
            Log.d("EditStyledText.EditorManager", "--- findLineStart:" + i + "," + editable.length() + "," + i2);
            return i2;
        }

        private int findLineEnd(Editable editable, int i) {
            int i2 = i;
            while (true) {
                if (i2 >= editable.length()) {
                    break;
                } else if (editable.charAt(i2) != '\n') {
                    i2++;
                } else {
                    i2++;
                    break;
                }
            }
            Log.d("EditStyledText.EditorManager", "--- findLineEnd:" + i + "," + editable.length() + "," + i2);
            return i2;
        }

        public void showSoftKey(int i, int i2) {
            Log.d("EditStyledText.EditorManager", "--- showsoftkey");
            if (!this.mEST.isFocused() || isSoftKeyBlocked()) {
                return;
            }
            this.mSkr.mNewStart = Selection.getSelectionStart(this.mEST.getText());
            this.mSkr.mNewEnd = Selection.getSelectionEnd(this.mEST.getText());
            if (((InputMethodManager) this.this$0.getContext().getSystemService("input_method")).showSoftInput(this.mEST, 0, this.mSkr) && this.mSkr != null) {
                Selection.setSelection(this.this$0.getText(), i, i2);
            }
        }

        public void hideSoftKey() {
            Log.d("EditStyledText.EditorManager", "--- hidesoftkey");
            if (!this.mEST.isFocused()) {
                return;
            }
            this.mSkr.mNewStart = Selection.getSelectionStart(this.mEST.getText());
            this.mSkr.mNewEnd = Selection.getSelectionEnd(this.mEST.getText());
            ((InputMethodManager) this.mEST.getContext().getSystemService("input_method")).hideSoftInputFromWindow(this.mEST.getWindowToken(), 0, this.mSkr);
        }

        public void blockSoftKey() {
            Log.d("EditStyledText.EditorManager", "--- blockSoftKey:");
            hideSoftKey();
            this.mSoftKeyBlockFlag = true;
        }

        public void unblockSoftKey() {
            Log.d("EditStyledText.EditorManager", "--- unblockSoftKey:");
            this.mSoftKeyBlockFlag = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class SoftKeyReceiver extends ResultReceiver {
        EditStyledText mEST;
        int mNewEnd;
        int mNewStart;

        @Override // android.os.ResultReceiver
        protected void onReceiveResult(int i, Bundle bundle) {
            if (i != 2) {
                Selection.setSelection(this.mEST.getText(), this.mNewStart, this.mNewEnd);
            }
        }
    }

    /* loaded from: classes.dex */
    public static class SavedStyledTextState extends View.BaseSavedState {
        public int mBackgroundColor;

        SavedStyledTextState(Parcelable parcelable) {
            super(parcelable);
        }

        @Override // android.view.View.BaseSavedState, android.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeInt(this.mBackgroundColor);
        }

        public String toString() {
            return "EditStyledText.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " bgcolor=" + this.mBackgroundColor + "}";
        }
    }

    /* loaded from: classes.dex */
    private class MenuHandler implements MenuItem.OnMenuItemClickListener {
        private MenuHandler() {
        }

        @Override // android.view.MenuItem.OnMenuItemClickListener
        public boolean onMenuItemClick(MenuItem menuItem) {
            return EditStyledText.this.onTextContextMenuItem(menuItem.getItemId());
        }
    }

    /* loaded from: classes.dex */
    public static class StyledTextInputConnection extends InputConnectionWrapper {
        EditStyledText mEST;

        public StyledTextInputConnection(InputConnection inputConnection, EditStyledText editStyledText) {
            super(inputConnection, true);
            this.mEST = editStyledText;
        }

        @Override // android.view.inputmethod.InputConnectionWrapper, android.view.inputmethod.InputConnection
        public boolean commitText(CharSequence charSequence, int i) {
            Log.d("EditStyledText", "--- commitText:");
            this.mEST.mManager.unsetTextComposingMask();
            return super.commitText(charSequence, i);
        }

        @Override // android.view.inputmethod.InputConnectionWrapper, android.view.inputmethod.InputConnection
        public boolean finishComposingText() {
            Log.d("EditStyledText", "--- finishcomposing:");
            if (!this.mEST.isSoftKeyBlocked() && !this.mEST.isButtonsFocused() && !this.mEST.isEditting()) {
                this.mEST.onEndEdit();
            }
            return super.finishComposingText();
        }
    }

    /* loaded from: classes.dex */
    public static class EditStyledTextSpans {

        /* loaded from: classes.dex */
        public static class HorizontalLineSpan extends DynamicDrawableSpan {
            HorizontalLineDrawable mDrawable;

            @Override // android.text.style.DynamicDrawableSpan
            public Drawable getDrawable() {
                return this.mDrawable;
            }

            public void resetWidth(int i) {
                this.mDrawable.renewBounds(i);
            }
        }

        /* loaded from: classes.dex */
        public static class MarqueeSpan extends CharacterStyle {
            private int mMarqueeColor;
            private int mType;

            public void resetColor(int i) {
                this.mMarqueeColor = getMarqueeColor(this.mType, i);
            }

            private int getMarqueeColor(int i, int i2) {
                int alpha = Color.alpha(i2);
                int red = Color.red(i2);
                int green = Color.green(i2);
                int blue = Color.blue(i2);
                if (alpha == 0) {
                    alpha = 128;
                }
                switch (i) {
                    case 0:
                        if (red > 128) {
                            red /= 2;
                            break;
                        } else {
                            red = (255 - red) / 2;
                            break;
                        }
                    case 1:
                        if (green > 128) {
                            green /= 2;
                            break;
                        } else {
                            green = (255 - green) / 2;
                            break;
                        }
                    case 2:
                        return 16777215;
                    default:
                        Log.e("EditStyledText", "--- getMarqueeColor: got illigal marquee ID.");
                        return 16777215;
                }
                return Color.argb(alpha, red, green, blue);
            }

            @Override // android.text.style.CharacterStyle
            public void updateDrawState(TextPaint textPaint) {
                textPaint.bgColor = this.mMarqueeColor;
            }
        }

        /* loaded from: classes.dex */
        public static class RescalableImageSpan extends ImageSpan {
            private final int MAXWIDTH;
            Uri mContentUri;
            private Context mContext;
            private Drawable mDrawable;
            public int mIntrinsicHeight;
            public int mIntrinsicWidth;

            @Override // android.text.style.ImageSpan, android.text.style.DynamicDrawableSpan
            public Drawable getDrawable() {
                Bitmap decodeStream;
                if (this.mDrawable != null) {
                    return this.mDrawable;
                }
                if (this.mContentUri != null) {
                    System.gc();
                    try {
                        InputStream openInputStream = this.mContext.getContentResolver().openInputStream(this.mContentUri);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(openInputStream, null, options);
                        openInputStream.close();
                        InputStream openInputStream2 = this.mContext.getContentResolver().openInputStream(this.mContentUri);
                        int i = options.outWidth;
                        int i2 = options.outHeight;
                        this.mIntrinsicWidth = i;
                        this.mIntrinsicHeight = i2;
                        if (options.outWidth > this.MAXWIDTH) {
                            i = this.MAXWIDTH;
                            i2 = (i2 * this.MAXWIDTH) / options.outWidth;
                            decodeStream = BitmapFactory.decodeStream(openInputStream2, new Rect(0, 0, i, i2), null);
                        } else {
                            decodeStream = BitmapFactory.decodeStream(openInputStream2);
                        }
                        this.mDrawable = new BitmapDrawable(this.mContext.getResources(), decodeStream);
                        this.mDrawable.setBounds(0, 0, i, i2);
                        openInputStream2.close();
                    } catch (Exception e) {
                        Log.e("EditStyledTextSpan", "Failed to loaded content " + this.mContentUri, e);
                        return null;
                    } catch (OutOfMemoryError e2) {
                        Log.e("EditStyledTextSpan", "OutOfMemoryError");
                        return null;
                    }
                } else {
                    this.mDrawable = super.getDrawable();
                    rescaleBigImage(this.mDrawable);
                    this.mIntrinsicWidth = this.mDrawable.getIntrinsicWidth();
                    this.mIntrinsicHeight = this.mDrawable.getIntrinsicHeight();
                }
                return this.mDrawable;
            }

            private void rescaleBigImage(Drawable drawable) {
                Log.d("EditStyledTextSpan", "--- rescaleBigImage:");
                if (this.MAXWIDTH < 0) {
                    return;
                }
                int intrinsicWidth = drawable.getIntrinsicWidth();
                int intrinsicHeight = drawable.getIntrinsicHeight();
                Log.d("EditStyledTextSpan", "--- rescaleBigImage:" + intrinsicWidth + "," + intrinsicHeight + "," + this.MAXWIDTH);
                if (intrinsicWidth > this.MAXWIDTH) {
                    intrinsicWidth = this.MAXWIDTH;
                    intrinsicHeight = (intrinsicHeight * this.MAXWIDTH) / intrinsicWidth;
                }
                drawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
            }
        }

        /* loaded from: classes.dex */
        public static class HorizontalLineDrawable extends ShapeDrawable {
            private static boolean DBG_HL = false;
            private Spannable mSpannable;
            private int mWidth;

            @Override // android.graphics.drawable.ShapeDrawable, android.graphics.drawable.Drawable
            public void draw(Canvas canvas) {
                renewColor();
                canvas.drawRect(new Rect(0, 9, this.mWidth, 11), getPaint());
            }

            public void renewBounds(int i) {
                if (DBG_HL) {
                    Log.d("EditStyledTextSpan", "--- renewBounds:" + i);
                }
                if (i > 20) {
                    i -= 20;
                }
                this.mWidth = i;
                setBounds(0, 0, i, 20);
            }

            private void renewColor(int i) {
                if (DBG_HL) {
                    Log.d("EditStyledTextSpan", "--- renewColor:" + i);
                }
                getPaint().setColor(i);
            }

            private void renewColor() {
                HorizontalLineSpan parentSpan = getParentSpan();
                Spannable spannable = this.mSpannable;
                ForegroundColorSpan[] foregroundColorSpanArr = (ForegroundColorSpan[]) spannable.getSpans(spannable.getSpanStart(parentSpan), spannable.getSpanEnd(parentSpan), ForegroundColorSpan.class);
                if (DBG_HL) {
                    Log.d("EditStyledTextSpan", "--- renewColor:" + foregroundColorSpanArr.length);
                }
                if (foregroundColorSpanArr.length > 0) {
                    renewColor(foregroundColorSpanArr[foregroundColorSpanArr.length - 1].getForegroundColor());
                }
            }

            private HorizontalLineSpan getParentSpan() {
                Spannable spannable = this.mSpannable;
                HorizontalLineSpan[] horizontalLineSpanArr = (HorizontalLineSpan[]) spannable.getSpans(0, spannable.length(), HorizontalLineSpan.class);
                if (horizontalLineSpanArr.length > 0) {
                    for (HorizontalLineSpan horizontalLineSpan : horizontalLineSpanArr) {
                        if (horizontalLineSpan.getDrawable() == this) {
                            return horizontalLineSpan;
                        }
                    }
                }
                Log.e("EditStyledTextSpan", "---renewBounds: Couldn't find");
                return null;
            }
        }
    }

    /* loaded from: classes.dex */
    public class EditModeActions {
        private HashMap<Integer, EditModeActionBase> mActionMap;
        private EditorManager mManager;
        private int mMode;

        public void onAction(int i, Object[] objArr) {
            getAction(i).addParams(objArr);
            this.mMode = i;
            doNext(i);
        }

        public void onAction(int i) {
            onAction(i, null);
        }

        public void onSelectAction() {
            doNext(5);
        }

        private EditModeActionBase getAction(int i) {
            if (this.mActionMap.containsKey(Integer.valueOf(i))) {
                return this.mActionMap.get(Integer.valueOf(i));
            }
            return null;
        }

        public boolean doNext(int i) {
            Log.d("EditModeActions", "--- do the next action: " + i + "," + this.mManager.getSelectState());
            EditModeActionBase action = getAction(i);
            if (action == null) {
                Log.e("EditModeActions", "--- invalid action error.");
                return false;
            }
            switch (this.mManager.getSelectState()) {
                case 0:
                    return action.doNotSelected();
                case 1:
                    return action.doStartPosIsSelected();
                case 2:
                    return action.doEndPosIsSelected();
                case 3:
                    if (this.mManager.isWaitInput()) {
                        return action.doSelectionIsFixedAndWaitingInput();
                    }
                    return action.doSelectionIsFixed();
                default:
                    return false;
            }
        }

        /* loaded from: classes.dex */
        public class EditModeActionBase {
            private Object[] mParams;

            protected boolean doNotSelected() {
                return false;
            }

            protected boolean doStartPosIsSelected() {
                return doNotSelected();
            }

            protected boolean doEndPosIsSelected() {
                return doStartPosIsSelected();
            }

            protected boolean doSelectionIsFixed() {
                return doEndPosIsSelected();
            }

            protected boolean doSelectionIsFixedAndWaitingInput() {
                return doEndPosIsSelected();
            }

            protected void addParams(Object[] objArr) {
                this.mParams = objArr;
            }
        }
    }
}
