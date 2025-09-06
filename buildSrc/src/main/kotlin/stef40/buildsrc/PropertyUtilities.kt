package stef40.buildsrc

import java.io.File
import java.util.Properties

private val localProperties by lazy {
	Properties().also { File("local.properties").takeIf(File::isFile)?.bufferedReader()?.use(it::load) }
}

fun getPropertyValue(
	key: String
) = (System.getProperty(key) ?: localProperties.getProperty(key))?.ifEmpty { null }
