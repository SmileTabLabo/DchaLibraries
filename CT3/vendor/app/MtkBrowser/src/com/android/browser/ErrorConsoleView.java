package com.android.browser;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.Vector;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: b.zip:com/android/browser/ErrorConsoleView.class */
public class ErrorConsoleView extends LinearLayout {
    private TextView mConsoleHeader;
    private int mCurrentShowState;
    private ErrorConsoleListView mErrorList;
    private Vector<ConsoleMessage> mErrorMessageCache;
    private Button mEvalButton;
    private EditText mEvalEditText;
    private LinearLayout mEvalJsViewGroup;
    private boolean mSetupComplete;
    private WebView mWebView;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: b.zip:com/android/browser/ErrorConsoleView$ErrorConsoleListView.class */
    public static class ErrorConsoleListView extends ListView {
        private ErrorConsoleMessageList mConsoleMessages;

        /* JADX INFO: Access modifiers changed from: private */
        /* loaded from: b.zip:com/android/browser/ErrorConsoleView$ErrorConsoleListView$ErrorConsoleMessageList.class */
        public static class ErrorConsoleMessageList extends BaseAdapter implements ListAdapter {

            /* renamed from: -android-webkit-ConsoleMessage$MessageLevelSwitchesValues  reason: not valid java name */
            private static final int[] f3androidwebkitConsoleMessage$MessageLevelSwitchesValues = null;
            private LayoutInflater mInflater;
            private Vector<ConsoleMessage> mMessages = new Vector<>();

            /* renamed from: -getandroid-webkit-ConsoleMessage$MessageLevelSwitchesValues  reason: not valid java name */
            private static /* synthetic */ int[] m139getandroidwebkitConsoleMessage$MessageLevelSwitchesValues() {
                if (f3androidwebkitConsoleMessage$MessageLevelSwitchesValues != null) {
                    return f3androidwebkitConsoleMessage$MessageLevelSwitchesValues;
                }
                int[] iArr = new int[ConsoleMessage.MessageLevel.values().length];
                try {
                    iArr[ConsoleMessage.MessageLevel.DEBUG.ordinal()] = 4;
                } catch (NoSuchFieldError e) {
                }
                try {
                    iArr[ConsoleMessage.MessageLevel.ERROR.ordinal()] = 1;
                } catch (NoSuchFieldError e2) {
                }
                try {
                    iArr[ConsoleMessage.MessageLevel.LOG.ordinal()] = 5;
                } catch (NoSuchFieldError e3) {
                }
                try {
                    iArr[ConsoleMessage.MessageLevel.TIP.ordinal()] = 2;
                } catch (NoSuchFieldError e4) {
                }
                try {
                    iArr[ConsoleMessage.MessageLevel.WARNING.ordinal()] = 3;
                } catch (NoSuchFieldError e5) {
                }
                f3androidwebkitConsoleMessage$MessageLevelSwitchesValues = iArr;
                return iArr;
            }

            public ErrorConsoleMessageList(Context context) {
                this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
            }

            public void add(ConsoleMessage consoleMessage) {
                this.mMessages.add(consoleMessage);
                notifyDataSetChanged();
            }

            @Override // android.widget.BaseAdapter, android.widget.ListAdapter
            public boolean areAllItemsEnabled() {
                return false;
            }

            public void clear() {
                this.mMessages.clear();
                notifyDataSetChanged();
            }

            @Override // android.widget.Adapter
            public int getCount() {
                return this.mMessages.size();
            }

            @Override // android.widget.Adapter
            public Object getItem(int i) {
                return this.mMessages.get(i);
            }

            @Override // android.widget.Adapter
            public long getItemId(int i) {
                return i;
            }

            @Override // android.widget.Adapter
            public View getView(int i, View view, ViewGroup viewGroup) {
                ConsoleMessage consoleMessage = this.mMessages.get(i);
                if (consoleMessage == null) {
                    return null;
                }
                if (view == null) {
                    view = this.mInflater.inflate(17367053, viewGroup, false);
                }
                TextView textView = (TextView) view.findViewById(16908308);
                TextView textView2 = (TextView) view.findViewById(16908309);
                textView.setText(consoleMessage.sourceId() + ":" + consoleMessage.lineNumber());
                textView.setTextColor(-1);
                textView2.setText(consoleMessage.message());
                switch (m139getandroidwebkitConsoleMessage$MessageLevelSwitchesValues()[consoleMessage.messageLevel().ordinal()]) {
                    case 1:
                        textView2.setTextColor(-65536);
                        break;
                    case 2:
                        textView2.setTextColor(-16776961);
                        break;
                    case 3:
                        textView2.setTextColor(Color.rgb(255, 192, 0));
                        break;
                    default:
                        textView2.setTextColor(-3355444);
                        break;
                }
                return view;
            }

