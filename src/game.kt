val defaultSquare = Square(positionX = 42,positionY = 69)
val defaultMove   = Move(defaultSquare, defaultSquare)


class Game {
    val board = Board()

    fun startingPosition(){
        for (i in 0..7){
            val pawnW = Piece(true,'P')
            val pawnB = Piece(false, 'P')
            this.board.squares[1][i].putPiece(pawnW)
            this.board.squares[6][i].putPiece(pawnB)

        }

        for (i in 0..1){
            val rookW = Piece(true, 'R')
            val rookB = Piece(false,'R')
            this.board.squares[0][i*7].putPiece(rookW)
            this.board.squares[7][i*7].putPiece(rookB)
        }


        for (i in 1..6 step 5){
            val knightW = Piece(true, 'N')
            val knightB = Piece(false,'N')
            this.board.squares[0][i].putPiece(knightW)
            this.board.squares[7][i].putPiece(knightB)
        }

        for (i in 2..5 step 3){
            val bishopW = Piece(true,  'B')
            val bishopB = Piece(false, 'B')
            this.board.squares[0][i].putPiece(bishopW)
            this.board.squares[7][i].putPiece(bishopB)
        }

        val queenW = Piece(true, 'Q')
        val queenB = Piece(false,'Q')
        val kingW  = Piece(true, 'K')
        val kingB  = Piece(false,'K')

        this.board.squares[0][3].putPiece(queenW)
        this.board.squares[7][3].putPiece(queenB)
        this.board.squares[0][4].putPiece(kingW)
        this.board.squares[7][4].putPiece(kingB)
    }

    private val a0 = Input("move")
    private val a1 = Input("check")
    private val a2 = Input("checkmate")
    private val a3 = Input ("draw")
    private val s0 = State ("white Turn")
    private val s1 = State ("black Turn")
    private val s2 = State ("white Check")
    private val s3 = State ("black Check")
    private val s4 = State ( "checkmate")
    private val s5 = State ( "draw")

    val stateMachine = StateMachine(
        states = setOf(s0, s1, s2, s3, s4),
        inputs = setOf(a0, a1, a2),
        delta = { state: State, input: Input ->
            when(input) {
                a0 -> when (state) {
                    s0 -> s1
                    s1 -> s0
                    s2 -> s1
                    s3 -> s0
                    else -> state
                }
                a1 -> when (state) {
                    s0 -> s3
                    s1 -> s2
                    s2 -> s3
                    s3 -> s2
                    else -> state
                }
                a2 -> when (state) {
                    s0 -> s4
                    s1 -> s4
                    s2 -> s4
                    s3 -> s4
                    else -> state
                }
                a3 -> when (state) {
                    s0 -> s5
                    s1 -> s5
                    s2 -> s5
                    s3 -> s5
                    else -> state
                }
                else -> state
            }
        },
        initialState = s0,
        isFinalState = { state: State -> state in listOf(s4,s5) }
    )

    fun transition(dfa: StateMachine, input: MutableList<Input>): State {
        var state = dfa.initialState
        for (character in input) {
            state = dfa.delta(state, character)
        }
        return state
    }

    private fun parsePawnMove(input: String, moves: MutableSet<ArrayList<Square>>): Move{
        return try{
            var moveFrom = defaultSquare
            val y = input[0].toInt()-97
            val x = input[1].toInt()-49
            when (moves.first()[0].getColor()){
                    "w" -> if (x == 7) return defaultMove
                    "b" -> if (x == 0) return defaultMove
            }
            moves.forEach {
                if (it.contains(this.board.squares[x][y]) && it[0].getType()=='P') moveFrom = it[0]
            }
            Move(moveFrom, this.board.squares[x][y])
        } catch(e: StringIndexOutOfBoundsException){
            defaultMove
        }
    }

    private fun parse5Move(input: String, moves: MutableSet<ArrayList<Square>>): Move{
        try{
            var moveFrom = defaultSquare
            val y = input[3].toInt()-97
            val x = input[4].toInt()-49
            if(!this.board.squares[x][y].hasPiece()) {
                moves.forEach {
                    if (it[0].getType() == input[0] && it[0].getY() == input[1].toInt() - 97 && it[0].getX() == input[2].toInt() - 49) {
                        moveFrom = it[0]
                    }
                }
            }
            return Move(moveFrom, this.board.squares[x][y])
        }
        catch(e: StringIndexOutOfBoundsException){
            return defaultMove
        }
    }

