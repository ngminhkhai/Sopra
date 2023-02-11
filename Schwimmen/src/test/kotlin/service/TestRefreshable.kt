package service

import view.Refreshable

/**
 * [Refreshable] implementation that refreshes nothing, but remembers
 * if a refresh method has been called (since last [reset])
 */
class TestRefreshable: Refreshable {

    var refreshAfterStartNewGameCalled: Boolean = false
        private set

    var refreshAfterCardChangeCalled: Boolean = false
        private set

    var refreshAfterPassedCalled: Boolean = false
        private set

    var refreshAfterKnockedCalled: Boolean = false
        private set

    var refreshAfterStartTurnCalled: Boolean = false
        private set

    var refreshAfterEndTurnCalled: Boolean = false
        private set

    var refreshAfterPauseGameCalled: Boolean = false
        private set

    var refreshAbortGameCalled: Boolean = false
        private set

    var refreshAfterResultCalled: Boolean = false
        private set

    /**
     * resets all *Called properties to false
     */
    fun reset() {
        refreshAfterStartNewGameCalled = false
        refreshAfterCardChangeCalled = false
        refreshAfterPassedCalled = false
        refreshAfterKnockedCalled = false
        refreshAfterStartTurnCalled = false
        refreshAfterEndTurnCalled = false
        refreshAfterPauseGameCalled = false
        refreshAbortGameCalled = false
        refreshAfterResultCalled = false
    }

    override fun refreshAfterStartNewGame() {
        refreshAfterStartNewGameCalled = true
    }
    override fun refreshAfterCardChange() {
        refreshAfterCardChangeCalled = true
    }
    override fun refreshAfterPassed() {
        refreshAfterPassedCalled = true
    }
    override fun refreshAfterKnocked() {
        refreshAfterKnockedCalled = true
    }
    override fun refreshAfterStartTurn() {
        refreshAfterStartTurnCalled = true
    }
    override fun refreshAfterEndTurn() {
        refreshAfterEndTurnCalled = true
    }
    override fun refreshAfterPauseGame() {
        refreshAfterPauseGameCalled= true
    }
    override fun refreshAbortGame() {
        refreshAbortGameCalled = true
    }
    override fun refreshAfterResult(points:List<Double>) {
        refreshAfterResultCalled = true
    }


}