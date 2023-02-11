package entity
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

/**
 * Test cases for [CardManager]
 */
class CardManagerTest {
    /**
     * Instances to test with
     */
    private val c1 = Card(CardSuit.DIAMONDS, CardValue.THREE)
    private val c2 = Card(CardSuit.HEARTS, CardValue.SEVEN)
    private val c3 = Card(CardSuit.CLUBS, CardValue.QUEEN)
    private val cardManager=CardManager()
    // unicode characters for the suits, as those should be used by [Card.toString]
    private val heartsChar = '\u2665' // ♥
    private val diamondsChar = '\u2666' // ♦
    private val clubsChar = '\u2663' // ♣


    /**
     * Test if shuffle works
     */
    @Test
    fun testShuffle() {
        val stack = CardManager(random = Random(42))
        fillTheCards(stack)
        stack.drawCards(stack.size)
        putOnTop(stack,listOf(c1, c2, c3))
        stack.shuffle()
        assertEquals(listOf(c2,c3,c1), stack.drawCards(3))
        assertEquals(0, stack.size)
    }

    /**
     * Test if drawing from an empty stack throws an exception
     * Or Drawing a negative number of card
     */
    @Test
    fun testDrawFail() {
        fillTheCards(cardManager)
        val testStack = cardManager.drawCards(3)
        val size= testStack.size
        assertEquals(size,3)
        assertFails { cardManager.drawCards(33) }
        assertFails { cardManager.drawCards(-1) }
    }
    /**
     * Test to see if toString works as it should
     * I didn't test Spade since this method is also already tested in CardTest
     */
    @Test
    fun testToString() {
        assertEquals(diamondsChar + "3", c1.toString())
        assertEquals(heartsChar + "7", c2.toString())
        assertEquals(clubsChar + "Q", c3.toString())
    }
    /**
     * Fills CardManagers stacks with a skat-deck to test draw and shuffle
     * @param cardManager, whose cards shall be filled
     */
    private fun fillTheCards(cardManager: CardManager){
        CardSuit.values().forEach{ suit ->
            CardValue.shortDeck().forEach{ value->
                cardManager.cards.add(Card(suit,value))
            }
        }
    }

    /**
     * puts a given list of cards on top of this card stack, so that
     * the last element of the passed parameter [cards] will be on top of
     * the stack afterwards.
     *
     * @param cards that shall be put on
     * @param cardManager cards
     */
    private fun putOnTop (cardManager:CardManager,cards: List<Card>) {
        cards.forEach(cardManager.cards::addFirst)
    }

}