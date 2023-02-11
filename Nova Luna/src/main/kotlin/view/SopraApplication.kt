package view

import entity.AI
import entity.AbstractPlayer
import entity.PlayerColor
import service.RootService
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.BidirectionalMap
import tools.aqua.bgw.util.Font

/**
 *
 * Main application for the boardGame "Nova Luna". Implements the [BoardGameApplication].
 */
class SopraApplication : BoardGameApplication("SoPra Game"), Refreshable {
    /**
     * gameState
     */
    var gameRunning = false
    /**
     * [RootService] connecting the view layer with the other layers
     */
    private  val rootService = RootService()

    /**
     * Scene instances which the game consists of
     */
    private val gridScene = Array(4){
        GridScene(rootService)
    }

    /**
     * [lastScene] instance to help with switching back to Scenes
     */
    private var lastScene = MenuScene()

    /**
     * [selectedGame] MutableString in which selected Game name is saved
     */
    private var selectedGame=""

    /**
     * [gameLabel] which is added to LoadGameScene after selecting a saved Game
     * shows the chosen game or "No game selected" if selection was aborted
     */
    private var gameLabel= Label(300,600, width = 200)
    /**
     * [BidirectionalMap] to map from the [AbstractPlayer] to his [GridScene]
     * [PlayerColor] instead of [AbstractPlayer] is used because of redo/undo feature
     */
    val gridSceneToPlayer = BidirectionalMap<GridScene, PlayerColor>()

    /**
     * [BoardScene] that is shown after starting/loading a game
     */
    private val boardScene = BoardScene(rootService,this).apply {
        pauseButton.onMouseClicked={
            showMenuScene(pauseScene)
            lastScene=pauseScene
        }
        questionMarkButton.apply {
            onMouseClicked = {
                hintBoxPane.isVisible = true
                questionMarkTextBox.apply {
                    Font(size = 15)
                    text  = rootService.playerActionService.getHint()
                    alignment = Alignment.TOP_LEFT
                }
                this@SopraApplication.showHintInGrid()
            }
        }
    }

    /**
     * [StartMenuScene] shown at the start of the application
     */
    private  val startScene = StartMenuScene().apply {
        newGameButton.onMouseClicked ={ showMenuScene(setUpScene) }
        loadGameButton.onMouseClicked ={ showMenuScene(loadGameScene)}
        highScoreButton.onMouseClicked = {showMenuScene(highScoreScene)}
        quitButton.onMouseClicked = { this@SopraApplication.exit() }
    }

    /**
     * [SetUpScene] shown after choosing to create a new game
     */
    private val setUpScene = SetUpScene(rootService).apply {
        loadCsvButton.onMouseClicked={
            val tmpFileDialog=showFileDialog(csvFileDialog)
            pathCsv = if(!tmpFileDialog.isEmpty){
                "CsvDatei/"+tmpFileDialog.get()[0].name
            } else{
                "CsvDatei/nl_tiles.csv"
            }
        }
        backButton.onMouseClicked={backToMenu()}
    }

    /**
     * [loadGameScene] shown after choosing to select a saved game
     */
    private val loadGameScene = LoadMenuScene().apply {
      selectButton.onMouseClicked= {
          removeComponents(gameLabel)
          val optionalGameList = showFileDialog(gameList)
          if (optionalGameList.isEmpty) {
              gameLabel.apply{text = "No game selected"}
              this.addComponents(gameLabel)
          }
          else {
              selectedGame = optionalGameList.get()[0].name
              if (selectedGame.isNotBlank()) {
                  startButton.isDisabled = false
              }
              gameLabel.apply { text = "Selected Game: " + selectedGame.removeSuffix(".bin") }
              this.addComponents(gameLabel)
          }
          startButton.onMouseClicked = {
              rootService.ioService.loadGame(selectedGame.removeSuffix(".bin"))
              hideMenuScene()
          }
      }
        backButton.onMouseClicked={
            backToMenu()}

    }
    /**
     * [HighscoreScene] shown after choosing to view Highscore-list
     */
    private val highScoreScene = HighscoreScene (rootService).apply {
        backButton.onMouseClicked={backToMenu()}
    }

    /**
     * [PauseScene] shown after pausing the game
     */
    private val pauseScene= PauseScene().apply {
        lastScene=this
        continueButton.onMouseClicked={this@SopraApplication.hideMenuScene()}
        loadGameButton.onMouseClicked={this@SopraApplication.showMenuScene(loadGameScene)}
        saveGameButton.onMouseClicked={this@SopraApplication.showMenuScene(saveMenuScene)}
        backToMenuButton.onMouseClicked={
            lastScene=startScene
            backToMenu()
        }
    }

    /**
     * [ResultScene] show after the game is over
     */
    private val resultScene = ResultScene(rootService).apply{
        backToButton.onMouseClicked={
            this@SopraApplication.lastScene = startScene
            this@SopraApplication.showMenuScene(startScene)
        }
        highScoreButton.onMouseClicked={
            this@SopraApplication.lastScene = this
            this@SopraApplication.highScoreScene.getHighScoreList()
            showMenuScene(highScoreScene)
        }
    }

