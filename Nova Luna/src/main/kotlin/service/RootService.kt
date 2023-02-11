package service

import entity.NovaLunaApplication
import view.Refreshable
import java.io.Serializable

/**
 * service that is connected to every part of infrastructure
 *
 * @property gameService to control the game events
 * @property playerActionService to control the player actions
 */
class RootService:Serializable  {
    val gameService = GameService(this)
    val playerActionService = PlayerActionService(this)
    val ioService = IOService(this)
    //var currentGame : Game? = null
    //weil die referenz zur novalunaApplication fehlt bekommt der rootservice die referenz zum highscore
    //var highScores = Highscore()
    var novaLunaApplication = NovaLunaApplication()

    /**
     * Function to add the actionlistener
     */
    fun addRefreshables(vararg newRefreshables: Refreshable){
        newRefreshables.forEach {
            this.gameService.addRefreshable(it)
            this.playerActionService.addRefreshable(it)
            this.ioService.addRefreshable(it)
        }
    }

}