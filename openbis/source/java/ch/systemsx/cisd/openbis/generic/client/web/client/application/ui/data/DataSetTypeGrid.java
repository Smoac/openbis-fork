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

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.data.DataSetTypeColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type.AbstractEntityTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type.AddTypeDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DialogWithOnlineHelpUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * Grid displaying data set types.
 * 
 * @author Piotr Buczek
 * @author Izabela Adamczyk
 */
public class DataSetTypeGrid extends AbstractEntityTypeGrid<DataSetType>
{
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "data-set-type-browser";

    public static final String GRID_ID = BROWSER_ID + "_grid";

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final DataSetTypeGrid grid = new DataSetTypeGrid(viewContext);
        return grid.asDisposableWithoutToolbar();
    }

    private DataSetTypeGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, GRID_ID);
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, DataSetType> resultSetConfig,
            AbstractAsyncCallback<ResultSet<DataSetType>> callback)
    {
        viewContext.getService().listDataSetTypes(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<DataSetType> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportDataSetTypes(exportCriteria, callback);
    }

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.DATA_SET;
    }

    @Override
    protected void register(DataSetType dataSetType, AsyncCallback<Void> registrationCallback)
    {
        viewContext.getService().registerDataSetType(dataSetType, registrationCallback);
    }

    @Override
    protected DataSetType createNewEntityType()
    {
        return new DataSetType();
    }

    @Override
    protected IColumnDefinitionKind<DataSetType>[] getStaticColumnsDefinition()
    {
        return DataSetTypeColDefKind.values();
    }

    @Override
    protected Window createEditEntityTypeDialog(final EntityKind entityKind,
            final DataSetType dataSetType)
    {
        final String code = dataSetType.getCode();
        String title =
                viewContext.getMessage(Dict.EDIT_TYPE_TITLE_TEMPLATE, entityKind.getDescription(),
                        code);
        return new AbstractRegistrationDialog(viewContext, title, postRegistrationCallback)
            {
                private final DescriptionField descriptionField;

                private final TextField<String> mainDataSetPatternField;

                private final TextField<String> mainDataSetPathField;

                {
                    descriptionField = createDescriptionField(viewContext);
                    descriptionField.setValueAndUnescape(dataSetType.getDescription());
                    addField(descriptionField);

                    mainDataSetPatternField = createMainDataSetPatternField();
                    mainDataSetPatternField.setValue(StringEscapeUtils.unescapeHtml(dataSetType
                            .getMainDataSetPattern()));
                    addField(mainDataSetPatternField);

                    mainDataSetPathField = createMainDataSetPathField();
                    mainDataSetPathField.setValue(StringEscapeUtils.unescapeHtml(dataSetType
                            .getMainDataSetPath()));
                    addField(mainDataSetPathField);

                    DialogWithOnlineHelpUtils.addHelpButton(viewContext, this,
                            createHelpPageIdentifier());
                }

                @Override
                protected void register(AsyncCallback<Void> registrationCallback)
                {
                    dataSetType.setDescription(descriptionField.getValue());
                    dataSetType.setMainDataSetPattern(mainDataSetPatternField.getValue());
                    dataSetType.setMainDataSetPath(mainDataSetPathField.getValue());
                    viewContext.getService().updateEntityType(entityKind, dataSetType,
                            registrationCallback);
                }

                private HelpPageIdentifier createHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageIdentifier.HelpPageDomain.DATA_SET_TYPE,
                            HelpPageIdentifier.HelpPageAction.EDIT);
                }

            };
    }

    @Override
    protected Window createRegisterEntityTypeDialog(String title, DataSetType newEntityType)
    {
        return new AddTypeDialog<DataSetType>(viewContext, title, postRegistrationCallback,
                newEntityType)
            {

                private TextField<String> mainDataSetPatternField;

                private TextField<String> mainDataSetPathField;

                {
                    mainDataSetPatternField = createMainDataSetPatternField();
                    addField(mainDataSetPatternField);

                    mainDataSetPathField = createMainDataSetPathField();
                    addField(mainDataSetPathField);

                    DialogWithOnlineHelpUtils.addHelpButton(viewContext, this,
                            createHelpPageIdentifier());
                }

                @Override
                protected void register(DataSetType dataSetType,
                        AsyncCallback<Void> registrationCallback)
                {
                    dataSetType.setMainDataSetPath(mainDataSetPathField.getValue());
                    dataSetType.setMainDataSetPattern(mainDataSetPatternField.getValue());
                    DataSetTypeGrid.this.register(dataSetType, registrationCallback);
                }

                private HelpPageIdentifier createHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageIdentifier.HelpPageDomain.DATA_SET_TYPE,
                            HelpPageIdentifier.HelpPageAction.REGISTER);
                }
            };
    }

    private TextField<String> createMainDataSetPatternField()
    {
        TextField<String> mainDataSetPatternField = new TextField<String>();
        mainDataSetPatternField.setFieldLabel(viewContext.getMessage(Dict.MAIN_DATA_SET_PATTERN));
        GWTUtils.setToolTip(mainDataSetPatternField, viewContext
                .getMessage(Dict.MAIN_DATA_SET_PATTERN_TOOLTIP));
        mainDataSetPatternField.setEmptyText(viewContext
                .getMessage(Dict.MAIN_DATA_SET_PATTERN_EXAMPLE));
        return mainDataSetPatternField;
    }

    private TextField<String> createMainDataSetPathField()
    {
        TextField<String> mainDataSetPathField = new TextField<String>();
        mainDataSetPathField.setFieldLabel(viewContext.getMessage(Dict.MAIN_DATA_SET_PATH));
        GWTUtils.setToolTip(mainDataSetPathField, viewContext
                .getMessage(Dict.MAIN_DATA_SET_PATH_TOOLTIP));
        mainDataSetPathField.setEmptyText(viewContext.getMessage(Dict.MAIN_DATA_SET_PATH_EXAMPLE));
        return mainDataSetPathField;
    }
}
