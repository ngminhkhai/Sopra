package ai


import entity.*
import service.RootService

/**
 * Consists of method [findStep] (player: AbstractPlayer, rootServ: RootService), which returns an IntArray containing
 *      -Index of a Tile in MiddleTiles
 *      -X Coordinate where this Tile should be placed in the players grid
 *      -Y Coordinate
 *      -Flag, whether middleTiles should be filled (0 for no, 1 for yes). If they should be, the method needs to be
 *      called again, because no Tile could have been determined yet.
 *  @property difficulty skill of this AI-service object. Default is HARD
 */
class AIService(private val difficulty: Difficulty = Difficulty.HARD){
    /**
     * Function that finds optimal tile and its position in grid.
     * findStep serves as the interface translating the actions determined by an AI into instructions for the GUI- and
     * service layer.
     *
     * @param [player] player that must make next step
     * @param  rootServ [service.RootService] Object, which holds references to all elements needed
     * to describe the current state of the game
     *
     * @return IntArray with 4 Elements: index of optimal tile, X-coordinate, Y-coordinate, flag fillTiles(0 for no).
     */
    fun findStep(player: AbstractPlayer, rootServ: RootService): IntArray {
        val retval = firstMove(player.tileGrid,rootServ)
        if(retval.isNotEmpty()) return retval
        return when(difficulty){
            Difficulty.EASY -> findStepEasy(rootServ)

            Difficulty.MEDIUM-> findStepMedium(rootServ)

            Difficulty.HARD-> findStepHard(player,rootServ)

        }
    }

    /**
     * Routine to find a simple Step
     */
    private fun findStepEasy(rootServ: RootService): IntArray{
        val game = rootServ.novaLunaApplication.currentGame
        checkNotNull(game){"No game in progress"}
        val available = availableTiles(game.middleTiles,game.moonPos)
        val opt : IntArray
        for(x in 0..8){
            for (y in 0..8){
                if(validChoice(x,y,game.currPlayer.tileGrid.tiles)){
                    println(game.currPlayer.tileGrid.tiles[x][y])
                    opt = intArrayOf(x,y)
                    return intArrayOf(game.middleTiles.indexOf(available[0]),opt[0],opt[1],0)

                }
            }
        }
        return intArrayOf()
    }

    /**
     * Routine to find a slightly more sophisticated Step
     */
    private fun findStepMedium(rootServ: RootService): IntArray{
        val rs = generateClonedGame(rootServ)
        val game = rs.novaLunaApplication.currentGame
        checkNotNull(game){"No game in progress"}
        val available = bestOfList(availableTiles(game.middleTiles,game.moonPos),game.currPlayer.tileGrid)

        val opt = mostTasksAffected(available,game.currPlayer.tileGrid)
        return if(available != null) {
            intArrayOf(game.middleTiles.indexOf(available),opt[0],opt[1],0)}
        else{
            intArrayOf(-1,-1,-1,1)
        }
    }

    /**
     * Routine to find a smart Step
     */
    private fun findStepHard(player: AbstractPlayer, rootService: RootService): IntArray{
        val root = TreeNode(arrayListOf(0))
        val rootServ = generateClonedGame(rootService)
        val game : Game?= rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        assert(player == game.currPlayer){"Tried to call findStep for player that is currently waiting"}
        makeTree(rootServ,root,4,player,player)
        var retval = searchTree(root)
        retval = if(availableTiles(game.middleTiles,game.moonPos).size < 3){     //If fillTiles is a possibility
            if(retval[1] == -1 || retval[3] < -game.listOfPlayers.size ){
                println("findStepHard advises a fill")
                intArrayOf(-1,-1,-1,1)
            }else{
                println("findStepHard does not advise a  possible fill")
                intArrayOf(retval[0],retval[1],retval[2],0)
            }
        }else{
            intArrayOf(retval[0],retval[1],retval[2],0)
        }

        /*
        A tree node consists of the following values : flagMainPlayer?,MiddleIndexOfTile,TasksDone,TasksAffected,x,y
        A return value of searchTree consists of MidIndex of optimal tile,x,y,done,affected,fill
        Done and affected are the sum of each 'optimal' move, and signal a balance between our done and the enemies done
        negative values mean, the enemy gains more than we do, which is expected, because there are potentially
        three enemies fighting our player.
        The Fill flag is an option, which we can use, if we do not think the balance is in our favor.
        */

        return  intArrayOf(retval[0],retval[1],retval[2],retval[3])

    }

