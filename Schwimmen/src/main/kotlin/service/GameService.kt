package service

import entity.*
import kotlin.random.Random


/**
 * Service layer class that provides the logic for actions not directly
 * related to a single player.
 */

class GameService(private val rootService: RootService): AbstractRefreshableService() {
    private val seed: Random = Random

    /**
     * Calculates a Players Card Points after a game is over
     *
     * preconditions:
     * - a game was started (i.e. != null)
     * @throws IllegalStateException if one of the conditions is violated
     * @return List with the points of each player (I added the return for the test)
     *
     */
    fun evaluateCardPoints() :List<Double> {

        val game = rootService.currentGame
        checkNotNull(game) { "No game started yet."}
        val size = game.listOfPlayers.size
        val cardPoints = ArrayList<Double>(4)

        for (i in 0 until size){
                cardPoints.add(i,calculatePoints(game.listOfPlayers[i].handCards))
        }
        onAllRefreshables {refreshAfterResult(cardPoints)}
        return cardPoints
    }

    /**
     * Starts a new game (overwriting a currently active one, if it exists)
     *
     * @param playerNames List of all the player names (at least 2 but no more than 4)
     * @throws IllegalStateException if not enough playerNames are entered
     */
    fun startNewGame( playerNames: List<String>)
    {

        val playerList = ArrayList<Player>()
        require(playerNames.size in 2..4) {"must contain at least 2 and no more than 4 Strings"}
        for (i in playerNames.indices){
            playerList.add(i,Player(playerNames[i]))
        }
        val game = Game(playerList)
        game.drawStack.cards = initCardStack(seed) as ArrayDeque<Card>

        check(game.drawStack.size==32) {"Game starts with a 32 card-sized stack"}
        game.middleCards = game.drawStack.drawCards(3)
        for (i in playerList.indices){
               game.listOfPlayers[i].handCards = game.drawStack.drawCards(3)
            }
        rootService.currentGame = game
        onAllRefreshables { refreshAfterStartNewGame() }

    }

    /**
     * Checks if next Player can make his turn
     * if the game has ended due to insufficient cards or knocking the game will calculate the winner [evaluateCardPoints]
     * if everyone hasPassed [checkPassedLastTurn] will change the middle cards
     */

    fun startNextTurn(){
        val game=rootService.currentGame
        checkNotNull(game)
        if (game.drawStack.empty){
            evaluateCardPoints()
        }
        checkKnockedLastTurn()
        checkPassedLastTurn()
        onAllRefreshables { refreshAfterStartTurn() }
    }

    /**
     * Checks  if Player has knocked, if they did game will end with [evaluateCardPoints]
     */
    private fun checkKnockedLastTurn(){
        val game=rootService.currentGame
        checkNotNull(game)
        if(game.getCurrentPlayer().hasKnocked){
            evaluateCardPoints()
        }
    }

    /**
     * Checks if all Players have passed in one round
     * resets PassedFlag + changes middleCards under the condition:
     * - everyone passed
     * - drawStack size >= 3
     * else nothing happen
     * (if everyone passed but  drawStack size < 3 the game ends)
     *
     */
   private fun checkPassedLastTurn(){
        val game=rootService.currentGame
        checkNotNull(game)
        if(game.getCurrentPlayer().hasPassed){
            if (game.drawStack.size<3){
               evaluateCardPoints()
            }
            else{
                game.middleCards= game.drawStack.drawCards(3)
                rootService.playerActionService.resetPassedFlag()
                onAllRefreshables { refreshAfterCardChange()}
            }
        }

    }

    /**
     * Creates a shuffled list of 32 cards (of all four suits and cards
     * from 7 to Ace)
     * @param seed to shuffle the stack
     * @return stack.cards the shuffled card stack for the game
     */
    private fun initCardStack(seed: Random) : List<Card> {
        val stack = CardManager(seed)
        CardSuit.values().forEach{ suit ->
            CardValue.shortDeck().forEach{ value->
               stack.cards.add(Card(suit,value))
            }
        }
        stack.shuffle()
        return stack.cards
    }

    /**
     * ends Game
     */
    fun abortGame(){
        onAllRefreshables { refreshAbortGame() }
    }
    /**
     * Calculates the Points of a hand
     * @param handCards of the player whose Points shall be calculated
     * @return points of a players handCards
     */
    private fun calculatePoints(handCards: List<Card>):Double{
        var points = 0.0
        var tmpPoints: Double
        val heartList = ArrayList<Card> ()
        val spadeList = ArrayList<Card> ()
        val clubList = ArrayList<Card> ()
        val diamondList = ArrayList<Card> ()
        for (i in handCards.indices){
            when (handCards[i].suit) {
                CardSuit.HEARTS -> {
                    heartList.add(handCards[i])
                }
                CardSuit.SPADES -> {
                    spadeList.add(handCards[i])
                }
                CardSuit.CLUBS -> {
                    clubList.add(handCards[i])
                }
                else -> {
                    diamondList.add(handCards[i])
                }
            }
        }
        tmpPoints = cardValue(heartList)
        if (tmpPoints > points){
            points=tmpPoints
        }
        tmpPoints = cardValue(spadeList)
        if (tmpPoints > points){
            points=tmpPoints
        }
        tmpPoints = cardValue(clubList)
        if (tmpPoints > points){
            points=tmpPoints
        }
        tmpPoints = cardValue(diamondList)
        if (tmpPoints > points){
            points=tmpPoints
        }
        if (same3Cards(handCards)){
            points = 30.5
        }
        return points
    }


    /**
     * Checks if player has 3 cards with the same value but different Suit
     * @param [handCards]
     * @return Boolean [check]
     */
    private fun same3Cards (handCards: List<Card>):Boolean{
        val tmpCardValue= handCards[0].value
        var check = false
        if (tmpCardValue==handCards[1].value&&tmpCardValue==handCards[2].value){
            check=true
        }
        return check
    }

    /**
     * calculates the value of the given card list
     * @param handCards list which where sorted by suit in [calculatePoints]
     * @return Double
     */
    private fun cardValue (handCards: ArrayList<Card>): Double {
        if (handCards.isEmpty()) return 0.0
        else {
            var points = 0.0
            for (i in handCards.indices) {
                when (handCards[i].value) {
                    CardValue.SIX -> {
                        points += 6.0
                    }
                    CardValue.SEVEN -> {
                        points += 7.0
                    }
                    CardValue.EIGHT -> {
                        points += 8.0
                    }
                    CardValue.NINE -> {
                        points += 9.0
                    }
                    CardValue.ACE -> {
                        points += 11.0
                    }
                    else -> {
                        points += 10.0
                    }
                }
            }
            return points
        }
    }

}