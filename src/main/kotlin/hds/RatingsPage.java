package hds;

import hds.db.DB;
import hds.db.RatingRow;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;

public class RatingsPage extends WebPage {

    public RatingsPage(PageParameters pageParameters) {
        IColumn[] columns = new IColumn[3];
        columns[0] = new PropertyColumn(new Model<>("Место"), "place", "place");
        columns[1] = new PropertyColumn(new Model<>("Пользователь"), "githubId", "githubId");
        columns[2] = new PropertyColumn(new Model<>("Строки"), "lines", "lines");

        final String lng = pageParameters.get("lng").toString();
        final String tech = pageParameters.get("type").toString();
        final List<RatingRow> ratings = ("lng".equals(tech))
                ? new DB().getLngRatings(lng)
                : new DB().getTechRatings(lng);

        DefaultDataTable table = new DefaultDataTable("datatable", asList(columns),
                new SortableDataProvider() {

                    @Override
                    public Iterator iterator(int first, int count) {
                        return ratings.iterator();
                    }

                    @Override
                    public int size() {
                        return ratings.size();
                    }

                    @Override
                    public IModel model(Object object) {
                        return new Model<>((java.io.Serializable) object);
                    }
                }, 10);

        add(table);

        add(new Label("lng", new Model<Serializable>(lng)));
    }

}