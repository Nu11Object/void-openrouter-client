import com.nullo.voidapp.buildlogic.library

plugins {
    id("com.google.devtools.ksp")
    id("androidx.room3")
}

dependencies {
    add("kspAndroid", library("androidx-room3-compiler"))
    add("kspJvm", library("androidx-room3-compiler"))
    add("kspIosArm64", library("androidx-room3-compiler"))
    add("kspIosSimulatorArm64", library("androidx-room3-compiler"))
}
