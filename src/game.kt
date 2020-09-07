class Game(){
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
    private val s0 = State ("white Turn")
    private val s1 = State ("black Turn")
    private val s2 = State ("white Check")
    private val s3 = State ("black Check")
    private val s4 = State ( "checkmate")

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
                else -> state
            }
        },
        initialState = s0,
        isFinalState = { state: State -> state == s4 }
    )

    fun transition(dfa: StateMachine, input: MutableList<Input>): State {
        var state = dfa.initialState
        for (character in input) {
            state = dfa.delta(state, character)
        }
        return state
    }



    fun parseMove(input: String, moves: MutableSet<ArrayList<Square>>): Move{
        var moveFrom = Square(positionX = 10,positionY = 10)
        var y = 0
        var x = 0
        var special:String? = null
        var bool = false
        if (input.isNotEmpty())
            when {
                input[0] in 'a'..'h' && input[1] !='x' -> {
                    y = input[0].toInt()-97
                    x = input[1].toInt()-49
                    moves.forEach {
                        if (it.contains(this.board.squares[x][y]) && it[0].getType()=='P') moveFrom = it[0]
                    }
                }
                input.contains('x') -> {
                    special = "capture "
                    when {

                        input [1] == 'x' ->{
                            y = input[2].toInt()-97
                            x = input[3].toInt()-49
                        }
                        input [2] == 'x' ->{
                            y = input[3].toInt()-97
                            x = input[4].toInt()-49
                        }
                        input [3] == 'x' ->{
                            y = input[4].toInt()-97
                            x = input[5].toInt()-49
                        }
                    }
                }
                input[1] in 'a'..'h' && input[2] !in 'a'..'h' -> {
                    y = input[1].toInt()-97
                    x = input[2].toInt()-49
                    moves.forEach {
                        if (it[0].getType()==input[0]) {
                            if (it.contains(this.board.squares[x][y])) {
                                moveFrom = it[0]

                                if (bool) {
                                    moveFrom = Square(positionX = 10,positionY = 10)
                                }
                                bool = true
                            }
                        }
                    }
                }
                else -> {
                    y = input[2].toInt()-97
                    x = input[3].toInt()-49
                    moves.forEach {
                        if (it[0].getType()==input[0] && it[0].getY() == (input[1].toInt()-49)) {
                                moveFrom = it[0]
                        }
                    }
                }
            }

        return Move(moveFrom, this.board.squares[x][y], special)
    }
    fun executeMove(move: Move){
        this.board.basicMove(move.squareFrom, move.squareTo)
    }
}