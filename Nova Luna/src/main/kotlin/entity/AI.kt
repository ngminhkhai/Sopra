package entity

/**
 * AI Player, who is a Subclass of [AbstractPlayer].
 * @param difficulty Skill-Level of the AI Object
 */
class AI(val difficulty: Difficulty,
         name: String,
         color: PlayerColor,
         numberOfToken:Int = 21,
         totalCost: Int,
         tileGrid: Grid) : AbstractPlayer(name, color,numberOfToken,totalCost,false, false, tileGrid) {

    /**
     * Returns deep copy of this object.
     */
    fun deepCopy(): AI {
        val newAI = AI(this.difficulty, this.name, this.color, this.numberOfTokens, this.totalCost, this.tileGrid.deepCopy())
        newAI.hintUsed = this.hintUsed
        newAI.wantToFillTiles = this.wantToFillTiles
        return newAI
    }
}