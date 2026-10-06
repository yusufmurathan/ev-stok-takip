package com.evstok.app

import android.app.Application
import android.content.Context
import com.evstok.app.data.StockRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class EvStokApp : Application() {

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val container: AppContainer by lazy {
        AppContainer(this, appScope)
    }
}

class AppContainer(context: Context, scope: CoroutineScope) {
    val repository: StockRepository = StockRepository(context, scope)
}