    /*--------------------------------------------------------------------------------------------------------*/
    /*---------------------  Assisting functions building up to make/searchTree  -----------------------------*/
    /*--------------------------------------------------------------------------------------------------------*/

    /**
     * Method iterates through a given Tree to determine the best possible move.
     *
     * @param root Root Node of the (current branch of the) Tree.
     * @return An IntArray containing the Tile, which will have the best Outcome. The Tile is specified by the first
     * three Indexes, which contain its Position in middleTiles, and the x and y coordinates it should be placed.
     * The other Indexes specify the Sum of completed Tasks and of affected Tasks. These sums contain the opponents
     * completed tasks as negative Integers. The last Index is a flag, if fillTiles should be called.
     * --> MidPos,x,y,done,affected,fill.
     */
    private fun searchTree(root : TreeNode<ArrayList<Int>>):IntArray{
        if(root.value.isEmpty()){       //Nodes, that advise fill are empty. The root of the tree contains a 0
            return intArrayOf(-1,-1,-1,0,0,1)
        }
        val isMainPlayer = if(root.value[0] == 1){1}else{-1}

        if(root.children.isEmpty()){
            return intArrayOf(root.value[1],root.value[4],root.value[5],
                                root.value[2]*isMainPlayer,root.value[3]*isMainPlayer,0)
        }
        var retval = intArrayOf(0,0,0, Int.MIN_VALUE)
        root.children.forEach { node ->
            val temp = searchTree(node)
            if(temp[3]>retval[3]){
                retval = temp
            }
        }
        if(root.value.size == 1){
            return retval
        }
        retval = intArrayOf(root.value[1], root.value[4], root.value[5],
            root.value[2]*isMainPlayer + retval[3],root.value[3]*isMainPlayer + retval[4],0)
        return retval
    }

    /**
     * Function builds a decision-Tree of all possible game steps.
     * if there are no tiles on the table, function will build nothing.
     *
     * @param [rootService] Root Service of current game
     * @param [root] Parent of the nodes built in a particular iteration of this function, which represents the last
     *      move made in the game/ makeTree function.
     * @param [depth] number of game steps that should be added in tree/ Max number of iterations
     * @param [currPlayer] player that makes a step in the current Iteration of this method.
     * @param [mainPlayer] player, whose optimal step/move is being determined (i.e. Player who asked for a hint or
     *                  an AI Player).
     */
    private fun makeTree(rootService : RootService, root : TreeNode<ArrayList<Int>> ,
                         depth : Int, currPlayer: AbstractPlayer, mainPlayer: AbstractPlayer ) {
        val rs = generateClonedGame(rootService)
        val currentGame: Game? = rs.novaLunaApplication.currentGame
        checkNotNull(currentGame){"makeTree: No current game found"}
        val availableTiles = availableTiles(currentGame.middleTiles, currentGame.moonPos)
        assert(availableTiles.size > 0){"makeTree: No Tiles left in middleStack"}
        var nextNode : TreeNode<ArrayList<Int>>

        if (currPlayer == mainPlayer){
            availableTiles.forEach{  tile->
                val currGrid = currentGame.currPlayer.tileGrid.deepCopy()

                nextNode = nodeForTile((availableTiles.size < 3),1,tile,currGrid)

                if (nextNode.value.isEmpty()){
                    root.addChild(nextNode)
                    return@forEach
                }
                assert(nextNode.value[0] != -1){"makeTree: Did not build a node"}
                root.addChild(nextNode)
                nextNode.value[1] =currentGame.middleTiles.indexOf(tile)

                val tempRootService = generateClonedGame(rs)
                val tempGame = tempRootService.novaLunaApplication.currentGame
                checkNotNull(tempGame)
                tempRootService.playerActionService.takeTile(tile,false)
                tempRootService.playerActionService.placeOnGrid(
                    tile,
                    nextNode.value[4],
                    nextNode.value[5],
                    false
                )
                if(depth != 1){
                    tempGame.nextPlayer()
                    makeTree(tempRootService, nextNode, depth-1 , tempGame.currPlayer, mainPlayer)
                }
            }
        }
        else          //Current Player differs from Main Player
        {
            val optTile: Tile? = bestOfList(availableTiles, currPlayer.tileGrid)
            nextNode = nodeForTile((availableTiles.size < 3), 0, optTile, currPlayer.tileGrid)
            if (nextNode.value.isEmpty()){
                root.addChild(nextNode)
                return
            }
            assert(nextNode.value[0] != -1) { "makeTree: Did not build a node" }
            root.addChild(nextNode)
            nextNode.value[1] = currentGame.middleTiles.indexOf(optTile)
            val tempRootService = generateClonedGame(rs)
            val tempGame = tempRootService.novaLunaApplication.currentGame
            checkNotNull(tempGame)
            tempRootService.playerActionService.takeTile(optTile,false)
            tempRootService.playerActionService.placeOnGrid(
                optTile,
                nextNode.value[4],
                nextNode.value[5],
                false
            )
            if (depth != 1) {
                tempGame.nextPlayer()
                makeTree(tempRootService, nextNode, depth - 1, tempGame.currPlayer, mainPlayer)
            }
        }
    }

