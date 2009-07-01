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
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ToolBarEvent;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.EntityTypeColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;

/**
 * Abstarct grid displaying entity types.
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractEntityTypeGrid extends AbstractSimpleBrowserGrid<EntityType>
{
    private IDelegatedAction postRegistrationCallback;

    abstract protected EntityKind getEntityKind();

    abstract protected void registerEntityType(String code, String descriptionOrNull,
            AsyncCallback<Void> registrationCallback);

    protected AbstractEntityTypeGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            String browserId, String gridId)
    {
        super(viewContext, browserId, gridId);
        setDisplayTypeIDGenerator(DisplayTypeIDGenerator.TYPE_BROWSER_GRID);
        postRegistrationCallback = createRefreshGridAction();
        extendBottomToolbar();
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        final EntityKind entityKind = getEntityKind();
        pagingToolbar.add(new TextToolItem(viewContext.getMessage(Dict.ADD_NEW_TYPE_BUTTON),
                new SelectionListener<ToolBarEvent>()
                    {
                        @Override
                        public void componentSelected(ToolBarEvent ce)
                        {
                            createRegisterEntityTypeDialog(entityKind).show();
                        }
                    }));

        pagingToolbar.add(new SeparatorToolItem());

        Button editButton =
                createSelectedItemButton(viewContext.getMessage(Dict.EDIT_TYPE_BUTTON),
                        new ISelectedEntityInvoker<BaseEntityModel<EntityType>>()
                            {

                                public void invoke(BaseEntityModel<EntityType> selectedItem)
                                {
                                    EntityType entityType = selectedItem.getBaseObject();
                                    createEditEntityTypeDialog(entityKind, entityType).show();
                                }

                            });
        pagingToolbar.add(new AdapterToolItem(editButton));
        Button deleteButton = createDeleteButton(viewContext);
        enableButtonOnSelectedItems(deleteButton);
        pagingToolbar.add(new AdapterToolItem(deleteButton));

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
                    List<BaseEntityModel<EntityType>> types = getSelectedItems();
                    if (types.isEmpty())
                    {
                        return;
                    }
                    final List<String> selectedTypeCodes = new ArrayList<String>();
                    for (BaseEntityModel<EntityType> model : types)
                    {
                        EntityType term = model.getBaseObject();
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
        return new AddTypeDialog(viewContext, title, postRegistrationCallback)
            {
                @Override
                protected void register(String code, String descriptionOrNull,
                        AsyncCallback<Void> registrationCallback)
                {
                    registerEntityType(code, descriptionOrNull, registrationCallback);
                }
            };
    }

    private Window createEditEntityTypeDialog(final EntityKind entityKind,
            final EntityType entityType)
    {
        final String code = entityType.getCode();
        String title =
                viewContext.getMessage(Dict.EDIT_TYPE_TITLE_TEMPLATE, entityKind.getDescription(),
                        code);
        return new AbstractRegistrationDialog(viewContext, title, postRegistrationCallback)
            {
                private final TextField<String> descriptionField;
                {
                    descriptionField = createDescriptionField(viewContext);
                    descriptionField.setValue(StringEscapeUtils.unescapeHtml(entityType
                            .getDescription()));
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

    final class RefreshCallback extends AbstractAsyncCallback<Void>
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
    protected IColumnDefinitionKind<EntityType>[] getStaticColumnsDefinition()
    {
        return EntityTypeColDefKind.values();
    }

    @Override
    protected ColumnDefsAndConfigs<EntityType> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<EntityType> schema = super.createColumnsDefinition();
        schema.setGridCellRendererFor(EntityTypeColDefKind.DESCRIPTION.id(),
                createMultilineStringCellRenderer());
        return schema;
    }

    @Override
    protected List<IColumnDefinition<EntityType>> getInitialFilters()
    {
        return asColumnFilters(new EntityTypeColDefKind[]
            { EntityTypeColDefKind.CODE });
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        // grid is refreshed manually when a new type is added, so there can be no auto-refresh
        return new DatabaseModificationKind[] {};
    }

}