    private fun parsePieceMove(input: String, moves: MutableSet<ArrayList<Square>>): Move{
        try{
            var moveFrom = defaultSquare
            val y = input[1].toInt()-97
            val x = input[2].toInt()-49
            var bool = false
            if(!this.board.squares[x][y].hasPiece()){
                moves.forEach {
                    if (it[0].getType()==input[0]) {
                        if (it.contains(this.board.squares[x][y])) {
                            moveFrom = it[0]

                            if (bool) {
                                moveFrom = defaultSquare
                            }
                            bool = true
                        }
                    }
                }
            }
            if (moveFrom.getType()=='K' && y-moveFrom.getY()>1) return parseCastlingShort(moves)
            else if (moveFrom.getType()=='K' && moveFrom.getY()-y>1) return parseCastlingLong(moves)
            return Move(moveFrom, this.board.squares[x][y])
        }
        catch(e: StringIndexOutOfBoundsException){
            return defaultMove
        }
    }

    private fun parse4Move(input: String, moves: MutableSet<ArrayList<Square>>): Move{
        try{
            var moveFrom = defaultSquare
            val y = input[2].toInt()-97
            val x = input[3].toInt()-49
            if(!this.board.squares[x][y].hasPiece()){
                moves.forEach {
                    if (it[0].getType()==input[0] && it[0].getY() == (input[1].toInt()-97)) {
                        moveFrom = it[0]
                    }
                }
            }
            return Move(moveFrom, this.board.squares[x][y])
        }
        catch(e: StringIndexOutOfBoundsException){
            return defaultMove
        }
    }

    private fun capture1(input: String, moves: MutableSet<ArrayList<Square>>): Move{
        try{
            var moveFrom = defaultSquare
            val y = input[2].toInt()-97
            val x = input[3].toInt()-49
            val special: String
            special = if (this.board.squares[x][y].hasPiece()) "x"
            else "e"
            var bool = false
            moves.forEach {
                if (it[0].getType()==input[0] || (it[0].getType()=='P' && ((it[0].getY()==input[0].toInt()-97) || special == "e"))) {
                    if (it.contains(this.board.squares[x][y])) {
                        moveFrom = it[0]

                        if (bool) {
                            moveFrom = defaultSquare
                        }
                        bool = true
                    }
                }
            }

            if(moveFrom.getType()=='P'){
                when (moves.first()[0].getColor()){
                    "w" -> if (x == 7) return defaultMove
                    "b" -> if (x == 0) return defaultMove
                }
            }
            return Move(moveFrom, this.board.squares[x][y], special)
        }
        catch(e: StringIndexOutOfBoundsException){
            return defaultMove
        }
    }

    fun enPassantWhite(move: Move){
        this.board.basicMove(move.squareFrom, move.squareTo)
        this.board.squares[move.squareTo.getX()-1][move.squareTo.getY()].emptySquare()
    }

    fun enPassantBlack(move: Move){
        this.board.basicMove(move.squareFrom, move.squareTo)
        this.board.squares[move.squareTo.getX()+1][move.squareTo.getY()].emptySquare()
    }

    private fun capture2(input: String, moves: MutableSet<ArrayList<Square>>): Move{
        var moveFrom = defaultSquare
        try{
            val y = input[3].toInt()-97
            val x = input[4].toInt()-49
            val special: String
            special = if (this.board.squares[x][y].hasPiece()) "x"
            else "e"
            moves.forEach {
                if (it[0].getType()==input[0] || (it[0].getType()=='P' && ((it[0].getY()==input[1].toInt()-97) || special == "e"))){
                    moveFrom = it[0]
                }
            }

            return Move(moveFrom, this.board.squares[x][y], special)
        }
        catch(e: StringIndexOutOfBoundsException){
            return defaultMove
        }

    }

