package com.android.systemui.statusbar.policy;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.RemoteInputController;
import com.android.systemui.statusbar.stack.ScrollContainer;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/RemoteInputView.class */
public class RemoteInputView extends LinearLayout implements View.OnClickListener, TextWatcher {
    public static final Object VIEW_TAG = new Object();
    private RemoteInputController mController;
    private RemoteEditText mEditText;
    private NotificationData.Entry mEntry;
    private PendingIntent mPendingIntent;
    private ProgressBar mProgressBar;
    private RemoteInput mRemoteInput;
    private RemoteInput[] mRemoteInputs;
    private boolean mRemoved;
    private ScrollContainer mScrollContainer;
    private View mScrollContainerChild;
    private ImageButton mSendButton;

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/RemoteInputView$RemoteEditText.class */
    public static class RemoteEditText extends EditText {
        private final Drawable mBackground;
        private RemoteInputView mRemoteInputView;
        boolean mShowImeOnInputConnection;

        public RemoteEditText(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.mBackground = getBackground();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void defocusIfNeeded() {
            if ((this.mRemoteInputView == null || !this.mRemoteInputView.mEntry.row.isChangingPosition()) && isFocusable() && isEnabled()) {
                setInnerFocusable(false);
                if (this.mRemoteInputView != null) {
                    this.mRemoteInputView.onDefocus();
                }
                this.mShowImeOnInputConnection = false;
            }
        }

        @Override // android.widget.TextView, android.view.View
        public void getFocusedRect(Rect rect) {
            super.getFocusedRect(rect);
            rect.top = this.mScrollY;
            rect.bottom = this.mScrollY + (this.mBottom - this.mTop);
        }

        @Override // android.widget.TextView, android.view.View
        public boolean onCheckIsTextEditor() {
            boolean z = false;
            if (!(this.mRemoteInputView != null ? this.mRemoteInputView.mRemoved : false)) {
                z = super.onCheckIsTextEditor();
            }
            return z;
        }

        @Override // android.widget.TextView
        public void onCommitCompletion(CompletionInfo completionInfo) {
            clearComposingText();
            setText(completionInfo.getText());
            setSelection(getText().length());
        }

        @Override // android.widget.TextView, android.view.View
        public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
            InputMethodManager inputMethodManager;
            InputConnection onCreateInputConnection = super.onCreateInputConnection(editorInfo);
            if (this.mShowImeOnInputConnection && onCreateInputConnection != null && (inputMethodManager = InputMethodManager.getInstance()) != null) {
                post(new Runnable(this, inputMethodManager) { // from class: com.android.systemui.statusbar.policy.RemoteInputView.RemoteEditText.1
                    final RemoteEditText this$1;
                    final InputMethodManager val$imm;

                    {
                        this.this$1 = this;
                        this.val$imm = inputMethodManager;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.val$imm.viewClicked(this.this$1);
                        this.val$imm.showSoftInput(this.this$1, 0);
                    }
                });
            }
            return onCreateInputConnection;
        }

        @Override // android.widget.TextView, android.view.View
        protected void onFocusChanged(boolean z, int i, Rect rect) {
            super.onFocusChanged(z, i, rect);
            if (z) {
                return;
            }
            defocusIfNeeded();
        }

        @Override // android.widget.TextView, android.view.View
        public boolean onKeyPreIme(int i, KeyEvent keyEvent) {
            if (i == 4 && keyEvent.getAction() == 1) {
                defocusIfNeeded();
                InputMethodManager.getInstance().hideSoftInputFromWindow(getWindowToken(), 0);
                return true;
            }
            return super.onKeyPreIme(i, keyEvent);
        }

        @Override // android.widget.TextView, android.view.View
        protected void onVisibilityChanged(View view, int i) {
            super.onVisibilityChanged(view, i);
            if (isShown()) {
                return;
            }
            defocusIfNeeded();
        }

        @Override // android.view.View
        public boolean requestRectangleOnScreen(Rect rect) {
            return this.mRemoteInputView.requestScrollTo();
        }

