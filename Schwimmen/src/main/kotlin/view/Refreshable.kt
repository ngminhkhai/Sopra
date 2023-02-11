package view


/**
 * This interface provides a mechanism for the service layer classes to communicate
 * (usually to the view classes) that certain changes have been made to the entity
 * layer, so that the user interface can be updated accordingly.
 *
 * Default (empty) implementations are provided for all methods, so that implementing
 * UI classes only need to react to events relevant to them.
 *
 *
 */
interface Refreshable {

    /**
     * perform refreshes that are necessary after a new game started
     */
    fun refreshAfterStartNewGame() {}

    /**
     * perform refreshes that are necessary after a player has provoked an actions which results into him changing handCards
     */
    fun refreshAfterCardChange() {}

    /**
     * perform refreshes that are necessary after a player has passed
     */
    fun refreshAfterPassed() {}

    /**
     * perform refreshes that are necessary after a player has knocked
     */
    fun refreshAfterKnocked() {}

    /**
     * perform refreshes that are necessary after a player has startes his turn
     */
    fun refreshAfterStartTurn() {}

    /**
     * perform refreshes that are necessary after a player has ended his turn
     */
    fun refreshAfterEndTurn() {}

    /**
     * performs refreshes that are necessary after a player has paused the game
     */
    fun refreshAfterPauseGame() {}

    /**
     * performs refreshes that are necessary after a player has stopped a game
     */
    fun refreshAbortGame() {}

    /**
     * perform refreshes that are necessary after the last round was played
     */
    fun refreshAfterResult(points:List<Double>){}

}