package service

import entity.Card
import entity.CardSuit
import entity.CardValue
import view.Refreshable
import kotlin.test.*

/**
 * Class that provides tests for [GameService] and [PlayerActionService] (both at the same time,
 * as their functionality is not easily separable) by basically playing through some sample games.
 * [TestRefreshable] is used to validate correct refreshing behavior even though no GUI
 * is present.
 */

class ServiceTest {
    /**
     * Instances to test with
     */
    private val playerList = listOf("Abby","Bob","Cindy","David")

    private fun setUpGame(vararg refreshables: Refreshable): RootService {
        val mc = RootService()

        refreshables.forEach { mc.addRefreshable(it) }

        val cards1= ArrayList<Card>(3)
        // player 1 handCards
        cards1.add(Card(CardSuit.CLUBS, CardValue.QUEEN))
        cards1.add(Card(CardSuit.SPADES, CardValue.TEN))
        cards1.add(Card(CardSuit.DIAMONDS, CardValue.SEVEN))

        val cards2 = ArrayList<Card>(3)
        // player 2 right handCards
        cards2.add(Card(CardSuit.DIAMONDS, CardValue.JACK))
        cards2.add(Card(CardSuit.SPADES, CardValue.SEVEN))
        cards2.add(Card(CardSuit.DIAMONDS, CardValue.KING))

        val midCards = ArrayList<Card>(3)
            // middle cards
        midCards.add(Card(CardSuit.HEARTS, CardValue.ACE))
        midCards.add(Card(CardSuit.SPADES, CardValue.NINE))
        midCards.add(Card(CardSuit.CLUBS, CardValue.ACE))

        val drawStack = ArrayDeque<Card>()

        drawStack.add(Card(CardSuit.CLUBS, CardValue.EIGHT))
        drawStack.add(Card(CardSuit.CLUBS, CardValue.NINE))
        drawStack.add(Card(CardSuit.HEARTS, CardValue.KING))
        drawStack.add(Card(CardSuit.DIAMONDS, CardValue.QUEEN))
        drawStack.add(Card(CardSuit.SPADES, CardValue.QUEEN))
        drawStack.add(Card(CardSuit.HEARTS, CardValue.QUEEN))


        mc.gameService.startNewGame(listOf("Abby","Bob"))
        val mcGame =mc.currentGame
        checkNotNull(mcGame)
        mcGame.drawStack.cards= drawStack
        mcGame.listOfPlayers[0].handCards = cards1
        mcGame.listOfPlayers[1].handCards = cards2
        mcGame.middleCards=midCards
        mc.currentGame=mcGame
        println(mc.currentGame)
        return mc
    }

    /**
     * Tests the default case of starting a game: instantiate a [RootService] and then run
     * startNewGame on its [RootService.gameService].
     */

    @Test
    fun testStartNewGame() {
        val testRefreshable = TestRefreshable()
        val mc = RootService()
        mc.addRefreshable(testRefreshable)

        assertFalse(testRefreshable.refreshAfterStartNewGameCalled)
        assertNull(mc.currentGame)
        mc.gameService.startNewGame(playerList)
        val game = mc.currentGame
        checkNotNull(game)
        assertTrue(testRefreshable.refreshAfterStartNewGameCalled)
        assertNotNull(mc.currentGame)

        assertEquals(3, game.middleCards.size)
        assertEquals(3, game.listOfPlayers[0].handCards.size)
        assertEquals(3, game.listOfPlayers[1].handCards.size)
        assertEquals(3, game.listOfPlayers[2].handCards.size)
        assertEquals(3, game.listOfPlayers[3].handCards.size)
        assertEquals(17, game.drawStack.size)
        // test startNewGame with list containing only one Name
        assertFails {  mc.gameService.startNewGame(listOf("Bob")) }
        // test startNewGame with list containing too many Names
        val tooManyPlayers = listOf("Abby","Bob","Cindy","David","Emma")
        assertFails {  mc.gameService.startNewGame(tooManyPlayers) }
    }

