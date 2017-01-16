/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
public class CreateSampleTypeTest extends AbstractTest
{

    @Test
    public void testCreateWithCodeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleTypeCreation type = new SampleTypeCreation();

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSampleTypes(sessionToken, Arrays.asList(type));
                }
            }, "Code cannot be empty");
    }

    @Test
    public void testCreateWithCodeExisting()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleTypeCreation type = new SampleTypeCreation();
        type.setCode("WELL");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSampleTypes(sessionToken, Arrays.asList(type));
                }
            }, "Entity type 'WELL'  already exists in the database and needs to be unique");
    }

    @Test
    public void testCreateWithCodeIncorrect()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleTypeCreation type = new SampleTypeCreation();
        type.setCode("?!*");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSampleTypes(sessionToken, Arrays.asList(type));
                }
            }, "Given code '?!*' contains illegal characters (allowed: A-Z, a-z, 0-9 and _, -, .)");
    }

    @Test
    public void testCreateWithValidationPluginIdNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleTypeCreation type = new SampleTypeCreation();
        type.setCode("NEW_SAMPLE_TYPE");
        type.setValidationPluginId(new PluginPermId("IDONTEXIST"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSampleTypes(sessionToken, Arrays.asList(type));
                }
            }, "Object with PluginPermId = [IDONTEXIST] has not been found");
    }

    @Test
    public void testCreateWithValidationPluginNameCaseSensitive()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleTypeCreation typeCorrectCase = new SampleTypeCreation();
        typeCorrectCase.setCode("NEW_SAMPLE_TYPE");
        typeCorrectCase.setValidationPluginId(new PluginPermId("validateOK"));

        v3api.createSampleTypes(sessionToken, Arrays.asList(typeCorrectCase));

        final SampleTypeCreation typeIncorrectCase = new SampleTypeCreation();
        typeIncorrectCase.setCode("NEW_SAMPLE_TYPE_2");
        typeIncorrectCase.setValidationPluginId(new PluginPermId("validateok"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSampleTypes(sessionToken, Arrays.asList(typeIncorrectCase));
                }
            }, "Object with PluginPermId = [validateok] has not been found");
    }

    @Test
    public void testCreateWithValidationPluginOfIncorrectType()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleTypeCreation type = new SampleTypeCreation();
        type.setCode("NEW_SAMPLE_TYPE");
        type.setValidationPluginId(new PluginPermId("properties"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSampleTypes(sessionToken, Arrays.asList(type));
                }
            }, "Sample type validation plugin has to be of type 'Entity Validator'. The specified plugin with id 'properties' is of type 'Dynamic Property Evaluator'");
    }

    @Test
    public void testCreateWithValidationPluginOfIncorrectEntityType()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleTypeCreation type = new SampleTypeCreation();
        type.setCode("NEW_SAMPLE_TYPE");
        type.setValidationPluginId(new PluginPermId("testExperiment"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSampleTypes(sessionToken, Arrays.asList(type));
                }
            }, "Sample type validation plugin has entity kind set to 'EXPERIMENT'. Expected a plugin where entity kind is either 'SAMPLE' or null");
    }

    @Test
    public void testCreateWithPropertyTypeIdNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleTypeCreation type = new SampleTypeCreation();
        type.setCode("NEW_SAMPLE_TYPE");

        PropertyAssignmentCreation assignment = new PropertyAssignmentCreation();
        type.setPropertyAssignments(Arrays.asList(assignment));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSampleTypes(sessionToken, Arrays.asList(type));
                }
            }, "PropertyTypeId cannot be null");
    }

    @Test
    public void testCreateWithPropertyTypeIdNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleTypeCreation type = new SampleTypeCreation();
        type.setCode("NEW_SAMPLE_TYPE");

        PropertyAssignmentCreation assignment = new PropertyAssignmentCreation();
        assignment.setPropertyTypeId(new PropertyTypePermId("IDONTEXIST"));
        type.setPropertyAssignments(Arrays.asList(assignment));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSampleTypes(sessionToken, Arrays.asList(type));
                }
            }, "Object with PropertyTypePermId = [IDONTEXIST] has not been found");
    }

    @Test
    public void testCreateWithPropertyAssignmentsWithIncorrectOrdinal()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PropertyAssignmentCreation assignmentCreation = new PropertyAssignmentCreation();
        assignmentCreation.setPropertyTypeId(new PropertyTypePermId("DESCRIPTION"));
        assignmentCreation.setOrdinal(-1);

        final SampleTypeCreation typeCreation = new SampleTypeCreation();
        typeCreation.setCode("NEW_SAMPLE_TYPE");
        typeCreation.setPropertyAssignments(Arrays.asList(assignmentCreation));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSampleTypes(sessionToken, Arrays.asList(typeCreation));
                }
            }, "Ordinal cannot be <= 0");
    }

    @Test
    public void testCreateWithPropertyAssignmentsWithNullOrdinals()
    {
        PropertyAssignmentCreation assignmentCreation1 = new PropertyAssignmentCreation();
        assignmentCreation1.setPropertyTypeId(new PropertyTypePermId("DESCRIPTION"));

        PropertyAssignmentCreation assignmentCreation2 = new PropertyAssignmentCreation();
        assignmentCreation2.setPropertyTypeId(new PropertyTypePermId("SIZE"));

        final SampleTypeCreation typeCreation = new SampleTypeCreation();
        typeCreation.setCode("NEW_SAMPLE_TYPE");
        typeCreation.setPropertyAssignments(Arrays.asList(assignmentCreation1, assignmentCreation2));

        SampleType type = createAndGet(TEST_USER, typeCreation);

        assertEquals(type.getPropertyAssignments().size(), 2);

        PropertyAssignment assignment1 = type.getPropertyAssignments().get(0);
        assertEquals(assignment1.getPropertyType().getCode(), "DESCRIPTION");
        assertEquals(assignment1.getOrdinal(), Integer.valueOf(1));

        PropertyAssignment assignment2 = type.getPropertyAssignments().get(1);
        assertEquals(assignment2.getPropertyType().getCode(), "SIZE");
        assertEquals(assignment2.getOrdinal(), Integer.valueOf(2));
    }

    @Test
    public void testCreateWithPropertyAssignmentsWithNullAndNotNullOrdinals()
    {
        PropertyAssignmentCreation assignmentCreation1 = new PropertyAssignmentCreation();
        assignmentCreation1.setPropertyTypeId(new PropertyTypePermId("DESCRIPTION"));
        assignmentCreation1.setOrdinal(2);

        PropertyAssignmentCreation assignmentCreation2 = new PropertyAssignmentCreation();
        assignmentCreation2.setPropertyTypeId(new PropertyTypePermId("VOLUME"));
        assignmentCreation2.setOrdinal(null);

        PropertyAssignmentCreation assignmentCreation3 = new PropertyAssignmentCreation();
        assignmentCreation3.setPropertyTypeId(new PropertyTypePermId("SIZE"));
        assignmentCreation3.setOrdinal(1);

        PropertyAssignmentCreation assignmentCreation4 = new PropertyAssignmentCreation();
        assignmentCreation4.setPropertyTypeId(new PropertyTypePermId("OFFSET"));
        assignmentCreation4.setOrdinal(null);

        final SampleTypeCreation typeCreation = new SampleTypeCreation();
        typeCreation.setCode("NEW_SAMPLE_TYPE");
        typeCreation.setPropertyAssignments(Arrays.asList(assignmentCreation1, assignmentCreation2, assignmentCreation3, assignmentCreation4));

        SampleType type = createAndGet(TEST_USER, typeCreation);

        assertEquals(type.getPropertyAssignments().size(), 4);

        PropertyAssignment assignment1 = type.getPropertyAssignments().get(0);
        assertEquals(assignment1.getPropertyType().getCode(), "SIZE");
        assertEquals(assignment1.getOrdinal(), Integer.valueOf(1));

        PropertyAssignment assignment2 = type.getPropertyAssignments().get(1);
        assertEquals(assignment2.getPropertyType().getCode(), "DESCRIPTION");
        assertEquals(assignment2.getOrdinal(), Integer.valueOf(2));

        PropertyAssignment assignment3 = type.getPropertyAssignments().get(2);
        assertEquals(assignment3.getPropertyType().getCode(), "VOLUME");
        assertEquals(assignment3.getOrdinal(), Integer.valueOf(3));

        PropertyAssignment assignment4 = type.getPropertyAssignments().get(3);
        assertEquals(assignment4.getPropertyType().getCode(), "OFFSET");
        assertEquals(assignment4.getOrdinal(), Integer.valueOf(4));
    }

    @Test
    public void testCreateWithPropertyAssignmentWithPluginIdNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PropertyAssignmentCreation assignmentCreation = new PropertyAssignmentCreation();
        assignmentCreation.setPropertyTypeId(new PropertyTypePermId("DESCRIPTION"));
        assignmentCreation.setPluginId(new PluginPermId("IDONTEXIST"));

        final SampleTypeCreation type = new SampleTypeCreation();
        type.setCode("NEW_SAMPLE_TYPE");
        type.setPropertyAssignments(Arrays.asList(assignmentCreation));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSampleTypes(sessionToken, Arrays.asList(type));
                }
            }, "Object with PluginPermId = [IDONTEXIST] has not been found");
    }

    @Test
    public void testCreateWithPropertyAssignmentWithPluginNameCaseSensitive()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PropertyAssignmentCreation assignmentCreationCorrectCase = new PropertyAssignmentCreation();
        assignmentCreationCorrectCase.setPropertyTypeId(new PropertyTypePermId("DESCRIPTION"));
        assignmentCreationCorrectCase.setPluginId(new PluginPermId("properties"));

        final SampleTypeCreation typeCorrectCase = new SampleTypeCreation();
        typeCorrectCase.setCode("NEW_SAMPLE_TYPE");
        typeCorrectCase.setPropertyAssignments(Arrays.asList(assignmentCreationCorrectCase));

        v3api.createSampleTypes(sessionToken, Arrays.asList(typeCorrectCase));

        PropertyAssignmentCreation assignmentCreationIncorrectCase = new PropertyAssignmentCreation();
        assignmentCreationIncorrectCase.setPropertyTypeId(new PropertyTypePermId("DESCRIPTION"));
        assignmentCreationIncorrectCase.setPluginId(new PluginPermId("PROPERTIES"));

        final SampleTypeCreation typeIncorrectCase = new SampleTypeCreation();
        typeIncorrectCase.setCode("NEW_SAMPLE_TYPE_2");
        typeIncorrectCase.setPropertyAssignments(Arrays.asList(assignmentCreationIncorrectCase));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSampleTypes(sessionToken, Arrays.asList(typeIncorrectCase));
                }
            }, "Object with PluginPermId = [PROPERTIES] has not been found");
    }

    @Test
    public void testCreateWithPropertyAssignmentWithPluginOfIncorrectType()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PropertyAssignmentCreation assignmentCreation = new PropertyAssignmentCreation();
        assignmentCreation.setPropertyTypeId(new PropertyTypePermId("DESCRIPTION"));
        assignmentCreation.setPluginId(new PluginPermId("validateOK"));

        final SampleTypeCreation type = new SampleTypeCreation();
        type.setCode("NEW_SAMPLE_TYPE");
        type.setPropertyAssignments(Arrays.asList(assignmentCreation));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSampleTypes(sessionToken, Arrays.asList(type));
                }
            }, "Property assignment plugin has to be of type 'Dynamic Property Evaluator' or 'Managed Property Handler'. The specified plugin with id 'validateOK' is of type 'Entity Validator'");
    }

    @Test
    public void testCreateWithPropertyAssignmentWithPluginOfIncorrectEntityType()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PropertyAssignmentCreation assignmentCreation = new PropertyAssignmentCreation();
        assignmentCreation.setPropertyTypeId(new PropertyTypePermId("DESCRIPTION"));
        assignmentCreation.setPluginId(new PluginPermId("propertiesExperiment"));

        final SampleTypeCreation type = new SampleTypeCreation();
        type.setCode("NEW_SAMPLE_TYPE");
        type.setPropertyAssignments(Arrays.asList(assignmentCreation));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSampleTypes(sessionToken, Arrays.asList(type));
                }
            }, "Property assignment plugin has entity kind set to 'EXPERIMENT'. Expected a plugin where entity kind is either 'SAMPLE' or null");
    }

    @Test
    public void testCreateWithInstanceAdmin()
    {
        testCreateWithUser(TEST_USER);
    }

    @Test
    public void testCreateWithSpaceAdmin()
    {
        assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testCreateWithUser(TEST_SPACE_USER);
                }
            });
    }

    private void testCreateWithUser(String userId)
    {
        PropertyAssignmentCreation assignmentCreation = new PropertyAssignmentCreation();
        assignmentCreation.setSection("test section");
        assignmentCreation.setOrdinal(10);
        assignmentCreation.setPropertyTypeId(new PropertyTypePermId("DESCRIPTION"));
        assignmentCreation.setPluginId(new PluginPermId("properties"));
        assignmentCreation.setMandatory(true);
        assignmentCreation.setInitialValueForExistingEntities("initial value");
        assignmentCreation.setShowInEditView(true);
        assignmentCreation.setShowRawValueInForms(true);

        SampleTypeCreation creation = new SampleTypeCreation();
        creation.setCode("NEW_SAMPLE_TYPE");
        creation.setDescription("a new description");
        creation.setAutoGeneratedCode(true);
        creation.setSubcodeUnique(true);
        creation.setGeneratedCodePrefix("TEST_PREFIX");
        creation.setListable(true);
        creation.setShowContainer(true);
        creation.setShowParents(true);
        creation.setShowParentMetadata(true);
        creation.setValidationPluginId(new PluginPermId("validateOK"));
        creation.setPropertyAssignments(Arrays.asList(assignmentCreation));

        SampleType sampleType = createAndGet(userId, creation);

        assertEquals(sampleType.getCode(), creation.getCode());
        assertEquals(sampleType.getDescription(), creation.getDescription());
        assertEquals(sampleType.isAutoGeneratedCode(), (Boolean) creation.isAutoGeneratedCode());
        assertEquals(sampleType.isSubcodeUnique(), (Boolean) creation.isSubcodeUnique());
        assertEquals(sampleType.getGeneratedCodePrefix(), creation.getGeneratedCodePrefix());
        assertEquals(sampleType.isListable(), (Boolean) creation.isListable());
        assertEquals(sampleType.isShowContainer(), (Boolean) creation.isShowContainer());
        assertEquals(sampleType.isShowParents(), (Boolean) creation.isShowParents());
        assertEquals(sampleType.isShowParentMetadata(), (Boolean) creation.isShowParentMetadata());
        // TODO validation script

        assertEquals(sampleType.getPropertyAssignments().size(), 1);

        PropertyAssignment assignment = sampleType.getPropertyAssignments().get(0);
        assertEquals(assignment.getSection(), assignmentCreation.getSection());
        assertEquals(assignment.getOrdinal(), assignmentCreation.getOrdinal());
        assertEquals(assignment.getPropertyType().getCode(), "DESCRIPTION");
        // TODO plugin
        assertEquals(assignment.isMandatory(), (Boolean) assignmentCreation.isMandatory());
        assertEquals(assignment.isShowInEditView(), (Boolean) assignmentCreation.isShowInEditView());
        assertEquals(assignment.isShowRawValueInForms(), (Boolean) assignmentCreation.isShowRawValueInForms());
        assertEquals(assignment.getRegistrator().getUserId(), userId);
    }

    private SampleType createAndGet(String userId, SampleTypeCreation creation)
    {
        final String sessionToken = v3api.login(userId, PASSWORD);

        List<EntityTypePermId> permIds = v3api.createSampleTypes(sessionToken, Arrays.asList(creation));

        assertEquals(permIds.size(), 1);

        SampleTypeSearchCriteria criteria = new SampleTypeSearchCriteria();
        criteria.withId().thatEquals(permIds.get(0));

        SampleTypeFetchOptions fo = new SampleTypeFetchOptions();
        fo.withPropertyAssignments().withPropertyType();
        fo.withPropertyAssignments().withRegistrator();

        SearchResult<SampleType> results = v3api.searchSampleTypes(sessionToken, criteria, fo);

        assertEquals(results.getObjects().size(), 1);

        return results.getObjects().get(0);
    }

}
