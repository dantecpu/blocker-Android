package io.github.newbugger.android.libkit.utils

import io.github.newbugger.android.libkit.libsu.LibsuCommand


class ServiceHelper(private val packageName: String) {

    private var serviceInfo: String = ""
    private val serviceList: MutableList<String> = mutableListOf()

    fun isServiceRunning(serviceName: String): Boolean {
        val shortName = if (serviceName.startsWith(packageName)) {
            serviceName.removePrefix(packageName)
        } else {
            serviceName
        }
        val fullRegex = SERVICE_REGEX.format(packageName, serviceName).toRegex()
        val shortRegex = SERVICE_REGEX.format(packageName, shortName).toRegex()
        serviceList.forEach {
            if (it.contains(fullRegex) || it.contains(shortRegex)) {
                if (it.contains("app=ProcessRecord{")) {
                    return true
                }
            }
        }
        return false
    }

    fun refreshRoot() {
        serviceList.clear()
        serviceInfo = try {
            LibsuCommand.output(LibsuCommand.command("dumpsys activity services -p $packageName")).joinToString(separator = ", ")
        } catch (e: Throwable) {
            e.printStackTrace()
            "null"
        }
        parseServiceInfo()
    }

    private fun parseServiceInfo() {
        if (serviceInfo.contains("(nothing)")) {
            return
        }
        val list = serviceInfo.split("\n[\n]+".toRegex()).toMutableList()
        if (list.lastOrNull()?.contains("Connection bindings to services") == true) {
            list.removeAt(list.size - 1)
        }
        serviceList.addAll(list)
    }

    companion object {
        private const val SERVICE_REGEX = """ServiceRecord\{(.*?) %s\/%s\}"""
    }

}
