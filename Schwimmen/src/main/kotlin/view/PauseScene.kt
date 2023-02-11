package view

import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual

/**
 * class for PauseScene
 */

class PauseScene : MenuScene(400, 1080), Refreshable {

    private val headlineLabel = Label(
        width = 300, height = 50, posX = 50, posY = 50,
        text = "Pause Game",
        font = Font(size = 22)
    )

    val backToMenuButton = Button(
        width = 140, height = 35,
        posX = 50, posY = 240,
        text = " Back to Menu"
    ).apply {
        visual = ColorVisual(221, 136, 136)
    }

    val continueButton = Button(
        width = 140, height = 35,
        posX = 210, posY = 240,
        text = "Continue"
    ).apply {
        visual = ColorVisual(136, 221, 136)

    }
    init {
        addComponents(
            headlineLabel,
            backToMenuButton,
            continueButton
        )
    }
}