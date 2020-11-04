package shreckye.covscript.simplisticide.kotlin

sealed class FList<out T> : Iterable<T> {
    abstract val head: T
    abstract val tail: FList<T>
    override fun iterator(): Iterator<T> = object : Iterator<T> {
        private var current: FList<T> = this@FList
        override fun hasNext(): Boolean =
            current is FCons<T>

        override fun next(): T =
            (current as? FCons<T>)?.run {
                current = tail
                head
            } ?: throw NoSuchElementException()
    }
}

class FCons<out T>(override val head: T, override val tail: FList<T>) : FList<T>()
object FNil : FList<Nothing>() {
    override val head: Nothing get() = throw NoSuchElementException()
    override val tail: FList<Nothing> get() = throw NoSuchElementException()
}

@Suppress("NOTHING_TO_INLINE")
inline infix fun <T> T.cons(tail: FList<T>) = FCons(this, tail)