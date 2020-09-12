class Square(var piece: Piece? = null, val rank: Int, val column: Int) {

    fun hasPiece() = this.piece != null

    fun getColor() = this.piece?.getColor()

    fun getInverseColor() = this.piece?.getInverseColor()

    fun getType() = this.piece?.type

    fun getHasMoved() = this.piece?.hasMoved

    fun emptySquare() {
        this.piece = null
    }
}
