/*
 * Copyright 2009 ETH Zuerich, CISD
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

import java.util.List;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.AddPersonDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.PersonColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * Grid displaying persons.
 * 
 * @author Piotr Buczek
 */
public class PersonGrid extends AbstractSimpleBrowserGrid<Person>
{
    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "person-browser";

    public static final String GRID_ID = BROWSER_ID + "_grid";

    public static final String ADD_BUTTON_ID = BROWSER_ID + "_add-button";

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final PersonGrid grid = new PersonGrid(viewContext);
        grid.extendBottomToolbar();
        return grid.asDisposableWithoutToolbar();
    }

    private PersonGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, GRID_ID);
        setDisplayTypeIDGenerator(DisplayTypeIDGenerator.PROJECT_BROWSER_GRID);
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        final Button addGroupButton =
                new Button(viewContext.getMessage(Dict.BUTTON_ADD_PERSON),
                        new SelectionListener<ComponentEvent>()
                            {
                                @Override
                                public void componentSelected(ComponentEvent ce)
                                {
                                    AddPersonDialog dialog =
                                            new AddPersonDialog(viewContext, new IDelegatedAction()
                                                {
                                                    public void execute()
                                                    {
                                                        refresh();
                                                    }
                                                });
                                    dialog.show();
                                }
                            });
        addGroupButton.setId(ADD_BUTTON_ID);
        addButton(addGroupButton);

        addEntityOperationsSeparator();
    }

    @Override
    protected IColumnDefinitionKind<Person>[] getStaticColumnsDefinition()
    {
        return PersonColDefKind.values();
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, Person> resultSetConfig,
            AbstractAsyncCallback<ResultSet<Person>> callback)
    {
        viewContext.getService().listPersons(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<Person> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportPersons(exportCriteria, callback);
    }

    @Override
    protected List<IColumnDefinition<Person>> getInitialFilters()
    {
        return asColumnFilters(new PersonColDefKind[]
            { PersonColDefKind.USER_ID });
    }

    @Override
    protected void showEntityViewer(final Person person, boolean editMode)
    {
        assert false : "not implemented";
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { DatabaseModificationKind.createOrDelete(ObjectKind.PERSON),
                    DatabaseModificationKind.edit(ObjectKind.PERSON) };
    }

}