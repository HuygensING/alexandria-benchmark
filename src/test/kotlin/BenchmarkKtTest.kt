import nl.knaw.huygens.alexandria.benchmark.median
import org.junit.Test
import kotlin.test.assertEquals

internal class BenchmarkKtTest {

    @Test
    fun test_median_of_even_number_of_longs() {
        val longs = listOf(10L, 20L, 30L, 40L, 50L, 60L, 70L, 80L, 90L, 100L)
        val median = longs.median()
        assertEquals(55, median)
    }

    @Test
    fun test_median_of_uneven_number_of_longs() {
        val longs = listOf(10L, 20L, 30L, 40L, 50L, 60L, 70L, 80L, 90L)
        val median = longs.median()
        assertEquals(50, median)
    }
}

