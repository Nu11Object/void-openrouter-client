package com.nullo.voidapp.core.utils

import androidx.window.core.layout.WindowSizeClass
import com.nullo.voidapp.core.utils.compose.DeviceConfiguration
import com.nullo.voidapp.core.utils.compose.DeviceConfiguration.Companion.fromWindowSizeClass
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

private fun deviceConfiguration(widthDp: Int, heightDp: Int): DeviceConfiguration =
    fromWindowSizeClass(WindowSizeClass(widthDp, heightDp))

val DeviceConfigurationTest by testSuite {
    test("zero size is mobile portrait") {
        deviceConfiguration(0, 0) shouldBe DeviceConfiguration.MOBILE_PORTRAIT
    }

    test("width below the medium breakpoint is mobile portrait") {
        deviceConfiguration(599, 1000) shouldBe DeviceConfiguration.MOBILE_PORTRAIT
    }

    test("width below the medium breakpoint and short height is mobile portrait") {
        deviceConfiguration(599, 479) shouldBe DeviceConfiguration.MOBILE_PORTRAIT
    }

    test("medium width and medium height is tablet portrait") {
        deviceConfiguration(600, 480) shouldBe DeviceConfiguration.TABLET_PORTRAIT
    }

    test("medium width just below expanded and medium height is tablet portrait") {
        deviceConfiguration(839, 480) shouldBe DeviceConfiguration.TABLET_PORTRAIT
    }

    test("medium width and short height is tablet landscape") {
        deviceConfiguration(600, 479) shouldBe DeviceConfiguration.TABLET_LANDSCAPE
    }

    test("medium width and tall height is tablet portrait") {
        deviceConfiguration(600, 1000) shouldBe DeviceConfiguration.TABLET_PORTRAIT
    }

    test("expanded width and medium height is desktop") {
        deviceConfiguration(840, 480) shouldBe DeviceConfiguration.DESKTOP
    }

    test("expanded width and short height is mobile landscape") {
        deviceConfiguration(840, 479) shouldBe DeviceConfiguration.MOBILE_LANDSCAPE
    }

    test("expanded width and tall height is desktop") {
        deviceConfiguration(1200, 1000) shouldBe DeviceConfiguration.DESKTOP
    }
}
