package view

import entity.*
import tools.aqua.bgw.components.gamecomponentviews.CardView
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.util.BidirectionalMap
import tools.aqua.bgw.visual.ColorVisual
import service.RootService
import tools.aqua.bgw.components.container.CardStack
import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ImageVisual

/**
 * class for GameScene
 */
class GameScene (private val rootService: RootService) : BoardGameScene(1920, 1080), Refreshable {
    //Positions of Cards and Players
    private val drawStack =  LabeledStackView(posX = 1300, posY = 400, "drawStack")
    private val cardImageLoader = CardImageLoader()
    private var flip = true
    private var drawStackSize= Label(posX = 1322, posY = 580, width= 100, height=100, font = Font(30))
    //temporary cards for changeOneCard
    private var selectedMidCard = Card(CardSuit.HEARTS,CardValue.TEN)
    private var selectHandCard = Card(CardSuit.HEARTS,CardValue.TEN)
    // Boolean to check if Selection of Cards is Done
    private var selectionDoneM=false
    private var selectionDoneH=false
    //Labels for Players
    private var currentPlayer= Label(posX = 900,
        posY = 1000,font = Font(25),width = 200.0)
    private var player2= Label(posX = 900,
        posY = 50,font = Font(25),width = 200.0)
    private var player3= Label(posX = 1540 ,
        posY = 570,font = Font(25),width = 200.0)
    private var player4= Label(posX = 120,
        posY = 570,font = Font(25),width = 200.0)
    // visualCards for other player
    private val visualPlayer2 : LinearLayout<CardView> = LinearLayout(
        height = 220,
        width = 250,
        posX = 850,
        posY = 100,
        spacing = -80,
        alignment = Alignment.CENTER,
        visual = ColorVisual(255, 255, 255, 50)
    )
    private val visualPlayer3 : LinearLayout<CardView> = LinearLayout(
        height = 220,
        width = 250,
        posX = 1500,
        posY = 350,
        spacing = -80,
        alignment = Alignment.CENTER,
        visual = ColorVisual(255, 255, 255, 50)
    )
    private val visualPlayer4 : LinearLayout<CardView> = LinearLayout(
        height = 220,
        width = 250,
        posX = 100,
        posY = 350,
        spacing = -80,
        alignment = Alignment.CENTER,
        visual = ColorVisual(255, 255, 255, 50)
    )
    //middleCards
    private var middleCards: LinearLayout<CardView> = LinearLayout(
        height = 220,
        width = 450,
        posX = 735,
        posY = 500,
        spacing = 0,
        alignment = Alignment.CENTER,
        visual = ColorVisual(255, 255, 255, 50)
    )
    //currentPlayer
    private var currentPlayerHand: LinearLayout<CardView> = LinearLayout(
        height = 220,
        width = 450,
        posX = 730,
        posY = 750,
        spacing = 0,
        alignment = Alignment.CENTER,
        visual = ColorVisual(255, 255, 255, 50)

    )

    //Buttons for playerAction
    private var changeAllCardsButton = Button(
                width = 200, height = 40,
                posX = 200, posY = 700,
                text = "switch all cards",
                font = Font(size = 23)
            ).apply{
                visual=(ColorVisual(255,100,0))
                onMouseClicked ={
                    rootService.playerActionService.changeAllCards()
                }
    }

    private val changeOneCardsButton = Button(
        width = 200, height = 40,
        posX = 200, posY = 760,
        text = "switch one card",
        font = Font(size = 23)
    ).apply{
        visual=(ColorVisual(255,100,0))
        onMouseClicked ={
            if(selectionDoneM&&selectionDoneH){
                rootService.playerActionService.changeSingleCard(selectHandCard,selectedMidCard)
                disableSelectedButtons()
            }
            this.isDisabled=true
            midOne.isDisabled=false
            midTwo.isDisabled=false
            midThree.isDisabled=false
            handOne.isDisabled=false
            handTwo.isDisabled=false
            handThree.isDisabled=false
        }
    }

