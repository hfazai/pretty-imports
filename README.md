# pretty-imports

Lightweight library for cleaning imports in source code.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Presentation

The main purpose of this library is to programmatically clean and order the imports in source code files of a project.

This tool is configurable and extremely easy to use.

### Usage

**Configuration:**

The configuration API allows to specify the order of imports, either you want to remove empty spaces or not, the path to the source code, excluded files, etc.

This is an example:

````KOTLIN
val configuration = ImportConfiguration(
  order = listOf("java", "javax", "kotlin", "com", "org"),
  trim = true,
  path = "path_to_project",
  exclude = "*Options*.java" // exclude some generated files
)
````

**Pretty imports usage:**

- Kotlin:
````KOTLIN
val configuration = ImportConfiguration([params])

prettify(configuration)
````

### Supported languages:
For the moment, this is the supported languages: 
- Kotlin.
- Java.

This library is extendable and will support many other languages in the future.

## Roadmap:
- The library still under construction.
- A stable version will be released very soon.
- Allow configuration using json files.
- Supporting as much as possible of programming languages.
- Creating a gradle plugin to allow using pretty imports via gradle.
- Using pretty imports from command line.

## Contributing
All PRs are welcome. For feature requests and bug reports, please feel free to create an issue.

## License
Licensed under Apache 2.0. See [Licence](Licence).

Copyright 2021-2021 Hichem Fazai
