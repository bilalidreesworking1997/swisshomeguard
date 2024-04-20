package ch.swisshomeguard

import ch.swisshomeguard.data.DataRepository
import ch.swisshomeguard.data.DefaultDataRepository
import ch.swisshomeguard.data.WebService

object ServiceLocator {

    var repository: DataRepository? = null

    fun provideRepository(): DataRepository {
        synchronized(this) {
            return repository ?: createMoviesRepository()
        }
    }

    private fun createMoviesRepository(): DataRepository {
        val newRepo = DefaultDataRepository(createWebService())
        repository = newRepo
        return newRepo
    }

    private fun createWebService(): WebService = WebService.create()
}