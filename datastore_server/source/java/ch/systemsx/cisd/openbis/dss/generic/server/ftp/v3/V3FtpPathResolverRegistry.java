/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3;

import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverConfig;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.IFtpPathResolver;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.IFtpPathResolverRegistry;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;

/**
 * A registry of ftp resolvers. It keeps the style of old-style resolver regisrty, but actually only calls itself root resolver.
 * 
 * @author Jakub Straszewski
 */
public class V3FtpPathResolverRegistry implements IFtpPathResolverRegistry
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            V3FtpPathResolverRegistry.class);

    private IApplicationServerApi v3api;

    public IApplicationServerApi getV3api()
    {
        if (v3api == null)
            v3api = ServiceProvider.getV3ApplicationService();
        return v3api;
    }

    /**
     * initializes the registry with all known {@link IFtpPathResolver}-s.
     */
    public V3FtpPathResolverRegistry(FtpPathResolverConfig config)
    {

    }

    @Override
    public FtpFile resolve(String path, FtpPathResolverContext resolverContext)
    {
        try
        {
            V3RootLevelResolver resolver = new V3RootLevelResolver(resolverContext);
            String[] split = path.equals("/") ? new String[] {} : path.substring(1).split("/");
            return resolver.resolve(path, split);
        } catch (Exception e)
        {
            operationLog.warn(e);
        }
        return V3Resolver.getNonExistingFile(path, path);
    }

}
