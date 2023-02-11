package view

import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.visual.ImageVisual


/**
 * TileView used to visualize Tiles.
 * It is a token view with additional functionality.
 */
class TileView  (
    posX: Number = 0,
    posY: Number = 0,
    width: Number = TILE_SIZE,
    height: Number = TILE_SIZE,
    visual: ImageVisual
) : TokenView(posX = posX, posY = posY, width = width, height = height, visual = visual){
    init {
        this.isDraggable = true
        this.onDragGestureStarted = { this.scale(1.2) }
        this.onDragGestureEnded = { _ , _ -> this.scale(1.0) }
    }
}