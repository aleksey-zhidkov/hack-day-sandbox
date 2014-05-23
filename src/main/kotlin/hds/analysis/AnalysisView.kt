package hds.analysis

import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.TextField
import com.vaadin.ui.Alignment
import com.vaadin.ui.Button
import com.vaadin.ui.Label
import com.vaadin.ui.UI

public class AnalysisView : HorizontalLayout(), View {

    private var ui: UI? = null

    private var repositories = Label("Репозитории")

    private var reposLock = Object()

    private var totalRepos = 0

    private var processedRepos = 0

    private var files = Label("Файлы")

    private var filesLock = Object()

    private var totalFiles = 0

    private var processedFiles = 0

    {
        setSizeFull()

        val githubUser = TextField("GitHub user")
        githubUser.setInputPrompt("Имя пользователя GitHub")
        githubUser.setCaption("")
        addComponent(githubUser)
        setComponentAlignment(githubUser, Alignment.MIDDLE_CENTER)

        val analyze = Button("Анализировать")
        addComponent(analyze)
        setComponentAlignment(analyze, Alignment.MIDDLE_CENTER)
        analyze.addListener {
            if (githubUser.getValue() != null) {
                githubUser.setEnabled(false)
                analyze.setEnabled(false)
                AnalysisService().analyze(githubUser.getValue()!!, AnalysisCallbackImpl())
            }
        }

        addComponent(repositories)
        setComponentAlignment(repositories, Alignment.MIDDLE_CENTER)

        addComponent(files)
        setComponentAlignment(files, Alignment.MIDDLE_CENTER)
    }

    override fun enter(event: ViewChangeListener.ViewChangeEvent?) {
        ui = UI.getCurrent()
    }

    inner class AnalysisCallbackImpl : AnalysisCallback {

        fun updateRepos() {
            if (ui != null) {
                synchronized(reposLock) {
                    ui?.access {
                        repositories.setValue("Репозитории: $processedRepos/$totalRepos")
                    }
                }
            }
        }

        fun updateFiles() {
            if (ui != null) {
                synchronized(filesLock) {
                    ui?.access {
                        files.setValue("Файлы: $processedFiles/$totalFiles")
                    }
                }
            }
        }


        override fun onRepositoryFound() {
            totalRepos += 1
            updateRepos()
        }

        override fun onRepositoryProcessed() {
            processedRepos += 1
            updateRepos()
        }

        override fun onFileFound() {
            totalFiles += 1
            updateFiles()
        }

        override fun onFileProcessed() {
            processedFiles += 1
            updateFiles()
        }

        override fun onFinish() {
            println("AAAA")
        }

        override fun onError(reason: String) {
        }

    }

}