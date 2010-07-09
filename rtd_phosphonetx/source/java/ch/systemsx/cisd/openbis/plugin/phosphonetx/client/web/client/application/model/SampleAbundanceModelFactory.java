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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.model;

import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.EntityGridModelFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RealNumberFormatingParameters;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.columns.SampleAbundanceColDefKind;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.SampleWithPropertiesAndAbundance;

/**
 * {@link ModelData} for {@link SampleWithPropertiesAndAbundance}. Allows to display static columns
 * and properties.
 * 
 * @author Pitor Buczek
 */
public final class SampleAbundanceModelFactory
{
    public static ColumnDefsAndConfigs<SampleWithPropertiesAndAbundance> createColumnsSchema(
            final IViewContext<?> viewContext, List<PropertyType> propertyTypes)
    {
        return new SampleAbundanceModelFactory(viewContext).doCreateColumnsSchema(viewContext,
                propertyTypes);
    }

    public static BaseEntityModel<SampleWithPropertiesAndAbundance> createModel(
            final IViewContext<?> viewContext,
            GridRowModel<SampleWithPropertiesAndAbundance> entity,
            RealNumberFormatingParameters realNumberFormatingParameters)
    {
        List<IColumnDefinitionUI<SampleWithPropertiesAndAbundance>> allColumnsDefinition =
                new SampleAbundanceModelFactory(viewContext).createColumnsSchemaForRendering(
                        entity, realNumberFormatingParameters);
        BaseEntityModel<SampleWithPropertiesAndAbundance> model =
                new BaseEntityModel<SampleWithPropertiesAndAbundance>(entity, allColumnsDefinition);
        return model;
    }

    private final EntityGridModelFactory<SampleWithPropertiesAndAbundance> entityGridModelFactory;

    private SampleAbundanceModelFactory(final IViewContext<?> viewContext)
    {
        this.entityGridModelFactory =
                new EntityGridModelFactory<SampleWithPropertiesAndAbundance>(viewContext,
                        SampleAbundanceColDefKind.values());
    }

    private List<IColumnDefinitionUI<SampleWithPropertiesAndAbundance>> createColumnsSchemaForRendering(
            GridRowModel<SampleWithPropertiesAndAbundance> sample,
            RealNumberFormatingParameters realNumberFormatingParameters)
    {
        List<IColumnDefinitionUI<SampleWithPropertiesAndAbundance>> columns =
                entityGridModelFactory.createColumnsSchemaForRendering(sample,
                        realNumberFormatingParameters);
        return columns;
    }

    private ColumnDefsAndConfigs<SampleWithPropertiesAndAbundance> doCreateColumnsSchema(
            IMessageProvider messageProvider, List<PropertyType> propertyTypes)
    {
        assert messageProvider != null : "message provider needed to create table headers";

        ColumnDefsAndConfigs<SampleWithPropertiesAndAbundance> columns =
                entityGridModelFactory.createColumnsSchema(messageProvider, propertyTypes);
        return columns;
    }
}
