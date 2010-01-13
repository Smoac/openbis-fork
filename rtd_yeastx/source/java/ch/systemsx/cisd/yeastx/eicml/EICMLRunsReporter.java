/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.yeastx.eicml;

import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.SimpleTableModelBuilder.asNum;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractDatastorePlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.SimpleTableModelBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.yeastx.db.DBUtils;

/**
 * Reporting plugin which shows all the run details for the chosen datasets.
 * 
 * @author Tomasz Pylak
 */
public class EICMLRunsReporter extends AbstractDatastorePlugin implements IReportingPluginTask
{
    private final IEICMSRunDAO query;

    public EICMLRunsReporter(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
        this.query = createQuery(properties);
    }

    private static IEICMSRunDAO createQuery(Properties properties)
    {
        final DatabaseConfigurationContext dbContext = DBUtils.createAndInitDBContext(properties);
        DataSource dataSource = dbContext.getDataSource();
        return QueryTool.getQuery(dataSource, IEICMSRunDAO.class);
    }

    private static final long serialVersionUID = 1L;

    public TableModel createReport(List<DatasetDescription> datasets)
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder();
        addReportHeaders(builder);
        for (DatasetDescription dataset : datasets)
        {
            EICMSRunDTO run = query.getMSRunByDatasetPermId(dataset.getDatasetCode());
            if (run != null)
            {
                builder.addRow(createRow(run, dataset));
            }
        }
        return builder.getTableModel();
    }

    private static List<ISerializableComparable> createRow(EICMSRunDTO run,
            DatasetDescription dataset)
    {
        List<ISerializableComparable> row = new ArrayList<ISerializableComparable>();
        row.add(asText(dataset.getDatasetCode()));
        row.add(asText(run.getRawDataFilePath()));
        row.add(asText(run.getRawDataFileName()));
        row.add(asText(run.getInstrumentType()));
        row.add(asText(run.getInstrumentManufacturer()));
        row.add(asText(run.getInstrumentModel()));
        row.add(asText(run.getMethodIonisation()));
        row.add(asText(run.getMethodSeparation()));
        row.add(asDate(run.getAcquisitionDate()));
        row.add(asNum(run.getStartTime()));
        row.add(asNum(run.getEndTime()));
        row.add(asText(run.getOperator()));
        return row;
    }

    private static ISerializableComparable asDate(Date dateOrNull)
    {
        if (dateOrNull != null)
        {
            return SimpleTableModelBuilder.asDate(dateOrNull);
        } else
        {
            return SimpleTableModelBuilder.asText("");
        }
    }

    private static ISerializableComparable asText(String textOrNull)
    {
        String text = textOrNull == null ? "" : textOrNull;
        return SimpleTableModelBuilder.asText(text);
    }

    private static void addReportHeaders(SimpleTableModelBuilder builder)
    {
        builder.addHeader("Dataset");
        builder.addHeader("Raw data file path");
        builder.addHeader("Raw data file name");
        builder.addHeader("Instrument type");
        builder.addHeader("Instrument manufacturer");
        builder.addHeader("Instrument model");
        builder.addHeader("Method ionisation");
        builder.addHeader("Method separation");
        builder.addHeader("Acquisition date");
        builder.addHeader("Start time");
        builder.addHeader("End time");
        builder.addHeader("Operator");
    }
}
