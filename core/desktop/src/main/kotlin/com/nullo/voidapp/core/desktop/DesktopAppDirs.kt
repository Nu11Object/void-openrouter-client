package com.nullo.voidapp.core.desktop

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object DesktopAppDirs {
    private const val APP_ID = "com.nullo.voidapp"

    val dataDir: Path by lazy {
        resolveDataDir(
            osName = System.getProperty("os.name"),
            environment = { name -> System.getenv(name) },
            home = Paths.get(System.getProperty("user.home")),
            appId = APP_ID
        ).also { Files.createDirectories(it) }
    }

    internal fun resolveDataDir(
        osName: String,
        environment: (String) -> String?,
        home: Path,
        appId: String,
    ): Path {
        val baseDir = when {
            osName.startsWith("Windows") -> environment("LOCALAPPDATA").toAbsolutePathOrNull()
                ?: home.resolve("AppData").resolve("Local")

            osName.startsWith("Mac") -> home.resolve("Library").resolve("Application Support")

            else -> environment("XDG_DATA_HOME").toAbsolutePathOrNull()
                ?: home.resolve(".local").resolve("share")
        }
        return baseDir.resolve(appId)
    }

    private fun String?.toAbsolutePathOrNull(): Path? = this
        ?.takeIf { it.isNotBlank() }
        ?.let(Paths::get)
        ?.takeIf { it.isAbsolute }
}
