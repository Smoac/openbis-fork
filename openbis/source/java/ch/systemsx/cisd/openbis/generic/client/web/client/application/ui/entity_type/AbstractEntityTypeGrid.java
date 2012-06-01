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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type;

import java.util.ArrayList;
import java.util.Arrays;
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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.TextToolItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityTypeGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Abstract grid displaying entity types.
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractEntityTypeGrid<T extends EntityType> extends TypedTableGrid<T>
{
    protected IDelegatedAction postRegistrationCallback;

    abstract protected EntityKind getEntityKind();

    abstract protected void register(T entityType, AsyncCallback<Void> registrationCallback);

    protected AbstractEntityTypeGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            String browserId, String gridId)
    {
        super(viewContext, browserId, true, DisplayTypeIDGenerator.TYPE_BROWSER_GRID);

        postRegistrationCallback = createRefreshGridAction();
        extendBottomToolbar();
        allowMultipleSelection();
    }

    @Override
    public String getGridDisplayTypeID()
    {
        return createGridDisplayTypeID("-" + getEntityKind().toString());
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        final EntityKind entityKind = getEntityKind();
        addButton(new TextToolItem(viewContext.getMessage(Dict.ADD_NEW_TYPE_BUTTON),
                new SelectionListener<ButtonEvent>()
                    {
                        @Override
                        public void componentSelected(ButtonEvent ce)
                        {
                            createRegisterEntityTypeDialog(entityKind).show();
                        }
                    }));

        Button editButton =
                createSelectedItemButton(viewContext.getMessage(Dict.EDIT_TYPE_BUTTON),
                        new ISelectedEntityInvoker<BaseEntityModel<TableModelRowWithObject<T>>>()
                            {

                                @Override
                                public void invoke(BaseEntityModel<TableModelRowWithObject<T>> selectedItem,
                                        boolean keyPressed)
                                {
                                    T entityType = selectedItem.getBaseObject().getObjectOrNull();
                                    createEditEntityTypeDialog(entityKind, entityType).show();
                                }

                            });
        addButton(editButton);
        Button deleteButton = createDeleteButton(viewContext);
        enableButtonOnSelectedItems(deleteButton);
        addButton(deleteButton);

        addEntityOperationsSeparator();
    }

    protected void deleteEntityTypes(List<String> types, AsyncCallback<Void> callback)
    {
        viewContext.getCommonService().deleteEntityTypes(getEntityKind(), types, callback);
    }

    private Button createDeleteButton(final IViewContext<ICommonClientServiceAsync> context)
    {
        Button deleteButton = new Button(context.getMessage(Dict.BUTTON_DELETE));
        deleteButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    List<BaseEntityModel<TableModelRowWithObject<T>>> types = getSelectedItems();
                    if (types.isEmpty())
                    {
                        return;
                    }
                    final List<String> selectedTypeCodes = new ArrayList<String>();
                    for (BaseEntityModel<TableModelRowWithObject<T>> model : types)
                    {
                        EntityType term = model.getBaseObject().getObjectOrNull();
                        selectedTypeCodes.add(term.getCode());
                    }
                    ConfirmationDialog confirmationDialog =
                            new ConfirmationDialog(context
                                    .getMessage(Dict.DELETE_CONFIRMATION_TITLE), context
                                    .getMessage(Dict.DELETE_CONFIRMATION_MESSAGE,
                                            StringUtils.joinList(selectedTypeCodes)))
                                {
                                    @Override
                                    protected void onYes()
                                    {
                                        deleteEntityTypes(selectedTypeCodes, new RefreshCallback(
                                                viewContext));
                                    }
                                };
                    confirmationDialog.show();
                }
            });
        return deleteButton;
    }

    private Window createRegisterEntityTypeDialog(final EntityKind entityKind)
    {
        String title =
                viewContext.getMessage(Dict.ADD_TYPE_TITLE_TEMPLATE, entityKind.getDescription());

        T newEntityType = createNewEntityType();
        return createRegisterEntityTypeDialog(title, newEntityType);
    }

    abstract protected T createNewEntityType();

    protected Window createRegisterEntityTypeDialog(String title, T newEntityType)
    {
        return new AddTypeDialog<T>(viewContext, title, postRegistrationCallback, newEntityType)
            {
                @Override
                protected void register(T entityType, AsyncCallback<Void> registrationCallback)
                {
                    AbstractEntityTypeGrid.this.register(entityType, registrationCallback);
                }
            };
    }

    protected Window createEditEntityTypeDialog(final EntityKind entityKind, final T entityType)
    {
        final String code = entityType.getCode();
        String title =
                viewContext.getMessage(Dict.EDIT_TYPE_TITLE_TEMPLATE, entityKind.getDescription(),
                        code);
        return new AbstractRegistrationDialog(viewContext, title, postRegistrationCallback)
            {
                private final DescriptionField descriptionField;
                {
                    descriptionField = createDescriptionField(viewContext);
                    FieldUtil.setValueWithUnescaping(descriptionField, entityType.getDescription());
                    addField(descriptionField);
                }

                @Override
                protected void register(AsyncCallback<Void> registrationCallback)
                {
                    entityType.setDescription(descriptionField.getValue());
                    viewContext.getService().updateEntityType(entityKind, entityType,
                            registrationCallback);
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
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return columnID.toLowerCase();
    }

    @Override
    protected ColumnDefsAndConfigs<TableModelRowWithObject<T>> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<TableModelRowWithObject<T>> schema = super.createColumnsDefinition();
        schema.setGridCellRendererFor(EntityTypeGridColumnIDs.DESCRIPTION,
                createMultilineStringCellRenderer());
        return schema;
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(EntityTypeGridColumnIDs.CODE);
    }
    
    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        // grid is refreshed manually when a new type is added, so there can be no auto-refresh
        return new DatabaseModificationKind[] {};
    }

}
