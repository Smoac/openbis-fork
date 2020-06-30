/*
 * Copyright 2020 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.etl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.systemsx.cisd.common.jython.evaluator.IJythonEvaluator;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.utilities.ICredentials;
import ch.systemsx.cisd.etlserver.DefaultStorageProcessor;
import ch.systemsx.cisd.etlserver.plugins.AbstractMaintenanceTaskWithStateFile;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetUpdatable;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.IImageGenerationAlgorithm;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.SimpleImageContainerDataConfig;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.SimpleImageDataConfig;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ThumbnailsStorageFormat;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetStructure;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.JythonBasedProcessingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchService;
import ch.systemsx.cisd.openbis.dss.screening.server.plugins.jython.ScreeningJythonIngestionService;
import ch.systemsx.cisd.openbis.dss.screening.server.plugins.jython.ScreeningPluginScriptRunnerFactory;
import ch.systemsx.cisd.openbis.dss.shared.DssScreeningUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;

/**
 * @author Franz-Josef Elmer
 */
public class MicroscopyThumbnailsCreationTask extends AbstractMaintenanceTaskWithStateFile
{

    private static final String DATA_SET_CONTAINER_TYPE_KEY = "data-set-container-type";

    private static final String DATA_SET_CONTAINER_TYPE_DEFAULT = "MICROSCOPY_IMG_CONTAINER";

    private static final String DATA_SET_THUMBNAIL_TYPE_REGEX_KEY = "data-set-thumbnail-type-regex";

    private static final String DATA_SET_THUMBNAIL_TYPE_REGEX_DEFAULT = "MICROSCOPY_IMG_THUMBNAIL";

    private static final String MAIN_DATA_SET_TYPE_REGEX_KEY = "main-data-set-type-regex";

    private static final String MAIN_DATA_SET_TYPE_REGEX_DEFAULT = "MICROSCOPY_IMG";

    private static final String MAX_NUMBER_OF_DATA_SETS_KEY = "max-number-of-data-sets";

    private static final int MAX_NUMBER_OF_DATA_SETS_DEFAULT = 1000;

