/*
 * Copyright 2010 ETH Zuerich, CISD
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

package eu.basysbio.cisd.dss;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public abstract class UploaderTestCase extends AbstractFileSystemTestCase
{

    protected static final String PROJECT_CODE = "P1";
    static final String GROUP_CODE = "/G1";
    protected static final long EXP_ID = 42L;
    protected static final String EXP_PERM_ID = "perm-" + EXP_ID;

    /**
     *
     *
     */
    public UploaderTestCase()
    {
        super();
    }

    /**
     *
     *
     * @param cleanAfterMethod
     */
    public UploaderTestCase(boolean cleanAfterMethod)
    {
        super(cleanAfterMethod);
    }

    protected Experiment createExperiment(String code)
    {
        Experiment experiment = new Experiment();
        experiment.setId(EXP_ID);
        experiment.setCode(code);
        experiment.setPermId(EXP_PERM_ID);
        Project project = new Project();
        project.setCode(PROJECT_CODE);
        Space space = new Space();
        space.setIdentifier(GROUP_CODE);
        project.setSpace(space);
        experiment.setProject(project);
        return experiment;
    }

}