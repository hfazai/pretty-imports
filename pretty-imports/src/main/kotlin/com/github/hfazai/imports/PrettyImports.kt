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

fun sortImports(files: List<File>, configuration: ImportConfiguration) {
  files.forEach {
    val (oldContent, newLines, fileContents) = sortImportsInternal(it, configuration)

    if(oldContent != newLines) {
      val fileContents = fileContents.replace(oldContent, newLines)
      val writer = FileWriter(it)
      writer.append(fileContents)
      writer.flush()
    }
  }
}

fun sortImports(file: File, configuration: ImportConfiguration): String {
  val sc = Scanner(file)
  val sorted = sortImports(sc, configuration)

  sc.close()
  return sorted
}

fun sortImports(file: String, configuration: ImportConfiguration): String {
  val lines = file.lines()

  return sortImports(lines.iterator(), configuration)
}

private fun sortImports(iterable: Iterator<String>, configuration: ImportConfiguration): String {
  return sortImportsInternal(iterable, configuration).second
}

private fun sortImportsInternal(file: File, configuration: ImportConfiguration): Triple<String, String, String> {
  val sc = Scanner(file)
  val sorted = sortImportsInternal(sc, configuration)

  sc.close()
  return sorted
}

private fun sortImportsInternal(iterable: Iterator<String>, configuration: ImportConfiguration): Triple<String, String, String> {
  val fileContent = StringBuffer()
  var oldContent = StringBuffer()
  val importsList = mutableListOf<String>()
  var end = false
  var found = false

  iterable.forEach { line ->
    fileContent.append(line + System.lineSeparator())
    if (!end && line.startsWith("import ")) {
      importsList.add(line)
      oldContent.append(line + System.lineSeparator())
      found = true
    } else if (!end && found && (line == System.lineSeparator() || line.trim() == "")) {
      oldContent.append(line + System.lineSeparator())
    }

    if (found) {
      if (!line.startsWith("import ") && !(line == System.lineSeparator() || line.trim() == "")) {
        end = true
      }
    }
  }

  if (oldContent.isNotEmpty()) {
    oldContent = oldContent.delete(oldContent.length - 1, oldContent.length)
  }

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

    res.forEachIndexed { _, line ->
      val beforeDot = line.substring(0, line.indexOf("."))

      if (previous != beforeDot) {
        append(System.lineSeparator())
        previous = beforeDot
      }

      append(line + System.lineSeparator())
    }
  }

  return Triple(oldContent.toString(), newContent, fileContent.toString())
}

private fun findSources(configuration: ImportConfiguration): MutableCollection<File> {
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
