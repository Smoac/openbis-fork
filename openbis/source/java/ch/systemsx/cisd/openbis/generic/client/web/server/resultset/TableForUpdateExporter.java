/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.server.IBasicTableDataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.TSVRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class TableForUpdateExporter
{

    @SuppressWarnings("unchecked")
    public static String getExportTableForUpdate(GridRowModels<?> rows, EntityKind entityKind,
            String lineSeparator)
    {
        switch (entityKind)
        {
            case EXPERIMENT:
                return getExperimentTableForUpdate(
                        (GridRowModels<TableModelRowWithObject<Experiment>>) rows, lineSeparator);
            case SAMPLE:
                return getSampleTableForUpdate(
                        (GridRowModels<TableModelRowWithObject<Sample>>) rows, lineSeparator);
            case DATA_SET:
                return getDataSetTableForUpdate(
                        (GridRowModels<TableModelRowWithObject<ExternalData>>) rows, lineSeparator);
            case MATERIAL:
                return "Export of materials for update is currently not supported.";
        }
        throw new IllegalArgumentException("Unspecified entity kind.");
    }

    public static String getExperimentTableForUpdate(
            GridRowModels<TableModelRowWithObject<Experiment>> rows, String lineSeparator)
    {
        TypedTableModelBuilder<Experiment> builder = new TypedTableModelBuilder<Experiment>();
        builder.addColumn(NewExperiment.IDENTIFIER_COLUMN);
        builder.addColumn("project");
        for (GridRowModel<TableModelRowWithObject<Experiment>> row : rows)
        {
            Experiment experiment = row.getOriginalObject().getObjectOrNull();
            builder.addRow(experiment);
            builder.column(NewExperiment.IDENTIFIER_COLUMN).addString(experiment.getIdentifier());
            addProperties(builder, experiment.getProperties());
        }
        return TSVRenderer.createTable(new Adapter<Experiment>(builder), lineSeparator);
    }

    public static String getSampleTableForUpdate(
            GridRowModels<TableModelRowWithObject<Sample>> rows, String lineSeparator)
    {
        Map<String, List<Sample>> samples = new TreeMap<String, List<Sample>>();
        for (GridRowModel<TableModelRowWithObject<Sample>> row : rows)
        {
            Sample sample = row.getOriginalObject().getObjectOrNull();
            String sampleType = sample.getSampleType().getCode();
            List<Sample> sampleList = samples.get(sampleType);
            if (sampleList == null)
            {
                sampleList = new ArrayList<Sample>();
                samples.put(sampleType, sampleList);
            }
            sampleList.add(sample);
        }

        StringBuilder builder = new StringBuilder();
        Set<Entry<String, List<Sample>>> entrySet = samples.entrySet();
        for (Entry<String, List<Sample>> entry : entrySet)
        {
            if (samples.size() > 1)
            {
                builder.append("[").append(entry.getKey()).append("]\n");
            }
            builder.append(getTableForSamples(entry, lineSeparator));
        }
        return builder.toString();
    }

    private static String getTableForSamples(Map.Entry<String, List<Sample>> entry,
            String lineSeparator)
    {
        TypedTableModelBuilder<Sample> builder = new TypedTableModelBuilder<Sample>();
        builder.addColumn(NewSample.IDENTIFIER_COLUMN);
        builder.addColumn(NewSample.CONTAINER);
        builder.addColumn(NewSample.PARENTS);
        builder.addColumn(NewSample.EXPERIMENT);
        List<Sample> samples = entry.getValue();
        Collections.sort(samples, new Comparator<Sample>()
            {
                @Override
                public int compare(Sample s1, Sample s2)
                {
                    return s1.getIdentifier().compareTo(s2.getIdentifier());
                }
            });
        for (Sample sample : samples)
        {
            builder.addRow(sample);
            builder.column(NewSample.IDENTIFIER_COLUMN).addString(sample.getIdentifier());
            Sample container = sample.getContainer();
            if (container != null)
            {
                builder.column(NewSample.CONTAINER).addString(container.getIdentifier());
            }
            Set<Sample> parents = sample.getParents();
            if (parents != null && parents.isEmpty() == false)
            {
                StringBuilder sb = new StringBuilder();
                for (Sample parent : parents)
                {
                    if (sb.length() > 0)
                    {
                        sb.append(",");
                    }
                    sb.append(parent.getIdentifier());
                }
                builder.column(NewSample.PARENTS).addString(sb.toString());
            }
            Experiment experiment = sample.getExperiment();
            if (experiment != null)
            {
                builder.column(NewSample.EXPERIMENT).addString(experiment.getIdentifier());
            }
            addProperties(builder, sample.getProperties());
        }
        return TSVRenderer.createTable(new Adapter<Sample>(builder), lineSeparator);
    }

    public static String getDataSetTableForUpdate(
            GridRowModels<TableModelRowWithObject<ExternalData>> rows, String lineSeparator)
    {
        TypedTableModelBuilder<ExternalData> builder = new TypedTableModelBuilder<ExternalData>();
        builder.addColumn(NewDataSet.CODE);
        builder.addColumn(NewDataSet.CONTAINER);
        builder.addColumn(NewDataSet.PARENTS);
        builder.addColumn(NewDataSet.EXPERIMENT);
        builder.addColumn(NewDataSet.SAMPLE);
        for (GridRowModel<TableModelRowWithObject<ExternalData>> row : rows)
        {
            ExternalData dataSet = row.getOriginalObject().getObjectOrNull();
            builder.addRow(dataSet);
            builder.column(NewDataSet.CODE).addString(dataSet.getCode());
            ContainerDataSet container = dataSet.tryGetContainer();
            if (container != null)
            {
                builder.column(NewDataSet.CONTAINER).addString(container.getCode());
            }
            Collection<ExternalData> parents = dataSet.getParents();
            if (parents != null && parents.isEmpty() == false)
            {
                StringBuilder sb = new StringBuilder();
                for (ExternalData parent : parents)
                {
                    if (sb.length() > 0)
                    {
                        sb.append(",");
                    }
                    sb.append(parent.getCode());
                }
                builder.column(NewDataSet.PARENTS).addString(sb.toString());
            }
            builder.column(NewDataSet.EXPERIMENT)
                    .addString(dataSet.getExperiment().getIdentifier());
            Sample sample = dataSet.getSample();
            if (sample != null)
            {
                builder.column(NewDataSet.SAMPLE).addString(sample.getIdentifier());
            }
            addProperties(builder, dataSet.getProperties());
        }
        return TSVRenderer.createTable(new Adapter<ExternalData>(builder), lineSeparator);
    }

    private static void addProperties(TypedTableModelBuilder<?> builder,
            List<IEntityProperty> properties)
    {
        builder.columnGroup("").addPropertiesForUpdate(filterUpdatableProperties(properties));
    }

    private static List<IEntityProperty> filterUpdatableProperties(List<IEntityProperty> properties)
    {
        List<IEntityProperty> filteredProperties = new ArrayList<IEntityProperty>();
        for (IEntityProperty property : properties)
        {
            if (property.isDynamic() == false && property.isDynamic() == false)
            {
                filteredProperties.add(property);
            }
        }
        return filteredProperties;
    }

    private static final class Adapter<T extends Serializable> implements IBasicTableDataProvider
    {
        private TypedTableModel<T> model;

        Adapter(TypedTableModelBuilder<T> builder)
        {
            model = builder.getModel();
        }

        @Override
        public List<String> getAllColumnTitles()
        {
            List<TableModelColumnHeader> headers = model.getHeader();
            List<String> titles = new ArrayList<String>();
            for (TableModelColumnHeader header : headers)
            {
                titles.add(header.getId());
            }
            return titles;
        }

        @Override
        public List<List<? extends Comparable<?>>> getRows()
        {
            List<TableModelRowWithObject<T>> rows = model.getRows();
            List<List<? extends Comparable<?>>> result =
                    new ArrayList<List<? extends Comparable<?>>>();
            for (TableModelRowWithObject<T> row : rows)
            {
                result.add(row.getValues());
            }
            return result;
        }
    }

}
