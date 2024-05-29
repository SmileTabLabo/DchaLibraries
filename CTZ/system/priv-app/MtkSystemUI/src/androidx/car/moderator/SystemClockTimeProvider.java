package androidx.car.moderator;

import android.os.SystemClock;
import androidx.car.moderator.ContentRateLimiter;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class SystemClockTimeProvider implements ContentRateLimiter.ElapsedTimeProvider {
    @Override // androidx.car.moderator.ContentRateLimiter.ElapsedTimeProvider
    public long getElapsedRealtime() {
        return SystemClock.elapsedRealtime();
    }
}
