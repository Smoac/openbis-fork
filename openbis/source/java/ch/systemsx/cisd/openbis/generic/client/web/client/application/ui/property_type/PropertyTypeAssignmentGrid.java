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

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.PropertyFieldFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
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

        private UnassignmentPreparationCallback(
                IViewContext<ICommonClientServiceAsync> viewContext,
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

    private final IDelegatedAction postRegistrationCallback;

    private PropertyTypeAssignmentGrid(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, GRID_ID,
                DisplayTypeIDGenerator.PROPERTY_TYPE_ASSIGNMENT_BROWSER_GRID);
        extendBottomToolbar();
        postRegistrationCallback = createRefreshGridAction();
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        addButton(createSelectedItemButton(viewContext.getMessage(Dict.BUTTON_EDIT),
                new ISelectedEntityInvoker<BaseEntityModel<EntityTypePropertyType<?>>>()
                    {

                        public void invoke(BaseEntityModel<EntityTypePropertyType<?>> selectedItem)
                        {
                            final EntityTypePropertyType<?> etpt = selectedItem.getBaseObject();
                            if (etpt.isManagedInternally())
                            {
                                final String errorMsg =
                                        "Assignments of internally managed property types cannot be edited.";
                                MessageBox.alert("Error", errorMsg, null);
                            } else
                            {
                                createEditDialog(etpt).show();
                            }
                        }
                    }));
        addButton(createSelectedItemButton(viewContext.getMessage(Dict.UNASSIGN_BUTTON_LABEL),
                new ISelectedEntityInvoker<BaseEntityModel<EntityTypePropertyType<?>>>()
                    {
                        public void invoke(BaseEntityModel<EntityTypePropertyType<?>> selectedItem)
                        {
                            final EntityTypePropertyType<?> etpt = selectedItem.getBaseObject();
                            unassignPropertyType(etpt);
                        }

                    }));

        addEntityOperationsSeparator();
    }

    private Window createEditDialog(final EntityTypePropertyType<?> etpt)
    {
        final EntityKind entityKind = etpt.getEntityKind();
        final String entityTypeCode = etpt.getEntityType().getCode();
        final String propertyTypeCode = etpt.getPropertyType().getCode();
        final String title =
                viewContext.getMessage(Dict.EDIT_PROPERTY_TYPE_ASSIGNMENT_TITLE, entityKind
                        .getDescription(), entityTypeCode, propertyTypeCode);

        return new AbstractRegistrationDialog(viewContext, title, postRegistrationCallback)
            {
                {
                    setScrollMode(Scroll.NONE);
                }

                private final boolean originalIsMandatory;

                private final CheckBox mandatoryCheckbox;

                private final Field<?> defaultValueField;

                {
                    originalIsMandatory = etpt.isMandatory();

                    mandatoryCheckbox = new CheckBox();
                    mandatoryCheckbox.setFieldLabel(viewContext.getMessage(Dict.MANDATORY));
                    mandatoryCheckbox.setValue(originalIsMandatory);
                    addField(mandatoryCheckbox);

                    // default value needs to be specified only if currently property is optional
                    if (originalIsMandatory == false)
                    {
                        defaultValueField =
                                PropertyFieldFactory.createField(etpt.getPropertyType(), false,
                                        viewContext.getMessage(Dict.DEFAULT_UPDATE_VALUE),
                                        "default_value_field", null, viewContext).get();
                        defaultValueField.setToolTip(viewContext
                                .getMessage(Dict.DEFAULT_UPDATE_VALUE_TOOLTIP));
                        addField(defaultValueField);

                        mandatoryCheckbox.addListener(Events.Change, new Listener<FieldEvent>()
                            {
                                public void handleEvent(FieldEvent be)
                                {
                                    defaultValueField.setVisible(getMandatoryValue());
                                }
                            });
                        mandatoryCheckbox.fireEvent(Events.Change);
                    } else
                    {
                        defaultValueField = null;
                    }
                }

                private String getDefaultValue()
                {
                    if (defaultValueField != null)
                    {
                        return PropertyFieldFactory.valueToString(defaultValueField.getValue());
                    }
                    return null;
                }

                private boolean getMandatoryValue()
                {
                    return mandatoryCheckbox.getValue();
                }

                @Override
                protected void register(AsyncCallback<Void> registrationCallback)
                {
                    boolean isMandatory = getMandatoryValue();
                    // update only if isMandatory value has changed
                    if (isMandatory != originalIsMandatory)
                    {
                        viewContext.getService().updatePropertyTypeAssignment(entityKind,
                                propertyTypeCode, entityTypeCode, getMandatoryValue(),
                                getDefaultValue(), registrationCallback);
                    } else
                    {
                        final String alertMsg = "Nothing to update.";
                        MessageBox.info("Information", alertMsg, null);
                    }
                }
            };
    }

    private void unassignPropertyType(final EntityTypePropertyType<?> etpt)
    {
        final EntityKind entityKind = etpt.getEntityKind();
        final String entityTypeCode = etpt.getEntityType().getCode();
        final String propertyTypeCode = etpt.getPropertyType().getCode();
        final IBrowserGridActionInvoker invoker = asActionInvoker();
        final AsyncCallback<Integer> callback =
                new UnassignmentPreparationCallback(viewContext, etpt, invoker);
        viewContext.getService().countPropertyTypedEntities(entityKind, propertyTypeCode,
                entityTypeCode, callback);
    }

    @Override
    protected IColumnDefinitionKind<EntityTypePropertyType<?>>[] getStaticColumnsDefinition()
    {
        return PropertyTypeAssignmentColDefKind.values();
    }

    @Override
    protected BaseEntityModel<EntityTypePropertyType<?>> createModel(
            GridRowModel<EntityTypePropertyType<?>> entity)
    {
        BaseEntityModel<EntityTypePropertyType<?>> model = super.createModel(entity);
        model.renderAsMultilineStringWithTooltip(PropertyTypeAssignmentColDefKind.DESCRIPTION.id());
        return model;
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
        return DatabaseModificationKind.any(ObjectKind.PROPERTY_TYPE_ASSIGNMENT);
    }

}
