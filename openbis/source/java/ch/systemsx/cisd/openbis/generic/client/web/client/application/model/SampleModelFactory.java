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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample.CommonSampleColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample.ParentContainerSampleColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample.ParentGeneratedFromSampleColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * {@link ModelData} for {@link Sample}. Allows to display static columns, properties and sample
 * parents/containers.
 * 
 * @author Izabela Adamczyk
 * @author Tomasz Pylak
 */
public final class SampleModelFactory
{
    public static ColumnDefsAndConfigs<Sample> createColumnsSchema(
            IMessageProvider messageProvider, List<PropertyType> propertyTypes,
            SampleType selectedTypeOrNull)
    {
        return new SampleModelFactory().doCreateColumnsSchema(messageProvider, propertyTypes,
                selectedTypeOrNull);
    }

    public static BaseEntityModel<Sample> createModel(Sample entity)
    {
        List<IColumnDefinitionUI<Sample>> allColumnsDefinition =
                new SampleModelFactory().createColumnsSchemaForRendering(entity);
        BaseEntityModel<Sample> model = new BaseEntityModel<Sample>(entity, allColumnsDefinition);
        model.renderAsLinkWithAnchor(CommonSampleColDefKind.EXPERIMENT.id());
        return model;
    }

    private final EntityGridModelFactory<Sample> entityGridModelFactory;

    private SampleModelFactory()
    {
        this.entityGridModelFactory =
                new EntityGridModelFactory<Sample>(CommonSampleColDefKind.values());
    }

    private List<IColumnDefinitionUI<Sample>> createColumnsSchemaForRendering(Sample sample)
    {
        List<IColumnDefinitionUI<Sample>> columns =
                entityGridModelFactory.createColumnsSchemaForRendering(sample);
        List<IColumnDefinitionUI<Sample>> parentColumns =
                createParentColumnsSchema(null, sample.getSampleType());
        columns.addAll(parentColumns);
        return columns;
    }

    private ColumnDefsAndConfigs<Sample> doCreateColumnsSchema(IMessageProvider messageProvider,
            List<PropertyType> propertyTypes, SampleType selectedTypeOrNull)
    {
        assert messageProvider != null : "message provider needed to create table headers";

        ColumnDefsAndConfigs<Sample> columns =
                entityGridModelFactory.createColumnsSchema(messageProvider, propertyTypes);
        if (selectedTypeOrNull != null)
        {
            List<IColumnDefinitionUI<Sample>> parentColumnsSchema =
                    createParentColumnsSchema(messageProvider, selectedTypeOrNull);
            columns.addColumns(parentColumnsSchema);
        }
        return columns;
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
        if (sampleType.isShowContainer())
        {
            String headerText =
                    msgProviderOrNull == null ? null : msgProviderOrNull.getMessage(Dict.PART_OF);
            list.add(new ParentContainerSampleColDef(1, headerText));
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

    public final static List<ModelData> asSampleModels(final List<Sample> samples)
    {
        final List<ModelData> sampleModels = new ArrayList<ModelData>(samples.size());
        for (final Sample sample : samples)
        {
            sampleModels.add(SampleModelFactory.createModel(sample));
        }
        return sampleModels;
    }
}
