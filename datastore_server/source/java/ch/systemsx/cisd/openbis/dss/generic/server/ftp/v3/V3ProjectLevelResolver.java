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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.ftpserver.ftplet.FtpFile;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;

class V3ProjectLevelResolver extends V3Resolver
{
    private ProjectIdentifier projectIdentifier;

    public V3ProjectLevelResolver(String spaceCode, String projectCode, FtpPathResolverContext resolverContext)
    {
        super(resolverContext);
        this.projectIdentifier = new ProjectIdentifier(spaceCode, projectCode);
    }

    @Override
    public FtpFile resolve(String fullPath, String[] subPath)
    {
        if (subPath.length == 0)
        {
            ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
            fetchOptions.withExperiments();

            Map<IProjectId, Project> projects = api.getProjects(sessionToken, Collections.singletonList(projectIdentifier), fetchOptions);
            Project project = projects.get(projectIdentifier);

            List<FtpFile> files = new LinkedList<>();
            for (Experiment exp : project.getExperiments())
            {
                files.add(createDirectoryScaffolding(fullPath, exp.getCode()));
            }
            return createDirectoryWithContent(fullPath, files);
        } else
        {
            String item = subPath[0];
            String[] remaining = Arrays.copyOfRange(subPath, 1, subPath.length);
            V3ExperimentLevelResolver resolver =
                    new V3ExperimentLevelResolver(new ExperimentIdentifier(projectIdentifier.getIdentifier() + "/" + item), resolverContext);
            return resolver.resolve(fullPath, remaining);
        }
    }
}