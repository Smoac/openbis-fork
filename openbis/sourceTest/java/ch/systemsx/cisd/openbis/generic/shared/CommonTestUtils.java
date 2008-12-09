/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared;

import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Contains methods and constants which may be used by many tests.
 * 
 * @author Izabela Adamczyk
 */
public class CommonTestUtils
{
    static final String HOME_DATABASE_INSTANCE_CODE = "HOME_DATABASE";

    private static final String HOME_GROUP_CODE = "HOME_GROUP";

    private static final String EXPERIMENT_TYPE = "EXPERIMENT_TYPE";

    private static final String PROJECT_CODE = "PROJECT_EVOLUTION";

    private static final String SAMPLE_CODE = "CP001";

    private static final String SAMPLE_TYPE = "SAMPLE_TYPE";

    public static DatabaseInstancePE createDatabaseInstance(final String code)
    {
        final DatabaseInstancePE databaseInstance = new DatabaseInstancePE();
        databaseInstance.setCode(code);
        return databaseInstance;
    }

    static public PersonPE createPersonFromPrincipal(final Principal principal)
    {
        final PersonPE person = new PersonPE();
        person.setUserId(principal.getUserId());
        person.setFirstName(principal.getFirstName());
        person.setLastName(principal.getLastName());
        person.setEmail(principal.getEmail());
        return person;
    }

    static public GroupPE createGroup(final String groupCode,
            final DatabaseInstancePE databaseInstance)
    {
        final GroupPE group = new GroupPE();
        group.setCode(groupCode);
        group.setDatabaseInstance(databaseInstance);
        return group;
    }

    public static final ExperimentTypePE createExperimentType()
    {
        final ExperimentTypePE sampleTypePE = new ExperimentTypePE();
        sampleTypePE.setCode(CommonTestUtils.EXPERIMENT_TYPE);
        sampleTypePE
                .setDatabaseInstance(createDatabaseInstance(CommonTestUtils.HOME_DATABASE_INSTANCE_CODE));
        return sampleTypePE;
    }

    public static final ProjectIdentifier createProjectIdentifier()
    {
        final ProjectIdentifier identifier =
                new ProjectIdentifier(CommonTestUtils.HOME_DATABASE_INSTANCE_CODE,
                        CommonTestUtils.HOME_GROUP_CODE, CommonTestUtils.PROJECT_CODE);
        return identifier;
    }

    public static final ProjectPE createProject(final ProjectIdentifier pi)
    {
        final ProjectPE project = new ProjectPE();
        project.setCode(pi.getProjectCode());
        project.setGroup(createGroup(pi.getGroupCode(), createDatabaseInstance(pi
                .getDatabaseInstanceCode())));
        return project;
    }

    public static final SampleIdentifier createSampleIdentifier()
    {
        final SampleIdentifier identifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier(
                        CommonTestUtils.HOME_DATABASE_INSTANCE_CODE), CommonTestUtils.SAMPLE_CODE);
        return identifier;
    }

    protected static final SampleTypePE createSampleType()
    {
        final SampleTypePE sampleTypePE = new SampleTypePE();
        sampleTypePE.setCode(CommonTestUtils.SAMPLE_TYPE);
        sampleTypePE.setGeneratedFromHierarchyDepth(0);
        sampleTypePE.setContainerHierarchyDepth(0);
        return sampleTypePE;
    }

    public static final SamplePE createSample()
    {
        final SamplePE samplePE = new SamplePE();
        samplePE.setCode(CommonTestUtils.SAMPLE_CODE);
        final SampleTypePE sampleTypePE = createSampleType();
        samplePE.setSampleType(sampleTypePE);
        return samplePE;
    }

    public static final String USER_ID = "test";

}
