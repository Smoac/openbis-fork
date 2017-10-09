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
import java.util.Iterator;
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

/**
 * @author pkupczyk
 */
public class GetSemanticAnnotationTest extends AbstractTest
{

    @Test
    public void testGetByPermId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SemanticAnnotationPermId permId1 = new SemanticAnnotationPermId("ST_MASTER_PLATE");
        SemanticAnnotationPermId permId2 = new SemanticAnnotationPermId("ST_CONTROL_LAYOUT_PT_PLATE_GEOMETRY");

        Map<ISemanticAnnotationId, SemanticAnnotation> map =
                v3api.getSemanticAnnotations(sessionToken, Arrays.asList(permId1, permId2),
                        new SemanticAnnotationFetchOptions());

        assertEquals(2, map.size());

        Iterator<SemanticAnnotation> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId2);

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SemanticAnnotationPermId permId1 = new SemanticAnnotationPermId("ST_MASTER_PLATE");
        SemanticAnnotationPermId permId2 = new SemanticAnnotationPermId("I_DONT_EXIST");
        SemanticAnnotationPermId permId3 = new SemanticAnnotationPermId("ST_CONTROL_LAYOUT_PT_PLATE_GEOMETRY");

        Map<ISemanticAnnotationId, SemanticAnnotation> map =
                v3api.getSemanticAnnotations(sessionToken, Arrays.asList(permId1, permId2, permId3), new SemanticAnnotationFetchOptions());

        assertEquals(2, map.size());

        Iterator<SemanticAnnotation> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId3);

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId3).getPermId(), permId3);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsDuplicated()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SemanticAnnotationPermId permId1 = new SemanticAnnotationPermId("ST_MASTER_PLATE");
        SemanticAnnotationPermId permId2 = new SemanticAnnotationPermId("ST_MASTER_PLATE");

        Map<ISemanticAnnotationId, SemanticAnnotation> map =
                v3api.getSemanticAnnotations(sessionToken, Arrays.asList(permId1, permId2), new SemanticAnnotationFetchOptions());

        assertEquals(1, map.size());

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsWithInstanceAdmin()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        Map<ISemanticAnnotationId, SemanticAnnotation> map = v3api.getSemanticAnnotations(sessionToken,
                Arrays.asList(new SemanticAnnotationPermId("ST_MASTER_PLATE")), new SemanticAnnotationFetchOptions());

        assertEquals(1, map.size());
    }

    @Test
    public void testGetByIdsWithSpaceAdmin()
    {
        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        Map<ISemanticAnnotationId, SemanticAnnotation> map = v3api.getSemanticAnnotations(sessionToken,
                Arrays.asList(new SemanticAnnotationPermId("ST_MASTER_PLATE")), new SemanticAnnotationFetchOptions());

        assertEquals(1, map.size());
    }

    @Test
    public void testGetByIdsWithSpaceObserver()
    {
        String sessionToken = v3api.login(TEST_GROUP_OBSERVER, PASSWORD);

        Map<ISemanticAnnotationId, SemanticAnnotation> map = v3api.getSemanticAnnotations(sessionToken,
                Arrays.asList(new SemanticAnnotationPermId("ST_MASTER_PLATE")), new SemanticAnnotationFetchOptions());

        assertEquals(1, map.size());
    }

    @Test
    public void testGetByIdsWithFetchOptionsEmpty()
    {
        SemanticAnnotationPermId permId = new SemanticAnnotationPermId("ST_MASTER_PLATE");
        SemanticAnnotationFetchOptions fetchOptions = new SemanticAnnotationFetchOptions();

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        Map<ISemanticAnnotationId, SemanticAnnotation> map = v3api.getSemanticAnnotations(sessionToken, Arrays.asList(permId), fetchOptions);
        SemanticAnnotation annotation = map.get(permId);

        assertEquals(annotation.getPermId(), permId);
        assertEquals(annotation.getPredicateOntologyId(), "testPredicateOntologyId1");
        assertEquals(annotation.getPredicateOntologyVersion(), "testPredicateOntologyVersion1");
        assertEquals(annotation.getPredicateAccessionId(), "testPredicateAccessionId1");
        assertEquals(annotation.getDescriptorOntologyId(), "testDescriptorOntologyId1");
        assertEquals(annotation.getDescriptorOntologyVersion(), "testDescriptorOntologyVersion1");
        assertEquals(annotation.getDescriptorAccessionId(), "testDescriptorAccessionId1");
        assertEqualsDate(annotation.getCreationDate(), "2017-09-18 10:18:10");

        assertEntityTypeNotFetched(annotation);
        assertPropertyTypeNotFetched(annotation);
        assertPropertyAssignmentNotFetched(annotation);
    }

    @Test
    public void testGetByIdsWithEntityType()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SemanticAnnotationCreation creation = new SemanticAnnotationCreation();
        creation.setEntityTypeId(new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE));

        List<SemanticAnnotationPermId> permIds = v3api.createSemanticAnnotations(sessionToken, Arrays.asList(creation));

        SemanticAnnotationFetchOptions fetchOptions = new SemanticAnnotationFetchOptions();
        fetchOptions.withEntityType();

        Map<ISemanticAnnotationId, SemanticAnnotation> map = v3api.getSemanticAnnotations(sessionToken, permIds, fetchOptions);
        SemanticAnnotation annotation = map.get(permIds.get(0));

        assertEquals(annotation.getPermId(), permIds.get(0));
        assertEquals(annotation.getEntityType().getCode(), "CELL_PLATE");
        assertEquals(annotation.getEntityType().getDescription(), "Cell Plate");

        assertPropertyTypeNotFetched(annotation);
        assertPropertyAssignmentNotFetched(annotation);
    }

    @Test
    public void testGetByIdsWithPropertyType()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SemanticAnnotationCreation creation = new SemanticAnnotationCreation();
        creation.setPropertyTypeId(new PropertyTypePermId("COMMENT"));

        List<SemanticAnnotationPermId> permIds = v3api.createSemanticAnnotations(sessionToken, Arrays.asList(creation));

        SemanticAnnotationFetchOptions fetchOptions = new SemanticAnnotationFetchOptions();
        fetchOptions.withPropertyType();

        Map<ISemanticAnnotationId, SemanticAnnotation> map = v3api.getSemanticAnnotations(sessionToken, permIds, fetchOptions);
        SemanticAnnotation annotation = map.get(permIds.get(0));

        assertEquals(annotation.getPermId(), permIds.get(0));
        assertEquals(annotation.getPropertyType().getCode(), "COMMENT");
        assertEquals(annotation.getPropertyType().getDescription(), "Any other comments");

        assertEntityTypeNotFetched(annotation);
        assertPropertyAssignmentNotFetched(annotation);
    }

    @Test
    public void testGetByIdsWithPropertyAssignment()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SemanticAnnotationCreation creation = new SemanticAnnotationCreation();
        creation.setPropertyAssignmentId(
                new PropertyAssignmentPermId(new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE), new PropertyTypePermId("COMMENT")));

        List<SemanticAnnotationPermId> permIds = v3api.createSemanticAnnotations(sessionToken, Arrays.asList(creation));

        SemanticAnnotationFetchOptions fetchOptions = new SemanticAnnotationFetchOptions();
        fetchOptions.withPropertyAssignment().withEntityType();
        fetchOptions.withPropertyAssignment().withPropertyType();

        Map<ISemanticAnnotationId, SemanticAnnotation> map = v3api.getSemanticAnnotations(sessionToken, permIds, fetchOptions);
        SemanticAnnotation annotation = map.get(permIds.get(0));

        assertEquals(annotation.getPermId(), permIds.get(0));
        assertEquals(annotation.getPropertyAssignment().getEntityType().getCode(), "CELL_PLATE");
        assertEquals(annotation.getPropertyAssignment().getEntityType().getDescription(), "Cell Plate");
        assertEquals(annotation.getPropertyAssignment().getPropertyType().getCode(), "COMMENT");
        assertEquals(annotation.getPropertyAssignment().getPropertyType().getDescription(), "Any other comments");

        assertEntityTypeNotFetched(annotation);
        assertPropertyTypeNotFetched(annotation);
    }

}