            @Override // android.widget.BaseAdapter, android.widget.Adapter
            public boolean hasStableIds() {
                return true;
            }

            @Override // android.widget.BaseAdapter, android.widget.ListAdapter
            public boolean isEnabled(int i) {
                return false;
            }
        }

        public ErrorConsoleListView(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.mConsoleMessages = new ErrorConsoleMessageList(context);
            setAdapter((ListAdapter) this.mConsoleMessages);
        }

        public void addErrorMessage(ConsoleMessage consoleMessage) {
            this.mConsoleMessages.add(consoleMessage);
            setSelection(this.mConsoleMessages.getCount());
        }

        public void clearErrorMessages() {
            this.mConsoleMessages.clear();
        }
    }

    public ErrorConsoleView(Context context) {
        super(context);
        this.mCurrentShowState = 2;
        this.mSetupComplete = false;
    }

    private void commonSetupIfNeeded() {
        if (this.mSetupComplete) {
            return;
        }
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(2130968598, this);
        this.mConsoleHeader = (TextView) findViewById(2131558473);
        this.mErrorList = (ErrorConsoleListView) findViewById(2131558474);
        this.mEvalJsViewGroup = (LinearLayout) findViewById(2131558475);
        this.mEvalEditText = (EditText) findViewById(2131558476);
        this.mEvalButton = (Button) findViewById(2131558477);
        this.mEvalButton.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.browser.ErrorConsoleView.1
            final ErrorConsoleView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (this.this$0.mWebView != null) {
                    this.this$0.mWebView.loadUrl("javascript:" + ((Object) this.this$0.mEvalEditText.getText()));
                }
                this.this$0.mEvalEditText.setText("");
            }
        });
        this.mConsoleHeader.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.browser.ErrorConsoleView.2
            final ErrorConsoleView this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (this.this$0.mCurrentShowState == 0) {
                    this.this$0.showConsole(1);
                } else {
                    this.this$0.showConsole(0);
                }
            }
        });
        if (this.mErrorMessageCache != null) {
            for (ConsoleMessage consoleMessage : this.mErrorMessageCache) {
                this.mErrorList.addErrorMessage(consoleMessage);
            }
            this.mErrorMessageCache.clear();
        }
        this.mSetupComplete = true;
    }

    public void addErrorMessage(ConsoleMessage consoleMessage) {
        if (this.mSetupComplete) {
            this.mErrorList.addErrorMessage(consoleMessage);
            return;
        }
        if (this.mErrorMessageCache == null) {
            this.mErrorMessageCache = new Vector<>();
        }
        this.mErrorMessageCache.add(consoleMessage);
    }

    public void clearErrorMessages() {
        if (this.mSetupComplete) {
            this.mErrorList.clearErrorMessages();
        } else if (this.mErrorMessageCache != null) {
            this.mErrorMessageCache.clear();
        }
    }

    public int getShowState() {
        if (this.mSetupComplete) {
            return this.mCurrentShowState;
        }
        return 2;
    }

    public int numberOfErrors() {
        if (this.mSetupComplete) {
            return this.mErrorList.getCount();
        }
        return this.mErrorMessageCache == null ? 0 : this.mErrorMessageCache.size();
    }

    public void setWebView(WebView webView) {
        this.mWebView = webView;
    }

    public void showConsole(int i) {
        commonSetupIfNeeded();
        switch (i) {
            case 0:
                this.mConsoleHeader.setVisibility(0);
                this.mConsoleHeader.setText(2131493259);
                this.mErrorList.setVisibility(8);
                this.mEvalJsViewGroup.setVisibility(8);
                break;
            case 1:
                this.mConsoleHeader.setVisibility(0);
                this.mConsoleHeader.setText(2131493260);
                this.mErrorList.setVisibility(0);
                this.mEvalJsViewGroup.setVisibility(0);
                break;
            case 2:
                this.mConsoleHeader.setVisibility(8);
                this.mErrorList.setVisibility(8);
                this.mEvalJsViewGroup.setVisibility(8);
                break;
        }
        this.mCurrentShowState = i;
    }
}