        void setInnerFocusable(boolean z) {
            setFocusableInTouchMode(z);
            setFocusable(z);
            setCursorVisible(z);
            if (!z) {
                setBackground(null);
                return;
            }
            requestFocus();
            setBackground(this.mBackground);
        }
    }

    public RemoteInputView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void findScrollContainer() {
        if (this.mScrollContainer != null) {
            return;
        }
        this.mScrollContainerChild = null;
        ViewParent viewParent = this;
        while (true) {
            ViewParent viewParent2 = viewParent;
            if (viewParent2 == null) {
                return;
            }
            if (this.mScrollContainerChild == null && (viewParent2 instanceof ExpandableView)) {
                this.mScrollContainerChild = (View) viewParent2;
            }
            if (viewParent2.getParent() instanceof ScrollContainer) {
                this.mScrollContainer = (ScrollContainer) viewParent2.getParent();
                if (this.mScrollContainerChild == null) {
                    this.mScrollContainerChild = (View) viewParent2;
                    return;
                }
                return;
            }
            viewParent = viewParent2.getParent();
        }
    }

    public static RemoteInputView inflate(Context context, ViewGroup viewGroup, NotificationData.Entry entry, RemoteInputController remoteInputController) {
        RemoteInputView remoteInputView = (RemoteInputView) LayoutInflater.from(context).inflate(2130968790, viewGroup, false);
        remoteInputView.mController = remoteInputController;
        remoteInputView.mEntry = entry;
        remoteInputView.setTag(VIEW_TAG);
        return remoteInputView;
    }

