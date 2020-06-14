package android.content.pm;

import android.content.ComponentName;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;


public interface IPackageManager extends IInterface {

    // https://cs.android.com/android/platform/superproject/+/
    // master:out/soong/.intermediates/frameworks/base/framework-minus-apex/
    // android_common/xref28/srcjars.xref/frameworks/base/core/
    // java/android/content/pm/IPackageManager.java;bpv=1;bpt=1;l=...

    // l=10559
    int getApplicationEnabledSetting(String packageName, int userId)
            throws RemoteException;

    // l=10549
    int getComponentEnabledSetting(ComponentName componentName, int userId)
            throws RemoteException;

    // l=10554
    void setApplicationEnabledSetting(String packageName, int newState,
                                      int flags, int userId,
                                      String callingPackage)
            throws RemoteException;

    // l=10544
    void setComponentEnabledSetting(ComponentName componentName, int newState,
                                    int flags, int userId)
            throws RemoteException;

    abstract class Stub extends Binder implements IPackageManager {
        public static IPackageManager asInterface(IBinder obj) {
            throw new UnsupportedOperationException();
        }
    }

}
