package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.MathUtils;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/* loaded from: a.zip:com/android/systemui/statusbar/phone/DozeParameters.class */
public class DozeParameters {
    private static final boolean DEBUG = Log.isLoggable("DozeParameters", 3);
    private static PulseSchedule sPulseSchedule;
    private final Context mContext;

    /* loaded from: a.zip:com/android/systemui/statusbar/phone/DozeParameters$PulseSchedule.class */
    public static class PulseSchedule {
        private static final Pattern PATTERN = Pattern.compile("(\\d+?)s", 0);
        private int[] mSchedule;
        private String mSpec;

        public static PulseSchedule parse(String str) {
            if (TextUtils.isEmpty(str)) {
                return null;
            }
            try {
                PulseSchedule pulseSchedule = new PulseSchedule();
                pulseSchedule.mSpec = str;
                String[] split = str.split(",");
                pulseSchedule.mSchedule = new int[split.length];
                for (int i = 0; i < split.length; i++) {
                    Matcher matcher = PATTERN.matcher(split[i]);
                    if (!matcher.matches()) {
                        throw new IllegalArgumentException("Bad token: " + split[i]);
                    }
                    pulseSchedule.mSchedule[i] = Integer.parseInt(matcher.group(1));
                }
                if (DozeParameters.DEBUG) {
                    Log.d("DozeParameters", "Parsed spec [" + str + "] as: " + pulseSchedule);
                }
                return pulseSchedule;
            } catch (RuntimeException e) {
                Log.w("DozeParameters", "Error parsing spec: " + str, e);
                return null;
            }
        }

        public long getNextTime(long j, long j2) {
            for (int i = 0; i < this.mSchedule.length; i++) {
                long j3 = j2 + (this.mSchedule[i] * 1000);
                if (j3 > j) {
                    return j3;
                }
            }
            return 0L;
        }

        public String toString() {
            return Arrays.toString(this.mSchedule);
        }
    }

    public DozeParameters(Context context) {
        this.mContext = context;
    }

    private boolean getBoolean(String str, int i) {
        return SystemProperties.getBoolean(str, this.mContext.getResources().getBoolean(i));
    }

    private int getInt(String str, int i) {
        return MathUtils.constrain(SystemProperties.getInt(str, this.mContext.getResources().getInteger(i)), 0, 60000);
    }

    private String getString(String str, int i) {
        return SystemProperties.get(str, this.mContext.getString(i));
    }

    public void dump(PrintWriter printWriter) {
        printWriter.println("  DozeParameters:");
        printWriter.print("    getDisplayStateSupported(): ");
        printWriter.println(getDisplayStateSupported());
        printWriter.print("    getPulseDuration(pickup=false): ");
        printWriter.println(getPulseDuration(false));
        printWriter.print("    getPulseDuration(pickup=true): ");
        printWriter.println(getPulseDuration(true));
        printWriter.print("    getPulseInDuration(pickup=false): ");
        printWriter.println(getPulseInDuration(false));
        printWriter.print("    getPulseInDuration(pickup=true): ");
        printWriter.println(getPulseInDuration(true));
        printWriter.print("    getPulseInVisibleDuration(): ");
        printWriter.println(getPulseVisibleDuration());
        printWriter.print("    getPulseOutDuration(): ");
        printWriter.println(getPulseOutDuration());
        printWriter.print("    getPulseOnSigMotion(): ");
        printWriter.println(getPulseOnSigMotion());
        printWriter.print("    getVibrateOnSigMotion(): ");
        printWriter.println(getVibrateOnSigMotion());
        printWriter.print("    getPulseOnPickup(): ");
        printWriter.println(getPulseOnPickup());
        printWriter.print("    getVibrateOnPickup(): ");
        printWriter.println(getVibrateOnPickup());
        printWriter.print("    getProxCheckBeforePulse(): ");
        printWriter.println(getProxCheckBeforePulse());
        printWriter.print("    getPulseOnNotifications(): ");
        printWriter.println(getPulseOnNotifications());
        printWriter.print("    getPulseSchedule(): ");
        printWriter.println(getPulseSchedule());
        printWriter.print("    getPulseScheduleResets(): ");
        printWriter.println(getPulseScheduleResets());
        printWriter.print("    getPickupVibrationThreshold(): ");
        printWriter.println(getPickupVibrationThreshold());
        printWriter.print("    getPickupPerformsProxCheck(): ");
        printWriter.println(getPickupPerformsProxCheck());
    }

    public boolean getDisplayStateSupported() {
        return getBoolean("doze.display.supported", 2131623959);
    }

    public boolean getPickupPerformsProxCheck() {
        return getBoolean("doze.pickup.proxcheck", 2131623964);
    }

    public int getPickupVibrationThreshold() {
        return getInt("doze.pickup.vibration.threshold", 2131755078);
    }

    public boolean getProxCheckBeforePulse() {
        return getBoolean("doze.pulse.proxcheck", 2131623962);
    }

    public int getPulseDuration(boolean z) {
        return getPulseInDuration(z) + getPulseVisibleDuration() + getPulseOutDuration();
    }

    public int getPulseInDuration(boolean z) {
        return z ? getInt("doze.pulse.duration.in.pickup", 2131755080) : getInt("doze.pulse.duration.in", 2131755079);
    }

    public boolean getPulseOnNotifications() {
        return getBoolean("doze.pulse.notifications", 2131623963);
    }

    public boolean getPulseOnPickup() {
        return getBoolean("doze.pulse.pickup", 2131623961);
    }

    public boolean getPulseOnSigMotion() {
        return getBoolean("doze.pulse.sigmotion", 2131623960);
    }

    public int getPulseOutDuration() {
        return getInt("doze.pulse.duration.out", 2131755082);
    }

    public PulseSchedule getPulseSchedule() {
        String string = getString("doze.pulse.schedule", 2131493279);
        if (sPulseSchedule == null || !sPulseSchedule.mSpec.equals(string)) {
            sPulseSchedule = PulseSchedule.parse(string);
        }
        return sPulseSchedule;
    }

    public int getPulseScheduleResets() {
        return getInt("doze.pulse.schedule.resets", 2131755077);
    }

    public int getPulseVisibleDuration() {
        return getInt("doze.pulse.duration.visible", 2131755081);
    }

    public boolean getVibrateOnPickup() {
        return SystemProperties.getBoolean("doze.vibrate.pickup", false);
    }

    public boolean getVibrateOnSigMotion() {
        return SystemProperties.getBoolean("doze.vibrate.sigmotion", false);
    }
}
