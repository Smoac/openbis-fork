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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type;

import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.PropertyTypeAssignmentColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * Grid with 'entity type' - 'property type' assignments.
 * 
 * @author Izabela Adamczyk
 */
public class PropertyTypeAssignmentGrid extends
        AbstractSimpleBrowserGrid<EntityTypePropertyType<?>>
{
    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID =
            GenericConstants.ID_PREFIX + "property-type-assignment-browser";

    public static final String GRID_ID = BROWSER_ID + "_grid";

    static final class UnassignmentPreparationCallback extends AbstractAsyncCallback<Integer>
    {
        private final IViewContext<ICommonClientServiceAsync> commonViewContext;
        private final EntityTypePropertyType<?> etpt;
        private final IBrowserGridActionInvoker invoker;

        private UnassignmentPreparationCallback(IViewContext<ICommonClientServiceAsync> viewContext,
                EntityTypePropertyType<?> etpt, IBrowserGridActionInvoker invoker)
        {
            super(viewContext);
            commonViewContext = viewContext;
            this.etpt = etpt;
            this.invoker = invoker;
        }

        @Override
        protected void process(Integer result)
        {
            Dialog dialog =
                    new UnassignmentConfirmationDialog(commonViewContext, etpt, result, invoker);
            dialog.show();
        }
    }

    static final class RefreshCallback extends AbstractAsyncCallback<Void>
    {
        private final IBrowserGridActionInvoker invoker;

        private RefreshCallback(IViewContext<?> viewContext, IBrowserGridActionInvoker invoker)
        {
            super(viewContext);
            this.invoker = invoker;
        }
        
        @Override
        protected void process(Void result)
        {
            invoker.refresh();
        }
    }
    
    private static final class UnassignmentConfirmationDialog extends Dialog
    {
        private final IViewContext<ICommonClientServiceAsync> viewContext;
        private final IBrowserGridActionInvoker invoker;
        private final EntityKind entityKind;
        private final String entityTypeCode;
        private final String propertyTypeCode;

        UnassignmentConfirmationDialog(IViewContext<ICommonClientServiceAsync> viewContext,
                EntityTypePropertyType<?> etpt, int numberOfProperties,
                IBrowserGridActionInvoker invoker)
        {
            this.viewContext = viewContext;
            this.invoker = invoker;
            setHeading(viewContext.getMessage(Dict.UNASSIGNMENT_CONFIRMATION_DIALOG_TITLE));
            setButtons(Dialog.YESNO);
            setHideOnButtonClick(true);
            setModal(true);
            entityKind = etpt.getEntityKind();
            entityTypeCode = etpt.getEntityType().getCode();
            propertyTypeCode = etpt.getPropertyType().getCode();
            String entityKindCode = entityKind.toString().toLowerCase();
            if (numberOfProperties == 0)
            {
                addText(viewContext.getMessage(
                        Dict.UNASSIGNMENT_CONFIRMATION_TEMPLATE_WITHOUT_PROPERTIES, entityKindCode,
                        entityTypeCode, propertyTypeCode));
            } else
            {
                addText(viewContext.getMessage(
                        Dict.UNASSIGNMENT_CONFIRMATION_TEMPLATE_WITH_PROPERTIES, entityKindCode,
                        entityTypeCode, propertyTypeCode, numberOfProperties));
            }
            setWidth(400);
        }

        @Override
        protected void onButtonPressed(Button button)
        {
            super.onButtonPressed(button);
            if (button.getItemId().equals(Dialog.YES))
            {
                viewContext.getService().unassignPropertyType(entityKind, propertyTypeCode,
                        entityTypeCode, new RefreshCallback(viewContext, invoker));
            }
        }
    }
    
    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        return new PropertyTypeAssignmentGrid(viewContext).asDisposableWithoutToolbar();
    }

    private PropertyTypeAssignmentGrid(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, GRID_ID);
        Button button = new Button(viewContext.getMessage(Dict.UNASSIGN_BUTTON_LABEL));
        button.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    List<BaseEntityModel<EntityTypePropertyType<?>>> items = getSelectedItems();
                    if (items.size() == 1)
                    {
                        BaseEntityModel<EntityTypePropertyType<?>> item = items.get(0);
                        final EntityTypePropertyType<?> etpt = item.getBaseObject();
                        EntityKind entityKind = etpt.getEntityKind();
                        String propertyTypeCode = etpt.getPropertyType().getCode();
                        String entityTypeCode = etpt.getEntityType().getCode();
                        IBrowserGridActionInvoker invoker = asActionInvoker();
                        AsyncCallback<Integer> callback =
                                new UnassignmentPreparationCallback(viewContext, etpt, invoker);
                        viewContext.getService().countPropertyTypedEntities(entityKind,
                                propertyTypeCode, entityTypeCode, callback);
                    }
                }
            });
        pagingToolbar.add(new AdapterToolItem(button));
        setDisplayTypeIDGenerator(DisplayTypeIDGenerator.PROPERTY_TYPE_ASSIGNMENT_BROWSER_GRID);

    }

    @Override
    protected IColumnDefinitionKind<EntityTypePropertyType<?>>[] getStaticColumnsDefinition()
    {
        return PropertyTypeAssignmentColDefKind.values();
    }

    @Override
    protected List<IColumnDefinition<EntityTypePropertyType<?>>> getInitialFilters()
    {
        return asColumnFilters(new PropertyTypeAssignmentColDefKind[]
            { PropertyTypeAssignmentColDefKind.PROPERTY_TYPE_CODE,
                    PropertyTypeAssignmentColDefKind.ENTITY_TYPE_CODE,
                    PropertyTypeAssignmentColDefKind.ENTITY_KIND });
    }

    @Override
    protected void listEntities(
            DefaultResultSetConfig<String, EntityTypePropertyType<?>> resultSetConfig,
            AbstractAsyncCallback<ResultSet<EntityTypePropertyType<?>>> callback)
    {
        viewContext.getService().listPropertyTypeAssignments(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<EntityTypePropertyType<?>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportPropertyTypeAssignments(exportCriteria, callback);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { DatabaseModificationKind.createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT) };
    }
    
}