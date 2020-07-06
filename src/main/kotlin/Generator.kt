package nl.knaw.huygens.alexandria.benchmark

import org.apache.commons.io.FileUtils
import java.io.File
import java.lang.String.format
import java.nio.file.Paths

fun main() {
    val projectDirAbsolutePath = Paths.get("").toAbsolutePath().toString()
    val dataPath = Paths.get(projectDirAbsolutePath, "data")
    val paragraphsPath = Paths.get(dataPath.toAbsolutePath().toString(), "lines.txt")
    val lines = FileUtils.readLines(paragraphsPath.toFile(), "UTF-8")
    for (i in 1..lines.lastIndex) {
        val tagml = """
            |[tagml>[p>
            |${lines.shuffled().subList(0, i).joinToString("\n") { "[l>$it<l]" }}
            |<p]<tagml]
            """.trimMargin()
        val file = File(dataPath.toAbsolutePath().toString(), format("generated_%04d_lines.tagml", i)
        )
        FileUtils.writeStringToFile(file, tagml, "UTF-8")
    }
}