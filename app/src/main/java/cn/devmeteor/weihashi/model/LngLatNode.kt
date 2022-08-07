package cn.devmeteor.weihashi.model

import cn.devmeteor.treepicker.TreeNode
import cn.devmeteor.treepicker.TreePicker
import kotlin.random.Random

class LngLatNode() : TreePicker.Convertible {

    var name: String? = null
    var next: MutableList<LngLatNode>? = null
    var lng = -1.0
    var lat = -1.0

    constructor(name: String) : this() {
        this.name = name
    }

    constructor(name: String, next: MutableList<LngLatNode>) : this() {
        this.name = name
        this.next = next
    }


    override fun toString(): String =
        "{name:$name,lng:$lng,lat:$lat}"

    override fun convert(): TreeNode = convertNodes(this)!!

    private fun convertNodes(node: LngLatNode?): TreeNode? {
        if (node == null)
            return null
        val converted = TreeNode(node.name!!)
        if (node.next != null) {
            converted.next = ArrayList()
            for (n in node.next!!) {
                (converted.next as ArrayList<TreeNode>).add(convertNodes(n)!!)
            }
        }
        return converted
    }

}