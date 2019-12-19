package com.luo123.androidlab

import com.luo123.androidlab.update.UpdateMessageListModel
import com.luo123.androidlab.update.UpdateMessageModel
import org.junit.Test

import org.junit.Assert.*
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.nodes.Tag

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun yamlTest() {
        val updateMessageListModel = UpdateMessageListModel().apply {
            latestVersionCode = 1
            messageList = mutableMapOf(Pair(1, UpdateMessageModel().apply {
                version = "1.0"
                message = "lalala"
                downloadUrl = "23333"
            }))
        }
        println(Yaml().dump(updateMessageListModel))
    }
}