    /**
     * Test for function evaluateCardPoints
     */
    @Test
    fun testEvaluateCardPoints(){
        val testRefreshable = TestRefreshable()
        val mc = setUpGame(testRefreshable)
        assertFalse {testRefreshable.refreshAfterResultCalled }


        var points =  mc.gameService.evaluateCardPoints()
        assertEquals(10.0,points[0]) // points of player 1 handCards:[♣Q, ♠10, ♦7]
        assertEquals(20.0,points[1]) // points of player 2 handCards: [♦K, ♠7, ♦J]
        assertTrue {testRefreshable.refreshAfterResultCalled}

        //testing case 3 cards with the same value
        testRefreshable.reset()
        val mcGame = mc.currentGame
        checkNotNull(mcGame)
        mcGame.getCurrentPlayer().handCards= arrayListOf(
            Card(CardSuit.CLUBS, CardValue.QUEEN),
            Card(CardSuit.DIAMONDS, CardValue.QUEEN),
            Card(CardSuit.SPADES, CardValue.QUEEN)
        )
        assertFalse {testRefreshable.refreshAfterResultCalled }
        points =  mc.gameService.evaluateCardPoints()
        assertEquals(30.5,points[0]) // points of player 1 handCards:[♣Q,♦Q, ♠Q ]
        assertTrue {testRefreshable.refreshAfterResultCalled}
    }

    /**
     * Game simulation to test pass, knock and endTurn, which also includes:
     * startNextTurn, checkedLastKnocked, checkPassedLastTurn
     */
    @Test
    fun testGameSimulation(){
        val testRefreshable = TestRefreshable()
        val mc = setUpGame(testRefreshable)
        var mcGame =mc.currentGame
        checkNotNull(mcGame)
        assertFalse {testRefreshable.refreshAfterKnockedCalled}
        assertFalse {testRefreshable.refreshAfterPassedCalled}
        assertFalse {testRefreshable.refreshAfterStartTurnCalled}
        assertFalse {testRefreshable.refreshAfterCardChangeCalled}
        assertFalse {testRefreshable.refreshAfterEndTurnCalled}
        //player1 passes and ends turn
        mc.playerActionService.pass()
        var currPlayer= mcGame.getCurrentPlayer()
        mc.currentGame=mcGame
        assertTrue {currPlayer.hasPassed}
        assertTrue {testRefreshable.refreshAfterPassedCalled}
        mc.playerActionService.endTurn()
        assertTrue {testRefreshable.refreshAfterEndTurnCalled}
        assertTrue {testRefreshable.refreshAfterStartTurnCalled}

        testRefreshable.reset()
        mcGame =mc.currentGame
        checkNotNull(mcGame)
        // test to see if game moves to the next Player
        assertEquals(mcGame.listOfPlayers[1],mcGame.getCurrentPlayer())
        // player2 passes and ends turn
        mc.playerActionService.pass()
        currPlayer=mcGame.getCurrentPlayer()
        assertTrue {currPlayer.hasPassed}
        assertTrue {testRefreshable.refreshAfterPassedCalled}
        val oldMidCards = mcGame.middleCards
        mc.currentGame=mcGame
        mc.playerActionService.endTurn()
        assertTrue {testRefreshable.refreshAfterEndTurnCalled}
        assertTrue {testRefreshable.refreshAfterStartTurnCalled}

        testRefreshable.reset()
        mcGame =mc.currentGame
        checkNotNull(mcGame)
        // test to see if game moves to the next Player
        assertEquals(mcGame.listOfPlayers[0],mcGame.getCurrentPlayer())
        //test to see if middleCards are replaced if everyone has passed
        assertNotEquals(oldMidCards,mcGame.middleCards)

        //test if PassedFlag have been reset
        assertFalse {mcGame.getCurrentPlayer().hasPassed }

        //test knock and checkedLastTurnKnocked
        assertFalse{mcGame.getCurrentPlayer().hasKnocked}
        mc.playerActionService.knock()
        assertTrue{mcGame.getCurrentPlayer().hasKnocked}
        assertTrue {testRefreshable.refreshAfterKnockedCalled}
        mc.currentGame=mcGame
        mc.playerActionService.endTurn()
        //player2 passes
        mc.playerActionService.pass()
        assertFalse {testRefreshable.refreshAfterResultCalled}
        //endTurn() moves to nextPlayer , startNextTurn() checks if player hasKnocked, if that is the case game ends and result are shown
        mc.playerActionService.endTurn()
        assertTrue {testRefreshable.refreshAfterResultCalled}
    }

