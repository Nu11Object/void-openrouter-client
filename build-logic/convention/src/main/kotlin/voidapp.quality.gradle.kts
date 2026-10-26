import com.nullo.voidapp.buildlogic.requiredVersion

plugins {
    id("org.jlleitschuh.gradle.ktlint")
}

ktlint {
    version.set(project.requiredVersion("ktlint-engine"))
    outputToConsole.set(true)
    outputColorName.set("RED")

    filter {
        val buildDirectory = project.layout.buildDirectory
        exclude { element ->
            element.file.invariantSeparatorsPath.startsWith(
                buildDirectory.get().asFile.invariantSeparatorsPath + "/"
            )
        }
    }
}
