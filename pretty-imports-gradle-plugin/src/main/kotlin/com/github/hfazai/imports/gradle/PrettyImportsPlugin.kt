/*
 * Copyright 2021-2022 Hichem Fazai (https://github.com/hfazai).
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
package com.github.hfazai.imports.gradle

import com.github.hfazai.imports.Rule
import com.github.hfazai.imports.prettify

import org.gradle.api.Plugin
import org.gradle.api.Project

class PrettyImportsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Add the 'greeting' extension object
        val extension = project.extensions.create("prettyImports", PrettyImportsExtension::class.java)
        // Add a task that uses configuration from the extension object
        project.task("prettyImports") {
            it.doLast {
                prettify(
                    Rule(
                        extension.order.get(),
                        extension.projectPath.get(),
                        extension.excludes.get(),
                        extension.trim.get(),
                        extension.languages.get()
                    )
                )
            }
        }
    }
}
