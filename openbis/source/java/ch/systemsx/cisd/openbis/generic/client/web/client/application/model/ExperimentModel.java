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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.experiment.CommonExperimentColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.experiment.PropertyExperimentColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;

/**
 * {@link ModelData} for {@link Experiment}
 * 
 * @author Izabela Adamczyk
 */
public final class ExperimentModel extends BaseEntityModel<Experiment>
{
    private static final long serialVersionUID = 1L;

    public ExperimentModel(final Experiment entity)
    {
        super(entity, createColumnsSchema(entity));
    }

    // here we create the columns definition having just one table row. We need them only to render
    // column values (headers have been already created), so no message provider is needed.
    private static List<IColumnDefinitionUI<Experiment>> createColumnsSchema(Experiment entity)
    {
        List<IColumnDefinitionUI<Experiment>> list = createCommonColumnsSchema(null);
        for (ExperimentProperty prop : entity.getProperties())
        {
            ExperimentTypePropertyType etpt = prop.getEntityTypePropertyType();
            list.add(new PropertyExperimentColDef(etpt.getPropertyType()));
        }
        return list;
    }

    public static ColumnDefsAndConfigs<Experiment> createColumnsSchema(
            IMessageProvider messageProvider, ExperimentType selectedTypeOrNull)
    {
        List<IColumnDefinitionUI<Experiment>> commonColumnsSchema =
                createCommonColumnsSchema(messageProvider);
        ColumnDefsAndConfigs<Experiment> columns = ColumnDefsAndConfigs.create(commonColumnsSchema);
        if (selectedTypeOrNull != null)
        {
            List<IColumnDefinitionUI<Experiment>> propertyColumnsSchema =
                    createPropertyColumnsSchema(selectedTypeOrNull);
            columns.addColumns(propertyColumnsSchema);
        }
        return columns;
    }

    private static List<IColumnDefinitionUI<Experiment>> createPropertyColumnsSchema(
            ExperimentType selectedType)
    {
        List<ExperimentTypePropertyType> entityTypePropertyTypes =
                selectedType.getExperimentTypePropertyTypes();
        List<IColumnDefinitionUI<Experiment>> list = createColDefList();
        for (ExperimentTypePropertyType etpt : entityTypePropertyTypes)
        {
            list.add(new PropertyExperimentColDef(etpt.getPropertyType()));
        }
        return list;
    }

    private static List<IColumnDefinitionUI<Experiment>> createCommonColumnsSchema(
            IMessageProvider msgProviderOrNull)
    {
        return createColumnsDefinition(CommonExperimentColDefKind.values(), msgProviderOrNull);
    }

    private static ArrayList<IColumnDefinitionUI<Experiment>> createColDefList()
    {
        return new ArrayList<IColumnDefinitionUI<Experiment>>();
    }
}
