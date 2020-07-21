package top.trumeet.mipush.xmipushenhance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcel;
import android.os.UserHandle;
import androidx.annotation.Nullable;
import com.oasisfeng.hack.Hack;

/**
 * Some packaged ways to access certain hidden apis.
 */
public final class HiddenApiHelper {
    /**
     * The same usage as UserHandle#of(int)
     */
    @Nullable
    public static UserHandle createUserHandle(int userId) {
        final Parcel parcel = Parcel.obtain();
        parcel.writeInt(userId);
        final UserHandle handle = UserHandle.readFromParcel(parcel);
        parcel.recycle();
        return handle;
    }

    @SuppressLint("PrivateApi")
    public static Context getSystemContext() {
        try {
            final Object activityThread = Hack.into("android.app.ActivityThread")
                    .staticMethod("systemMain")
                    .fallbackReturning(null)
                    .withoutParams()
                    .invoke()
                    .statically();
            return (Context) Hack.into("android.app.ActivityThread")
                    .method("getSystemContext")
                    .returning(Class.forName("android.app.ContextImpl"))
                    .fallbackReturning(null)
                    .withoutParams()
                    .invoke()
                    .on(activityThread);
        } catch (Exception e) {
            return null;
        }
    }
}
