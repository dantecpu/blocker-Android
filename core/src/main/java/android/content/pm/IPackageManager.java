package android.content.pm;

import android.content.ComponentName;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

public interface IPackageManager extends IInterface {

    void setComponentEnabledSetting(ComponentName componentName, int state, int flag, int userId) throws RemoteException;

    void setApplicationEnabledSetting(String packageName, int state, int flag, int userId) throws RemoteException;

    abstract class Stub extends Binder implements IPackageManager {

        public static IPackageManager asInterface(IBinder obj) {
            throw new UnsupportedOperationException();
        }

    }

}
