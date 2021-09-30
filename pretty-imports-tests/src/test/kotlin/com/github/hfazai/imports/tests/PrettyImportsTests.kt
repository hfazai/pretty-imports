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
package com.github.hfazai.imports.tests

import kotlin.test.assertEquals

import com.github.hfazai.imports.DefaultConfiguration
import com.github.hfazai.imports.Imports
import com.github.hfazai.imports.Rule
import com.github.hfazai.imports.prettifyImports
import com.github.hfazai.imports.replaceImports

import org.junit.Test

class PrettyImportsTests: ImportsTest() {
  @Test
  fun `test sort imports from same package prefix`() {
    val content =
      """import com.library.b.a;
import com.library.a.a;
import com.library.a.b;"""

    val sortedImports =
      """import com.library.a.a;
import com.library.a.b;
import com.library.b.a;"""

    val sortConfiguration = DefaultConfiguration
    val prettyImports = prettifyImports(content, sortConfiguration)

    assertEquals(Imports(content, sortedImports, content), prettyImports)
  }

  @Test
  fun `test sort imports from different packages`() {
    val content =
      """import javax.swing.JButton;
import com.library.a.a;
import java.awt.Color;"""

    val sortedImports =
      """import java.awt.Color;

import javax.swing.JButton;

import com.library.a.a;"""

    val sortConfiguration = DefaultConfiguration
    val prettyImports = prettifyImports(content, sortConfiguration)

    assertEquals(Imports(content, sortedImports, content), prettyImports)
  }

  @Test
  fun `test contents to be replaced`() {
    val content =
      """
package


import javax.swing.JButton;
import com.library.a.a;
import java.awt.Color;





Class A 
"""

    val oldImports =
      """

import javax.swing.JButton;
import com.library.a.a;
import java.awt.Color;




"""

    val sortConfiguration = DefaultConfiguration
    val prettyImports = prettifyImports(content, sortConfiguration)

    assertEquals(oldImports, prettyImports.oldImports)
  }

  @Test
  fun `test contents to be replaced without trimming`() {
    val content =
      """
package


import javax.swing.JButton;
import com.library.a.a;
import java.awt.Color;





Class A 
"""
    val trimmedImports =
      """import javax.swing.JButton;
import com.library.a.a;
import java.awt.Color;"""

    val sortedImports =
      """
package


import java.awt.Color;

import javax.swing.JButton;

import com.library.a.a;





Class A 
"""

    val sortConfiguration = Rule(DefaultConfiguration.order,
                                 DefaultConfiguration.projectPath,
                                 DefaultConfiguration.excludes,
                                 false,
                                 DefaultConfiguration.languages)
    val prettyImports = prettifyImports(content, sortConfiguration)
    val fileContents = content.replaceImports(prettyImports.oldImports, prettyImports.newImports, sortConfiguration.trim)


    assertEquals(trimmedImports, prettyImports.oldImports)
    assertEquals(sortedImports, fileContents)
  }

  @Test
  fun `test prettify imports with trimming`() {
    val content =
      """
package


import javax.swing.JButton;
import com.library.a.a;
import java.awt.Color;





Class A 
"""

    val sortedImports =
      """
package

import java.awt.Color;

import javax.swing.JButton;

import com.library.a.a;

Class A 
"""

    val sortConfiguration = DefaultConfiguration
    val prettyImports = prettifyImports(content, sortConfiguration)
    val fileContents = content.replaceImports(prettyImports.oldImports, prettyImports.newImports, sortConfiguration.trim)

    assertEquals(sortedImports, fileContents)
  }
}
