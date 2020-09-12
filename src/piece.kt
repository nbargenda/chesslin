class Piece(private val color: Boolean, var type: Char, var hasMoved: Boolean = false) {

    fun getColor() =  if (this.color) "w" else "b"

    fun getInverseColor() = if (this.color) "b" else "w"
}
