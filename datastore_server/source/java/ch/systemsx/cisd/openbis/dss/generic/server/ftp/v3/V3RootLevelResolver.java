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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.ftpserver.ftplet.FtpFile;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;

class V3RootLevelResolver extends V3Resolver
{
    public V3RootLevelResolver(FtpPathResolverContext resolverContext)
    {
        super(resolverContext);
    }

    @Override
    public FtpFile resolve(String fullPath, String[] subPath)
    {
        if (subPath.length == 0)
        {
            List<Space> spaces =
                    api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions()).getObjects();
            List<FtpFile> files = new LinkedList<>();
            for (Space space : spaces)
            {
                files.add(createDirectoryScaffolding(fullPath, space.getCode()));
            }
            return createDirectoryWithContent(fullPath, files);
        } else
        {
            String item = subPath[0];
            String[] remaining = Arrays.copyOfRange(subPath, 1, subPath.length);
            V3SpaceLevelResolver resolver = new V3SpaceLevelResolver(item, resolverContext);
            return resolver.resolve(fullPath, remaining);
        }
    }
}