    /**
     * Clones the current state of the game, with instances of the entity-layer objects,
     * and relevant objects from the service-layer.
     * @param rootServ Holds al the references of the game, that will be copied.
     */
    private fun generateClonedGame(rootServ: RootService):RootService{
        val result = RootService()

        result.ioService.mapOfID = mutableMapOf()
        result.ioService.mapOfID = rootServ.ioService.mapOfID.toMutableMap()
        result.ioService.idMappingSaved = rootServ.ioService.idMappingSaved
        result.ioService.tile = rootServ.ioService.tile

        val game = rootServ.novaLunaApplication.currentGame
        checkNotNull(game){"Generate cloned game: No game in progress"}
        result.novaLunaApplication.currentGame = game.deepCopy()
        result.novaLunaApplication.highscore.scoreboard = rootServ.novaLunaApplication.highscore.scoreboard
        return result
    }

    /**
     * Builds a TreeNode for [tile].
     * @param  fill Flag, if fillTiles is an Option
     * @param ourPlayer Flag for currPlayer == mainPlayer (1 for yes)
     * @param grid Players grid
     *
     * @return A [TreeNode] with values set to an [ArrayList]<Int> containing
     * FlagRelevantPlayer, midPos, numberOfDone, numberOfAffected, x, y
     * If the function determines, that a fill would be preferable to making a move, values will be an empty ArrayList.
     *
     * Note : MidPos is always set to -1, because the necessary parameters are missing.
     */
    private fun nodeForTile(fill : Boolean,ourPlayer : Int,tile: Tile?, grid: Grid):TreeNode<ArrayList<Int>>{
        if(tile == null){return TreeNode(arrayListOf())}
        var optPosition = mostTasksCompleted(tile,grid)
        if(optPosition[0] == -1){
            return TreeNode(arrayListOf())
        }
        if (fill && optPosition[2] <1){
            optPosition = mostTasksAffected(tile,grid)
            if (optPosition[2] <2){
                return TreeNode(arrayListOf())
            }
            return TreeNode(arrayListOf(ourPlayer,-1,0,optPosition[2],optPosition[0],optPosition[1]))
        }else{
            grid.tiles[optPosition[0]][optPosition[1]]= tile
            tile.xPos = optPosition[0]
            tile.yPos = optPosition[1]
            return TreeNode(arrayListOf(ourPlayer,-1,optPosition[2],
                countAffectedByTile(tile,grid.tiles),optPosition[0],optPosition[1]))
        }

    }

