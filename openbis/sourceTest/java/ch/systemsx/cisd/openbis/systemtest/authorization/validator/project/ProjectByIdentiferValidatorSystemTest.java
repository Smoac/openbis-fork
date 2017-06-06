/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.authorization.validator.project;

import org.springframework.beans.factory.annotation.Autowired;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.systemtest.authorization.validator.CommonValidatorSystemTest;

/**
 * @author pkupczyk
 */
public class ProjectByIdentiferValidatorSystemTest extends CommonValidatorSystemTest<Project>
{

    @Autowired
    private ProjectValidatorTestService service;

    @Override
    protected Project createObject(SpacePE spacePE, ProjectPE projectPE)
    {
        Project project = new Project();
        project.setIdentifier(new ProjectIdentifier(spacePE.getCode(), projectPE.getCode()).toString());
        return project;
    }

    @Override
    protected Project validateObject(IAuthSessionProvider sessionProvider, Project object)
    {
        return service.testProjectByIdentifierValidator(sessionProvider, object);
    }

}
