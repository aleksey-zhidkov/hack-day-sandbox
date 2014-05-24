package hds.analysis

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

public trait AnalysisCallback {

    fun onRepositoryFound(): Unit

    fun onRepositoryProcessed(): Unit

    fun onFileFound(): Unit

    fun onFileProcessed(): Unit

    fun onLinesByExtensionChanged(linesByExtension : ConcurrentHashMap<String, AtomicInteger>): Unit

    fun onFinish(): Unit

    fun onError(reason: String)

}