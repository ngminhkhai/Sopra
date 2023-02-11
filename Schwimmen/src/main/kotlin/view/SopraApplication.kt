package view

import tools.aqua.bgw.core.BoardGameApplication
import service.RootService


class SopraApplication : BoardGameApplication("Swim"), Refreshable{

    // Central service from which all others are created/accessed
    // also holds the currently active game
    private val rootService = RootService()

    // Scenes

    // This is where the actual game takes place
    private val gameScene = GameScene(rootService)

    // This menu scene is shown after each finished game (i.e. no more cards to draw)
    private val gameFinishedMenuScene = ResultScene(rootService).apply {
        newGameButton.onMouseClicked = {
            this@SopraApplication.showMenuScene(newGameMenuScene)
        }
        quitButton.onMouseClicked = {
            exit()
        }
    }

    // This menu scene is shown after application start and if the "new game" button
    // is clicked in the gameFinishedMenuScene
    private val newGameMenuScene = StartMenuScene(rootService).apply {
        quitButton.onMouseClicked = {
            exit()
        }
    }

    // menu is shown after one pauses the Game
    //shows the GameMenu when backToMenuButton is pressed
    //hides the PauseMenu when continueButton is pressed
    private val pauseScene= PauseScene().apply{
        backToMenuButton.onMouseClicked={
            this@SopraApplication.showMenuScene(newGameMenuScene)
        }
        continueButton.onMouseClicked = {
            hideMenuScene()
        }
    }
    //hides the blackScreen when clickButton is pressed
    private val blackscreen= BlackScreen().apply{
     clickButton.onMouseClicked={
         hideMenuScene()
        }
    }



    init {
        // all scenes and the application itself need too
        // reacts to changes done in the service layer
        rootService.addRefreshables(
            this,
            gameScene,
            gameFinishedMenuScene,
            newGameMenuScene,
            pauseScene
        )

        // This is just done so that the blurred background when showing
        // the new game menu has content and looks nicer
        rootService.gameService.startNewGame(listOf("Bob", "Alice"))

        this.showGameScene(gameScene)
        this.showMenuScene(newGameMenuScene, 0)
        registerGameEvents()

    }

    override fun refreshAfterStartNewGame() {
        this.hideMenuScene()
    }

    override fun refreshAfterResult(points:List<Double>) {
        this.showMenuScene(gameFinishedMenuScene)
    }

    override fun refreshAfterEndTurn() {
        this.showMenuScene(blackscreen)
    }

    private fun registerGameEvents(){
        gameScene.pauseButton.onMouseClicked = {
            this.showMenuScene(pauseScene)
        }

    }

}

