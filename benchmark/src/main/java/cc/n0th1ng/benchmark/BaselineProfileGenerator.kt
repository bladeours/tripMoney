package cc.n0th1ng.benchmark

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class BaselineProfileGenerator {

    @RequiresApi(Build.VERSION_CODES.P)
    @get:Rule
    val rule = BaselineProfileRule()

    @RequiresApi(Build.VERSION_CODES.P)
    @Test
    fun startup() = rule.collect(
        maxIterations = 1,
        packageName = "cc.n0th1ng.tripmoney",
        profileBlock = {
            device.executeShellCommand(
                "rm -rf /data/data/cc.n0th1ng.tripmoney/files/datastore/"
            )
            startActivityAndWait()
            device.wait(Until.hasObject(By.text("Włochy")), 10_000)
            device.findObject(By.text("Włochy")).click()
            val isVisible = device.wait(Until.hasObject(By.desc("expensesList")), 10_000)
            assert(isVisible)
            val expensesList = device.findObject(By.desc("expensesList"))
            expensesList.setGestureMargin(device.displayWidth / 5)
            expensesList.fling(Direction.DOWN)
            expensesList.fling(Direction.UP)
            expensesList.fling(Direction.DOWN)
            expensesList.fling(Direction.UP)
        }
    )


}