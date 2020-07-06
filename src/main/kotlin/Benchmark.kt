package nl.knaw.huygens.alexandria.benchmark

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import nl.knaw.huc.di.tag.tagml.importer.TAGMLImporter
import nl.knaw.huygens.alexandria.storage.BDBTAGStore
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.time.StopWatch
import org.slf4j.LoggerFactory
import tech.tablesaw.api.*
import tech.tablesaw.plotly.Plot
import tech.tablesaw.plotly.api.LinePlot
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.streams.toList

data class BenchmarkResult(
        val file: String,
        val fileSize: Long,
        val parseTimes: List<Long>,
        val markupNodes: Int,
        val textNodes: Int
)

fun main() {
    val sourcePaths = listSourcePaths()
    val results: MutableList<BenchmarkResult> = mutableListOf()
    val tmpDir: Path = mkTmpDir()
    val store = BDBTAGStore(tmpDir.toString(), false)
    for (path in sourcePaths) {
        val filesize = Files.size(path)
        print("parsing $path ($filesize):")
        val tagml = FileUtils.readFileToString(path.toFile(), "UTF-8").trim { it <= ' ' }
        val parseTimes = mutableListOf<Long>()
        var markupNodes: Int = 0
        var textNodes: Int = 0
        for (i in 0..10) {
            print(" $i")
            val stopwatch = StopWatch.createStarted()
            store.runInTransaction {
                val document = TAGMLImporter(store).importTAGML(tagml)
                markupNodes = document.dto.markupIds.size
                textNodes = document.dto.textNodeIds.size
            }
            stopwatch.stop()
            parseTimes += stopwatch.getTime(TimeUnit.MILLISECONDS)
        }
        println()
        results += BenchmarkResult(path.fileName.toString(), filesize, parseTimes, markupNodes, textNodes)
    }
    rmTmpDir(tmpDir)
    println(results
            .sortedBy { it.fileSize }
            .joinToString("\n") {
                "${it.file} (${it.fileSize}) median parse time: ${it.parseTimes.median()} ms"
            })
    plot(results.sortedBy { it.fileSize })
}

private fun listSourcePaths(): List<Path> {
    val projectDirAbsolutePath = Paths.get("").toAbsolutePath().toString()
    val dataPath = Paths.get(projectDirAbsolutePath, "data")
    return Files.walk(dataPath)
            .filter { Files.isRegularFile(it) }
            .filter { it.toString().endsWith(".tagml") }
            .toList()
}

fun List<Long>.median(): Long {
    val half = this.size / 2
    val sorted = this.sorted()
    return if (size % 2 == 0) {
        (sorted[half - 1] + sorted[half]) / 2
    } else {
        sorted[half]
    }
}

private fun mkTmpDir(): Path {
    val sysTmp = System.getProperty("java.io.tmpdir")
    var tmpPath = Paths.get(sysTmp, ".alexandria")
    rmTmpDir(tmpPath)
    if (!tmpPath.toFile().exists()) {
        tmpPath = Files.createDirectory(tmpPath)
    }
    return tmpPath
}

private fun rmTmpDir(tmpPath: Path) =
        tmpPath.toFile().deleteRecursively()

private fun plot(results: List<BenchmarkResult>) {
    (LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger).level = Level.WARN
    val fileSizeLabel = "file size (b)"
    val medianParseTimeLabel = "median parse time (ms)"
    val averageParseTimeLabel = "average parse time (ms)"
    val totalNodesLabel = "total nodes"
    val table = Table.create("Alexandria benchmark")
            .addColumns(
                    StringColumn.create("filename", results.map { it.file }),
                    LongColumn.create(fileSizeLabel, *results.map { it.fileSize }.toLongArray()),
                    IntColumn.create("markup nodes", *results.map { it.markupNodes }.toIntArray()),
                    IntColumn.create("text nodes", *results.map { it.textNodes }.toIntArray()),
                    IntColumn.create(totalNodesLabel, *results.map { it.textNodes + it.markupNodes }.toIntArray()),
                    DoubleColumn.create(averageParseTimeLabel, *results.map { it.parseTimes.average() }.toDoubleArray()),
                    LongColumn.create(medianParseTimeLabel, *results.map { it.parseTimes.median() }.toLongArray())
            )
    table.write().csv("benchmark.csv")
    FileUtils.writeStringToFile(File("benchmark.txt"), table.printAll(), "UTF-8")
    Plot.show(
            LinePlot.create(
                    "file size / median parse time",
                    table,
                    fileSizeLabel,
                    medianParseTimeLabel
            ))

    Plot.show(
            LinePlot.create(
                    "file size / average parse time",
                    table,
                    fileSizeLabel,
                    averageParseTimeLabel
            ))
    Plot.show(
            LinePlot.create(
                    "total nodes / median parse time",
                    table.sortAscendingOn(totalNodesLabel, medianParseTimeLabel),
                    totalNodesLabel,
                    medianParseTimeLabel
            ))
}
