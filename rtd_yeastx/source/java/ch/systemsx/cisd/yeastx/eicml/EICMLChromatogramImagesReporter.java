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

import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.SimpleTableModelBuilder.asText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractDatastorePlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.SimpleTableModelBuilder;
import ch.systemsx.cisd.openbis.dss.yeastx.server.EICMLChromatogramGeneratorServlet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ImageTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
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

    private static final int THUMBNAIL_SIZE = 60;

    private static final long serialVersionUID = 1L;

    private final IEICMSRunDAO query;

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
        List<EICMSRunDTO> runs = fetchRuns(datasets);
        for (EICMSRunDTO run : runs)
        {
            DataIterator<ChromatogramDTO> chromatograms = query.getChromatogramsForRun(run);
            addRun(builder, run, chromatograms);
        }
        return builder.getTableModel();
    }

    private List<EICMSRunDTO> fetchRuns(List<DatasetDescription> datasets)
    {
        List<EICMSRunDTO> runs = new ArrayList<EICMSRunDTO>();
        for (DatasetDescription dataset : datasets)
        {
            EICMSRunDTO run = query.getMSRunByDatasetPermId(dataset.getDatasetCode());
            if (run != null)
            {
                runs.add(run);
            }
        }
        return runs;
    }

    private static void addRun(SimpleTableModelBuilder builder, EICMSRunDTO run,
            DataIterator<ChromatogramDTO> chromatograms)
    {
        for (ChromatogramDTO chromatogram : chromatograms)
        {
            builder.addRow(createRow(builder, run, chromatogram));
        }
    }

    private static List<ISerializableComparable> createRow(SimpleTableModelBuilder builder,
            EICMSRunDTO run, ChromatogramDTO chromatogram)
    {
        List<ISerializableComparable> row = new ArrayList<ISerializableComparable>();

        row.add(asText(chromatogram.getLabel()));

        StringBuffer imageURL = new StringBuffer();

        imageURL.append(CHROMATOGRAM_SERVLET);
        
        imageURL.append("?");
        imageURL.append(EICMLChromatogramGeneratorServlet.DATASET_CODE_PARAM);
        imageURL.append("=");
        imageURL.append(run.getId());

        imageURL.append("&");
        imageURL.append(EICMLChromatogramGeneratorServlet.CHROMATOGRAM_CODE_PARAM);
        imageURL.append("=");
        imageURL.append(chromatogram.getId());

        imageURL.append("&");
        imageURL.append(EICMLChromatogramGeneratorServlet.IMAGE_WIDTH_PARAM);
        imageURL.append("=");
        imageURL.append(THUMBNAIL_SIZE);

        imageURL.append("&");
        imageURL.append(EICMLChromatogramGeneratorServlet.IMAGE_HEIGHT_PARAM);
        imageURL.append("=");
        imageURL.append(THUMBNAIL_SIZE);

        row.add(new ImageTableCell(imageURL.toString(), THUMBNAIL_SIZE, THUMBNAIL_SIZE));
        return row;
    }

    private static void addReportHeaders(SimpleTableModelBuilder builder)
    {
        builder.addHeader("Label");
        builder.addHeader("Chromatogram");
    }
}
