package ai

/**
 * Class holds one node of the decision tree built to determine an AI-Players move.
 * @param value represents the contents of this particular node.
 */
class TreeNode<T>(var value: T){
    /**
     * The nodes parent Node.
     */
    private var parent : TreeNode<T>? = null

    /**
     * The nodes children. There is no limit to the number of children.
     */
    var children : MutableList<TreeNode<T>> = mutableListOf()

    /**
     * Adds [node] as a child of this node.
     */
    fun addChild(node:TreeNode<T>){
        children.add(node)
        node.parent = this
    }

    /**
     * Returns contents of this node as a String.
     */
    override fun toString(): String {
        var s = "$value"
        if (children.isNotEmpty()) {
            s += " { ${children.map { it.toString() }} }"
        }
        return s
    }
}
