package view

import entity.AbstractPlayer

/**
 * This interface provides a mechanism for the service layer classes to communicate
 * (usually to the view classes) that certain changes have been made to the entity
 * layer, so that the user interface can be updated accordingly.
 *
 * Default (empty) implementations are provided for all methods, so that implementing
 * UI classes only need to react to events relevant to them.
 */

interface Refreshable {
    /**
     * perform refreshes after updateTask
     */
    fun refreshAfterUpdateTask(){}
    /**
     * perform refreshes after Result
     */
    fun refreshAfterResult(){}

    /**
     * perform refreshes after TilePlaced
     */
    fun refreshAfterTilePlaced(){}

    /**
     * perform refreshes after TilePicked
     */
    fun refreshAfterTilePicked(){}

    /**
     * perform refreshes after FillTile
     */
    fun refreshAfterFillTile(){}

    /**
     * perform refreshes after StartGame
     */
    fun refreshAfterStartGame(){}

    /**
     * perform refreshes after LoadGame
     */
    fun refreshAfterLoadGame(){}

    /**
     * perform refreshes after SaveGame
     */
    fun refreshAfterSaveGame(){}

    /**
     * perform refreshes after StartTurn
     */
    fun refreshAfterStartTurn(){}

    /**
     * perform refreshes after EndTurn
     */
    fun refreshAfterEndTurn(){}

    /**
     * perform refreshes after HighScore
     */
    fun refreshAfterHighScore(){}

    /**
     * perform refreshes after an undo action
     * @param playerToUndo The [AbstractPlayer] that gets his action undone
     */
    fun refreshAfterUndo(playerToUndo : AbstractPlayer){}

    /**
     * perform refreshes after a redo action
     * @param playerToRedo The [AbstractPlayer] that gets his action redone
     */
    fun refreshAfterRedo(playerToRedo : AbstractPlayer){}

}