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
import com.vaadin.ui.FormLayout
import com.vaadin.ui.CustomLayout
import com.vaadin.ui.CssLayout
import com.vaadin.ui.VerticalLayout

public class AnalysisView(val navigator: Navigator) : HorizontalLayout(), View {

    private var ui: UI? = null

    private val centerLayout = VerticalLayout()

    private val formLayout = FormLayout()

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
//        centerLayout.addStyleName("center")
//        setSizeFull()
        val analyze = Button("Анализировать")
        analyze.addStyleName("btn btn-primary")

        centerLayout.setSizeFull()
//        formLayout.addStyleName("form-horizontal")
        githubUser.setInputPrompt("Имя пользователя GitHub")
        githubUser.setCaption("")
        githubUser.addStyleName("form-control")
        formLayout.addComponent(analyze)
        formLayout.addComponent(githubUser)
        centerLayout.addComponent(formLayout)
        addComponent(centerLayout)
        centerLayout.setComponentAlignment(formLayout, Alignment.MIDDLE_CENTER)
//        setComponentAlignment(githubUser, Alignment.MIDDLE_CENTER)




//        setComponentAlignment(analyze, Alignment.MIDDLE_CENTER)
        analyze.addListener {
            val githubUserId = githubUser.getValue()
            if (githubUserId != null && githubUserId.isNotEmpty()) {
                githubUser.setEnabled(false)
                analyze.setEnabled(false)
                AnalysisService().analyze(githubUserId, AnalysisCallbackImpl())
            }
        }

//        addComponent(repositories)
        setComponentAlignment(repositories, Alignment.MIDDLE_CENTER)

//        addComponent(files)
//        setComponentAlignment(files, Alignment.MIDDLE_CENTER)

//        addComponent(linesByExtensionLabel)
//        setComponentAlignment(files, Alignment.BOTTOM_LEFT)
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
                navigator.navigateTo("${ResultsView.NAME}/${githubUser.getValue()}")
            }
        }

        override fun onError(reason: String) {
        }

    }

}