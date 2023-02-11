package view

import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
/**
 * The Start Menu shown at the start of the application; at the end after the results or if the game gets aborted.
 * Has four buttons where the player/s can select whenever to
 * - start a new game : [newGameButton]
 * - load an old game : [loadGameButton]
 * - view Highscores : [highScoreButton]
 * - exit the application :[quitButton].
 */
class StartMenuScene: MenuScene(800, 1080), Refreshable {
    /**
     * "Nova Luna" Label at the top of the scene
     */
    private val headlineLabel = Label(
        width = 300, height = 50, posX = 250, posY = 20,
        text = "Nova Luna",
        font = Font(size = 40)
    )
    /**
     * New Game-Button which calls the set-up menu to create a new game
     */
     val newGameButton = Button(
        width = 200, height = 50,
        posX = 150, posY = 330,
        text = "New Game",
        font = Font(30)
    ).apply {
        visual = ColorVisual(124, 179, 66)
    }

    /**
     * Load Game-Button which loads a previously saved game
     * opens a list of saved games to select from
     */
     val loadGameButton = Button(
        width = 200, height = 50,
        posX = 450, posY = 330,
        text = "Load Game",
        font = Font(30)
    ).apply {
        visual = ColorVisual(255, 235, 59)
        onMouseClicked = {

        }
    }

    /**
     * Highscore-Button which shows the Highscore-list
     */

    val highScoreButton = Button(
        width = 200, height = 50,
        posX = 150, posY = 530,
        text = "Highscore",
        font = Font(30)
    ).apply {
        visual = ColorVisual(245, 127, 23)
        onMouseClicked = {

        }
    }
    /**
     * Quit button that terminates the application.
     */
    val quitButton = Button(
        width = 200, height = 50,
        posX = 450, posY = 530,
        text = "Quit",
        font = Font(30)
    ).apply {
        visual = ColorVisual(244, 67, 54)
    }

    /**
     * Add all the components to the scene.
     */
    init {
        opacity = 0.7
        addComponents(headlineLabel,newGameButton, quitButton, loadGameButton, highScoreButton)
    }


}