package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.MainScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = mainViewModel)
            }
        }
    }

    /**
     * Refresh ViewModel state whenever the user returns to the app.
     * This is important after the user:
     * - Grants notification permissions in system settings
     * - Enables battery optimization exemption
     * - Returns from the lockscreen activity
     * Without this, the AppStatusCard would show stale permission status.
     */
    override fun onResume() {
        super.onResume()
        mainViewModel.refreshPermissions()
        mainViewModel.syncServiceState()
    }

    /**
     * Handle re-delivery of the intent when the activity is re-launched as singleTop.
     * This prevents multiple instances when a notification tap brings the app to front.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