    /**
     * Function finds the Tile out of a list, that gets the most tasks done, or if that value is zero for all tiles,
     * affects the most tasks.
     *
     * @param tiles to choose from.
     * @param grid to place the tiles in.
     *
     * @return Tile that will finish most tasks, or if that value is 0, affects the most tasks.
     */
    private fun bestOfList( tiles : MutableList<Tile>, grid: Grid) : Tile?{
        var retval = intArrayOf()
        var retTile : Tile? = null
        var temp : IntArray
        if(tiles.isEmpty()){return null}
        tiles.forEach { tile ->
            temp = mostTasksCompleted(tile, grid)
            if(retval.isEmpty() || temp[2]>retval[2]){
                retTile = tile
                retval = temp
            }
        }
        if(retval[2]>1){return  retTile}
        tiles.forEach { tile ->
            temp = mostTasksAffected(tile, grid)
            if(retval.isEmpty() || temp[2]>retval[2]){
                retTile = tile
                retval = temp
            }
        }
        checkNotNull(retTile){"BestOfList could not determine a tile"}
        return retTile
    }

    /**
     * Function checks, if this is the first Move for the Player and, if it is, determines their best possible Move.
     *
     * @param grid Grid, where a potential Tile would be placed.
     * @param rootServ current RootService.
     * @return An empty IntArray, if this is not the first Move. Otherwise, an IntArray findStep() can return.
     *      That Array will contain the position of the middleTile (,available to the player) with the most tasks,
     *      Position 4 4 (which is the middle of the players grid) and 0, as a Flag to not fill up the tiles.
     */
    private fun firstMove(grid: Grid, rootServ: RootService):IntArray{
        val rootService = generateClonedGame(rootServ)
        val tiles = grid.tiles
        var isFirstStep = true
        for (x in 0..8){
            for (y in 0..8){
                if(tiles[x][y] != null){
                    isFirstStep = false
                    break
                }
            }
        }
        return if(isFirstStep){
            val currentGame = rootService.novaLunaApplication.currentGame
            checkNotNull(currentGame){"firstMove : No Game in Progress"}
            var nrOfTasks = -1
            var tempTile : Tile? = null
            availableTiles(currentGame.middleTiles,currentGame.moonPos).forEach { tile ->
                if (tile.tasks.size > nrOfTasks){
                    tempTile = tile
                    nrOfTasks = tile.tasks.size
                }
            }
            checkNotNull(tempTile){"firstMove : No available Tile has more than -1 Tasks"}
            intArrayOf(currentGame.middleTiles.indexOf(tempTile),4,4,0)
        }else{
            intArrayOf()
        }

    }

    /**
     * Function returns tiles that can be taken in current step.
     *
     * @param [tiles] Array of tiles that are on the table.
     * @param [moonPos] index with current position of the Meeple.
     * @return [MutableList] containing the Tiles.
     */
    private fun availableTiles(tiles : Array<Tile?>, moonPos : Int) : MutableList<Tile>{
        var currPos = moonPos
        val result = mutableListOf<Tile>()
        while(result.size < 3){
            currPos ++
            if(currPos == 12) currPos = 0
            val temp = tiles[currPos]
            if(temp != null) result.add(temp)
            if(currPos == moonPos) break
        }
        return result
    }

    /*------------------------------------------------------------------------------------------------------*/
    /*                       Assisting functions building up to MostTasksDone/Affected                      */
    /*------------------------------------------------------------------------------------------------------*/

    /**
     * This function determines the position in which a given Tile placed in a grid will affect the most tasks.
     * @param place The tile to be placed.
     * @param grid The grid in which the [place] might be placed (This parameter is still a [Grid] Object,
     *  the grid in all the assisting functions will be a 2d Array storing nullable [Tile]s ).
     *
     * @return An Array containing, as Integers, x and y coordinates of the determined position,
     *  and the number of tasks that will be affected, if [place] will be placed at x y.
     */
    private fun mostTasksAffected(place : Tile?, grid: Grid): IntArray{
        checkNotNull(place){"mostTasksAffected : place is null"}
        val currentOpt = intArrayOf(-1,-1, Int.MIN_VALUE) // x,y,affected
        val tiles = grid.deepCopy().tiles
        var temp : Array<Array<Tile?>>
        var placeClone : Tile
        for(x in 0..8){
            for(y in 0..8){
                if(validChoice(x,y,tiles)){
                    placeClone = place.deepCopy()
                    placeClone.xPos = x
                    placeClone.yPos = y
                    temp = grid.deepCopy().tiles; temp[x][y] = placeClone        //temp represents the grid, if place were placed at x y
                    val affected =  countAffectedByTile(placeClone,temp)


                    if(affected > currentOpt[2]){
                        currentOpt[0] = x
                        currentOpt[1] = y
                        currentOpt[2] = affected
                    }
                }
            }
        }
        return currentOpt
    }

