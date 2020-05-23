package io.github.newbugger.android.ifw.util

import android.util.Log
import io.github.newbugger.android.ifw.entity.Rules
import org.simpleframework.xml.core.Persister
import java.io.File

object RuleSerializer {
    private val serializer by lazy { Persister() }
    private const val tag = "io.github.newbugger.android.ifw.util.RuleSerializer"
    fun deserialize(file: File): Rules? {
        if (!file.exists()) {
            return null
        }
        return try {
            serializer.read(Rules::class.java, file)
        } catch (e: Exception) {
            Log.e(tag, "${file.absolutePath} is not a valid ifw rule, skipping", e)
            null
        }
    }
}