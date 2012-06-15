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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentContentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Contains methods and constants which may be used by many tests.
 * 
 * @author Izabela Adamczyk
 */
public class CommonTestUtils
{
    public static final VocabularyTermPE BRAIN = createVocabularyTerm("BRAIN");

    private static final VocabularyTermPE LEG = createVocabularyTerm("LEG");

    private static final VocabularyTermPE HEAD = createVocabularyTerm("HEAD");

    public static final TechId TECH_ID = new TechId(1L);

    public final static String ATTACHMENT_CONTENT_TEXT = "Lorem ipsum...";

    public final static String HOME_DATABASE_INSTANCE_CODE = "HOME_DATABASE";

    public static final String HOME_GROUP_CODE = "HOME_GROUP";

    private static final String EXPERIMENT_TYPE = "EXPERIMENT_TYPE";

    public static final String PROJECT_CODE = "PROJECT_EVOLUTION";

    public static final String EXPERIMENT_CODE = "EXPERIMENT_ONECELL_ORGANISM";

    private static final String SAMPLE_CODE = "CP001";

    private static final String SAMPLE_TYPE = "SAMPLE_TYPE";

    private static final String DATA_SET_CODE = "20081105092158673-1";

    private static final String DATA_SET_TYPE = "PROPRIETARY";

    public static final String USER_ID = "test";

    private static final String MATERIAL_TYPE_VIRUS = "MATERIAL_TYPE_VIRUS";

    public static final MaterialTypePE VIRUS = createMaterialType();

    public static int VERSION_22 = 22;

    public static String FILENAME = "oneCellOrganismData.txt";

    public static VocabularyPE ORGAN = createVocabulary("USER.ORGAN",
            Arrays.asList(HEAD, LEG, BRAIN));

    public static class ExamplePropertyTypes
    {

        public static PropertyTypePE INFECTED_ORGAN = createPropertyType("USER.INFECTED_ORGAN",
                DataTypeCode.CONTROLLEDVOCABULARY, ORGAN, null);

        public static PropertyTypePE INFECTING_VIRUS = createPropertyType("USER.INFECTING_VIRUS",
                DataTypeCode.MATERIAL, null, VIRUS);

        public static PropertyTypePE DESCRIPTION = createPropertyType("USER.DESCRIPTION",
                DataTypeCode.VARCHAR, null, null);

        public static PropertyTypePE NOTES = createPropertyType("USER.NOTES", DataTypeCode.VARCHAR,
                null, null);

        public static PropertyTypePE CATEGORY_DESCRIPTION = createPropertyType(
                "USER.CATEGORY_DESCRIPTION", DataTypeCode.VARCHAR, null, null);
    }

    public static ExperimentPropertyPE createCategoryProperty(ExperimentTypePE experimentType)
    {
        ExperimentTypePropertyTypePE categoryAssignment =
                createAssignment(ExamplePropertyTypes.CATEGORY_DESCRIPTION, experimentType);
        ExperimentPropertyPE added = new ExperimentPropertyPE();
        added.setEntityTypePropertyType(categoryAssignment);
        added.setValue("VE029910");
        return added;
    }

    public static ExperimentPropertyPE createNotesProperty(ExperimentTypePE experimentType)
    {
        ExperimentTypePropertyTypePE notesAssignment =
                createAssignment(ExamplePropertyTypes.NOTES, experimentType);
        ExperimentPropertyPE deleted = new ExperimentPropertyPE();
        deleted.setEntityTypePropertyType(notesAssignment);
        deleted.setValue("Check the impact on the hand on 03/04/2008.");
        return deleted;
    }

    public static ExperimentPropertyPE createOrganProperty(ExperimentTypePE experimentType)
    {

        ExperimentTypePropertyTypePE organAssignment =
                createAssignment(ExamplePropertyTypes.INFECTED_ORGAN, experimentType);
        ExperimentPropertyPE changed = new ExperimentPropertyPE();
        changed.setEntityTypePropertyType(organAssignment);
        changed.setVocabularyTerm(CommonTestUtils.BRAIN);
        return changed;
    }

    public static ExperimentTypePropertyTypePE createAssignment(PropertyTypePE propertyType,
            ExperimentTypePE type)
    {
        ExperimentTypePropertyTypePE assignment = new ExperimentTypePropertyTypePE();
        assignment.setEntityType(type);
        assignment.setPropertyType(propertyType);
        return assignment;
    }

