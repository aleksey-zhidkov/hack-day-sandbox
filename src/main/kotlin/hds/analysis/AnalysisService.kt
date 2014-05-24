package hds.analysis

import kotlin.concurrent.thread
import java.util.HashSet
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import org.jooq.impl.DSL
import hds.db.tables.Language
import java.util.Collections
import hds.db.connection
import hds.db.tables.PersonTechnologies
import hds.db.tables.Technology
import org.jooq.SQLDialect
import org.jooq.impl.DefaultConfiguration
import hds.db.tables.daos.PersonDao
import hds.db.tables.daos.TechnologyDao

public class AnalysisService() {

    fun analyze(githubUser: String, callback: AnalysisCallback) {

        thread {
            val toWait = HashSet<Thread>()
            val parser = spider.GithubParser(githubUser)
            val repoNames = parser.repoNameList(githubUser)

            val linesByExt = ConcurrentHashMap<String, AtomicInteger>()
            val linesByTechnology = ConcurrentHashMap<String, AtomicInteger>()
            repoNames?.forEach { repoName ->
                val t = thread {
                    callback.onRepositoryFound()
                    try {
                        parser.cloneRepo(githubUser, repoName!!)
                        parser.getLineCountByExt(githubUser, repoName!!, linesByExt, linesByTechnology, callback)/*parser.dirList(repoName ?: "", "", null)*/
                        callback.onRepositoryProcessed()
                    } catch (e: Throwable) {
                        println(e.getMessage() ?: "Unknown error")
                        callback.onError(e.getMessage() ?: "Unknown error")
                    }
                }

                toWait.add(t)
            }

            for (t in toWait) {
                t.join()
            }

            val connection = connection()
            val create = DSL.using(connection)

            val conf = DefaultConfiguration().set(connection).set(SQLDialect.POSTGRES)
            val personID: Int = PersonDao(conf).fetchByGithubId(githubUser)!!.head!!.getId()!!

            for ((tech, linesCount) in linesByTechnology) {
                val techs = TechnologyDao(conf).fetchByName(tech)!!.head
                if (techs == null) {
                    System.err.println(tech)
                    continue
                }
                val techId: Int = techs.getId()!!
                val pt = (create.select(PersonTechnologies.PERSON_TECHNOLOGIES.LINES_COUNT).
                from(PersonTechnologies.PERSON_TECHNOLOGIES).
                join(Technology.TECHNOLOGY).onKey().
                where(Technology.TECHNOLOGY.NAME!!.equal(tech)).fetchOne()?.getValue(0) as Long?) ?: 0

                if (pt == 0L) {
                    create.insertInto(PersonTechnologies.PERSON_TECHNOLOGIES,
                            PersonTechnologies.PERSON_TECHNOLOGIES.PERSON_ID, PersonTechnologies.PERSON_TECHNOLOGIES.TECHNOLOGY_ID,
                            PersonTechnologies.PERSON_TECHNOLOGIES.LINES_COUNT).values(personID, techId, linesCount.get().toLong()).execute()
                } else {
                    create.update(PersonTechnologies.PERSON_TECHNOLOGIES).
                    set(PersonTechnologies.PERSON_TECHNOLOGIES.LINES_COUNT, linesCount.get().toLong()).
                    where(PersonTechnologies.PERSON_TECHNOLOGIES.PERSON_ID!!.equal(personID)).
                    and(PersonTechnologies.PERSON_TECHNOLOGIES.TECHNOLOGY_ID!!.equal(techId)).execute()
                }
            }

            callback.onFinish()
        }
    }

    fun filterLanguages(potentialLanguages: List<String>): List<String> {
        return potentialLanguages.filter { l ->
            when(l) {
                "java", "js", "rb", "py", "haml", "cs", "cpp", "h", "css", "clj", "asm", "kt", "scala" -> true
                else -> false
            }
        }
    }
}