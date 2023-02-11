package service

import entity.Card

/**
 * Service layer class that provides the logic for the possible actions a player
 * can take in Swim:
 *  - changeAllCards
 *  - changeSingleCard
 *  - pass
 *  - knock
 *  - endTurn
 *  - pauseGame
 */

class PlayerActionService(private val rootService: RootService) : AbstractRefreshableService() {
    /**
     * variable to keep track of whenever the currentPlayer already did his move or not
     */

     var playerActionCalled = false
    /**
     *  draws all cards from the middle and puts them on the players hand
     *  @throws IllegalStateException if currentPlayer already did his action (to prevent changing cards multiple times)
     *  @throws IllegalStateException if game has not started yet
     */
    fun changeAllCards(){
        check(!playerActionCalled)
        val game = rootService.currentGame
        checkNotNull(game)
        val playerCards= game.getCurrentPlayer().handCards
        game.getCurrentPlayer().handCards = game.middleCards
        game.middleCards = playerCards
        playerActionCalled=true
        resetPassedFlag()
        onAllRefreshables { refreshAfterCardChange()}
    }

    /**
     * exchanges one card from the middle with a card on the players hand
     * @param playerCard the Card of the players hand which will be exchanged with
     * @param [middleCard] the Card from the open middle  which the player wants to switch with
     * @throws IllegalArgumentException if player didn't choose one Card from his cards and one card from the midCards
     * @throws IllegalStateException if player already did his action and is trying to do another one
     * @throws IllegalStateException if game has not started yet
     */
    fun changeSingleCard (playerCard: Card, middleCard :Card) {
        check(!playerActionCalled)
        val game = rootService.currentGame
        checkNotNull(game)
        val  indexPlayerCard = getIndexOf(playerCard,game.getCurrentPlayer().handCards)
        val  indexMiddleCard: Int = getIndexOf(middleCard, game.middleCards)
        if(indexPlayerCard==-1||indexMiddleCard==-1){
            throw IllegalArgumentException("playerCard or middleCard weren't selected right")
        }
        game.getCurrentPlayer().handCards[indexPlayerCard]= middleCard

        game.middleCards[indexMiddleCard]= playerCard
        playerActionCalled=true
        resetPassedFlag()
        onAllRefreshables { refreshAfterCardChange()}
    }

    /**
     * Player passes his turn Pass-Flag is set to true
     *  @throws IllegalStateException if player already did his action and is trying to do another one
     *  @throws IllegalStateException if game has not started yet
     */
    fun pass (){
        check(!playerActionCalled)
        val game = rootService.currentGame
        checkNotNull(game)
        game.getCurrentPlayer().hasPassed=true
        playerActionCalled=true
        onAllRefreshables { refreshAfterPassed()}
    }
    /**
     * Player passes his turn Pass-Flag is set to true
     * @throws IllegalStateException if player already did his action and is trying to do another one
     * @throws IllegalStateException if game has not started yet
     */
    fun knock (){
        check(!playerActionCalled)
        val game = rootService.currentGame
        checkNotNull(game)
        game.getCurrentPlayer().hasKnocked=true
        resetPassedFlag()
        playerActionCalled=true
        onAllRefreshables { refreshAfterKnocked()}
    }

    /**
     * ends the PlayersTurn and checks if next Players turn can be started
     * @throws IllegalStateException if player decides to end his turn without doing an action
     */
    fun endTurn(){
        check(playerActionCalled)
        playerActionCalled=false
        val game = rootService.currentGame
        checkNotNull(game)
        game.nextPlayer()
        onAllRefreshables { refreshAfterEndTurn()}
        rootService.gameService.startNextTurn()
    }

    /**
     * Returns the index of cards within a list if the list doesn't contain the card it returns -1
     *
     * @param middleCard Card which index has to be found within the list of [middleCards]
     * @param middleCards List of cards were the given card should be found in
     */
    private fun getIndexOf(middleCard: Card, middleCards: ArrayList<Card>): Int {
        for (i in 0..2) {
            if (middleCard == middleCards[i]) {
                return i
            }
        }
        return -1

    }

    /**
     * shows PauseScreen
     */

    fun pauseGame(){
        onAllRefreshables { refreshAfterPauseGame() }
    }


    /**
     * sets all Players Pass Flag back to false
     */

    fun resetPassedFlag(){
        val game = rootService.currentGame
        checkNotNull(game)
        for (i in 0 until game.listOfPlayers.size) {
            game.listOfPlayers[i].hasPassed=false
        }
        rootService.currentGame=game
    }
}