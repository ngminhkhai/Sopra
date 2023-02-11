package entity

import java.io.Serializable
import java.util.*

/**
 * Game Object to hold the current state of a game.
 * @param moonPos Position of the meeple.
 * @param nMiddleTiles Number of Tiles in [middleTiles]. 12 field but at least one field always null max 11 Cards
 * @param drawStack [Deque] of Tiles.
 * @param listOfPlayers Holds the actors of the game, [AbstractPlayer]s.
 * @param nextState Reference to the next state of the game. Default is null,
 *      as the next State will probably not be determined when this object is initialized.
 * @param prevState Reference to the previous state of the game. Default is null,
 *      because this might be the first state of the game.
 */
data class Game(var moonPos: Int = 0, var nMiddleTiles: Int, var currPlayer: AbstractPlayer,
                var nextState: Game? = null, var prevState : Game?,
                var middleTiles : Array<Tile?>,
                var drawStack : MutableList<Tile>,
                var listOfPlayers : List<AbstractPlayer>,
                var listOfLastPayersColor: MutableList<PlayerColor> = mutableListOf()
           ): Serializable {

    //Variable for redo function
    var redoPossible = false

    /**
     * Sets the [currPlayer] to the player with the smallest [Player.totalCost]
     */
    fun nextPlayer() {
        // Allways store the color of the last player in the list of the 10 last commands of next Player
        // Letzter Spieler in der Liste ist immer der aktuellste gewesen

        //der fall wenn noch nicht jeder spieler dran war
        if(listOfLastPayersColor.size < listOfPlayers.size -1 ){
            listOfLastPayersColor.add(currPlayer.color)
            currPlayer = listOfPlayers[listOfPlayers.indexOf(currPlayer)+1]
        }
        else if ( listOfLastPayersColor.size >= listOfPlayers.size -1 && listOfLastPayersColor.size < 10) {
            listOfLastPayersColor.add(currPlayer.color)
        }else{
            val list2 = listOfLastPayersColor
            for(i in listOfLastPayersColor.size.. 1){
                listOfLastPayersColor[i-1] = list2[i]
            }
            listOfLastPayersColor[9] = currPlayer.color
        }
        //wenigsten totalcost suchen
        val leastTotalCost = listOfPlayers.sortedBy { it.totalCost }[0].totalCost
        //alle spieler mit den wenigsten totalcost suchen und speichern
        val playerWithLeastTotalCost = listOfPlayers.filter { it.totalCost == leastTotalCost }
        var foundNextPlayer = false
        // in der history den ersten spieler aus playerWithLeastTotalCost suchen
        for(color in listOfLastPayersColor.reversed()) {
            if(!foundNextPlayer){
                val playerWithColor = playerWithLeastTotalCost.filter { it.color == color }
                if(playerWithColor.isNotEmpty()) {
                    check(playerWithColor.size == 1){"Players with nondistinct colors are forbidden!"}

                    currPlayer = playerWithColor[0]
                    foundNextPlayer = true
                }
            }
        }
    }

    /**
     * calculates the neighbour tiles (at most four) of one tile on the grid of the given player
     * @param player is the owner of the given tile
     * @param tile is the tile of interest
     */
    fun getNeighbourTiles(player: AbstractPlayer, tile: Tile): List<Tile> {
        val xPos = tile.xPos
        val yPos = tile.yPos
        val grid = player.tileGrid
        val neighbours = mutableListOf<Tile>()

        // Unsicher über Zeile und Spalten des Grids

        // check and add neighbours
        if (xPos >= 1) {
            val n1 = grid.tiles[xPos-1][yPos]
            if (n1 != null) {
                neighbours.add(n1)
            }

        }
        if (xPos <= 7) {
            val n2 = grid.tiles[xPos+1][yPos]
            if (n2 != null){
                neighbours.add(n2)
            }

        }
        if (yPos >= 1) {
            val n3 = grid.tiles[xPos][yPos-1]
            if (n3 != null){
                neighbours.add(n3)
            }

        }
        if (yPos <= 7) {
            val n4 = grid.tiles[xPos][yPos+1]
            if (n4 != null){
                neighbours.add(n4)
            }

        }

        return neighbours
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Game

        if (moonPos != other.moonPos) return false
        if (nMiddleTiles != other.nMiddleTiles) return false
        if (currPlayer != other.currPlayer) return false
        if (nextState != other.nextState) return false
        if (prevState != other.prevState) return false
        if (!middleTiles.contentEquals(other.middleTiles)) return false
        if (drawStack != other.drawStack) return false
        if (listOfPlayers != other.listOfPlayers) return false
        if (listOfLastPayersColor != other.listOfLastPayersColor) return false
        if (redoPossible != other.redoPossible) return false

        return true
    }

    override fun hashCode(): Int {
        var result = moonPos
        result = 31 * result + nMiddleTiles
        result = 31 * result + currPlayer.hashCode()
        result = 31 * result + (nextState?.hashCode() ?: 0)
        result = 31 * result + (prevState?.hashCode() ?: 0)
        result = 31 * result + middleTiles.contentHashCode()
        result = 31 * result + drawStack.hashCode()
        result = 31 * result + listOfPlayers.hashCode()
        result = 31 * result + listOfLastPayersColor.hashCode()
        result = 31 * result + redoPossible.hashCode()
        return result
    }

    /**
     * Returns deep copy of this object.
     */
    fun deepCopy(): Game{
        //new Player list
        val newPlayerList = mutableListOf<AbstractPlayer>()
        this.listOfPlayers.forEach {
            if(it is Player){
                newPlayerList.add(it.deepCopy())
            }else if(it is AI){
                newPlayerList.add(it.deepCopy())
            }
        }
        //update currentPlayer
        var newCurrPlayer = newPlayerList[0]
        newPlayerList.forEach { if(it.color == this.currPlayer.color) newCurrPlayer = it }
        //new draw stack
        val newDrawStack = mutableListOf<Tile>()
        this.drawStack.forEach { newDrawStack.add(it.deepCopy()) }
        //new player color list
        val newColorList = mutableListOf<PlayerColor>()
        this.listOfLastPayersColor.forEach { newColorList.add(it) }
        val newGame = Game(this.moonPos, this.nMiddleTiles, newCurrPlayer, this.nextState, this.prevState,
            Array(12) {null} ,newDrawStack, newPlayerList, newColorList)
        newGame.redoPossible = this.redoPossible
        //update middle tiles
        for(i in this.middleTiles.indices){
            val tile = this.middleTiles[i]
            if(tile != null){
                newGame.middleTiles[i] = tile.deepCopy()
            }
        }

        return newGame
    }
}

