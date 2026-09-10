package no.nova.training

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import no.nova.training.ui.NovaApp
import no.nova.training.ui.theme.NovaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as NovaApplication).repository
        setContent {
            NovaTheme { NovaApp(repository) }
        }
    }
}
