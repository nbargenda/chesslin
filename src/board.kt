import java.lang.IndexOutOfBoundsException
import java.lang.NullPointerException
import kotlin.math.absoluteValue

class Board{


    var squares = arrayListOf<ArrayList<Square>>()
    val moveHistory = arrayListOf<Move>()
    private val moves = Moves()

    init{
        for(i in 0..7){
            squares.add(arrayListOf())
            for (j in 0..7){
                squares[i].add(Square(null,i,j))
            }
        }
    }

    fun hasMoves(square: Square): Boolean{
         return possibleMoves(square).isNotEmpty()
    }

    fun removePinnedMoves(possibleMoves: MutableSet<ArrayList<Square>>, otherMoves: MutableSet<ArrayList<Square>>): MutableSet<ArrayList<Square>> {
        val result = mutableSetOf<ArrayList<Square>>()
        possibleMoves.forEach {
            val pinningPiece = isPinned(it[0], otherMoves)
            if(pinningPiece.hasPiece()){
                it.forEach { square->
                    if (square != pinningPiece) result.add(it)
                }
            }
        }
        possibleMoves.removeAll(result)
        return possibleMoves
    }

    fun possibleMovesSquare(square: Square): ArrayList<Square>{
        val result = arrayListOf(square)
        result.addAll(possibleMoves(square))
        return result
    }

    private fun findKing(color: String?): Square{
        val pieces = getPieces()
        pieces.forEach {
            if (it.getType() == 'K' && it.getColor() == color) return it
        }
        return defaultSquare
    }

/*         |    |
*     lu   | us |   ru
*  ________|____|________
*     ls   | K  |   rs
*  ________|____|________
*          |    |
*     ld   | ds |  rd
*          |    |
*
 */
    private fun wouldPinItself(square: Square, squareThatChecks: Square): Boolean{
        val kingSquare = findKing(square.getColor())
        val threatenedSquares = threatenedSquaresRBQ(squareThatChecks)
    val where: String = when {
        kingSquare.getX() > squareThatChecks.getX() -> when {
            kingSquare.getY() > squareThatChecks.getY() -> "ld"
            kingSquare.getY() < squareThatChecks.getY() -> "rd"
            else -> "ds"
        }
        kingSquare.getX() < squareThatChecks.getX() -> when {
            kingSquare.getY() > squareThatChecks.getY() -> "lu"
            kingSquare.getY() < squareThatChecks.getY() -> "ru"
            else -> "us"
        }
        else -> if (kingSquare.getY() < squareThatChecks.getY()) "rs"
        else "ls"
    }

        threatenedSquares.forEach {
            when (where){
                "ld" ->{if(square.getX() > it.getX() && square.getY() > it.getY() && square == it) return true}
                "rd" ->{if(square.getX() > it.getX() && square.getY() < it.getY() && square == it) return true}
                "lu" ->{if(square.getX() < it.getX() && square.getY() > it.getY() && square == it) return true}
                "ru" ->{if(square.getX() < it.getX() && square.getY() < it.getY() && square == it) return true}
                "ds" ->{if (it.getY() == kingSquare.getY() && it.getX() > squareThatChecks.getX()) return true}
                "us" ->{if (it.getY() == kingSquare.getY() && it.getX() < squareThatChecks.getX()) return true}
                "rs" ->{if (it.getX() == kingSquare.getX() && it.getY() < squareThatChecks.getY()) return true}
                "ls" ->{if (it.getX() == kingSquare.getX() && it.getY() > squareThatChecks.getY()) return true}
            }
        }
        return false
    }