    /**
     * Test a different playthrough , test to check changeAllCards, changeOneCard or cards are empty
     */

    @Test
    fun testGameSimulation2() {
        val testRefreshable = TestRefreshable()
        val mc = setUpGame(testRefreshable)
        var mcGame = mc.currentGame
        checkNotNull(mcGame)
        assertFalse { testRefreshable.refreshAfterCardChangeCalled }
        assertFalse { testRefreshable.refreshAfterStartTurnCalled }
        assertFalse { testRefreshable.refreshAfterEndTurnCalled }

        //test player1 changesAllCards + test doing that twice
        //handCards:[♣Q, ♠10, ♦7], midCards:[♥A, ♠9, ♣A]
        val handCardsTmp= mcGame.getCurrentPlayer().handCards
        val midCardsTmp=mcGame.middleCards
        mc.playerActionService.changeAllCards()
        //handCards:[♥A, ♠9, ♣A], midCards:[♣Q, ♠10, ♦7]
        assertEquals(midCardsTmp,mcGame.getCurrentPlayer().handCards)
        assertEquals(handCardsTmp,mcGame.middleCards)
        assertTrue(testRefreshable.refreshAfterCardChangeCalled)
        assertFalse(mcGame.getCurrentPlayer().hasPassed)
        assertFails { mc.playerActionService.changeAllCards() }
        mc.currentGame=mcGame
        mc.playerActionService.endTurn()

        testRefreshable.reset()

        //test player2 changesSingleCard : handCards[♦J,♠7,♦K], midCards[♣Q,♠10,♦7]
        val kingOfDiamond = Card(CardSuit.DIAMONDS,CardValue.KING)
        val tenOfSpade =Card(CardSuit.SPADES,CardValue.TEN)
        mc.playerActionService.changeSingleCard(kingOfDiamond,tenOfSpade)
        mcGame= mc.currentGame
        checkNotNull(mcGame)
        assertEquals(tenOfSpade,mcGame.getCurrentPlayer().handCards[2])
        assertEquals(kingOfDiamond,mcGame.middleCards[1])
        assertTrue(testRefreshable.refreshAfterCardChangeCalled)
        mc.currentGame=mcGame
        // test wrong cards handCards[♦J,♠7,♠10], midCards[♣Q,♦K,♦7] + testing doing changesSingleCard twice
        assertFails { mc.playerActionService.changeSingleCard(kingOfDiamond,tenOfSpade) }
        assertFails { mc.playerActionService.changeSingleCard(tenOfSpade,kingOfDiamond)}
        mc.playerActionService.endTurn()
        testRefreshable.reset()

        // test game ending because of empty Stack

        mc.playerActionService.pass()
        mc.playerActionService.endTurn()
        mc.playerActionService.pass()
        mc.playerActionService.endTurn()
        //3 cards in drawStack left
        mc.playerActionService.pass()
        mc.playerActionService.endTurn()
        mc.playerActionService.pass()
        // drawStack empty -> everyone has passed put drawStack is not able to renew the cards
        // game ends
        mc.playerActionService.endTurn()


    }

    /**
     * test to see if pauseGame and abortGame works
     */
    @Test
    fun testAbortPauseGame (){
        val testRefreshable = TestRefreshable()
        val mc = setUpGame(testRefreshable)

        // test game paused
        assertFalse { testRefreshable.refreshAfterPauseGameCalled}
        mc.playerActionService.pauseGame()
        assertTrue { testRefreshable.refreshAfterPauseGameCalled }

        //test abort Game
        assertFalse { testRefreshable.refreshAbortGameCalled }
        mc.gameService.abortGame()
        assertTrue { testRefreshable.refreshAbortGameCalled }
    }
}