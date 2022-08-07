package cn.devmeteor.weihashi

import cn.devmeteor.treepicker.TreeNode
import cn.devmeteor.weihashi.model.LngLatNode
import com.google.gson.Gson
import org.junit.Test
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class DataTest {

    @Test
    fun mapDataTest() {
        println(
            Gson().fromJson(
                BufferedReader(InputStreamReader(FileInputStream(File("src/main/assets/map_data.json")))),
                LngLatNode::class.java
            )
        )
    }

}