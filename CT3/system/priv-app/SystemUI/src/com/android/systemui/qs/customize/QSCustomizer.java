package com.android.systemui.qs.customize;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toolbar;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.qs.QSContainer;
import com.android.systemui.qs.QSDetailClipper;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.phone.NotificationsQuickSettingsContainer;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.mediatek.systemui.PluginManager;
import java.util.ArrayList;
import java.util.Iterator;
/* loaded from: a.zip:com/android/systemui/qs/customize/QSCustomizer.class */
public class QSCustomizer extends LinearLayout implements Toolbar.OnMenuItemClickListener {
    private boolean isShown;
    private final QSDetailClipper mClipper;
    private final Animator.AnimatorListener mCollapseAnimationListener;
    private boolean mCustomizing;
    private final Animator.AnimatorListener mExpandAnimationListener;
    private QSTileHost mHost;
    private final KeyguardMonitor.Callback mKeyguardCallback;
    private NotificationsQuickSettingsContainer mNotifQsContainer;
    private PhoneStatusBar mPhoneStatusBar;
    private QSContainer mQsContainer;
    private RecyclerView mRecyclerView;
    private TileAdapter mTileAdapter;
    private Toolbar mToolbar;

    public QSCustomizer(Context context, AttributeSet attributeSet) {
        super(new ContextThemeWrapper(context, 2131952149), attributeSet);
        this.mKeyguardCallback = new KeyguardMonitor.Callback(this) { // from class: com.android.systemui.qs.customize.QSCustomizer.1
            final QSCustomizer this$0;

            {
                this.this$0 = this;
            }

            @Override // com.android.systemui.statusbar.policy.KeyguardMonitor.Callback
            public void onKeyguardChanged() {
                if (this.this$0.mHost.getKeyguardMonitor().isShowing()) {
                    this.this$0.hide(0, 0);
                }
            }
        };
        this.mExpandAnimationListener = new AnimatorListenerAdapter(this) { // from class: com.android.systemui.qs.customize.QSCustomizer.2
            final QSCustomizer this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.this$0.mNotifQsContainer.setCustomizerAnimating(false);
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                this.this$0.setCustomizing(true);
                this.this$0.mNotifQsContainer.setCustomizerAnimating(false);
            }
        };
        this.mCollapseAnimationListener = new AnimatorListenerAdapter(this) { // from class: com.android.systemui.qs.customize.QSCustomizer.3
            final QSCustomizer this$0;

            {
                this.this$0 = this;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                if (!this.this$0.isShown) {
                    this.this$0.setVisibility(8);
                }
                this.this$0.mNotifQsContainer.setCustomizerAnimating(false);
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (!this.this$0.isShown) {
                    this.this$0.setVisibility(8);
                }
                this.this$0.mNotifQsContainer.setCustomizerAnimating(false);
                this.this$0.mRecyclerView.setAdapter(this.this$0.mTileAdapter);
            }
        };
        this.mClipper = new QSDetailClipper(this);
        LayoutInflater.from(getContext()).inflate(2130968755, this);
        this.mToolbar = (Toolbar) findViewById(16909295);
        TypedValue typedValue = new TypedValue();
        this.mContext.getTheme().resolveAttribute(16843531, typedValue, true);
        this.mToolbar.setNavigationIcon(getResources().getDrawable(typedValue.resourceId, this.mContext.getTheme()));
        this.mToolbar.setNavigationOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.qs.customize.QSCustomizer.4
            final QSCustomizer this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.this$0.hide(((int) view.getX()) + (view.getWidth() / 2), ((int) view.getY()) + (view.getHeight() / 2));
            }
        });
        this.mToolbar.setOnMenuItemClickListener(this);
        this.mToolbar.getMenu().add(0, 1, 0, this.mContext.getString(17040504));
        this.mToolbar.setTitle(2131493879);
        this.mRecyclerView = (RecyclerView) findViewById(16908298);
        this.mTileAdapter = new TileAdapter(getContext());
        this.mRecyclerView.setAdapter(this.mTileAdapter);
        this.mTileAdapter.getItemTouchHelper().attachToRecyclerView(this.mRecyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        gridLayoutManager.setSpanSizeLookup(this.mTileAdapter.getSizeLookup());
        this.mRecyclerView.setLayoutManager(gridLayoutManager);
        this.mRecyclerView.addItemDecoration(this.mTileAdapter.getItemDecoration());
        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator();
        defaultItemAnimator.setMoveDuration(150L);
        this.mRecyclerView.setItemAnimator(defaultItemAnimator);
    }

    private void reset() {
        ArrayList arrayList = new ArrayList();
        for (String str : PluginManager.getQuickSettingsPlugin(this.mContext).customizeQuickSettingsTileOrder(this.mContext.getString(2131493276)).split(",")) {
            arrayList.add(str);
        }
        this.mTileAdapter.setTileSpecs(arrayList);
    }

    private void save() {
        this.mTileAdapter.saveSpecs(this.mHost);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setCustomizing(boolean z) {
        this.mCustomizing = z;
        this.mQsContainer.notifyCustomizeChanged();
    }

    private void setTileSpecs() {
        ArrayList arrayList = new ArrayList();
        Iterator<T> it = this.mHost.getTiles().iterator();
        while (it.hasNext()) {
            arrayList.add(((QSTile) it.next()).getTileSpec());
        }
        this.mTileAdapter.setTileSpecs(arrayList);
        this.mRecyclerView.setAdapter(this.mTileAdapter);
    }

    public void hide(int i, int i2) {
        if (this.isShown) {
            MetricsLogger.hidden(getContext(), 358);
            this.isShown = false;
            this.mToolbar.dismissPopupMenus();
            setCustomizing(false);
            save();
            this.mClipper.animateCircularClip(i, i2, false, this.mCollapseAnimationListener);
            this.mNotifQsContainer.setCustomizerAnimating(true);
            this.mNotifQsContainer.setCustomizerShowing(false);
            announceForAccessibility(this.mContext.getString(2131493444));
            this.mHost.getKeyguardMonitor().removeCallback(this.mKeyguardCallback);
        }
    }

    public boolean isCustomizing() {
        return this.mCustomizing;
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        View findViewById = findViewById(2131886580);
        if (findViewById != null) {
            boolean z = true;
            if (configuration.smallestScreenWidthDp < 600) {
                z = configuration.orientation != 2;
            }
            findViewById.setVisibility(z ? 0 : 8);
        }
    }

    @Override // android.widget.Toolbar.OnMenuItemClickListener
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case 1:
                MetricsLogger.action(getContext(), 359);
                reset();
                return false;
            default:
                return false;
        }
    }

    public void setContainer(NotificationsQuickSettingsContainer notificationsQuickSettingsContainer) {
        this.mNotifQsContainer = notificationsQuickSettingsContainer;
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
        this.mPhoneStatusBar = qSTileHost.getPhoneStatusBar();
        this.mTileAdapter.setHost(qSTileHost);
    }

    public void setQsContainer(QSContainer qSContainer) {
        this.mQsContainer = qSContainer;
    }

    public void show(int i, int i2) {
        if (this.isShown) {
            return;
        }
        MetricsLogger.visible(getContext(), 358);
        this.isShown = true;
        setTileSpecs();
        setVisibility(0);
        this.mClipper.animateCircularClip(i, i2, true, this.mExpandAnimationListener);
        new TileQueryHelper(this.mContext, this.mHost).setListener(this.mTileAdapter);
        this.mNotifQsContainer.setCustomizerAnimating(true);
        this.mNotifQsContainer.setCustomizerShowing(true);
        announceForAccessibility(this.mContext.getString(2131493901));
        this.mHost.getKeyguardMonitor().addCallback(this.mKeyguardCallback);
    }
}
