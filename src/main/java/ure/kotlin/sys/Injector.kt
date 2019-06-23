package ure.kotlin.sys

import ure.kotlin.sys.dagger.AppComponent
import ure.kotlin.sys.dagger.DaggerAppComponent
import ure.sys.dagger.AppModule

/**
 * This is a static class that provides easy access to our AppModule singleton for dependency
 * injection wherever it's needed.  See the docs for dagger.AppComponent for more details.
 */
object Injector {

    @JvmStatic
    val appComponent: AppComponent by lazy {
        DaggerAppComponent.builder()
            .appModule(AppModule())
            .build()
    }
}
