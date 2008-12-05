package ch.systemsx.cisd.openbis.generic.client.web.client.application;

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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.PersonModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.DateRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnFilter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.AddPersonDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;

/**
 * Implements person listing functionality.
 * 
 * @author Izabela Adamczyk
 */
public class PersonsView extends ContentPanel
{
    public static final String ID = GenericConstants.ID_PREFIX + "persons-view";

    public static final String ADD_BUTTON_ID = ID + "_add-button";

    public static final String TABLE_ID = ID + "_table";

    private final CommonViewContext viewContext;

    public PersonsView(final CommonViewContext viewContext)
    {
        this.viewContext = viewContext;
        setLayout(new FitLayout());
        setHeaderVisible(false);
        setHeading("List persons");
        setId(ID);
    }

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        refresh();
    }

    private void display(final List<Person> persons)
    {
        removeAll();

        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        final ColumnConfig codeColumnConfig = new ColumnConfig();
        codeColumnConfig.setId(ModelDataPropertyNames.USER_ID);
        codeColumnConfig.setHeader("User ID");
        codeColumnConfig.setWidth(ColumnConfigFactory.DEFAULT_COLUMN_WIDTH);
        configs.add(codeColumnConfig);

        final ColumnConfig firstNameColumnConfig = new ColumnConfig();
        firstNameColumnConfig.setId(ModelDataPropertyNames.FIRST_NAME);
        firstNameColumnConfig.setHeader("First Name");
        firstNameColumnConfig.setWidth(ColumnConfigFactory.DEFAULT_COLUMN_WIDTH);
        configs.add(firstNameColumnConfig);

        final ColumnConfig lastNameColumnConfig = new ColumnConfig();
        lastNameColumnConfig.setId(ModelDataPropertyNames.LAST_NAME);
        lastNameColumnConfig.setHeader("Last Name");
        lastNameColumnConfig.setWidth(ColumnConfigFactory.DEFAULT_COLUMN_WIDTH);
        configs.add(lastNameColumnConfig);

        final ColumnConfig emailNameColumnConfig = new ColumnConfig();
        emailNameColumnConfig.setId(ModelDataPropertyNames.EMAIL);
        emailNameColumnConfig.setHeader("Email");
        emailNameColumnConfig.setWidth(ColumnConfigFactory.DEFAULT_COLUMN_WIDTH);
        configs.add(emailNameColumnConfig);

        final ColumnConfig registratorColumnConfig = new ColumnConfig();
        registratorColumnConfig.setId(ModelDataPropertyNames.REGISTRATOR);
        registratorColumnConfig.setHeader("Registrator");
        registratorColumnConfig.setWidth(ColumnConfigFactory.DEFAULT_COLUMN_WIDTH);
        configs.add(registratorColumnConfig);

        final ColumnConfig registrationDateColumnConfig = new ColumnConfig();
        registrationDateColumnConfig.setId(ModelDataPropertyNames.REGISTRATION_DATE);
        registrationDateColumnConfig.setHeader("Registration Date");
        registrationDateColumnConfig.setWidth(ColumnConfigFactory.DEFAULT_COLUMN_WIDTH);
        registrationDateColumnConfig.setAlignment(HorizontalAlignment.RIGHT);
        registrationDateColumnConfig.setDateTimeFormat(DateRenderer.DEFAULT_DATE_TIME_FORMAT);
        configs.add(registrationDateColumnConfig);

        final ColumnModel cm = new ColumnModel(configs);

        final ListStore<PersonModel> store = new ListStore<PersonModel>();
        store.add(getPersonModels(persons));

        final ContentPanel cp = new ContentPanel();
        cp.setBodyBorder(false);
        cp.setHeading("Person list");
        cp.setButtonAlign(HorizontalAlignment.CENTER);
        final PersonsView personList = this;

        cp.setLayout(new FitLayout());
        cp.setSize("90%", "90%");

        final Grid<PersonModel> grid = new Grid<PersonModel>(store, cm);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        grid.setId(TABLE_ID);
        grid.setBorders(true);
        cp.add(grid);

        final Button addPersonButton =
                new Button("Add person", new SelectionListener<ComponentEvent>()
                    {
                        @Override
                        public void componentSelected(ComponentEvent ce)
                        {
                            new AddPersonDialog(viewContext, personList).show();
                        }
                    });
        addPersonButton.setId(ADD_BUTTON_ID);

        final ToolBar toolBar = new ToolBar();
        toolBar.add(new LabelToolItem("Filter:"));
        toolBar.add(new AdapterToolItem(new ColumnFilter<PersonModel>(store,
                ModelDataPropertyNames.USER_ID, "user id")));
        toolBar.add(new SeparatorToolItem());
        toolBar.add(new AdapterToolItem(addPersonButton));
        cp.setBottomComponent(toolBar);

        add(cp);
        layout();

    }

    List<PersonModel> getPersonModels(final List<Person> persons)
    {
        final List<PersonModel> pms = new ArrayList<PersonModel>();
        for (final Person p : persons)
        {
            pms.add(new PersonModel(p));
        }
        return pms;
    }

    public void refresh()
    {
        removeAll();
        add(new Text("data loading..."));
        viewContext.getService().listPersons(new ListPersonsCallback(viewContext));
    }

    //
    // Helper classes
    //

    public final class ListPersonsCallback extends AbstractAsyncCallback<List<Person>>
    {
        private ListPersonsCallback(final CommonViewContext viewContext)
        {
            super(viewContext);
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        public final void process(final List<Person> persons)
        {
            display(persons);
        }
    }
}
