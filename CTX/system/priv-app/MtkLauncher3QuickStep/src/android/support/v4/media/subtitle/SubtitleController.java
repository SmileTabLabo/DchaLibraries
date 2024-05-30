package android.support.v4.media.subtitle;

import android.content.Context;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.v4.media.subtitle.SubtitleTrack;
import android.view.accessibility.CaptioningManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
@RequiresApi(28)
@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
/* loaded from: classes.dex */
public class SubtitleController {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int WHAT_HIDE = 2;
    private static final int WHAT_SELECT_DEFAULT_TRACK = 4;
    private static final int WHAT_SELECT_TRACK = 3;
    private static final int WHAT_SHOW = 1;
    private Anchor mAnchor;
    private final Handler.Callback mCallback;
    private CaptioningManager.CaptioningChangeListener mCaptioningChangeListener;
    private CaptioningManager mCaptioningManager;
    private Handler mHandler;
    private Listener mListener;
    private ArrayList<Renderer> mRenderers;
    private final Object mRenderersLock;
    private SubtitleTrack mSelectedTrack;
    private boolean mShowing;
    private MediaTimeProvider mTimeProvider;
    private boolean mTrackIsExplicit;
    private ArrayList<SubtitleTrack> mTracks;
    private final Object mTracksLock;
    private boolean mVisibilityIsExplicit;

    /* loaded from: classes.dex */
    public interface Anchor {
        Looper getSubtitleLooper();

        void setSubtitleWidget(SubtitleTrack.RenderingWidget renderingWidget);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public interface Listener {
        void onSubtitleTrackSelected(SubtitleTrack subtitleTrack);
    }

    /* loaded from: classes.dex */
    public static abstract class Renderer {
        public abstract SubtitleTrack createTrack(MediaFormat mediaFormat);

        public abstract boolean supports(MediaFormat mediaFormat);
    }

    public SubtitleController(Context context) {
        this(context, null, null);
    }

