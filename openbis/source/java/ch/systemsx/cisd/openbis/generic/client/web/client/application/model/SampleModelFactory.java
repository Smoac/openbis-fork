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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample.AbstractParentSampleColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample.CommonSampleColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample.ParentContainerSampleColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample.ParentGeneratedFromSampleColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RealNumberFormatingParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * {@link ModelData} for {@link Sample}. Allows to display static columns, properties and sample
 * parents/containers.
 * 
 * @author Izabela Adamczyk
 * @author Tomasz Pylak
 * @author Piotr Buczek
 */
public final class SampleModelFactory
{
    public static ColumnDefsAndConfigs<Sample> createColumnsSchema(
            final IViewContext<?> viewContext, List<PropertyType> propertyTypes,
            List<AbstractParentSampleColDef> parentColumnsSchema)
    {
        return new SampleModelFactory(viewContext).doCreateColumnsSchema(viewContext,
                propertyTypes, parentColumnsSchema);
    }

    public static BaseEntityModel<Sample> createModel(final IViewContext<?> viewContext,
            GridRowModel<Sample> sampleModel, SampleType sampleType,
            RealNumberFormatingParameters realNumberFormatingParameters)
    {
        List<? extends IColumnDefinitionUI<Sample>> allColumnsDefinition =
                new SampleModelFactory(viewContext).createColumnsSchemaForRendering(sampleModel,
                        sampleType, realNumberFormatingParameters);
        BaseEntityModel<Sample> model =
                new BaseEntityModel<Sample>(sampleModel, allColumnsDefinition);
        return model;
    }

    private final EntityGridModelFactory<Sample> entityGridModelFactory;

    private SampleModelFactory(final IViewContext<?> viewContext)
    {
        this.entityGridModelFactory =
                new EntityGridModelFactory<Sample>(viewContext, CommonSampleColDefKind.values());
    }

    private List<IColumnDefinitionUI<Sample>> createColumnsSchemaForRendering(
            GridRowModel<Sample> sampleModel, SampleType sampleType,
            RealNumberFormatingParameters realNumberFormatingParameters)
    {
        List<IColumnDefinitionUI<Sample>> columns =
                entityGridModelFactory.createColumnsSchemaForRendering(sampleModel,
                        realNumberFormatingParameters);
        List<AbstractParentSampleColDef> parentColumns =
                createParentColumnsSchema(null, sampleType);
        columns.addAll(parentColumns);
        return columns;
    }

    private ColumnDefsAndConfigs<Sample> doCreateColumnsSchema(IMessageProvider messageProvider,
            List<PropertyType> propertyTypes,
            List<? extends IColumnDefinitionUI<Sample>> parentColumnsSchema)
    {
        assert messageProvider != null : "message provider needed to create table headers";

        ColumnDefsAndConfigs<Sample> columns =
                entityGridModelFactory.createColumnsSchema(messageProvider, propertyTypes);
        columns.addColumns(parentColumnsSchema);
        return columns;
    }

    public static List<AbstractParentSampleColDef> createParentColumnsSchema(
            IMessageProvider msgProviderOrNull, SampleType sampleTypeOrNull)
    {
        List<AbstractParentSampleColDef> list = createColDefList();
        if (sampleTypeOrNull != null)
        {
            for (int depth = 1; depth <= sampleTypeOrNull.getGeneratedFromHierarchyDepth(); depth++)
            {
                String headerText =
                        getParentColumnHeader(msgProviderOrNull, Dict.GENERATED_FROM, depth);
                list.add(new ParentGeneratedFromSampleColDef(depth, headerText));
            }
            if (sampleTypeOrNull.isShowContainer())
            {
                String headerText =
                        msgProviderOrNull == null ? null : msgProviderOrNull
                                .getMessage(Dict.PART_OF);
                list.add(new ParentContainerSampleColDef(1, headerText));
            }
        }
        return list;
    }

    private static ArrayList<AbstractParentSampleColDef> createColDefList()
    {
        return new ArrayList<AbstractParentSampleColDef>();
    }

    private static String getParentColumnHeader(IMessageProvider msgProviderOrNull,
            String messageKey, int depth)
    {
        return msgProviderOrNull == null ? null : msgProviderOrNull.getMessage(messageKey, depth);
    }

}
