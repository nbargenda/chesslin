
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

    private val a0 = Input("Move")
    private val a1 = Input("Check")
    private val a2 = Input("Checkmate")
    private val s0 = State ("Whites Turn")
    private val s1 = State ("Blacks Turn")
    private val s2 = State ("White Check")
    private val s3 = State ("Black Check")
    private val s4 = State ( "Checkmate")
    val stateMachine = StateMachine(
        states = setOf(s0, s1, s2, s3, s4),
        inputs = setOf(a0, a1, a2),
        delta = { state: State, input: Input ->
            when(input) {
                a0 -> when (state) {
                    s0 -> s1
                    s1 -> s0
                    else -> state
                }
                else -> state
            }
        },
        initialState = s0,
        isFinalState = { state: State -> state == s4 }
    )

}