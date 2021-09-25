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

fun sortImports(imports: String, sortConfiguration: SortConfiguration): String {
  val lines = imports.lines()
  var buffer = StringBuffer()
  val importsList = mutableListOf<String>()
  var end = false
  var found = false

  lines.forEach { line ->
    if (!end && line.startsWith("import ")) {
      importsList.add(line)
      buffer.append(line + System.lineSeparator())
      found = true
    } else if (!end && found && (line == System.lineSeparator() || line.trim() == "")) {

      buffer.append(line + System.lineSeparator())
    }
    if (found) {
      if (!line.startsWith("import ") && !(line == System.lineSeparator() || line.trim() == "")) {
        end = true
      }
    }
  }

  if (buffer.isNotEmpty()) {
    buffer = buffer.delete(buffer.length - 1, buffer.length)
  }

  importsList.sortBy {
    it.removeSuffix(";")
  }

  // MAP: order -> imports
  val orderMap = mutableMapOf<String, MutableList<String>>()

  importsList.forEach { import ->
    val order = sortConfiguration.order.firstOrNull {
      import.startsWith("import ${it.removeSuffix(".")}.")
    } ?: "other"

    if (orderMap[order] == null) {
      orderMap[order] = mutableListOf(import)
    } else {
      orderMap[order]!!.add(import)
    }
  }

  val order = sortConfiguration.order + "other"
  val res = mutableListOf<String>()

  order.forEach {
    val sortedImports = orderMap[it]

    if (sortedImports != null) {
      res.addAll(sortedImports.filterNot { it == "" })
    }
  }

  return buildString {
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
}
