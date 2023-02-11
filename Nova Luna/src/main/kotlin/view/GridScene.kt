package view

import entity.*
import service.RootService
import tools.aqua.bgw.animation.*
import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.DynamicComponentView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.util.Coordinate
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import kotlin.math.max
import kotlin.math.min

/**
 * Grid Scene representing the [Grid] of an [AbstractPlayer]. It is possible to load in a grid from a player and to place
 * tiles on the grid and submitting it to the service layer. The grid scene can be opened in "view mode" ([setToViewMode])
 * where just the grid of the player is shown and a "back to board" button or in "tile to place mode" ([setToTilePlaceMode])
 * where an actual tile can be placed and submitted and the turn can be ended.
 */
class GridScene(private val rootService: RootService) : BoardGameScene(1920, 1080), Refreshable {

    /**
     * Speed of the played animations (in ms)
     */
    var animationSpeed = 400

    /**
     * The delay before the AI ends the turn (in ms)
     */
    var aiDelayToEndTurn = 1500

    /**
     * Flag to keep track if this scene has a tile view. Used to make [tryAcceptDrop] more simple.
     */
    private var hasTileView = false

    /**
     * The [TileView] that gets dragged and dropped on the grid
     */
    private var tileViewToPlace : TileView ? = null

    /**
     * The actual [Tile] that gets handed to the Service Layer after submitting
     */
    private var actualTileToPlace : Tile ? = null

    /**
     * The position of the [Tile]/[TileView] that gets submitted
     */
    private var posToSubmit = Coordinate(-1,-1)

