/*
 * Copyright 2015 ETH Zuerich, CISD
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
package ch.ethz.sis.openbis.generic.server.dssapi.v3;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.iterators.IteratorChain;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.IDataStoreId;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.FullDataSetCreation;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.create.DataSetFileCreation;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.DataSetFilePermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;
import ch.ethz.sis.openbis.generic.server.dssapi.v3.download.DataSetFileDownloadInputStream;
import ch.ethz.sis.openbis.generic.server.dssapi.v3.pathinfo.PathInfoFeeder;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.filesystem.SimpleFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.api.v1.PutDataSetService;
import ch.systemsx.cisd.etlserver.path.IPathsInfoDAO;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDssServiceRpc;
import ch.systemsx.cisd.openbis.dss.generic.server.IStreamRepository;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IPluginTaskInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.DssSessionAuthorizationHolder;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PathInfoDataSourceProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;

import net.lemnik.eodsql.QueryTool;

/**
 * @author Jakub Straszewski
 */
@Component(DataStoreServerApi.INTERNAL_SERVICE_NAME)
public class DataStoreServerApi extends AbstractDssServiceRpc<IDataStoreServerApi>
        implements IDataStoreServerApi
{
    /**
     * Name of this service for which it is registered as Spring bean
     */
    public static final String INTERNAL_SERVICE_NAME = "data-store-server_INTERNAL";

    /**
     * Logger with {@link LogCategory#OPERATION} with name of the concrete class, needs to be static for our purpose.
     */
    protected static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataStoreServerApi.class);

    public String DSS_SERVICE_NAME = "DSS Service";

    @Autowired
    private IConfigProvider configProvider;

    @Autowired
    private IApplicationServerApi as;

    /**
     * The designated constructor.
     */
    @Autowired
    public DataStoreServerApi(IEncapsulatedOpenBISService openBISService,
            IQueryApiServer apiServer, IPluginTaskInfoProvider infoProvider)
    {
        // NOTE: IShareIdManager and IHierarchicalContentProvider will be lazily created by spring
        this(openBISService, apiServer, infoProvider, new SimpleFreeSpaceProvider(), null, null);
    }

    DataStoreServerApi(IEncapsulatedOpenBISService openBISService, IQueryApiServer apiServer,
            IPluginTaskInfoProvider infoProvider, IFreeSpaceProvider freeSpaceProvider,
            IShareIdManager shareIdManager, IHierarchicalContentProvider contentProvider)
    {
        this(openBISService, apiServer, infoProvider, null, freeSpaceProvider, shareIdManager,
                contentProvider, new PutDataSetService(openBISService, operationLog));
    }

    /**
     * A constructor for testing.
     */
    public DataStoreServerApi(IEncapsulatedOpenBISService openBISService,
            IQueryApiServer apiServer, IPluginTaskInfoProvider infoProvider,
            IStreamRepository streamRepository, IFreeSpaceProvider freeSpaceProvider,
            IShareIdManager shareIdManager, IHierarchicalContentProvider contentProvider,
            PutDataSetService service)
    {
        super(openBISService, streamRepository, shareIdManager, contentProvider);
        // queryApiServer = apiServer;
        // this.freeSpaceProvider = freeSpaceProvider;
        // putService = service;
        // this.sessionWorkspaceRootDirectory = infoProvider.getSessionWorkspaceRootDir();
        operationLog.info("[rpc] Started DSS API V3 service.");
    }

    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Override
    public SearchResult<DataSetFile> searchFiles(String sessionToken, DataSetFileSearchCriteria searchCriteria, DataSetFileFetchOptions fetchOptions)
    {
        getOpenBISService().checkSession(sessionToken);

        List<DataSetFile> result = new ArrayList<>();
        Collection<ISearchCriteria> criteria = searchCriteria.getCriteria();

        Set<String> resultDataSets = null;
        Map<String, DataSet> dataSetMap = new HashMap<>();

        String dataStoreCode = configProvider.getDataStoreCode();

        for (ISearchCriteria iSearchCriterion : criteria)
        {
            if (iSearchCriterion instanceof DataSetSearchCriteria)
            {
                DataSetFetchOptions fo = new DataSetFetchOptions();
                fo.withDataStore();
                SearchResult<DataSet> searchResult =
                        as.searchDataSets(sessionToken, (DataSetSearchCriteria) iSearchCriterion, fo);
                List<DataSet> dataSets = searchResult.getObjects();
                HashSet<String> codes = new HashSet<String>();

                for (DataSet dataSet : dataSets)
                {
                    if (dataStoreCode.equals(dataSet.getDataStore().getCode()))
                    {
                        codes.add(dataSet.getCode());
                        dataSetMap.put(dataSet.getCode(), dataSet);
                    }
                }

                if (resultDataSets == null)
                {
                    resultDataSets = codes;
                } else if (searchCriteria.getOperator().equals(SearchOperator.OR)) // is an or
                {
                    resultDataSets.addAll(codes);
                } else
                { // is an and
                    resultDataSets.retainAll(codes);
                }
            }
        }

        if (resultDataSets != null)
        {
            for (String code : resultDataSets)
            {
                IHierarchicalContent content = getHierarchicalContentProvider(sessionToken).asContent(code);
                for (IHierarchicalContentNode node : iterate(content.getRootNode()))
                {
                    DataSet dataSet = dataSetMap.get(code);
                    DataSetFile file = new DataSetFile();
                    file.setPermId(new DataSetFilePermId(new DataSetPermId(code), node.getRelativePath()));
                    file.setPath(node.getRelativePath());
                    file.setDataSetPermId(dataSet.getPermId());
                    file.setDataStore(dataSet.getDataStore());
                    file.setDirectory(node.isDirectory());
                    if (node.isDirectory() == false)
                    {
                        file.setFileLength(node.getFileLength());
                        if (node.isChecksumCRC32Precalculated())
                        {
                            file.setChecksumCRC32(node.getChecksumCRC32());
                        }
                        setChecksumOf(file, node.getChecksum());
                    }
                    result.add(file);
                }
            }
        }

        return new SearchResult<DataSetFile>(result, result.size());
    }

    private void setChecksumOf(DataSetFile file, String checksum)
    {
        if (checksum == null)
        {
            return;
        }
        String[] splitted = checksum.split(":", 2);
        if (splitted.length < 2 || splitted[0].length() == 0 || splitted[1].length() == 0)
        {
            return;
        }
        file.setChecksumType(splitted[0]);
        file.setChecksum(splitted[1]);
    }

    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Override
    public InputStream downloadFiles(String sessionToken, List<? extends IDataSetFileId> fileIds,
            DataSetFileDownloadOptions downloadOptions)
    {
        getOpenBISService().checkSession(sessionToken);

        IHierarchicalContentProvider contentProvider = getHierarchicalContentProvider(sessionToken);
        Map<IHierarchicalContentNode, String> contentNodes = new LinkedHashMap<IHierarchicalContentNode, String>();

        for (IDataSetFileId fileId : fileIds)
        {
            if (fileId instanceof DataSetFilePermId)
            {
                DataSetFilePermId filePermId = (DataSetFilePermId) fileId;

                if (filePermId.getDataSetId() instanceof DataSetPermId)
                {
                    String dataSetCode = ((DataSetPermId) filePermId.getDataSetId()).getPermId();
                    Status authorizationStatus = DssSessionAuthorizationHolder.getAuthorizer().checkDatasetAccess(sessionToken, dataSetCode);

                    if (authorizationStatus.isOK())
                    {
                        String filePath = filePermId.getFilePath();

                        IHierarchicalContent content = contentProvider.asContent(dataSetCode);
                        IHierarchicalContentNode node = content.getNode(filePath);

                        if (node.isDirectory() && downloadOptions.isRecursive())
                        {
                            for (IHierarchicalContentNode child : iterate(node))
                            {
                                contentNodes.put(child, dataSetCode);
                            }
                        } else
                        {
                            contentNodes.put(node, dataSetCode);
                        }
                    }
                } else
                {
                    throw new IllegalArgumentException("Unsupported dataSetId: " + fileId);
                }
            } else
            {
                throw new IllegalArgumentException("Unsupported fileId: " + fileId);
            }
        }

        return new DataSetFileDownloadInputStream(contentNodes);
    }

    private Iterable<IHierarchicalContentNode> iterate(final IHierarchicalContentNode node)
    {
        return new Iterable<IHierarchicalContentNode>()
            {
                @Override
                public Iterator<IHierarchicalContentNode> iterator()
                {
                    IteratorChain<IHierarchicalContentNode> chain = new IteratorChain<>(Collections.singleton(node).iterator());

                    if (node.isDirectory())
                    {
                        for (IHierarchicalContentNode child : node.getChildNodes())
                        {
                            chain.addIterator(iterate(child).iterator());
                        }
                    }
                    return chain;
                }
            };
    }

    @Override
    public int getMajorVersion()
    {
        return 3;
    }

    @Override
    public int getMinorVersion()
    {
        return 0;
    }

    @Override
    public IDataStoreServerApi createLogger(IInvocationLoggerContext context)
    {
        return new DataStoreServerApiLogger(context);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN })
    public List<DataSetPermId> createDataSets(String sessionToken, List<FullDataSetCreation> newDataSets)
    {
        if (PathInfoDataSourceProvider.isDataSourceDefined() == false)
        {
            throw new IllegalStateException("Pathinfo DB not configured - cannot store dataset file information");
        }
        injectDataStoreIdAndCodesIfNeeded(newDataSets);
        IPathsInfoDAO dao = QueryTool.getQuery(PathInfoDataSourceProvider.getDataSource(), IPathsInfoDAO.class);

        for (int i = 0; i < newDataSets.size(); i++)
        {
            FullDataSetCreation fullDataSetCreation = newDataSets.get(i);
            List<DataSetFileCreation> files = fullDataSetCreation.getFileMetadata();

            long dataSetId = dao.createDataSet(fullDataSetCreation.getMetadataCreation().getCode(), "");
            String dataSetCode = fullDataSetCreation.getMetadataCreation().getCode();
            PathInfoFeeder feeder = new PathInfoFeeder(dataSetId, dataSetCode, files);

            try
            {
                feeder.storeFilesWith(dao);
            } catch (IllegalArgumentException e)
            {
                dao.rollback();
                dao.close();
                throw new UserFailureException(e.getMessage());
            }
        }
        dao.commit();
        dao.close();

        List<DataSetCreation> metadata = new ArrayList<>();
        for (int i = 0; i < newDataSets.size(); i++)
        {
            DataSetCreation creation = newDataSets.get(i).getMetadataCreation();
            metadata.add(creation);
        }

        return as.createDataSets(sessionToken, metadata);
    }
    
    private void injectDataStoreIdAndCodesIfNeeded(List<FullDataSetCreation> newDataSets)
    {
        String dataStoreCode = configProvider.getDataStoreCode();
        DataStorePermId dataStoreId = new DataStorePermId(dataStoreCode);
        int numberOfPermIdsToGenerate = 0;
        for (int i = 0; i < newDataSets.size(); i++)
        {
            FullDataSetCreation fullDataSetCreation = newDataSets.get(i);
            DataSetCreation metadataCreation = fullDataSetCreation.getMetadataCreation();
            IDataStoreId dsid = metadataCreation.getDataStoreId();
            if (dsid != null && dsid instanceof DataStorePermId)
            {
                String code = ((DataStorePermId) dsid).getPermId();
                if (dataStoreCode.equals(code) == false)
                {
                    throw new UserFailureException("Data store id specified for creation object with index "
                            + i + " is '" + code + "' instead of '" + dataStoreCode + "' or undefined.");
                }
            }
            metadataCreation.setDataStoreId(dataStoreId);
            if (metadataCreation.isAutoGeneratedCode())
            {
                numberOfPermIdsToGenerate++;
            } else if (StringUtils.isBlank(metadataCreation.getCode()))
            {
                throw new UserFailureException("Neither code nor auto generating code specified in creation object with index " + i);
            } else
            {
                metadataCreation.setCode(StringUtils.upperCase(metadataCreation.getCode()));
            }
        }
        if (numberOfPermIdsToGenerate > 0)
        {
            List<String> permIds = getOpenBISService().createPermIds(numberOfPermIdsToGenerate);
            for (FullDataSetCreation newDataSet : newDataSets)
            {
                DataSetCreation metadataCreation = newDataSet.getMetadataCreation();
                if (metadataCreation.isAutoGeneratedCode())
                {
                    metadataCreation.setCode(permIds.remove(0));
                    metadataCreation.setAutoGeneratedCode(false);
                }
            }
        }
    }
}
