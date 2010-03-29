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
import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.SimpleTableModelBuilder.asText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.lemnik.eodsql.DataIterator;

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.SimpleTableModelBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Reporting plugin which shows all the chromatograms details for the chosen datasets.
 * 
 * @author Tomasz Pylak
 */
public class EICMLChromatogramsReporter extends AbstractEICMLDatastoreReportingPlugin
{
    private static final long serialVersionUID = 1L;

    public EICMLChromatogramsReporter(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
    }

    @Override
    protected final TableModel createReport(List<DatasetDescription> datasets, IEICMSRunDAO query)
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder();
        addReportHeaders(builder);
        List<EICMSRunDTO> runs = fetchRuns(datasets, query);
        for (EICMSRunDTO run : runs)
        {
            DataIterator<ChromatogramDTO> chromatograms = query.getChromatogramsForRun(run);
            addRun(builder, run, chromatograms);
        }
        return builder.getTableModel();
    }

    private List<EICMSRunDTO> fetchRuns(List<DatasetDescription> datasets, IEICMSRunDAO query)
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
        row.add(asNum(calcMin(chromatogram.getRunTimes())));
        row.add(asNum(calcMax(chromatogram.getRunTimes())));
        row.add(asNum(calcMax(chromatogram.getIntensities())));
        row.add(asNum(chromatogram.getQ1Mz()));
        row.add(asNum(chromatogram.getQ3LowMz()));
        row.add(asNum(chromatogram.getQ3HighMz()));
        row.add(asText("" + chromatogram.getPolarity()));
        return row;
    }

    private static float calcMax(float[] values)
    {
        if (values.length == 0)
        {
            return -1;
        }
        float max = values[0];
        for (int i = 1; i < values.length; i++)
        {
            if (values[i] > max)
            {
                max = values[i];
            }
        }
        return max;
    }

    private static float calcMin(float[] values)
    {
        if (values.length == 0)
        {
            return -1;
        }
        float min = values[0];
        for (int i = 1; i < values.length; i++)
        {
            if (values[i] < min)
            {
                min = values[i];
            }
        }
        return min;
    }

    private static void addReportHeaders(SimpleTableModelBuilder builder)
    {
        builder.addHeader("Label");
        builder.addHeader("RT Start");
        builder.addHeader("RT End");
        builder.addHeader("Max. Intensity");
        builder.addHeader("Q1 Mz");
        builder.addHeader("Q3Low Mz");
        builder.addHeader("Q3High Mz");
        builder.addHeader("Polarity");
    }
}
