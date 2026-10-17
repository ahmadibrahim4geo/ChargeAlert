package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.PreferencesRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Charge Alert", appName)
  }

  @Test
  fun `preferences repository default target percentage is 80`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = PreferencesRepository(context)
    val settings = repo.loadSettings()
    assertEquals(80, settings.targetPercentage)
  }
}
