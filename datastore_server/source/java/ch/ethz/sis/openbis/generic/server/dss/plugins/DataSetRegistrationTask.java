/*
 * Copyright 2016 ETH Zuerich, SIS
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
//TODO implement db table for data source experiment perm id to harvester perm_id
//TODO try to implement sample relationship sync like DS rel. sync
//TODO check if already loaded harvesterEntityGraph can be used in most cases
//TODO check if harvesterEntityGraph can be partially loaded as required
//TODO correctly handle saving of last sync timestamp
//TODO different last sync timestamp files for different plugins - 
//this is actually handled by setting up different harvester plugins with different files
//TODO when deleting make sure we are not emptying all the trash but just the ones we synchronized
//TODO checksums for data set files
//TODO check why the material modification date is not during after material sync
package ch.ethz.sis.openbis.generic.server.dss.plugins;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownload;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadReader;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;
import ch.ethz.sis.openbis.generic.server.EntityRetriever;
import ch.ethz.sis.openbis.generic.server.dss.plugins.ResourceListParserData.Connection;
import ch.ethz.sis.openbis.generic.server.dss.plugins.ResourceListParserData.DataSetWithConnections;
import ch.ethz.sis.openbis.generic.server.dss.plugins.ResourceListParserData.ExperimentWithConnections;
import ch.ethz.sis.openbis.generic.server.dss.plugins.ResourceListParserData.MaterialWithLastModificationDate;
import ch.ethz.sis.openbis.generic.server.dss.plugins.ResourceListParserData.ProjectWithConnections;
import ch.ethz.sis.openbis.generic.server.dss.plugins.ResourceListParserData.SampleWithConnections;
import ch.ethz.sis.openbis.generic.shared.entitygraph.EdgeNodePair;
import ch.ethz.sis.openbis.generic.shared.entitygraph.EntityGraph;
import ch.ethz.sis.openbis.generic.shared.entitygraph.Node;
import ch.systemsx.cisd.common.concurrent.ITaskExecutor;
import ch.systemsx.cisd.common.concurrent.ParallelizedExecutor;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.http.JettyHttpClientFactory;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.parser.MemorySizeFormatter;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.common.ssl.SslCertificateHelper;
import ch.systemsx.cisd.dbmigration.SimpleDatabaseConfigurationContext;
import ch.systemsx.cisd.etlserver.plugins.AbstractDataSetDeletionPostProcessingMaintenanceTask;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.ConversionUtils;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetUpdatable;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.IngestionService;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.PluginTaskInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISampleImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IMasterDataRegistrationTransaction;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.EncapsulatedCommonServer;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.MasterDataRegistrationService;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialWithType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PropertyBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.AtomicEntityOperationDetailsBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

public class DataSetRegistrationTask<T extends DataSetInformation> implements IMaintenanceTask
{
    protected static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AbstractDataSetDeletionPostProcessingMaintenanceTask.class);
    // private static final String DATA_SOURCE_URI = "https://bs-mbpr121.d.ethz.ch:8444/datastore_server/re-sync"; //
    // "http://localhost:8889/datastore_server/re-sync";

    final DateFormat formater = new SimpleDateFormat("dd-MM-yy HH-mm-ss", Locale.ENGLISH);

    private static final String DATA_SOURCE_SECTION_NAME = "data-source";

    private static final String DATA_SOURCE_URL_PROPERTY_NAME = "server-url";

    private static final String DATA_SOURCE_OPENBIS_URL_PROPERTY_NAME = "openbis-url";

    private static final String DATA_SOURCE_DSS_URL_PROPERTY_NAME = "dss-url";

    private static final String DATA_SOURCE_SPACE_PROPERTY_NAME = "space";

    private static final String DATA_SOURCE_PREFIX_PROPERTY_NAME = "prefix";

    private static final String DATA_SOURCE_AUTH_REALM_PROPERTY_NAME = "auth-realm";

    private static final String DATA_SOURCE_AUTH_USER_PROPERTY_NAME = "auth-user";

    private static final String DATA_SOURCE_AUTH_PASS_PROPERTY_NAME = "auth-pass";

    private static final String HARVESTER_SECTION_NAME = "harvester";

    private static final String HARVESTER_SPACE_PROPERTY_NAME = "space";

    private static final String HARVESTER_TEMP_DIR_PROPERTY_NAME = "tmp-dir";

    private static final String DEFAULT_LAST_SYNC_TIMESTAMP_FILE = "last-sync-timestamp-file.txt";
    private static final String HARVESTER_LAST_SYNC_TIMESTAMP_FILE = "last-sync-timestamp-file";

    private File lastSyncTimestampFile;

    private File newLastSyncTimeStampFile;

    private String dataSourceURI;

    private String dataSourceOpenbisURL;

    private String dataSourceDSSURL;

    private String realm;

    private String user;

    private String pass;

    private String dataSourceSpace;

    private String dataSourcePrefix;

    private String harvesterSpace;

    private String harvesterTempDir;

    private File storeRoot;

    private IEncapsulatedOpenBISService service;

    private DataSetProcessingContext context;

    private ResourceListParserData data;

    private Date lastSyncTimestamp;

    private SimpleDatabaseConfigurationContext dbConfigurationContext;

    private JdbcTemplate jdbcTemplate;

    private void initializePluginProperties()
    {
        dataSourceURI = "https://localhost:8444/datastore_server/re-sync";
        realm = "OAI-PMH";
        user = "admin";
        pass = "aa";
        dataSourceSpace = "DEFAULT";
        harvesterSpace = "DST";
    }

    public static void main(String[] args)
    {

        DataSetRegistrationTask dsrt = new DataSetRegistrationTask();

        // File storeRoot = new File("targets/store");
        // File temp = new File(storeRoot, "harvester-tmp");
        // temp.mkdirs();
        // File file = new File(temp, "20160630103851669-100");
        // file.mkdirs();
        //
        // System.exit(0);
        
        Properties properties = new Properties();
        properties.put(PluginTaskInfoProvider.STOREROOT_DIR_KEY, "targets/store");

        Document doc;
        try
        {
            dsrt.initializePluginProperties();
            doc = dsrt.getResourceListAsXMLDoc();
            ResourceListParser parser = ResourceListParser.create("DST", new Date(0L));
            parser.parseResourceListDocument(doc);
        } catch (XPathExpressionException | ParserConfigurationException | SAXException | IOException | URISyntaxException | InterruptedException
                | TimeoutException | ExecutionException e)
        {
            e.printStackTrace();
        }
    }

    private void extractHarvesterProperties(Properties properties)
    {
        SectionProperties harvesterSectionProperties =
                PropertyParametersUtil.extractSingleSectionProperties(properties,
                        HARVESTER_SECTION_NAME, false);
        harvesterSpace = harvesterSectionProperties.getProperties().getProperty(HARVESTER_SPACE_PROPERTY_NAME);
        harvesterTempDir = harvesterSectionProperties.getProperties().getProperty(HARVESTER_TEMP_DIR_PROPERTY_NAME);
        String fileName =
                properties.getProperty(HARVESTER_LAST_SYNC_TIMESTAMP_FILE,
                        DEFAULT_LAST_SYNC_TIMESTAMP_FILE);
        lastSyncTimestampFile = new File(fileName);
        newLastSyncTimeStampFile = new File(fileName + ".new");
    }

    private void extractDataSourceProperties(Properties properties)
    {
        SectionProperties dataSourceSectionProperties =
                PropertyParametersUtil.extractSingleSectionProperties(properties,
                        DATA_SOURCE_SECTION_NAME, false);
        dataSourceURI = dataSourceSectionProperties.getProperties().getProperty(DATA_SOURCE_URL_PROPERTY_NAME);
        dataSourceOpenbisURL = dataSourceSectionProperties.getProperties().getProperty(DATA_SOURCE_OPENBIS_URL_PROPERTY_NAME);
        dataSourceDSSURL = dataSourceSectionProperties.getProperties().getProperty(DATA_SOURCE_DSS_URL_PROPERTY_NAME);
        realm = dataSourceSectionProperties.getProperties().getProperty(DATA_SOURCE_AUTH_REALM_PROPERTY_NAME);
        user = dataSourceSectionProperties.getProperties().getProperty(DATA_SOURCE_AUTH_USER_PROPERTY_NAME);
        pass = dataSourceSectionProperties.getProperties().getProperty(DATA_SOURCE_AUTH_PASS_PROPERTY_NAME);
        dataSourceSpace = dataSourceSectionProperties.getProperties().getProperty(DATA_SOURCE_SPACE_PROPERTY_NAME);
        dataSourcePrefix = dataSourceSectionProperties.getProperties().getProperty(DATA_SOURCE_PREFIX_PROPERTY_NAME);
    }

    private Document getResourceListAsXMLDoc() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException,
            URISyntaxException,
            InterruptedException, TimeoutException, ExecutionException
    {

        HttpClient client = JettyHttpClientFactory.getHttpClient();

        // Add authentication credentials
        AuthenticationStore auth = client.getAuthenticationStore();
        auth.addAuthentication(new BasicAuthentication(new URI(dataSourceURI), realm, user, pass));

        Request requestEntity = client.newRequest(dataSourceURI + "?verb=resourcelist.xml&space=" + dataSourceSpace).method("GET");

        ContentResponse contentResponse;
        contentResponse = requestEntity.send();
        int statusCode = contentResponse.getStatus();

        if (statusCode != HttpStatus.Code.OK.getCode())
        {
            throw new RuntimeException("Status Code was " + statusCode + " instead of " + HttpStatus.Code.OK.getCode());
        }

        byte[] content = contentResponse.getContent();
        ByteArrayInputStream bis = new ByteArrayInputStream(content);

        DocumentBuilderFactory domFactory =
                DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        return builder.parse(bis);
    }

    private final class DataSetRegistrationTaskExecutor implements ITaskExecutor<DataSetWithConnections>
    {

        private List<String> dataSetCodes;

        public DataSetRegistrationTaskExecutor(List<String> dataSetCodes)
        {
            this.dataSetCodes = dataSetCodes;
        }

        @Override
        public Status execute(DataSetWithConnections dataSet)
        {
            System.out.println("start " + dataSet.getDataSet().getCode());

            Properties props = setProperties();

            DataSetRegistrationIngestionService ingestionService = new DataSetRegistrationIngestionService(props, storeRoot, dataSetCodes, dataSet.getDataSet());
            ingestionService.createAggregationReport(new HashMap<String, Object>(), context);
            System.out.println("finished " + dataSet.getDataSet().getCode());
            dataSetCodes.add(dataSet.getDataSet().getCode());
            return Status.OK;
        }

        private Properties setProperties()
        {
            Properties props = new Properties();
            props.setProperty("user", DataSetRegistrationTask.this.user);
            props.setProperty("pass", DataSetRegistrationTask.this.pass);
            props.setProperty("as-url", DataSetRegistrationTask.this.dataSourceOpenbisURL);
            props.setProperty("dss-url", DataSetRegistrationTask.this.dataSourceDSSURL);
            props.setProperty("harvester-temp-dir", DataSetRegistrationTask.this.harvesterTempDir);
            return props;
        }
    }

    private void registerMasterData()
    {
        EncapsulatedCommonServer encapsulatedServer = EncapsulatedCommonServer.create("http://localhost:8888/openbis/openbis", "admin", "a");
        MasterDataRegistrationService service = new MasterDataRegistrationService(encapsulatedServer);
        IMasterDataRegistrationTransaction transaction = service.transaction();
        transaction.getOrCreateNewDataSetType("test dataset type");
        // service.commit();
    }

    private static class DataSetRegistrationIngestionService extends IngestionService<DataSetInformation>
    {

        private static final long serialVersionUID = 1L;

        private List<String> dataSetCodes;

        private final NewExternalData dataSet;

        private final String loginUser;

        private final String loginPass;

        private final String asUrl;

        private final String dssUrl;

        private final String harvesterTempDir;

        public DataSetRegistrationIngestionService(Properties properties, File storeRoot, List<String> dataSetCodes, NewExternalData ds)
        {
            super(properties, storeRoot);
            this.dataSetCodes = dataSetCodes;
            this.dataSet = ds;
            this.loginUser = properties.getProperty("user");
            this.loginPass = properties.getProperty("pass");
            this.asUrl = properties.getProperty("as-url");
            this.dssUrl = properties.getProperty("dss-url");
            this.harvesterTempDir = properties.getProperty("harvester-temp-dir");
        }

        @Override
        protected TableModel process(IDataSetRegistrationTransactionV2 transaction, Map<String, Object> parameters, DataSetProcessingContext context)
        {
            IDataSetUpdatable dataSetForUpdate = transaction.getDataSetForUpdate(dataSet.getCode());
            ISampleImmutable sample = null;

            if (dataSet.getSampleIdentifierOrNull() != null)
            {
                sample = transaction.getSampleForUpdate(dataSet.getSampleIdentifierOrNull().toString());
            }
            IExperimentImmutable experiment = null;
            if (dataSet.getExperimentIdentifierOrNull() != null)
            {
                experiment = transaction.getExperimentForUpdate(dataSet.getExperimentIdentifierOrNull().toString());
            }

            List<NewProperty> dataSetProperties = dataSet.getDataSetProperties();

            if (dataSetForUpdate == null) {
                // REGISTER NEW DATA SET
                IDataSet ds = transaction.createNewDataSet(dataSet.getDataSetType().getCode(), dataSet.getCode());
                dataSetCodes.add(ds.getDataSetCode());
                ds.setSample(sample);
                ds.setExperiment(experiment);
                ds.setParentDatasets(dataSet.getParentDataSetCodes());
                for (NewProperty newProperty : dataSetProperties)
                {
                    ds.setPropertyValue(newProperty.getPropertyCode(), newProperty.getValue());
                }
    
                File storeRoot = transaction.getGlobalState().getStoreRootDir();
                File temp = new File(storeRoot, this.harvesterTempDir);
                temp.mkdirs();
                File file = new File(temp, ds.getDataSetCode());
                file.mkdirs();
    
                downloadDataSetFiles(file, ds.getDataSetCode());
    
                File dsPath = new File(file, "original");
                for (File f : dsPath.listFiles())
                {
                    transaction.moveFile(f.getAbsolutePath(), ds);
                }
            }
            else {
                // UPDATE data set meta data excluding the container/contained relationships
                dataSetForUpdate.setSample(sample);
                dataSetForUpdate.setExperiment(experiment);
                dataSetForUpdate.setParentDatasets(dataSet.getParentDataSetCodes());
                for (NewProperty newProperty : dataSetProperties)
                {
                    dataSetForUpdate.setPropertyValue(newProperty.getPropertyCode(), newProperty.getValue());
                }
            }
            return null;
        }

        private void downloadDataSetFiles(File file, String dataSetCode)
        {
            SslCertificateHelper.trustAnyCertificate(asUrl);
            SslCertificateHelper.trustAnyCertificate(dssUrl);

            IDataStoreServerApi dss =
                    HttpInvokerUtils.createStreamSupportingServiceStub(IDataStoreServerApi.class,
                            dssUrl + IDataStoreServerApi.SERVICE_URL, 10000);
            IApplicationServerApi as = HttpInvokerUtils
                    .createServiceStub(IApplicationServerApi.class, asUrl
                            + IApplicationServerApi.SERVICE_URL, 10000);
            String sessionToken = as.login(loginUser, loginPass);

            DataSetFileSearchCriteria criteria = new DataSetFileSearchCriteria();
            criteria.withDataSet().withCode().thatEquals(dataSetCode);
            SearchResult<DataSetFile> result = dss.searchFiles(sessionToken, criteria, new DataSetFileFetchOptions());
            List<DataSetFile> files = result.getObjects();

            List<IDataSetFileId> fileIds = new LinkedList<IDataSetFileId>();
            for (DataSetFile f : files)
            {
                fileIds.add(f.getPermId());
            }
            // Download the files & print the contents
            DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();
            options.setRecursive(false);
            InputStream stream = dss.downloadFiles(sessionToken, fileIds, options);
            DataSetFileDownloadReader reader = new DataSetFileDownloadReader(stream);
            DataSetFileDownload fileDownload = null;
            while ((fileDownload = reader.read()) != null)
            {
                DataSetFile dsFile = fileDownload.getDataSetFile();
                if (dsFile.getPath().equals(""))
                    continue;
                // if (dsFile.getPath().equals("original"))
                // continue;
                String filePath = dsFile.getPath();// .substring("original/".length());
                File output = new File(file.getAbsolutePath(), filePath);
                if (dsFile.isDirectory())
                {
                    output.mkdirs();
                }
                else
                {
                    System.err.println("Downloaded " + dsFile.getPath() + " "
                            + MemorySizeFormatter.format(dsFile.getFileLength()));
                    Path path = Paths.get(file.getAbsolutePath(), filePath);
                    try
                    {
                        Files.copy(fileDownload.getInputStream(), path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        storeRoot = new File(DssPropertyParametersUtil.loadServiceProperties().getProperty(PluginTaskInfoProvider.STOREROOT_DIR_KEY));
        service = ServiceProvider.getOpenBISService();
        context = new DataSetProcessingContext(null, null, null, null, null, null);
        extractDataSourceProperties(properties);
        extractHarvesterProperties(properties);

        dbConfigurationContext = new SimpleDatabaseConfigurationContext(properties);
        jdbcTemplate = new JdbcTemplate(dbConfigurationContext.getDataSource());
    }

    private Map<String, String> retrievePermIdMappings(String entityKind)
    {
        final Map<String, String> mappings = new HashMap<String, String>();
        List<Map<String, Object>> rows =
                jdbcTemplate.query("select source_perm_id, destination_perm_id from synced_entities where source_prefix = '" + dataSourcePrefix
                        + "' AND entity_kind = '"
                        + entityKind + "'",
                        new ColumnMapRowMapper());
        for (Map<String, Object> row : rows)
        {
            mappings.put(row.get("source_perm_id").toString(), row.get("destination_perm_id").toString());
        }

        return mappings;
    }

    @Override
    public void execute()
    {
        try
        {

            Map<String, String> expMapHarvesterToDataSource = retrievePermIdMappings("EXPERIMENT");

            operationLog.info(this.getClass() + " started.");
            operationLog.info("Start synchronization from data source: " + dataSourceOpenbisURL + " space:" + dataSourceSpace);

            operationLog.info("register master data");
            registerMasterData();

            if (lastSyncTimestampFile.exists())
            {
                String timeStr = FileUtilities.loadToString(lastSyncTimestampFile).trim();
                try
                {
                    lastSyncTimestamp = formater.parse(timeStr);
                } catch (ParseException e)
                {
                    operationLog.error("Cannot parse value as time:" + timeStr);
                    return;
                }
            }
            else
            {
                lastSyncTimestamp = new Date(0L);
            }
            // save the current time into a temp file as last sync time
            FileUtilities.writeToFile(newLastSyncTimeStampFile, formater.format(new Date()));

            //retrieve the document from the data source
            operationLog.info("Retrieving the resource list..");
            Document doc = getResourceListAsXMLDoc();

            Space destSpace = ServiceProvider.getOpenBISService().tryGetSpace(new SpaceIdentifier(harvesterSpace));
            if (destSpace == null)
            {
                operationLog.error("Space " + harvesterSpace + " does not exist");
                throw new RuntimeException("Space " + harvesterSpace + " does not exist");
            }
            
            // parse the resource list, this sends back all projects,
            // experiments, samples and data sets contained in the XML together with their last modification date to be used for filtering
            operationLog.info("parsing the resource list xml document");
            ResourceListParser parser = ResourceListParser.create(harvesterSpace, lastSyncTimestamp);
            data = parser.parseResourceListDocument(doc);

            // go through the resources returned by the parser and decide on add/update/delete operations
            Map<String, ProjectWithConnections> projectsToProcess = data.projectsToCreate;
            Map<String, ExperimentWithConnections> experimentsToProcess = data.experimentsToCreate;
            Map<String, SampleWithConnections> samplesToProcess = data.samplesToCreate;
            Map<String, DataSetWithConnections> dataSetsToProcess = data.datasetsToCreate;
            List<MaterialWithLastModificationDate> materialsToProcess = data.materialsToCreate;

            AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();

            EntityRetriever entityRetriever =
                    EntityRetriever.createWithSessionToken(ServiceProvider.getV3ApplicationService(), ServiceProvider.getOpenBISService()
                            .getSessionToken());
            EntityGraph<Node<?>> harvesterEntityGraph = entityRetriever.getEntityGraph(harvesterSpace);
                    
            processProjects(projectsToProcess, experimentsToProcess, builder, harvesterEntityGraph);
        
            processExperiments(expMapHarvesterToDataSource, experimentsToProcess, samplesToProcess, dataSetsToProcess, builder, harvesterEntityGraph);

            processSamples(samplesToProcess, builder, harvesterEntityGraph);

            processMaterials(materialsToProcess, builder);

            operationLog.info("Registering meta data...");
            AtomicEntityOperationResult operationResult = service.performEntityOperations(builder.getDetails());
            System.err.println("entity operation result: " + operationResult);
            
            // // //set parent and container data set codes before everything else
            // // //container and physical data sets can both be parents/children of each other
            // for (DataSetWithConnections dsWithConn : dataSetsToProcess.values())
            // {
            // for (Connection conn : dsWithConn.getConnections())
            // {
            // NewExternalData dataSet = dsWithConn.getDataSet();
            // if (dataSetsToProcess.containsKey(conn.getToPermId()) && conn.getType().equals("Child"))
            // {
            // NewExternalData externalData = dataSetsToProcess.get(conn.getToPermId()).getDataSet();
            // List<String> parentDataSetCodes = externalData.getParentDataSetCodes();
            // parentDataSetCodes.add(dataSet.getCode());
            // externalData.setParentDataSetCodes(parentDataSetCodes);
            // }
            // else if (dataSetsToProcess.containsKey(conn.getToPermId()) && conn.getType().equals("Component")) {
            // NewExternalData componentDataSet = dataSetsToProcess.get(conn.getToPermId()).getDataSet();
            // NewContainerDataSet containerDataSet = (NewContainerDataSet) dataSet;
            // List<String> containedDataSetCodes = containerDataSet.getContainedDataSetCodes();
            // containedDataSetCodes.add(componentDataSet.getCode());
            // containerDataSet.setContainedDataSetCodes(containedDataSetCodes);
            // }
            // }
            // }

            // register physical data sets
            operationLog.info("Registering data sets...");
            Map<String, DataSetWithConnections> physicalDSMap = data.filterPhysicalDataSetsByLastModificationDate(lastSyncTimestamp);
            List<DataSetWithConnections> dsList = new ArrayList<DataSetWithConnections>(physicalDSMap.values());
            List<String> dataSetCodes = Collections.synchronizedList(new ArrayList<String>());

            // TODO this parallelization needs to be revisited: In case of a data set appearing before DSs it is dependent on,
            // the parallelization will result in an error, i.e. The graph needs to be topologically sorted before it can be
            // parallelized
            ParallelizedExecutor.process(dsList, new DataSetRegistrationTaskExecutor(dataSetCodes), 0.5, 10, "register data sets", 0, false);

            // link physical data sets registered above to container data sets
            System.err.println("start linking/un-linking container and contained data sets");
            builder = new AtomicEntityOperationDetailsBuilder();

            builder.user(DataSetRegistrationTask.this.user);
            Map<String, NewExternalData> datasetsToUpdate = new HashMap<String, NewExternalData>();
            
            // set parent and container data set codes before everything else
            // container and physical data sets can both be parents/children of each other
            Map<String, Set<String>> dsToParents = new HashMap<String, Set<String>>();
            Map<String, Set<String>> dsToContained = new HashMap<String, Set<String>>();
            for (DataSetWithConnections dsWithConn : dataSetsToProcess.values())
            {
                for (Connection conn : dsWithConn.getConnections())
                {
                    NewExternalData dataSet = dsWithConn.getDataSet();
                    if (dataSetsToProcess.containsKey(conn.getToPermId()) && conn.getType().equals("Child"))
                    {
                        NewExternalData childDataSet = dataSetsToProcess.get(conn.getToPermId()).getDataSet();
                        List<String> parentDataSetCodes = childDataSet.getParentDataSetCodes();
                        parentDataSetCodes.add(dataSet.getCode());

                        // datasetsToUpdate.put(childDataSet.getCode(), childDataSet);
                        dsToParents.put(childDataSet.getCode(), new HashSet<String>(parentDataSetCodes));
                    }
                    else if (dataSetsToProcess.containsKey(conn.getToPermId()) && conn.getType().equals("Component"))
                    {
                        NewExternalData componentDataSet = dataSetsToProcess.get(conn.getToPermId()).getDataSet();
                        NewContainerDataSet containerDataSet = (NewContainerDataSet) dataSet;
                        List<String> containedDataSetCodes = containerDataSet.getContainedDataSetCodes();
                        containedDataSetCodes.add(componentDataSet.getCode());
                        dsToContained.put(dataSet.getCode(), new HashSet<String>(containedDataSetCodes));
                    }
                }
            }
            // go through all the data sets, decide what needs to be updated
            for (DataSetWithConnections dsWithConn : dataSetsToProcess.values())
            {
                NewExternalData dataSet = (NewExternalData) dsWithConn.getDataSet();
                if (dsWithConn.getLastModificationDate().after(lastSyncTimestamp))
                {
                    if (physicalDSMap.containsKey(dataSet.getCode()) == false && harvesterEntityGraph.containsEntity(dataSet.getCode()) == false)
                    {
                        builder.dataSet(dataSet);
                    }
                    else {
                        
                        datasetsToUpdate.put(dataSet.getCode(), dataSet);
                        }
                    }
            }

            // go thru to-be-updated DS list and establish/break relations
            for (NewExternalData dataSet : datasetsToUpdate.values())
            {
                DataSetBatchUpdatesDTO dsBatchUpdatesDTO = createDataSetBatchUpdateDTO(dataSet);
                if (dataSet instanceof NewContainerDataSet)
                {
                    NewContainerDataSet containerDS = (NewContainerDataSet) dataSet;
                    if(dsToContained.containsKey(containerDS.getCode())) {
                        dsBatchUpdatesDTO.setModifiedContainedDatasetCodesOrNull(dsToContained.get(dataSet.getCode()).toArray(new
                                String[containerDS.getContainedDataSetCodes().size()]));
                    }
                    else
                    {
                        dsBatchUpdatesDTO.setModifiedContainedDatasetCodesOrNull(new String[0]);
                    }
                    dsBatchUpdatesDTO.getDetails().setContainerUpdateRequested(true);
                }
                if (dsToParents.containsKey(dataSet.getCode()))
                {
                    dsBatchUpdatesDTO.setModifiedParentDatasetCodesOrNull(dsToParents.get(dataSet.getCode()).toArray(
                            new String[dataSet.getParentDataSetCodes().size()]));
                    // TODO should this always be true or should we flag the ones that require parent update. Same for container
                }
                else
                {
                    dsBatchUpdatesDTO.setModifiedParentDatasetCodesOrNull(new String[0]);
                }
                dsBatchUpdatesDTO.getDetails().setParentsUpdateRequested(true);
                SampleIdentifier sampleIdentifier = dataSet.getSampleIdentifierOrNull();
                if (sampleIdentifier != null)
                {
                    Sample sampleWithExperiment = service.tryGetSampleWithExperiment(sampleIdentifier);
                    dsBatchUpdatesDTO.setSampleIdentifierOrNull(SampleIdentifierFactory.parse(sampleWithExperiment.getIdentifier()));
                    dsBatchUpdatesDTO.getDetails().setSampleUpdateRequested(true);
                }
                else
                {
                    dsBatchUpdatesDTO.setSampleIdentifierOrNull(null);
                    dsBatchUpdatesDTO.getDetails().setSampleUpdateRequested(true);
                }

                ExperimentIdentifier expIdentifier = dataSet.getExperimentIdentifierOrNull();
                if (expIdentifier != null)
                {
                    Experiment experiment = service.tryGetExperiment(expIdentifier);
                    dsBatchUpdatesDTO.setExperimentIdentifierOrNull(ExperimentIdentifierFactory.parse(experiment.getIdentifier()));
                    dsBatchUpdatesDTO.getDetails().setExperimentUpdateRequested(true);
                }
                else
                {
                    dsBatchUpdatesDTO.setExperimentIdentifierOrNull(null);
                    dsBatchUpdatesDTO.getDetails().setExperimentUpdateRequested(true);
                }

                builder.dataSetUpdate(dsBatchUpdatesDTO);
            }
            operationResult = service.performEntityOperations(builder.getDetails());
            System.err.println("entity operation result: " + operationResult);

            operationLog.info("Saving the timestamp of synn start into file");
            saveSyncTimestamp();

            operationLog.info("Done and dusted...");
            operationLog.info(this.getClass() + " finished executing.");

        } catch (XPathExpressionException | ParserConfigurationException | SAXException | IOException | URISyntaxException | InterruptedException
                | TimeoutException | ExecutionException e)
        {
            operationLog.error("Sync failed: " + e.getMessage());
        }
    }

    private void saveSyncTimestamp()
    {
        newLastSyncTimeStampFile.renameTo(lastSyncTimestampFile);
    }

    private DataSetBatchUpdatesDTO createDataSetBatchUpdateDTO(NewExternalData childDS)
    {
        AbstractExternalData dsInHarvester = service.tryGetDataSet(childDS.getCode());
        ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetUpdatable updateUpdatable = new
                ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetUpdatable(dsInHarvester, service);
        DataSetBatchUpdatesDTO dsBatchUpdatesDTO = ConversionUtils.convertToDataSetBatchUpdatesDTO(updateUpdatable);
        //
        dsBatchUpdatesDTO.setDatasetId(TechId.create(dsInHarvester));
        List<IEntityProperty> entityProperties = new ArrayList<IEntityProperty>();
        for (NewProperty prop : childDS.getDataSetProperties())
        {
            String propertyCode = prop.getPropertyCode();
            String value = prop.getValue();
            entityProperties.add(new PropertyBuilder(propertyCode).value(value).getProperty());
        }
        dsBatchUpdatesDTO.setProperties(entityProperties);
        return dsBatchUpdatesDTO;
    }

    private void processMaterials(List<MaterialWithLastModificationDate> materialsToProcess, AtomicEntityOperationDetailsBuilder builder)
    {
        // process materials
        for (MaterialWithLastModificationDate newMaterialWithType : materialsToProcess)
        {
            NewMaterialWithType newIncomingMaterial = newMaterialWithType.getMaterial();
            Material material = service.tryGetMaterial(new MaterialIdentifier(newIncomingMaterial.getCode(), newIncomingMaterial.getType()));
            if (material == null)
            {
                builder.material(newIncomingMaterial);
            }
            else if (newMaterialWithType.getLastModificationDate().after(lastSyncTimestamp))
            {
                // TODO what should the date argument below be? Same question for version argument in other entities
                MaterialUpdateDTO update =
                        new MaterialUpdateDTO(TechId.create(material), Arrays.asList(newIncomingMaterial.getProperties()),
                                material.getModificationDate());
                builder.materialUpdate(update);
            }
        }
    }

    private void processSamples(Map<String, SampleWithConnections> samplesToProcess, AtomicEntityOperationDetailsBuilder builder,
            EntityGraph<Node<?>> harvesterEntityGraph)
    {
        // process samples
        Map<SampleIdentifier, NewSample> samplesToUpdate = new HashMap<SampleIdentifier, NewSample>();
        Set<String> sampleWithUpdatedParents = new HashSet<String>();
        for (SampleWithConnections sample : samplesToProcess.values())
        {
            NewSample newSmp = sample.getSample();
            if (sample.getLastModificationDate().after(lastSyncTimestamp)) {
                SampleIdentifier sampleIdentifier = SampleIdentifierFactory.parse(newSmp);
                Node<?> sampleInHarvesterGraph = harvesterEntityGraph.getNodeForIdentifier(sampleIdentifier.toString());
                if (sampleInHarvesterGraph == null)
                {
                    // ADD SAMPLE
                    builder.sample(newSmp);
                }
                else
                {
                    // defer creation of sample update objects until all samples have been gone through;
                    samplesToUpdate.put(sampleIdentifier, newSmp);
                    List<EdgeNodePair> edgesExistingOnHarvester =
                            harvesterEntityGraph.getNeighboursForEntityWithIdentifier(sampleIdentifier.toString(), null);
                    for (EdgeNodePair edgeNodePair : edgesExistingOnHarvester)
                    {
                        if (edgeNodePair.getEdge().getType().equals("Child"))
                        {
                            String childSampleIdentifier = edgeNodePair.getNode().getIdentifier();
                            SampleWithConnections childSampleWithConns = findChildInSamplesToProcess(childSampleIdentifier, samplesToProcess);
                            if (childSampleWithConns == null)
                            {
                                // TODO Handle sample delete
                            }
                            else
                            {
                                // the childSample will appear in the incoming samples list anyway
                                // but we want to make sure its parent modification is handled
                                NewSample childSample = childSampleWithConns.getSample();
                                sampleWithUpdatedParents.add(childSample.getIdentifier());
                            }
                        }
                    }
                }
            }
            for (Connection conn : sample.getConnections())
            {
                if (conn.getType().equals("Component"))
                {
                    NewSample containedSample = samplesToProcess.get(conn.getToPermId()).getSample();
                    containedSample.setContainerIdentifier(newSmp.getIdentifier());
                }
                else if (conn.getType().equals("Child"))
                {
                    NewSample childSample = samplesToProcess.get(conn.getToPermId()).getSample();
                    String[] parents = childSample.getParentsOrNull();
                    List<String> parentIds = null;
                    if (parents == null)
                    {
                        parentIds = new ArrayList<String>();
                    }
                    else
                    {
                        parentIds = new ArrayList<String>(Arrays.asList(parents));
                    }
                    parentIds.add(newSmp.getIdentifier());
                    childSample.setParentsOrNull(parentIds.toArray(new String[parentIds.size()]));
                }
                // TODO how about Connection Type
                // else if (conn.getType().equals("Connection")) // TODO not sure if this guarantees that we have a dataset in the toPermId
                // {
                // NewExternalData externalData = dataSetsToCreate.get(conn.getToPermId()).getDataSet();
                // externalData.setSampleIdentifierOrNull(new SampleIdentifier(newSmp.getIdentifier()));
                // }
            }
        }
        
        // create sample update dtos for the samples that need to be updated
        for (SampleIdentifier sampleIdentifier : samplesToUpdate.keySet())
        {
            NewSample newSmp = samplesToUpdate.get(sampleIdentifier);
            Sample sampleWithExperiment = service.tryGetSampleWithExperiment(sampleIdentifier);

            TechId sampleId = TechId.create(sampleWithExperiment);
            String expIdentifier = newSmp
                    .getExperimentIdentifier();
            String[] modifiedParentIds = newSmp.getParentsOrNull();
            if (modifiedParentIds == null)
            {
                if (sampleWithUpdatedParents.contains(newSmp.getIdentifier()))
                {
                    modifiedParentIds = new String[0];
                }
            }

            SampleUpdatesDTO updates =
                    new SampleUpdatesDTO(sampleId, Arrays.asList(newSmp.getProperties()), (expIdentifier == null) ? null
                            : ExperimentIdentifierFactory.parse(expIdentifier),
                            Collections.<NewAttachment> emptyList(), sampleWithExperiment.getVersion(),
                            sampleIdentifier, newSmp.getContainerIdentifier() == null ? null : newSmp.getContainerIdentifier(),
                            modifiedParentIds);
            builder.sampleUpdate(updates);
        }
    }

    private void processExperiments(Map<String, String> expMapHarvesterToDataSource, Map<String, ExperimentWithConnections> experimentsToProcess,
            Map<String, SampleWithConnections> samplesToProcess, Map<String, DataSetWithConnections> dataSetsToProcess,
            AtomicEntityOperationDetailsBuilder builder, EntityGraph<Node<?>> harvesterEntityGraph)
    {
        // process experiments
        for (ExperimentWithConnections exp : experimentsToProcess.values())
        {
            NewExperiment newIncomingExp = exp.getExperiment();
            if (exp.getLastModificationDate().after(lastSyncTimestamp)) {
                Node<?> experimentInHarvesterGraph =
                        harvesterEntityGraph.getNodeWithPermId(expMapHarvesterToDataSource.get(exp.getPermIdInDataSource()));
                if (experimentInHarvesterGraph == null)
                {
                    // ADD EXPERIMENT
                    builder.experiment(newIncomingExp);
                }
                else {
                    // UPDATE EXPERIMENT
                    Experiment experiment = service.tryGetExperimentByPermId(expMapHarvesterToDataSource.get(exp.getPermIdInDataSource()));
                    ExperimentUpdatesDTO expUpdate = new ExperimentUpdatesDTO();
                    expUpdate.setProjectIdentifier(ExperimentIdentifierFactory.parse(newIncomingExp.getIdentifier()));
                    expUpdate.setVersion(experiment.getVersion());
                    expUpdate.setProperties(Arrays.asList(newIncomingExp.getProperties()));
                    expUpdate.setExperimentId(TechId.create(experiment));
                    builder.experimentUpdate(expUpdate);
                    // TODO attachments
                    expUpdate.setAttachments(Collections.<NewAttachment> emptyList());
                }
            }
            for (Connection conn : exp.getConnections())
            {
                if (samplesToProcess.containsKey(conn.getToPermId()))
                {
                    SampleWithConnections sample = samplesToProcess.get(conn.getToPermId());
                    NewSample newSample = sample.getSample();
                    newSample.setExperimentIdentifier(newIncomingExp.getIdentifier());
                }
                if (dataSetsToProcess.containsKey(conn.getToPermId()))
                {
                    NewExternalData externalData = dataSetsToProcess.get(conn.getToPermId()).getDataSet();
                    externalData.setExperimentIdentifierOrNull(ExperimentIdentifierFactory.parse(newIncomingExp.getIdentifier()));
                }
            }
        }
    }

    private void processProjects(Map<String, ProjectWithConnections> projectsToProcess, Map<String, ExperimentWithConnections> experimentsToProcess,
            AtomicEntityOperationDetailsBuilder builder, EntityGraph<Node<?>> harvesterEntityGraph)
    {
        for (ProjectWithConnections prj : projectsToProcess.values())
        {
            NewProject incomingProject = prj.getProject();
            if (prj.getLastModificationDate().after(lastSyncTimestamp))
            {
                Project project = service.tryGetProject(ProjectIdentifierFactory.parse(incomingProject.getIdentifier()));
                
                if (project == null)
                {
                    // ADD PROJECT
                    builder.project(incomingProject);
                }
                else
                {
                    // UPDATE PROJECT
                    ProjectUpdatesDTO prjUpdate = new ProjectUpdatesDTO();
                    prjUpdate.setVersion(project.getVersion());
                    prjUpdate.setTechId(TechId.create(project));
                    prjUpdate.setDescription(incomingProject.getDescription());
                    // TODO attachments????
                    prjUpdate.setAttachments(Collections.<NewAttachment> emptyList());
                    builder.projectUpdate(prjUpdate); // ConversionUtils.convertToProjectUpdateDTO(new
                                                    // ch.systemsx.cisd.etlserver.registrator.api.v2.impl.Project(project))
                }
            }
            for (Connection conn : prj.getConnections())
            {
                String connectedExpPermId = conn.getToPermId();
                if (experimentsToProcess.containsKey(connectedExpPermId)) 
                {
                  //the project is connected to an experiment
                    ExperimentWithConnections exp = experimentsToProcess.get(connectedExpPermId);
                    NewExperiment newExp = exp.getExperiment();
                    // check if our local graph has the same connection
                    if (harvesterEntityGraph.containsEntity(newExp.getIdentifier()) == false)
                    {
                        // add new edge
                        String oldIdentifier = newExp.getIdentifier();
                        int index = oldIdentifier.lastIndexOf('/');
                        String expCode = oldIdentifier.substring(index + 1);

                        newExp.setIdentifier(incomingProject.getIdentifier() + "/" + expCode);
                        // add new experiment node
                    }
                    // else
                    // {
                    // if (harvesterEntityGraph.edgeExists(incomingProject.getIdentifier(), newExp.getIdentifier(), conn.getType()) == false)
                    // {
                    // // add new edge
                    // // String fullExpIdentifier = newExp.getIdentifier().replace("{1}", incomingProject.getIdentifier());
                    // // newExp.setIdentifier(fullExpIdentifier);
                    // }
                    // else
                    // {
                    // // do nothing
                    // }
                    // }
                }
                else
                {
                    // TODO This means the XML contains the connection but not the connected entitiy.
                    // These sort of problems maybe recorded in a separate synchronization log?
                    // ???????????????
                    operationLog.info("Connected experiment with permid : " + connectedExpPermId + " is missing");
                }
            }

            // //check if we have existing edges in our harvester that are no longer in the data source
            // List<EdgeNodePair> existingEdges = harvesterEntityGraph.getNeighboursForEntity(incomingProject.getIdentifier());
            // for (EdgeNodePair edgeNodePair : existingEdges)
            // {
            // String experimentIdentifier = edgeNodePair.getNode().getIdentifier();
            // List<Connection> connections = prj.getConnections();
            // for (Connection conn : connections) {
            // if
            // }
            // }
        }
    }

    private SampleWithConnections findChildInSamplesToProcess(String childSampleIdentifier, Map<String, SampleWithConnections> samplesToProcess)
    {
        for (SampleWithConnections sample : samplesToProcess.values())
        {
            if(sample.getSample().getIdentifier().equals(childSampleIdentifier)) {
                return sample;
            }
        }
        return null;
    }
}
