import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Path
import org.junit.jupiter.api.assertThrows

class SearchTest {

    val abs = "/root/path/here/" // include your root path till project folder
    // Note all of these tests pass. Also included in the directory is a png file and the search
    // function is able to pass over when iterating through files in the specified directory

    @Test
    fun hola() = runBlocking {
        // edit below file path to test (left mine below as an example)
        val dir = Path.of(abs + "/InternshipProject1/src")
        val result = searchForTextOccurrences("hola", dir).toList()

        val simplified = result
            .map { Triple(it.file.fileName.toString(), it.line, it.offset) }
            .sortedWith(compareBy({ it.first }, { it.second }, { it.third }))

        assertTrue(simplified.contains(Triple("Search.kt", 15, 2)))
    }

    @Test
    fun holaSize() = runBlocking {
        // edit below file path to test
        val dir = Path.of(abs + "InternshipProject1/src")
        val result = searchForTextOccurrences("hola", dir).toList()

        val simplified = result
            .map { Triple(it.file.fileName.toString(), it.line, it.offset) }
            .sortedWith(compareBy({ it.first }, { it.second }, { it.third }))

        assertEquals(5, simplified.size)
    }

    @Test
    fun email() = runBlocking {
        // add your own file path
        val dir = Path.of(abs + "InternshipProject1/src/main/")
        val result = searchForTextOccurrences("arhanc21@gmail.com", dir).toList()

        val simplified = result
            .map { Triple(it.file.fileName.toString(), it.line, it.offset) }
            .sortedWith(compareBy({ it.first }, { it.second }, { it.third }))

        assertTrue(simplified.contains(Triple("plugin.xml", 4, 19)))
        assertEquals(2, simplified.size)
    }

    @Test
    fun empty(): Unit = runBlocking {
        // add your own file path
        val dir = Path.of(abs + "InternshipProject1/src/main/")
        // Cannot first house outcome in variable called "result" as it will throw an error before we check
        assertThrows<IllegalArgumentException> {
            searchForTextOccurrences("", dir).toList()
        }
    }

    @Test
    fun badPath(): Unit = runBlocking {
        val dir = Path.of("/bad/path")

        // Path does not exist; tests successfully
        assertThrows<IllegalArgumentException> {
            searchForTextOccurrences("good times", dir).toList()
        }
    }
}