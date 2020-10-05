package shreckye.covscript.simplisticide.kotlin

// A simplistic implementation of Scala `Try`
sealed class Try<out T> {
    abstract fun isSuccess(): Boolean
    abstract fun get(): T
}

class Success<out T>(val value: T) : Try<T>() {
    override fun isSuccess(): Boolean = true
    override fun get(): T = value
}

class Failure<out T>(val e: Exception) : Try<T>() {
    override fun isSuccess(): Boolean = false
    override fun get(): T = throw e
}

inline fun <T> tryBlock(block: () -> T) =
    try {
        Success(block())
    } catch (e: Exception) {
        Failure(e)
    }