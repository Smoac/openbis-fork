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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp;

import org.apache.ftpserver.ftplet.FtpFile;

/**
 * {@link IFtpPathResolver}-s can translate String paths to {@link FtpFile} objects.
 * 
 * @author Kaloyan Enimanev
 */
public interface IFtpPathResolver
{
    /**
     * @param path a normalized path, containing no trailing slashes.
     */
    boolean canResolve(String path);

    /**
     * @param path a normalized path, containing no trailing slashes.
     */
    FtpFile resolve(String path, FtpPathResolverContext resolverContext);

}
