package ure.kotlin.log

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import java.io.IOException
import java.util.logging.LogManager
import kotlin.reflect.KClass

inline fun <reified T> T.getLog(): Log =
    LogFactory.getLog(T::class.java)

fun configureLog(clazz: KClass<*>) {
    try {
        val configInputStream = clazz.java.getResourceAsStream("/logging.properties")
        LogManager.getLogManager().readConfiguration(configInputStream)
    } catch (e: IOException) {
        throw Error("Can't configure logger", e)
    }
}
