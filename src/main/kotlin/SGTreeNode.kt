import java.util.*
import kotlin.collections.ArrayDeque

class SGTreeNode<T : Comparable<T>>(value: T) {
    var value: T? = value
    var leftChild: SGTreeNode<T>? = null
    var rightChild: SGTreeNode<T>? = null

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val treeNode: SGTreeNode<*> = o as SGTreeNode<*>
        return value == treeNode.value &&
                leftChild == treeNode.leftChild &&
                rightChild == treeNode.rightChild
    }

    override fun hashCode(): Int {
        return Objects.hash(value, leftChild, rightChild)
    }

    val weight: Int
        get() {
            var leftWeight = 0
            var rightWeight = 0
            leftWeight = leftChild?.weight ?: 0
            rightWeight = rightChild?.weight ?: 0
            return leftWeight + rightWeight
        }

    fun search(searchValue: T): SGTreeNode<T>? {
        val compareVal = searchValue.compareTo(value!!)
        return when {
            (compareVal == 0) -> this
            (compareVal < 0) -> leftChild?.search(searchValue)
            else -> rightChild?.search(searchValue)
        }
    }

    fun subtreeAsList(includeCurrent: Boolean, result: MutableList<T>) {
        leftChild?.subtreeAsList(true, result)
        if (includeCurrent) value?.let { result.add(it) }
        rightChild?.subtreeAsList(true, result)
    }

    fun findPath(node: SGTreeNode<T>, path: ArrayDeque<SGTreeNode<T>>) {
        val compareVal: Int = value?.let { node.value?.compareTo(it) } ?: 0
        if (compareVal < 0 && leftChild != null) {
            path.addFirst(this)
            leftChild!!.findPath(node, path)
        } else if (compareVal > 0 && rightChild != null) {
            path.addFirst(this)
            rightChild!!.findPath(node, path)
        }
    }

    fun addAsChild(newNode: SGTreeNode<T>, currPath: ArrayDeque<SGTreeNode<T>> = ArrayDeque()) {
        if (value?.let { newNode.value?.compareTo(it) } ?: 0 < 0) {
            currPath.addFirst(this)
            if (leftChild == null) leftChild = newNode else leftChild!!.addAsChild(newNode, currPath)
        } else if (value?.let { newNode.value?.compareTo(it) } ?: 0 > 0) {
            currPath.addFirst(this)
            if (rightChild == null) rightChild = newNode else rightChild!!.addAsChild(newNode, currPath)
        }
    }

    fun recursiveIns(values: MutableList<T>, start: Int, end: Int) {
        if (start < 0 || end > values.size - 1 || start > end) return
        if (start == end) {
            val compareVal = value?.let { values[start].compareTo(it) }
            val newNode: SGTreeNode<T> = SGTreeNode(values[start])
            if (compareVal != null) {
                if (compareVal <= 0) leftChild = newNode else rightChild = newNode
            }
        } else {
            val medianInd = (start + end) / 2
            val newNode: SGTreeNode<T> = SGTreeNode<T>(values[medianInd])
            addAsChild(newNode)
            newNode.recursiveIns(values, start, medianInd - 1)
            newNode.recursiveIns(values, medianInd, end)
        }

    }
}