    private val passButton = Button(
        width = 200, height = 40,
        posX = 200, posY = 820,
        text = "pass",
        font = Font(size = 25)
    ).apply{
        visual=(ColorVisual(255,100,0))
        onMouseClicked ={
            rootService.playerActionService.pass()
        }
    }
    private val knockButton = Button(
        width = 200, height = 40,
        posX = 200, posY = 880,
        text = "knock",
        font = Font(size = 25)
    ).apply{
        visual=(ColorVisual(255,100,0))
        onMouseClicked ={
            rootService.playerActionService.knock()
        }
    }
    private val endTurnButton = Button(
        width = 200, height = 40,
        posX = 1500, posY = 770,
        text = "end Turn",
        font = Font(size = 25)
    ).apply{
        visual=(ColorVisual(221, 136, 136))
        if(!rootService.playerActionService.playerActionCalled){
            isDisabled=true
        }
        onMouseClicked={
            rootService.playerActionService.endTurn()
        }
    }
    val pauseButton = Button(
        width = 140, height = 35,
        posX = 1570, posY = 80,
        text = "Pause",
        font = Font(size = 22)
    ).apply{
        visual=(ColorVisual(136,136,200))
    }

    /**
     * all Buttons used to select cards for changeOneCard
     */
    private val midOne = Button(
        width = 130, height =200,
        posX = 765, posY = 510,
        text = "",
    ).apply{
        opacity=0.1
        isDisabled=true
        onMouseClicked={
            val game=rootService.currentGame
            checkNotNull(game)
            selectedMidCard=game.middleCards[0]
            selectionDoneM=true
            checkSelected()
        }
    }
    private val midTwo = Button(
        width = 130, height =200,
        posX = 895, posY = 510,
        text = "",
    ).apply{
        opacity=0.1
        isDisabled=true
        onMouseClicked={
            val game=rootService.currentGame
            checkNotNull(game)
            selectedMidCard=game.middleCards[1]
            selectionDoneM=true
            checkSelected()
        }
    }
    private val midThree = Button(
        width = 130, height =200,
        posX = 1025, posY = 510,
        text = "",
    ).apply{
        opacity=0.1
        isDisabled=true
        onMouseClicked={
            val game=rootService.currentGame
            checkNotNull(game)
            selectedMidCard=game.middleCards[2]
            selectionDoneM=true
            checkSelected()
        }

    }
    private val handOne = Button(
        width = 130, height =200,
        posX = 760, posY = 760,
        text = "",
    ).apply{
        opacity=0.1
        isDisabled=true
        onMouseClicked={
            val game=rootService.currentGame
            checkNotNull(game)
            selectHandCard=game.getCurrentPlayer().handCards[0]
            selectionDoneH=true
            checkSelected()
        }
    }
    private val handTwo = Button(
        width = 130, height =200,
        posX = 890, posY = 760,
        text = "",
    ).apply{
        opacity=0.1
        isDisabled=true
        onMouseClicked={
            val game=rootService.currentGame
            checkNotNull(game)
            selectHandCard=game.getCurrentPlayer().handCards[1]
            selectionDoneH=true
            checkSelected()
        }
    }
    private val handThree = Button(
        width = 130, height =200,
        posX = 1020, posY = 760,
        text = "",
    ).apply{
        opacity=0.1
        isDisabled=true
        onMouseClicked={
            val game=rootService.currentGame
            checkNotNull(game)
            selectHandCard=game.getCurrentPlayer().handCards[2]
            selectionDoneH=true
            checkSelected()
        }
    }
    /**
     * structure to hold pairs of (card, cardView) that can be used
     *
     * 1. to find the corresponding view for a card passed on by a refresh method (forward lookup)
     *
     * 2. to find the corresponding card to pass to a service method on the occurrence of
     * ui events on views (backward lookup).
     */
    private val cardMap: BidirectionalMap<Card, CardView> = BidirectionalMap()

