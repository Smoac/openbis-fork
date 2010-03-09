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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.QueryTool;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractDatastorePlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.SimpleTableModelBuilder;
import ch.systemsx.cisd.openbis.dss.yeastx.server.EICMLChromatogramGeneratorServlet;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GeneratedImageTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.yeastx.db.DBUtils;

/**
 * Reporting plugin which shows images for the chromatograms contained in the specified datasets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class EICMLChromatogramImagesReporter extends AbstractDatastorePlugin implements
        IReportingPluginTask
{
    private static final String CHROMATOGRAM_SERVLET = "chromatogram";
    
    private static final int MZ_1_COLUMN_INDEX = 1;

    private static final int THUMBNAIL_WIDTH = 300;

    private static final int THUMBNAIL_HEIGHT = 150;

    private static final int IMAGE_WIDTH = 1066;

    private static final int IMAGE_HEIGHT = 600;

    private static final long serialVersionUID = 1L;

    private final IEICMSRunDAO query;

    /**
     * An internal helper class for storing the information for the query parameters to the image
     * servlet.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private static class DatasetRun
    {
        private final String datasetCode;

        private final EICMSRunDTO run;

        DataIterator<ChromatogramDTO> chromatograms;

        public DatasetRun(String datasetCode, EICMSRunDTO run)
        {
            this.datasetCode = datasetCode;
            this.run = run;
        }

        String getDatasetCode()
        {
            return datasetCode;
        }

        EICMSRunDTO getRun()
        {
            return run;
        }

        DataIterator<ChromatogramDTO> getChromatograms()
        {
            return chromatograms;
        }

        void setChromatograms(DataIterator<ChromatogramDTO> chromatograms)
        {
            this.chromatograms = chromatograms;
        }
    }

    public EICMLChromatogramImagesReporter(Properties properties, File storeRoot)
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

    public TableModel createReport(List<DatasetDescription> datasets)
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder();
        addReportHeaders(builder);
        List<DatasetRun> runs = fetchRuns(datasets);
        for (DatasetRun datasetRun : runs)
        {
            DataIterator<ChromatogramDTO> chromatograms =
                    query.getChromatogramsForRun(datasetRun.getRun());
            datasetRun.setChromatograms(chromatograms);
            addRun(builder, datasetRun);
        }
        TableModel tableModel = builder.getTableModel();
        List<TableModelRow> rows = tableModel.getRows();
        Collections.sort(rows, new Comparator<TableModelRow>()
            {
                public int compare(TableModelRow r1, TableModelRow r2)
                {
                    ISerializableComparable v1 = r1.getValues().get(MZ_1_COLUMN_INDEX);
                    ISerializableComparable v2 = r2.getValues().get(MZ_1_COLUMN_INDEX);
                    return v1.compareTo(v2);
                }
            });
        return tableModel;
    }

    private List<DatasetRun> fetchRuns(List<DatasetDescription> datasets)
    {
        List<DatasetRun> runs = new ArrayList<DatasetRun>();
        for (DatasetDescription dataset : datasets)
        {
            EICMSRunDTO run = query.getMSRunByDatasetPermId(dataset.getDatasetCode());
            if (run != null)
            {
                runs.add(new DatasetRun(dataset.getDatasetCode(), run));
            }
        }
        return runs;
    }

    private static void addRun(SimpleTableModelBuilder builder, DatasetRun datasetRun)
    {
        String datasetCode = datasetRun.getDatasetCode();
        EICMSRunDTO run = datasetRun.getRun();
        DataIterator<ChromatogramDTO> chromatograms = datasetRun.getChromatograms();
        for (ChromatogramDTO chromatogram : chromatograms)
        {
            builder.addRow(createRow(builder, datasetCode, run, chromatogram));
        }
    }

    private static List<ISerializableComparable> createRow(SimpleTableModelBuilder builder,
            String datasetCode, EICMSRunDTO run, ChromatogramDTO chromatogram)
    {
        List<ISerializableComparable> row = new ArrayList<ISerializableComparable>();

        GeneratedImageTableCell imageCell =
                new GeneratedImageTableCell(
                        GenericSharedConstants.DATA_STORE_SERVER_WEB_APPLICATION_NAME + "/"
                                + CHROMATOGRAM_SERVLET, IMAGE_WIDTH, IMAGE_HEIGHT, THUMBNAIL_WIDTH,
                        THUMBNAIL_HEIGHT);

        imageCell.addParameter(EICMLChromatogramGeneratorServlet.DATASET_CODE_PARAM, datasetCode);
        imageCell.addParameter(EICMLChromatogramGeneratorServlet.CHROMATOGRAM_CODE_PARAM,
                chromatogram.getId());

        String chromatogramLabel = chromatogram.getLabel();
        row.add(SimpleTableModelBuilder.asText(chromatogramLabel));
        int mz1 = getMz1(chromatogramLabel);
        row.add(SimpleTableModelBuilder.asNum(mz1));
        int mz2 = getMz2(chromatogramLabel);
        row.add(SimpleTableModelBuilder.asNum(mz2));
        row.add(imageCell);
        return row;
    }

    // All eic_chromatograms.label values have a format [-]EIC mz1[>mz2].
    @Private
    static int getMz1(String chromatogramLabel)
    {
        String textBefore = "EIC ";
        int ixEic = chromatogramLabel.indexOf(textBefore);
        if (ixEic == -1)
        {
            return -1;
        }
        int ixGt = chromatogramLabel.indexOf(">");
        if (ixGt == -1)
        {
            ixGt = chromatogramLabel.length();
        }
        if (ixGt < ixEic)
        {
            return -1;
        }
        String text = chromatogramLabel.substring(ixEic + textBefore.length(), ixGt);
        return parseNumber(text, -1);
    }

    // All eic_chromatograms.label values have a format [-]EIC mz1[>mz2].
    @Private
    static int getMz2(String chromatogramLabel)
    {
        int ixGt = chromatogramLabel.indexOf(">");
        if (ixGt == -1)
        {
            return -1;
        }
        String text = chromatogramLabel.substring(ixGt + 1);
        return parseNumber(text, -1);
    }

    private static int parseNumber(String text, int defaultValue)
    {
        try
        {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex)
        {
            return defaultValue;
        }
    }

    private static void addReportHeaders(SimpleTableModelBuilder builder)
    {
        builder.addHeader("Label", 100);
        builder.addHeader("m/z 1", 50);
        builder.addHeader("m/z 2", 50);
        builder.addHeader("Chromatogram", THUMBNAIL_WIDTH);
    }
}
