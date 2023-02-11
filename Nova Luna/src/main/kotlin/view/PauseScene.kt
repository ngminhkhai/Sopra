package view

import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual

/**
 * Pause Scene which one can excess in Game. Here you can choose to either:
 * - Pause the Game
 * - Continue the Game and return to the [BoardScene] via the [continueButton]
 * - Load a different Game from [LoadMenuScene] via the [loadGameButton]
 * - Save the current Game with [SaveMenuScene] via the [saveGameButton]
 * - return to [StartMenuScene] to exit the game or view the HighScores via the [backToMenuButton]
 */
class PauseScene : MenuScene(800, 1080), Refreshable {

    /**
     * "Pause Game" Label at the top of the scene
     */
    private val headlineLabel = Label(
        width = 300, height = 50, posX = 250, posY = 20,
        text = "Pause",
        font = Font(size = 40)
    )

    /**
     * continue Button to resume the game
     * brings you back to the [BoardScene]
     */
    val continueButton = Button(
        width = 200, height = 50,
        posX = 300, posY = 300,
        text = "Continue",
        font = Font(25)
    ).apply {
        visual = ColorVisual(124, 179, 66)
    }

    /**
     * load Button to load a saved game
     * opens [LoadMenuScene]
     */

    val loadGameButton = Button(
        width = 200, height = 50,
        posX = 300, posY = 400,
        text = "Load Game",
        font = Font(25)
    ).apply {
        visual = ColorVisual(255, 235, 59)
        onMouseClicked = {

        }
    }

    /**
     * save game button to save the ongoing game
     * opens [SaveMenuScene]
     */
    val saveGameButton = Button(
        width = 200, height = 50,
        posX = 300, posY = 500,
        text = "Save Game",
        font = Font(25)
    ).apply {
        visual = ColorVisual(3, 155, 229)
    }

    /**
     * back to Menu Button to return to the [StartMenuScene]
     */
    val backToMenuButton = Button(
        width = 200, height = 50,
        posX = 300, posY = 600,
        text = "Back to Menu",
        font = Font(25)
    ).apply {
        visual = ColorVisual(244, 67, 54)
        onMouseClicked = {

        }
    }
    /**
     * Add all the components to the scene and fill out the text fields with default names.
     */
    init {
        opacity = 0.7
        addComponents(headlineLabel,continueButton,saveGameButton, loadGameButton, backToMenuButton)
    }

}