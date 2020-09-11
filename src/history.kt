class Turn(val turn: Int, val player: Char, squares: ArrayList<ArrayList<Square>>, moves: MutableSet<ArrayList<Square>>, val move: Move, val input: String)

class History(val turnHistory: ArrayList<Turn>){

    override fun toString(): String {
        var i = 0
        var result =""
        this.turnHistory.forEach {

            if(it.turn % 2 !=0){ i++
                result += "$i. "
            }
            result += it.input+" "
        }
        return result
    }
}
