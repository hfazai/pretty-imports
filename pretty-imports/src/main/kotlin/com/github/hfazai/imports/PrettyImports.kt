/*
 * Copyright 2021-2021 Hichem Fazai (https://github.com/hfazai).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.hfazai.imports

import java.io.File
import java.io.FileWriter
import java.util.Scanner

import org.apache.commons.io.filefilter.DirectoryFileFilter
import org.apache.commons.io.filefilter.RegexFileFilter
import org.apache.commons.io.FileUtils.listFiles

fun prettify(configuration: Rule) {
  val sourceFiles = findSources(configuration)

  prettifyImports(sourceFiles, configuration)
}

fun prettifyImports(files: MutableCollection<File>, configuration: Rule) {
  files.forEach {
    val (oldContent, newLines, fileContents) = prettifyImportsInternal(it, configuration)

    if (oldContent != newLines) {
      val fileContents = fileContents.replaceImports(oldContent, newLines, configuration.trim)
      val writer = FileWriter(it)
      writer.append(fileContents)
      writer.flush()
    }
  }
}

fun prettifyImports(fileContent: String, configuration: Rule): Imports {
  val lines = fileContent.lines()

  return prettifyImportsInternal(lines.iterator(), configuration)
}

private fun prettifyImportsInternal(file: File, configuration: Rule): Imports {
  val sc = Scanner(file)
  val sorted = prettifyImportsInternal(sc, configuration)

  sc.close()
  return sorted
}

private fun prettifyImportsInternal(iterable: Iterator<String>, configuration: Rule): Imports {
  val fileContent = StringBuffer()
  val oldContentBuffer = StringBuffer()
  val importsList = mutableListOf<String>()
  var found = false
  var end = false

  while (iterable.hasNext()) {
    val line = if (iterable is Scanner) {
      iterable.nextLine()
    } else {
      iterable.next()
    }

    // Append line to file contents
    fileContent.append(line + System.lineSeparator())

    when {
      end -> {
        continue
      }
      line.trim().startsWith("import ") -> {
        importsList.add(line.trim())
        oldContentBuffer.append(line + System.lineSeparator())
        found = true
      }
      !found && line.isNotBlank() -> {
        oldContentBuffer.setLength(0)
      }
      !found -> {
        oldContentBuffer.append(line + System.lineSeparator())
      }
      found && line.isBlank() -> {
        oldContentBuffer.append(line + System.lineSeparator())
      }
      found && line.isNotBlank() -> {
        end = true
      }
    }
  }

  val oldContent =
    (if (!configuration.trim) {
      oldContentBuffer.toString().trim()
    } else {
      oldContentBuffer.toString()
    }).removeSuffix(System.lineSeparator())

  importsList.sortBy {
    it.removeSuffix(";")
  }

  // MAP: order -> imports
  val orderMap = mutableMapOf<String, MutableList<String>>()

  importsList.forEach { import ->
    val order = configuration.order.firstOrNull {
      import.startsWith("import ${it.removeSuffix(".")}.")
    } ?: "other"

    if (orderMap[order] == null) {
      orderMap[order] = mutableListOf(import)
    } else {
      orderMap[order]!!.add(import)
    }
  }

  val order = configuration.order + "other"
  val res = mutableListOf<String>()

  order.forEach {
    val sortedImports = orderMap[it]

    if (sortedImports != null) {
      res.addAll(sortedImports.filterNot { it == "" })
    }
  }

  val newContent = buildString {
    var previous = res.firstOrNull()?.substring(0, res.first().indexOf("."))

    res.forEachIndexed { index, line ->
      val beforeDot = line.substring(0, line.indexOf("."))

      if (previous != beforeDot) {
        append(System.lineSeparator())
        previous = beforeDot
      }

      if (index == res.size - 1) {
        append(line)
      } else {
        append(line + System.lineSeparator())
      }
    }
  }

  return Imports(oldContent, newContent, fileContent.toString())
}

fun String.replaceImports(oldContent: String, newContent: String, trimmed: Boolean): String {
  return if (trimmed) {
    replace(oldContent, System.lineSeparator() + newContent + System.lineSeparator())
  } else {
    replace(oldContent, newContent)
  }
}

private fun findSources(configuration: Rule): MutableCollection<File> {
  val pathName = configuration.projectPath
  val pattern =
    configuration.languages
      .joinToString("|", "(", ")") {
        ".*" + it.extension.removePrefix(".")
      }

  return listFiles(
    File(pathName),
    RegexFileFilter(pattern),
    DirectoryFileFilter.DIRECTORY
  )
}

data class Imports(val importsToReplace: String, val newImports: String, val fileContent: String)
