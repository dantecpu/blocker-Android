package io.github.newbugger.android.blocker.util

import android.content.Context
//import android.content.pm.IPackageManager
//import android.content.pm.PackageInfo
//import android.content.pm.ParceledListSlice
//import android.content.pm.UserInfo
import android.os.Parcel
import android.os.RemoteException
import com.elvishew.xlog.Logger
import com.elvishew.xlog.XLog
import moe.shizuku.api.ShizukuBinderWrapper
import moe.shizuku.api.ShizukuService
import moe.shizuku.api.SystemServiceHelper

/*
class ShizukuApi {

    private val logger: Logger = XLog.tag("ShizukuApi").build()

    // method 1: use ShizukuBinderWrapper

    private val iPackageManager: IPackageManager =
            IPackageManager.Stub.asInterface(
                    ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package"))
            )

    private fun getPackageManager(): IPackageManager {
        return iPackageManager
    }

    fun pmGetInstalledPackages(flags: Int, userId: Int): List<PackageInfo?>? {
        return if (!ShizukuService.pingBinder()) {
            ArrayList()
        } else try {
            val listSlice: ParceledListSlice<PackageInfo> =
                    getPackageManager().getInstalledPackages(flags, userId)
            listSlice.list
        } catch (tr: Throwable) {
            throw RuntimeException(tr.message, tr)
        }
    }

    fun umGetUsers(excludeDying: Boolean): List<UserInfo?>? {
        val data = SystemServiceHelper.obtainParcel(Context.USER_SERVICE, "android.os.IUserManager", "getUsers")
        val reply = Parcel.obtain()
        data.writeInt(if (excludeDying) 1 else 0)
        var res: List<UserInfo?>? = null
        try {
            ShizukuService.transactRemote(data, reply, 0)
            reply.readException()
            res = reply.createTypedArrayList(UserInfo.CREATOR)
        } catch (e: RemoteException) {
            logger.e("UserManager#getUsers", e)
        } finally {
            data.recycle()
            reply.recycle()
        }
        return res
    }

}*/
