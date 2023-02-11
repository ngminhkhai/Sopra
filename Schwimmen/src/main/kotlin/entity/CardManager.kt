package entity

import kotlin.random.Random

/**
 *  Entity class that represents the CardManager of the Game , contains [random] variable to help with the function [shuffle]
 *  as well as the [cards] which will be known as the drawStack of the Game
 */
class CardManager(
    private val random: Random = Random,
) {
    var cards: ArrayDeque<Card> =  ArrayDeque(32)

    /**
     * the amount of cards in this stack
     */
    val size: Int get() = cards.size

    /**
     * Returns `true` if the stack is empty, `false` otherwise.
     */
    val empty: Boolean get() = cards.isEmpty()

    /**
     * Shuffles the cards in this stack
     */
    fun shuffle() {
        cards.shuffle(random)
    }

    /**
     * Draws [amount] cards from this stack.
     *
     * @param amount the number of cards to draw; defaults to 1 if omitted.
     *
     * @throws IllegalArgumentException if not enough cards on stack to draw the desired amount.
     */
     fun drawCards(amount: Int=1): ArrayList<Card>{
         require (amount in 1..cards.size) { "can't draw $amount cards from $cards" }
         return List(amount) { cards.removeFirst() } as ArrayList<Card> /* = java.util.ArrayList<entity.Card> */
     }

    /**
     * overrides toString to cards.toString()
     */
    override fun toString(): String = cards.toString()
}