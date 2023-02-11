package entity
/**
 * Entity class that represents a game state of "Swim" . Has a [listOfPlayers] as well the
 * [middleCards] and the [drawStack] of the Game.
 */
 class Game (
    val listOfPlayers: ArrayList<Player>
) {
    val drawStack = CardManager()

    var middleCards = ArrayList<Card>(3)

    var currPlayerIndex = 0

    /**
     * sets the currentPlayer [currPlayerIndex] to the nextPlayer in the [listOfPlayers]
     * the [currPlayerIndex] shouldn't be smaller or bigger than the number of Players
     */
    fun nextPlayer() {
       if(currPlayerIndex >= listOfPlayers.size-1){
           currPlayerIndex=0
       }else {
           currPlayerIndex++
       }
    }
    /**
     * gets and returns the current Player on [currPlayerIndex]
     * @return the Player object which represents the current Player
     */
    fun getCurrentPlayer(): Player {
        return listOfPlayers[currPlayerIndex]
    }
}