package view


import ai.AIService
import entity.*
import service.RootService
import tools.aqua.bgw.animation.DelayAnimation
import tools.aqua.bgw.animation.SequentialAnimation
import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.container.Area
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.event.KeyCode
import tools.aqua.bgw.util.BidirectionalMap
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual
import java.awt.Color

/**
 * Main game scene that holds the [Board] and the buttons to the grids of the player.
 */
class BoardScene(private var rootService: RootService, private val application:
    SopraApplication) : BoardGameScene(1920, 1080), Refreshable{

    /**
     * The animation speed for the AI (in ms)
     */
    var aiAnimationSpeed = DEFAULT_ANIMATION_SPEED

    /**
     * The nova luna [Board] with the tiles, player tokens and meeple token
     */
    private val board = Board(rootService, 400,150)

    /**
     * Instance which saves the next Step of an AI-Player as suggest by [AIService.findStep]
     */
    private var aiAction= IntArray(4)

    /**
     * A [Pane] to make placement of the [gridButtons] easier
     */
    private val gridButtonLayout = Pane<ComponentView>(1500,300, 2 * TILE_SIZE, 2 * TILE_SIZE)

    /**
     * A [Pane] to make placement of the [fillButton],[undoButton] and [redoButton] easier
     */
    private val micButtonLayout = Pane<Button>(1500,950,350,150)

    /**
     * The button to fill the tiles on the [board]. Should only be enabled/disabled through
     * the [tryEnableDisableFillButton] method.
     */
    private val fillButton = Button(
        0,0,
        200,50,
        "Fill Tiles",
        Font(size = 20, fontWeight = Font.FontWeight.BOLD)).apply {
            visual = ColorVisual.LIGHT_GRAY
            isDisabled = true
            onMouseClicked = { rootService.playerActionService.fillTiles() }
    }

    /**
     * The undo button. Calls the undo action in the player action service.
     * Enabled/Disabled through the [tryEnableDisableRedoUndoButton].
     */
    private val undoButton = Button(210,0, 50,50, visual = ImageVisual("undo.png")).apply {
        onMouseClicked = {rootService.playerActionService.undo()}
    }

    /**
     * The redo button. Calls the redo action in the player action service.
     * Enabled/Disabled through the [tryEnableDisableRedoUndoButton].
     */
    private val redoButton = Button(270,0, 50,50, visual = ImageVisual("redo.png")).apply {
        onMouseClicked = {rootService.playerActionService.redo()}
    }

    /**
     * The question mark button. Calls the AI and ask for advice.
     * The advice is displayed
     */
     val questionMarkButton = Button(20,950, 50,50, visual = ImageVisual("question_mark.png")).apply {
    }

    /**
     * [Pane] for the hintbox consisting of [questionMarkTextBox] and [questionMarkTextBoxBorder].
     * Makes placing and visibility easier
     */
    val hintBoxPane = Pane<Button>(80,880, 370,190).apply { isVisible = false }

    /**
     * The question mark text box (button).
     * Displays the AI advice.
     */
    private val questionMarkTextBoxBorder = Button(0,0, 370,190).apply {
        visual = ColorVisual.DARK_GRAY
    }

    /**
     * The question mark text box (button).
     * Displays the AI advice.
     */
     val questionMarkTextBox = Button(10,10, 350,170, "",
        Font(size = 24), isWrapText = true)

    /**
     * Button to resume the AI after the AI has been paused. Only visible if AI is paused. Should only be activated/
     * deactivated with [tryPauseResumeAI].
     */
    private val resumeAiButton = Button(1500, 30, 150 ,50, "Resume AI").apply {
        font = Font(size = 20, fontWeight = Font.FontWeight.BOLD)
        visual = ColorVisual.ORANGE
        isVisible = false
        onMouseClicked = {this@BoardScene.tryPauseResumeAI(false)}
    }

    /**
     * A totally normal [Area] that does totally normal stuff that is very important.
     */
    private val totallyNormalArea = Area<TokenView>(0,0,200,200).apply {
        this.rotation = 180.0
        onMouseEntered = { this.visual = ImageVisual("steven.png")}
        onMouseExited = { this.visual = Visual.EMPTY}
    }

    /**
     * [BidirectionalMap] to map from the players to the [gridButtons]
     * [PlayerColor] instead of [AbstractPlayer] is used because of redo/undo feature
     */
    private val playerToButtonMap = BidirectionalMap<PlayerColor, Button>()

    /**
     * [TokenView] that is on the right of the [gridButtons] to show which player is the current player
     */
    private val currentPlayerToken = TokenView(120,0,110,110, ImageVisual("Sun.png"))

    /**
     * Button that opens the [PauseScene]
     */
    val pauseButton = Button(
        width = 150, height = 50,
        posX = 1750, posY = 30,
        text = "Pause",
        font = Font(size = 20, fontWeight = Font.FontWeight.BOLD)
    ).apply {
        visual = ColorVisual(245, 127, 23)
    }

    /**
     * Buttons that open the grid scene of the player they are mapped to
     * @see [playerToButtonMap]
     * @see [initGridButtons]
     */
    private val gridButtons = Array(4){
        Button(0,it*150,120,120,"", font = Font(size = 20, fontWeight = Font.FontWeight.BOLD)).apply {
            visual = ColorVisual.GRAY
            isVisible = false
        }
    }



    init {
        this.onKeyPressed = { if (it.keyCode == KeyCode.ESCAPE){ this@BoardScene.tryPauseResumeAI(true) } }
        background = ImageVisual("StolenBackground.png")
        gridButtonLayout.addAll(*gridButtons, currentPlayerToken)
        micButtonLayout.addAll(fillButton, undoButton, redoButton)
        hintBoxPane.addAll(questionMarkTextBoxBorder,questionMarkTextBox)
        addComponents(board, gridButtonLayout, micButtonLayout, pauseButton,questionMarkButton, hintBoxPane,
            totallyNormalArea, resumeAiButton)
    }

    /**
     * Method to completely reset this scene
     */
    private fun resetBoardScene(){
        board.resetBoard()
        hintBoxPane.isVisible = false
        playerToButtonMap.clear()
        gridButtons.forEach { it.isVisible = false }
        if(this.application.gameRunning){
            resumeAiButton.isVisible = false
        }
        this.tryEnableDisableFillButton()
    }

    /**
     * Init this board scene and the [board]
     */
    private fun initBoardScene(){
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        initGridButtons(game)
        board.initBoard()
    }

    /**
     * Make grid buttons visible (corresponding to the number of players), give them the
     * player color and show the player name and his token count. Also add a lambda ([loadGridOnClick])
     * to the onMouseClicked property so the right [GridScene] in the right mode gets opened.
     */
    private fun initGridButtons(game : Game){
        for(i in 0 until game.listOfPlayers.size){
            val player = game.listOfPlayers[i]
            gridButtons[i].apply {
                text = "${player.name}\n (${player.numberOfTokens})"
                visual = player.color.getVisual()
                font = if(player.color == PlayerColor.BLACK){
                    Font(size = 20,color = Color.WHITE, fontWeight = Font.FontWeight.BOLD)
                }else{
                    Font(size = 20, fontWeight = Font.FontWeight.BOLD)
                }
                isVisible = true
                onMouseClicked = {
                    loadGridOnClick(player.color)
                }
            }
            playerToButtonMap.add(player.color, gridButtons[i])
        }
    }

    /**
     * Method used for the [gridButtons] so the button opens the right [GridScene].
     * If the current player clicks on his own grid button while [board] has a tile selected a take tile action is
     * performed and his [GridScene] opens in "Tile place mode". Otherwise, the [GridScene] of the
     * corresponding player just opens in "View mode".
     */
    private fun loadGridOnClick(playerColor: PlayerColor){
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        val playerGrid = application.gridSceneToPlayer.backward(playerColor)

        if(board.hasSelectedTile() && playerColor == game.currPlayer.color){
            val tile = board.getSelectedTile()
            rootService.playerActionService.takeTile(tile)
            playerGrid.setToTilePlaceMode(tile)
        }
        else{
            playerGrid.setToViewMode()
            application.showGameScene(playerGrid)
        }
    }

    /**
     * Sets the position of the [currentPlayerToken] to the current player and update
     * the token count on the [gridButtons].
     */
    private fun updateCurrPlayerAndTokenCount(){
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        val currPlayerButton = playerToButtonMap.forward(game.currPlayer.color)
        currentPlayerToken.posY = currPlayerButton.posY
        for (player in game.listOfPlayers){
            val button = playerToButtonMap.forward(player.color)
            if(player is AI){
                button.text = button.text.replaceAfter("\n", "AI\n(${player.numberOfTokens})")
                if(!this.application.gameRunning){
                    button.text = button.text.replaceAfter("\n", "Paused\n(${player.numberOfTokens})")
                }
            }
            else{
                button.text = button.text.replaceAfter("\n", "(${player.numberOfTokens})")
            }
        }
    }

    /**
     * Enables the [redoButton] and/or [undoButton] if a redo/undo action is possible.
     * Otherwise, disable those buttons.
     */
    private fun tryEnableDisableRedoUndoButton(){
        this.redoButton.isDisabled = true
        this.undoButton.isDisabled = true
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        if(game.nextState != null){
            this.redoButton.isDisabled = false
        }
        if(game.prevState != null){
            this.undoButton.isDisabled = false
        }
    }

    /**
     * Enables the [fillButton] if two or less [TokenView]s are on the [board].
     * Otherwise, disables it.
     */
    private fun tryEnableDisableFillButton(){
        this.fillButton.isDisabled = true
        if (board.numberOfTilesOnOuterCircle() <= 2){
            this.fillButton.isDisabled = false
        }
    }

    /**
     * After starting the game,
     * reset the board scene (so pausing and starting new game works).
     * Then initiate the scene.
     * if the first Player is an AI, his playTrough will be visualized with [aIMove]
     */
    override fun refreshAfterStartGame() {
        this.resetBoardScene()
        this.initBoardScene()
        this.updateCurrPlayerAndTokenCount()
        this.tryEnableDisableFillButton()
        this.tryEnableDisableRedoUndoButton()
        this.tryAIMove()
    }

    /**
     * Load whole scene after loading a game
     */
    override fun refreshAfterLoadGame() {
        this.refreshAfterStartGame()
    }

    /**
     * Load whole scene again after an undo action
     */
    override fun refreshAfterUndo(playerToUndo: AbstractPlayer) {
        this.tryPauseResumeAI(true)
        this.refreshAfterStartGame()
    }

    /**
     * Load whole scene again after a redo action
     */
    override fun refreshAfterRedo(playerToRedo: AbstractPlayer) {
        this.tryPauseResumeAI(true)
        this.refreshAfterStartGame()
    }

    /**
     * After every turn, update the [currentPlayerToken] and the token count.
     * Also enable/disable the [fillButton].
     * If the current Player is an AI his moves will be visualized with [aIMove]
     */
    override fun refreshAfterStartTurn() {
        this.updateCurrPlayerAndTokenCount()
        this.tryEnableDisableFillButton()
        this.tryEnableDisableRedoUndoButton()
        this.tryAIMove()
    }

    /**
     * After a tile gets picked, play an animation where the player token and meeple token on the [board]
     * get updated then open the [GridScene] of the current player.
     * If the currentPlayer is an AI the [GridScene] will be set to a spectorMode in which the tile placement
     * of mentioned player is visualized via [GridScene.setToViewAIMode]
     */
    override fun refreshAfterTilePicked() {
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        val playerGrid = application.gridSceneToPlayer.backward(game.currPlayer.color)
        val playerGridButton = playerToButtonMap.forward(game.currPlayer.color)
        val selectedTileAnimation = board.getSelectedTileMoveAnimation(playerGridButton)
        val playerTokenMeepleAnimation = board.updatePlayerTokenAndMeeple().apply {
            onFinished = {
                this@BoardScene.unlock()
                application.showGameScene(playerGrid)
                if (game.currPlayer is AI) {
                    playerGrid.setToViewAIMode(aiAction[1],aiAction[2])
                    aiAction=IntArray(4)
                }
            }
        }

        //Check if fill tiles action should be called
        if(game.nMiddleTiles == 0 && game.drawStack.size > 0){
            rootService.playerActionService.fillTiles()
        }

        this@BoardScene.lock()
        this@BoardScene.playAnimation( SequentialAnimation( selectedTileAnimation, playerTokenMeepleAnimation) )
    }

    /**
     * Update visuals on the [board] after a fill tile action with an animation.
     */
    override fun refreshAfterFillTile() {
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        this@BoardScene.lock()
        val delay = DelayAnimation(1000).apply {
            onFinished = {
                //IMPORTANT that aiAction[3] is true or else it fails when the AI picks the last tile from the middle
                //=> AI gets 2 calls which annihilates the application
                if(game.currPlayer is AI && aiAction[3]==1){
                    println("AI evaluates filled middle Tiles")
                    this@BoardScene.tryAIMove()
                }
            }
        }
        val animation = board.getFillTilesAnimation().apply {
            onFinished = {
                this@BoardScene.tryEnableDisableFillButton()
                this@BoardScene.unlock()
            }
        }
        this@BoardScene.playAnimation(SequentialAnimation(animation, delay))
    }

    /**
     * Function which calls [AIService.findStep] and handles the returned [aiAction]
     * It first checks whenever or not the AI wants to fill up the Tiles
     * and then picks the selectedTile according to findStep
     * the final placement in the playerGrid is done in [refreshAfterTilePicked]
     *@param game , the current game
     */
    private fun aIMove(game:Game){
        val playerDifficulty = (game.currPlayer as AI).difficulty
        aiAction = AIService(playerDifficulty).findStep(game.currPlayer,rootService)
        if(aiAction[3]==1){
            println("The AI filled the board")
            rootService.playerActionService.fillTiles() //If the AI fills, it needs to be called again,
        } else {                  //because no optimal tile can be determined before all possible tiles are known
            val tile = game.middleTiles[aiAction[0]]
            checkNotNull(tile){"AI-Move determined no tile"}
            board.aiSelectTileView(tile)
            loadGridOnClick(game.currPlayer.color)
        }
    }

    /**
     * Check if the current player is an AI and if he is paused.
     * Do an AI move if possible and Activate/Deactivate components accordingly.
     */
    private fun tryAIMove(){
        val game=rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        if(game.currPlayer is AI){
            this.disableEnableButtonsForAIMove(true)
            if(this.application.gameRunning){
                this.board.animationSpeed = this.aiAnimationSpeed
                val delay = DelayAnimation(500).apply{ onFinished= {
                    aIMove(game)
                    }
                }
                playAnimation(delay)
            }
            else{
                this.resumeAiButton.isDisabled = false
                this.pauseButton.isDisabled = false
                this.micButtonLayout.isDisabled = false
                this.tryEnableDisableFillButton()
            }
        }
        else{
            this.board.animationSpeed = DEFAULT_ANIMATION_SPEED
            this.disableEnableButtonsForAIMove(false)
        }
    }

    private fun disableEnableButtonsForAIMove(disable : Boolean){
        this.micButtonLayout.isDisabled = disable
        if(!disable){
            this.tryEnableDisableFillButton()
        }
        this.questionMarkButton.isDisabled = disable
        this.board.isDisabled = disable
        this.pauseButton.isDisabled = disable
        this.resumeAiButton.isDisabled = disable
    }

    /**
     * Stops all AI from running by setting [SopraApplication.gameRunning] to false or resumes by setting
     * it to true. Pausing can be achieved by a redo/undo action or pressing escape.
     * Resuming is only achieved by pressing the [resumeAiButton].
     */
    private fun tryPauseResumeAI(pause : Boolean){
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        if(game.listOfPlayers.filterIsInstance<AI>().isNotEmpty()){
            if(pause){
                this.application.gameRunning = false
                this.resumeAiButton.isVisible = true
            }
            else{
                this.application.gameRunning = true
                this.resumeAiButton.isVisible = false
                if(game.currPlayer is AI){
                    this.refreshAfterStartTurn()
                }
            }
            this.updateCurrPlayerAndTokenCount()
        }
    }

}