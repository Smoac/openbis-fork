/*
 * Copyright 2016 ETH Zuerich, CISD
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

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.common.server.ISessionTokenProvider;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.file.DirectoryResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.file.FileResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.file.NonExistingFileResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.Cache;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;

/**
 * @author Jakub Straszewski
 */
public class ResolverContext implements ISessionTokenProvider
{
    private final String sessionToken;

    private final Cache cache;

    private final IApplicationServerApi api;

    private IHierarchicalContentProvider contentProvider;

    private final String fullPath;

    public ResolverContext(String sessionToken, Cache cache, IApplicationServerApi api, String fullPath)
    {
        this.sessionToken = sessionToken;
        this.cache = cache;
        this.api = api;
        this.fullPath = fullPath;
    }

    public FileResponse createFileResponse(IHierarchicalContentNode node, IHierarchicalContent content)
    {
        return new FileResponse(fullPath, node, content);
    }

    public DirectoryResponse createDirectoryResponse()
    {
        return new DirectoryResponse(fullPath);
    }

    public NonExistingFileResponse createNonExistingFileResponse(String errorMsg)
    {
        return new NonExistingFileResponse(fullPath, errorMsg);
    }

    @Override
    public String getSessionToken()
    {
        return sessionToken;
    }

    public IHierarchicalContentProvider getContentProvider()
    {
        if (contentProvider == null)
        {
            contentProvider = ServiceProvider.getHierarchicalContentProvider();
        }
        return contentProvider.cloneFor(this);
    }

    public IApplicationServerApi getApi()
    {
        return api;
    }

    public Cache getCache()
    {
        return cache;
    }

}