    private void reset() {
        this.mEditText.getText().clear();
        this.mEditText.setEnabled(true);
        this.mSendButton.setVisibility(0);
        this.mProgressBar.setVisibility(4);
        this.mController.removeSpinning(this.mEntry.key);
        updateSendButton();
        onDefocus();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendRemoteInput() {
        Bundle bundle = new Bundle();
        bundle.putString(this.mRemoteInput.getResultKey(), this.mEditText.getText().toString());
        Intent addFlags = new Intent().addFlags(268435456);
        RemoteInput.addResultsToIntent(this.mRemoteInputs, addFlags, bundle);
        this.mEditText.setEnabled(false);
        this.mSendButton.setVisibility(4);
        this.mProgressBar.setVisibility(0);
        this.mEntry.remoteInputText = this.mEditText.getText();
        this.mController.addSpinning(this.mEntry.key);
        this.mController.removeRemoteInput(this.mEntry);
        this.mEditText.mShowImeOnInputConnection = false;
        this.mController.remoteInputSent(this.mEntry);
        MetricsLogger.action(this.mContext, 398, this.mEntry.notification.getPackageName());
        try {
            this.mPendingIntent.send(this.mContext, 0, addFlags);
        } catch (PendingIntent.CanceledException e) {
            Log.i("RemoteInput", "Unable to send remote input result", e);
            MetricsLogger.action(this.mContext, 399, this.mEntry.notification.getPackageName());
        }
    }

    private void updateSendButton() {
        boolean z = false;
        ImageButton imageButton = this.mSendButton;
        if (this.mEditText.getText().length() != 0) {
            z = true;
        }
        imageButton.setEnabled(z);
    }

    @Override // android.text.TextWatcher
    public void afterTextChanged(Editable editable) {
        updateSendButton();
    }

    @Override // android.text.TextWatcher
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    public void close() {
        this.mEditText.defocusIfNeeded();
    }

    public void focus() {
        MetricsLogger.action(this.mContext, 397, this.mEntry.notification.getPackageName());
        setVisibility(0);
        this.mController.addRemoteInput(this.mEntry);
        this.mEditText.setInnerFocusable(true);
        this.mEditText.mShowImeOnInputConnection = true;
        this.mEditText.setText(this.mEntry.remoteInputText);
        this.mEditText.setSelection(this.mEditText.getText().length());
        this.mEditText.requestFocus();
        updateSendButton();
    }

    public PendingIntent getPendingIntent() {
        return this.mPendingIntent;
    }

    public boolean isActive() {
        return this.mEditText.isFocused();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mEntry.row.isChangingPosition() && getVisibility() == 0 && this.mEditText.isFocusable()) {
            this.mEditText.requestFocus();
        }
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view == this.mSendButton) {
            sendRemoteInput();
        }
    }

    public void onDefocus() {
        this.mController.removeRemoteInput(this.mEntry);
        this.mEntry.remoteInputText = this.mEditText.getText();
        if (!this.mRemoved) {
            setVisibility(4);
        }
        MetricsLogger.action(this.mContext, 400, this.mEntry.notification.getPackageName());
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mEntry.row.isChangingPosition()) {
            return;
        }
        this.mController.removeRemoteInput(this.mEntry);
        this.mController.removeSpinning(this.mEntry.key);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mProgressBar = (ProgressBar) findViewById(2131886637);
        this.mSendButton = (ImageButton) findViewById(2131886636);
        this.mSendButton.setOnClickListener(this);
        this.mEditText = (RemoteEditText) getChildAt(0);
        this.mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener(this) { // from class: com.android.systemui.statusbar.policy.RemoteInputView.1
            final RemoteInputView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.widget.TextView.OnEditorActionListener
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean z = keyEvent == null ? (i == 6 || i == 5) ? true : i == 4 : false;
                boolean z2 = (keyEvent == null || !KeyEvent.isConfirmKey(keyEvent.getKeyCode())) ? false : keyEvent.getAction() == 0;
                if (z || z2) {
                    if (this.this$0.mEditText.length() > 0) {
                        this.this$0.sendRemoteInput();
                        return true;
                    }
                    return true;
                }
                return false;
            }
        });
        this.mEditText.addTextChangedListener(this);
        this.mEditText.setInnerFocusable(false);
        this.mEditText.mRemoteInputView = this;
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            findScrollContainer();
            if (this.mScrollContainer != null) {
                this.mScrollContainer.requestDisallowLongPress();
                this.mScrollContainer.requestDisallowDismiss();
            }
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    public void onNotificationUpdateOrReset() {
        boolean z = false;
        if (this.mProgressBar.getVisibility() == 0) {
            z = true;
        }
        if (z) {
            reset();
        }
    }

    @Override // android.text.TextWatcher
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        super.onTouchEvent(motionEvent);
        return true;
    }

    public boolean requestScrollTo() {
        findScrollContainer();
        this.mScrollContainer.lockScrollTo(this.mScrollContainerChild);
        return true;
    }

    public void setPendingIntent(PendingIntent pendingIntent) {
        this.mPendingIntent = pendingIntent;
    }

    public void setRemoteInput(RemoteInput[] remoteInputArr, RemoteInput remoteInput) {
        this.mRemoteInputs = remoteInputArr;
        this.mRemoteInput = remoteInput;
        this.mEditText.setHint(this.mRemoteInput.getLabel());
    }

    public void setRemoved() {
        this.mRemoved = true;
    }

    public void stealFocusFrom(RemoteInputView remoteInputView) {
        remoteInputView.close();
        setPendingIntent(remoteInputView.mPendingIntent);
        setRemoteInput(remoteInputView.mRemoteInputs, remoteInputView.mRemoteInput);
        focus();
    }

    public boolean updatePendingIntentFromActions(Notification.Action[] actionArr) {
        Intent intent;
        if (this.mPendingIntent == null || actionArr == null || (intent = this.mPendingIntent.getIntent()) == null) {
            return false;
        }
        for (Notification.Action action : actionArr) {
            RemoteInput[] remoteInputs = action.getRemoteInputs();
            if (action.actionIntent != null && remoteInputs != null && intent.filterEquals(action.actionIntent.getIntent())) {
                RemoteInput remoteInput = null;
                for (RemoteInput remoteInput2 : remoteInputs) {
                    if (remoteInput2.getAllowFreeFormInput()) {
                        remoteInput = remoteInput2;
                    }
                }
                if (remoteInput != null) {
                    setPendingIntent(action.actionIntent);
                    setRemoteInput(remoteInputs, remoteInput);
                    return true;
                }
            }
        }
        return false;
    }
}
