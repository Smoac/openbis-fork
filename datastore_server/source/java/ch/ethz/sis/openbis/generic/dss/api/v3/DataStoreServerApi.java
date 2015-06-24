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
package ch.ethz.sis.openbis.generic.dss.api.v3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.ethz.sis.openbis.generic.dss.api.v3.dto.entity.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dss.api.v3.dto.search.FileSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.DataSetSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ISearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SearchOperator;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.filesystem.SimpleFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.api.v1.PutDataSetService;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDssServiceRpc;
import ch.systemsx.cisd.openbis.dss.generic.server.IStreamRepository;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IPluginTaskInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;

/**
 * @author Jakub Straszewski
 */
@Component(IDataStoreServerApi.INTERNAL_SERVICE_NAME)
public class DataStoreServerApi extends AbstractDssServiceRpc<IDataStoreServerApi>
        implements IDataStoreServerApi
{
    /**
     * Logger with {@link LogCategory#OPERATION} with name of the concrete class, needs to be static for our purpose.
     */
    @SuppressWarnings("hiding")
    protected static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataStoreServerApi.class);

    public String DSS_SERVICE_NAME = "DSS Service";

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
        operationLog.info("[rpc] Started DSS API V1 service.");
    }

    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Override
    public List<DataSetFile> searchFiles(String sessionToken, FileSearchCriterion searchCriterion)
    {
        List<DataSetFile> result = new ArrayList<>();

        Collection<ISearchCriterion> criteria = searchCriterion.getCriteria();

        Set<String> resultDataSets = null;
        Map<String, DataSetPermId> permIds = new HashMap<>();

        for (ISearchCriterion iSearchCriterion : criteria)
        {
            if (iSearchCriterion instanceof DataSetSearchCriterion)
            {
                List<DataSet> dataSets = as.searchDataSets(sessionToken, (DataSetSearchCriterion) iSearchCriterion, new DataSetFetchOptions());
                System.err.println(dataSets.size());
                HashSet<String> codes = new HashSet<String>();
                for (DataSet dataSet : dataSets)
                {
                    codes.add(dataSet.getCode());
                    permIds.put(dataSet.getCode(), dataSet.getPermId());
                }

                if (resultDataSets == null)
                {
                    resultDataSets = codes;
                } else if (searchCriterion.getOperator().equals(SearchOperator.OR)) // is an or
                {
                    resultDataSets.addAll(codes);
                } else
                { // is an and
                    resultDataSets.retainAll(codes);
                }
            }
        }
        for (String code : resultDataSets)
        {
            IHierarchicalContent content = getHierarchicalContentProvider(sessionToken).asContent(code);
            List<IHierarchicalContentNode> nodes = content.listMatchingNodes("");
            for (IHierarchicalContentNode node : nodes)
            {
                DataSetFile file = new DataSetFile();
                file.setFileName(node.getFile().getPath());
                file.setPermId(permIds.get(code));
                result.add(file);
            }
        }
        return result;
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

}
