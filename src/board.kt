import java.lang.IndexOutOfBoundsException
import java.lang.NullPointerException
import kotlin.math.absoluteValue

class Board {

    var squares = arrayListOf<ArrayList<Square>>()
    private val moveHistory = arrayListOf<Move>()
    private val moves = Moves()

    init {
        for (i in 0..7) {
            squares.add(arrayListOf())
            for (j in 0..7) {
                squares[i].add(Square(null, i, j))
            }
        }
    }

    // if King could move on a threatened square, remove it.
    fun removeKingMovesCheck(moves: MutableSet<ArrayList<Square>>, otherPieces: MutableSet<Square>): MutableSet<ArrayList<Square>> {
        val result = arrayListOf<Square>()
        val threatenedSquares: MutableSet<Square> = mutableSetOf()
        otherPieces.forEach { square ->
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
        kingMoves.forEach {
            if (it in threatenedSquares && it.getType() != 'K') result.add(it)
            if (kingMoves[0].getColor() == "w") {
                if (it.rank < 6) {
                    if ((it.column < 7 && this.squares[it.rank + 1][it.column + 1].getType() == 'P' && this.squares[it.rank + 1][it.column + 1].getColor() == "b") ||
                        (it.column > 0 && this.squares[it.rank + 1][it.column - 1].getType() == 'P' && this.squares[it.rank + 1][it.column - 1].getColor() == "b")
                    )
                        result.add(it)
                }
            } else {
                if (it.rank > 1) {
                    if ((it.column < 7 && this.squares[it.rank - 1][it.column + 1].getType() == 'P' && this.squares[it.rank - 1][it.column + 1].getColor() == "w") ||
                        (it.column > 0 && this.squares[it.rank - 1][it.column - 1].getType() == 'P' && this.squares[it.rank - 1][it.column - 1].getColor() == "w")
                    )
                        result.add(it)
                }
            }
        }

        moves.remove(kingMoves)
        kingMoves.removeAll(result)
        moves.add(kingMoves)
        return removeEmptyMoves(moves)
    }

    // possibleMoves returns squares with the first square with the piece which moves, therefore it's necessary to remove empty moves sometimes
    fun removeEmptyMoves(moves: MutableSet<ArrayList<Square>>): MutableSet<ArrayList<Square>> {
        val result = mutableSetOf<ArrayList<Square>>()
        moves.forEach {
            if (it.size <= 1 || !it[0].hasPiece()) result.add(it)
        }
        moves.removeAll(result)
        return moves
    }

    fun removePinnedMoves(possibleMoves: MutableSet<ArrayList<Square>>, otherMoves: MutableSet<ArrayList<Square>>): MutableSet<ArrayList<Square>> {
        val result = mutableSetOf<ArrayList<Square>>()
        possibleMoves.forEach {
            val pinningPiece = isPinned(it[0], otherMoves)
            if (pinningPiece.hasPiece()) {
                it.forEach { square ->
                    if (square != pinningPiece) result.add(it) // BUG: if square == pinningPiece at some point, still gets removed
                }
            }
        }

        possibleMoves.removeAll(result)
        return possibleMoves
    }

    private fun removeInvalidMoves(squares: MutableSet<List<Int>>): MutableSet<Square> {
        val result = mutableSetOf<Square>()
        squares.forEach {
            if (it[0] in 0..7 && it[1] in 0..7) {
                result.add(this.squares[it[0]][it[1]])
            }
        }
        return result
    }

    /*
*          |    |
*     lu   | us |   ru
*  ________|____|________
*     ls   | K  |   rs
*  ________|____|________
*          |    |
*     ld   | ds |  rd
*          |    |
*
* if moving onto square would pin the piece, return true
* this method is less complicated than isPinned, because we already know the square that is checking the King
 */
    private fun wouldPinItself(square: Square, squareThatChecks: Square): Boolean {
        val kingSquare = findKing(squareThatChecks.getInverseColor())
        val threatenedSquares = threatenedSquaresRBQ(squareThatChecks)
        val where: String = when {
            kingSquare.rank > squareThatChecks.rank -> when {
                kingSquare.column > squareThatChecks.column -> "ld"
                kingSquare.column < squareThatChecks.column -> "rd"
                else -> "ds"
            }
            kingSquare.rank < squareThatChecks.rank -> when {
                kingSquare.column > squareThatChecks.column -> "lu"
                kingSquare.column < squareThatChecks.column -> "ru"
                else -> "us"
            }
            else -> if (kingSquare.column < squareThatChecks.column) "rs"
            else "ls"
        }

        threatenedSquares.forEach {
            when (where) {
                "ld" -> if (squareThatChecks.rank < it.rank && squareThatChecks.column < it.column && square == it) return true
                "rd" -> if (squareThatChecks.rank < it.rank && squareThatChecks.column > it.column && square == it) return true
                "lu" -> if (squareThatChecks.rank > it.rank && squareThatChecks.column < it.column && square == it) return true
                "ru" -> if (squareThatChecks.rank > it.rank && squareThatChecks.column > it.column && square == it) return true
                "ds" -> if (it.column == kingSquare.column && it.rank > squareThatChecks.rank && square == it) return true
                "us" -> if (it.column == kingSquare.column && it.rank < squareThatChecks.rank && square == it) return true
                "rs" -> if (it.rank == kingSquare.rank && it.column < squareThatChecks.column && square == it) return true
                "ls" -> if (it.rank == kingSquare.rank && it.column > squareThatChecks.column && square == it) return true
            }
        }
        return false
    }

    private fun isPinned(square: Square, otherMoves: MutableSet<ArrayList<Square>>): Square {
        val kingSquare = findKing(square.getColor())
        val threateningSquares: MutableSet<Square> = mutableSetOf()
        if (square.getType() == 'K') return defaultSquare
        if (square.rank != kingSquare.rank && square.column != kingSquare.column &&
            ((square.rank - kingSquare.rank).absoluteValue != (square.column - kingSquare.column).absoluteValue))
            return defaultSquare
        removeEmptyMoves(otherMoves).forEach {
            try {
                if (it[0].getType()!! in "QBR") {
                    it.forEach { threatSquare ->
                        if (square == threatSquare) threateningSquares.add(it[0])
                    }
                }
            } catch (e: NullPointerException) {
                e.printStackTrace()
                return defaultSquare
            }
        }
        // when there are 2, returns defaultsquare -> not pinned! WHICH MIGHT BE WRONG
        threateningSquares.forEach {
            when {
                it.rank > square.rank -> {
                    when {
                        it.column < square.column -> {
                            val x = square.rank
                            val y = square.column
                            for (i in 1..(maxOf(x, y)..7).count()) {
                                try {
                                    if (this.squares[x - i][y + i].hasPiece()) {
                                        if (this.squares[x - i][y + i].getType() == 'K') return it
                                        return defaultSquare
                                    }
                                } catch (e: IndexOutOfBoundsException) { }
                            }
                        } // rd
                        it.column > square.column -> {
                            val x = square.rank
                            val y = square.column
                            for (i in 1..(maxOf(x, y)..7).count()) {
                                try {
                                    if (this.squares[x - i][y - i].hasPiece()) {
                                        if (this.squares[x - i][y - i].getType() == 'K') return it
                                        return defaultSquare
                                    }
                                } catch (e: IndexOutOfBoundsException) {}
                            }
                        } // ld
                        else -> {
                            for (i in square.rank - 1 downTo 0) {
                                if (this.squares[i][it.column].hasPiece()) {
                                    if (this.squares[i][it.column].getType() == 'K') return it
                                    return defaultSquare
                                }
                            }
                        }
                    }
                }
                it.rank < square.rank -> {
                    when {
                        it.column < square.column -> {
                            val x = square.rank
                            val y = square.column
                            for (i in 1..(maxOf(x, y)..7).count()) {
                                try {
                                    if (this.squares[x + i][y + i].hasPiece()) {
                                        if (this.squares[x + i][y + i].getType() == 'K') return it
                                        return defaultSquare
                                    }
                                } catch (e: IndexOutOfBoundsException) { }
                            }
                        } // ru
                        it.column > square.column -> {
                            val x = square.rank
                            val y = square.column
                            for (i in 1..(maxOf(x, y)..7).count()) {
                                try {
                                    if (this.squares[x + i][y - i].hasPiece()) {
                                        if (this.squares[x + i][y - i].getType() == 'K') return it
                                        return defaultSquare
                                    }
                                } catch (e: IndexOutOfBoundsException) { }
                            }
                        } // lu
                        else -> {
                            for (i in square.rank + 1..7) {
                                if (this.squares[i][it.column].hasPiece()) {
                                    if (this.squares[i][it.column].getType() == 'K') return it
                                    return defaultSquare
                                }
                            }
                        }
                    }
                }

                else -> {
                    if (it.column < square.column) {
                        for (i in square.column + 1..7) {
                            if (this.squares[it.rank][i].hasPiece()) {
                                if (this.squares[it.rank][i].getType() == 'K') return it
                                return defaultSquare
                            }
                        }
                    } else {
                        for (i in square.column - 1 downTo 0) {
                            if (this.squares[it.rank][i].hasPiece()) {
                                if (this.squares[it.rank][i].getType() == 'K') return it
                                return defaultSquare
                            }
                        }
                    }
                }
            }
        }

        return defaultSquare
    }

    // returns all possible moves when in check
    fun possibleMovesCheck(moves: MutableSet<ArrayList<Square>>, otherMoves: MutableSet<ArrayList<Square>>): MutableSet<ArrayList<Square>> {
        val result = mutableSetOf<ArrayList<Square>>()
        var threats = 0
        val king = findKing(moves.first()[0].getColor())
        val checkSquares = mutableSetOf<Square>()
        val threatenedSquares = mutableSetOf<Square>()
        // find out if 1 or 2 threats
        // if 1 = can kill/block threat IF NOT PINNED
        // if 2+ = only king can move
        otherMoves.forEach {
            if (it.contains(king)) {
                threats++
                checkSquares.add(it[0])
            }
            threatenedSquares.addAll(threatenedSquares(it[0]))
        }
        if (threats > 1) {
            moves.forEach {
                if (it[0].getType() == 'K') {
                    if (!threatenedSquares.contains(it[1])) result.add(it)
                }
            }
        } else {
            moves.forEach {
                when (it[0].getType()) {
                    'K' -> if (!threatenedSquares.contains(it[1])) result.add(it)
                    else -> {
                        if (!isPinned(it[0], otherMoves).hasPiece()) {
                            checkSquares.forEach { checkSquare ->
                                when (checkSquare.getType()) {
                                    'P', 'N' -> {
                                        it.forEach { square ->
                                            if (square != it[0] && square == checkSquare) result.add(
                                                arrayListOf(it[0], square))
                                        }
                                    }
                                    'Q', 'B', 'R' -> {
                                        it.forEach { square ->
                                            if (square != it[0] && ((square == checkSquare) || (wouldPinItself(square, checkSquares.first())))) result.add(arrayListOf(it[0], square))
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

    // returns all possible moves
    fun possibleMovesSquare(square: Square): ArrayList<Square> {
        val result = arrayListOf(square)
        result.addAll(possibleMoves(square))
        return result
    }

    private fun possibleMoves(square: Square): MutableSet<Square> {
        val result = mutableSetOf<Square>()
        if (square.getType() == 'P') {
            if (square.getColor() == "w" && !this.squares[square.rank + 1][square.column].hasPiece()) {

                if (!square.getHasMoved()!! && !this.squares[square.rank + 2][square.column].hasPiece())
                    result.add(this.squares[square.rank + 2][square.column])
                result.add(this.squares[square.rank + 1][square.column])

            } else if (square.getColor() == "b" && !this.squares[square.rank - 1][square.column].hasPiece()) {
                if (!square.getHasMoved()!! && !this.squares[square.rank - 2][square.column].hasPiece())
                    result.add(this.squares[square.rank - 2][square.column])
                result.add(this.squares[square.rank - 1][square.column])
            }

        }
        if (square.getType() == 'K' && !square.getHasMoved()!!) {
            when {
                checkCastlingShort(square) -> {
                    if (square.getColor() == "w") result.add(this.squares[0][6])
                    else result.add(this.squares[7][6])
                }
                checkCastlingLong(square) -> {
                    if (square.getColor() == "w") result.add(this.squares[0][2])
                    else result.add(this.squares[7][2])
                }
            }
        }
        val threatenedSquares = threatenedSquares(square)
        threatenedSquares.forEach {
            if (it.getColor() != square.getColor()) result.add(it)
        }
        return result
    }

    fun defendedThreatenedSquares(square: Square): ArrayList<Set<Square>> {
        val color = square.getColor()
        val defSquares = mutableSetOf<Square>()
        val threatSquares = mutableSetOf<Square>()
        val threatenedSquares = threatenedSquares(square)
        threatenedSquares.forEach {
            if (it.getColor().equals(color)) defSquares.add(it)
            else threatSquares.add(it)
        }
        return arrayListOf(defSquares, threatSquares)
    }

    // returns all squares which are threatened by the piece on square
    private fun threatenedSquares(square: Square): Set<Square> {
        return when (square.getType()) {
            'P' -> threatenedSquaresPawn(square)
            'K', 'N' -> threatenedSquaresKnightKing(square)
            'B', 'R', 'Q' -> threatenedSquaresRBQ(square)
            else -> setOf()
        }
    }

    private fun checkCastlingShort(square: Square): Boolean {
        val threatenedSquares: MutableSet<Set<Square>> = mutableSetOf()
        val pieces: MutableSet<Square>
        try {
            if (square.getColor() == "w") {
                pieces = getBlackPieces(getPieces())
                pieces.forEach {
                    threatenedSquares.add(threatenedSquares(it))
                }
                if (!this.squares[0][7].getHasMoved()!! && !this.squares[0][5].hasPiece() && !this.squares[0][6].hasPiece()) {
                    threatenedSquares.forEach {
                        if (it.contains(this.squares[0][5]) || it.contains(this.squares[0][6]) || it.contains(this.squares[0][4])) return false
                    }
                    return true
                }
            } else {
                pieces = getWhitePieces(getPieces())
                pieces.forEach {
                    threatenedSquares.add(threatenedSquares(it))
                }
                if (!this.squares[7][7].getHasMoved()!! && !this.squares[7][5].hasPiece() && !this.squares[7][6].hasPiece()) {
                    threatenedSquares.forEach {
                        if (it.contains(this.squares[7][5]) || it.contains(this.squares[7][6]) || it.contains(this.squares[7][4])) return false
                    }
                    return true
                }
            }
        } catch (e: NullPointerException) {
            return false
        }
        return false
    }

    private fun checkCastlingLong(square: Square): Boolean {
        val threatenedSquares: MutableSet<Set<Square>> = mutableSetOf()
        val pieces: MutableSet<Square>
        try {
            if (square.getColor() == "w") {
                pieces = getBlackPieces(getPieces())
                pieces.forEach {
                    threatenedSquares.add(threatenedSquares(it))
                }
                if (!this.squares[0][0].getHasMoved()!! && !this.squares[0][3].hasPiece() && !this.squares[0][2].hasPiece() && !this.squares[0][1].hasPiece()) {
                    threatenedSquares.forEach {
                        if (it.contains(this.squares[0][4]) || it.contains(this.squares[0][3]) || it.contains(this.squares[0][2]) || it.contains(
                                this.squares[0][1]
                            )
                        ) return false
                    }
                    return true
                }
            } else {
                pieces = getWhitePieces(getPieces())
                pieces.forEach {
                    threatenedSquares.add(threatenedSquares(it))
                }
                if (!this.squares[7][0].getHasMoved()!! && !this.squares[7][3].hasPiece() && !this.squares[7][2].hasPiece() && !this.squares[7][1].hasPiece()) {
                    threatenedSquares.forEach {
                        if (it.contains(this.squares[7][4]) || it.contains(this.squares[7][3]) || it.contains(this.squares[7][2]) || it.contains(
                                this.squares[7][1]
                            )
                        ) return false
                    }
                    return true
                }

            }
            return false
        } catch (e: NullPointerException) {
            return false
        }
    }

    private fun checkEnPassanteLeft(square: Square, lastMove: Move): Boolean {
        return if (square.getColor() == "w") {
            lastMove.squareTo.getType() == 'P' && lastMove.squareFrom.rank == square.rank + 2 &&
                    lastMove.squareFrom.column == square.column - 1 && lastMove.squareTo.column == square.column - 1 &&
                    lastMove.squareTo.rank == square.rank
        } else {
            lastMove.squareTo.getType() == 'P' && lastMove.squareFrom.rank == square.rank - 2 &&
                    lastMove.squareFrom.column == square.column - 1 && lastMove.squareTo.column == square.column - 1 &&
                    lastMove.squareTo.rank == square.rank
        }
    }

    private fun checkEnPassanteRight(square: Square, lastMove: Move): Boolean {
        return if (square.getColor() == "w") {
            lastMove.squareTo.getType() == 'P' && lastMove.squareFrom.rank == square.rank + 2 &&
                    lastMove.squareFrom.column == square.column + 1 && lastMove.squareTo.column == square.column + 1 &&
                    lastMove.squareTo.rank == square.rank
        } else {
            lastMove.squareTo.getType() == 'P' && lastMove.squareFrom.rank == square.rank - 2 &&
                    lastMove.squareFrom.column == square.column + 1 && lastMove.squareTo.column == square.column + 1 &&
                    lastMove.squareTo.rank == square.rank
        }
    }

    private fun checkBlock(squares: MutableSet<Square>): MutableSet<List<Int>> {
        val result = mutableSetOf<List<Int>>()
        squares.forEach {
            result.add(listOf(it.rank, it.column))
            if (this.squares[it.rank][it.column].hasPiece()) return result
        }
        return result
    }

    private fun threatenedSquaresPawn(square: Square): MutableSet<Square> {
        var lastMove = defaultMove
        val result = mutableSetOf<Square>()
        if (this.moveHistory.isNotEmpty()) {
            lastMove = this.moveHistory.last()
        }

        if (square.getColor() == "w") {
            if (square.column > 0 && (this.squares[square.rank + 1][square.column - 1].hasPiece() || checkEnPassanteLeft(
                    square,
                    lastMove
                ))
            )
                result.add(this.squares[square.rank + 1][square.column - 1])
            if (square.column < 7 && (this.squares[square.rank + 1][square.column + 1].hasPiece() || checkEnPassanteRight(
                    square,
                    lastMove
                ))
            )
                result.add(this.squares[square.rank + 1][square.column + 1])
        } else {
            if (square.column > 0 && (this.squares[square.rank - 1][square.column - 1].hasPiece() || checkEnPassanteLeft(
                    square,
                    lastMove
                ))
            )
                result.add(this.squares[square.rank - 1][square.column - 1])
            if (square.column < 7 && (this.squares[square.rank - 1][square.column + 1].hasPiece() || checkEnPassanteRight(
                    square,
                    lastMove
                ))
            )
                result.add(this.squares[square.rank - 1][square.column + 1])
        }

        return result
    }

    private fun threatenedSquaresRBQ(square: Square): MutableSet<Square> {
        val possibleMoves = moves.getMove(square.getType())
        val possibleSquaresUp: MutableSet<List<Int>> = mutableSetOf()
        val possibleSquaresDown: MutableSet<List<Int>> = mutableSetOf()
        val possibleSquaresLeft: MutableSet<List<Int>> = mutableSetOf()
        val possibleSquaresRight: MutableSet<List<Int>> = mutableSetOf()
        val possibleSquaresXX: MutableSet<List<Int>> = mutableSetOf()
        val possibleSquaresXY: MutableSet<List<Int>> = mutableSetOf()
        val possibleSquaresYX: MutableSet<List<Int>> = mutableSetOf()
        val possibleSquaresYY: MutableSet<List<Int>> = mutableSetOf()
        val x = square.rank
        val y = square.column
        val possibleSquares = mutableSetOf<List<Int>>()

        possibleMoves.forEach {
            if (square.getType() == 'R' || square.getType() == 'Q') {
                if (it[0] > 0 && (it[1]) == 0) possibleSquaresUp.add(listOf(x + it[0], y))
                else if (it[0] < 0 && (it[1]) == 0) possibleSquaresDown.add(listOf(x + it[0], y))
                else if ((it[0]) == 0 && it[1] < 0) possibleSquaresLeft.add(listOf(x, y + it[1]))
                else if ((it[0]) == 0 && it[1] > 0) possibleSquaresRight.add(listOf(x, y + it[1]))
            }
            if (square.getType() == 'B' || square.getType() == 'Q') {
                if (it[0] > 0 && it[1] > 0) possibleSquaresXX.add(listOf(x + it[0], y + it[1]))
                else if (it[0] < 0 && it[1] < 0) possibleSquaresYY.add(listOf(x + it[0], y + it[1]))
                else if (it[0] > 0 && it[1] < 0) possibleSquaresXY.add(listOf(x + it[0], y + it[1]))
                else if (it[0] < 0 && it[1] > 0) possibleSquaresYX.add(listOf(x + it[0], y + it[1]))
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

    private fun threatenedSquaresKnightKing(square: Square): MutableSet<Square> {
        val possibleMoves = moves.getMove(square.getType())
        val possibleSquares: MutableSet<List<Int>> = mutableSetOf()
        possibleMoves.forEach {
            possibleSquares.add(listOf(square.rank + it[0], square.column + it[1]))
        }
        return removeInvalidMoves(possibleSquares)
    }

    private fun findKing(color: String?): Square {
        val pieces = getPieces()
        pieces.forEach {
            if (it.getType() == 'K' && it.getColor() == color) return it
        }
        return defaultSquare
    }

    fun getPieces(): MutableSet<Square> {
        val result = mutableSetOf<Square>()
        this.squares.forEach {
            it.forEach { it2 ->
                if (it2.hasPiece())
                    result.add(it2)
            }
        }
        return result
    }

    fun getWhitePieces(pieces: MutableSet<Square>): MutableSet<Square> {
        val result = mutableSetOf<Square>()
        pieces.forEach {
            if (it.getColor() == "w")
                result.add(it)
        }
        return result
    }

    fun getBlackPieces(pieces: MutableSet<Square>): MutableSet<Square> {
        val result = mutableSetOf<Square>()
        pieces.forEach {
            if (it.getColor() == "b")
                result.add(it)
        }
        return result
    }

    fun toASCII(): String {
        var string = String()

        for (i in 7 downTo 0) {
            for (j in 0..7) {
                string += if (this.squares[i][j].hasPiece()) {
                    this.squares[i][j].getColor() + this.squares[i][j].getType()
                } else {
                    "_ "
                }
            }
            if (i > 0) string += "\n"

        }
        string = mapToASCII(string)
        return string
    }

    fun basicMove(squareA: Square, squareB: Square) {
        this.moveHistory.add(Move(squareA, squareB))
        squareB.piece = squareA.piece
        squareB.piece!!.hasMoved = true
        squareA.emptySquare()
    }

    fun castleShort(color: Char) {
        var rank = 0
        if (color == 'b') rank = 7
        this.squares[rank][6].piece = this.squares[rank][4].piece
        this.squares[rank][5].piece = this.squares[rank][7].piece
        this.squares[rank][6].piece!!.hasMoved = true
        this.squares[rank][5].piece!!.hasMoved = true
        this.squares[rank][4].emptySquare()
        this.squares[rank][7].emptySquare()
    }

    fun castleLong(color: Char) {
        var rank = 0
        if (color == 'b') rank = 7
        this.squares[rank][2].piece = this.squares[rank][4].piece
        this.squares[rank][3].piece = this.squares[rank][0].piece
        this.squares[rank][2].piece!!.hasMoved = true
        this.squares[rank][3].piece!!.hasMoved = true
        this.squares[rank][4].emptySquare()
        this.squares[rank][0].emptySquare()
    }

    fun promotion(squareFrom: Square, squareTo: Square, type: Char) {
        this.moveHistory.add(Move(squareFrom, squareTo))
        squareFrom.piece!!.type = type
        squareTo.piece = squareFrom.piece
        squareFrom.emptySquare()
    }
}
