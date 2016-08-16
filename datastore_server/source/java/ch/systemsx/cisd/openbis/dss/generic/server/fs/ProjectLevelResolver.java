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

package ch.systemsx.cisd.openbis.dss.generic.server.fs;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.file.FtpDirectoryResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.file.IFtpFile;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.file.FtpNonExistingFile;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;

class ProjectLevelResolver implements IResolver
{
    private ProjectIdentifier projectIdentifier;

    public ProjectLevelResolver(String spaceCode, String projectCode)
    {
        this.projectIdentifier = new ProjectIdentifier(spaceCode, projectCode);
    }

    @Override
    public IFtpFile resolve(String fullPath, String[] subPath, FtpPathResolverContext context)
    {
        if (subPath.length == 0)
        {
            ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
            fetchOptions.withExperiments();

            Map<IProjectId, Project> projects =
                    context.getV3Api().getProjects(context.getSessionToken(), Collections.singletonList(projectIdentifier), fetchOptions);
            Project project = projects.get(projectIdentifier);

            FtpDirectoryResponse response = new FtpDirectoryResponse(fullPath);
            if (project == null)
            {
                return new FtpNonExistingFile(fullPath, null);
            }
            for (Experiment exp : project.getExperiments())
            {
                response.addDirectory(exp.getCode());
            }
            return response;
        } else
        {
            String item = subPath[0];
            String[] remaining = Arrays.copyOfRange(subPath, 1, subPath.length);
            ExperimentLevelResolver resolver =
                    new ExperimentLevelResolver(new ExperimentIdentifier(projectIdentifier.getIdentifier() + "/" + item));
            return resolver.resolve(fullPath, remaining, context);
        }
    }
}