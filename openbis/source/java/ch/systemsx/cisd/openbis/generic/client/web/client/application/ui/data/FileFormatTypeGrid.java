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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.Collections;
import java.util.List;

import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ToolBarEvent;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.TypeColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type.AddTypeDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class FileFormatTypeGrid extends AbstractSimpleBrowserGrid<AbstractType>
{
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "file-format-type-browser";

    public static final String GRID_ID = BROWSER_ID + "_grid";

    public static final String ADD_NEW_TYPE_BUTTON_ID = GRID_ID + "-" + Dict.ADD_NEW_TYPE_BUTTON;
    
    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final FileFormatTypeGrid grid = new FileFormatTypeGrid(viewContext);
        Component toolbar = grid.createToolbar(EntityKind.DATA_SET);
        return grid.asDisposableWithToolbar(toolbar);
    }

    private IDelegatedAction postRegistrationCallback;
    
    private FileFormatTypeGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, GRID_ID);
        setDisplayTypeIDGenerator(DisplayTypeIDGenerator.FILE_FORMAT_TYPE_BROWSER_GRID);
        postRegistrationCallback = new IDelegatedAction()
            {
                public void execute()
                {
                    FileFormatTypeGrid.this.refresh();
                }
            };
    }

    public final Component createToolbar(final EntityKind entityKind)
    {
        ToolBar toolbar = new ToolBar();
        toolbar.add(new FillToolItem());
        TextToolItem createItem = new TextToolItem(viewContext.getMessage(Dict.ADD_NEW_TYPE_BUTTON),
                new SelectionListener<ToolBarEvent>()
                    {
                        @Override
                        public void componentSelected(ToolBarEvent ce)
                        {
                            createRegisterFileTypeDialog().show();
                        }
                    });
        createItem.setId(ADD_NEW_TYPE_BUTTON_ID);
        toolbar.add(createItem);
        return toolbar;
    }

    private Window createRegisterFileTypeDialog()
    {
        String title =
                viewContext.getMessage(Dict.ADD_TYPE_TITLE_TEMPLATE, "File");
        return new AddTypeDialog(viewContext, title, postRegistrationCallback)
            {
                @Override
                protected void register(String code, String descriptionOrNull,
                        AsyncCallback<Void> registrationCallback)
                {
                    FileFormatType type = new FileFormatType();
                    type.setCode(code);
                    type.setDescription(descriptionOrNull);
                    viewContext.getService().registerFileType(type, registrationCallback);
                }
            };
    }
    @Override
    protected IColumnDefinitionKind<AbstractType>[] getStaticColumnsDefinition()
    {
        return TypeColDefKind.values();
    }

    @Override
    protected List<IColumnDefinition<AbstractType>> getInitialFilters()
    {
        return Collections.emptyList();
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, AbstractType> resultSetConfig,
            AbstractAsyncCallback<ResultSet<AbstractType>> callback)
    {
        viewContext.getService().listFileTypes(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<AbstractType> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportFileTypes(exportCriteria, callback);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[] {};
    }

}
