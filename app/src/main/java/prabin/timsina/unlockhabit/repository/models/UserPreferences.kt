package prabin.timsina.unlockhabit.repository.models

data class UserPreferences(
    val autoLaunchPackage: String?,
    val userEnabledService: Boolean,
    val shouldLaunchDirectly: Boolean,
    val heartsLeft: Int,
    val heartsResetAt: Long,
    val dailyUnlocks: List<DailyUnlock>,
)
