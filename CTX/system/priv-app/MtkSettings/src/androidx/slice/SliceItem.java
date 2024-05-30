package androidx.slice;

import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.graphics.drawable.IconCompat;
import android.support.v4.util.Pair;
import androidx.versionedparcelable.CustomVersionedParcelable;
import java.util.Arrays;
import java.util.List;
/* loaded from: classes.dex */
public final class SliceItem extends CustomVersionedParcelable {
    String mFormat;
    protected String[] mHints;
    Object mObj;
    String mSubType;

    /* loaded from: classes.dex */
    public interface ActionHandler {
        void onAction(SliceItem sliceItem, Context context, Intent intent);
    }

    public SliceItem(Object obj, String format, String subType, String[] hints) {
        this.mHints = new String[0];
        this.mHints = hints;
        this.mFormat = format;
        this.mSubType = subType;
        this.mObj = obj;
    }

    public SliceItem(Object obj, String format, String subType, List<String> hints) {
        this(obj, format, subType, (String[]) hints.toArray(new String[hints.size()]));
    }

    public SliceItem() {
        this.mHints = new String[0];
    }

    public SliceItem(PendingIntent intent, Slice slice, String format, String subType, String[] hints) {
        this(new Pair(intent, slice), format, subType, hints);
    }

    public List<String> getHints() {
        return Arrays.asList(this.mHints);
    }

    public void addHint(String hint) {
        this.mHints = (String[]) ArrayUtils.appendElement(String.class, this.mHints, hint);
    }

    public String getFormat() {
        return this.mFormat;
    }

    public String getSubType() {
        return this.mSubType;
    }

    public CharSequence getText() {
        return (CharSequence) this.mObj;
    }

    public IconCompat getIcon() {
        return (IconCompat) this.mObj;
    }

    public PendingIntent getAction() {
        return (PendingIntent) ((Pair) this.mObj).first;
    }

    public void fireAction(Context context, Intent i) throws PendingIntent.CanceledException {
        Object action = ((Pair) this.mObj).first;
        if (action instanceof PendingIntent) {
            ((PendingIntent) action).send(context, 0, i, null, null);
        } else {
            ((ActionHandler) action).onAction(this, context, i);
        }
    }

    public RemoteInput getRemoteInput() {
        return (RemoteInput) this.mObj;
    }

    public int getInt() {
        return ((Integer) this.mObj).intValue();
    }

    public Slice getSlice() {
        if ("action".equals(getFormat())) {
            return (Slice) ((Pair) this.mObj).second;
        }
        return (Slice) this.mObj;
    }

    public long getLong() {
        return ((Long) this.mObj).longValue();
    }

    @Deprecated
    public long getTimestamp() {
        return ((Long) this.mObj).longValue();
    }

    public boolean hasHint(String hint) {
        return ArrayUtils.contains(this.mHints, hint);
    }

    public SliceItem(Bundle in) {
        this.mHints = new String[0];
        this.mHints = in.getStringArray("hints");
        this.mFormat = in.getString("format");
        this.mSubType = in.getString("subtype");
        this.mObj = readObj(this.mFormat, in);
    }

    public Bundle toBundle() {
        Bundle b = new Bundle();
        b.putStringArray("hints", this.mHints);
        b.putString("format", this.mFormat);
        b.putString("subtype", this.mSubType);
        writeObj(b, this.mObj, this.mFormat);
        return b;
    }

    public boolean hasAnyHints(String... hints) {
        if (hints == null) {
            return false;
        }
        for (String hint : hints) {
            if (ArrayUtils.contains(this.mHints, hint)) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    private void writeObj(Bundle dest, Object obj, String type) {
        char c;
        switch (type.hashCode()) {
            case -1422950858:
                if (type.equals("action")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 104431:
                if (type.equals("int")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 3327612:
                if (type.equals("long")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 3556653:
                if (type.equals("text")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 100313435:
                if (type.equals("image")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 100358090:
                if (type.equals("input")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 109526418:
                if (type.equals("slice")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                dest.putBundle("obj", ((IconCompat) obj).toBundle());
                return;
            case 1:
                dest.putParcelable("obj", (Parcelable) obj);
                return;
            case 2:
                dest.putParcelable("obj", ((Slice) obj).toBundle());
                return;
            case 3:
                dest.putParcelable("obj", (PendingIntent) ((Pair) obj).first);
                dest.putBundle("obj_2", ((Slice) ((Pair) obj).second).toBundle());
                return;
            case 4:
                dest.putCharSequence("obj", (CharSequence) obj);
                return;
            case 5:
                dest.putInt("obj", ((Integer) this.mObj).intValue());
                return;
            case 6:
                dest.putLong("obj", ((Long) this.mObj).longValue());
                return;
            default:
                return;
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    private static Object readObj(String type, Bundle in) {
        char c;
        switch (type.hashCode()) {
            case -1422950858:
                if (type.equals("action")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 104431:
                if (type.equals("int")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 3327612:
                if (type.equals("long")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 3556653:
                if (type.equals("text")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 100313435:
                if (type.equals("image")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 100358090:
                if (type.equals("input")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 109526418:
                if (type.equals("slice")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return IconCompat.createFromBundle(in.getBundle("obj"));
            case 1:
                return in.getParcelable("obj");
            case 2:
                return new Slice(in.getBundle("obj"));
            case 3:
                return in.getCharSequence("obj");
            case 4:
                return new Pair(in.getParcelable("obj"), new Slice(in.getBundle("obj_2")));
            case 5:
                return Integer.valueOf(in.getInt("obj"));
            case 6:
                return Long.valueOf(in.getLong("obj"));
            default:
                throw new RuntimeException("Unsupported type " + type);
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    public static String typeToString(String format) {
        char c;
        switch (format.hashCode()) {
            case -1422950858:
                if (format.equals("action")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 104431:
                if (format.equals("int")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 3327612:
                if (format.equals("long")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 3556653:
                if (format.equals("text")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 100313435:
                if (format.equals("image")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 100358090:
                if (format.equals("input")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 109526418:
                if (format.equals("slice")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return "Slice";
            case 1:
                return "Text";
            case 2:
                return "Image";
            case 3:
                return "Action";
            case 4:
                return "Int";
            case 5:
                return "Long";
            case 6:
                return "RemoteInput";
            default:
                return "Unrecognized format: " + format;
        }
    }

    public String toString() {
        return toString("");
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    public String toString(String indent) {
        char c;
        StringBuilder sb = new StringBuilder();
        String format = getFormat();
        switch (format.hashCode()) {
            case -1422950858:
                if (format.equals("action")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 104431:
                if (format.equals("int")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 3327612:
                if (format.equals("long")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 3556653:
                if (format.equals("text")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 100313435:
                if (format.equals("image")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 109526418:
                if (format.equals("slice")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                sb.append(getSlice().toString(indent));
                break;
            case 1:
                sb.append(indent);
                sb.append(getAction());
                sb.append(",\n");
                sb.append(getSlice().toString(indent));
                break;
            case 2:
                sb.append(indent);
                sb.append('\"');
                sb.append(getText());
                sb.append('\"');
                break;
            case 3:
                sb.append(indent);
                sb.append(getIcon());
                break;
            case 4:
                sb.append(indent);
                sb.append(getInt());
                break;
            case 5:
                sb.append(indent);
                sb.append(getLong());
                break;
            default:
                sb.append(indent);
                sb.append(typeToString(getFormat()));
                break;
        }
        if (!"slice".equals(getFormat())) {
            sb.append(' ');
            Slice.addHints(sb, this.mHints);
        }
        sb.append(",\n");
        return sb.toString();
    }
}
