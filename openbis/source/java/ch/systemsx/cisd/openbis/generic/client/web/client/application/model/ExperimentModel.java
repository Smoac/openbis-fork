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

import ch.systemsx.cisd.openbis.generic.client.shared.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.client.shared.ExperimentType;
import ch.systemsx.cisd.openbis.generic.client.shared.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.client.shared.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.columns.CommonExperimentColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.columns.PropertyExperimentColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.CommonColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;

/**
 * {@link ModelData} for {@link Experiment}
 * 
 * @author Izabela Adamczyk
 */
public final class ExperimentModel extends AbstractEntityModel<Experiment>
{
    private static final long serialVersionUID = 1L;

    private static final String PROPERTY_PREFIX = "property";

    public static String createID(final PropertyType propertyType)
    {
        return PROPERTY_PREFIX + propertyType.isInternalNamespace() + propertyType.getCode();
    }

    public ExperimentModel(final Experiment experiment)
    {
        set(ModelDataPropertyNames.OBJECT, experiment);

        List<IColumnDefinitionUI<Experiment>> columnsSchema = createColumnsSchema(experiment);
        for (IColumnDefinition<Experiment> column : columnsSchema)
        {
            String value = renderColumnValue(experiment, column);
            set(column.getIdentifier(), value);
        }
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

    public final static List<ExperimentModel> asExperimentModels(final List<Experiment> experiments)
    {
        final List<ExperimentModel> sampleModels =
                new ArrayList<ExperimentModel>(experiments.size());
        for (final Experiment exp : experiments)
        {
            sampleModels.add(new ExperimentModel(exp));
        }
        return sampleModels;
    }

    public static ColumnDefsAndConfigs<Experiment> createColumnsSchema(
            IMessageProvider messageProvider, ExperimentType selectedTypeOrNull)
    {
        ColumnDefsAndConfigs<Experiment> columns = new ColumnDefsAndConfigs<Experiment>();
        columns.addColumns(createCommonColumnsSchema(messageProvider), true);
        if (selectedTypeOrNull != null)
        {
            List<IColumnDefinitionUI<Experiment>> propertyColumnsSchema =
                    createPropertyColumnsSchema(selectedTypeOrNull);
            columns.addColumns(propertyColumnsSchema, false);
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
        List<IColumnDefinitionUI<Experiment>> list = createColDefList();
        for (CommonExperimentColDefKind columnKind : CommonExperimentColDefKind.values())
        {
            list.add(createColumn(columnKind, msgProviderOrNull));
        }
        return list;
    }

    private static ArrayList<IColumnDefinitionUI<Experiment>> createColDefList()
    {
        return new ArrayList<IColumnDefinitionUI<Experiment>>();
    }

    private static CommonColumnDefinition<Experiment> createColumn(CommonExperimentColDefKind columnKind,
            IMessageProvider messageProviderOrNull)
    {
        String headerText = null;
        if (messageProviderOrNull != null)
        {
            headerText = messageProviderOrNull.getMessage(columnKind.getHeaderMsgKey());
        }
        return new CommonColumnDefinition<Experiment>(columnKind, headerText);
    }

    public Experiment getBaseObject()
    {
        return get(ModelDataPropertyNames.OBJECT);
    }

}
