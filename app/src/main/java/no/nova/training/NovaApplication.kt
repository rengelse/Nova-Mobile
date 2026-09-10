package no.nova.training

import android.app.Application
import no.nova.training.data.NovaRepository

class NovaApplication : Application() {
    lateinit var repository: NovaRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = NovaRepository(this)
    }
}