    /**
     * Submit button. Only shown when grid is opened in tile place mode.
     * Only active when a [TileView] has been placed on the grid.
     * Clicking it submits the [Tile].
     */
    private val submitButton = Button(
        posX = 1700,
        posY = 490,
        width = 120,
        height = 60,
        text = "Submit",
        font = Font(size = 24, fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual(66, 201, 120)
    ).apply {
        isVisible = false
        onMouseClicked = {this@GridScene.submit()}
    }

    /**
     * End turn button. Only shown when grid is opened in tile place mode and
     * the submit button has been clicked. Ends the current players turn =>
     * Calls endTurn() on the service layer.
     */
    private val endTurnButton = Button(
        posX = 1700,
        posY = 490,
        width = 120,
        height = 60,
        text = "End Turn",
        font = Font(size = 20, fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual.RED
    ).apply {
        isVisible = false
        onMouseClicked = {
            rootService.playerActionService.endTurn()
        }
    }

    /**
     * Back to board button. Only shown when grid is opened in view mode.
     * Brings you back to the [BoardScene]
     */
    val backButton = Button(
        posX = 1700,
        posY = 490,
        width = 120,
        height = 60,
        text = "Back to\n Board",
        font = Font(size = 18, fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual.RED
    ).apply {
    }

    /**
     * Labels A,B,C... on top of the grid
     */
    private val topLabels = Array(GRID_NUMBER_OF_COLS){
        Label(
            250 + it * TILE_SIZE, 10, TILE_SIZE,50, "${(it+65).toChar()}",
            Font(20, fontWeight = Font.FontWeight.BOLD)
        )
    }

    /**
     * Labels 1,2,3... on the left of the grid
     */
    private val sideLabels = Array(GRID_NUMBER_OF_ROWS){
        Label(200, 60 + it * TILE_SIZE, 50, TILE_SIZE, "${it+1}", Font(20, fontWeight = Font.FontWeight.BOLD))
    }


    /**
     * [Pane] to hold all the [TileContainer] of the grid
     */
    private val gridContainer = Pane<TileContainer>(
        posX = 250,
        posY = 60,
        width = GRID_NUMBER_OF_ROWS * TILE_SIZE,
        height = GRID_NUMBER_OF_COLS * TILE_SIZE
    )

    /**
     * Actual 9x9 grid consisting of [TileContainer]. Placing a tile in a container enables the submit button, and
     * it sets the [posToSubmit].
     */
    private val tiles = Array(GRID_NUMBER_OF_ROWS){ i ->
        Array(GRID_NUMBER_OF_COLS){ j -> TileContainer(posX = (i % GRID_NUMBER_OF_COLS)*TILE_SIZE,
        posY = j*TILE_SIZE,
        width = TILE_SIZE,
        height = TILE_SIZE).apply {
            showSelection()
            acceptDropWhen = { this@GridScene.tryAcceptDrop(i,j) }
            onAdd = {
                this@GridScene.submitButton.isDisabled = false
                posToSubmit = Coordinate(i, j)
                }
            }
        }
    }

    /**
     * [Pane] for the hintbox consisting of [questionMarkTextBox] and [questionMarkTextBoxBorder].
     * Makes placing and visibility easier
     */
    val hintBoxPane = Pane<Button>(1400,680, 370,190).apply { isVisible = false }

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
    val questionMarkTextBox = Button(10,10, 350,170, "For advice hit the question mark",
        Font(size = 24), isWrapText = true).apply {
    }

    init {
        background = ImageVisual("StolenBackground.png")
        for(grid in tiles){
            for(tile in grid){
                gridContainer.add(tile)
            }
        }
        hintBoxPane.addAll(questionMarkTextBoxBorder, questionMarkTextBox)
        addComponents(gridContainer, submitButton, endTurnButton, backButton, *topLabels, *sideLabels, hintBoxPane)
    }

    /**
     * Completely reset the grid scene
     */
    fun resetGrid(){
        for(i in 0 until GRID_NUMBER_OF_ROWS) {
            for (j in 0 until GRID_NUMBER_OF_COLS) {
                this.tiles[i][j].resetContainer()
                this.tiles[i][j].showSelection()
            }
        }
        this.components.forEach {
            if(it is TileView){
                removeComponents(it)
            }
        }
        animationSpeed = 400
        this@GridScene.submitButton.isDisabled = false
        this@GridScene.endTurnButton.isDisabled = false
        hintBoxPane.isVisible = false
        hasTileView = false
        tileViewToPlace = null
        actualTileToPlace = null
        this.setToViewMode()
    }

    /**
     * Should ALWAYS be called (or [setToTilePlaceMode]) BEFORE showing this game scene.
     * Only show the grid and the "back to board" button.
     */
    fun setToViewMode(){
        this.enableCorrectButtons(true)
    }

    /**
     * Should ALWAYS be called (or [setToViewMode]) when showing this game scene.
     * Show the grid and the tile you want to place.
     * If this method gets called it possible for the player to place the specified tile on the grid, to submit it
     * and to end his turn.
     * @param newTile The actual [Tile] you want to place.
     */
    fun setToTilePlaceMode(newTile : Tile){
        this.enableCorrectButtons(false)
        actualTileToPlace = newTile
        val tImgLoader = TileImageLoader()
        val tileView = TileView(posX = 1300, posY = 460, visual = ImageVisual(tImgLoader.loadTileImage(newTile)))
        this.addComponents(tileView)
    }

    /**
     * Called when AI Player moves
     * After AI Player selects a tile from the [Board] his playerGrid will open and this fun will be called
     * it lock the scene for any Player Input and simply shows the [getTileToGridAnim],
     * after the Animation is played the placement will be submitted via [submit], triggering the end of this players
     * turn
     * @param x-Coordinate of the [TileContainer] in the [Grid]
     * @param y-Coordinate of the [TileContainer] in the [Grid]
     */
    fun setToViewAIMode(x:Int,y:Int){
        this@GridScene.lock()
        this.submitButton.isDisabled = true
        this.endTurnButton.isDisabled = true
        val tileView = this.components.last()
        if (tileView is TileView){
        val moveAnim = this.getTileToGridAnim(tileView,tiles[x][y]).apply {
            onFinished={
                if (tryAcceptDrop(x,y)){
                    posToSubmit= Coordinate(x,y)
                    this@GridScene.removeComponents(tileView)
                    tiles[x][y].add(tileView)
                    this@GridScene.submit()
                    this@GridScene.unlock()
                    playAnimation(DelayAnimation(aiDelayToEndTurn).apply {
                        onFinished={
                            this@GridScene.submitButton.isDisabled = false
                            this@GridScene.endTurnButton.isDisabled = false
                            rootService.playerActionService.endTurn()
                            }
                        }
                    )
                }
            }
        }
        playAnimation(moveAnim)
        }
    }

    /**
     * Loads in a representation of the [Grid] of the player. Sets this player as the owner of the grid.
     * Should only be called once after starting or loading the game. Or after redo/undo actions.
     * @param player [AbstractPlayer] whose grid gets represented
     */
    fun loadGridFromPlayer(player : AbstractPlayer){
        val tImgLoader = TileImageLoader()
        for(i in 0 until GRID_NUMBER_OF_ROWS){
            for(j in 0 until GRID_NUMBER_OF_COLS){
                this.tiles[i][j].resetContainer()
                val playerTile = player.tileGrid.tiles[i][j]
                if(playerTile != null){
                    val tileView = TileView(posX = 0,posY = 0, visual = ImageVisual(tImgLoader.loadTileImage(playerTile)))
                    tileView.isDraggable = false
                    this.tiles[i][j].add(tileView)
                    val finishedTasksIndices = playerTile.tasks.withIndex()
                        .filter { it.value.isFinished }.map { it.index }.toIntArray()
                    if(finishedTasksIndices.isNotEmpty()){
                        this.tiles[i][j].showTaskToken(player.color, *finishedTasksIndices)
                    }
                    hasTileView = true
                }
            }
        }
    }

    /**
     * Enable buttons depending on which mode this grid is in.
     */
    private fun enableCorrectButtons(viewMode : Boolean){
        submitButton.isVisible = false
        endTurnButton.isVisible = false
        backButton.isVisible = false
        if(viewMode){
            backButton.isVisible = true
        }
        else{
            submitButton.isVisible = true
            submitButton.isDisabled = true
        }
    }

    /**
     * Method for checking if a [TileContainer] is allowed to take the tile. Only allowed when grid is empty
     * or the vertical/horizontal neighbors have a [TileView]
     * @param row The row of the [TileContainer] the [TileView] should be dropped in
     * @param col The column of the [TileContainer] the [TileView] should be dropped in
     */
    private fun tryAcceptDrop(row : Int, col : Int) : Boolean{
        var flag = false
        if(!this.hasTileView){
            flag = true
        }
        if(tiles[row][max(0, col-1)].hasTileView() || tiles[row][min(8,col + 1)].hasTileView()){
            flag = true
        }
        if(tiles[max(0, row-1)][col].hasTileView() || tiles[min(8, row+1)][col].hasTileView()){
            flag = true
        }
        return flag
    }

    /**
     * Submits the [actualTileToPlace] to the service layer and disables the placed [TileView].
     * Enables the [endTurnButton] and resets [tileViewToPlace] and [actualTileToPlace].
     * @throws IllegalStateException When there is no tile to place ,or it is out of bounds.
     */
    private fun submit(){
        checkNotNull(actualTileToPlace){ "No tile to submit"}
        check(posToSubmit.xCoord > -1 && posToSubmit.yCoord > -1){ "Wrong position to submit" }

        val placedTile = tiles[posToSubmit.xCoord.toInt()][posToSubmit.yCoord.toInt()].getTileView()
        checkNotNull(placedTile){ "Placed tile view should never be null" }

        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)

        placedTile.isDraggable = false
        submitButton.isVisible = false
        endTurnButton.isVisible = true
        rootService.playerActionService.placeOnGrid(
            actualTileToPlace, posToSubmit.xCoord.toInt(), posToSubmit.yCoord.toInt()
        )
        tileViewToPlace = null
        actualTileToPlace = null
        hasTileView = true
    }

    /**
     * Updates all the task token (see [TileContainer]) with an animation.
     * Should only be called after a tile place action.
     */
    fun placeTaskToken() {
        val toAnimateList = mutableListOf<SequentialAnimation>()
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        this.lock()
        for(i in 0 until GRID_NUMBER_OF_ROWS) {
            for (j in 0 until GRID_NUMBER_OF_COLS) {
                val tile = game.currPlayer.tileGrid.tiles[i][j]
                if(tiles[i][j].hasTileView() && tile != null && tile.tasks.any{it.isFinished}){
                    val finishedTasksIndices = tile.tasks.withIndex()
                        .filter { it.value.isFinished }.map { it.index }.toIntArray()
                    if(tiles[i][j].canShowTaskToken(*finishedTasksIndices)){
                        val firstRotAnim = RotationAnimation<ComponentView>(tiles[i][j], 1080.0, animationSpeed)
                            .apply {
                            onFinished = {tiles[i][j].showTaskToken(game.currPlayer.color,*finishedTasksIndices)}
                        }
                        val secondRotAnim = RotationAnimation<ComponentView>(tiles[i][j], 1080.0, animationSpeed)
                        toAnimateList.add(SequentialAnimation(firstRotAnim, secondRotAnim))
                    }
                }
            }
        }
        if(toAnimateList.isNotEmpty()){
            this.playAnimation(
                SequentialAnimation(*toAnimateList.toTypedArray()).apply {
                    onFinished = { this@GridScene.unlock() }
                }
            )
        }
        else{
            this@GridScene.unlock()
        }
    }

    /**
     * Plays an animation where the tileView moves to the given place in the Grid (tContainer).
     * @param tileView which will be placed in the given tileContainer
     * @param tContainer the tileContainer in which the tileView will be placed in
     * @return [MovementAnimation] which visualizes the placement of the Tile in the Grid
     */
    private fun getTileToGridAnim(tileView: TileView,tContainer:TileContainer): MovementAnimation<DynamicComponentView> {
        val destCoordinate =Coordinate(
            (tContainer.posX % TILE_SIZE)* GRID_NUMBER_OF_COLS,
            tContainer.posY% TILE_SIZE)
       return MovementAnimation.toComponentView(tileView,tContainer,this@GridScene, animationSpeed + 200).apply {
           tileView.reposition(destCoordinate.xCoord, destCoordinate.yCoord)
       }
   }

}