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

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.createOrDelete;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;

import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.PropertyTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.PropertyTypeColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.XmlField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * Grid displaying property types.
 * 
 * @author Tomasz Pylak
 */
public class PropertyTypeGrid extends AbstractSimpleBrowserGrid<PropertyType>
{
    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "property-type-browser";

    public static final String GRID_ID = BROWSER_ID + "_grid";

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        return new PropertyTypeGrid(viewContext).asDisposableWithoutToolbar();
    }

    private final IDelegatedAction postRegistrationCallback;

    private PropertyTypeGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, GRID_ID, DisplayTypeIDGenerator.PROPERTY_TYPE_BROWSER_GRID);
        extendBottomToolbar();
        postRegistrationCallback = createRefreshGridAction();
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        final Button addButton =
                new Button(viewContext.getMessage(Dict.BUTTON_ADD, "Property Type"),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public void componentSelected(ButtonEvent ce)
                                {
                                    DispatcherHelper.dispatchNaviEvent(new ComponentProvider(
                                            viewContext).getPropertyTypeRegistration());
                                }
                            });
        addButton(addButton);

        addButton(createSelectedItemButton(viewContext.getMessage(Dict.BUTTON_EDIT),
                new ISelectedEntityInvoker<BaseEntityModel<PropertyType>>()
                    {

                        public void invoke(BaseEntityModel<PropertyType> selectedItem,
                                boolean keyPressed)
                        {
                            final PropertyType propertyType = selectedItem.getBaseObject();
                            if (propertyType.isManagedInternally())
                            {
                                final String errorMsg =
                                        "Internally managed property types cannot be edited.";
                                MessageBox.alert("Error", errorMsg, null);
                            } else
                            {
                                createEditDialog(propertyType).show();
                            }
                        }
                    }));

        addButton(createSelectedItemsButton(viewContext.getMessage(Dict.BUTTON_DELETE),
                new AbstractCreateDialogListener()
                    {

                        @Override
                        protected Dialog createDialog(List<PropertyType> propertyTypes,
                                IBrowserGridActionInvoker invoker)
                        {
                            return new PropertyTypeListDeletionConfirmationDialog(viewContext,
                                    propertyTypes, createDeletionCallback(invoker));
                        }

                        @Override
                        protected boolean validateSelectedData(List<PropertyType> data)
                        {
                            String errorMsg =
                                    "Internally managed property types cannot be deleted.";
                            for (PropertyType propertyType : data)
                            {
                                if (propertyType.isManagedInternally())
                                {
                                    MessageBox.alert("Error", errorMsg, null);
                                    return false;
                                }
                            }
                            return true;
                        }
                    }));
        allowMultipleSelection(); // we allow deletion of multiple property types

        addEntityOperationsSeparator();
    }

    private Window createEditDialog(final PropertyType propertyType)
    {
        final String code = propertyType.getCode();
        final String description = propertyType.getDescription();
        final String label = propertyType.getLabel();
        final String title = viewContext.getMessage(Dict.EDIT_TITLE, "Property Type", code);
        final DataTypeCode dataTypeCode = propertyType.getDataType().getCode();

        return new AbstractRegistrationDialog(viewContext, title, postRegistrationCallback)
            {
                private final DescriptionField descriptionField;

                private final TextField<String> labelField;

                private XmlField xmlSchemaField;

                private XmlField xslTransformationsField;

                {
                    boolean mandatory = true;

                    labelField = createTextField(viewContext.getMessage(Dict.LABEL), mandatory);
                    labelField.setMaxLength(GenericConstants.COLUMN_LABEL);
                    labelField.setValue(StringEscapeUtils.unescapeHtml(label));
                    addField(labelField);

                    descriptionField = createDescriptionField(viewContext, mandatory);
                    descriptionField.setValue(StringEscapeUtils.unescapeHtml(description));
                    addField(descriptionField);

                    if (dataTypeCode == DataTypeCode.XML)
                    {
                        xmlSchemaField = createXmlSchemaField();
                        xmlSchemaField.setValueAndUnescape(propertyType.getSchema());
                        addField(xmlSchemaField);

                        xslTransformationsField = createXslTransformationsField();
                        xslTransformationsField.setValueAndUnescape(propertyType
                                .getTransformation());
                        addField(xslTransformationsField);
                    }
                }

                @Override
                protected void register(AsyncCallback<Void> registrationCallback)
                {
                    propertyType.setDescription(descriptionField.getValue());
                    propertyType.setLabel(labelField.getValue());
                    if (dataTypeCode == DataTypeCode.XML)
                    {
                        propertyType.setSchema(xmlSchemaField.getValue());
                        propertyType.setTransformation(xslTransformationsField.getValue());
                    }

                    viewContext.getService().updatePropertyType(propertyType, registrationCallback);
                }

                // XML data type specific

                private final XmlField createXmlSchemaField()
                {
                    return new XmlField(viewContext.getMessage(Dict.XML_SCHEMA), false);
                }

                private final XmlField createXslTransformationsField()
                {
                    return new XmlField(viewContext.getMessage(Dict.XSLT), false);
                }
            };
    }

    @Override
    protected IColumnDefinitionKind<PropertyType>[] getStaticColumnsDefinition()
    {
        return PropertyTypeColDefKind.values();
    }

    @Override
    protected List<IColumnDefinition<PropertyType>> getInitialFilters()
    {
        return asColumnFilters(new PropertyTypeColDefKind[]
            { PropertyTypeColDefKind.LABEL, PropertyTypeColDefKind.CODE,
                    PropertyTypeColDefKind.DATA_TYPE });
    }

    @Override
    protected PropertyTypeModel createModel(GridRowModel<PropertyType> entity)
    {
        PropertyTypeModel model = new PropertyTypeModel(entity, getStaticColumnsDefinition());
        model.renderAsMultilineStringWithTooltip(PropertyTypeColDefKind.DESCRIPTION.id());
        model.renderAsMultilineStringWithTooltip(PropertyTypeColDefKind.SCHEMA.id());
        model.renderAsMultilineStringWithTooltip(PropertyTypeColDefKind.TRANSFORMATION.id());
        return model;
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, PropertyType> resultSetConfig,
            AbstractAsyncCallback<ResultSet<PropertyType>> callback)
    {
        viewContext.getService().listPropertyTypes(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<PropertyType> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportPropertyTypes(exportCriteria, callback);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { createOrDelete(ObjectKind.PROPERTY_TYPE), edit(ObjectKind.PROPERTY_TYPE),
                    createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                    edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT) };
    }
}
