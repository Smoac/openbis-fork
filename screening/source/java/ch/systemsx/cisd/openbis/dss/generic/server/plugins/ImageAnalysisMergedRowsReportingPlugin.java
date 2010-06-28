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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.base.mdarray.MDDoubleArray;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractDatastorePlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IRowBuilder;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.SimpleTableModelBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.PlateUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureValuesDTO;

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

    private static final class Bundle
    {
        private ImgDatasetDTO dataSet;
        private List<ImgFeatureDefDTO> featureDefinitions;
        private Map<ImgFeatureDefDTO, List<ImgFeatureValuesDTO>> featureDefToValuesMap;
    }
    
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
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder(true);
        builder.addHeader(DATA_SET_CODE_TITLE);
        builder.addHeader(PLATE_IDENTIFIER_TITLE);
        builder.addHeader(ROW_TITLE);
        builder.addHeader(COLUMN_TITLE);
        List<Bundle> bundles = new ArrayList<Bundle>();
        Set<String> featureNames = new HashSet<String>();
        for (DatasetDescription datasetDescription : datasets)
        {
            String datasetCode = datasetDescription.getDatasetCode();
            ImgDatasetDTO dataSet = getDAO().tryGetDatasetByPermId(datasetCode);
            if (dataSet == null)
            {
                throw new UserFailureException("Unkown data set " + datasetCode);
            }
            Bundle bundle = new Bundle();
            List<ImgFeatureDefDTO> featureDefinitions = getDAO().listFeatureDefsByDataSetId(dataSet.getId());
            bundle.dataSet = dataSet;
            bundle.featureDefinitions = featureDefinitions;
            bundle.featureDefToValuesMap = new HashMap<ImgFeatureDefDTO, List<ImgFeatureValuesDTO>>();
            bundles.add(bundle);
            for (ImgFeatureDefDTO featureDefinition : featureDefinitions)
            {
                String featureName = featureDefinition.getName();
                if (featureNames.contains(featureName) == false)
                {
                    builder.addHeader(featureName);
                    featureNames.add(featureName);
                }
                List<ImgFeatureValuesDTO> featureValueSets =
                        getDAO().getFeatureValues(featureDefinition);
                if (featureValueSets.isEmpty())
                {
                    throw new UserFailureException("At least one set of values for feature "
                            + featureName + " of data set " + datasetCode
                            + " expected.");
                }
                bundle.featureDefToValuesMap.put(featureDefinition, featureValueSets);
            }
        }
        for (Bundle bundle : bundles)
        {
            String dataSetCode = bundle.dataSet.getPermId();
            ImgContainerDTO container = getDAO().getContainerById(bundle.dataSet.getContainerId());
            SampleIdentifier identifier = getService().tryToGetSampleIdentifier(container.getPermId());
            for (int rowIndex = 0; rowIndex < container.getNumberOfRows(); rowIndex++)
            {
                for (int colIndex = 0; colIndex < container.getNumberOfColumns(); colIndex++)
                {
                    IRowBuilder rowBuilder = builder.addRow();
                    rowBuilder.setCell(DATA_SET_CODE_TITLE, dataSetCode);
                    rowBuilder.setCell(PLATE_IDENTIFIER_TITLE, identifier.toString());
                    rowBuilder.setCell(ROW_TITLE, PlateUtils.translateRowNumberIntoLetterCode(rowIndex + 1));
                    rowBuilder.setCell(COLUMN_TITLE, colIndex + 1);
                    for (ImgFeatureDefDTO featureDefinition : bundle.featureDefinitions)
                    {
                        List<ImgFeatureValuesDTO> featureValueSets = bundle.featureDefToValuesMap.get(featureDefinition);
                        // We take only the first set of feature value sets
                        ImgFeatureValuesDTO featureValues = featureValueSets.get(0);
                        MDDoubleArray array = featureValues.getValuesDoubleArray();
                        rowBuilder.setCell(featureDefinition.getName(), array.get(rowIndex, colIndex));
                    }
                }
            }
        }
        return builder.getTableModel();
    }

    private IImagingQueryDAO getDAO()
    {
        if (dao == null)
        {
            DataSource dataSource =
                    ServiceProvider.getDataSourceProvider().getDataSource(
                            ScreeningConstants.IMAGING_DATA_SOURCE);
            dao = QueryTool.getQuery(dataSource, IImagingQueryDAO.class);
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
