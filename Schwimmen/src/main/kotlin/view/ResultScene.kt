package view

import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import java.awt.Color
import service.RootService

class ResultScene (private val rootService: RootService) : MenuScene(400, 1080), Refreshable {

    private val headlineLabel = Label(
        width = 300, height = 50, posX = 50, posY = 50,
        text = "Result",
        font = Font(size = 25)
    )

    private val p1Score = Label(width = 300, height = 35, posX = 50, posY = 125,font = Font(22))
    private val p2Score = Label(width = 300, height = 35, posX = 50, posY = 160,font = Font(22))
    private val p3Score = Label(width = 300, height = 35, posX = 50, posY = 195,font = Font(22))
    private val p4Score = Label(width = 300, height = 35, posX = 50, posY = 230,font = Font(22))
    private val gameResult = Label(width = 300, height = 35, posX = 50, posY = 265,font = Font(22))

    val quitButton = Button(width = 150, height = 40, posX = 50, posY = 350, text = "Quit",font = Font(22)).apply {
        visual = ColorVisual(Color(221,136,136))
    }

    val newGameButton = Button(width = 150, height = 40, posX = 210, posY = 350, text = "New Game",font = Font(22)).apply {
        visual = ColorVisual(Color(136, 221, 136))
    }

    init {
        opacity = .5
        addComponents(headlineLabel, p1Score, p2Score,p3Score,p4Score, gameResult, newGameButton, quitButton)
    }

    override fun refreshAfterResult(points: List<Double>) {
        val game =  rootService.currentGame
        checkNotNull(game)
        when(points.size){
            2->{  p1Score.text=game.listOfPlayers[0].pName+": "+points[0].toString()
                p2Score.text=game.listOfPlayers[1].pName+": "+points[1].toString()
                gameResult.text= "Winner: "+game.listOfPlayers[points.indexOf(maxOf(points[0],points[1]))].pName
                if(points[0].equals(points[1])){
                    gameResult.text = "It's a Draw !"
                }
            }

            3->{  p1Score.text=game.listOfPlayers[0].pName+": "+points[0].toString()
                p2Score.text=game.listOfPlayers[1].pName+": "+points[1].toString()
                p3Score.text=game.listOfPlayers[1].pName+": "+points[2].toString()
                calculateWinner(points,3)

            }

            4->{  p1Score.text=game.listOfPlayers[0].pName+": "+points[0].toString()
                p2Score.text=game.listOfPlayers[1].pName+": "+points[1].toString()
                p3Score.text=game.listOfPlayers[1].pName+": "+points[2].toString()
                p4Score.text=game.listOfPlayers[1].pName+": "+points[3].toString()
                calculateWinner(points,4)

            }
        }

    }
    private fun calculateWinner(points: List<Double>, players: Int){
        val game=rootService.currentGame
        checkNotNull(game)
        if (players==3){
            val maxPointsIndex = points.indexOf(maxOf(points[0],points[1],points[2]))
            gameResult.text="Winner: " + game.listOfPlayers[maxPointsIndex].pName
            for (i in points.indices){
                if(points[maxPointsIndex].equals(points[i])&& maxPointsIndex!=i){
                    gameResult.text+= game.listOfPlayers[i].pName
                }
            }
            if (points[0].equals(points[1])&&points[2].equals(points[1])){
                gameResult.text = "It's a Draw !"
            }
        }
        if(players==4){
            val maxPointsIndex = points.indexOf(maxOf(points[0],points[1],points[2],points[3]))
            gameResult.text="Winner: " + game.listOfPlayers[maxPointsIndex].pName
            for (i in points.indices){
                if(points[maxPointsIndex].equals(points[i])&& maxPointsIndex!=i){
                    gameResult.text+= game.listOfPlayers[i].pName
                }
            }
            if (points[0].equals(points[1])&&points[2].equals(points[1])&&points[2].equals(points[3])){
                gameResult.text = "It's a Draw !"
            }
        }
    }


}