    public static DatabaseInstancePE createDatabaseInstance(final String code)
    {
        final DatabaseInstancePE databaseInstance = new DatabaseInstancePE();
        databaseInstance.setCode(code);
        databaseInstance.setUuid("UUID-" + code);
        return databaseInstance;
    }

    public static DatabaseInstancePE createHomeDatabaseInstance()
    {
        return createDatabaseInstance(HOME_DATABASE_INSTANCE_CODE);
    }

    static public PersonPE createPersonFromPrincipal(final Principal principal)
    {
        final PersonPE person = new PersonPE();
        person.setUserId(principal.getUserId());
        person.setFirstName(principal.getFirstName());
        person.setLastName(principal.getLastName());
        person.setEmail(principal.getEmail());
        person.setDatabaseInstance(createHomeDatabaseInstance());
        person.setActive(true);

        return person;
    }

    static public SpacePE createSpace(final String groupCode,
            final DatabaseInstancePE databaseInstance)
    {
        final SpacePE space = new SpacePE();
        space.setCode(groupCode);
        space.setDatabaseInstance(databaseInstance);
        return space;
    }

    public static SpacePE createSpace(SpaceIdentifier identifier)
    {
        DatabaseInstancePE db = createDatabaseInstance(identifier.getDatabaseInstanceCode());
        return createSpace(identifier.getSpaceCode(), db);
    }

    public static final ExperimentTypePE createExperimentType()
    {
        final ExperimentTypePE sampleTypePE = new ExperimentTypePE();
        sampleTypePE.setCode(EXPERIMENT_TYPE);
        sampleTypePE.setDatabaseInstance(createHomeDatabaseInstance());
        return sampleTypePE;
    }

    public static final SpaceIdentifier createSpaceIdentifier()
    {
        final SpaceIdentifier identifier =
                new SpaceIdentifier(HOME_DATABASE_INSTANCE_CODE, HOME_GROUP_CODE);
        return identifier;
    }

    public static final ProjectIdentifier createProjectIdentifier()
    {
        final ProjectIdentifier identifier =
                new ProjectIdentifier(HOME_DATABASE_INSTANCE_CODE, HOME_GROUP_CODE, PROJECT_CODE);
        return identifier;
    }

    public static VocabularyPE createVocabulary(String fullCode, List<VocabularyTermPE> terms)
    {
        VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.setCode(fullCode);
        vocabulary.setDatabaseInstance(createHomeDatabaseInstance());
        if (terms != null)
        {
            vocabulary.setTerms(terms);
        }
        return vocabulary;
    }

    public static VocabularyTermPE createVocabularyTerm(String code)
    {
        VocabularyTermPE term = new VocabularyTermPE();
        term.setCode(code);
        return term;
    }

    public static final PropertyTypePE createPropertyType(String fullCode, DataTypeCode type,
            VocabularyPE vocabularyOrNull, MaterialTypePE materialTypeOrNull)
    {
        PropertyTypePE result = new PropertyTypePE();
        result.setCode(fullCode);
        result.setDatabaseInstance(createHomeDatabaseInstance());
        result.setType(createDataType(type));
        result.setVocabulary(vocabularyOrNull);
        result.setMaterialType(materialTypeOrNull);
        result.setDescription("Description of " + fullCode);
        result.setLabel("Label of " + fullCode);
        return result;
    }

    private static DataTypePE createDataType(DataTypeCode type)
    {
        DataTypePE result = new DataTypePE();
        result.setCode(type);
        return result;
    }

