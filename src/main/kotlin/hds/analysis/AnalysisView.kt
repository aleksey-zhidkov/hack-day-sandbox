package hds.analysis

import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.TextField
import com.vaadin.ui.Alignment
import com.vaadin.ui.Button
import com.vaadin.ui.Label
import com.vaadin.ui.UI
import java.util.concurrent.atomic.AtomicInteger
import com.vaadin.navigator.Navigator
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

public class AnalysisView(val navigator: Navigator) : HorizontalLayout(), View {

    private var ui: UI? = null

    private val githubUser = TextField("GitHub user")

    private var repositories = Label("Репозитории")

    private var reposLock = Object()

    private var totalRepos = 0
    
    private val processedFiles = AtomicInteger(0)

    private var files = Label("Файлы")

    private var linesByExtensionLabel = Label("Строки кода:")

    private var filesLock = Object()

    private var processedRepos = 0

    {
        setSizeFull()

        githubUser.setInputPrompt("Имя пользователя GitHub")
        githubUser.setCaption("")
        addComponent(githubUser)
        setComponentAlignment(githubUser, Alignment.MIDDLE_CENTER)

        val analyze = Button("Анализировать")
        addComponent(analyze)
        setComponentAlignment(analyze, Alignment.MIDDLE_CENTER)
        analyze.addListener {
            val githubUserId = githubUser.getValue()
            if (githubUserId != null && githubUserId.isNotEmpty()) {
                githubUser.setEnabled(false)
                analyze.setEnabled(false)
                AnalysisService().analyze(githubUserId, AnalysisCallbackImpl())
            }
        }

        addComponent(repositories)
        setComponentAlignment(repositories, Alignment.MIDDLE_CENTER)

        addComponent(files)
        setComponentAlignment(files, Alignment.MIDDLE_CENTER)

        addComponent(linesByExtensionLabel)
        setComponentAlignment(files, Alignment.BOTTOM_LEFT)
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
                        files.setValue("Обработано файлов: $processedFiles")
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
            updateFiles()
        }

        override fun onFileProcessed() {
            processedFiles.incrementAndGet()
            updateFiles()
        }

        override fun onLinesByExtensionChanged(linesByExtension : ConcurrentHashMap<String, AtomicInteger>) {
            linesByExtensionLabel.setValue("Строки кода: " + linesByExtension.toString())
        }

        override fun onFinish() {
            ui?.access {
                //navigator.navigateTo("${ResultsView.NAME}/${githubUser.getValue()}")
            }
        }

        override fun onError(reason: String) {
        }

    }

}