package android.support.v4.media;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.util.Log;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/* loaded from: classes.dex */
public final class SessionCommandGroup2 {
    private static final String KEY_COMMANDS = "android.media.mediasession2.commandgroup.commands";
    private static final String PREFIX_COMMAND_CODE = "COMMAND_CODE_";
    private static final String PREFIX_COMMAND_CODE_PLAYBACK = "COMMAND_CODE_PLAYBACK_";
    private static final String PREFIX_COMMAND_CODE_PLAYLIST = "COMMAND_CODE_PLAYLIST_";
    private static final String PREFIX_COMMAND_CODE_VOLUME = "COMMAND_CODE_VOLUME_";
    private static final String TAG = "SessionCommandGroup2";
    private Set<SessionCommand2> mCommands = new HashSet();

    public SessionCommandGroup2() {
    }

    public SessionCommandGroup2(@Nullable SessionCommandGroup2 other) {
        if (other != null) {
            this.mCommands.addAll(other.mCommands);
        }
    }

    public void addCommand(@NonNull SessionCommand2 command) {
        if (command == null) {
            throw new IllegalArgumentException("command shouldn't be null");
        }
        this.mCommands.add(command);
    }

    public void addCommand(int commandCode) {
        if (commandCode == 0) {
            throw new IllegalArgumentException("command shouldn't be null");
        }
        this.mCommands.add(new SessionCommand2(commandCode));
    }

    public void addAllPredefinedCommands() {
        addCommandsWithPrefix(PREFIX_COMMAND_CODE);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addAllPlaybackCommands() {
        addCommandsWithPrefix(PREFIX_COMMAND_CODE_PLAYBACK);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addAllPlaylistCommands() {
        addCommandsWithPrefix(PREFIX_COMMAND_CODE_PLAYLIST);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addAllVolumeCommands() {
        addCommandsWithPrefix(PREFIX_COMMAND_CODE_VOLUME);
    }

    private void addCommandsWithPrefix(String prefix) {
        Field[] fields = SessionCommand2.class.getFields();
        if (fields != null) {
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].getName().startsWith(prefix) && !fields[i].getName().equals("COMMAND_CODE_CUSTOM")) {
                    try {
                        this.mCommands.add(new SessionCommand2(fields[i].getInt(null)));
                    } catch (IllegalAccessException e) {
                        Log.w(TAG, "Unexpected " + fields[i] + " in MediaSession2");
                    }
                }
            }
        }
    }

    public void removeCommand(@NonNull SessionCommand2 command) {
        if (command == null) {
            throw new IllegalArgumentException("command shouldn't be null");
        }
        this.mCommands.remove(command);
    }

    public void removeCommand(int commandCode) {
        if (commandCode == 0) {
            throw new IllegalArgumentException("commandCode shouldn't be COMMAND_CODE_CUSTOM");
        }
        this.mCommands.remove(new SessionCommand2(commandCode));
    }

    public boolean hasCommand(@NonNull SessionCommand2 command) {
        if (command == null) {
            throw new IllegalArgumentException("command shouldn't be null");
        }
        return this.mCommands.contains(command);
    }

    public boolean hasCommand(int commandCode) {
        if (commandCode == 0) {
            throw new IllegalArgumentException("Use hasCommand(Command) for custom command");
        }
        for (SessionCommand2 command : this.mCommands) {
            if (command.getCommandCode() == commandCode) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    public Set<SessionCommand2> getCommands() {
        return new HashSet(this.mCommands);
    }

    @NonNull
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public Bundle toBundle() {
        ArrayList<Bundle> list = new ArrayList<>();
        for (SessionCommand2 command : this.mCommands) {
            list.add(command.toBundle());
        }
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(KEY_COMMANDS, list);
        return bundle;
    }

    @Nullable
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public static SessionCommandGroup2 fromBundle(Bundle commands) {
        List<Parcelable> list;
        if (commands == null || (list = commands.getParcelableArrayList(KEY_COMMANDS)) == null) {
            return null;
        }
        SessionCommandGroup2 commandGroup = new SessionCommandGroup2();
        for (int i = 0; i < list.size(); i++) {
            Parcelable parcelable = list.get(i);
            if (parcelable instanceof Bundle) {
                Bundle commandBundle = (Bundle) parcelable;
                SessionCommand2 command = SessionCommand2.fromBundle(commandBundle);
                if (command != null) {
                    commandGroup.addCommand(command);
                }
            }
        }
        return commandGroup;
    }
}