    /**
     * This function determines the position in which a given Tile placed in a grid will finish the most tasks.
     * @param place The tile to be placed.
     * @param grid The grid in which the [place] might be placed (This parameter is still a [Grid] Object,
     *  the grid in all the assisting functions will be a 2d Array storing nullable [Tile]s ).
     *
     * @return An Array containing, as Integers, x and y coordinates of the determined position,
     *  and the number of tasks that will be completed, if [place] will be placed at x y.
     */
    private fun mostTasksCompleted(place : Tile?, grid: Grid): IntArray{
        checkNotNull(place){"mostTasksCompleted : place is null"}
        val tiles = grid.deepCopy().tiles
        var temp : Array<Array<Tile?>>
        var placeClone : Tile?
        val currentOpt = intArrayOf(-1,-1,-1)  // x,y,newlyCompletedTasks.
        for( x in 0..8){        //Iterate through grid, which is specified as 9x9
            for( y in 0..8){
                if(validChoice(x,y,tiles)){
                    //println("A validChoice was detected")
                    temp = grid.deepCopy().tiles
                    placeClone = place.deepCopy()
                    placeClone.xPos = x
                    placeClone.yPos = y
                    temp[x][y] = placeClone
                    //Temp is the state of the AI-players grid, if he placed [place] at x,y
                    val count = countDoneButNotFlagged(temp)
                    //println("count is $count")
                    if(count > currentOpt[2]){
                        //println("Setting currentOpt")
                        currentOpt[0] = x
                        currentOpt[1] = y
                        currentOpt[2] = count
                    }
                }
            }
        }
        //println("CurrentOpt x is ${currentOpt[0]}")
        return currentOpt
    }

    /**
     * @return Tasks in [grid] affected by [influence].
     */
    private fun countAffectedByTile(influence : Tile, grid : Array<Array<Tile?>>):Int{
        assert(grid[influence.xPos][influence.yPos] == influence){
            "countAffectedByTile : Influence is not in grid"
        }
        val colorCluster = returnColorCluster(influence.xPos,influence.yPos,grid)
        val cluster = mutableListOf<Tile>()//This will represent colorCluster and all tiles adjacent to it
        var effCounter = 0
        colorCluster.forEach { tile ->
            cluster.add(tile)
            val adj = getAdjacentTiles(tile.xPos,tile.yPos,grid)
            adj.forEach { adjTile ->
                if(!cluster.contains(adjTile) && !colorCluster.contains(adjTile)){cluster.add(adjTile)}
            }
        }
        cluster.forEach { tile ->
            tile.tasks.forEach{ task ->
                val counter = task.colors.count { taskColor -> taskColor == influence.color }
                //Counter represents the number of Tiles the task at hand requires of the Color of the placed tile
                if(counter >= cluster.size){
                    effCounter++
                }
            }
        }
        return effCounter
    }

    /**
     * @return Number of completed Tasks in [grid], that are not flagged as finished.
     */
    private fun countDoneButNotFlagged(grid : Array<Array<Tile?>>)  : Int{
        var counter = 0
        for(x in 0..8){
            for(y in 0..8){
                val tile = checkTile(x,y,grid)
                if(tile != null){
                    counter += checkTasksOfTile(tile,grid)
                }
            }
        }
        return counter
    }

