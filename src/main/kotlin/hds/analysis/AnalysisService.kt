package hds.analysis

import kotlin.concurrent.thread
import java.util.HashSet

public class AnalysisService() {

    fun analyze(githubUser: String, callback: AnalysisCallback) {

        thread {
            val toWait = HashSet<Thread>()
            val reposCnt = (Math.random() * 10).toInt()
            for (i in 0..reposCnt) {
                Thread.sleep((1000L * Math.random()).toLong())

                val t = thread {
                    callback.onRepositoryFound()
                    val filesCount = (Math.random() * 10).toInt()

                    for (j in 0..filesCount) {
                        Thread.sleep((Math.random() * 100).toLong())
                        callback.onFileFound()
                        callback.onFileProcessed()
                    }

                    callback.onRepositoryProcessed()
                }

                toWait.add(t)
            }

            for (t in toWait) {
                t.join()
            }
            callback.onFinish()
        }
    }

}