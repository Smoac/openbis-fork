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
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentContentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Contains methods and constants which may be used by many tests.
 * 
 * @author Izabela Adamczyk
 */
public class CommonTestUtils
{
    public final static String ATTACHMENT_CONTENT_TEXT = "Lorem ipsum...";

    public final static String HOME_DATABASE_INSTANCE_CODE = "HOME_DATABASE";

    public static final String HOME_GROUP_CODE = "HOME_GROUP";

    private static final String EXPERIMENT_TYPE = "EXPERIMENT_TYPE";

    public static final String PROJECT_CODE = "PROJECT_EVOLUTION";

    public static final String EXPERIMENT_CODE = "EXPERIMENT_ONECELL_ORGANISM";

    private static final String SAMPLE_CODE = "CP001";

    private static final String SAMPLE_TYPE = "SAMPLE_TYPE";

    public static int VERSION_22 = 22;

    public static String FILENAME = "oneCellOrganismData.txt";

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

    public static GroupPE createGroup(GroupIdentifier projectIdentifier)
    {
        DatabaseInstancePE db = createDatabaseInstance(projectIdentifier.getDatabaseInstanceCode());
        return createGroup(projectIdentifier.getGroupCode(), db);
    }

    public static final ExperimentTypePE createExperimentType()
    {
        final ExperimentTypePE sampleTypePE = new ExperimentTypePE();
        sampleTypePE.setCode(EXPERIMENT_TYPE);
        sampleTypePE.setDatabaseInstance(createDatabaseInstance(HOME_DATABASE_INSTANCE_CODE));
        return sampleTypePE;
    }

    public static final ProjectIdentifier createProjectIdentifier()
    {
        final ProjectIdentifier identifier =
                new ProjectIdentifier(HOME_DATABASE_INSTANCE_CODE, HOME_GROUP_CODE, PROJECT_CODE);
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

    public static final SampleTypePE createSampleType()
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

    private static final String MATERIAL_TYPE_VIRUS = "MATERIAL_TYPE_VIRUS";

    public static final ExperimentIdentifier createExperimentIdentifier()
    {
        final ExperimentIdentifier identifier =
                new ExperimentIdentifier(createProjectIdentifier(), EXPERIMENT_CODE);
        return identifier;
    }

    public static final ExperimentPE createExperiment(final ExperimentIdentifier ei)
    {
        final ExperimentPE exp = new ExperimentPE();
        exp.setCode(ei.getExperimentCode());
        exp.setProject(createProject(new ProjectIdentifier(ei.getDatabaseInstanceCode(), ei
                .getGroupCode(), ei.getProjectCode())));
        return exp;
    }

    public static AttachmentPE createAttachment()
    {
        final AttachmentPE attachmentPE = new AttachmentPE();
        attachmentPE.setFileName(FILENAME);
        attachmentPE.setVersion(VERSION_22);
        attachmentPE.setAttachmentContent(createAttachmentContent(ATTACHMENT_CONTENT_TEXT));
        return attachmentPE;
    }

    public static AttachmentContentPE createAttachmentContent(final String content)
    {
        final AttachmentContentPE attachmentContentPE = new AttachmentContentPE();
        attachmentContentPE.setValue(content.getBytes());
        return attachmentContentPE;
    }

    public static MaterialTypePE createMaterialType()
    {

        final MaterialTypePE type = new MaterialTypePE();
        type.setCode(MATERIAL_TYPE_VIRUS);
        type.setDatabaseInstance(createDatabaseInstance(HOME_DATABASE_INSTANCE_CODE));
        return type;
    }

}
