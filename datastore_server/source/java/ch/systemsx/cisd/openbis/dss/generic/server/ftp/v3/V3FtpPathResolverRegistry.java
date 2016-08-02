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

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverConfig;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.IFtpPathResolverRegistry;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3.file.V3FtpNonExistingFile;

/**
 * A registry of ftp resolvers. It keeps the style of old-style resolver regisrty, but actually only calls itself root resolver.
 * 
 * @author Jakub Straszewski
 */
public class V3FtpPathResolverRegistry implements IFtpPathResolverRegistry
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            V3FtpPathResolverRegistry.class);

    @Override
    public void initialize(FtpPathResolverConfig config)
    {

    }

    // note to self - the resolver context is created fresh for each request
    @Override
    public FtpFile resolve(String path, FtpPathResolverContext resolverContext)
    {
        System.err.println(path + " Resolver registry: " + this.hashCode());
        try
        {
            V3RootLevelResolver resolver = new V3RootLevelResolver(resolverContext);
            String[] split = path.equals("/") ? new String[] {} : path.substring(1).split("/");
            return resolver.resolve(path, split);
        } catch (Exception e)
        {
            operationLog.warn(e);
        }
        return new V3FtpNonExistingFile(path, "Error when retrieving path");
    }

}
