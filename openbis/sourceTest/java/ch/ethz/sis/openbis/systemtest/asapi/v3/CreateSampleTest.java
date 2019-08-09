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
import static org.testng.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.create.AttachmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.Batch;
import ch.ethz.sis.openbis.systemtest.asapi.v3.index.ReindexingState;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

import junit.framework.Assert;

/**
 * @author pkupczyk
 */
public class CreateSampleTest extends AbstractSampleTest
{
    @Test
    public void testCreateSampleUsingCreationIdAsSpaceId()
    {
        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_USER, PASSWORD);

                    SampleCreation creation = new SampleCreation();
                    creation.setCode("TEST_SAMPLE_42");
                    creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
                    CreationId creationId = new CreationId("not-a-space-id");
                    creation.setCreationId(creationId);
                    creation.setSpaceId(creationId);

                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, "Object with CreationId = [not-a-space-id] has not been found");

    }

    @Test
    public void testCreateSharedSampleWithNoHomeSpaceAndNoAdminRights()
    {
        final String code = "TEST_TO_FAIL";
        SampleIdentifier identifier = new SampleIdentifier("/" + code);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_NO_HOME_SPACE, PASSWORD);

                    SampleCreation creation = new SampleCreation();
                    creation.setCode(code);
                    creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
                    creation.setCreationId(new CreationId("creation " + code));

                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, identifier);
    }

    @Test
    public void testCreateSharedSampleWithNoAdminRights()
    {
        final String code = "TEST_TO_FAIL";
        SampleIdentifier identifier = new SampleIdentifier("/" + code);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);

                    SampleCreation creation = new SampleCreation();
                    creation.setCode(code);
                    creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
                    creation.setCreationId(new CreationId("creation " + code));

                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, identifier);
    }

    @Test
    public void testCreateWithIndexCheck()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sample = sampleCreation("TO_BE_REINDEXED");
        ReindexingState state = new ReindexingState();

        List<SamplePermId> permIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        assertSamplesReindexed(state, permIds.get(0).getPermId());
    }

    @Test
    public void testCreateWithNonAutogeneratedCodeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final SampleCreation sample = sampleCreation(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Arrays.asList(sample));
                }
            }, "Code cannot be empty for a non auto generated code", patternContains("checking data (1/1)"));
    }

    @Test
    public void testCreateWithAutogeneratedCodeNotNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final SampleCreation sample = sampleCreation("SAMPLE_WITH_USER_GIVEN_CODE");
        sample.setAutoGeneratedCode(true);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Arrays.asList(sample));
                }
            }, "Code should be empty when auto generated code is selected",
                patternContains("checking data (1/1)", toDblQuotes("'code' : 'SAMPLE_WITH_USER_GIVEN_CODE'")));
    }

    @Test
    public void testCreateWithAutogeneratedCodeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final SampleCreation sample1 = sampleCreation(null);
        sample1.setAutoGeneratedCode(true);
        final SampleCreation sample2 = sampleCreation(null);
        sample2.setAutoGeneratedCode(true);

        List<SamplePermId> sampleWithAutogeneratedCode = v3api.createSamples(sessionToken, Arrays.asList(sample1, sample2));
        AssertionUtil.assertCollectionSize(sampleWithAutogeneratedCode, 2);
    }

    @Test
    public void testCreateWithAdminUserInAnotherSpace()
    {
        final String code = "TEST_TO_FAIL";
        final SampleIdentifier identifier = new SampleIdentifier("/TEST-SPACE/" + code);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);

                    SampleCreation creation = new SampleCreation();
                    creation.setCode(code);
                    creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
                    creation.setSpaceId(new SpacePermId("TEST-SPACE"));
                    creation.setCreationId(new CreationId("creation " + code));

                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, identifier);
    }

    @Test
    public void testCreateWithCodeExisting()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation sample = sampleCreation("SAMPLE_WITH_EXISTING_CODE");
        v3api.createSamples(sessionToken, Arrays.asList(sample));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Arrays.asList(sample));
                }
            }, "Insert/Update of sample (code: SAMPLE_WITH_EXISTING_CODE) failed because sample with the same code already exists");
    }

    @Test
    public void testCreateWithCodeIncorrect()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final SampleCreation sample = sampleCreation("?!*");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Arrays.asList(sample));
                }
            }, "The code '?!*' contains illegal characters", patternContains("checking data (1/1)", toDblQuotes("'code' : '?!*'")));
    }

    @Test
    public void testCreateWithTypeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITHOUT_TYPE");
        sample.setSpaceId(new SpacePermId("CISD"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Arrays.asList(sample));
                }
            }, "Type id cannot be null", patternContains("checking data (1/1)", toDblQuotes("'code' : 'SAMPLE_WITHOUT_TYPE'")));
    }

    @Test
    public void testCreateWithTypeNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final IEntityTypeId typeId = new EntityTypePermId("IDONTEXIST");
        final SampleCreation sample = new SampleCreation();
        sample.setTypeId(typeId);
        sample.setCode("SAMPLE_WITH_NONEXISTENT_TYPE");
        sample.setSpaceId(new SpacePermId("CISD"));

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Arrays.asList(sample));
                }
            }, typeId, patternContains("checking data (1/1)", toDblQuotes("'code' : 'SAMPLE_WITH_NONEXISTENT_TYPE'")));
    }

    @Test
    public void testCreateWithPropertyCodeNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_NONEXISTENT_PROPERTY_CODE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProperty("NONEXISTENT_PROPERTY_CODE", "any value");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Arrays.asList(creation));
                }
            }, "Property type with code 'NONEXISTENT_PROPERTY_CODE' does not exist",
                patternContains("updating properties (1/1)", toDblQuotes("'identifier' : '/CISD/SAMPLE_WITH_NONEXISTENT_PROPERTY_CODE'")));
    }

    @Test
    public void testCreateWithPropertyValueIncorrect()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_INCORRECT_PROPERTY_VALUE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProperty("ORGANISM", "NON_EXISTENT_ORGANISM");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Arrays.asList(creation));
                }
            }, "Vocabulary value 'NON_EXISTENT_ORGANISM' is not valid. It must exist in 'ORGANISM' controlled vocabulary",
                patternContains("updating properties (1/1)", toDblQuotes("'identifier' : '/CISD/SAMPLE_WITH_INCORRECT_PROPERTY_VALUE'")));
    }

    @Test
    public void testCreateWithPropertyValueMandatoryButNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_EMPTY_MANDATORY_PROPERTY");
        creation.setTypeId(new EntityTypePermId("CONTROL_LAYOUT"));
        creation.setSpaceId(new SpacePermId("CISD"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Arrays.asList(creation));
                }
            }, "Value of mandatory property '$PLATE_GEOMETRY' not specified",
                patternContains("verifying (1/1)", toDblQuotes("'identifier' : '/CISD/SAMPLE_WITH_EMPTY_MANDATORY_PROPERTY'")));
    }

    @Test
    public void testCreateWithSpaceNullAndExperimentNotNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation creation = new SampleCreation();
        creation.setCode("SHARED_SAMPLE_TEST");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, "Shared samples cannot be attached to experiments",
                patternContains("verifying (1/1)", toDblQuotes("'identifier' : '/SHARED_SAMPLE_TEST'")));
    }

    @Test
    public void testCreateWithExperiment()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));

        List<SamplePermId> permIds = v3api.createSamples(sessionToken, Collections.singletonList(creation));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withExperiment();
        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, permIds, fetchOptions);
        Sample sample = map.get(permIds.get(0));
        assertEquals(sample.getCode(), "SAMPLE_WITH_EXPERIMENT");
        assertEquals(sample.getSpace().getCode(), "CISD");
        assertEquals(sample.getExperiment().getCode(), "EXP1");
    }

    @Test
    public void testCreateWithSpaceNullAsAdminUser()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation creation = new SampleCreation();
        creation.setCode("SHARED_SAMPLE_TEST");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        List<SamplePermId> permIds = v3api.createSamples(sessionToken, Collections.singletonList(creation));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, permIds, fetchOptions);
        Sample sample = map.values().iterator().next();

        assertEquals(sample.getCode(), "SHARED_SAMPLE_TEST");
        assertNull(sample.getSpace());
    }

    @Test
    public void testCreateWithSpaceNullAsSpaceUser()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final SampleCreation creation = new SampleCreation();
        creation.setCode("SHARED_SAMPLE_TEST");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, new SampleIdentifier("/SHARED_SAMPLE_TEST"),
                patternContains("checking access (1/1)", toDblQuotes("'identifier' : '/SHARED_SAMPLE_TEST'")));
    }

    @Test
    public void testCreateWithSpaceUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISpaceId spaceId = new SpacePermId("CISD");
        final SampleCreation creation = new SampleCreation();
        creation.setCode("UNAUTHORIZED_SPACE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(spaceId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, new SampleIdentifier("/CISD/UNAUTHORIZED_SPACE"),
                patternContains("checking access (1/1)", toDblQuotes("'identifier' : '/CISD/UNAUTHORIZED_SPACE'")));
    }

    @Test
    public void testCreateWithSpaceNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISpaceId spaceId = new SpacePermId("IDONTEXIST");
        final SampleCreation creation = new SampleCreation();
        creation.setCode("NONEXISTENT_SPACE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(spaceId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, spaceId, patternContains("setting relation sample-space (1/1)", toDblQuotes("'identifier' : '/NONEXISTENT_SPACE'")));
    }

    @Test
    public void testCreateWithSpaceInconsistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_INCONSISTENT_SPACE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("TEST-SPACE"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, "Sample space must be the same as experiment space",
                patternContains("verifying (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/SAMPLE_WITH_INCONSISTENT_SPACE'")));
    }

    @Test
    public void testCreateWithExperimentUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IExperimentId experimentId = new ExperimentPermId("200811050951882-1028");
        final SampleCreation creation = new SampleCreation();
        creation.setCode("UNAUTHORIZED_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("TEST-SPACE"));
        creation.setExperimentId(experimentId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, experimentId,
                patternContains("setting relation sample-experiment (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/UNAUTHORIZED_EXPERIMENT'")));
    }

    @Test
    public void testCreateWithExperimentNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IExperimentId experimentId = new ExperimentPermId("IDONTEXIST");
        final SampleCreation creation = new SampleCreation();
        creation.setCode("NONEXISTENT_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("TEST-SPACE"));
        creation.setExperimentId(experimentId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, experimentId,
                patternContains("setting relation sample-experiment (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/NONEXISTENT_EXPERIMENT'")));
    }

    @Test(dataProvider = "tf-ft-tt")
    public void testCreateWithParentChild(boolean setRelationOnChild, boolean setRelationOnParent)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sampleParent = new SampleCreation();
        sampleParent.setCode("SAMPLE_PARENT");
        sampleParent.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleParent.setSpaceId(new SpacePermId("CISD"));
        sampleParent.setCreationId(new CreationId("parentid"));

        SampleCreation sampleChild = new SampleCreation();
        sampleChild.setCode("SAMPLE_CHILDREN");
        sampleChild.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleChild.setSpaceId(new SpacePermId("CISD"));
        sampleChild.setCreationId(new CreationId("childid"));

        if (setRelationOnChild)
        {
            sampleChild.setParentIds(Arrays.asList(sampleParent.getCreationId()));
        }

        if (setRelationOnParent)
        {
            sampleParent.setChildIds(Arrays.asList(sampleChild.getCreationId()));
        }

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sampleParent, sampleChild));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withChildrenUsing(fetchOptions);
        fetchOptions.withParentsUsing(fetchOptions);

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, sampleIds, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        Sample parent = samples.get(0);
        Sample child = samples.get(1);

        AssertionUtil.assertCollectionContains(parent.getChildren(), child);
        AssertionUtil.assertCollectionContains(child.getParents(), parent);

        AssertionUtil.assertCollectionSize(child.getParents(), 1);
        AssertionUtil.assertCollectionSize(child.getChildren(), 0);

        AssertionUtil.assertCollectionSize(parent.getParents(), 0);
        AssertionUtil.assertCollectionSize(parent.getChildren(), 1);
    }

    @Test
    public void testCreateWithParentChildCircularDependency()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sampleParent = new SampleCreation();
        sampleParent.setCode("SAMPLE_PARENT");
        sampleParent.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleParent.setSpaceId(new SpacePermId("CISD"));
        sampleParent.setCreationId(new CreationId("parentid"));

        List<SamplePermId> parentPermId = v3api.createSamples(sessionToken, Arrays.asList(sampleParent));

        final SampleCreation sampleChild = new SampleCreation();
        sampleChild.setCode("SAMPLE_CHILD");
        sampleChild.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleChild.setSpaceId(new SpacePermId("CISD"));
        sampleChild.setCreationId(new CreationId("childid"));
        sampleChild.setParentIds(parentPermId);

        final SampleCreation sampleGrandChild = new SampleCreation();
        sampleGrandChild.setCode("SAMPLE_GRAND_CHILD");
        sampleGrandChild.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleGrandChild.setSpaceId(new SpacePermId("CISD"));
        sampleGrandChild.setParentIds(Arrays.asList(sampleChild.getCreationId()));
        sampleGrandChild.setChildIds(parentPermId);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Arrays.asList(sampleChild, sampleGrandChild));
                }
            }, "Circular dependency found");
    }

    @Test
    public void testCreateWithParentAndChildViolatingBusinessRules()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation sampleParent = new SampleCreation();
        sampleParent.setCode("SAMPLE_PARENT");
        sampleParent.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleParent.setSpaceId(new SpacePermId("CISD"));
        sampleParent.setCreationId(new CreationId("parentid"));

        final SampleCreation sampleChild = new SampleCreation();
        sampleChild.setCode("SAMPLE_CHILDREN");
        sampleChild.setTypeId(new EntityTypePermId("CELL_PLATE"));

        sampleChild.setParentIds(Arrays.asList(sampleParent.getCreationId()));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Arrays.asList(sampleParent, sampleChild));
                }
            }, "can not be a space sample because of a child database instance sample",
                patternContains("verifying (1/2)", toDblQuotes("'identifier' : '/CISD/SAMPLE_PARENT'")));
    }

    @Test
    public void testCreateWithParentUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId parentId = createCisdSample("PARENT_SAMPLE");
        final SampleCreation creation = sampleCreation("TEST-SPACE", "HAS_UNAUTHORIZED_PARENT");
        creation.setParentIds(Collections.singletonList(parentId));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, parentId,
                patternContains("setting relation sample-parents (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/HAS_UNAUTHORIZED_PARENT'")));
    }

    @Test
    public void testCreateWithParentNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId parentId = new SamplePermId("IDONTEXIST");
        final SampleCreation creation = sampleCreation("TEST-SPACE", "HAS_NONEXISTENT_PARENT");
        creation.setParentIds(Collections.singletonList(parentId));

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, parentId,
                patternContains("setting relation sample-parents (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/HAS_NONEXISTENT_PARENT'")));
    }

    @Test
    public void testCreateWithChildUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId childId = createCisdSample("CHILD_SAMPLE");
        final SampleCreation creation = sampleCreation("TEST-SPACE", "HAS_UNAUTHORIZED_CHILD");
        creation.setChildIds(Collections.singletonList(childId));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, childId,
                patternContains("setting relation sample-children (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/HAS_UNAUTHORIZED_CHILD'")));
    }

    @Test
    public void testCreateWithChildNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId childId = new SamplePermId("IDONTEXIST");
        final SampleCreation creation = sampleCreation("TEST-SPACE", "HAS_NONEXISTENT_CHILD");
        creation.setChildIds(Collections.singletonList(childId));

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, childId, patternContains("setting relation sample-children (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/HAS_NONEXISTENT_CHILD'")));
    }

    @Test
    public void testCreateWithChildViolatingBusinessRules()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation sampleChild = new SampleCreation();
        sampleChild.setCode("SAMPLE_CHILDREN");
        sampleChild.setTypeId(new EntityTypePermId("CELL_PLATE"));

        sampleChild.setParentIds(Arrays.asList(new SampleIdentifier("/CISD/MP002-1")));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Arrays.asList(sampleChild));
                }
            }, "The database instance sample '/SAMPLE_CHILDREN' can not be child of the space sample '/CISD/MP002-1'",
                patternContains("verifying (1/1)", toDblQuotes("'identifier' : '/SAMPLE_CHILDREN'")));
    }

    @Test
    public void testCreateWithContainerUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId containerId = createCisdSample("CONTAINER_SAMPLE");
        final SampleCreation creation = sampleCreation("TEST-SPACE", "HAS_UNAUTHORIZED_CONTAINER");
        creation.setContainerId(containerId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, containerId,
                patternContains("setting relation sample-container (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/HAS_UNAUTHORIZED_CONTAINER'")));
    }

    @Test
    public void testCreateWithContainerNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId containerId = new SamplePermId("IDONTEXIST");
        final SampleCreation creation = sampleCreation("TEST-SPACE", "HAS_NONEXISTENT_CONTAINER");
        creation.setContainerId(containerId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, containerId,
                patternContains("setting relation sample-container (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/HAS_NONEXISTENT_CONTAINER'")));
    }

    @Test(dataProvider = "tf-ft-tt")
    public void testCreateWithContainerComponents(boolean setRelationOnChild, boolean setRelationOnParent)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sampleParent = new SampleCreation();
        sampleParent.setCode("SAMPLE_CONTAINER");
        sampleParent.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleParent.setSpaceId(new SpacePermId("CISD"));
        sampleParent.setCreationId(new CreationId("parentid"));

        SampleCreation sampleChild = new SampleCreation();
        sampleChild.setCode("SAMPLE_SUB_SAMPLE");
        sampleChild.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleChild.setSpaceId(new SpacePermId("CISD"));
        sampleChild.setCreationId(new CreationId("childid"));

        if (setRelationOnChild)
        {
            sampleChild.setContainerId(sampleParent.getCreationId());
        }

        if (setRelationOnParent)
        {
            sampleParent.setComponentIds(Arrays.asList(sampleChild.getCreationId()));
        }

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sampleParent, sampleChild));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withComponentsUsing(fetchOptions);
        fetchOptions.withContainerUsing(fetchOptions);

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, sampleIds, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        Sample container = samples.get(0);
        Sample subSample = samples.get(1);

        AssertionUtil.assertCollectionContains(container.getComponents(), subSample);
        assertEquals(subSample.getContainer(), container);

        AssertionUtil.assertCollectionSize(subSample.getComponents(), 0);

        assertEquals(container.getContainer(), null);
        AssertionUtil.assertCollectionSize(container.getComponents(), 1);
    }

    @Test
    public void testCreateWithContainerCircularDependency()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation sample1 = sampleCreation("SAMPLE_1");
        final SampleCreation sample2 = sampleCreation("SAMPLE_2");
        final SampleCreation sample3 = sampleCreation("SAMPLE_3");

        sample2.setContainerId(sample1.getCreationId());
        sample3.setContainerId(sample2.getCreationId());
        sample1.setContainerId(sample3.getCreationId());

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Arrays.asList(sample1, sample2, sample3));
                }
            }, "cannot be it's own container", patternContains("verifying (1/3)", toDblQuotes("'identifier' : '/CISD/SAMPLE_3:SAMPLE_1'")));
    }

    @Test(dataProvider = "tf-ft-tt", enabled = false)
    public void testCreateWithContainerInconsistent(boolean setSubSample, boolean setOtherContainer)
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation container1 = new SampleCreation();
        container1.setCode("SAMPLE_CONTAINER_1");
        container1.setTypeId(new EntityTypePermId("CELL_PLATE"));
        container1.setSpaceId(new SpacePermId("CISD"));
        container1.setCreationId(new CreationId("cont1"));

        final SampleCreation container2 = new SampleCreation();
        container2.setCode("SAMPLE_CONTAINER_2");
        container2.setTypeId(new EntityTypePermId("CELL_PLATE"));
        container2.setSpaceId(new SpacePermId("CISD"));
        container2.setCreationId(new CreationId("cont2"));

        final SampleCreation subSample = new SampleCreation();
        subSample.setCode("SAMPLE_SUB_SAMPLE");
        subSample.setTypeId(new EntityTypePermId("CELL_PLATE"));
        subSample.setSpaceId(new SpacePermId("CISD"));
        subSample.setCreationId(new CreationId("subSample"));

        container1.setComponentIds(Arrays.asList(subSample.getCreationId()));

        if (setSubSample)
        {
            subSample.setContainerId(container2.getCreationId());
        }

        if (setOtherContainer)
        {
            container2.setComponentIds(Arrays.asList(subSample.getCreationId()));
        }

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Arrays.asList(container1, container2, subSample));
                }
            }, "Inconsistent container");
    }

    @Test
    public void testCreateWithContainerCreatedWithSampleReferencedByIdentifier()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sc1 = sampleCreation("SAMPLE_1");
        SampleCreation sc2 = sampleCreation("SAMPLE_2");

        sc2.setContainerId(new SampleIdentifier("/CISD/SAMPLE_1"));

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sc1, sc2));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withContainerUsing(fetchOptions);

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, sampleIds, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 2);

        Sample sample1 = samples.get(0);
        Sample sample2 = samples.get(1);

        assertEquals(sample2.getContainer(), sample1);
    }

    @Test
    public void testCreateWithContainerCreatedWithSampleReferencedByCreationId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sc1 = sampleCreation("SAMPLE_1");
        SampleCreation sc2 = sampleCreation("SAMPLE_2");

        sc2.setContainerId(sc1.getCreationId());

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sc1, sc2));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withContainerUsing(fetchOptions);

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, sampleIds, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 2);

        Sample sample1 = samples.get(0);
        Sample sample2 = samples.get(1);

        assertEquals(sample2.getContainer(), sample1);
    }

    @Test
    public void testCreateWithComponentAndIndexCheck()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SamplePermId componentId = createCisdSample("COMPONENT_SAMPLE");
        final SampleCreation creation = sampleCreation("CISD", "HAS_COMPONENT");
        creation.setComponentIds(Collections.singletonList(componentId));

        ReindexingState state = new ReindexingState();

        List<SamplePermId> containerIds = v3api.createSamples(sessionToken, Collections.singletonList(creation));
        assertEquals(containerIds.size(), 1);
        SamplePermId containerId = containerIds.get(0);

        assertSamplesReindexed(state, containerId.getPermId(), componentId.getPermId());
    }

    @Test
    public void testCreateWithComponentsUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId componentId = createCisdSample("COMPONENT_SAMPLE");
        final SampleCreation creation = sampleCreation("TEST-SPACE", "HAS_UNAUTHORIZED_COMPONENT");
        creation.setComponentIds(Collections.singletonList(componentId));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, componentId,
                patternContains("setting relation sample-components (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/HAS_UNAUTHORIZED_COMPONENT'")));
    }

    @Test
    public void testCreateWithComponentNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId componentId = new SamplePermId("IDONTEXIST");
        final SampleCreation creation = sampleCreation("TEST-SPACE", "HAS_NONEXISTENT_COMPONENT");
        creation.setComponentIds(Collections.singletonList(componentId));

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, componentId,
                patternContains("setting relation sample-components (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/HAS_NONEXISTENT_COMPONENT'")));
    }

    @Test
    public void testCreateWithSystemProperty()
    {
        String systemPropertyName = "$PLATE_GEOMETRY";
        String systemPropertyValue = "384_WELLS_16X24";

        String simplePropertyCode = "PLATE_GEOMETRY";
        String simplePropertValue = "I'm just random";

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        createNewPropertyType(sessionToken, "MASTER_PLATE", simplePropertyCode);

        SampleCreation samp1 = new SampleCreation();
        samp1.setCode("SAMPLE_WITH_SYS_PROPERTY");
        samp1.setTypeId(new EntityTypePermId("MASTER_PLATE"));
        samp1.setSpaceId(new SpacePermId("CISD"));
        samp1.setProperty(systemPropertyName, systemPropertyValue);
        samp1.setProperty(simplePropertyCode, simplePropertValue);

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken,
                Arrays.asList(samp1));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, sampleIds, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        Sample foundSample = samples.get(0);
        assertEquals(foundSample.getProperty(systemPropertyName), systemPropertyValue);
        assertEquals(foundSample.getProperty(simplePropertyCode), simplePropertValue);
    }

    @Test
    public void testCreateWithMultipleSamples()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleCreation samp1 = new SampleCreation();
        samp1.setCode("SAMPLE_WITH_SPACE1");
        samp1.setTypeId(new EntityTypePermId("CELL_PLATE"));
        samp1.setSpaceId(new SpacePermId("CISD"));
        samp1.setProperty("COMMENT", "hello");
        samp1.setContainerId(new SampleIdentifier("/CISD/MP002-1"));
        samp1.setTagIds(Arrays.<ITagId> asList(
                new TagPermId("/test/TEST_METAPROJECTS"), new TagPermId("/test/ANOTHER_TEST_METAPROJECTS")));
        AttachmentCreation a = new AttachmentCreation();

        byte[] attachmentContent = "attachment".getBytes();
        a.setContent(attachmentContent);
        a.setDescription("attachment description");
        a.setFileName("attachment.txt");
        a.setTitle("attachment title");
        samp1.setAttachments(Arrays.asList(a));

        SampleCreation samp2 = new SampleCreation();
        samp2.setCode("SAMPLE_WITH_SPACE2");
        samp2.setTypeId(new EntityTypePermId("CELL_PLATE"));
        samp2.setSpaceId(new SpacePermId("CISD"));
        samp2.setContainerId(new SamplePermId("200811050917877-331"));
        List<SamplePermId> sample2Parents = Arrays.asList(new SamplePermId("200811050917877-331"), new SamplePermId("200902091219327-1025"));
        samp2.setParentIds(sample2Parents);

        SampleCreation sampleWithOutSpace = new SampleCreation();
        sampleWithOutSpace.setCode("SAMPLE_WITHOUT_SPACE");
        sampleWithOutSpace.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleWithOutSpace.setChildIds(sample2Parents);

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken,
                Arrays.asList(samp1, samp2, sampleWithOutSpace));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withType();
        fetchOptions.withSpace();
        fetchOptions.withRegistrator();
        fetchOptions.withAttachments().withContent();
        fetchOptions.withContainer();
        fetchOptions.withTags();
        fetchOptions.withParents();

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, sampleIds, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        Sample sampleWithSpace1 = samples.get(0);
        assertEquals(sampleWithSpace1.getCode(), "SAMPLE_WITH_SPACE1");
        assertEquals(sampleWithSpace1.getType().getCode(), "CELL_PLATE");
        assertEquals(sampleWithSpace1.getSpace().getPermId().getPermId(), "CISD");
        assertEquals(sampleWithSpace1.getIdentifier().getIdentifier(), "/CISD/MP002-1:SAMPLE_WITH_SPACE1");
        assertEquals(sampleWithSpace1.getRegistrator().getUserId(), TEST_USER);
        assertEquals(sampleWithSpace1.getContainer().getCode(), "MP002-1");
        List<Attachment> attachments = sampleWithSpace1.getAttachments();
        AssertionUtil.assertCollectionSize(attachments, 1);
        assertEquals(attachments.get(0).getContent(), attachmentContent);
        HashSet<String> tagIds = new HashSet<String>();
        for (Tag tag : sampleWithSpace1.getTags())
        {
            tagIds.add(tag.getPermId().getPermId());
        }
        assertEquals(tagIds, new HashSet<String>(Arrays.asList("/test/TEST_METAPROJECTS", "/test/ANOTHER_TEST_METAPROJECTS")));

        Sample sampleWithSpace2 = samples.get(1);
        assertEquals(sampleWithSpace2.getCode(), "SAMPLE_WITH_SPACE2");
        assertEquals(sampleWithSpace2.getType().getCode(), "CELL_PLATE");
        assertEquals(sampleWithSpace2.getSpace().getPermId().getPermId(), "CISD");
        assertEquals(sampleWithSpace2.getIdentifier().getIdentifier(), "/CISD/MP002-1:SAMPLE_WITH_SPACE2");
        assertEquals(sampleWithSpace2.getRegistrator().getUserId(), TEST_USER);
        assertEquals(sampleWithSpace2.getContainer().getCode(), "MP002-1");
        List<String> parentsIds = new LinkedList<String>();
        for (Sample s : sampleWithSpace2.getParents())
        {
            parentsIds.add(s.getPermId().getPermId());
        }
        Collections.sort(parentsIds);
        assertEquals(parentsIds.toString(), "[200811050917877-331, 200902091219327-1025]");

        Sample sampleWithoutSpace = samples.get(2);
        assertEquals(sampleWithoutSpace.getCode(), "SAMPLE_WITHOUT_SPACE");
        assertEquals(sampleWithoutSpace.getType().getCode(), "CELL_PLATE");
        assertEquals(sampleWithoutSpace.getSpace(), null);
        assertEquals(sampleWithoutSpace.getIdentifier().getIdentifier(), "/SAMPLE_WITHOUT_SPACE");
        assertEquals(sampleWithoutSpace.getRegistrator().getUserId(), TEST_USER);

        SampleFetchOptions onlyParentsAndChildren = new SampleFetchOptions();
        onlyParentsAndChildren.withParents();
        onlyParentsAndChildren.withChildren();

        map = v3api.getSamples(sessionToken, sample2Parents, onlyParentsAndChildren);
        samples = new ArrayList<Sample>(map.values());

        for (Sample sample : samples)
        {
            AssertionUtil.assertCollectionContainsString(sample.getParents(), sampleWithoutSpace.getPermId().getPermId());
            AssertionUtil.assertCollectionContainsString(sample.getChildren(), sampleWithSpace2.getPermId().getPermId());
        }
    }

    @Test
    public void testCreateWithExceptionContextAndFewerSamplesThanBatchSize()
    {
        int sampleCount = 5;
        int incorrectSampleIndex = 1;

        Assert.assertTrue(sampleCount < Batch.DEFAULT_BATCH_SIZE);
        Assert.assertTrue(incorrectSampleIndex < sampleCount);
        Assert.assertTrue(incorrectSampleIndex > 0);

        testWithExceptionContext(sampleCount, incorrectSampleIndex);
    }

    @Test
    public void testCreateWithExceptionContextAndMoreSamplesThanBatchSize()
    {
        int sampleCount = Batch.DEFAULT_BATCH_SIZE + 5;
        int incorrectSampleIndex = Batch.DEFAULT_BATCH_SIZE + 1;

        Assert.assertTrue(sampleCount > Batch.DEFAULT_BATCH_SIZE);
        Assert.assertTrue(incorrectSampleIndex < sampleCount);
        Assert.assertTrue(incorrectSampleIndex > Batch.DEFAULT_BATCH_SIZE);

        testWithExceptionContext(sampleCount, incorrectSampleIndex);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testCreateWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("PA_TEST");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("TEST-SPACE"));
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        v3api.createSamples(sessionToken, Collections.singletonList(creation));
                    }
                });
        } else if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<SamplePermId> permIds = v3api.createSamples(sessionToken, Collections.singletonList(creation));
            assertEquals(permIds.size(), 1);
        } else
        {
            assertUnauthorizedObjectAccessException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        v3api.createSamples(sessionToken, Collections.singletonList(creation));
                    }
                }, creation.getExperimentId());
        }
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("LOG_TEST_1");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        SampleCreation creation2 = new SampleCreation();
        creation2.setCode("LOG_TEST_2");
        creation2.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation2.setSpaceId(new SpacePermId("CISD"));
        
        SampleCreation creation3 = new SampleCreation();
        creation3.setCode("LOG_TEST_3");
        creation3.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation3.setSpaceId(new SpacePermId("CISD"));
        creation3.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));

        v3api.createSamples(sessionToken, Arrays.asList(creation, creation2, creation3));

        assertAccessLog(
                "create-samples  NEW_SAMPLES('[SampleCreation[spaceId=<null>,projectId=<null>,experimentId=<null>,code=LOG_TEST_1], SampleCreation[spaceId=CISD,projectId=<null>,experimentId=<null>,code=LOG_TEST_2], SampleCreation[spaceId=CISD,projectId=<null>,experimentId=/CISD/NEMO/EXP1,code=LOG_TEST_3]]')");
    }

    private void testWithExceptionContext(int sampleCount, int incorrectSampleIndex)
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final List<SampleCreation> samples = new ArrayList<SampleCreation>();
        for (int i = 0; i < sampleCount; i++)
        {
            samples.add(sampleCreation("TEST_SAMPLE_" + (i + 1)));
        }

        samples.get(incorrectSampleIndex).setTypeId(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, samples);
                }
            }, "Type id cannot be null",
                patternContains("checking data (" + (incorrectSampleIndex + 1) + "/" + sampleCount + ")",
                        toDblQuotes("'code' : 'TEST_SAMPLE_" + (incorrectSampleIndex + 1) + "'")));
    }

    private SampleCreation sampleCreation(String code)
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode(code);
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setCreationId(new CreationId("creation " + code));
        return creation;
    }

    private SampleCreation sampleCreation(String spaceCode, String code)
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode(code);
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId(spaceCode));
        creation.setCreationId(new CreationId("creation " + code));
        return creation;
    }

    private SamplePermId createCisdSample(String code)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleCreation creation = sampleCreation("CISD", code);
        List<SamplePermId> ids = v3api.createSamples(sessionToken, Collections.singletonList(creation));
        v3api.logout(sessionToken);
        return ids.get(0);
    }

    @DataProvider(name = "tf-ft-tt")
    Object[][] getDataProviderForRelationTest()
    {
        return new Object[][] {
                new Object[] { true, false },
                new Object[] { false, true },
                new Object[] { true, true },
        };
    }

}
