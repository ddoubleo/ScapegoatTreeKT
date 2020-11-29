import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.util.*
import kotlin.NoSuchElementException
import kotlin.collections.ArrayDeque

class ScapegoatTree<T : Comparable<T>>() : MutableSet<T> {
    var root: SGTreeNode<T>? = null
    var alpha = 0.0 // Коэффийент балансировки 0.5 < alpha < 1
    override var size: Int = 0
    var elementClass: Class<*>? = null
    var lastRebuildSize: Int = 0

    constructor(value: T, alpha: Double) : this() {
        if (alpha in 0.5..1.0) this.alpha = alpha else throw IllegalArgumentException("Alpha must be in range 0.5..1.0")
        this.root = SGTreeNode<T>(value)
        size = 1
        elementClass = value.javaClass
        lastRebuildSize = 1
    }

    constructor(alpha: Double) : this() {
        if (alpha in 0.5..1.0) this.alpha = alpha else throw IllegalArgumentException("Alpha must be in range 0.5..1.0")
    }

    override fun add(element: T): Boolean {
        if (elementClass == null) elementClass = element.javaClass
        if (elementClass != element.javaClass) return false
        if (this.contains(element)) return false
        if (root == null) {
            root = SGTreeNode(element)
            return true
        }
        val path: ArrayDeque<SGTreeNode<T>> = ArrayDeque()
        root!!.addAsChild(SGTreeNode(element), path)
        size++
        while (path.isNotEmpty()) {
            val node: SGTreeNode<T> = path.removeFirst()
            val currAlpWeight: Double = node.weight * alpha
            var rightWeight = 0.0
            var leftWeight = 0.0
            if (node.rightChild != null) rightWeight = node.rightChild!!.weight.toDouble()
            if (node.leftChild != null) leftWeight = node.leftChild!!.weight.toDouble()
            if (rightWeight > leftWeight || leftWeight > currAlpWeight) {
                rebuild(true, node, path)
                break
            }


        }
        return true
    }

    private fun rebuild(saveCurr: Boolean, node: SGTreeNode<T>, path: ArrayDeque<SGTreeNode<T>>) {
        val subtreeArr = mutableListOf<T>()
        node.subtreeAsList(saveCurr, subtreeArr)
        val medianInd = (subtreeArr.size - 1) / 2
        if (node == root) {
            root = SGTreeNode(subtreeArr[medianInd])
            root!!.recursiveIns(subtreeArr, 0, medianInd)
            root!!.recursiveIns(subtreeArr, medianInd + 1, subtreeArr.lastIndex)
        } else {
            val parentNode: SGTreeNode<T> = path.removeFirst()
            if (subtreeArr.size == 0 && !saveCurr) {
                if (parentNode.value?.let { node.value!!.compareTo(it) }!! <= 0) parentNode.leftChild = null
                return
            }
            val newScapegoat: SGTreeNode<T> = SGTreeNode(subtreeArr[medianInd])
            if (parentNode.value?.let { newScapegoat.value!!.compareTo(it) }!! <= 0) parentNode.leftChild = newScapegoat
            newScapegoat.recursiveIns(subtreeArr, 0, medianInd - 1)
            newScapegoat.recursiveIns(subtreeArr, medianInd + 1, subtreeArr.lastIndex)
        }

    }

    override fun addAll(elements: Collection<T>): Boolean {
        var isSuccessful = true
        for (element in elements) isSuccessful = this.remove(element)
        return isSuccessful
    }

    override fun clear() {
        root = null
    }

    override fun iterator(): MutableIterator<T> = SGTreeIterator()


    inner class SGTreeIterator internal constructor() : MutableIterator<T> {
        private val stack = Stack<SGTreeNode<T>>()
        private var prev: SGTreeNode<T>? = null
        private var current = root

        override fun hasNext(): Boolean {
            return stack.isNotEmpty() || current != null
        }

        override fun next(): T {
            while (current != null) {
                stack.push(current)
                current = current?.leftChild
            }
            if (!hasNext()) throw NoSuchElementException()
            current = stack.pop()
            val result = current?.value
            prev = current
            current = current?.rightChild
            return result!!
        }

        override fun remove() {
            if (prev == null) throw IllegalStateException()
            remove(prev!!.value)
            prev = null
        }

    }





    override fun remove(element: T): Boolean {
        if (elementClass != element.javaClass) return false
        if (!this.contains(element)) return false
        val removingNode = root?.search(element)
        if (removingNode == root && size == 1) {
            root = null
            return true
        }
        val path: ArrayDeque<SGTreeNode<T>> = ArrayDeque<SGTreeNode<T>>()
        root!!.findPath(removingNode!!, path)
        rebuild(false, removingNode, path)
        size--
        if (size * alpha < lastRebuildSize) {
            val defaultPath: ArrayDeque<SGTreeNode<T>> = ArrayDeque()
            defaultPath.addFirst(root!!)
            rebuild(true, root!!, defaultPath)
            lastRebuildSize = size
        }
        return true
    }


    override fun removeAll(elements: Collection<T>): Boolean {
        var isSuccessful = true
        for (element in elements) isSuccessful = this.remove(element)
        return isSuccessful
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        var isSuccessful = true
        for (element in this) if (elements.contains(element)) isSuccessful = this.remove(element)
        return isSuccessful
    }

    override fun contains(element: T): Boolean {
        if (root == null) return false
        if (elementClass != element.javaClass) return false
        return root!!.search(element) != null
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        var isSuccessful = true
        for (element in elements) isSuccessful = contains(element)
        return isSuccessful
    }

    override fun isEmpty(): Boolean {
        return root == null
    }


}