package view

import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual


class BlackScreen (): MenuScene(1920, 1080), Refreshable {

    private val headlineLabel = Label(
        width = 300, height = 50, posX = 50, posY = 50,
        text = "Next Player ready up",
        font = Font(size = 220)
    )


    val clickButton = Button(
        width = 1920, height = 1080,
        posX = 0, posY = 0,
        text = " Next Player \n Click to Continue",
        font = Font(size = 150),

    ).apply {
        visual = ColorVisual(30, 30, 30)

    }

    init {
        opacity = 1.0
        addComponents(
            headlineLabel,
            clickButton
        )
    }
}