    /**
     * Constructor which adds all the Components
     */
    init {
        // dark green for "Casino table" flair
        background = ColorVisual(108, 168, 59)
    }
    /**
     * Initializes the complete GUI, i.e. with all Positions of Cards,Players and their Labels
     *  The views of drawStack, middleStack and Player1, Player2, Player3, Player4 are set
     *  clears any previous gameComponents
     */
    override fun refreshAfterStartNewGame() {
        val game = rootService.currentGame
        checkNotNull(game) { "No started game found." }
        clearComponents()
        addComponents(
            changeAllCardsButton,
            changeOneCardsButton,
            passButton,
            knockButton,
            pauseButton,
            endTurnButton,
            drawStack,
            currentPlayerHand,
            middleCards,
            drawStackSize,
            currentPlayer,
            midOne,
            midTwo,
            midThree,
            handOne,
            handTwo,
            handThree
        )
        initializeOpenCards(game.middleCards,middleCards,cardImageLoader,flip)
        initializeOpenCards(game.getCurrentPlayer().handCards,currentPlayerHand,cardImageLoader,flip)
        initializeViewStack(game.drawStack.cards,drawStack,cardImageLoader)
        drawStackSize.apply { text ="["+game.drawStack.size.toString()+"]"}
        currentPlayer.apply { text=game.getCurrentPlayer().pName }
        when(game.listOfPlayers.size){
            2-> {
                player2.apply { text = game.listOfPlayers[1].pName }
                addComponents(player2)
                addComponents(visualPlayer2)
                initializeOpenCards(game.listOfPlayers[1].handCards,visualPlayer2,cardImageLoader,false)
            }
            3->{
                player3.apply {text=game.listOfPlayers[1].pName}
                player4.apply {text=game.listOfPlayers[2].pName}
                addComponents(player3)
                addComponents(player4)
                addComponents(visualPlayer3)
                addComponents(visualPlayer4)
                initializeOpenCards(game.listOfPlayers[1].handCards,visualPlayer3,cardImageLoader,false)
                initializeOpenCards(game.listOfPlayers[2].handCards,visualPlayer4,cardImageLoader,false)
            }

            4->{
                player2.apply {text=game.listOfPlayers[2].pName}
                player3.apply {text=game.listOfPlayers[1].pName}
                player4.apply {text=game.listOfPlayers[3].pName}
                addComponents(player2)
                addComponents(player3)
                addComponents(player4)
                addComponents(visualPlayer2)
                addComponents(visualPlayer3)
                addComponents(visualPlayer4)
                initializeOpenCards(game.listOfPlayers[1].handCards,visualPlayer3,cardImageLoader,false)
                initializeOpenCards(game.listOfPlayers[2].handCards,visualPlayer2,cardImageLoader,false)
                initializeOpenCards(game.listOfPlayers[3].handCards,visualPlayer4,cardImageLoader,false)
            }
        }
    }

    /**
     * checks if one has selected a midCard and a handCard for changeOneCard
     */
    private fun checkSelected(){
        if(selectionDoneM&&selectionDoneH){
            changeOneCardsButton.isDisabled=false
        }
    }
    /**
     * private function to disable the cardSelectButtons for changeOneCard
     */
    private fun disableSelectedButtons(){
        midOne.isDisabled=true
        midTwo.isDisabled=true
        midThree.isDisabled=true
        handOne.isDisabled=true
        handTwo.isDisabled=true
        handThree.isDisabled=true
        selectionDoneH=false
        selectionDoneM=false
    }
    /**
     * clears [stackView], adds a new [CardView] for each
     * element of [stack] onto it, and adds the newly created view/card pair
     * to the global [cardMap].
     */
    private fun initializeViewStack(stack: MutableList<Card>, stackView: CardStack<CardView>, cardImageLoader: CardImageLoader) {
        stackView.clear()
        stack.forEach { card ->
            val cardView = CardView(
                height = 200,
                width = 130,
                front = ImageVisual(cardImageLoader.frontImageFor(card.suit, card.value)),
                back = ImageVisual(cardImageLoader.backImage)
            )
            stackView.add(cardView)
            cardMap.add(card to cardView)

        }
    }

