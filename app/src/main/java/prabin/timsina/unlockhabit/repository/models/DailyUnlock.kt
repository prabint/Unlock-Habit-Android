package prabin.timsina.unlockhabit.repository.models

import kotlinx.serialization.Serializable

@Serializable
data class DailyUnlock(
    val date: String, // MM-dd-yyyy
    val count: Int,
)
