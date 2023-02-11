package view

import service.RootService
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual

/**
 * class for the StartMenu
 */

class StartMenuScene (private val rootService: RootService) : MenuScene(400, 1080), Refreshable {
    private var nameList = mutableListOf<String>()
    private val headlineLabel = Label(
        width = 300, height = 50, posX = 50, posY = 50,
        text = "Start New Game",
        font = Font(size = 22)
    )
    private val p1Label = Label(
        width = 100, height = 35,
        posX = 50, posY = 125,
        text = "Player 1:",
        font = Font(size = 22)
    )
    private val p1Input: TextField = TextField(
        width = 200, height = 35,
        posX = 150, posY = 125,
        text = ""
    ).apply {
        onKeyTyped = {
            startButton.isDisabled = checkBlankNames()
            names()
        }
    }

    private val p2Label = Label(
        width = 100, height = 35,
        posX = 50, posY = 180,
        text = "Player 2:",
        font = Font(size = 22)
    )

    // type inference fails here, so explicit  ": TextField" is required
    // see https://discuss.kotlinlang.org/t/unexpected-type-checking-recursive-problem/6203/14
    private val p2Input: TextField = TextField(
        width = 200, height = 35,
        posX = 150, posY = 180,
        text = ""
    ).apply {
        onKeyTyped = {
            startButton.isDisabled = checkBlankNames()
            names()
        }
    }

    private val p3Label = Label(
        width = 100, height = 35,
        posX = 50, posY = 235,
        text = "Player 3:",
        font = Font(size = 22)
    )
    private val p3Input: TextField=TextField(
        width=200, height = 35,
        posX = 150, posY =235,
        text = ""
    ).apply {
        onKeyTyped={
            startButton.isDisabled=checkBlankNames()
            names()
        }
    }

    private val p4Label = Label(
        width = 100, height = 35,
        posX = 50, posY = 290,
        text = "Player 4:" ,
        font = Font(size = 22)
    )
    private val p4Input: TextField=TextField(
        width=200, height = 35,
        posX = 150, posY =290,
        text = ""
    ).apply {
        onKeyTyped={
            startButton.isDisabled=checkBlankNames()
            names()
        }
    }

    val quitButton = Button(
        width = 150, height = 35,
        posX = 50, posY = 400,
        text = "Quit",
        font = Font(size = 22)
    ).apply {
        visual = ColorVisual(221, 136, 136)
    }

    private val startButton = Button(
        width = 150, height = 35,
        posX = 210, posY = 400,
        text = "Start",
        font = Font(size = 22)
    ).apply {
        visual = ColorVisual(136, 221, 136)
        onMouseClicked = {
            rootService.gameService.startNewGame(
                names()

            )
        }
    }

    init {
        opacity = .5
        startButton.isDisabled=true
        addComponents(
            headlineLabel,
            p1Label, p1Input,
            p2Label, p2Input,
            p3Label, p3Input,
            p4Label, p4Input,
            startButton, quitButton
        )
    }

    /**
     * checks which textFields where left empty to see if enough players are participating
     */
    private fun checkBlankNames():Boolean{
        var check = false
        if (p1Input.text.isBlank()&&p2Input.text.isBlank()&&p3Input.text.isBlank()){
            check=true
        }
        else if (p1Input.text.isBlank()&&p2Input.text.isBlank()&&p4Input.text.isBlank()){
            check=true
        }
        else if (p1Input.text.isBlank()&&p3Input.text.isBlank()&&p4Input.text.isBlank()){
            check=true
        }
        else if (p2Input.text.isBlank()&&p3Input.text.isBlank()&&p4Input.text.isBlank()){
            check=true
        }
        return check
    }

    /**
     * puts all Names on a List
     */
    private fun names():List<String>{
        nameList= mutableListOf<String>()
        if (p1Input.text.isNotBlank()){
            nameList.add(p1Input.text.trim())
        }
        if (p2Input.text.isNotBlank()){
            nameList.add(p2Input.text.trim())
        }
        if (p3Input.text.isNotBlank()){
            nameList.add(p3Input.text.trim())
        }
        if (p4Input.text.isNotBlank()){
            nameList.add(p4Input.text.trim())
        }
        return nameList
    }

}