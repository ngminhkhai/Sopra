package view

import entity.PlayerColor
import entity.Tile
import service.RootService
import tools.aqua.bgw.animation.*
import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.util.BidirectionalMap
import tools.aqua.bgw.util.Coordinate
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual

/**
 * A class to represent an actual board in nova luna. The board is a [Pane] with the visual of the original nova luna
 * board. This class holds the middle tile views (+ reference to the actual [Tile]), meeple token and player tokens.
 * It provides the ability to update the positions of the token corresponding to the actions in the service layer.
 * Furthermore, this class also provides the ability to select a middle tile (dependent on the meeple position)
 * and to remove the selected tile, while getting the corresponding [Tile].
 * @param rootService RootService connecting it with the service layer
 * @param posX x-Position of this board
 * @param posY y-Position of this board
 */
class Board (
    var rootService: RootService,
    posX: Number = 0,
    posY: Number = 0,
): Pane<ComponentView>(posX = posX, posY = posY, width = 800, height = 800, visual = ImageVisual("board.png") ){

    /**
     * Speed of the played animations (in ms)
     */
    var animationSpeed = DEFAULT_ANIMATION_SPEED

    /**
     * Property to store the selected tile view
     */
    var selectedTile : TileView? = null
    private set

    /**
     * [BidirectionalMap] to store the reference between a [TileView] and the corresponding [Tile]
     */
    private val tileToTileView = BidirectionalMap<Tile, TileView>()

    /**
     * The player [TokenView]s to represent the players on the board (mapped to the actual [PlayerColor] with a [Pair])
     */
    private val playerToken = arrayOf(
        Pair(PlayerColor.BLUE,TokenView(0,0,30,30, ColorVisual.BLUE)),
        Pair(PlayerColor.BLACK, TokenView(0,0,30,30, ColorVisual.BLACK)),
        Pair(PlayerColor.WHITE, TokenView(0,0,30,30, ColorVisual.WHITE)),
        Pair(PlayerColor.ORANGE, TokenView(0,0,30,30, ColorVisual.ORANGE))
    )

    /**
     * [TokenView] to represent meeple on the board
     */
    private val meepleToken = TokenView(0,0,110,110, ImageVisual("meeple.png"))

    /**
     * A [TileStack] that show how many tiles are left in the draw stack.
     */
    private val tileStack = TileStack()

    /**
     * Array of [LinearLayout]s that get placed in a circle in the middle of the board to represent fields on the
     * actual board. Used to store the [TokenView]s that represent the player. By using a [LinearLayout] more than one
     * player token can be shown on the same field.
     */
    private val innerCircle = Array(24){
        LinearLayout<TokenView>(0,0,60,60, 0.5, Visual.EMPTY).apply {
            alignment = Alignment.CENTER_LEFT
        }
    }

    /**
     * Array of [TileContainer]s that get placed in a circle around the board. Used to represent and store
     * the middle tiles.
     */
    private val outerCircle = Array(12){
        TileContainer(0,0, TILE_SIZE, TILE_SIZE, Visual.EMPTY)
    }

    init {
        Utility.circleLayout(Utility.getCenter(this@Board), 260.0, true, *innerCircle)
        Utility.circleLayout(Utility.getCenter(this@Board), 400.0, true,  *outerCircle)
        Utility.moveByCenter(tileStack, Coordinate(400,400))
        this.addAll(*outerCircle, *innerCircle, tileStack)
    }

    /**
     * Check if board has a [TileView] selected
     * @return true if this board has a [TileView] selected
     */
    fun hasSelectedTile() : Boolean{
        return selectedTile != null
    }

    /**
     * @return number of [TileView]s on the outer circle
     */
    fun numberOfTilesOnOuterCircle() : Int{
        return outerCircle.count { it.hasTileView() }
    }

    /**
     * Completely reset the board => Remove all tiles, player tokens and meeple token
     */
    fun resetBoard(){
        tileToTileView.clear()
        selectedTile = null
        outerCircle.forEach { it.clear() }
        innerCircle.forEach { it.clear() }
        tileStack.restoreVisual()
    }

    /**
     * Initiates the board after starting or loading a game. Places [TileView]s corresponding to the middle tiles
     * around the board. Add the player [TokenView]s to the field and the meeple token view.
     * Should be used after starting and loading a game. And after redo/undo actions.
     */
    fun initBoard(){
        this.resetBoard()
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        tileToTileView.clear()
        initTiles()
        outerCircle[game.moonPos].add(meepleToken)
        for(player in game.listOfPlayers.reversed()){
            val token = playerToken.find { it.first == player.color }?.second
            checkNotNull(token){ "Player token should never be null" }
            innerCircle[player.totalCost % 24].add(token)
        }
        tileStack.updateNumberOfTiles(game.drawStack.size)
    }

    /**
     * Deselects a selected [TileView]. Does nothing if no tile view is
     * selected.
     */
    private fun resetSelection(){
        val sView = selectedTile
        if(sView != null){
            sView.posY = 0.0
            selectedTile = null
        }
    }

    /**
     * Fill [outerCircle] with tiles.
     */
    private fun initTiles(){
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        val loader = TileImageLoader()
        for(i in 0..11) {
            val tile = game.middleTiles[i]
            if (tile != null && !outerCircle[i].hasTileView()) {
                val img = ImageVisual(loader.loadTileImage(tile))
                val tileView = TileView(0, 0, TILE_SIZE, TILE_SIZE, img).apply {
                    isDraggable = false
                    onMouseClicked = { trySelectTile(this) }
                }
                tileToTileView.add(tile to tileView)
                outerCircle[i].add(tileView)
            }
        }
        tileStack.updateNumberOfTiles(game.drawStack.size)
    }

    /**
     * Plays an animation where the new [TileView]s move to their position on the outer circle of the board.
     * The starting point of the animation is the middle of the board.
     * This method automatically resets the selected tile and updates the [tileStack].
     */
    fun getFillTilesAnimation() : ParallelAnimation{
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        this.resetSelection()
        val animationList = mutableListOf<ParallelAnimation>()
        val loader = TileImageLoader()
        for(i in 0..11){
            val tile = game.middleTiles[i]
            if(tile != null && !outerCircle[i].hasTileView()){
                val img = ImageVisual(loader.loadTileImage(tile))
                val tileView = TileView(0, 0, TILE_SIZE, TILE_SIZE, img ).apply {
                    isDraggable = false
                    onMouseClicked = { trySelectTile(this) }
                }
                Utility.moveByCenter(tileView, Coordinate(400,400))
                this@Board.add(tileView)
                val moveAnimation = MovementAnimation(
                    tileView,
                    tileView.posX,
                    outerCircle[i].posX,
                    tileView.posY,
                    outerCircle[i].posY,
                    animationSpeed
                )
                val rotAnimation = RotationAnimation(tileView,outerCircle[i].rotation, animationSpeed)
                val endAnimation = ParallelAnimation(moveAnimation,rotAnimation).apply {
                    onFinished = {
                        tileView.removeFromParent()
                        tileToTileView.add(tile to tileView)
                        tileView.posX = 0.0
                        tileView.posY = 0.0
                        outerCircle[i].add(tileView)
                        tileStack.updateNumberOfTiles(game.drawStack.size)
                    }
                }
                animationList.add(endAnimation)
            }
        }
        return ParallelAnimation(*animationList.toTypedArray())
    }

    /**
     * Can be used to update the position of the player token and the meeple token, after a takeTile-action, in form
     * of an animation.
     * @return [SequentialAnimation] of the player token and the meeple token
     */
    fun updatePlayerTokenAndMeeple() : SequentialAnimation{
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        val currPlayerToken = playerToken.find { it.first == game.currPlayer.color }?.second
        checkNotNull(currPlayerToken){ "Player token should never be null" }
        val tokenParent = currPlayerToken.parent
        val meepleParent = meepleToken.parent
        checkNotNull(tokenParent)
        checkNotNull(meepleParent)
        val meepleNewParent = outerCircle[game.moonPos]
        val tokenNewParent = innerCircle[game.currPlayer.totalCost % 24]

        var transVec = Utility.getTransformedTranslationVector(
            meepleParent,
            Coordinate(meepleNewParent.posX, meepleNewParent.posY),
            meepleParent.rotation
        )

        val meepleMoveAnim = ParallelAnimation(
            MovementAnimation(meepleToken, transVec.xCoord, transVec.yCoord, animationSpeed).apply {
                onFinished = {
                    meepleToken.removeFromParent()
                    meepleNewParent.add(meepleToken)
                }
            },
            RotationAnimation(meepleToken, meepleNewParent.rotation - meepleParent.rotation, animationSpeed)
        )

        transVec = Utility.getTransformedTranslationVector(
            tokenParent,
            Coordinate(tokenNewParent.posX, tokenNewParent.posY),
            tokenParent.rotation
        )
        val playerMoveAnim = ParallelAnimation(
            MovementAnimation(currPlayerToken, transVec.xCoord, transVec.yCoord, animationSpeed).apply {
                onFinished = {
                    currPlayerToken.removeFromParent()
                    tokenNewParent.add(currPlayerToken)
                }
            },
            RotationAnimation(currPlayerToken, tokenNewParent.rotation - tokenParent.rotation, animationSpeed)
        )

        return SequentialAnimation(meepleMoveAnim, playerMoveAnim)
    }

    /**
     * Returns an animation where the selected tile is moved to a grid button on the board scene. It is assumed
     * that the grid button is contained in an area/pane (which is contained in the actual board scene).
     * When the animation finishes the selected tile is removed.
     * Should only be used after calling the [getSelectedTile] method or else the selected tile is lost.
     * @param gridButton The grid button the selected tile should move to
     * @return [ParallelAnimation] of selected tile moving and rotating to the grid button
     * @throws IllegalStateException If no tile is selected or if the grid button is not contained in an area/pane
     */
    fun getSelectedTileMoveAnimation(gridButton : ComponentView) : ParallelAnimation{
        val selectedView = selectedTile
        checkNotNull(selectedView)
        val selectedParent = selectedView.parent
        checkNotNull(selectedParent)
        val gridParent = gridButton.parent
        checkNotNull(gridParent)
        //GridButton local coord -> global coord -> local coord in this pane
        val destCoord = Coordinate(
            gridButton.posX + gridParent.posX - this.posX,
            gridButton.posY + gridParent.posY - this.posY
        )
        val transVec = Utility.getTransformedTranslationVector(selectedParent, destCoord, selectedParent.rotation)
        val moveAnim = MovementAnimation(selectedView, transVec.xCoord, transVec.yCoord - selectedView.posY, animationSpeed)
        val rotAnim = RotationAnimation(selectedView, gridButton.rotation - selectedParent.rotation, animationSpeed)
        return ParallelAnimation(moveAnim,rotAnim).apply {
            onFinished = {
                selectedView.removeFromParent()
                tileToTileView.removeBackward(selectedView)
                selectedTile = null
            }

        }
    }

    /**
     * Returns the actual [Tile] this tile view was representing.
     * Should only be used with the [hasSelectedTile] method.
     * @return A [Tile] that was represented by the selected [TileView]
     * @throws IllegalStateException When the board has no selected tile view
     */
    fun getSelectedTile() : Tile{
        val sView = selectedTile
        checkNotNull(sView)
        return tileToTileView.backward(sView)
    }

    /**
     * Selects a tile for the A.I. ([getSelectedTile] method will work, if the tile to select
     * has a valid position).
     * @see [getSelectedTile]
     * @see [getSelectedTileMoveAnimation]
     */
    fun aiSelectTileView(tileToSelect : Tile){
        val tileView = tileToTileView.forward(tileToSelect)
        this.trySelectTile(tileView)
    }


    /**
     * Method used for the selection of the tiles.
     */
    private fun trySelectTile(tileView : TileView){
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        val meeplePos = game.moonPos

        val indexOfArea = outerCircle.indexOfFirst { it.contains(tileView) }
        require(meeplePos in 0..11)
        check(indexOfArea != -1){ "TileView does not exist" }

        var mPos = (meeplePos + 1) % 12
        var nTiles = 0
        while (mPos != indexOfArea){
            if(outerCircle[mPos % 12].hasTileView()){
                nTiles++
            }
            mPos = (mPos + 1) % 12
        }
        if(nTiles < 3){
            val oldSelectedTile = selectedTile
            oldSelectedTile?.reposition(0,0)
            selectedTile = if(oldSelectedTile == tileView){
                null
            } else{
                tileView.reposition(0,-40)
                tileView
            }
        }
    }
}