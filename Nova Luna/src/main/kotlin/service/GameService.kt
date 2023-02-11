package service

import entity.*
import java.io.Serializable

/**
 * Game service
 * @property startNewGame starts a new game
 * @property showHighscores activates the refreshable HighScore
 * @property moveMeeple moves the meeple with the Costs
 * @property startNextTurn starts the next round with the new Player
 * @property calculateWinner returns the order of player gouped by remaining chips
 */
class GameService (private val rootService: RootService):AbstractRefreshingService(),Serializable{
    /**
     * Start a new game
     * @param players List of players
     * @param pathCsv Csv File of the file
     * @param firstGame if true the correct number of tokens is distributed
     * @param randomOrderCards if all cards are shuffled and distributed to the middleTiles and draw stack
     * @param randomOrderPlayer player order is coincidentally
     */
    fun startNewGame(players: List<AbstractPlayer>, pathCsv:String, firstGame:Boolean = true,
                     randomOrderCards: Boolean = false, randomOrderPlayer: Boolean = false){
        //players with equal colors not allowed
        for (player in players) {
            if (players.filter { player.color == it.color }.size > 1) throw IllegalArgumentException("Only players with different colors allowed!")
        }
        // with reference to the number of Player an amount of cards is distributed
        if (firstGame){
            when (players.size){
                2 -> players.forEach{ it.numberOfTokens = 20 }
                3 -> players.forEach{ it.numberOfTokens = 17 }
                4 -> players.forEach{ it.numberOfTokens = 15 }
            }
        }else{
            players.forEach{ it.numberOfTokens = 20}
        }
        // cost of all players are at first all 0
        players.forEach{ it.totalCost = 0}
        // Load Csv
        var (middleTiles, drawStack) = rootService.ioService.loadCsvTile(pathCsv)
        // If random Order is choosen shuffle the Cards on the
        if (randomOrderCards){
            val(middleTilesNeu, drawStackNeu) = shuffleAllCards(middleTiles, drawStack)
            middleTiles = middleTilesNeu
            drawStack = drawStackNeu
        }
        //choose randomly the current player
        var playerList = players
        if (randomOrderPlayer){
            playerList = players.shuffled()
        }

        rootService.novaLunaApplication.currentGame = Game( 0, middleTiles.filterNotNull().size, playerList[0], null, null, middleTiles, drawStack, playerList)

        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        println("\nIt is " + game.currPlayer.name + "s turn.")

        onAllRefreshables { refreshAfterStartGame() }
    }

    /**
     * Shuffles all cards when a random order is wanted
     * @param middleTiles 11 tiles around the board and 1 Position of meeple
     * @param drawStack List which is used to draw cards
     */
    private fun shuffleAllCards(middleTiles : Array<Tile?>, drawStack : MutableList<Tile>): Pair<Array<Tile?>, MutableList<Tile>> {
        val allCards = mutableListOf<Tile>()
        drawStack.forEach{allCards.add(it)}
        // i = 0 is always null because it's the first position of meeple
        for(i in 1 until middleTiles.size){
            allCards.add(middleTiles[i]!!)
        }

        // All cards are shuffled
        allCards.shuffle()
        for(i in 1 until  middleTiles.size){
            middleTiles[i] = allCards[i-1]
        }
        drawStack.clear()
        for (i in middleTiles.size until allCards.size){
            drawStack.add(allCards[i])
        }
        return Pair(middleTiles, drawStack)
    }

    /**
     * this method gets triggered when someone presses the "show highscore" button in the menu. the
     * current highscores are in the rootService.highscore.scoreboard, so it only needs to refresh
     */
    private fun showHighscores(){
        onAllRefreshables { refreshAfterHighScore() }
    }

    /**
     * Moves the Meeple to the new Positon
     * possible Positons are 0 - 11
     * @param newPosition moves the meeple to the position
     */
    fun moveMeeple(newPosition: Int){
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        game.moonPos = newPosition
    }

    /**
     * starts a next turn, if there are enough tiles left
     * otherwise ends the game
     */
    fun startNextTurn(){
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game) { "No started game found." }

        //for redo & undo function
        game.redoPossible = true
        if (game.nMiddleTiles == 0 && game.drawStack.size == 0){
            println("No tiles at the drawstack & MiddleTiles. Game is over")
            onAllRefreshables {  refreshAfterResult() }
        }
        else{
            game.nextPlayer()
            println("\nIt is " + game.currPlayer.name + "s turn.")
            onAllRefreshables { refreshAfterStartTurn() }
        }
    }

    /**
     * calculates the winner (the player with the fewest remaining tokens wins)
     * @return returns a list sorted by the number of token
     */
    fun calculateWinner(): MutableList<AbstractPlayer>{
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game) {
            println("No started game found -> No winner")
            return mutableListOf()
        }
        // sort the list first one is the smallest value
        game.listOfPlayers = game.listOfPlayers.sortedBy { it.numberOfTokens }
        val minimum = game.listOfPlayers[0].numberOfTokens
        val resultList: MutableList<AbstractPlayer> = mutableListOf()
        // check if more than one player has less cards
        for(player in game.listOfPlayers){
            if (minimum == player.numberOfTokens){
                resultList.add(player)
            }
        }
        // if more than one player has the winning number of token
        if (resultList.size > 1){
            // wenn spiel fortgesetzt wird gewinnt der der frueher wieder an der Reihe ist
            val helpList: MutableList<AbstractPlayer> = mutableListOf()
            while (helpList.size < resultList.size){
                game.nextPlayer()
                for(player in resultList) {
                    if (game.currPlayer.equals(player)) {
                        helpList.add(player)
                    }
                }
            }

            // Copy the last players with higher cost than the winner
            for (player in game.listOfPlayers){
                if (player.numberOfTokens != minimum) {
                    helpList.add(player)
                }
            }
            return helpList
        }else{
            resultList.clear()
            game.listOfPlayers.forEach{resultList.add(it)}
            return resultList
        }
    }

    /**
     * Method stores the winner in the highsore
     * @param list which is sorted with the winner on the front
     */
    fun addToScore(list:MutableList<AbstractPlayer>){
        val score = rootService.novaLunaApplication.highscore.calculateHighScorePointsWinner(list)
        // only when no hint was used store in the highscore
        if (!list[0].hintUsed) {
            val toAdd = mutableListOf(Pair(list[0].name, score))
            rootService.novaLunaApplication.highscore.saveHighScore(toAdd, true)
        }
    }


}