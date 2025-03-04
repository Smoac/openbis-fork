/*
 * Copyright ETH 2016 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.server.fs.resolver;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.DataStoreKind;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.ResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.IResolver;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.IResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.file.IFileSystemViewResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.Cache;

/**
 * Resolves the content of the data set. Assumes that the first part of the path is data set code
 *
 * @author Jakub Straszewski
 */
public class DataSetContentResolver implements IResolver
{
    String dataSetCode;

    public DataSetContentResolver(String dataSetCode)
    {
        this.dataSetCode = dataSetCode;
    }

    @Override
    public IFileSystemViewResponse resolve(String[] subPath, IResolverContext context)
    {
        ResolverContext resolverContext = (ResolverContext) context;
        Cache cache = resolverContext.getCache();
        Boolean hasAccess = cache.getAccess(dataSetCode);

        if (hasAccess == null)
        {
            // this fetching of data set is for authorization purposes, as content provider doesn't check if user has access to data set
            DataSetSearchCriteria dataSetSearchCriteria = new DataSetSearchCriteria();
            dataSetSearchCriteria.withCode().thatEquals(dataSetCode);
            if (resolverContext.isShowAfsDataSets())
            {
                dataSetSearchCriteria.withDataStore().withKind().thatIn(DataStoreKind.DSS, DataStoreKind.AFS);
            }

            List<DataSet> dataSets =
                    context.getApi().searchDataSets(context.getSessionToken(), dataSetSearchCriteria, new DataSetFetchOptions()).getObjects();

            hasAccess = !dataSets.isEmpty();
            cache.putAccess(dataSetCode, hasAccess);
        }

        if (hasAccess.booleanValue() == false)
        {
            return context.createNonExistingFileResponse("Path doesn't exist or unauthorized");
        }

        IHierarchicalContent content = cache.getContent(dataSetCode);
        if (content == null)
        {
            content = context.getContentProvider().asContent(dataSetCode);
            cache.putContent(dataSetCode, content);
        }

        HierarchicalContentResolver resolver = new HierarchicalContentResolver(content);
        return resolver.resolve(subPath, context);
    }
}