package entity

/**
 * An actual player, subclass of [AbstractPlayer].
 */
class Player(
                name: String,
                color: PlayerColor,
                numberOfToken: Int = 21,
                totalCost: Int,
                tileGrid: Grid
                ) :AbstractPlayer(name, color,numberOfToken,totalCost,false, false, tileGrid) {

    /**
     * Returns deep copy of this object.
     */
    fun deepCopy(): Player {
        val newPlayer = Player(this.name, this.color, this.numberOfTokens, this.totalCost, this.tileGrid.deepCopy())
        newPlayer.hintUsed = this.hintUsed
        newPlayer.wantToFillTiles = this.wantToFillTiles
        return newPlayer
    }
}