    /**
     * [SaveMenuScene] shown after selecting saveGame in the pause Menu
     */
    private val saveMenuScene = SaveMenuScene(rootService).apply {
        backButton.onMouseClicked={backToMenu()}
    }

   init {
       rootService.addRefreshables(
           this,
           startScene,
           boardScene,
           *gridScene,
           resultScene
       )
       this.showGameScene(boardScene)
       this.showMenuScene(startScene)
       lastScene=startScene
    }

    /**
     * After starting a game, reset the [gridScene]s and load them.
     */
    override fun refreshAfterStartGame() {
        gameRunning=true
        this.gridSceneToPlayer.clear()
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        for(i in 0 until game.listOfPlayers.size){
            gridScene[i].resetGrid()
            gridScene[i].apply {
                loadGridFromPlayer(game.listOfPlayers[i])
                backButton.onMouseClicked = { this@SopraApplication.showGameScene(boardScene) }
            }
            gridSceneToPlayer.add(gridScene[i], game.listOfPlayers[i].color)
        }
        this.setAnimationSpeed()
        this.hideMenuScene()
    }

    override fun refreshAfterLoadGame() {
        this.refreshAfterStartGame()
    }

    /**
     * Show the [BoardScene] after the end of the turn.
     */
    override fun refreshAfterEndTurn() {
        this.showGameScene(boardScene)
        boardScene.hintBoxPane.isVisible = false
        gridScene.forEach {
            it.hintBoxPane.isVisible = false
        }
    }

    /**
     * Call the right [GridScene] to update the task tokens.
     */
    override fun refreshAfterUpdateTask() {
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        gridSceneToPlayer.backward(game.currPlayer.color).placeTaskToken()
    }

    /**
     * Reload grid of the player that has been undone. (Is this even an english sentence)
     */
    override fun refreshAfterUndo(playerToUndo: AbstractPlayer) {
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        val gridToUndo = gridSceneToPlayer.backward(game.currPlayer.color)
        gridToUndo.resetGrid()
        gridToUndo.loadGridFromPlayer(game.currPlayer)
    }

    /**
     * Reload grid of the player that has been redone.
     */
    override fun refreshAfterRedo(playerToRedo: AbstractPlayer) {
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        val oldGame = game.prevState
        checkNotNull(oldGame)
        val gridToRedo = gridSceneToPlayer.backward(oldGame.currPlayer.color)
        val player = game.listOfPlayers.find { it.color == oldGame.currPlayer.color }
        checkNotNull(player)
        gridToRedo.resetGrid()
        gridToRedo.loadGridFromPlayer(player)
    }

    /**
     * sets gameRunning too false to stop ai-Move
     * shows the [resultScene] after a game has ended
     */
    override fun refreshAfterResult() {
        gameRunning=false
        showGameScene(boardScene)
        showMenuScene(resultScene)
    }

    /**
     * Set the animation speed of the [BoardScene] and [GridScene] (only if the grid scene belongs to an AI) accordingly
     * to the animation speed chosen in the [SetUpScene].
     */
    private fun setAnimationSpeed(){
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        this.boardScene.aiAnimationSpeed = when(this.setUpScene.aiAnimationSpeedDropDown.selectedItem){
            "Slow" -> {SLOW_ANIMATION_SPEED}
            "Normal" -> {DEFAULT_ANIMATION_SPEED}
            "Fast" -> {FAST_ANIMATION_SPEED}
            else -> {FAST_ANIMATION_SPEED}
        }
        for(i in 0 until game.listOfPlayers.size){
            if(game.listOfPlayers[i] is AI){
                when(this.setUpScene.aiAnimationSpeedDropDown.selectedItem){
                    "Slow" -> {
                        gridScene[i].animationSpeed = 500
                        gridScene[i].aiDelayToEndTurn = 2500
                    }
                    "Normal" -> {
                        gridScene[i].animationSpeed = 400
                        gridScene[i].aiDelayToEndTurn = 1800
                    }
                    "Fast" -> {
                        gridScene[i].animationSpeed = 200
                        gridScene[i].aiDelayToEndTurn = 1500
                    }
                    else -> {
                        gridScene[i].animationSpeed = 200
                        gridScene[i].aiDelayToEndTurn = 1500
                    }
                }
            }
        }

    }

    /**
     * Show the hint only in the current player [GridScene]
     */
    private fun showHintInGrid(){
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        gridSceneToPlayer.backward(game.currPlayer.color).apply {
            hintBoxPane.isVisible = true
            questionMarkTextBox.apply {
                text = rootService.playerActionService.getHint()
                alignment = Alignment.TOP_LEFT
            }
        }
    }

    /**
     * switches to the last Menu
     */
    private fun backToMenu(){
        hideMenuScene()
        showMenuScene(lastScene)
    }

}

