package hds.analysis

import com.vaadin.navigator.View
import com.vaadin.ui.VerticalLayout
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.ui.Label
import com.vaadin.ui.Alignment
import com.vaadin.ui.themes.Reindeer
import org.jooq.impl.DSL
import hds.db.tables.PersonLanguages
import hds.db.tables.Language
import hds.db.tables.Person
import hds.db.connection
import hds.db.tables.Technology
import hds.db.tables.PersonTechnologies

public class ResultsView : VerticalLayout(), View {

    class object {
        public val NAME: String = "ResultsView"
    }

    val userIdLbl = Label()

    var userId: String? = null

    {
        setSizeFull()

        userIdLbl.setSizeUndefined()
        userIdLbl.setStyleName(Reindeer.LABEL_H1)
        addComponent(userIdLbl)
        setComponentAlignment(userIdLbl, Alignment.TOP_CENTER)
    }

    override fun enter(event: ViewChangeListener.ViewChangeEvent?) {
        if (event != null && event.getParameters() != null && event.getParameters().isNotEmpty()) {
            userId = event.getParameters()
            if (userId is String) {
                userIdLbl.setValue("GitHub ID: $userId")

                val connection = connection()

                val create = DSL.using(connection)
                val res = create.select(Language.LANGUAGE.NAME, PersonLanguages.PERSON_LANGUAGES.LINES_COUNT, PersonLanguages.PERSON_LANGUAGES.LANGUAGE_ID).
                from(PersonLanguages.PERSON_LANGUAGES).
                join(Language.LANGUAGE).onKey().
                join(Person.PERSON).onKey().
                where(Person.PERSON.GITHUB_ID!!.equal(userId)).
                orderBy(PersonLanguages.PERSON_LANGUAGES.LINES_COUNT!!.desc()).fetch()

                res!!.forEach {

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

                    val lbl = Label()
                    lbl.setValue("${lngName}: ${lngLinesCount} (${betterUsers + 1} Место" +
                    ", Лучше ${(1 - ((betterUsers.toDouble() + 1) / totalUsers.toDouble())) * 100}% пользователей)")
                    lbl.setSizeUndefined()
                    addComponent(lbl)
                    setComponentAlignment(lbl, Alignment.TOP_CENTER)
                }

                val resTech = create.select(Technology.TECHNOLOGY.NAME, PersonTechnologies.PERSON_TECHNOLOGIES.LINES_COUNT, PersonTechnologies.PERSON_TECHNOLOGIES.TECHNOLOGY_ID).
                from(PersonTechnologies.PERSON_TECHNOLOGIES).
                join(Technology.TECHNOLOGY).onKey().
                join(Person.PERSON).onKey().
                where(Person.PERSON.GITHUB_ID!!.equal(userId)).
                orderBy(PersonTechnologies.PERSON_TECHNOLOGIES.LINES_COUNT!!.desc()).fetch()

                resTech!!.forEach {

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

                    val lbl = Label()
                    lbl.setValue("${techName}: ${techLinesCount} (${betterUsers + 1} Место" +
                    ", Лучше ${(1 - ((betterUsers.toDouble() + 1) / totalUsers.toDouble())) * 100}% пользователей)")
                    lbl.setSizeUndefined()
                    addComponent(lbl)
                    setComponentAlignment(lbl, Alignment.TOP_CENTER)
                }

                connection.close()
            }
        }
    }


}