package com.arhan

import searchForTextOccurrences
import java.nio.file.Path
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        val src = Path.of("/abs/path/here")
        val flow = searchForTextOccurrences("arhanc21@gmail.com", src)

        flow.collect { occurrence ->
            println("${occurrence.file}:${occurrence.line}:${occurrence.offset}")
        }
    }
}