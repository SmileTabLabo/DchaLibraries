package com.android.systemui.media;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import java.lang.Thread;
import java.util.LinkedList;
/* loaded from: a.zip:com/android/systemui/media/NotificationPlayer.class */
public class NotificationPlayer implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private AudioManager mAudioManagerWithAudioFocus;
    private CreationAndCompletionThread mCompletionThread;
    private Looper mLooper;
    private MediaPlayer mPlayer;
    private String mTag;
    private CmdThread mThread;
    private PowerManager.WakeLock mWakeLock;
    private LinkedList<Command> mCmdQueue = new LinkedList<>();
    private final Object mCompletionHandlingLock = new Object();
    private final Object mQueueAudioFocusLock = new Object();
    private int mState = 2;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/media/NotificationPlayer$CmdThread.class */
    public final class CmdThread extends Thread {
        final NotificationPlayer this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        CmdThread(NotificationPlayer notificationPlayer) {
            super("NotificationPlayer-" + notificationPlayer.mTag);
            this.this$0 = notificationPlayer;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            Command command;
            while (true) {
                synchronized (this.this$0.mCmdQueue) {
                    command = (Command) this.this$0.mCmdQueue.removeFirst();
                }
                switch (command.code) {
                    case 1:
                        this.this$0.startSound(command);
                        break;
                    case 2:
                        if (this.this$0.mPlayer == null) {
                            Log.w(this.this$0.mTag, "STOP command without a player");
                            break;
                        } else {
                            long uptimeMillis = SystemClock.uptimeMillis() - command.requestTime;
                            if (uptimeMillis > 1000) {
                                Log.w(this.this$0.mTag, "Notification stop delayed by " + uptimeMillis + "msecs");
                            }
                            this.this$0.mPlayer.stop();
                            this.this$0.mPlayer.release();
                            this.this$0.mPlayer = null;
                            synchronized (this.this$0.mQueueAudioFocusLock) {
                                if (this.this$0.mAudioManagerWithAudioFocus != null) {
                                    this.this$0.mAudioManagerWithAudioFocus.abandonAudioFocus(null);
                                    this.this$0.mAudioManagerWithAudioFocus = null;
                                }
                            }
                            if (this.this$0.mLooper != null && this.this$0.mLooper.getThread().getState() != Thread.State.TERMINATED) {
                                this.this$0.mLooper.quit();
                                break;
                            }
                        }
                        break;
                }
                synchronized (this.this$0.mCmdQueue) {
                    if (this.this$0.mCmdQueue.size() == 0) {
                        this.this$0.mThread = null;
                        this.this$0.releaseWakeLock();
                        return;
                    }
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/media/NotificationPlayer$Command.class */
    public static final class Command {
        AudioAttributes attributes;
        int code;
        Context context;
        boolean looping;
        long requestTime;
        Uri uri;

        private Command() {
        }

        /* synthetic */ Command(Command command) {
            this();
        }

        public String toString() {
            return "{ code=" + this.code + " looping=" + this.looping + " attributes=" + this.attributes + " uri=" + this.uri + " }";
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: a.zip:com/android/systemui/media/NotificationPlayer$CreationAndCompletionThread.class */
    public final class CreationAndCompletionThread extends Thread {
        public Command mCmd;
        final NotificationPlayer this$0;

        public CreationAndCompletionThread(NotificationPlayer notificationPlayer, Command command) {
            this.this$0 = notificationPlayer;
            this.mCmd = command;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            Looper.prepare();
            this.this$0.mLooper = Looper.myLooper();
            synchronized (this) {
                AudioManager audioManager = (AudioManager) this.mCmd.context.getSystemService("audio");
                try {
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioAttributes(this.mCmd.attributes);
                    mediaPlayer.setDataSource(this.mCmd.context, this.mCmd.uri);
                    mediaPlayer.setLooping(this.mCmd.looping);
                    mediaPlayer.prepare();
                    if (this.mCmd.uri != null && this.mCmd.uri.getEncodedPath() != null && this.mCmd.uri.getEncodedPath().length() > 0 && !audioManager.isMusicActiveRemotely()) {
                        synchronized (this.this$0.mQueueAudioFocusLock) {
                            if (this.this$0.mAudioManagerWithAudioFocus == null) {
                                if (this.mCmd.looping) {
                                    audioManager.requestAudioFocus(null, AudioAttributes.toLegacyStreamType(this.mCmd.attributes), 1);
                                } else {
                                    audioManager.requestAudioFocus(null, AudioAttributes.toLegacyStreamType(this.mCmd.attributes), 3);
                                }
                                this.this$0.mAudioManagerWithAudioFocus = audioManager;
                            }
                        }
                    }
                    mediaPlayer.setOnCompletionListener(this.this$0);
                    mediaPlayer.setOnErrorListener(this.this$0);
                    mediaPlayer.start();
                    if (this.this$0.mPlayer != null) {
                        this.this$0.mPlayer.release();
                    }
                    this.this$0.mPlayer = mediaPlayer;
                } catch (Exception e) {
                    Log.w(this.this$0.mTag, "error loading sound for " + this.mCmd.uri, e);
                }
                notify();
            }
            Looper.loop();
        }
    }

    public NotificationPlayer(String str) {
        if (str != null) {
            this.mTag = str;
        } else {
            this.mTag = "NotificationPlayer";
        }
    }

    private void acquireWakeLock() {
        if (this.mWakeLock != null) {
            this.mWakeLock.acquire();
        }
    }

    private void enqueueLocked(Command command) {
        this.mCmdQueue.add(command);
        if (this.mThread == null) {
            acquireWakeLock();
            this.mThread = new CmdThread(this);
            this.mThread.start();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void releaseWakeLock() {
        if (this.mWakeLock != null) {
            this.mWakeLock.release();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startSound(Command command) {
        try {
            synchronized (this.mCompletionHandlingLock) {
                if (this.mLooper != null && this.mLooper.getThread().getState() != Thread.State.TERMINATED) {
                    this.mLooper.quit();
                }
                this.mCompletionThread = new CreationAndCompletionThread(this, command);
                synchronized (this.mCompletionThread) {
                    this.mCompletionThread.start();
                    this.mCompletionThread.wait();
                }
            }
            long uptimeMillis = SystemClock.uptimeMillis() - command.requestTime;
            if (uptimeMillis > 1000) {
                Log.w(this.mTag, "Notification sound delayed by " + uptimeMillis + "msecs");
            }
        } catch (Exception e) {
            Log.w(this.mTag, "error loading sound for " + command.uri, e);
        }
    }

    @Override // android.media.MediaPlayer.OnCompletionListener
    public void onCompletion(MediaPlayer mediaPlayer) {
        synchronized (this.mQueueAudioFocusLock) {
            if (this.mAudioManagerWithAudioFocus != null) {
                this.mAudioManagerWithAudioFocus.abandonAudioFocus(null);
                this.mAudioManagerWithAudioFocus = null;
            }
        }
        synchronized (this.mCmdQueue) {
            if (this.mCmdQueue.size() == 0) {
                synchronized (this.mCompletionHandlingLock) {
                    if (this.mLooper != null) {
                        this.mLooper.quit();
                    }
                    this.mCompletionThread = null;
                }
            }
        }
    }

    @Override // android.media.MediaPlayer.OnErrorListener
    public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
        Log.e(this.mTag, "error " + i + " (extra=" + i2 + ") playing notification");
        onCompletion(mediaPlayer);
        return true;
    }

    public void play(Context context, Uri uri, boolean z, AudioAttributes audioAttributes) {
        Command command = new Command(null);
        command.requestTime = SystemClock.uptimeMillis();
        command.code = 1;
        command.context = context;
        command.uri = uri;
        command.looping = z;
        command.attributes = audioAttributes;
        synchronized (this.mCmdQueue) {
            enqueueLocked(command);
            this.mState = 1;
        }
    }

    public void setUsesWakeLock(Context context) {
        if (this.mWakeLock != null || this.mThread != null) {
            throw new RuntimeException("assertion failed mWakeLock=" + this.mWakeLock + " mThread=" + this.mThread);
        }
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, this.mTag);
    }

    public void stop() {
        synchronized (this.mCmdQueue) {
            if (this.mState != 2) {
                Command command = new Command(null);
                command.requestTime = SystemClock.uptimeMillis();
                command.code = 2;
                enqueueLocked(command);
                this.mState = 2;
            }
        }
    }
}
