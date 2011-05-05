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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.ftpserver.ftplet.FtpFile;

import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpConstants;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.IFtpPathResolver;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * Resolves experiment folders with path "/<space-code>/<project-code>/<experiment-code>" to
 * {@link FtpFile}-s.
 * 
 * @author Kaloyan Enimanev
 */
public class ExperimentFolderResolver implements IFtpPathResolver
{
    private final IExperimentChildrenLister childLister;

    public ExperimentFolderResolver(IExperimentChildrenLister childLister)
    {
        this.childLister = childLister;
    }

    /**
     * @return <code>true</code> for all paths containing 3 levels of nested folders,
     *         <code>false</code> for all other paths.
     */
    public boolean canResolve(String path)
    {
        int nestedLevels = StringUtils.countMatches(path, FtpConstants.FILE_SEPARATOR);
        return nestedLevels == 3;
    }

    public FtpFile resolve(final String path, final FtpPathResolverContext resolverContext)
    {
        return new AbstractFtpFolder(path)
            {
                @Override
                public List<FtpFile> unsafeListFiles()
                {
                    return listChildrenNames(path, resolverContext);
                }

            };
    }

    private List<FtpFile> listChildrenNames(String expIdentifier, FtpPathResolverContext context)
    {
        IETLLIMSService service = context.getService();
        String sessionToken = context.getSessionToken();

        ExperimentIdentifier identifier =
                new ExperimentIdentifierFactory(expIdentifier).createIdentifier();

        Experiment exp = service.tryToGetExperiment(sessionToken, identifier);
        if (exp == null)
        {
            return Collections.emptyList();
        } else
        {
            return childLister.listExperimentChildrenPaths(exp, expIdentifier, context);
        }
    }
}
