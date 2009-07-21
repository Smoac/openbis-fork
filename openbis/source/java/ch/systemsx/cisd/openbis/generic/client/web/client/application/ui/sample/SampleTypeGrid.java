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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.SampleTypeColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type.AbstractEntityTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IntegerField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * Grid displaying sample types.
 * 
 * @author Tomasz Pylak
 */
public class SampleTypeGrid extends AbstractEntityTypeGrid
{
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "sample-type-browser";

    public static final String GRID_ID = BROWSER_ID + "_grid";

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final SampleTypeGrid grid = new SampleTypeGrid(viewContext);
        return grid.asDisposableWithoutToolbar();
    }

    private SampleTypeGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, GRID_ID);
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, EntityType> resultSetConfig,
            AbstractAsyncCallback<ResultSet<EntityType>> callback)
    {
        viewContext.getService().listSampleTypes(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<EntityType> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportSampleTypes(exportCriteria, callback);
    }

    @Override
    protected void registerEntityType(String code, String descriptionOrNull,
            AsyncCallback<Void> registrationCallback)
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode(code);
        sampleType.setDescription(descriptionOrNull);
        // FIXME
        sampleType.setListable(true);
        sampleType.setPartOfHierarchyDepth(0);
        sampleType.setGeneratedFromHierarchyDepth(3);

        viewContext.getService().registerSampleType(sampleType, registrationCallback);
    }

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.SAMPLE;
    }

    @Override
    protected Window createEditEntityTypeDialog(final EntityKind entityKind,
            final EntityType entityType)
    {
        assert entityType instanceof SampleType : "SampleType expected";
        final SampleType sampleType = (SampleType) entityType;
        final String code = entityType.getCode();
        String title =
                viewContext.getMessage(Dict.EDIT_TYPE_TITLE_TEMPLATE, entityKind.getDescription(),
                        code);
        return new AbstractRegistrationDialog(viewContext, title, postRegistrationCallback)
            {
                private final TextField<String> descriptionField;

                private final TextField<Number> generatedFromDepthField;

                private final TextField<Number> partOfDepthField;

                private final CheckBoxField isListableField;

                {
                    descriptionField = createDescriptionField(viewContext);
                    descriptionField.setValue(StringEscapeUtils.unescapeHtml(entityType
                            .getDescription()));
                    addField(descriptionField);

                    String listableFieldTitle = viewContext.getMessage(Dict.LISTABLE);
                    isListableField = new CheckBoxField(listableFieldTitle, false);
                    isListableField.setValue(sampleType.isListable());
                    addField(isListableField);

                    String generatedFromDepthFieldTitle =
                            viewContext.getMessage(Dict.GENERATED_FROM_HIERARCHY_DEPTH);
                    generatedFromDepthField = new IntegerField(generatedFromDepthFieldTitle, true);
                    generatedFromDepthField.setValue(sampleType.getGeneratedFromHierarchyDepth());
                    addField(generatedFromDepthField);

                    String partOfDepthFieldTitle =
                            viewContext.getMessage(Dict.PART_OF_HIERARCHY_DEPTH);
                    partOfDepthField = new IntegerField(partOfDepthFieldTitle, true);
                    partOfDepthField.setValue(sampleType.getPartOfHierarchyDepth());
                    addField(partOfDepthField);
                }

                @Override
                protected void register(AsyncCallback<Void> registrationCallback)
                {
                    sampleType.setDescription(descriptionField.getValue());
                    sampleType.setListable(isListableField.getValue());
                    sampleType.setGeneratedFromHierarchyDepth(generatedFromDepthField.getValue()
                            .intValue());
                    sampleType.setPartOfHierarchyDepth(generatedFromDepthField.getValue()
                            .intValue());
                    viewContext.getService().updateEntityType(entityKind, sampleType,
                            registrationCallback);
                }
            };
    }

    @Override
    protected IColumnDefinitionKind<EntityType>[] getStaticColumnsDefinition()
    {
        return SampleTypeColDefKind.values();
    }
}