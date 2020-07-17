package io.github.newbugger.android.blocker.core.prescription

import android.util.Xml
import org.xmlpull.v1.XmlSerializer
import java.io.StringWriter


object PrescriptionUtil {

    // TODO: xml formatter
    fun template(packageName: String, className: String,
                             typeC: String, sender: String,
                             action: String?, cat: String?,
                             typeF: String?, scheme: String?,
                             auth: String?, path: String?, pathOption: String?): String {
        val writer = StringWriter()
        Xml.newSerializer().apply {
            setOutput(writer)
            // setPrefix("", "http://greenify.github.io/schemas/prescription/v3")
            // startTag("", "prescriptions"); text("\n")
            startTag("", "prescription"); attribute("", "package", packageName); attribute("", "class", className); attribute("", "type", typeC); attribute("", "sender", sender)
            if (action != null) {text("\n"); startTag("", "intent-filter")}
            if (action != null) {text("\n"); startTag("", "action"); attribute("", "name", action); endTag("", "action")}
            if (cat != null) {text("\n"); startTag("", "cat"); attribute("", "name", cat); endTag("", "cat")}
            if (typeF != null) {text("\n"); startTag("", "type"); attribute("", "name", typeF); endTag("", "type")}
            if (scheme != null) {text("\n"); startTag("", "scheme"); attribute("", "name", scheme); endTag("", "scheme")}
            if (auth != null) {text("\n"); startTag("", "auth"); attribute("", "host", auth); endTag("", "auth")}
            if (path != null && pathOption != null) {text("\n"); startTag("", "path"); attribute("", pathOption, path); endTag("", "path")}
            if (action != null) {text("\n"); endTag("", "intent-filter")}
            text("\n"); endTag("", "prescription")
            // text("\n"); endTag("", "prescriptions")
            text("\n")
            endDocument()
        }
        return writer.toString()
    }

    fun footer(): String {
        return "</prescriptions>\n"
    }

    // https://developer.android.com/reference/org/xmlpull/v1/XmlSerializer
    // prefixes "xml" and "xmlns" are already bound and can not be redefined
    // or throw IllegalStateException if default namespace is already bound to non-empty string
    fun header(): String {
        return "<prescriptions xmlns=\"http://greenify.github.io/schemas/prescription/v3\">\n"
    }

    fun head(): String {
        val writer = StringWriter()
        Xml.newSerializer().apply {
            setOutput(writer)
            startDocument("UTF-8",true)
            text("\n\n")
            endDocument()
        }
        return writer.toString()
    }

    fun getSerializer(): XmlSerializer {
        val writer = StringWriter()
        return Xml.newSerializer().apply {
            setOutput(writer)
        }
    }

}
