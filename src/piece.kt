class Piece(private val color: Boolean, var type: Char, var hasMoved: Boolean = false) {

    fun getColor(): String {
        return if (this.color) "w" else "b"
    }
}
