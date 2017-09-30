/*
 * Copyright 2015 ETH Zuerich, CISD
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
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.ISemanticAnnotationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.SemanticAnnotationPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.update.SemanticAnnotationUpdate;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
public class UpdateSemanticAnnotationTest extends AbstractTest
{

    @Test
    public void testUpdateWithInstanceAdmin()
    {
        testUpdateWithUser(TEST_USER);
    }

    @Test
    public void testUpdateWithSpaceAdmin()
    {
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testUpdateWithUser(TEST_SPACE_USER);
                }
            }, null);
    }

    @Test
    public void testUpdateWithSpaceObserver()
    {
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testUpdateWithUser(TEST_GROUP_OBSERVER);
                }
            }, null);
    }

    @Test
    public void testUpdateWithNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISemanticAnnotationId annotationId = new SemanticAnnotationPermId("IDONTEXIST");
        final SemanticAnnotationUpdate update = new SemanticAnnotationUpdate();
        update.setSemanticAnnotationId(annotationId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSemanticAnnotations(sessionToken, Arrays.asList(update));
                }
            }, annotationId);
    }

    @Test
    public void testUpdate()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SemanticAnnotationPermId permId1 = new SemanticAnnotationPermId("20170918092158673-1");
        final SemanticAnnotationPermId permId2 = new SemanticAnnotationPermId("20170918092158673-2");

        Map<ISemanticAnnotationId, SemanticAnnotation> map =
                v3api.getSemanticAnnotations(sessionToken, Arrays.asList(permId1, permId2), new SemanticAnnotationFetchOptions());

        SemanticAnnotation before1 = map.get(permId1);
        SemanticAnnotation before2 = map.get(permId2);

        assertEquals(before1.getPermId(), permId1);
        assertEquals(before1.getPredicateOntologyId(), "testPredicateOntologyId1");
        assertEquals(before1.getPredicateOntologyVersion(), "testPredicateOntologyVersion1");
        assertEquals(before1.getPredicateAccessionId(), "testPredicateAccessionId1");
        assertEquals(before1.getDescriptorOntologyId(), "testDescriptorOntologyId1");
        assertEquals(before1.getDescriptorOntologyVersion(), "testDescriptorOntologyVersion1");
        assertEquals(before1.getDescriptorAccessionId(), "testDescriptorAccessionId1");

        assertEquals(before2.getPermId(), permId2);
        assertEquals(before2.getPredicateOntologyId(), "testPredicateOntologyId2");
        assertEquals(before2.getPredicateOntologyVersion(), "testPredicateOntologyVersion2");
        assertEquals(before2.getPredicateAccessionId(), "testPredicateAccessionId2");
        assertEquals(before2.getDescriptorOntologyId(), "testDescriptorOntologyId2");
        assertEquals(before2.getDescriptorOntologyVersion(), "testDescriptorOntologyVersion2");
        assertEquals(before2.getDescriptorAccessionId(), "testDescriptorAccessionId2");

        final SemanticAnnotationUpdate update1 = new SemanticAnnotationUpdate();
        update1.setSemanticAnnotationId(permId1);
        update1.setPredicateOntologyId("updatedPredicateOntologyId1");
        update1.setPredicateOntologyVersion("updatedPredicateOntologyVersion1");
        update1.setPredicateAccessionId("updatedPredicateAccessionId1");
        update1.setDescriptorOntologyId("updatedDescriptorOntologyId1");
        update1.setDescriptorOntologyVersion("updatedDescriptorOntologyVersion1");
        update1.setDescriptorAccessionId("updatedDescriptorAccessionId1");

        final SemanticAnnotationUpdate update2 = new SemanticAnnotationUpdate();
        update2.setSemanticAnnotationId(permId2);
        update2.setPredicateOntologyId("updatedPredicateOntologyId2");
        update2.setDescriptorOntologyId("updatedDescriptorOntologyId2");

        v3api.updateSemanticAnnotations(sessionToken, Arrays.asList(update1, update2));
        map = v3api.getSemanticAnnotations(sessionToken, Arrays.asList(permId1, permId2), new SemanticAnnotationFetchOptions());

        SemanticAnnotation after1 = map.get(permId1);
        SemanticAnnotation after2 = map.get(permId2);

        assertEquals(after1.getPermId(), permId1);
        assertEquals(after1.getPredicateOntologyId(), update1.getPredicateOntologyId().getValue());
        assertEquals(after1.getPredicateOntologyVersion(), update1.getPredicateOntologyVersion().getValue());
        assertEquals(after1.getPredicateAccessionId(), update1.getPredicateAccessionId().getValue());
        assertEquals(after1.getDescriptorOntologyId(), update1.getDescriptorOntologyId().getValue());
        assertEquals(after1.getDescriptorOntologyVersion(), update1.getDescriptorOntologyVersion().getValue());
        assertEquals(after1.getDescriptorAccessionId(), update1.getDescriptorAccessionId().getValue());

        assertEquals(after2.getPermId(), permId2);
        assertEquals(after2.getPredicateOntologyId(), update2.getPredicateOntologyId().getValue());
        assertEquals(after2.getPredicateOntologyVersion(), before2.getPredicateOntologyVersion());
        assertEquals(after2.getPredicateAccessionId(), before2.getPredicateAccessionId());
        assertEquals(after2.getDescriptorOntologyId(), update2.getDescriptorOntologyId().getValue());
        assertEquals(after2.getDescriptorOntologyVersion(), before2.getDescriptorOntologyVersion());
        assertEquals(after2.getDescriptorAccessionId(), before2.getDescriptorAccessionId());
    }

    private void testUpdateWithUser(String userId)
    {
        String sessionToken = v3api.login(userId, PASSWORD);

        SemanticAnnotationUpdate update = new SemanticAnnotationUpdate();
        update.setSemanticAnnotationId(new SemanticAnnotationPermId("20170918092158673-1"));
        update.setPredicateOntologyId("testPredicateOntologyId1Updated");

        v3api.updateSemanticAnnotations(sessionToken, Arrays.asList(update));
    }

}
