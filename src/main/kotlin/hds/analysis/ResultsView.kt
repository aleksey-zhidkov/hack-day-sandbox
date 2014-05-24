package hds.analysis

import com.vaadin.navigator.View
import com.vaadin.ui.VerticalLayout
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.ui.Label
import com.vaadin.ui.Alignment
import com.vaadin.ui.themes.Reindeer
import java.sql.DriverManager
import org.jooq.impl.DefaultConfiguration
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import hds.db.tables.PersonLanguages
import hds.db.tables.Language
import hds.db.tables.Person

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

                val connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/hds", "hds", "hds")
                val conf = DefaultConfiguration().set(connection).set(SQLDialect.POSTGRES)

                val res = DSL.using(connection).
                select(Language.LANGUAGE.NAME, PersonLanguages.PERSON_LANGUAGES.LINES_COUNT).
                        from(PersonLanguages.PERSON_LANGUAGES).
                        join(Language.LANGUAGE).on(PersonLanguages.PERSON_LANGUAGES.LANGUAGE_ID!!.equal(Language.LANGUAGE.ID)).
                        join(Person.PERSON).on(PersonLanguages.PERSON_LANGUAGES.PERSON_ID!!.equal(Person.PERSON.ID)).
                        where(Person.PERSON.GITHUB_ID!!.equal(userId)).
                        orderBy(PersonLanguages.PERSON_LANGUAGES.LINES_COUNT).fetch()

                res!!.forEach {
                    val lbl = Label()
                    lbl.setValue("${it.getValue(0)}: ${it.getValue(1)}")
                    lbl.setSizeUndefined()
                    addComponent(lbl)
                    setComponentAlignment(lbl, Alignment.TOP_CENTER)
                }

                connection.close()
            }
        }
    }


}