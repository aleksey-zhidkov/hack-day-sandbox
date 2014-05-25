package hds.db

import java.sql.DriverManager
import java.sql.Connection
import org.jooq.ResultQuery
import org.jooq.impl.DSL
import hds.db.tables.Language
import hds.db.tables.PersonLanguages
import hds.db.tables.Person
import hds.db.tables.Technology
import hds.db.tables.PersonTechnologies

fun connection(): Connection {
    val url = if ("true" == System.getProperty("production")) {
        "jdbc:postgresql://postgres-atmoresume.jelasticloud.com/hds"
    } else {
        "jdbc:postgresql://localhost:5432/hds"
    }
    return DriverManager.getConnection(url, System.getProperty("db.user", "hds")!!, System.getProperty("db.pass", "hds")!!)
}

public class DB {

    fun getConnection(): Connection {
        return connection()
    }

    fun resultsLanguages(userId: String): List<String>{
        val connection = connection()

        val create = DSL.using(connection)
        val res = create.select(Language.LANGUAGE.NAME, PersonLanguages.PERSON_LANGUAGES.LINES_COUNT, PersonLanguages.PERSON_LANGUAGES.LANGUAGE_ID).
        from(PersonLanguages.PERSON_LANGUAGES).
        join(Language.LANGUAGE).onKey().
        join(Person.PERSON).onKey().
        where(Person.PERSON.GITHUB_URL!!.equal(userId)).
        orderBy(PersonLanguages.PERSON_LANGUAGES.LINES_COUNT!!.desc()).fetch()

        val languages = res!!.map {

            val lngName = it.getValue(0)
            val lngLinesCount = (it.getValue(1) as Long).toLong()
            val lngId = it.getValue(2) as Int

            val totalUsers = create.selectCount().
            from(PersonLanguages.PERSON_LANGUAGES).
            where(PersonLanguages.PERSON_LANGUAGES.LANGUAGE_ID!!.equal(lngId)).
            fetchOne()!!.getValue(0) as Int

            val betterUsers = create.selectCount().
            from(PersonLanguages.PERSON_LANGUAGES).
            where(PersonLanguages.PERSON_LANGUAGES.LANGUAGE_ID!!.equal(lngId)).
            and(PersonLanguages.PERSON_LANGUAGES.LINES_COUNT!!.gt(lngLinesCount)).
            fetchOne()!!.getValue(0) as Int

            val percentrage = (1 - ((betterUsers.toDouble() + 1) / totalUsers.toDouble())) * 100
            val pStr = if (betterUsers == 0) {
                " из 1-ого:)"
            } else if (betterUsers + 1 == totalUsers) {
                "Есть куда рости"
            } else {
                ", Лучше ${Math.round(percentrage)}% пользователей)"
            }
            "${lngName}: ${lngLinesCount} (${betterUsers + 1} Место" + pStr
        }

        connection.close()
        return languages
    }

    fun resultsTechs(userId: String): List<String> {
        val connection = connection()
        val create = DSL.using(connection)

        val resTech = create.select(Technology.TECHNOLOGY.NAME, PersonTechnologies.PERSON_TECHNOLOGIES.LINES_COUNT, PersonTechnologies.PERSON_TECHNOLOGIES.TECHNOLOGY_ID).
        from(PersonTechnologies.PERSON_TECHNOLOGIES).
        join(Technology.TECHNOLOGY).onKey().
        join(Person.PERSON).onKey().
        where(Person.PERSON.GITHUB_ID!!.equal(userId)).
        orderBy(PersonTechnologies.PERSON_TECHNOLOGIES.LINES_COUNT!!.desc()).fetch()

        val techs = resTech!!.map {

            val techName = it.getValue(0)
            val techLinesCount = (it.getValue(1) as Long).toLong()
            val techId = it.getValue(2) as Int

            val totalUsers = create.selectCount().
            from(PersonTechnologies.PERSON_TECHNOLOGIES).
            where(PersonTechnologies.PERSON_TECHNOLOGIES.TECHNOLOGY_ID!!.equal(techId)).
            fetchOne()!!.getValue(0) as Int

            val betterUsers = create.selectCount().
            from(PersonTechnologies.PERSON_TECHNOLOGIES).
            where(PersonTechnologies.PERSON_TECHNOLOGIES.TECHNOLOGY_ID!!.equal(techId)).
            and(PersonTechnologies.PERSON_TECHNOLOGIES.LINES_COUNT!!.gt(techLinesCount)).
            fetchOne()!!.getValue(0) as Int


            val percentrage = (1 - ((betterUsers.toDouble() + 1) / totalUsers.toDouble())) * 100
            val pStr = if (betterUsers == 0) {
                " из 1-ого:)"
            } else if (betterUsers + 1 == totalUsers) {
                "Есть куда рости"
            } else {
                ", Лучше ${Math.round(percentrage)}% пользователей)"
            }
            "${techName}: ${techName} (${betterUsers + 1} Место" + pStr
        }

        connection.close()
        return techs
    }
}