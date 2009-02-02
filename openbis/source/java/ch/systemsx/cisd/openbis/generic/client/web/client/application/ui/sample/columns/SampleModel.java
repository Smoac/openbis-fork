/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.client.shared.SampleProperty;
import ch.systemsx.cisd.openbis.generic.client.shared.SampleType;
import ch.systemsx.cisd.openbis.generic.client.shared.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.AbstractEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;

/**
 * {@link ModelData} for {@link Sample}
 * 
 * @author Izabela Adamczyk
 * @author Tomasz Pylak
 */
public final class SampleModel extends AbstractEntityModel<Sample>
{
    private static final long serialVersionUID = 1L;

    public SampleModel(final Sample entity)
    {
        super(entity, createColumnsSchema(entity));
        set(ModelDataPropertyNames.SAMPLE_TYPE, entity.getSampleType() != null ? entity
                .getSampleType().getCode() : null);
    }

    public static ColumnDefsAndConfigs<Sample> createColumnsSchema(
            IMessageProvider messageProvider, SampleType selectedTypeOrNull)
    {
        assert messageProvider != null : "message provider needed to create table headers";

        List<IColumnDefinitionUI<Sample>> commonColumnsSchema =
                createCommonColumnsSchema(messageProvider);
        ColumnDefsAndConfigs<Sample> columns = ColumnDefsAndConfigs.create(commonColumnsSchema);
        if (selectedTypeOrNull != null)
        {
            List<IColumnDefinitionUI<Sample>> parentColumnsSchema =
                    createParentColumnsSchema(messageProvider, selectedTypeOrNull);
            columns.addColumns(parentColumnsSchema);

            List<IColumnDefinitionUI<Sample>> propertyColumnsSchema =
                    createPropertyColumnsSchema(selectedTypeOrNull);
            columns.addColumns(propertyColumnsSchema);
        }
        return columns;
    }

    public static ColumnDefsAndConfigs<Sample> createBasicColumnsSchema(
            IMessageProvider messageProvider)
    {
        assert messageProvider != null : "message provider needed to create table headers";

        ColumnDefsAndConfigs<Sample> columns = new ColumnDefsAndConfigs<Sample>();
        columns.addColumns(createCommonColumnsSchema(messageProvider));
        return columns;
    }

    // here we create the columns definition having just one table row. We need them only to render
    // column values (headers have been already created), so no message provider is needed.
    private static List<IColumnDefinitionUI<Sample>> createColumnsSchema(Sample sample)
    {
        List<IColumnDefinitionUI<Sample>> list = createCommonColumnsSchema(null);
        List<IColumnDefinitionUI<Sample>> parentColumns =
                createParentColumnsSchema(null, sample.getSampleType());
        list.addAll(parentColumns);
        for (SampleProperty prop : sample.getProperties())
        {
            SampleTypePropertyType etpt = prop.getEntityTypePropertyType();
            list.add(new PropertySampleColDef(etpt.getPropertyType(), etpt.isDisplayed()));
        }
        return list;
    }

    private static List<IColumnDefinitionUI<Sample>> createPropertyColumnsSchema(
            SampleType selectedType)
    {
        List<SampleTypePropertyType> sampleTypePropertyTypes =
                selectedType.getSampleTypePropertyTypes();
        List<IColumnDefinitionUI<Sample>> list = createColDefList();
        for (SampleTypePropertyType etpt : sampleTypePropertyTypes)
        {
            boolean isHidden = etpt.isDisplayed() == false;
            list.add(new PropertySampleColDef(etpt.getPropertyType(), isHidden));
        }
        return list;
    }

    public static List<IColumnDefinitionUI<Sample>> createCommonColumnsSchema(
            IMessageProvider msgProviderOrNull)
    {
        return createColumnsSchemaFrom(CommonSampleColDefKind.values(), msgProviderOrNull);
    }

    private static List<IColumnDefinitionUI<Sample>> createParentColumnsSchema(
            IMessageProvider msgProviderOrNull, SampleType sampleType)
    {
        List<IColumnDefinitionUI<Sample>> list = createColDefList();
        for (int depth = 1; depth <= sampleType.getGeneratedFromHierarchyDepth(); depth++)
        {
            String headerText =
                    getParentColumnHeader(msgProviderOrNull, Dict.GENERATED_FROM, depth);
            list.add(new ParentGeneratedFromSampleColDef(depth, headerText));
        }
        for (int depth = 1; depth <= sampleType.getPartOfHierarchyDepth(); depth++)
        {
            String headerText = getParentColumnHeader(msgProviderOrNull, Dict.PART_OF, depth);
            list.add(new ParentContainerSampleColDef(depth, headerText));
        }
        return list;
    }

    private static ArrayList<IColumnDefinitionUI<Sample>> createColDefList()
    {
        return new ArrayList<IColumnDefinitionUI<Sample>>();
    }

    private static String getParentColumnHeader(IMessageProvider msgProviderOrNull,
            String messageKey, int depth)
    {
        return msgProviderOrNull == null ? null : msgProviderOrNull.getMessage(messageKey, depth);
    }

    public final static List<SampleModel> asSampleModels(final List<Sample> samples)
    {
        final List<SampleModel> sampleModels = new ArrayList<SampleModel>(samples.size());
        for (final Sample sample : samples)
        {
            sampleModels.add(new SampleModel(sample));
        }
        return sampleModels;
    }
}
