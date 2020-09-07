
open class Piece(private val color: Boolean, val type: Char, var hasMoved: Boolean = false){


    fun getColor(): String{
        return if (this.color) "w" else "b"
    }

    fun setHasMoved(){
        this.hasMoved = true
    }
}