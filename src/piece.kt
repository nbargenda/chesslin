
open class Piece(private val color: Boolean, private val type: Char, private var hasMoved: Boolean = false){


    fun getColor(): String{
        return if (this.color) "w" else "b"
    }

    fun getType(): Char{
        return this.type
    }

    fun getHasMoved(): Boolean{
        return this.hasMoved
    }

    fun setHasMoved(){
        this.hasMoved = true
    }
}
