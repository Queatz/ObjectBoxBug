package com.example.objectboxbug

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import io.objectbox.BoxStore
import io.objectbox.android.AndroidScheduler
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var boxStore: BoxStore

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        boxStore = MyObjectBox.builder().androidContext(application).build()

        boxStore.boxFor(Model::class.java).query(Model_.name.notEqual("something"))
            .build()
            .subscribe()
            .on(AndroidScheduler.mainThread())
            .observer {
                findViewById<TextView>(R.id.textView).text = SimpleDateFormat.getTimeInstance().format(Date())
            }

        sync()
    }

    override fun onDestroy() {
        super.onDestroy()
        boxStore.close()
    }

    private fun sync() {
        val results = (0..10000).mapIndexed { it, i -> Model().apply { objectBoxId = i.toLong() } }

        findViewById<TextView>(R.id.textView).text = "${SimpleDateFormat.getTimeInstance().format(Date())}"

        boxStore.runInTxAsync({
            removeAllExcept(results.filter { Random.nextBoolean() }.map { it.objectBoxId }.toSet())

            boxStore.boxFor(Model::class.java).put(results)
        }, null)

        handler.postDelayed({
            sync()
        }, 10)
    }

    private fun removeAllExcept(idsToKeep: Collection<Long>) {
        val query = boxStore.boxFor(Model::class.java).query()

        var isNotFirst = false
        for (idToKeep in idsToKeep) {
            if (isNotFirst) {
                query.and()
            } else {
                isNotFirst = true
            }

            query.notEqual(Model_.__ID_PROPERTY, idToKeep)
        }

        query.build().remove()
    }
}