    /**
     * clears [stackView], adds a new [CardView] for each
     * element of [cards] onto it, and adds the newly created view/card pair
     * to the global [cardMap].
     * @param cards which are to be added in stackView
     * @param stackView which cards are to be added to
     * @param cardImageLoader creates cardsImage
     * @param flip true if front of Cards should be displayed, else backside is shown
     */
    private fun initializeOpenCards(cards: ArrayList<Card>, stackView: LinearLayout<CardView>, cardImageLoader: CardImageLoader, flip: Boolean) {
        stackView.clear()

        cards.forEach { card ->
            val cardView = CardView(
                height = 200,
                width = 130,
                front = ImageVisual(cardImageLoader.frontImageFor(card.suit, card.value)),
                back = ImageVisual(cardImageLoader.backImage)
            )
            if (flip){
            cardView.showFront()}
            stackView.add(cardView)
            cardMap.add(card to cardView)
        }
    }

    /**
     * disables ActionButton and unlocks endTurn Button
     */
    private fun playerMoveOver(){
        endTurnButton.isDisabled=false
        passButton.isDisabled=true
        knockButton.isDisabled=true
        changeOneCardsButton.isDisabled=true
        changeAllCardsButton.isDisabled=true
    }

    /**
     * shifts the playersLabels name + still have to figure out how to remove the passed
     */
    private fun reloadPlayerLabels(){
        val game = rootService.currentGame

        checkNotNull(game)

        val tmpLabel=currentPlayer.text
        when(game.listOfPlayers.size){
            2-> {
                currentPlayer.text = player2.text
                player2.text=tmpLabel
            }
            3 -> {
                currentPlayer.text=player3.text
                player3.text=player4.text
                player4.text=tmpLabel
            }
            4->{
                currentPlayer.text=player3.text
                player3.text=player2.text
                player2.text=player4.text
                player4.text=tmpLabel
            }
        }
    }

    /**
     * changes playerLabels
     */
    override fun refreshAfterEndTurn() {
        val game = rootService.currentGame
        checkNotNull(game) { "No started game found." }
        reloadPlayerLabels()

    }
    /**
     * Refreshes the Views of changed [middleCards] and handCards of current Player
     * disables all ActionButtons to hinder the player from doing actions twice
     */
    override fun refreshAfterCardChange() {
        val game = rootService.currentGame
        checkNotNull(game) { "No started game found." }
        playerMoveOver()
        initializeOpenCards(game.middleCards,middleCards,cardImageLoader,true)
        initializeOpenCards(game.getCurrentPlayer().handCards,currentPlayerHand,cardImageLoader,true)
    }

    /**
     * Refreshes the Views of changed midCards,handCards and drawStack after moving onto the next Player
     */
    override fun refreshAfterStartTurn() {
        val game =rootService.currentGame
        checkNotNull(game)
        endTurnButton.isDisabled=true
        passButton.isDisabled=false
        knockButton.isDisabled=false
        changeOneCardsButton.isDisabled=false
        changeAllCardsButton.isDisabled=false
        drawStackSize.apply { text =game.drawStack.size.toString()}
        initializeOpenCards(game.middleCards,middleCards,cardImageLoader,true)
        initializeOpenCards(game.getCurrentPlayer().handCards,currentPlayerHand,cardImageLoader,true)

    }

    /**
     * Refreshes Views of Labels after passing
     *  disables all ActionButtons to hinder the player from doing actions twice
     */

    override fun refreshAfterPassed() {
        val game = rootService.currentGame
        checkNotNull(game)
        currentPlayer.apply{text= game.getCurrentPlayer().pName+ " passed"
        }
        playerMoveOver()
    }
    /**
     * Refreshes Views of Labels after knocking
     *  disables all ActionButtons to hinder the player from doing actions twice
     */
    override fun refreshAfterKnocked() {
        val game = rootService.currentGame
        checkNotNull(game)
        currentPlayer.apply{text= game.getCurrentPlayer().pName+ " knocked"
        }
        playerMoveOver()
    }

}