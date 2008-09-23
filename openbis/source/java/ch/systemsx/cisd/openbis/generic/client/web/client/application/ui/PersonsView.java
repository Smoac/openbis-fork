/*
 * Copyright 2008 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.i18n.client.DateTimeFormat;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;

/**
 * @author Izabela Adamczyk
 */
public class PersonsView extends LayoutContainer
{

    private final GenericViewContext viewContext;

    public PersonsView(GenericViewContext viewContext)
    {
        this.viewContext = viewContext;
        setLayout(new FlowLayout(5));

    }

    private void display(final List<Person> persons)
    {
        removeAll();

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ColumnConfig codeColumnConfig = new ColumnConfig();
        codeColumnConfig.setId("userId");
        codeColumnConfig.setHeader("User ID");
        codeColumnConfig.setWidth(80);
        configs.add(codeColumnConfig);

        ColumnConfig firstNameColumnConfig = new ColumnConfig();
        firstNameColumnConfig.setId("firstName");
        firstNameColumnConfig.setHeader("First Name");
        firstNameColumnConfig.setWidth(80);
        configs.add(firstNameColumnConfig);

        ColumnConfig lastNameColumnConfig = new ColumnConfig();
        lastNameColumnConfig.setId("lastName");
        lastNameColumnConfig.setHeader("Last Name");
        lastNameColumnConfig.setWidth(80);
        configs.add(lastNameColumnConfig);

        ColumnConfig emailNameColumnConfig = new ColumnConfig();
        emailNameColumnConfig.setId("email");
        emailNameColumnConfig.setHeader("Email");
        emailNameColumnConfig.setWidth(150);
        configs.add(emailNameColumnConfig);

        GridCellRenderer<PersonModel> personRenderer = new GridCellRenderer<PersonModel>()
            {
                public String render(PersonModel model, String property, ColumnData config,
                        int rowIndex, int colIndex, ListStore<PersonModel> store)
                {
                    Person person = (Person) model.get(property);
                    return new PersonRenderer(person).toString();
                }
            };

        ColumnConfig registratorColumnConfig = new ColumnConfig();
        registratorColumnConfig.setId("registrator");
        registratorColumnConfig.setHeader("Registrator");
        registratorColumnConfig.setWidth(100);
        registratorColumnConfig.setRenderer(personRenderer);
        configs.add(registratorColumnConfig);

        ColumnConfig registrationDateColumnConfig = new ColumnConfig();
        registrationDateColumnConfig.setId("registrationDate");
        registrationDateColumnConfig.setHeader("Registration Date");
        registrationDateColumnConfig.setWidth(80);
        registrationDateColumnConfig.setDateTimeFormat(DateTimeFormat.getShortDateFormat());
        configs.add(registrationDateColumnConfig);

        ColumnModel cm = new ColumnModel(configs);

        ListStore<PersonModel> store = new ListStore<PersonModel>();
        store.add(getPersonModels(persons));

        ContentPanel cp = new ContentPanel();
        cp.setBodyBorder(false);
        cp.setHeading("Person list");
        cp.setButtonAlign(HorizontalAlignment.CENTER);
        cp.setIconStyle("icon-table");
        final PersonsView personList = this;

        cp.setLayout(new FitLayout());
        cp.setSize(700, 300);

        Grid<PersonModel> grid = new Grid<PersonModel>(store, cm);
        grid.setBorders(true);

        cp.add(grid);
        cp.addButton(new Button("Add person", new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    new PersonDialog(viewContext, personList).show();
                }
            }));

        add(cp);
        layout();

    }

    List<PersonModel> getPersonModels(List<Person> persons)
    {
        List<PersonModel> pms = new ArrayList<PersonModel>();
        for (Person p : persons)
        {
            pms.add(new PersonModel(p));
        }
        return pms;
    }

    public void refresh()
    {
        removeAll();
        add(new Text("data loading..."));
        viewContext.getService().listPersons(new AbstractAsyncCallback<List<Person>>(viewContext)
            {
                public void onSuccess(List<Person> persons)
                {
                    display(persons);
                }
            });
    }

}