    public SubtitleController(Context context, MediaTimeProvider timeProvider, Listener listener) {
        this.mRenderersLock = new Object();
        this.mTracksLock = new Object();
        this.mCallback = new Handler.Callback() { // from class: android.support.v4.media.subtitle.SubtitleController.1
            @Override // android.os.Handler.Callback
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        SubtitleController.this.doShow();
                        return true;
                    case 2:
                        SubtitleController.this.doHide();
                        return true;
                    case 3:
                        SubtitleController.this.doSelectTrack((SubtitleTrack) msg.obj);
                        return true;
                    case 4:
                        SubtitleController.this.doSelectDefaultTrack();
                        return true;
                    default:
                        return false;
                }
            }
        };
        this.mCaptioningChangeListener = new CaptioningManager.CaptioningChangeListener() { // from class: android.support.v4.media.subtitle.SubtitleController.2
            @Override // android.view.accessibility.CaptioningManager.CaptioningChangeListener
            public void onEnabledChanged(boolean enabled) {
                SubtitleController.this.selectDefaultTrack();
            }

            @Override // android.view.accessibility.CaptioningManager.CaptioningChangeListener
            public void onLocaleChanged(Locale locale) {
                SubtitleController.this.selectDefaultTrack();
            }
        };
        this.mTrackIsExplicit = false;
        this.mVisibilityIsExplicit = false;
        this.mTimeProvider = timeProvider;
        this.mListener = listener;
        this.mRenderers = new ArrayList<>();
        this.mShowing = false;
        this.mTracks = new ArrayList<>();
        this.mCaptioningManager = (CaptioningManager) context.getSystemService("captioning");
    }

    protected void finalize() throws Throwable {
        this.mCaptioningManager.removeCaptioningChangeListener(this.mCaptioningChangeListener);
        super.finalize();
    }

    public SubtitleTrack[] getTracks() {
        SubtitleTrack[] tracks;
        synchronized (this.mTracksLock) {
            tracks = new SubtitleTrack[this.mTracks.size()];
            this.mTracks.toArray(tracks);
        }
        return tracks;
    }

    public SubtitleTrack getSelectedTrack() {
        return this.mSelectedTrack;
    }

    private SubtitleTrack.RenderingWidget getRenderingWidget() {
        if (this.mSelectedTrack == null) {
            return null;
        }
        return this.mSelectedTrack.getRenderingWidget();
    }

    public boolean selectTrack(SubtitleTrack track) {
        if (track != null && !this.mTracks.contains(track)) {
            return false;
        }
        processOnAnchor(this.mHandler.obtainMessage(3, track));
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void doSelectTrack(SubtitleTrack track) {
        this.mTrackIsExplicit = true;
        if (this.mSelectedTrack == track) {
            return;
        }
        if (this.mSelectedTrack != null) {
            this.mSelectedTrack.hide();
            this.mSelectedTrack.setTimeProvider(null);
        }
        this.mSelectedTrack = track;
        if (this.mAnchor != null) {
            this.mAnchor.setSubtitleWidget(getRenderingWidget());
        }
        if (this.mSelectedTrack != null) {
            this.mSelectedTrack.setTimeProvider(this.mTimeProvider);
            this.mSelectedTrack.show();
        }
        if (this.mListener != null) {
            this.mListener.onSubtitleTrackSelected(track);
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:33:0x0084  */
    /* JADX WARN: Removed duplicated region for block: B:34:0x0086  */
    /* JADX WARN: Removed duplicated region for block: B:41:0x0095  */
    /* JADX WARN: Removed duplicated region for block: B:42:0x0098  */
    /* JADX WARN: Removed duplicated region for block: B:45:0x009e  */
    /* JADX WARN: Removed duplicated region for block: B:46:0x00a1  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public SubtitleTrack getDefaultTrack() {
        boolean languageMatches;
        int score;
        SubtitleTrack bestTrack = null;
        int bestScore = -1;
        Locale selectedLocale = this.mCaptioningManager.getLocale();
        Locale locale = selectedLocale;
        if (locale == null) {
            locale = Locale.getDefault();
        }
        Locale locale2 = locale;
        int i = 1;
        boolean selectForced = !this.mCaptioningManager.isEnabled();
        synchronized (this.mTracksLock) {
            Iterator<SubtitleTrack> it = this.mTracks.iterator();
            while (it.hasNext()) {
                SubtitleTrack track = it.next();
                MediaFormat format = track.getFormat();
                String language = format.getString("language");
                int i2 = MediaFormatUtil.getInteger(format, "is-forced-subtitle", 0) != 0 ? i : 0;
                int i3 = MediaFormatUtil.getInteger(format, "is-autoselect", i) != 0 ? i : 0;
                int i4 = MediaFormatUtil.getInteger(format, "is-default", 0) != 0 ? i : 0;
                if (locale2 != null && !locale2.getLanguage().equals("") && !locale2.getISO3Language().equals(language) && !locale2.getLanguage().equals(language)) {
                    languageMatches = false;
                    score = (i2 == 0 ? 0 : 8) + ((selectedLocale == null || i4 == 0) ? 0 : 4) + (i3 == 0 ? 0 : 2) + (!languageMatches ? 1 : 0);
                    if (selectForced || i2 != 0) {
                        if (((selectedLocale == null && i4 != 0) || (languageMatches && (i3 != 0 || i2 != 0 || selectedLocale != null))) && score > bestScore) {
                            bestScore = score;
                            bestTrack = track;
                        }
                    }
                    i = 1;
                }
                languageMatches = true;
                score = (i2 == 0 ? 0 : 8) + ((selectedLocale == null || i4 == 0) ? 0 : 4) + (i3 == 0 ? 0 : 2) + (!languageMatches ? 1 : 0);
                if (selectForced) {
                }
                if (selectedLocale == null) {
                    bestScore = score;
                    bestTrack = track;
                    i = 1;
                }
                bestScore = score;
                bestTrack = track;
                i = 1;
            }
        }
        return bestTrack;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class MediaFormatUtil {
        MediaFormatUtil() {
        }

        static int getInteger(MediaFormat format, String name, int defaultValue) {
            try {
                return format.getInteger(name);
            } catch (ClassCastException | NullPointerException e) {
                return defaultValue;
            }
        }
    }

    public void selectDefaultTrack() {
        processOnAnchor(this.mHandler.obtainMessage(4));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void doSelectDefaultTrack() {
        if (this.mTrackIsExplicit) {
            if (this.mVisibilityIsExplicit) {
                return;
            }
            if (this.mCaptioningManager.isEnabled() || (this.mSelectedTrack != null && MediaFormatUtil.getInteger(this.mSelectedTrack.getFormat(), "is-forced-subtitle", 0) != 0)) {
                show();
            } else if (this.mSelectedTrack != null && this.mSelectedTrack.getTrackType() == 4) {
                hide();
            }
            this.mVisibilityIsExplicit = false;
        }
        SubtitleTrack track = getDefaultTrack();
        if (track != null) {
            selectTrack(track);
            this.mTrackIsExplicit = false;
            if (!this.mVisibilityIsExplicit) {
                show();
                this.mVisibilityIsExplicit = false;
            }
        }
    }

    public void reset() {
        checkAnchorLooper();
        hide();
        selectTrack(null);
        this.mTracks.clear();
        this.mTrackIsExplicit = false;
        this.mVisibilityIsExplicit = false;
        this.mCaptioningManager.removeCaptioningChangeListener(this.mCaptioningChangeListener);
    }

    public SubtitleTrack addTrack(MediaFormat format) {
        SubtitleTrack track;
        synchronized (this.mRenderersLock) {
            Iterator<Renderer> it = this.mRenderers.iterator();
            while (it.hasNext()) {
                Renderer renderer = it.next();
                if (renderer.supports(format) && (track = renderer.createTrack(format)) != null) {
                    synchronized (this.mTracksLock) {
                        if (this.mTracks.size() == 0) {
                            this.mCaptioningManager.addCaptioningChangeListener(this.mCaptioningChangeListener);
                        }
                        this.mTracks.add(track);
                    }
                    return track;
                }
            }
            return null;
        }
    }

    public void show() {
        processOnAnchor(this.mHandler.obtainMessage(1));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void doShow() {
        this.mShowing = true;
        this.mVisibilityIsExplicit = true;
        if (this.mSelectedTrack != null) {
            this.mSelectedTrack.show();
        }
    }

    public void hide() {
        processOnAnchor(this.mHandler.obtainMessage(2));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void doHide() {
        this.mVisibilityIsExplicit = true;
        if (this.mSelectedTrack != null) {
            this.mSelectedTrack.hide();
        }
        this.mShowing = false;
    }

    public void registerRenderer(Renderer renderer) {
        synchronized (this.mRenderersLock) {
            if (!this.mRenderers.contains(renderer)) {
                this.mRenderers.add(renderer);
            }
        }
    }

    public boolean hasRendererFor(MediaFormat format) {
        synchronized (this.mRenderersLock) {
            Iterator<Renderer> it = this.mRenderers.iterator();
            while (it.hasNext()) {
                Renderer renderer = it.next();
                if (renderer.supports(format)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void setAnchor(Anchor anchor) {
        if (this.mAnchor == anchor) {
            return;
        }
        if (this.mAnchor != null) {
            checkAnchorLooper();
            this.mAnchor.setSubtitleWidget(null);
        }
        this.mAnchor = anchor;
        this.mHandler = null;
        if (this.mAnchor != null) {
            this.mHandler = new Handler(this.mAnchor.getSubtitleLooper(), this.mCallback);
            checkAnchorLooper();
            this.mAnchor.setSubtitleWidget(getRenderingWidget());
        }
    }

    private void checkAnchorLooper() {
    }

    private void processOnAnchor(Message m) {
        if (Looper.myLooper() == this.mHandler.getLooper()) {
            this.mHandler.dispatchMessage(m);
        } else {
            this.mHandler.sendMessage(m);
        }
    }
}
