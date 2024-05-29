package com.android.systemui.statusbar.policy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import com.android.settingslib.animation.AppearAnimationUtils;
import com.android.systemui.Interpolators;
import com.android.systemui.qs.tiles.UserDetailItemView;
import com.android.systemui.statusbar.phone.KeyguardStatusBarView;
import com.android.systemui.statusbar.phone.NotificationPanelView;
import com.android.systemui.statusbar.policy.UserSwitcherController;
/* loaded from: a.zip:com/android/systemui/statusbar/policy/KeyguardUserSwitcher.class */
public class KeyguardUserSwitcher {
    private final Adapter mAdapter;
    private boolean mAnimating;
    private final AppearAnimationUtils mAppearAnimationUtils;
    private final KeyguardUserSwitcherScrim mBackground;
    private ObjectAnimator mBgAnimator;
    public final DataSetObserver mDataSetObserver = new DataSetObserver(this) { // from class: com.android.systemui.statusbar.policy.KeyguardUserSwitcher.1
        final KeyguardUserSwitcher this$0;

        {
            this.this$0 = this;
        }

        @Override // android.database.DataSetObserver
        public void onChanged() {
            this.this$0.refresh();
        }
    };
    private final KeyguardStatusBarView mStatusBarView;
    private ViewGroup mUserSwitcher;
    private final Container mUserSwitcherContainer;
    private UserSwitcherController mUserSwitcherController;

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/KeyguardUserSwitcher$Adapter.class */
    public static class Adapter extends UserSwitcherController.BaseUserAdapter implements View.OnClickListener {
        private Context mContext;
        private KeyguardUserSwitcher mKeyguardUserSwitcher;

        public Adapter(Context context, UserSwitcherController userSwitcherController, KeyguardUserSwitcher keyguardUserSwitcher) {
            super(userSwitcherController);
            this.mContext = context;
            this.mKeyguardUserSwitcher = keyguardUserSwitcher;
        }

