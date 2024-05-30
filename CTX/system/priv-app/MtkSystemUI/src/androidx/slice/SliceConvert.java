package androidx.slice;

import android.app.slice.Slice;
import android.support.v4.graphics.drawable.IconCompat;
import android.support.v4.util.ArraySet;
import androidx.slice.Slice;
import java.util.Set;
/* loaded from: classes.dex */
public class SliceConvert {
    public static android.app.slice.Slice unwrap(Slice slice) {
        if (slice == null || slice.getUri() == null) {
            return null;
        }
        Slice.Builder builder = new Slice.Builder(slice.getUri(), unwrap(slice.getSpec()));
        builder.addHints(slice.getHints());
        for (SliceItem item : slice.getItems()) {
            String format = item.getFormat();
            char c = 65535;
            switch (format.hashCode()) {
                case -1422950858:
                    if (format.equals("action")) {
                        c = 3;
                        break;
                    }
                    break;
                case 104431:
                    if (format.equals("int")) {
                        c = 5;
                        break;
                    }
                    break;
                case 3327612:
                    if (format.equals("long")) {
                        c = 6;
                        break;
                    }
                    break;
                case 3556653:
                    if (format.equals("text")) {
                        c = 4;
                        break;
                    }
                    break;
                case 100313435:
                    if (format.equals("image")) {
                        c = 1;
                        break;
                    }
                    break;
                case 100358090:
                    if (format.equals("input")) {
                        c = 2;
                        break;
                    }
                    break;
                case 109526418:
                    if (format.equals("slice")) {
                        c = 0;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    builder.addSubSlice(unwrap(item.getSlice()), item.getSubType());
                    break;
                case 1:
                    builder.addIcon(item.getIcon().toIcon(), item.getSubType(), item.getHints());
                    break;
                case 2:
                    builder.addRemoteInput(item.getRemoteInput(), item.getSubType(), item.getHints());
                    break;
                case 3:
                    builder.addAction(item.getAction(), unwrap(item.getSlice()), item.getSubType());
                    break;
                case 4:
                    builder.addText(item.getText(), item.getSubType(), item.getHints());
                    break;
                case 5:
                    builder.addInt(item.getInt(), item.getSubType(), item.getHints());
                    break;
                case 6:
                    builder.addLong(item.getLong(), item.getSubType(), item.getHints());
                    break;
            }
        }
        return builder.build();
    }

    private static android.app.slice.SliceSpec unwrap(SliceSpec spec) {
        if (spec == null) {
            return null;
        }
        return new android.app.slice.SliceSpec(spec.getType(), spec.getRevision());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Set<android.app.slice.SliceSpec> unwrap(Set<SliceSpec> supportedSpecs) {
        Set<android.app.slice.SliceSpec> ret = new ArraySet<>();
        if (supportedSpecs != null) {
            for (SliceSpec spec : supportedSpecs) {
                ret.add(unwrap(spec));
            }
        }
        return ret;
    }

    public static Slice wrap(android.app.slice.Slice slice) {
        if (slice == null || slice.getUri() == null) {
            return null;
        }
        Slice.Builder builder = new Slice.Builder(slice.getUri());
        builder.addHints(slice.getHints());
        builder.setSpec(wrap(slice.getSpec()));
        for (android.app.slice.SliceItem item : slice.getItems()) {
            String format = item.getFormat();
            char c = 65535;
            switch (format.hashCode()) {
                case -1422950858:
                    if (format.equals("action")) {
                        c = 3;
                        break;
                    }
                    break;
                case 104431:
                    if (format.equals("int")) {
                        c = 5;
                        break;
                    }
                    break;
                case 3327612:
                    if (format.equals("long")) {
                        c = 6;
                        break;
                    }
                    break;
                case 3556653:
                    if (format.equals("text")) {
                        c = 4;
                        break;
                    }
                    break;
                case 100313435:
                    if (format.equals("image")) {
                        c = 1;
                        break;
                    }
                    break;
                case 100358090:
                    if (format.equals("input")) {
                        c = 2;
                        break;
                    }
                    break;
                case 109526418:
                    if (format.equals("slice")) {
                        c = 0;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    builder.addSubSlice(wrap(item.getSlice()), item.getSubType());
                    break;
                case 1:
                    builder.addIcon(IconCompat.createFromIcon(item.getIcon()), item.getSubType(), item.getHints());
                    break;
                case 2:
                    builder.addRemoteInput(item.getRemoteInput(), item.getSubType(), item.getHints());
                    break;
                case 3:
                    builder.addAction(item.getAction(), wrap(item.getSlice()), item.getSubType());
                    break;
                case 4:
                    builder.addText(item.getText(), item.getSubType(), item.getHints());
                    break;
                case 5:
                    builder.addInt(item.getInt(), item.getSubType(), item.getHints());
                    break;
                case 6:
                    builder.addLong(item.getLong(), item.getSubType(), item.getHints());
                    break;
            }
        }
        return builder.build();
    }

    private static SliceSpec wrap(android.app.slice.SliceSpec spec) {
        if (spec == null) {
            return null;
        }
        return new SliceSpec(spec.getType(), spec.getRevision());
    }

    public static Set<SliceSpec> wrap(Set<android.app.slice.SliceSpec> supportedSpecs) {
        Set<SliceSpec> ret = new ArraySet<>();
        if (supportedSpecs != null) {
            for (android.app.slice.SliceSpec spec : supportedSpecs) {
                ret.add(wrap(spec));
            }
        }
        return ret;
    }
}
