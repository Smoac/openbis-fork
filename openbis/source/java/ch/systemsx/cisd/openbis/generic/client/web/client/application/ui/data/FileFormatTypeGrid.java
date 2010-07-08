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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.TypeColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type.AddTypeDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.TextToolItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;

/**
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
        return grid.asDisposableWithoutToolbar();
    }

    private final IDelegatedAction postRegistrationCallback;

    private FileFormatTypeGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, GRID_ID,
                DisplayTypeIDGenerator.FILE_FORMAT_TYPE_BROWSER_GRID);
        postRegistrationCallback = createRefreshGridAction();
        extendBottomToolbar();
        allowMultipleSelection();
    }

    private final void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        TextToolItem createItem =
                new TextToolItem(viewContext.getMessage(Dict.ADD_NEW_TYPE_BUTTON),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public void componentSelected(ButtonEvent ce)
                                {
                                    createRegisterFileTypeDialog().show();
                                }
                            });
        createItem.setId(ADD_NEW_TYPE_BUTTON_ID);
        addButton(createItem);
        Button editButton =
                createSelectedItemButton(viewContext.getMessage(Dict.EDIT_TYPE_BUTTON),
                        new ISelectedEntityInvoker<BaseEntityModel<AbstractType>>()
                            {

                                public void invoke(BaseEntityModel<AbstractType> selectedItem,
                                        boolean keyPressed)
                                {
                                    AbstractType entityType = selectedItem.getBaseObject();
                                    createEditEntityTypeDialog(entityType).show();
                                }

                            });
        addButton(editButton);

        Button deleteButton = createDeleteButton(viewContext);
        enableButtonOnSelectedItems(deleteButton);
        addButton(deleteButton);

        addEntityOperationsSeparator();
    }

    private Window createRegisterFileTypeDialog()
    {
        String title = viewContext.getMessage(Dict.ADD_TYPE_TITLE_TEMPLATE, "File");
        return new AddTypeDialog<FileFormatType>(viewContext, title, postRegistrationCallback,
                new FileFormatType())
            {
                @Override
                protected void register(FileFormatType type,
                        AsyncCallback<Void> registrationCallback)
                {
                    viewContext.getService().registerFileType(type, registrationCallback);
                }

            };
    }

    private Button createDeleteButton(final IViewContext<ICommonClientServiceAsync> context)
    {
        Button deleteButton = new Button(context.getMessage(Dict.BUTTON_DELETE));
        deleteButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    List<BaseEntityModel<AbstractType>> types = getSelectedItems();
                    if (types.isEmpty())
                    {
                        return;
                    }
                    final List<String> selectedTypeCodes = new ArrayList<String>();
                    for (BaseEntityModel<AbstractType> model : types)
                    {
                        AbstractType term = model.getBaseObject();
                        selectedTypeCodes.add(term.getCode());
                    }
                    ConfirmationDialog confirmationDialog =
                            new ConfirmationDialog(context
                                    .getMessage(Dict.DELETE_CONFIRMATION_TITLE), context
                                    .getMessage(Dict.DELETE_CONFIRMATION_MESSAGE, StringUtils
                                            .joinList(selectedTypeCodes)))
                                {
                                    @Override
                                    protected void onYes()
                                    {
                                        viewContext.getCommonService()
                                                .deleteFileFormatTypes(selectedTypeCodes,
                                                        new RefreshCallback(viewContext));
                                    }
                                };
                    confirmationDialog.show();
                }
            });
        return deleteButton;
    }

    private Window createEditEntityTypeDialog(final AbstractType type)
    {
        final String code = type.getCode();
        String title =
                viewContext.getMessage(Dict.EDIT_TYPE_TITLE_TEMPLATE, type.getDescription(), code);
        return new AbstractRegistrationDialog(viewContext, title, postRegistrationCallback)
            {
                private final DescriptionField descriptionField;
                {
                    descriptionField = createDescriptionField(viewContext);
                    descriptionField.setValueAndUnescape(type.getDescription());
                    addField(descriptionField);

                }

                @Override
                protected void register(AsyncCallback<Void> registrationCallback)
                {
                    type.setDescription(descriptionField.getValue());
                    viewContext.getService().updateFileFormatType(type, registrationCallback);
                }
            };
    }

    private final class RefreshCallback extends AbstractAsyncCallback<Void>
    {
        private RefreshCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(Void result)
        {
            refresh();
        }
    }

    @Override
    protected IColumnDefinitionKind<AbstractType>[] getStaticColumnsDefinition()
    {
        return TypeColDefKind.values();
    }

    @Override
    protected ColumnDefsAndConfigs<AbstractType> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<AbstractType> schema = super.createColumnsDefinition();
        schema.setGridCellRendererFor(TypeColDefKind.DESCRIPTION.id(),
                createMultilineStringCellRenderer());
        return schema;
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
