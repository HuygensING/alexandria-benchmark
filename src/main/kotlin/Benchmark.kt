package nl.knaw.huygens.alexandria.benchmark

import nl.knaw.huc.di.tag.tagml.importer.TAGMLImporter
import nl.knaw.huygens.alexandria.storage.BDBTAGStore
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.time.StopWatch
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList

data class BenchmarkResult(val file: String, val fileSize: Long, val parseTimes: List<Long>)

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
        for (i in 0..10) {
            print(" $i")
            val stopwatch = StopWatch.createStarted()
            store.runInTransaction { TAGMLImporter(store).importTAGML(tagml) }
            stopwatch.stop()
            parseTimes += stopwatch.nanoTime
        }
        println()
        results += BenchmarkResult(path.fileName.toString(), filesize, parseTimes)
    }
    rmTmpDir(tmpDir)
    println(results.sortedBy { it.fileSize }.joinToString("\n"))
    println(results.map { it.parseTimes.median() })
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
    if (!tmpPath.toFile().exists()) {
        tmpPath = Files.createDirectory(tmpPath)
    }
    return tmpPath
}

private fun rmTmpDir(tmpPath: Path) =
        Files.walk(tmpPath)
                .map { it.toFile() }
                .forEach { it.deleteOnExit() }

