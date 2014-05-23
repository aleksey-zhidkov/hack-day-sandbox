package hds.analysis

public trait AnalysisCallback {

    fun onRepositoryFound(): Unit

    fun onRepositoryProcessed(): Unit

    fun onFileFound(): Unit

    fun onFileProcessed(): Unit

    fun onFinish(): Unit

    fun onError(reason: String)

}