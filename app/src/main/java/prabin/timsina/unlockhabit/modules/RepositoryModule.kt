package prabin.timsina.unlockhabit.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import prabin.timsina.unlockhabit.repository.DefaultUserPreferencesRepository
import prabin.timsina.unlockhabit.repository.UserPreferencesRepository
import prabin.timsina.unlockhabit.ui.screens.app_picker.DefaultInstalledAppRepository
import prabin.timsina.unlockhabit.ui.screens.app_picker.InstalledAppRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindInstalledAppRepository(repository: DefaultInstalledAppRepository): InstalledAppRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(repository: DefaultUserPreferencesRepository): UserPreferencesRepository
}
