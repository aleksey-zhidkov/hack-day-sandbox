package spider

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.commons.io.IOUtils
import java.io.LineNumberReader
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.auth.AuthScope
import org.apache.http.impl.client.BasicCredentialsProvider
import org.eclipse.jgit.api.Git
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.FileSystems
import java.nio.file.FileVisitResult
import java.io.FileReader
import java.io.File
import hds.analysis.AnalysisCallback
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import org.eclipse.jgit.api.ResetCommand.ResetType
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.HashSet


/**
 * @author a.maximov
 * @since 23.05.14
 */

public class GithubParser(githubUsername: String) {

    private val techTriggers = mapOf(
            "import android" to "Android",
            "import org.springframework" to "Spring",
            "import org.hibernate" to "Hibernate",
            "import akka" to "Akka",
            "import net.liftweb" to "Lift",
            "import liquibase" to "Liquibase",
            "import com.vaadin" to "Vaadin",
            "import org.jooq" to "JOOQ",
            "import javax.persistence" to "JPA"
    )

    private val langugaes = mapOf(
            "java" to "Java",
            "js" to "JavaScript",
            "rb" to "Ruby",
            "py" to "Python",
            "cs" to "C#",
            "cpp" to "C++",
            "h" to "C++",
            "kt" to "Kotlin",
            "groovy" to "Groovy",
            "php" to "PHP",
            "scala" to "Scala"
    )

    val pass = "123qwe!";

    val httpclient = DefaultHttpClient();
    val login = "hackotest"

    val githubApiUrl = "https://api.github.com"

    val githubUsername = githubUsername

    fun curl(url: String, login: String? = null, password: String? = null): String? {
        val httpGet = HttpGet(url)
        if (login != null) {
            val credsProvider = BasicCredentialsProvider()
            credsProvider.setCredentials(
                    AuthScope("github.com", 443),
                    UsernamePasswordCredentials(login, password))
            httpclient.setCredentialsProvider(credsProvider)
        }
        val response = httpclient.execute(httpGet)
        return IOUtils.toString(response?.getEntity()?.getContent())
    }

    fun cloneAllUserRepos(userName: String): List<String?>? {
        val repoNames = repoNameList(userName)
        repoNames?.forEach { r -> cloneRepo(userName, r!!) }
        return repoNames
    }


    fun getLineCountByExt(userName: String,
                          repoName: String,
                          linesByExt: ConcurrentHashMap<String, AtomicInteger>,
                          linesByTechnology: ConcurrentHashMap<String, AtomicInteger>,
                          callback: AnalysisCallback): ConcurrentHashMap<String, AtomicInteger> {
        Files.walkFileTree(FileSystems.getDefault()!!.getPath(repoPath(userName, repoName)), object : SimpleFileVisitor<Path?>() {
            override fun visitFile(filePath: Path?, attrs: BasicFileAttributes): FileVisitResult {
                val fileName = filePath?.getFileName().toString()
                if (filePath.toString().contains(".git"))
                    return FileVisitResult.SKIP_SUBTREE

                val ext = fileName.split("\\.").last()

                val lng = langugaes[ext.toLowerCase()]
                if (lng != null) {
                    val lineCount = LineNumberReader(FileReader(File(filePath?.toAbsolutePath().toString()))).read()
                    linesByExt.putIfAbsent(lng, AtomicInteger())
                    val counter = linesByExt[lng]
                    counter?.addAndGet(lineCount)

                    println("Parsing file $fileName")
                    val stream = FileInputStream(File(filePath?.toAbsolutePath().toString())).buffered();
                    try {
                        val reader = InputStreamReader(stream, "UTF-8");
                        val parsedTechs = HashSet<String>()
                        reader.forEachLine({

                            for ((trigger, tech) in techTriggers) {
                                if (it.contains(trigger) && !parsedTechs.contains(tech)) {
                                    linesByTechnology.putIfAbsent(tech, AtomicInteger())
                                    val techCounter = linesByTechnology[tech]
                                    techCounter?.addAndGet(lineCount)
                                    parsedTechs.add(tech)
                                }
                            }
                        }
                        )
                    } finally {
                        stream.close();
                    }
                }


                callback.onFileProcessed()
                callback.onLinesByExtensionChanged(linesByExt)
                return FileVisitResult.CONTINUE
            }
        })

        return linesByExt
    }

    fun repoNameList(userName: String): List<String?>? {
        val reposJson = Util.toList(curl("$githubApiUrl/users/$userName/repos", login, pass));
        return reposJson?.map { r -> r["name"] }
    }

    fun cloneRepo(userName: String, repoName: String) {
        val dir = File(repoPath(userName, repoName))
        println("dir=$dir")

        println("Pulling $userName/$repoName")
        try {
            val git = when(dir.exists()) {
                true -> Git.open(dir)
                else -> {
                    dir.mkdir(); Git.cloneRepository()?.setURI(githubRepoLink(userName, repoName))?.setDirectory(dir)?./*setCredentialsProvider(user)*/call()
                }
            }

            git?.fetch()?.call()
            git?.checkout()?.setName("master")?.call()
            git?.reset()?.setMode(ResetType.HARD)?.call()
        } catch (e: Throwable) {
            println(e.getMessage() ?: "Unknown error")
        }

        println("Pulled $userName/$repoName")
    }

    fun repoPath(userName: String, repoName: String): String {
        return "${System.getProperty("user.home")}/data/index/repos/$userName/$repoName"
    }

    fun githubRepoLink(userName: String, repoName: String): String {
        return "https://github.com/$userName/$repoName.git"
    }
}

