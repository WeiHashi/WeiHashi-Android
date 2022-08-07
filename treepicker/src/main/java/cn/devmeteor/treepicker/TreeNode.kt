package cn.devmeteor.treepicker

open class TreeNode() : TreePicker.Convertible {

    var name: String? = null
    var next: MutableList<TreeNode>? = null

    constructor(name: String) : this() {
        this.name = name
    }

    constructor(name: String, next: MutableList<TreeNode>) : this() {
        this.name = name
        this.next = next
    }

    override fun toString(): String =
        "{name:$name}"

    override fun convert(): TreeNode = this
}