    protected static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MicroscopyThumbnailsCreationTask.class);

    private Properties properties;

    private String dataSetContainerType;

    private Pattern dataSetThumbnailTypePattern;

    private Pattern mainDataSetTypePattern;

    private int maxCount;

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        this.properties = properties;
        defineStateFile(properties, getDirectoryProvider().getStoreRoot());
        dataSetContainerType = properties.getProperty(DATA_SET_CONTAINER_TYPE_KEY, DATA_SET_CONTAINER_TYPE_DEFAULT);
        dataSetThumbnailTypePattern = PropertyUtils.getPattern(properties, DATA_SET_THUMBNAIL_TYPE_REGEX_KEY, DATA_SET_THUMBNAIL_TYPE_REGEX_DEFAULT);
        mainDataSetTypePattern = PropertyUtils.getPattern(properties, MAIN_DATA_SET_TYPE_REGEX_KEY, MAIN_DATA_SET_TYPE_REGEX_DEFAULT);
        maxCount = PropertyUtils.getInt(properties, MAX_NUMBER_OF_DATA_SETS_KEY, MAX_NUMBER_OF_DATA_SETS_DEFAULT);
    }

    @Override
    public void execute()
    {
        String sessionToken = login();
        DataSetSearchCriteria searchCriteria = new DataSetSearchCriteria();
        searchCriteria.withType().withCode().thatEquals(dataSetContainerType);
        Date lastRegistrationDate = getLastRegistrationDate(new Date(0));
        String lastCode = getLastCode();
        operationLog.info("Search for data sets of type " + dataSetContainerType + " which are younger than "
                + renderTimeStamp(lastRegistrationDate) + (lastCode != null ? " and code after " + lastCode : ""));
        searchCriteria.withRegistrationDate().thatIsLaterThanOrEqualTo(lastRegistrationDate);
        if (lastCode != null)
        {
//            searchCriteria.withCode().thatsIsGreaterOrEqualTo(lastCode);
        }
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withComponents().withType();
        fetchOptions.withComponents().withExperiment();
        fetchOptions.withComponents().withSample();
        fetchOptions.sortBy().registrationDate();
        fetchOptions.sortBy().code();
        if (maxCount > 0)
        {
            fetchOptions.from(0);
            fetchOptions.count(maxCount);
        }
        SearchResult<DataSet> searchResult = getService().searchDataSets(sessionToken, searchCriteria, fetchOptions);
        List<DataSet> containerDataSets = searchResult.getObjects();
        int totalCount = searchResult.getTotalCount();
        operationLog.info(totalCount + " found."
                + (totalCount > containerDataSets.size() ? " Handle the first " + containerDataSets.size() : ""));
        int numberOfCreatedThumbnailDataSets = 0;
        for (DataSet containerDataSet : containerDataSets)
        {
            if (hasNoThumbnails(containerDataSet) && containerDataSet.getComponents().isEmpty() == false)
            {
                operationLog.info("Generate thumbnails for data set " + containerDataSet.getCode());
                createThumbnailDataSet(sessionToken, containerDataSet);
                numberOfCreatedThumbnailDataSets++;
            }
            updateTimeStampFile(renderTimeStampAndCode(containerDataSet.getRegistrationDate(), containerDataSet.getCode()));
        }
        operationLog.info(numberOfCreatedThumbnailDataSets + " thumbnail data sets have been created.");
    }

    private void createThumbnailDataSet(String sessionToken, DataSet containerDataSet)
    {
        String containerCode = containerDataSet.getCode();
        IImagingReadonlyQueryDAO imageDb = getImageDb();
        ImageDataSetStructure imageDataSetStructure = Utils.getImageDataSetStructure(imageDb, containerCode);
        SimpleImageContainerDataConfig config = new SimpleImageContainerDataConfig();

        ScreeningPluginScriptRunnerFactory scriptRunnerFactory = createScriptRunner(imageDataSetStructure, config);
        Properties ingestionServiceProperties = createIngestionServiceProperties();
        File storeRoot = getDirectoryProvider().getStoreRoot();
        ScreeningJythonIngestionService ingestionService =
                new ScreeningJythonIngestionService(ingestionServiceProperties, storeRoot, scriptRunnerFactory)
                    {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public TableModel process(IDataSetRegistrationTransactionV2 transaction,
                                Map<String, Object> parameters, DataSetProcessingContext context)
                        {
                            // Populate object 'config' by the jython script based on 'imageDataSetStructure'
                            super.process(transaction, parameters, context);
                            composeThumbnailDataSet(transaction, containerDataSet, imageDataSetStructure, config, context);
                            return null;
                        }
                    };
        IHierarchicalContentProvider contentProvider = getHierarchicalContentProvider();
        DataSetProcessingContext context =
                new DataSetProcessingContext(contentProvider, null, null, null, null, null, sessionToken);
        ingestionService.createAggregationReport(new HashMap<String, Object>(), context);
    }

    private void composeThumbnailDataSet(IDataSetRegistrationTransactionV2 transaction, DataSet containerDataSet,
            ImageDataSetStructure imageDataSetStructure, SimpleImageDataConfig config, DataSetProcessingContext context)
    {
        String containerCode = containerDataSet.getCode();
        IDataSetUpdatable container = transaction.getDataSetForUpdate(containerCode);
        DataSet mainDataSet = getMainDataSet(containerDataSet);
        IImageGenerationAlgorithm imageGenerationAlgorithm = config.getImageGenerationAlgorithm();
        if (imageGenerationAlgorithm != null)
        {
            List<IDataSet> thumbnailDatasets = new ArrayList<>();
            IImageProvider imageProvider = new ImageCache();
            IHierarchicalContent content = context.getHierarchicalContentProviderUnfiltered().asContent(containerCode);
            imageGenerationAlgorithm.setContent(content);
            ImageDataSetInformation imageDataSetInformation = new ImageDataSetInformation();
            imageDataSetInformation.setImageDataSetStructure(imageDataSetStructure);
            long t0 = System.currentTimeMillis();
            List<BufferedImage> images = imageGenerationAlgorithm.generateImages(imageDataSetInformation,
                    thumbnailDatasets, imageProvider);
            operationLog.info(images.size() + " thumbnails have been created for data set " + containerCode
                    + " in " + (System.currentTimeMillis() - t0) + " msec.");
            IDataSet dataSet = Utils.createDataSetAndImageFiles(transaction, imageGenerationAlgorithm, images);
            List<String> components = new ArrayList<>(container.getContainedDataSetCodes());
            components.add(dataSet.getDataSetCode());
            container.setContainedDataSetCodes(components);
            ISearchService searchService = transaction.getSearchService();
            Experiment experiment = mainDataSet.getExperiment();
            if (experiment != null)
            {
                dataSet.setExperiment(searchService.getExperimentByPermId(experiment.getPermId().getPermId()));
            }
            Sample sample = mainDataSet.getSample();
            if (sample != null)
            {
                dataSet.setSample(searchService.getSampleByPermId(sample.getPermId().getPermId()));
            }
        }

        List<ThumbnailsStorageFormat> thumbnailFormats = config.getImageStorageConfiguration().getThumbnailsStorageFormat();
        for (ThumbnailsStorageFormat thumbnailFormat : thumbnailFormats)
        {
            // to be implemented when needed
        }
    }

    private Properties createIngestionServiceProperties()
    {
        Properties ingestionServiceProperties = new Properties(properties);
        ingestionServiceProperties.setProperty(DefaultStorageProcessor.DO_NOT_CREATE_ORIGINAL_DIR_KEY, "true");
        return ingestionServiceProperties;
    }

    private ScreeningPluginScriptRunnerFactory createScriptRunner(ImageDataSetStructure imageDataSetStructure, 
            SimpleImageContainerDataConfig config)
    {
        ScreeningPluginScriptRunnerFactory scriptRunnerFactory = new ScreeningPluginScriptRunnerFactory(
                JythonBasedProcessingPlugin.getScriptPathProperty(properties))
            {

                private static final long serialVersionUID = 1L;

                @Override
                protected IJythonEvaluator createEvaluator(String scriptString, String[] jythonPath, 
                        DataSetProcessingContext context)
                {
                    IJythonEvaluator evaluator = super.createEvaluator(scriptString, jythonPath, context);
                    evaluator.set("image_data_set_structure", imageDataSetStructure);
                    evaluator.set("image_config", config);
                    return evaluator;
                }
            };
        return scriptRunnerFactory;
    }

    private boolean hasNoThumbnails(DataSet containerDataSet)
    {
        return getFirstMatchingComponentOrNull(containerDataSet, dataSetThumbnailTypePattern) == null;
    }

    private DataSet getMainDataSet(DataSet containerDataSet)
    {
        return getFirstMatchingComponentOrNull(containerDataSet, mainDataSetTypePattern);
    }

    private DataSet getFirstMatchingComponentOrNull(DataSet containerDataSet, Pattern pattern)
    {
        for (DataSet component : containerDataSet.getComponents())
        {
            if (pattern.matcher(component.getType().getCode()).matches())
            {
                return component;
            }
        }
        return null;
    }

    private String login()
    {
        ICredentials credentials = getEtlServerCredentials();
        return getService().login(credentials.getUserId(), credentials.getPassword());
    }

    protected IApplicationServerApi getService()
    {
        return ServiceProvider.getV3ApplicationService();
    }

    protected IDataSetDirectoryProvider getDirectoryProvider()
    {
        return ServiceProvider.getDataStoreService().getDataSetDirectoryProvider();
    }

    protected IHierarchicalContentProvider getHierarchicalContentProvider()
    {
        return ServiceProvider.getHierarchicalContentProvider();
    }

    protected IImagingReadonlyQueryDAO getImageDb()
    {
        return DssScreeningUtils.getQuery();
    }

}