    private fun isPinned(square: Square, otherMoves: MutableSet<ArrayList<Square>>): Square{
        val kingSquare = findKing(square.getColor())
        val threateningSquares: MutableSet<Square> = mutableSetOf()
        if (square.getType() == 'K') return defaultSquare
        if (square.getX() != kingSquare.getX() && square.getY() != kingSquare.getY() &&
            ((square.getX()-kingSquare.getX()).absoluteValue != (square.getY()-kingSquare.getY()).absoluteValue))
            return defaultSquare
        removeEmptyMoves(otherMoves).forEach {
            try{
                if (it[0].getType()!! in "QBR"){
                    it.forEach{threatSquare->
                        if (square == threatSquare) threateningSquares.add(it[0])
                    }
                }
            }
            catch(e: NullPointerException){
                e.printStackTrace()
                return defaultSquare
            }
        }
        threateningSquares.forEach {
            when {
                it.getX() > square.getX() ->{
                    when {
                        it.getY() < square.getY() ->{
                            val x = square.getX()
                            val y = square.getY()
                            for (i in 1..(maxOf(x,y)..7).count()){
                                try{
                                    if(this.squares[x-i][y+i].hasPiece()){
                                        if(this.squares[x-i][y+i].getType() == 'K') return it
                                        return defaultSquare
                                    }
                                }
                                catch(e: IndexOutOfBoundsException){
                                    return defaultSquare
                                }
                            }
                        } // unten rechts
                        it.getY() > square.getY() ->{
                            val x = square.getX()
                            val y = square.getY()
                            for (i in 1..(maxOf(x,y)..7).count()){
                                try{
                                    if(this.squares[x-i][y-i].hasPiece()){
                                        if(this.squares[x-i][y-i].getType() == 'K') return it
                                        return defaultSquare
                                    }
                                }
                                catch(e: IndexOutOfBoundsException){
                                    return defaultSquare
                                }
                            }
                        } // unten links
                        else->{
                            for (i in square.getX()-1 downTo 0){
                                if(this.squares[i][it.getY()].hasPiece()){
                                    if(this.squares[i][it.getY()].getType() =='K') return it
                                    return defaultSquare
                                }
                            }
                        }
                    }
                }
                it.getX() < square.getX() ->{
                    when {
                        it.getY() < square.getY() ->{
                            val x = square.getX()
                            val y = square.getY()
                            for (i in 1..(maxOf(x,y)..7).count()){
                                try{
                                    if(this.squares[x+i][y+i].hasPiece()){
                                        if(this.squares[x+i][y+i].getType() == 'K') return it
                                        return defaultSquare
                                    }
                                }
                                catch(e: IndexOutOfBoundsException){
                                    return defaultSquare
                                }
                            }
                        } // oben rechts
                        it.getY() > square.getY() ->{
                            val x = square.getX()
                            val y = square.getY()
                            for (i in 1..(maxOf(x,y)..7).count()){
                                try{
                                    if(this.squares[x+i][y-i].hasPiece()){
                                        if(this.squares[x+i][y-i].getType() == 'K') return it
                                        return defaultSquare
                                    }
                                }
                                catch(e: IndexOutOfBoundsException){
                                    return defaultSquare
                                }
                            }
                        } // oben links
                        else->{
                            for (i in square.getX()+1 ..7){
                                if(this.squares[i][it.getY()].hasPiece()){
                                    if(this.squares[i][it.getY()].getType() =='K') return it
                                    return defaultSquare
                                }
                            }
                        }
                    }
                }

                else -> {
                    if(it.getY() < square.getY()){
                        for (i in square.getY()+1..7){
                            if(this.squares[it.getX()][i].hasPiece()){
                                if(this.squares[it.getX()][i].getType() == 'K') return it
                                return defaultSquare
                            }
                        }
                    }
                    else{
                        for (i in square.getY()-1 downTo 0){
                            if(this.squares[it.getX()][i].hasPiece()){
                                if(this.squares[it.getX()][i].getType() == 'K') return it
                                return defaultSquare
                            }
                        }
                    }
                }
            }
        }

        return defaultSquare
    }

