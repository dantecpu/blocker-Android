package io.github.newbugger.android.ifw.util

import io.github.newbugger.android.ifw.entity.Rules
import org.simpleframework.xml.core.Persister
import java.io.File

object RuleSerializer {
    private val serializer by lazy { Persister() }
    private val tag = javaClass.name
    fun deserialize(file: File): Rules? {
        if (!file.exists()) {
            return null
        }
        return try {
            serializer.read(Rules::class.java, file)
        } catch (e: Exception) {
            null
        }
    }

    fun deserialize(file: String): Rules? {
        return try {
            serializer.read(Rules::class.java, file)
        } catch (e: Exception) {
            null
        }
    }
}