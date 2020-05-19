package io.github.newbugger.android.libkit.utils

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import io.github.newbugger.android.libkit.RootCommand


object DeviceUtil {
    fun enterLowPowerConsumptionMode() {
        RootCommand.runBlockingCommand("dumpsys battery unplug")
        RootCommand.runBlockingCommand("dumpsys deviceidle step")
    }

    fun forceDoze() {
        RootCommand.runBlockingCommand("dumpsys deviceidle force-idle")
    } 

    fun exitDoze() {
        RootCommand.runBlockingCommand("shell input keyevent KEYCODE_WAKEUP")
    }

    @Suppress("DEPRECATION")
    fun isScreenOn(context: Context): Boolean {
        val dm = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        var screenOn = true
        for (display in dm.displays) {
            if (display.state == Display.STATE_OFF) {
                screenOn = false
                break
            }
        }
        return screenOn
    }
}
