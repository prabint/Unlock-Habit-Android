package prabin.timsina.unlockhabit.utils

import timber.log.Timber

object AppDebugTree : Timber.DebugTree() {
    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?,
    ) {
        super.log(
            priority = priority,
            tag = "gb_$tag",
            message = message,
            t = t,
        )
    }
}
