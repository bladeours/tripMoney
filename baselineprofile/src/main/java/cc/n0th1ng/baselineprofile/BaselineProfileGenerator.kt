package cc.n0th1ng.baselineprofile

import android.R.attr.contentDescription
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        rule.collect(
            packageName = "cc.n0th1ng.tripmoney",
            includeInStartupProfile = true
        ) {
            pressHome()
            startActivityAndWait()


            // Give Compose time to render
            Thread.sleep(500)

            val listNav = device.wait(Until.findObject(By.desc("listExpenseScreen")), 10000)
            listNav?.click() ?: throw RuntimeException("listExpenseScreen not found or not clickable")
        }
    }
}