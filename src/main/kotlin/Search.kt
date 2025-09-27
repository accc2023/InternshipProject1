import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isReadable
import kotlin.io.path.isRegularFile

interface Occurrence {
    val file: Path
    val line: Int
    val offset: Int
}
//hola

data class OccurrenceData (
    override val file: Path,
    override val line: Int,
    override val offset: Int
): Occurrence

fun searchForTextOccurrences(
    stringToSearch: String,
    directory: Path
): Flow<Occurrence> = channelFlow {
    require(stringToSearch.isNotEmpty()) { "stringToSearch cannot be an empty string" }
    require(Files.isDirectory(directory)) { "directory has to exist and cannot be a file" }

    Files.walk(directory).use { paths ->
        paths
            .filter { it.isRegularFile() && it.isReadable() }
            .forEach { file ->
                launch(Dispatchers.IO.limitedParallelism(32)) {
                    try {
                        Files.readAllLines(file).forEachIndexed { lineNo, line ->
                            var start = 0
                            while (true) {
                                val index = line.indexOf(stringToSearch, start)
                                if (index == -1) break
                                send(OccurrenceData(file, lineNo + 1, index))
                                start = index + 1
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
    }
}