    public static final ProjectPE createProject(final ProjectIdentifier pi)
    {
        final ProjectPE project = new ProjectPE();
        project.setCode(pi.getProjectCode());
        project.setSpace(createSpace(pi.getSpaceCode(),
                createDatabaseInstance(pi.getDatabaseInstanceCode())));
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
        sampleTypePE.setId(1234567890123456L);
        sampleTypePE.setDatabaseInstance(createHomeDatabaseInstance());
        sampleTypePE.setCode(CommonTestUtils.SAMPLE_TYPE);
        sampleTypePE.setGeneratedFromHierarchyDepth(0);
        sampleTypePE.setContainerHierarchyDepth(0);
        sampleTypePE.setListable(true);
        sampleTypePE.setAutoGeneratedCode(false);
        sampleTypePE.setSubcodeUnique(false);
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

    public static final DataPE createDataSet()
    {
        final DataPE dataPE = new DataPE();
        dataPE.setCode(CommonTestUtils.DATA_SET_CODE);
        final DataSetTypePE dataSetTypePE = new DataSetTypePE();
        dataSetTypePE.setCode(CommonTestUtils.DATA_SET_TYPE);
        dataPE.setDataSetType(dataSetTypePE);
        return dataPE;
    }

    public static final MaterialPE createMaterial(String materialCode, String typeCode)
    {
        final MaterialPE materialPE = new MaterialPE();
        materialPE.setCode(materialCode);
        final MaterialTypePE materialTypePE = createMaterialType(typeCode);
        materialPE.setMaterialType(materialTypePE);
        return materialPE;
    }

    public static final ExperimentIdentifier createExperimentIdentifier()
    {
        final ExperimentIdentifier identifier =
                new ExperimentIdentifier(createProjectIdentifier(), EXPERIMENT_CODE);
        return identifier;
    }

    public static final ExperimentPE createExperiment()
    {
        return createExperiment(createExperimentIdentifier());
    }

    public static final ExperimentPE createExperiment(final ExperimentIdentifier ei)
    {
        final ExperimentPE exp = new ExperimentPE();
        final ExperimentTypePE expType = new ExperimentTypePE();
        expType.setCode("expType");
        exp.setExperimentType(expType);
        exp.setCode(ei.getExperimentCode());
        exp.setProject(createProject(new ProjectIdentifier(ei.getDatabaseInstanceCode(), ei
                .getSpaceCode(), ei.getProjectCode())));
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
        return createMaterialType(MATERIAL_TYPE_VIRUS);
    }

    public static MaterialTypePE createMaterialType(String typeCode)
    {
        final MaterialTypePE type = new MaterialTypePE();
        type.setCode(typeCode);
        type.setDatabaseInstance(createHomeDatabaseInstance());
        return type;
    }

    public final static SamplePropertyPE createSamplePropertyPE(final String code,
            final DataTypeCode dataType, final String value)
    {
        final SamplePropertyPE propertyPE = new SamplePropertyPE();
        final SampleTypePropertyTypePE entityTypePropertyTypePE = new SampleTypePropertyTypePE();
        final SampleTypePE sampleTypePE = new SampleTypePE();
        sampleTypePE.setListable(true);
        sampleTypePE.setAutoGeneratedCode(false);
        sampleTypePE.setSubcodeUnique(false);
        sampleTypePE.setGeneratedFromHierarchyDepth(0);
        sampleTypePE.setContainerHierarchyDepth(0);
        connectAndFill(code, code + "ST", dataType, value, propertyPE, entityTypePropertyTypePE,
                sampleTypePE);
        return propertyPE;
    }

    public final static ExperimentPropertyPE createExperimentPropertyPE(final String code,
            String type, final DataTypeCode dataType, final String value)
    {
        final ExperimentPropertyPE propertyPE = new ExperimentPropertyPE();
        final ExperimentTypePropertyTypePE entityTypePropertyTypePE =
                new ExperimentTypePropertyTypePE();
        final ExperimentTypePE sampleTypePE = new ExperimentTypePE();
        connectAndFill(code, type, dataType, value, propertyPE, entityTypePropertyTypePE,
                sampleTypePE);
        return propertyPE;
    }

    private static void connectAndFill(String code, String type, DataTypeCode dataType,
            String value, EntityPropertyPE propertyPE,
            EntityTypePropertyTypePE entityTypePropertyTypePE, EntityTypePE entityTypePE)
    {
        entityTypePE.setCode(type);
        PropertyTypePE propertyTypePE = new PropertyTypePE();
        propertyTypePE.setCode(code);
        propertyTypePE.setLabel(code);
        DataTypePE typePE = new DataTypePE();
        typePE.setCode(dataType);
        propertyTypePE.setType(typePE);
        entityTypePropertyTypePE.setPropertyType(propertyTypePE);
        entityTypePropertyTypePE.setEntityType(entityTypePE);
        propertyPE.setEntityTypePropertyType(entityTypePropertyTypePE);
        propertyPE.setValue(value);
    }

    public static String getResourceAsString(String path, String resource)
    {
        File file = new File(path, resource);
        try
        {
            return FileUtils.readFileToString(file);
        } catch (IOException ioex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ioex);
        }
    }
}
