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
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyAssignmentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.create.SemanticAnnotationCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.ISemanticAnnotationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.SemanticAnnotationPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
public class CreateSemanticAnnotationTest extends AbstractTest
{

    @Test
    public void testCreateWithInstanceAdmin()
    {
        testCreateWithUser(TEST_USER);
    }

    @Test
    public void testCreateWithSpaceAdmin()
    {
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testCreateWithUser(TEST_SPACE_USER);
                }
            }, null);
    }

    @Test
    public void testCreateWithSpaceObserver()
    {
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testCreateWithUser(TEST_GROUP_OBSERVER);
                }
            }, null);
    }

    @Test
    public void testCreateWithAllFields()
    {
        SemanticAnnotationCreation creation = new SemanticAnnotationCreation();
        creation.setEntityTypeId(new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE));

        creation.setPredicateOntologyId("a predicateOntologyId");
        creation.setPredicateOntologyVersion("a predicateOntologyVersion");
        creation.setPredicateAccessionId("a predicateAccessionId");

        creation.setDescriptorOntologyId("a descriptorOntologyId");
        creation.setDescriptorOntologyVersion("a descriptorOntologyVersion");
        creation.setDescriptorAccessionId("a descriptorAccessionId");

        SemanticAnnotationFetchOptions fo = new SemanticAnnotationFetchOptions();
        fo.withEntityType();

        SemanticAnnotation annotation = testCreate(TEST_USER, creation, fo);

        assertEquals(annotation.getEntityType().getPermId(), creation.getEntityTypeId());
        assertEquals(annotation.getPredicateOntologyId(), creation.getPredicateOntologyId());
        assertEquals(annotation.getPredicateOntologyVersion(), creation.getPredicateOntologyVersion());
        assertEquals(annotation.getPredicateAccessionId(), creation.getPredicateAccessionId());
        assertEquals(annotation.getDescriptorOntologyId(), creation.getDescriptorOntologyId());
        assertEquals(annotation.getDescriptorOntologyVersion(), creation.getDescriptorOntologyVersion());
        assertEquals(annotation.getDescriptorAccessionId(), creation.getDescriptorAccessionId());
    }

    @Test
    public void testCreateWithNoOwner()
    {
        SemanticAnnotationCreation creation = new SemanticAnnotationCreation();

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testCreate(TEST_USER, creation, new SemanticAnnotationFetchOptions());
                }
            }, "Exactly one of the following fields has be set: entityTypeId, propertyTypeId or propertyAssignmentId");
    }

    @Test
    public void testCreateWithMultipleOwners()
    {
        SemanticAnnotationCreation creation = new SemanticAnnotationCreation();
        creation.setEntityTypeId(new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE));
        creation.setPropertyTypeId(new PropertyTypePermId("COMMENT"));
        creation.setPropertyAssignmentId(
                new PropertyAssignmentPermId(new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE), new PropertyTypePermId("COMMENT")));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testCreate(TEST_USER, creation, new SemanticAnnotationFetchOptions());
                }
            }, "Exactly one of the following fields has be set: entityTypeId, propertyTypeId or propertyAssignmentId");
    }

    @Test
    public void testCreateWithSampleType()
    {
        SemanticAnnotationCreation creation = new SemanticAnnotationCreation();
        creation.setEntityTypeId(new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE));

        SemanticAnnotationFetchOptions fo = new SemanticAnnotationFetchOptions();
        fo.withEntityType();

        SemanticAnnotation annotation = testCreate(TEST_USER, creation, fo);
        assertEquals(annotation.getEntityType().getPermId(), creation.getEntityTypeId());
    }

    @Test
    public void testCreateWithSampleTypeNonexistent()
    {
        SemanticAnnotationCreation creation = new SemanticAnnotationCreation();
        creation.setEntityTypeId(new EntityTypePermId("I_DONT_EXIST", EntityKind.SAMPLE));

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testCreate(TEST_USER, creation, new SemanticAnnotationFetchOptions());
                }
            }, creation.getEntityTypeId());
    }

    @Test
    public void testCreateWithNonSampleType()
    {
        SemanticAnnotationCreation creation = new SemanticAnnotationCreation();
        creation.setEntityTypeId(new EntityTypePermId("SIRNA_HCS", EntityKind.EXPERIMENT));

        SemanticAnnotationFetchOptions fo = new SemanticAnnotationFetchOptions();
        fo.withEntityType();

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testCreate(TEST_USER, creation, fo);
                }
            }, "Semantic annotations can be defined for sample entity types only");
    }

    @Test
    public void testCreateWithPropertyType()
    {
        SemanticAnnotationCreation creation = new SemanticAnnotationCreation();
        creation.setPropertyTypeId(new PropertyTypePermId("DESCRIPTION"));

        SemanticAnnotationFetchOptions fo = new SemanticAnnotationFetchOptions();
        fo.withPropertyType();

        SemanticAnnotation annotation = testCreate(TEST_USER, creation, fo);
        assertEquals(annotation.getPropertyType().getPermId(), creation.getPropertyTypeId());
    }

    @Test
    public void testCreateWithPropertyTypeNonexistent()
    {
        SemanticAnnotationCreation creation = new SemanticAnnotationCreation();
        creation.setPropertyTypeId(new PropertyTypePermId("I_DONT_EXIST"));

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testCreate(TEST_USER, creation, new SemanticAnnotationFetchOptions());
                }
            }, creation.getPropertyTypeId());
    }

    @Test
    public void testCreateWithSampleTypePropertyType()
    {
        SemanticAnnotationCreation creation = new SemanticAnnotationCreation();
        creation.setPropertyAssignmentId(
                new PropertyAssignmentPermId(new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE), new PropertyTypePermId("COMMENT")));

        SemanticAnnotationFetchOptions fo = new SemanticAnnotationFetchOptions();
        fo.withPropertyAssignment();

        SemanticAnnotation annotation = testCreate(TEST_USER, creation, fo);
        assertEquals(annotation.getPropertyAssignment().getPermId(), creation.getPropertyAssignmentId());
    }

    @Test
    public void testCreateWithSampleTypePropertyTypeNonexistent()
    {
        SemanticAnnotationCreation creation = new SemanticAnnotationCreation();
        creation.setPropertyAssignmentId(
                new PropertyAssignmentPermId(new EntityTypePermId("I_DONT_EXIST", EntityKind.SAMPLE), new PropertyTypePermId("I_DONT_EXIST")));

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testCreate(TEST_USER, creation, new SemanticAnnotationFetchOptions());
                }
            }, creation.getPropertyAssignmentId());
    }

    @Test
    public void testCreateWithNonSampleTypePropertyType()
    {
        SemanticAnnotationCreation creation = new SemanticAnnotationCreation();
        creation.setPropertyAssignmentId(
                new PropertyAssignmentPermId(new EntityTypePermId("SIRNA_HCS", EntityKind.EXPERIMENT), new PropertyTypePermId("DESCRIPTION")));

        SemanticAnnotationFetchOptions fo = new SemanticAnnotationFetchOptions();
        fo.withPropertyAssignment();

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testCreate(TEST_USER, creation, fo);
                }
            }, "Semantic annotations can be defined for sample property assignments only");
    }

    private void testCreateWithUser(String userId)
    {
        SemanticAnnotationCreation creation = new SemanticAnnotationCreation();
        creation.setEntityTypeId(new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE));
        creation.setPredicateOntologyId(userId + "_ontology_id");

        SemanticAnnotation annotation = testCreate(userId, creation, new SemanticAnnotationFetchOptions());

        assertEquals(annotation.getDescriptorOntologyId(), creation.getDescriptorOntologyId());
    }

    private SemanticAnnotation testCreate(String userId, SemanticAnnotationCreation creation, SemanticAnnotationFetchOptions fo)
    {
        final String sessionToken = v3api.login(userId, PASSWORD);

        List<SemanticAnnotationPermId> permIds = v3api.createSemanticAnnotations(sessionToken, Arrays.asList(creation));

        Map<ISemanticAnnotationId, SemanticAnnotation> map =
                v3api.getSemanticAnnotations(sessionToken, permIds, fo);

        assertEquals(map.size(), 1);

        return map.values().iterator().next();
    }

}