    fun possibleMovesCheck(moves: MutableSet<ArrayList<Square>>, otherMoves: MutableSet<ArrayList<Square>>): MutableSet<ArrayList<Square>>{
        val result = mutableSetOf<ArrayList<Square>>()
        var threats = 0
        val king = findKing(moves.first()[0].getColor())
        val checkSquares = mutableSetOf<Square>()
        val threatenedSquares = mutableSetOf<Square>()
        // find out if 1 or 2 threats
        // if 1 = can kill/block threat IF NOT PINNED
        // if 2+ = only king can move, OR maybe block?
        otherMoves.forEach {
            if (it.contains(king)) {
                threats++
                checkSquares.add(it[0])
            }
            threatenedSquares.addAll(threatenedSquares(it[0]))
        }
        if (threats>1){
            moves.forEach {
                if (it[0].getType() == 'K'){
                    if (!threatenedSquares.contains(it[1])) result.add(it)
                }
            }
        }
        else {
            moves.forEach{
                when (it[0].getType()) {
                    'K' -> if (!threatenedSquares.contains(it[1])) result.add(it)
                    else -> {
                        if(!isPinned(it[0], otherMoves).hasPiece()){
                            checkSquares.forEach { checkSquare->
                                when (checkSquare.getType()){
                                    'P','N' -> {
                                        it.forEach{square->
                                            if (square!=it[0] && square == checkSquare) result.add(arrayListOf(it[0], square))
                                        }
                                    }
                                    'Q','B','R' -> {
                                        it.forEach{square->
                                            if (square!=it[0] && ((square == checkSquare) || (wouldPinItself(square, checkSquares.first())))) result.add(arrayListOf(it[0], square))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return result
    }

    private fun possibleMoves(square: Square): MutableSet<Square>{
        val result = mutableSetOf<Square>()
        if (square.getType()=='P'){
            if(square.getColor()=="w" && !this.squares[square.getX()+1][square.getY()].hasPiece()){

                if (!square.getHasMoved()!! && !this.squares[square.getX()+2][square.getY()].hasPiece() )
                    result.add(this.squares[square.getX()+2][square.getY()])
                result.add(this.squares[square.getX()+1][square.getY()])

            }
            else if(square.getColor()=="b" && !this.squares[square.getX()-1][square.getY()].hasPiece()){
                if (!square.getHasMoved()!! && !this.squares[square.getX()-2][square.getY()].hasPiece() )
                    result.add(this.squares[square.getX()-2][square.getY()])
                result.add(this.squares[square.getX()-1][square.getY()])
            }

        }
        if (square.getType()=='K' && !square.getHasMoved()!!){
            when {
                checkCastlingShort(square) -> {
                    if(square.getColor() == "w")  result.add(this.squares[0][6])
                    else                          result.add(this.squares[7][6])
                }
                checkCastlingLong(square)  -> {
                    if (square.getColor() == "w") result.add(this.squares[0][2])
                    else                          result.add(this.squares[7][2])
                }
            }
        }
        val threatenedSquares = threatenedSquares(square)
        threatenedSquares.forEach {
                if (it.getColor()!=square.getColor()) result.add(it)
            }
        return result
    }

    private fun removeInvalidMoves(squares: MutableSet<List<Int>>): MutableSet<Square>{
        val result = mutableSetOf<Square>()
        squares.forEach {
            if (it[0] in 0..7 && it[1] in 0..7) {
                result.add(this.squares[it[0]][it[1]])
            }
        }
        return result
    }


    fun defendedThreatenedSquares(square: Square): ArrayList<Set<Square>>{
        val color = square.getColor()
        val defSquares = mutableSetOf<Square>()
        val threatSquares = mutableSetOf<Square>()
        val threatenedSquares = threatenedSquares(square)
        threatenedSquares.forEach {
            if (it.getColor().equals(color)) defSquares.add(it)
            else threatSquares.add(it)
        }
        return arrayListOf(defSquares,threatSquares)
    }

    private fun threatenedSquares(square: Square): Set<Square> {
        return when (square.getType()){
            'P' -> threatenedSquaresPawn(square)
            'K','N' -> threatenedSquaresKnightKing(square)
            'B','R','Q' -> threatenedSquaresRBQ(square)
            else -> setOf()
        }
    }

    private fun checkCastlingShort(square: Square): Boolean{
        val threatenedSquares:MutableSet<Set<Square>> = mutableSetOf()
        val pieces:MutableSet<Square>
        try{
            if(square.getColor()=="w"){
                pieces = getBlackPieces(getPieces())
                pieces.forEach {
                    threatenedSquares.add(threatenedSquares(it))
                }
                if (!this.squares[0][7].getHasMoved()!! && !this.squares[0][5].hasPiece() && !this.squares[0][6].hasPiece()){
                    threatenedSquares.forEach {
                        if (it.contains(this.squares[0][5]) || it.contains(this.squares[0][6]) || it.contains(this.squares[0][4])) return false
                    }
                    return true
                }
            }
            else {
                pieces = getWhitePieces(getPieces())
                pieces.forEach {
                    threatenedSquares.add(threatenedSquares(it))
                }
                if (!this.squares[7][7].getHasMoved()!! && !this.squares[7][5].hasPiece() && !this.squares[7][6].hasPiece()){
                    threatenedSquares.forEach {
                        if (it.contains(this.squares[7][5]) || it.contains(this.squares[7][6]) || it.contains(this.squares[7][4])) return false
                    }
                    return true
                }
            }
        }
        catch(e: NullPointerException){
            return false
        }
        return false
    }

    private fun checkCastlingLong(square: Square): Boolean{
        val threatenedSquares:MutableSet<Set<Square>> = mutableSetOf()
        val pieces:MutableSet<Square>
        try{
            if(square.getColor()=="w"){
                pieces = getBlackPieces(getPieces())
                pieces.forEach {
                    threatenedSquares.add(threatenedSquares(it))
                }
                if (!this.squares[0][0].getHasMoved()!! && !this.squares[0][3].hasPiece() && !this.squares[0][2].hasPiece() && !this.squares[0][1].hasPiece()){
                    threatenedSquares.forEach {
                        if (it.contains(this.squares[0][4]) || it.contains(this.squares[0][3]) || it.contains(this.squares[0][2]) || it.contains(this.squares[0][1])) return false
                    }
                    return true
                }
            }
            else {
                pieces = getWhitePieces(getPieces())
                pieces.forEach {
                    threatenedSquares.add(threatenedSquares(it))
                }
                if (!this.squares[7][0].getHasMoved()!! && !this.squares[7][3].hasPiece() && !this.squares[7][2].hasPiece() && !this.squares[7][1].hasPiece()){
                    threatenedSquares.forEach {
                        if (it.contains(this.squares[7][4]) || it.contains(this.squares[7][3]) || it.contains(this.squares[7][2]) || it.contains(this.squares[7][1])) return false
                    }
                    return true
                }

            }
            return false
        }
        catch(e: NullPointerException){
            return false
        }
    }
    private fun checkEnPassanteLeft(square: Square, lastMove: Move): Boolean{
        return if (square.getColor()=="w"){
            lastMove.squareTo.getType()=='P' && lastMove.squareFrom.getX()==square.getX()+2 &&
                    lastMove.squareFrom.getY()==square.getY()-1 && lastMove.squareTo.getY()==square.getY()-1 &&
                    lastMove.squareTo.getX()==square.getX()
        } else{
            lastMove.squareTo.getType()=='P' && lastMove.squareFrom.getX()==square.getX()-2 &&
                    lastMove.squareFrom.getY()==square.getY()-1 && lastMove.squareTo.getY()==square.getY()-1 &&
                    lastMove.squareTo.getX()==square.getX()
        }
    }

    private fun checkEnPassanteRight(square: Square, lastMove: Move): Boolean{
        return if (square.getColor()=="w"){
            lastMove.squareTo.getType()=='P' && lastMove.squareFrom.getX()==square.getX()+2 &&
                    lastMove.squareFrom.getY()==square.getY()+1 && lastMove.squareTo.getY()==square.getY()+1 &&
                    lastMove.squareTo.getX()==square.getX()
        } else{
            lastMove.squareTo.getType()=='P' && lastMove.squareFrom.getX()==square.getX()-2 &&
                    lastMove.squareFrom.getY()==square.getY()+1 && lastMove.squareTo.getY()==square.getY()+1 &&
                    lastMove.squareTo.getX()==square.getX()
        }
    }

    private fun threatenedSquaresPawn(square: Square): MutableSet<Square>{
        var lastMove = defaultMove
        val result = mutableSetOf<Square>()
        if (this.moveHistory.isNotEmpty()) {
            lastMove = this.moveHistory.last()
        }

        if (square.getColor() == "w"){
            if (square.getY()>0 && (this.squares[square.getX()+1][square.getY()-1].hasPiece() || checkEnPassanteLeft(square, lastMove)))
                result.add(this.squares[square.getX()+1][square.getY()-1])
            if (square.getY()<7 && (this.squares[square.getX()+1][square.getY()+1].hasPiece() || checkEnPassanteRight(square, lastMove)))
                result.add(this.squares[square.getX()+1][square.getY()+1])
        }

        else {
            if (square.getY()>0 && (this.squares[square.getX()-1][square.getY()-1].hasPiece() || checkEnPassanteLeft(square, lastMove)))
                result.add(this.squares[square.getX()-1][square.getY()-1])
            if (square.getY()<7 && (this.squares[square.getX()-1][square.getY()+1].hasPiece() || checkEnPassanteRight(square, lastMove)))
                result.add(this.squares[square.getX()-1][square.getY()+1])
        }

        return result
    }

    private fun checkBlock(squares: MutableSet<Square>): MutableSet<List<Int>> {
        val result = mutableSetOf<List<Int>>()
        squares.forEach {
            result.add(listOf(it.getX(),it.getY()))
            if (this.squares[it.getX()][it.getY()].hasPiece()) return result
        }
        return result
    }

    private fun threatenedSquaresRBQ(square: Square): MutableSet<Square>{
        val possibleMoves = moves.getMove(square.getType())
        val possibleSquaresUp: MutableSet<List<Int>> = mutableSetOf()
        val possibleSquaresDown: MutableSet<List<Int>> = mutableSetOf()
        val possibleSquaresLeft: MutableSet<List<Int>> = mutableSetOf()
        val possibleSquaresRight: MutableSet<List<Int>> = mutableSetOf()
        val possibleSquaresXX: MutableSet<List<Int>> = mutableSetOf()
        val possibleSquaresXY: MutableSet<List<Int>> = mutableSetOf()
        val possibleSquaresYX: MutableSet<List<Int>> = mutableSetOf()
        val possibleSquaresYY: MutableSet<List<Int>> = mutableSetOf()
        val x = square.getX()
        val y = square.getY()
        val possibleSquares = mutableSetOf<List<Int>>()

        possibleMoves.forEach {
            if (square.getType()=='R' || square.getType()=='Q') {
                if (it[0] > 0 && (it[1]) == 0)      possibleSquaresUp.add(listOf(x + it[0], y))
                else if (it[0] < 0 && (it[1]) == 0) possibleSquaresDown.add(listOf(x + it[0], y))
                else if ((it[0]) == 0 && it[1] < 0) possibleSquaresLeft.add(listOf(x, y + it[1]))
                else if ((it[0]) == 0 && it[1] > 0) possibleSquaresRight.add(listOf(x, y + it[1]))
            }
            if (square.getType()=='B' || square.getType()=='Q'){
                if      (it[0]>0 && it[1]>0) possibleSquaresXX.add(listOf(x+it[0],y+it[1]))
                else if (it[0]<0 && it[1]<0) possibleSquaresYY.add(listOf(x+it[0],y+it[1]))
                else if (it[0]>0 && it[1]<0) possibleSquaresXY.add(listOf(x+it[0],y+it[1]))
                else if (it[0]<0 && it[1]>0) possibleSquaresYX.add(listOf(x+it[0],y+it[1]))
            }
        }

        possibleSquares.addAll(checkBlock(removeInvalidMoves(possibleSquaresUp)))
        possibleSquares.addAll(checkBlock(removeInvalidMoves(possibleSquaresDown)))
        possibleSquares.addAll(checkBlock(removeInvalidMoves(possibleSquaresLeft)))
        possibleSquares.addAll(checkBlock(removeInvalidMoves(possibleSquaresRight)))
        possibleSquares.addAll(checkBlock(removeInvalidMoves(possibleSquaresXX)))
        possibleSquares.addAll(checkBlock(removeInvalidMoves(possibleSquaresXY)))
        possibleSquares.addAll(checkBlock(removeInvalidMoves(possibleSquaresYX)))
        possibleSquares.addAll(checkBlock(removeInvalidMoves(possibleSquaresYY)))

        return removeInvalidMoves(possibleSquares)
    }

    private fun threatenedSquaresKnightKing(square: Square): MutableSet<Square>{
        val possibleMoves = moves.getMove(square.getType())
        val possibleSquares: MutableSet<List<Int>> = mutableSetOf()
        possibleMoves.forEach {
            possibleSquares.add(listOf(square.getX()+it[0],square.getY()+it[1]))
        }
        return removeInvalidMoves(possibleSquares)
    }

    fun getPieces(): MutableSet<Square>{
        val result = mutableSetOf<Square>()
        this.squares.forEach {
            it.forEach { it2 ->
                if (it2.hasPiece())
                    result.add(it2)
            }
        }
        return result
    }

    fun getWhitePieces(pieces: MutableSet<Square>): MutableSet<Square>{
        val result = mutableSetOf<Square>()
        pieces.forEach {
            if (it.getColor()=="w")
                result.add(it)
        }
        return result
    }

    fun getBlackPieces(pieces: MutableSet<Square>): MutableSet<Square>{
        val result = mutableSetOf<Square>()
        pieces.forEach {
            if (it.getColor()=="b")
                result.add(it)
        }
        return result
    }

    fun toASCII(): String{
        var string = String()

        for(i in 7 downTo 0){
            for(j in 0..7){
                string += if (this.squares[i][j].hasPiece()){
                    this.squares[i][j].getColor()+this.squares[i][j].piece!!.type
                } else {
                    "_ "
                }
            }
            if (i > 0) string += "\n"

        }
        string = mapToASCII(string)
        return string
    }

    fun basicMove(squareA: Square, squareB: Square){
        this.moveHistory.add(Move(squareA, squareB))
        squareB.putPiece(squareA.piece!!)
        squareB.piece!!.setHasMoved()
        squareA.emptySquare()
    }

    fun castleShort(color: Char){
        var rank = 0
        if (color == 'b') rank = 7
        this.squares[rank][6].putPiece(this.squares[rank][4].piece!!)
        this.squares[rank][5].putPiece(this.squares[rank][7].piece!!)
        this.squares[rank][6].piece!!.setHasMoved()
        this.squares[rank][5].piece!!.setHasMoved()
        this.squares[rank][4].emptySquare()
        this.squares[rank][7].emptySquare()
    }

    fun castleLong(color: Char){
        var rank = 0
        if (color == 'b') rank = 7
        this.squares[rank][2].putPiece(this.squares[rank][4].piece!!)
        this.squares[rank][3].putPiece(this.squares[rank][0].piece!!)
        this.squares[rank][2].piece!!.setHasMoved()
        this.squares[rank][3].piece!!.setHasMoved()
        this.squares[rank][4].emptySquare()
        this.squares[rank][0].emptySquare()
    }

    fun removeKingMovesCheck(moves: MutableSet<ArrayList<Square>>, otherPieces: MutableSet<Square>): MutableSet<ArrayList<Square>> {
        val result = arrayListOf<Square>()
        val threatenedSquares: MutableSet<Square> = mutableSetOf()
        otherPieces.forEach {square->
            threatenedSquares.addAll(threatenedSquares(square))
        }
        val nonEmptyThreatenedSquares: MutableSet<Square> = mutableSetOf()
        threatenedSquares.forEach {
            if (it.hasPiece()) nonEmptyThreatenedSquares.add(it)
        }
        threatenedSquares.removeAll(nonEmptyThreatenedSquares)

        var kingMoves = arrayListOf<Square>()
        moves.forEach {
            if (it[0].getType() == 'K') kingMoves = it
        }
        kingMoves.forEach{
            if (it in threatenedSquares && it.getType()!='K') result.add(it)
            if (kingMoves[0].getColor() == "w"){
                if(it.getX()<6){
                    if((it.getY()<7 && this.squares[it.getX()+1][it.getY()+1].getType()=='P' && this.squares[it.getX()+1][it.getY()+1].getColor()=="b")||
                        (it.getY()>0 && this.squares[it.getX()+1][it.getY()-1].getType()=='P'&& this.squares[it.getX()+1][it.getY()-1].getColor()=="b"))
                        result.add(it)
                }
            }
            else{
                if(it.getX()>1){
                    if((it.getY()<7 && this.squares[it.getX()-1][it.getY()+1].getType()=='P' && this.squares[it.getX()-1][it.getY()+1].getColor()=="w")||
                        (it.getY()>0 && this.squares[it.getX()-1][it.getY()-1].getType()=='P' && this.squares[it.getX()-1][it.getY()-1].getColor()=="w"))
                        result.add(it)
                }
            }
        }

        moves.remove(kingMoves)
        kingMoves.removeAll(result)
        moves.add(kingMoves)
        return removeEmptyMoves(moves)
    }

    fun removeEmptyMoves(moves: MutableSet<ArrayList<Square>>): MutableSet<ArrayList<Square>> {
        val result = mutableSetOf<ArrayList<Square>>()
        moves.forEach {
            if (it.size <= 1 || !it[0].hasPiece()) result.add(it)
        }
        moves.removeAll(result)
        return moves
    }

    fun promotion(squareFrom: Square, squareTo: Square, type: Char) {
        this.moveHistory.add(Move(squareFrom, squareTo))
        squareFrom.piece!!.type = type
        squareTo.putPiece(squareFrom.piece!!)
        squareFrom.emptySquare()
    }
}

class Square(var piece: Piece? = null, val positionX: Int, val positionY: Int){

    fun hasPiece(): Boolean {
        return this.piece != null
    }

    fun putPiece(piece: Piece){
        this.piece = piece
    }

    fun getColor(): String?{
        return this.piece?.getColor()
    }
    fun getType(): Char?{
        return this.piece?.type
    }

    fun getHasMoved(): Boolean?{
        return this.piece?.hasMoved
    }

    fun emptySquare(){
        this.piece = null
    }

    fun getX(): Int{
        return this.positionX
    }

    fun getY(): Int{
        return this.positionY
    }
}