    private fun capture3(input: String, moves: MutableSet<ArrayList<Square>>): Move{
        try{
            var moveFrom = defaultSquare
            val special = "x"
            val y = input[4].toInt()-97
            val x = input[5].toInt()-49
            moves.forEach {
                if (it[0].getType()==input[0] && it[0].getY()==input[1].toInt()-97 && it[0].getX()==input[2].toInt()-49){
                    moveFrom = it[0]
                }
            }
            return Move(moveFrom, this.board.squares[x][y], special)

        }
        catch(e: StringIndexOutOfBoundsException){
            return defaultMove
        }
    }

    private fun parseCapture(input: String, moves: MutableSet<ArrayList<Square>>): Move{
        return when {
            input [1] == 'x' -> capture1(input, moves)
            input [2] == 'x' -> capture2(input, moves)
            else             -> capture3(input, moves)
        }
    }

    private fun parseCastlingShort(moves: MutableSet<ArrayList<Square>>): Move{
        if(moves.first()[0].getColor() == "w") {
            moves.forEach {
                if (it[0].getType()=='K' && it.contains(this.board.squares[0][6]))
                    return Move(this.board.squares[0][4], this.board.squares[0][6],"s")
            }
        }
        else {
            moves.forEach {
                if (it[0].getType()=='K' && it.contains(this.board.squares[7][6]))
                    return Move(this.board.squares[7][4], this.board.squares[7][6],"s")
            }
        }
        return defaultMove
    }

    private fun parseCastlingLong(moves: MutableSet<ArrayList<Square>>): Move{
        if(moves.first()[0].getColor() == "w") {
            moves.forEach {
                if (it[0].getType()=='K' && it.contains(this.board.squares[0][2]))
                    return Move(this.board.squares[0][4], this.board.squares[0][2],"l")
            }
        }
        else{
            moves.forEach {
                if (it[0].getType()=='K' && it.contains(this.board.squares[7][2]))
                    return Move(this.board.squares[7][4], this.board.squares[7][2],"l")
            }
        }
        return defaultMove
    }

    fun parseMove(input: String, moves: MutableSet<ArrayList<Square>>): Move{

        return if (input.isNotEmpty())
            when {
                input[0] in 'a'..'h' && input[1] !='x'        -> parsePawnMove(input, moves)
                input.contains('=')                           -> parsePromotion(input, moves)
                input.contains('x')                           -> parseCapture(input, moves)
                input[1] in 'a'..'h' && input[2] !in 'a'..'h' -> parsePieceMove(input, moves)
                input.length==4                               -> parse4Move(input, moves)
                input == "O-O"                                -> parseCastlingShort(moves)
                input == "O-O-O"                              -> parseCastlingLong(moves)
                else                                          -> parse5Move(input, moves)
            }
        else defaultMove
    }

    private fun parsePromotion(input: String, moves: MutableSet<java.util.ArrayList<Square>>): Move {

        try{
            if (input.last()=='=') return defaultMove
            var moveFrom = defaultSquare
            val special = when{
                input.contains('x') -> "xp"+input.last()
                else                -> "p"+input.last()
            }
            val y = when{
                input.contains('x') -> input[2].toInt()-97
                else                -> input[0].toInt()-97
            }
            val x= when{
                input.contains('x') -> input[3].toInt()-49
                else                -> input[1].toInt()-49
            }
            if (special.contains('x')){
                moves.forEach {
                    if (it.contains(this.board.squares[x][y]) && it[0].getType()=='P' && it[0].getY() == input[0].toInt()-97 ) moveFrom = it[0]
                }
            }
            else{
                moves.forEach {
                    if (it.contains(this.board.squares[x][y]) && it[0].getType()=='P') moveFrom = it[0]
                }
            }
            return Move(moveFrom, this.board.squares[x][y], special)
        }
        catch(e: StringIndexOutOfBoundsException){
            return defaultMove
        }
    }

    fun executeMove(move: Move){
        this.board.basicMove(move.squareFrom, move.squareTo)
    }

    fun executePromotion(move: Move) {
        this.board.promotion(move.squareFrom, move.squareTo, move.special!!.last())
    }
}