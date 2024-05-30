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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
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

    public ErrorConsoleView(Context context) {
        super(context);
        this.mCurrentShowState = 2;
        this.mSetupComplete = false;
    }

    private void commonSetupIfNeeded() {
        if (this.mSetupComplete) {
            return;
        }
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.error_console, this);
        this.mConsoleHeader = (TextView) findViewById(R.id.error_console_header_id);
        this.mErrorList = (ErrorConsoleListView) findViewById(R.id.error_console_list_id);
        this.mEvalJsViewGroup = (LinearLayout) findViewById(R.id.error_console_eval_view_group_id);
        this.mEvalEditText = (EditText) findViewById(R.id.error_console_eval_text_id);
        this.mEvalButton = (Button) findViewById(R.id.error_console_eval_button_id);
        this.mEvalButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.browser.ErrorConsoleView.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (ErrorConsoleView.this.mWebView != null) {
                    HashMap hashMap = new HashMap();
                    hashMap.put(Browser.HEADER, Browser.UAPROF);
                    WebView webView = ErrorConsoleView.this.mWebView;
                    webView.loadUrl("javascript:" + ((Object) ErrorConsoleView.this.mEvalEditText.getText()), hashMap);
                }
                ErrorConsoleView.this.mEvalEditText.setText("");
            }
        });
        this.mConsoleHeader.setOnClickListener(new View.OnClickListener() { // from class: com.android.browser.ErrorConsoleView.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (ErrorConsoleView.this.mCurrentShowState == 0) {
                    ErrorConsoleView.this.showConsole(1);
                } else {
                    ErrorConsoleView.this.showConsole(0);
                }
            }
        });
        if (this.mErrorMessageCache != null) {
            Iterator<ConsoleMessage> it = this.mErrorMessageCache.iterator();
            while (it.hasNext()) {
                this.mErrorList.addErrorMessage(it.next());
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

    public int numberOfErrors() {
        if (this.mSetupComplete) {
            return this.mErrorList.getCount();
        }
        if (this.mErrorMessageCache == null) {
            return 0;
        }
        return this.mErrorMessageCache.size();
    }

    public void setWebView(WebView webView) {
        this.mWebView = webView;
    }

    public void showConsole(int i) {
        commonSetupIfNeeded();
        switch (i) {
            case 0:
                this.mConsoleHeader.setVisibility(0);
                this.mConsoleHeader.setText(R.string.error_console_header_text_minimized);
                this.mErrorList.setVisibility(8);
                this.mEvalJsViewGroup.setVisibility(8);
                break;
            case 1:
                this.mConsoleHeader.setVisibility(0);
                this.mConsoleHeader.setText(R.string.error_console_header_text_maximized);
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

    public int getShowState() {
        if (this.mSetupComplete) {
            return this.mCurrentShowState;
        }
        return 2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class ErrorConsoleListView extends ListView {
        private ErrorConsoleMessageList mConsoleMessages;

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

        /* JADX INFO: Access modifiers changed from: private */
        /* loaded from: classes.dex */
        public static class ErrorConsoleMessageList extends BaseAdapter implements ListAdapter {
            private LayoutInflater mInflater;
            private Vector<ConsoleMessage> mMessages = new Vector<>();

            public ErrorConsoleMessageList(Context context) {
                this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
            }

            public void add(ConsoleMessage consoleMessage) {
                this.mMessages.add(consoleMessage);
                notifyDataSetChanged();
            }

            public void clear() {
                this.mMessages.clear();
                notifyDataSetChanged();
            }

            @Override // android.widget.BaseAdapter, android.widget.ListAdapter
            public boolean areAllItemsEnabled() {
                return false;
            }

            @Override // android.widget.BaseAdapter, android.widget.ListAdapter
            public boolean isEnabled(int i) {
                return false;
            }

            @Override // android.widget.Adapter
            public long getItemId(int i) {
                return i;
            }

            @Override // android.widget.Adapter
            public Object getItem(int i) {
                return this.mMessages.get(i);
            }

            @Override // android.widget.Adapter
            public int getCount() {
                return this.mMessages.size();
            }

            @Override // android.widget.BaseAdapter, android.widget.Adapter
            public boolean hasStableIds() {
                return true;
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
                switch (AnonymousClass3.$SwitchMap$android$webkit$ConsoleMessage$MessageLevel[consoleMessage.messageLevel().ordinal()]) {
                    case 1:
                        textView2.setTextColor(-65536);
                        break;
                    case 2:
                        textView2.setTextColor(Color.rgb(255, 192, 0));
                        break;
                    case 3:
                        textView2.setTextColor(-16776961);
                        break;
                    default:
                        textView2.setTextColor(-3355444);
                        break;
                }
                return view;
            }
        }
    }

    /* renamed from: com.android.browser.ErrorConsoleView$3  reason: invalid class name */
    /* loaded from: classes.dex */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$android$webkit$ConsoleMessage$MessageLevel = new int[ConsoleMessage.MessageLevel.values().length];

        static {
            try {
                $SwitchMap$android$webkit$ConsoleMessage$MessageLevel[ConsoleMessage.MessageLevel.ERROR.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$webkit$ConsoleMessage$MessageLevel[ConsoleMessage.MessageLevel.WARNING.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$webkit$ConsoleMessage$MessageLevel[ConsoleMessage.MessageLevel.TIP.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }
}