        @Override // android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            UserSwitcherController.UserRecord item = getItem(i);
            if (!(view instanceof UserDetailItemView) || !(view.getTag() instanceof UserSwitcherController.UserRecord)) {
                view = LayoutInflater.from(this.mContext).inflate(2130968649, viewGroup, false);
                view.setOnClickListener(this);
            }
            UserDetailItemView userDetailItemView = (UserDetailItemView) view;
            String name = getName(this.mContext, item);
            if (item.picture == null) {
                userDetailItemView.bind(name, getDrawable(this.mContext, item).mutate(), item.resolveId());
            } else {
                userDetailItemView.bind(name, item.picture, item.info.id);
            }
            userDetailItemView.setAvatarEnabled(item.isSwitchToEnabled);
            view.setActivated(item.isCurrent);
            view.setTag(item);
            return view;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            UserSwitcherController.UserRecord userRecord = (UserSwitcherController.UserRecord) view.getTag();
            if (userRecord.isCurrent && !userRecord.isGuest) {
                this.mKeyguardUserSwitcher.hideIfNotSimple(true);
            } else if (userRecord.isSwitchToEnabled) {
                switchTo(userRecord);
            }
        }
    }

    /* loaded from: a.zip:com/android/systemui/statusbar/policy/KeyguardUserSwitcher$Container.class */
    public static class Container extends FrameLayout {
        private KeyguardUserSwitcher mKeyguardUserSwitcher;

        public Container(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            setClipChildren(false);
        }

        @Override // android.view.View
        public boolean onTouchEvent(MotionEvent motionEvent) {
            if (this.mKeyguardUserSwitcher == null || this.mKeyguardUserSwitcher.isAnimating()) {
                return false;
            }
            this.mKeyguardUserSwitcher.hideIfNotSimple(true);
            return false;
        }

        public void setKeyguardUserSwitcher(KeyguardUserSwitcher keyguardUserSwitcher) {
            this.mKeyguardUserSwitcher = keyguardUserSwitcher;
        }
    }

    public KeyguardUserSwitcher(Context context, ViewStub viewStub, KeyguardStatusBarView keyguardStatusBarView, NotificationPanelView notificationPanelView, UserSwitcherController userSwitcherController) {
        boolean z = context.getResources().getBoolean(2131623958);
        if (userSwitcherController == null || !z) {
            this.mUserSwitcherContainer = null;
            this.mStatusBarView = null;
            this.mAdapter = null;
            this.mAppearAnimationUtils = null;
            this.mBackground = null;
            return;
        }
        this.mUserSwitcherContainer = (Container) viewStub.inflate();
        this.mBackground = new KeyguardUserSwitcherScrim(context);
        reinflateViews();
        this.mStatusBarView = keyguardStatusBarView;
        this.mStatusBarView.setKeyguardUserSwitcher(this);
        notificationPanelView.setKeyguardUserSwitcher(this);
        this.mAdapter = new Adapter(context, userSwitcherController, this);
        this.mAdapter.registerDataSetObserver(this.mDataSetObserver);
        this.mUserSwitcherController = userSwitcherController;
        this.mAppearAnimationUtils = new AppearAnimationUtils(context, 400L, -0.5f, 0.5f, Interpolators.FAST_OUT_SLOW_IN);
        this.mUserSwitcherContainer.setKeyguardUserSwitcher(this);
    }

    private void cancelAnimations() {
        int childCount = this.mUserSwitcher.getChildCount();
        for (int i = 0; i < childCount; i++) {
            this.mUserSwitcher.getChildAt(i).animate().cancel();
        }
        if (this.mBgAnimator != null) {
            this.mBgAnimator.cancel();
        }
        this.mUserSwitcher.animate().cancel();
        this.mAnimating = false;
    }

    private void hide(boolean z) {
        if (this.mUserSwitcher == null || this.mUserSwitcherContainer.getVisibility() != 0) {
            return;
        }
        cancelAnimations();
        if (z) {
            startDisappearAnimation();
        } else {
            this.mUserSwitcherContainer.setVisibility(8);
        }
        this.mStatusBarView.setKeyguardUserSwitcherShowing(false, z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void refresh() {
        int childCount = this.mUserSwitcher.getChildCount();
        int count = this.mAdapter.getCount();
        int max = Math.max(childCount, count);
        for (int i = 0; i < max; i++) {
            if (i < count) {
                View childAt = i < childCount ? this.mUserSwitcher.getChildAt(i) : null;
                View view = this.mAdapter.getView(i, childAt, this.mUserSwitcher);
                if (childAt == null) {
                    this.mUserSwitcher.addView(view);
                } else if (childAt != view) {
                    this.mUserSwitcher.removeViewAt(i);
                    this.mUserSwitcher.addView(view, i);
                }
            } else {
                this.mUserSwitcher.removeViewAt(this.mUserSwitcher.getChildCount() - 1);
            }
        }
    }

    private void reinflateViews() {
        if (this.mUserSwitcher != null) {
            this.mUserSwitcher.setBackground(null);
            this.mUserSwitcher.removeOnLayoutChangeListener(this.mBackground);
        }
        this.mUserSwitcherContainer.removeAllViews();
        LayoutInflater.from(this.mUserSwitcherContainer.getContext()).inflate(2130968648, this.mUserSwitcherContainer);
        this.mUserSwitcher = (ViewGroup) this.mUserSwitcherContainer.findViewById(2131886358);
        this.mUserSwitcher.addOnLayoutChangeListener(this.mBackground);
        this.mUserSwitcher.setBackground(this.mBackground);
    }

    private boolean shouldExpandByDefault() {
        return this.mUserSwitcherController != null ? this.mUserSwitcherController.isSimpleUserSwitcher() : false;
    }

    private void startAppearAnimation() {
        int childCount = this.mUserSwitcher.getChildCount();
        View[] viewArr = new View[childCount];
        for (int i = 0; i < childCount; i++) {
            viewArr[i] = this.mUserSwitcher.getChildAt(i);
        }
        this.mUserSwitcher.setClipChildren(false);
        this.mUserSwitcher.setClipToPadding(false);
        this.mAppearAnimationUtils.startAnimation(viewArr, new Runnable(this) { // from class: com.android.systemui.statusbar.policy.KeyguardUserSwitcher.2
            final KeyguardUserSwitcher this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mUserSwitcher.setClipChildren(true);
                this.this$0.mUserSwitcher.setClipToPadding(true);
            }
        });
        this.mAnimating = true;
        this.mBgAnimator = ObjectAnimator.ofInt(this.mBackground, "alpha", 0, 255);
        this.mBgAnimator.setDuration(400L);
        this.mBgAnimator.setInterpolator(Interpolators.ALPHA_IN);
        this.mBgAnimator.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.statusbar.policy.KeyguardUserSwitcher.3
            final KeyguardUserSwitcher this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.mBgAnimator = null;
                this.this$0.mAnimating = false;
            }
        });
        this.mBgAnimator.start();
    }

    private void startDisappearAnimation() {
        this.mAnimating = true;
        this.mUserSwitcher.animate().alpha(0.0f).setDuration(300L).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(new Runnable(this) { // from class: com.android.systemui.statusbar.policy.KeyguardUserSwitcher.4
            final KeyguardUserSwitcher this$0;

            {
                this.this$0 = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.mUserSwitcherContainer.setVisibility(8);
                this.this$0.mUserSwitcher.setAlpha(1.0f);
                this.this$0.mAnimating = false;
            }
        });
    }

    public void hideIfNotSimple(boolean z) {
        if (this.mUserSwitcherContainer == null || this.mUserSwitcherController.isSimpleUserSwitcher()) {
            return;
        }
        hide(z);
    }

    boolean isAnimating() {
        return this.mAnimating;
    }

    public void onDensityOrFontScaleChanged() {
        if (this.mUserSwitcherContainer != null) {
            reinflateViews();
            refresh();
        }
    }

    public void setKeyguard(boolean z, boolean z2) {
        if (this.mUserSwitcher != null) {
            if (z && shouldExpandByDefault()) {
                show(z2);
            } else {
                hide(z2);
            }
        }
    }

    public void show(boolean z) {
        if (this.mUserSwitcher == null || this.mUserSwitcherContainer.getVisibility() == 0) {
            return;
        }
        cancelAnimations();
        this.mAdapter.refresh();
        this.mUserSwitcherContainer.setVisibility(0);
        this.mStatusBarView.setKeyguardUserSwitcherShowing(true, z);
        if (z) {
            startAppearAnimation();
        }
    }
}