    /**
     * @return number of tasks of [tile] that are finished, but not flagged as finished.
     */
    private fun checkTasksOfTile(tile : Tile, grid : Array<Array<Tile?>>) : Int{
        val tasks = tile.tasks
        if(tasks.isEmpty()){return 0}
        var taskCounter = 0
        tasks.forEach { task ->
            if(!task.isFinished){
                var isDone = true             //Flag assumes the task is done, until a condition cannot be fulfilled
                Color.values().forEach{ color ->
                    var required = task.colors.count { taskColor -> taskColor == color }

                    if(tile.color == color){
                        required -= returnColorCluster(tile.xPos,tile.yPos,grid).size
                    }else{
                        val referenceCluster = mutableListOf<Tile>()    //Contains all Elements already checked
                        val adj = getAdjacentTiles(tile.xPos,tile.yPos,grid)
                        adj.forEach { adjTile ->
                            if(adjTile.color == color && !referenceCluster.contains(adjTile)){
                                val adjCluster = returnColorCluster(adjTile.xPos,adjTile.yPos,grid)
                                required -= adjCluster.size
                                adjCluster.forEach { referenceCluster.add(it) }
                            }
                        }
                    }
                    if(required > 0){
                        isDone = false
                    }
                }
                if(isDone){
                    taskCounter++
                }
            }
        }
        return taskCounter
    }

    /**
     * @return A [MutableList]<[Tile]> containing all Elements of the ColorCluster surrounding the Tile at [x] [y].
     * A Color Cluster consists of all Tiles adjacent to each other, sharing the same color.
     * @throws IllegalStateException If [x] [y] does not contain a tile.
     * @throws AssertionError If a tile's coordinates in toCheck were not set(i.e. are  -1,-1).
     */
    private fun returnColorCluster( x : Int, y : Int,grid : Array<Array<Tile?>>) : MutableList<Tile>{
        var temp = checkTile(x,y,grid)
        checkNotNull(temp){"returnColorCluster: Tile not found"}
        val color = temp.color

        val isChecked = mutableListOf<Tile>()
        val toCheck = mutableListOf(temp)

        while (toCheck.isNotEmpty()){
            temp =toCheck.removeFirst()
            assert(temp.xPos != -1 && temp.yPos != -1){"returnColorCluster: Tile in toCheck could not be located"}
            val adj = getAdjacentTiles(temp.xPos,temp.yPos,grid)
            adj.forEach { tile ->
                if(tile.color == color && !isChecked.contains(tile) && !toCheck.contains(tile)){
                    toCheck.add(tile)
                }
            }           //Each Tile adjacent to temps position will be added to toCheck, if its Color matches color
            isChecked.add(temp)
        }
        return isChecked
    }

    /**
     * @params [x] and [y] specify coordinates in [grid], at which a Tile is expected to be saved.
     * @return either the specified tile or null, if there is no tile or if x and/or y are not valid coordinates.
     */
    private fun checkTile(x : Int, y : Int,grid : Array<Array<Tile?>>) : Tile?{
        return if(x in 0..8 && y in 0..8){
            grid[x][y]
        } else {
            null
        }
    }

    /**
     * @params [x] and [y] specify coordinates in [grid].
     * @return All tiles adjacent to position x y in grid
     */
    private fun getAdjacentTiles(x : Int, y : Int,grid : Array<Array<Tile?>>) : MutableList<Tile>{
        val retval = mutableListOf<Tile>()

        var temp = checkTile(x,y+1,grid)
        if(temp != null){
            retval.add(temp)
        }
        temp = checkTile(x+1,y,grid)
        if(temp != null){
            retval.add(temp)
        }
        temp = checkTile(x-1,y,grid)
        if(temp != null){
            retval.add(temp)
        }
        temp = checkTile(x,y-1,grid)
        if(temp != null){
            retval.add(temp)
        }

        return retval
    }

    /**
     * Checks if it is allowed to place a tile in [tiles] at position [x],[y].
     * @return true if there is at least one tile adjacent and the space itself is not taken.
     */
    private fun validChoice(x : Int, y : Int, tiles : Array<Array<Tile?>>) : Boolean{
        return (tiles[x][y] == null) && (getAdjacentTiles(x,y,tiles).size > 0)
    }

}



