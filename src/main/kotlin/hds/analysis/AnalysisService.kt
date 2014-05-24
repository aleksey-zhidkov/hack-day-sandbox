package hds.analysis

import kotlin.concurrent.thread
import java.util.HashSet
import java.util.HashMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.sql.DriverManager
import org.jooq.impl.DefaultConfiguration
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import hds.db.tables.Language
import hds.db.tables.PersonLanguages
import hds.db.tables.Person
import org.jooq.Condition
import org.jooq.JoinType
import java.util.ArrayList
import java.util.Collections

public class AnalysisService() {

    fun analyze(githubUser: String, callback: AnalysisCallback) {

        thread {
            val toWait = HashSet<Thread>()
            val parser = spider.GithubParser(githubUser)
            val repoNames = parser.repoNameList(githubUser)

            val linesByExt = ConcurrentHashMap<String, AtomicInteger>()
            repoNames?.forEach { repoName ->
                val t = thread {
                    callback.onRepositoryFound()
                    try {
                        parser.cloneRepo(githubUser, repoName!!)
                        parser.getLineCountByExt(githubUser, repoName!!, linesByExt, callback)/*parser.dirList(repoName ?: "", "", null)*/
                        callback.onRepositoryProcessed()
                    }
                    catch (e : Throwable)
                    {
                        println(e.getMessage() ?: "Unknown error")
                        callback.onError(e.getMessage() ?: "Unknown error")
                    }
                }

                toWait.add(t)
            }

            val languages = filterLanguages(Collections.list(linesByExt.keys()))

            val connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/hds", "hds", "hds")
            val conf = DefaultConfiguration().set(connection).set(SQLDialect.POSTGRES)
            val create = DSL.using(connection)

            languages.forEach {l -> create
                    .mergeInto(Language.LANGUAGE)
                    .using(create.selectOne())
                    .on(Language.LANGUAGE.NAME?.equal(l))
                    .whenNotMatchedThenInsert(Language.LANGUAGE.NAME).values(l).execute()
            }

            val selectQuery = create.select(Language.LANGUAGE.ID, Language.LANGUAGE.NAME)

            //TODO
//            languages.forEach { l -> selectQuery.addConditions(Language.LANGUAGE.NAME?.equal(l))}
            val idsLanguages = selectQuery.from(Language.LANGUAGE).fetch()

            val personId = create.select(Person.PERSON.ID).from(Person.PERSON).where(Person.PERSON.NAME?.eq(githubUser)).fetchOne()!!.getValue(0) as Long

//            idsLanguages?.forEach {l -> create
//                    .mergeInto(PersonLanguages.PERSON_LANGUAGES)
//                    .using(create.selectOne())
//                    .on(PersonLanguages.PERSON_LANGUAGES.LANGUAGE_ID?.equal(l.getValue(0) as Long)?.and(PersonLanguages.PERSON_LANGUAGES.PERSON_ID?.equal(personId)))
//                    .whenNotMatchedThenInsert(PersonLanguages.PERSON_LANGUAGES.PERSON_ID, PersonLanguages.PERSON_LANGUAGES.LANGUAGE_ID, PersonLanguages.PERSON_LANGUAGES.LINES_COUNT).values(personId, l.getValue(0), l.getValue(1)).execute()
//                    .whenMatchedThenUpdate(Language.LANGUAGE.NAME).values(l).execute()
//            }

//            val res = create.mergeInto(Language.LANGUAGE.NAME, PersonLanguages.PERSON_LANGUAGES.LINES_COUNT, PersonLanguages.PERSON_LANGUAGES.LANGUAGE_ID).

            for (t in toWait) {
                t.join()
            }
            callback.onFinish()
        }
    }

    fun filterLanguages(potentialLanguages : List<String>) : List<String> {
        return potentialLanguages.filter { l ->
            when(l)
            {
                "java", "js", "rb", "py", "haml", "cs", "cpp", "h", "css", "clj", "asm" -> true
                else -> false
            }
        }
    }
}