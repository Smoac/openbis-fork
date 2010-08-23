/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.generic.server.FeatureTableBuilder;
import ch.systemsx.cisd.openbis.dss.generic.server.FeatureTableRow;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractDatastorePlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.SimpleTableModelBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.CodeAndLabel;
import ch.systemsx.cisd.openbis.dss.shared.DssScreeningUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.PlateUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingQueryDAO;

/**
 * Reporting plugin that concatenates rows of tabular files of all data sets (stripping the header
 * lines of all but the first file) and delivers the result back in the table model. Each row has
 * additional Data Set code column.
 * 
 * @author Tomasz Pylak
 * @author Franz-Josef Elmer
 */
public class ImageAnalysisMergedRowsReportingPlugin extends AbstractDatastorePlugin implements
        IReportingPluginTask
{
    private static final long serialVersionUID = 1L;

    private static final String DATA_SET_CODE_TITLE = "Data Set Code";

    private static final String PLATE_IDENTIFIER_TITLE = "Plate Identifier";

    private static final String ROW_TITLE = "Row";

    private static final String COLUMN_TITLE = "Column";

    private IEncapsulatedOpenBISService service;

    private IImagingQueryDAO dao;

    public ImageAnalysisMergedRowsReportingPlugin(Properties properties, File storeRoot)
    {
        this(properties, storeRoot, null, null);
    }

    ImageAnalysisMergedRowsReportingPlugin(Properties properties, File storeRoot,
            IEncapsulatedOpenBISService service, IImagingQueryDAO dao)
    {
        super(properties, storeRoot);
        this.service = service;
        this.dao = dao;
    }

    public TableModel createReport(List<DatasetDescription> datasets)
    {
        FeatureTableBuilder featureTableBuilder = new FeatureTableBuilder(getDAO(), getService());
        for (DatasetDescription datasetDescription : datasets)
        {
            String dataSetCode = datasetDescription.getDatasetCode();
            featureTableBuilder.addFeatureVectorsOfDataSet(dataSetCode);
        }
        List<CodeAndLabel> codeAndLabels = featureTableBuilder.getCodesAndLabels();
        List<FeatureTableRow> rows = featureTableBuilder.createFeatureTableRows();
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder(true);
        builder.addHeader(DATA_SET_CODE_TITLE);
        builder.addHeader(PLATE_IDENTIFIER_TITLE);
        builder.addHeader(ROW_TITLE);
        builder.addHeader(COLUMN_TITLE);
        for (CodeAndLabel codeAndLabel : codeAndLabels)
        {
            builder.addHeader(codeAndLabel.getLabel(), codeAndLabel.getCode());
        }
        for (FeatureTableRow row : rows)
        {
            List<ISerializableComparable> values = new ArrayList<ISerializableComparable>();
            values.add(new StringTableCell(row.getDataSetCode()));
            values.add(new StringTableCell(row.getPlateIdentifier().toString()));
            values.add(new StringTableCell(PlateUtils.translateRowNumberIntoLetterCode(row
                    .getWellPosition().getWellRow())));
            values.add(new IntegerTableCell(row.getWellPosition().getWellColumn()));
            float[] featureValues = row.getFeatureValues();
            StringTableCell nullValue = new StringTableCell("");
            for (float value : featureValues)
            {
                if (Float.isNaN(value))
                {
                    values.add(nullValue);
                } else
                {
                    values.add(new DoubleTableCell(value));
                }
            }
            builder.addRow(values);
        }
        return builder.getTableModel();
    }

    private IImagingQueryDAO getDAO()
    {
        if (dao == null)
        {
            dao = DssScreeningUtils.createQuery();
        }
        return dao;
    }

    private IEncapsulatedOpenBISService getService()
    {
        if (service == null)
        {
            service = ServiceProvider.getOpenBISService();
        }
        return service;
    }
}
