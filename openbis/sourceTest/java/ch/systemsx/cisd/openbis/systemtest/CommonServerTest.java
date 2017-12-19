/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentWithContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelatedEntities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelationshipRole;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityHistory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignmentsCount;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.displaysettings.IDisplaySettingsUpdate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.metaproject.IMetaprojectId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.metaproject.MetaprojectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

import junit.framework.Assert;

/**
 * @author Franz-Josef Elmer
 */
public class CommonServerTest extends SystemTestCase
{

    private Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    @Test
    public void testDeleteGroupWithPersons()
    {
        String groupCode = "AUTHORIZATION_TEST_GROUP";
        String sessionToken = authenticateAs("test");

        // create a group

        NewAuthorizationGroup newGroup = new NewAuthorizationGroup();
        newGroup.setCode(groupCode);
        commonServer.registerAuthorizationGroup(sessionToken, newGroup);
        List<AuthorizationGroup> groups = commonServer.listAuthorizationGroups(sessionToken);
        TechId authorizationGroupTechId = new TechId(findAuthorizationGroup(groups, groupCode).getId());

        // add user to the group
        commonServer.addPersonsToAuthorizationGroup(sessionToken, authorizationGroupTechId, Arrays.asList("test_space", "test_role", "test"));

        commonServer.deleteAuthorizationGroups(sessionToken, Arrays.asList(authorizationGroupTechId), "no reason");
    }

    private AuthorizationGroup findAuthorizationGroup(List<AuthorizationGroup> spaces, final String spaceCode)
    {
        return IterableUtils.find(spaces, new Predicate<AuthorizationGroup>()
            {
                @Override
                public boolean evaluate(AuthorizationGroup object)
                {
                    return object.getCode().equals(spaceCode);
                }

            });
    }

    @Test
    public void testGetSampleWithAssignedPropertyTypesAndProperties()
    {
        Sample sample = commonServer.getSampleInfo(systemSessionToken, new TechId(1)).getParent();

        assertEquals("/CISD/CL1", sample.getIdentifier());
        EntityType entityType = sample.getEntityType();
        assertEquals("CONTROL_LAYOUT", entityType.getCode());
        assertAssignedPropertyTypes("[$PLATE_GEOMETRY*, DESCRIPTION]", entityType);
        assertProperties("[$PLATE_GEOMETRY: 384_WELLS_16X24, DESCRIPTION: test control layout]",
                sample);
    }

    @Test
    public void testGetExperimentWithAssignedPropertyTypesAndProperties()
    {
        Experiment experiment = commonServer.getExperimentInfo(systemSessionToken, new TechId(2));

        assertEquals("/CISD/NEMO/EXP1", experiment.getIdentifier());
        EntityType entityType = experiment.getEntityType();
        assertEquals("SIRNA_HCS", entityType.getCode());
        assertAssignedPropertyTypes("[DESCRIPTION*, GENDER, PURCHASE_DATE]", entityType);
        assertProperties("[DESCRIPTION: A simple experiment, GENDER: MALE]", experiment);
    }

    @Test
    public void testGetExperimentWithAttachments()
    {
        Experiment experiment = commonServer.getExperimentInfo(systemSessionToken, new TechId(2));

        assertEquals("/CISD/NEMO/EXP1", experiment.getIdentifier());
        List<Attachment> attachments = experiment.getAttachments();
        assertEquals("exampleExperiments.txt", attachments.get(0).getFileName());
        assertEquals(4, attachments.size());
    }

    @Test
    public void testGetDataSetWithAssignedPropertyTypesAndProperties()
    {
        AbstractExternalData dataSet = commonServer.getDataSetInfo(systemSessionToken, new TechId(14));

        assertEquals("20110509092359990-11", dataSet.getCode());
        DataSetType dataSetType = dataSet.getDataSetType();
        assertEquals("HCS_IMAGE", dataSetType.getCode());
        assertAssignedPropertyTypes("[ANY_MATERIAL, BACTERIUM, COMMENT*, GENDER]", dataSetType);
        assertEquals("[COMMENT: non-virtual comment]", dataSet.getProperties().toString());
        assertEquals("/CISD/DEFAULT/EXP-REUSE", dataSet.getExperiment().getIdentifier());
    }

    @Test
    public void testGetContainerDataSetWithContainedDataSets()
    {
        AbstractExternalData dataSet = commonServer.getDataSetInfo(systemSessionToken, new TechId(13));

        assertEquals("20110509092359990-10", dataSet.getCode());
        assertEquals(true, dataSet.isContainer());
        ContainerDataSet containerDataSet = dataSet.tryGetAsContainerDataSet();
        List<AbstractExternalData> containedDataSets = containerDataSet.getContainedDataSets();
        assertEntities("[20110509092359990-11, 20110509092359990-12]", containedDataSets);
    }

    @Test
    public void testGetDataSetWithChildrenAndParents()
    {
        AbstractExternalData dataSet = commonServer.getDataSetInfo(systemSessionToken, new TechId(10));

        assertEquals("20081105092259900-0", dataSet.getCode());

        // assertEntities("[20081105092359990-2]", dataSet.getChildren()); //removed as children are no more fetched with this call
        assertEntities("[]", dataSet.getChildren());
        assertEntities("[20081105092259000-9]", new ArrayList<AbstractExternalData>(dataSet.getParents()));
    }

    @Test
    public void testGetDataSetWithSample()
    {
        AbstractExternalData dataSet = commonServer.getDataSetInfo(systemSessionToken, new TechId(5));

        assertEquals("20081105092159111-1", dataSet.getCode());
        assertEquals("/CISD/CP-TEST-1", dataSet.getSampleIdentifier());
    }

    @Test
    public void testGetMaterialInfo()
    {
        Material materialInfo = commonServer.getMaterialInfo(systemSessionToken, new TechId(1));

        assertEquals("AD3", materialInfo.getCode());
        assertEquals(EntityKind.MATERIAL, materialInfo.getEntityKind());
        EntityType entityType = materialInfo.getEntityType();
        assertEquals("VIRUS", entityType.getCode());
        List<? extends EntityTypePropertyType<?>> assignedPropertyTypes = entityType.getAssignedPropertyTypes();
        assertEquals("VIRUS", assignedPropertyTypes.get(0).getEntityType().getCode());
        assertEquals("DESCRIPTION", assignedPropertyTypes.get(0).getPropertyType().getCode());
        assertEquals("VARCHAR", assignedPropertyTypes.get(0).getPropertyType().getDataType().getCode().toString());
        assertEquals(1, assignedPropertyTypes.size());
        assertEquals("[DESCRIPTION: Adenovirus 3]", materialInfo.getProperties().toString());
    }

    @Test
    public void testListMaterialIdsByMaterialProperties()
    {
        Collection<TechId> ids = commonServer.listMaterialIdsByMaterialProperties(systemSessionToken, Arrays.asList(new TechId(3736)));

        assertEquals("[3735]", ids.toString());
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListSamplesForExperimentWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        ListSampleCriteria criteria = ListSampleCriteria.createForExperiment(new TechId(23L)); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<Sample> samples = commonServer.listSamples(session.getSessionToken(), criteria);
            assertEntities(
                    "[/TEST-SPACE/EV-INVALID, /TEST-SPACE/EV-PARENT, /TEST-SPACE/EV-PARENT-NORMAL, /TEST-SPACE/EV-TEST, /TEST-SPACE/FV-TEST, /TEST-SPACE/SAMPLE-TO-DELETE]",
                    samples);
        } else
        {
            try
            {
                commonServer.listSamples(session.getSessionToken(), criteria);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListSamplesForSpaceWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        ListSampleCriteria criteria = new ListSampleCriteria();
        criteria.setIncludeSpace(true);

        if (user.isDisabledProjectUser())
        {
            try
            {
                commonServer.listSamples(session.getSessionToken(), criteria);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        } else
        {
            List<Sample> samples = commonServer.listSamples(session.getSessionToken(), criteria);

            if (user.isInstanceUser())
            {
                assertEquals(samples.size(), 38);
            } else if (user.isTestSpaceUser())
            {
                assertEquals(samples.size(), 7);
                assertEntities(
                        "[/TEST-SPACE/CP-TEST-4, /TEST-SPACE/EV-INVALID, /TEST-SPACE/EV-PARENT, /TEST-SPACE/EV-PARENT-NORMAL, /TEST-SPACE/EV-TEST, /TEST-SPACE/FV-TEST, /TEST-SPACE/SAMPLE-TO-DELETE]",
                        samples);
            } else if (user.isTestProjectUser())
            {
                assertEquals(samples.size(), 6);
                assertEntities(
                        "[/TEST-SPACE/EV-INVALID, /TEST-SPACE/EV-PARENT, /TEST-SPACE/EV-PARENT-NORMAL, /TEST-SPACE/EV-TEST, /TEST-SPACE/FV-TEST, /TEST-SPACE/SAMPLE-TO-DELETE]",
                        samples);
            } else
            {
                assertEntities("[]", samples);
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListSamplesOnBehalfOfUserForExperimentWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(TEST_USER, PASSWORD);

        ListSampleCriteria criteria = ListSampleCriteria.createForExperiment(new TechId(23L)); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST
        List<Sample> samples = commonServer.listSamplesOnBehalfOfUser(session.getSessionToken(), criteria, user.getUserId());

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            assertEntities(
                    "[/TEST-SPACE/EV-INVALID, /TEST-SPACE/EV-PARENT, /TEST-SPACE/EV-PARENT-NORMAL, /TEST-SPACE/EV-TEST, /TEST-SPACE/FV-TEST, /TEST-SPACE/SAMPLE-TO-DELETE]",
                    samples);
        } else
        {
            assertEntities("[]", samples);
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListSamplesOnBehalfOfForSpaceWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(TEST_USER, PASSWORD);

        ListSampleCriteria criteria = new ListSampleCriteria();
        criteria.setIncludeSpace(true);

        List<Sample> samples = commonServer.listSamplesOnBehalfOfUser(session.getSessionToken(), criteria, user.getUserId());

        if (user.isInstanceUser())
        {
            assertEquals(samples.size(), 38);
        } else if (user.isTestSpaceUser())
        {
            assertEquals(samples.size(), 7);
            assertEntities(
                    "[/TEST-SPACE/CP-TEST-4, /TEST-SPACE/EV-INVALID, /TEST-SPACE/EV-PARENT, /TEST-SPACE/EV-PARENT-NORMAL, /TEST-SPACE/EV-TEST, /TEST-SPACE/FV-TEST, /TEST-SPACE/SAMPLE-TO-DELETE]",
                    samples);
        } else if (user.isTestProjectUser() && user.hasPAEnabled())
        {
            assertEquals(samples.size(), 6);
            assertEntities(
                    "[/TEST-SPACE/EV-INVALID, /TEST-SPACE/EV-PARENT, /TEST-SPACE/EV-PARENT-NORMAL, /TEST-SPACE/EV-TEST, /TEST-SPACE/FV-TEST, /TEST-SPACE/SAMPLE-TO-DELETE]",
                    samples);
        } else
        {
            assertEntities("[]", samples);
        }
    }

    @Test
    public void testListSamplesByMaterialProperties()
    {
        List<TechId> materialIds = Arrays.asList(new TechId(34));
        List<Sample> samples = commonServer.listSamplesByMaterialProperties(systemSessionToken, materialIds);

        assertEntities("[/CISD/CP-TEST-1, /CISD/PLATE_WELLSEARCH:WELL-A01, /TEST-SPACE/FV-TEST]", samples);

        String observerSessionToken = commonServer.tryAuthenticate("observer", "a").getSessionToken();
        samples = commonServer.listSamplesByMaterialProperties(observerSessionToken, materialIds);

        assertEntities("[]", samples);

        // delete a sample
        commonServer.deleteSamples(systemSessionToken, Arrays.asList(new TechId(1051)), "test", DeletionType.TRASH);
        samples = commonServer.listSamplesByMaterialProperties(systemSessionToken, materialIds);

        assertEntities("[/CISD/CP-TEST-1, /TEST-SPACE/FV-TEST]", samples);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListSamplesByMaterialPropertiesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        List<TechId> materialIds = Arrays.asList(new TechId(34L)); // BACTERIUM-X

        if (user.isDisabledProjectUser())
        {
            try
            {
                commonServer.listSamplesByMaterialProperties(session.getSessionToken(), materialIds);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        } else
        {
            List<Sample> samples = commonServer.listSamplesByMaterialProperties(session.getSessionToken(), materialIds);

            if (user.isInstanceUser())
            {
                assertEntities("[/CISD/CP-TEST-1, /CISD/PLATE_WELLSEARCH:WELL-A01, /TEST-SPACE/FV-TEST]", samples);
            } else if (user.isTestSpaceUser() || user.isTestProjectUser())
            {
                assertEntities("[/TEST-SPACE/FV-TEST]", samples);
            } else
            {
                assertEntities("[]", samples);
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testDeleteSamplesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId sampleId = new TechId(1055L); // /TEST-SPACE/EV-TEST

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            commonServer.deleteSamples(session.getSessionToken(), Arrays.asList(sampleId), "test reason", DeletionType.TRASH);
            try
            {
                commonServer.getSampleInfo(session.getSessionToken(), sampleId);
                fail();
            } catch (UserFailureException e)
            {
                assertEquals(e.getMessage(), "Sample with ID '1055' does not exist.");
            }
        } else
        {
            try
            {
                commonServer.deleteSamples(session.getSessionToken(), Arrays.asList(sampleId), "test reason", DeletionType.TRASH);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testDeleteSampleAttachmentsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId sampleId = new TechId(1054L); // /TEST-SPACE/FV-TEST

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<Attachment> attachments = commonServer.listSampleAttachments(session.getSessionToken(), sampleId);
            assertEquals(attachments.size(), 1);

            commonServer.deleteSampleAttachments(session.getSessionToken(), sampleId, Arrays.asList("testSample.txt"), "test reason");

            attachments = commonServer.listSampleAttachments(session.getSessionToken(), sampleId);
            assertEquals(attachments.size(), 0);
        } else
        {
            try
            {
                commonServer.deleteSampleAttachments(session.getSessionToken(), sampleId, Arrays.asList("testSample.txt"), "test reason");
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListSampleAttachmentsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId sampleId = new TechId(1054L); // /TEST-SPACE/FV-TEST

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<Attachment> attachments = commonServer.listSampleAttachments(session.getSessionToken(), sampleId);
            assertEquals(attachments.size(), 1);
        } else
        {
            try
            {
                commonServer.listSampleAttachments(session.getSessionToken(), sampleId);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testUpdateSampleAttachmentsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId sampleId = new TechId(1054L); // /TEST-SPACE/FV-TEST

        Attachment attachment = new Attachment();
        attachment.setFileName("testSample.txt");
        attachment.setVersion(1);
        attachment.setDescription("Updated sample description");

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<Attachment> attachments = commonServer.listSampleAttachments(session.getSessionToken(), sampleId);
            assertEquals(attachments.get(0).getDescription(), "Test sample description");

            commonServer.updateSampleAttachments(session.getSessionToken(), sampleId, attachment);

            attachments = commonServer.listSampleAttachments(session.getSessionToken(), sampleId);
            assertEquals(attachments.get(0).getDescription(), "Updated sample description");
        } else
        {
            try
            {
                commonServer.updateSampleAttachments(session.getSessionToken(), sampleId, attachment);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testAddSampleAttachmentsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId sampleId = new TechId(1054L); // /TEST-SPACE/FV-TEST

        NewAttachment attachment = new NewAttachment();
        attachment.setFilePath("testSample2.txt");
        attachment.setContent("testContent2".getBytes());

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<Attachment> attachments = commonServer.listSampleAttachments(session.getSessionToken(), sampleId);
            assertEquals(attachments.size(), 1);

            commonServer.addSampleAttachments(session.getSessionToken(), sampleId, attachment);

            attachments = commonServer.listSampleAttachments(session.getSessionToken(), sampleId);
            assertEquals(attachments.size(), 2);
        } else
        {
            try
            {
                commonServer.addSampleAttachments(session.getSessionToken(), sampleId, attachment);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetProjectInfoByTechIdWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId projectId = new TechId(5L); // /TEST-SPACE/TEST-PROJECT

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            Project project = commonServer.getProjectInfo(session.getSessionToken(), projectId);
            assertNotNull(project);
            assertEquals(project.getIdentifier(), "/TEST-SPACE/TEST-PROJECT");
        } else
        {
            try
            {
                commonServer.getProjectInfo(session.getSessionToken(), projectId);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetProjectInfoByIdentifierWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        ProjectIdentifier projectIdentifier = new ProjectIdentifier("TEST-SPACE", "TEST-PROJECT");

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            Project project = commonServer.getProjectInfo(session.getSessionToken(), projectIdentifier);
            assertNotNull(project);
            assertEquals(project.getIdentifier(), "/TEST-SPACE/TEST-PROJECT");
        } else
        {
            try
            {
                commonServer.getProjectInfo(session.getSessionToken(), projectIdentifier);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetProjectIdHolderWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        String projectPermId = "20120814110011738-105"; // /TEST-SPACE/TEST-PROJECT

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            IIdHolder project = commonServer.getProjectIdHolder(session.getSessionToken(), projectPermId);
            assertNotNull(project);
            assertEquals(project.getId(), Long.valueOf(5));
        } else
        {
            try
            {
                commonServer.getProjectIdHolder(session.getSessionToken(), projectPermId);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testUpdateProjectWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        ProjectUpdatesDTO updates = new ProjectUpdatesDTO();
        updates.setTechId(new TechId(5L)); // /TEST-SPACE/TEST-PROJECT
        updates.setAttachments(Collections.<NewAttachment> emptyList());
        updates.setDescription(String.valueOf(System.currentTimeMillis()));

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            commonServer.updateProject(session.getSessionToken(), updates);
            Project info = commonServer.getProjectInfo(session.getSessionToken(), new ProjectIdentifier("TEST-SPACE", "TEST-PROJECT"));
            assertEquals(info.getDescription(), updates.getDescription());
        } else
        {
            try
            {
                commonServer.updateProject(session.getSessionToken(), updates);
                Assert.fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testUpdateProjectAttachmentsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId projectId = new TechId(5L); // /TEST-SPACE/TEST-PROJECT

        Attachment attachment = new Attachment();
        attachment.setFileName("testProject.txt");
        attachment.setTitle("new title");
        attachment.setVersion(1);

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            commonServer.updateProjectAttachments(session.getSessionToken(), projectId, attachment);

            List<Attachment> attachments = commonServer.listProjectAttachments(session.getSessionToken(), projectId);
            assertEquals(attachments.size(), 1);
            assertEquals(attachments.get(0).getTitle(), attachment.getTitle());
        } else
        {
            try
            {
                commonServer.updateProjectAttachments(session.getSessionToken(), projectId, attachment);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testDeleteProjectAttachmentsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO testSession = commonServer.tryAuthenticate(TEST_USER, PASSWORD);
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId projectId = new TechId(5L); // /TEST-SPACE/TEST-PROJECT
        String fileName = "testProject.txt";
        String reason = "testReason";
        Integer version = 1;

        AttachmentWithContent before = genericServer.getProjectFileAttachment(testSession.getSessionToken(), projectId, fileName, version);

        assertNotNull(before);

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            commonServer.deleteProjectAttachments(session.getSessionToken(), projectId, Arrays.asList(fileName), reason);

            try
            {
                genericServer.getProjectFileAttachment(testSession.getSessionToken(), projectId, fileName, version);
                fail();
            } catch (UserFailureException e)
            {
                assertEquals(e.getMessage(),
                        "Attachment 'testProject.txt' (version '1') not found in project '/TEST-SPACE/TEST-PROJECT'.");
            }
        } else
        {
            try
            {
                commonServer.deleteProjectAttachments(session.getSessionToken(), projectId, Arrays.asList(fileName), reason);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testDeleteProjectsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO testSession = commonServer.tryAuthenticate(TEST_USER, PASSWORD);
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);
        TechId projectId = new TechId(7L); // /TEST-SPACE/PROJECT-TO-DELETE

        Project info = commonServer.getProjectInfo(testSession.getSessionToken(), projectId);
        assertNotNull(info);

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            commonServer.deleteProjects(session.getSessionToken(), Arrays.asList(projectId), "testReason");

            try
            {
                commonServer.getProjectInfo(testSession.getSessionToken(), projectId);
            } catch (UserFailureException e)
            {
                assertEquals(e.getMessage(), "Project with ID 7 does not exist. Maybe someone has just deleted it.");
            }
        } else
        {
            try
            {
                commonServer.deleteProjects(session.getSessionToken(), Arrays.asList(projectId), "testReason");
                Assert.fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListProjectsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        if (user.isDisabledProjectUser())
        {
            try
            {
                commonServer.listProjects(session.getSessionToken());
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        } else
        {
            List<Project> projects = commonServer.listProjects(session.getSessionToken());

            if (user.isInstanceUser())
            {
                assertEntities(
                        "[/CISD/DEFAULT, /CISD/NEMO, /CISD/NOE, /TEST-SPACE/NOE, /TEST-SPACE/PROJECT-TO-DELETE, /TEST-SPACE/TEST-PROJECT, /TESTGROUP/TESTPROJ]",
                        projects);
            } else if (user.isTestSpaceUser())
            {
                assertEntities("[/TEST-SPACE/NOE, /TEST-SPACE/PROJECT-TO-DELETE, /TEST-SPACE/TEST-PROJECT]", projects);
            } else if (user.isTestGroupUser())
            {
                assertEntities("[/TESTGROUP/TESTPROJ]", projects);
            } else if (user.isTestProjectUser())
            {
                assertEntities("[/TEST-SPACE/PROJECT-TO-DELETE, /TEST-SPACE/TEST-PROJECT]", projects);
            } else
            {
                assertEntities("[]", projects);
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListProjectAttachmentsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId projectId = new TechId(5L); // /TEST-SPACE/TEST-PROJECT

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<Attachment> attachments = commonServer.listProjectAttachments(session.getSessionToken(), projectId);
            assertEquals(attachments.size(), 1);
            assertEquals(attachments.get(0).getFileName(), "testProject.txt");
        } else
        {
            try
            {
                commonServer.listProjectAttachments(session.getSessionToken(), projectId);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testRegisterProjectWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        ProjectIdentifier projectIdentifier = new ProjectIdentifier("TEST-SPACE", "PA_TEST");

        if (user.isInstanceUser() || user.isTestSpaceUser())
        {
            commonServer.registerProject(session.getSessionToken(), projectIdentifier, "testDescription", user.getUserId(),
                    Arrays.<NewAttachment> asList());

            Project projectInfo = commonServer.getProjectInfo(session.getSessionToken(), projectIdentifier);
            assertEquals(projectInfo.getIdentifier(), projectIdentifier.toString());
        } else
        {
            try
            {
                commonServer.registerProject(session.getSessionToken(), projectIdentifier, "testDescription", user.getUserId(),
                        Arrays.<NewAttachment> asList());
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testAddProjectAttachmentsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId projectId = new TechId(5L); // /TEST-SPACE/TEST-PROJECT

        NewAttachment attachment = new NewAttachment();
        attachment.setFilePath("testProject2.txt");
        attachment.setContent("testContent2".getBytes());

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            commonServer.addProjectAttachments(session.getSessionToken(), projectId, attachment);

            List<Attachment> attachments = commonServer.listProjectAttachments(session.getSessionToken(), projectId);
            assertEquals(attachments.size(), 2);
            assertEquals(attachments.get(0).getFileName(), "testProject.txt");
            assertEquals(attachments.get(1).getFileName(), "testProject2.txt");
        } else
        {
            try
            {
                commonServer.addProjectAttachments(session.getSessionToken(), projectId, attachment);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testDeleteExperimentsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO testSession = commonServer.tryAuthenticate(TEST_USER, PASSWORD);
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        IEntityProperty property = new EntityProperty();
        property.setValue("test description");
        PropertyType propertyType = new PropertyType();
        propertyType.setCode("DESCRIPTION");
        property.setPropertyType(propertyType);

        NewExperiment newExperiment = new NewExperiment("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST-2", "SIRNA_HCS");
        newExperiment.setProperties(new IEntityProperty[] { property });

        Experiment experiment = genericServer.registerExperiment(testSession.getSessionToken(), newExperiment, Collections.emptyList());

        Experiment before =
                commonServer.getExperimentInfo(testSession.getSessionToken(),
                        new ExperimentIdentifier("TEST-SPACE", "TEST-PROJECT", "EXP-SPACE-TEST-2"));

        assertNotNull(before);

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            commonServer.deleteExperiments(session.getSessionToken(), Arrays.asList(new TechId(experiment.getId())), "testReason",
                    DeletionType.TRASH);

            try
            {
                commonServer.getExperimentInfo(testSession.getSessionToken(),
                        new ExperimentIdentifier("TEST-SPACE", "TEST-PROJECT", "EXP-SPACE-TEST-2"));
                fail();
            } catch (UserFailureException e)
            {
                assertEquals(e.getMessage(), "Unkown experiment: /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST-2");
            }
        } else
        {
            try
            {
                commonServer.deleteExperiments(session.getSessionToken(), Arrays.asList(new TechId(experiment.getId())), "testReason",
                        DeletionType.TRASH);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testDeleteExperimentAttachmentsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO testSession = commonServer.tryAuthenticate(TEST_USER, PASSWORD);
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId experimentId = new TechId(23L); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST
        String fileName = "testExperiment.txt";
        String reason = "testReason";
        Integer version = 1;

        AttachmentWithContent before = genericServer.getExperimentFileAttachment(testSession.getSessionToken(), experimentId, fileName, version);

        assertNotNull(before);

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            commonServer.deleteExperimentAttachments(session.getSessionToken(), experimentId, Arrays.asList(fileName), reason);

            try
            {
                genericServer.getExperimentFileAttachment(testSession.getSessionToken(), experimentId, fileName, version);
                fail();
            } catch (UserFailureException e)
            {
                assertEquals(e.getMessage(),
                        "Attachment 'testExperiment.txt' (version '1') not found in experiment '/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST'.");
            }
        } else
        {
            try
            {
                commonServer.deleteExperimentAttachments(session.getSessionToken(), experimentId, Arrays.asList(fileName), reason);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListExperimentsByExperimentTypeAndSpaceIdentifierWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        ExperimentType experimentType = new ExperimentType();
        experimentType.setCode("SIRNA_HCS");

        if (user.isDisabledProjectUser())
        {
            try
            {
                commonServer.listExperiments(session.getSessionToken(), experimentType, new SpaceIdentifier("TEST-SPACE"));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        } else
        {
            List<Experiment> experiments = commonServer.listExperiments(session.getSessionToken(), experimentType, new SpaceIdentifier("TEST-SPACE"));

            if (user.isInstanceUser() || user.isTestSpaceUser())
            {
                assertEquals(experiments.size(), 3);
                assertEntities("[/TEST-SPACE/NOE/EXP-TEST-2, /TEST-SPACE/NOE/EXPERIMENT-TO-DELETE, /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST]",
                        experiments);
            } else if (user.isTestProjectUser())
            {
                assertEquals(experiments.size(), 1);
                assertEntities("[/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST]", experiments);
            } else
            {
                assertEquals(experiments.size(), 0);
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListExperimentsByExperimentTypeAndProjectIdentifierWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        ExperimentType experimentType = new ExperimentType();
        experimentType.setCode("SIRNA_HCS");

        ProjectIdentifier projectIdentifier = new ProjectIdentifier("TEST-SPACE", "TEST-PROJECT");

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<Experiment> experiments = commonServer.listExperiments(session.getSessionToken(), experimentType, projectIdentifier);
            assertEquals(experiments.size(), 1);
            assertEquals(experiments.get(0).getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        } else
        {
            try
            {
                commonServer.listExperiments(session.getSessionToken(), experimentType, projectIdentifier);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListExperimentsByExperimentTypeAndProjectIdentifiersWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        ExperimentType experimentType = new ExperimentType();
        experimentType.setCode("SIRNA_HCS");

        ProjectIdentifier projectIdentifier = new ProjectIdentifier("TEST-SPACE", "TEST-PROJECT");

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<Experiment> experiments = commonServer.listExperiments(session.getSessionToken(), experimentType, Arrays.asList(projectIdentifier));
            assertEquals(experiments.size(), 1);
            assertEquals(experiments.get(0).getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        } else
        {
            try
            {
                commonServer.listExperiments(session.getSessionToken(), experimentType, Arrays.asList(projectIdentifier));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListExperimentsByExperimentIdentifiersWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        ExperimentIdentifier experimentIdentifier = new ExperimentIdentifier("TEST-SPACE", "TEST-PROJECT", "EXP-SPACE-TEST");

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<Experiment> experiments = commonServer.listExperiments(session.getSessionToken(), Arrays.asList(experimentIdentifier));
            assertEquals(experiments.size(), 1);
            assertEquals(experiments.get(0).getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        } else
        {
            try
            {
                commonServer.listExperiments(session.getSessionToken(), Arrays.asList(experimentIdentifier));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListExperimentsHavingSamplesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        ExperimentType experimentType = new ExperimentType();
        experimentType.setCode("SIRNA_HCS");

        ProjectIdentifier projectIdentifier = new ProjectIdentifier("TEST-SPACE", "TEST-PROJECT");

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<Experiment> experiments =
                    commonServer.listExperimentsHavingSamples(session.getSessionToken(), experimentType, Arrays.asList(projectIdentifier));
            assertEquals(experiments.size(), 1);
            assertEquals(experiments.get(0).getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        } else
        {
            try
            {
                commonServer.listExperimentsHavingSamples(session.getSessionToken(), experimentType, Arrays.asList(projectIdentifier));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListExperimentsHavingDataSetsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        ExperimentType experimentType = new ExperimentType();
        experimentType.setCode("SIRNA_HCS");

        ProjectIdentifier projectIdentifier = new ProjectIdentifier("TEST-SPACE", "TEST-PROJECT");

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<Experiment> experiments =
                    commonServer.listExperimentsHavingDataSets(session.getSessionToken(), experimentType, Arrays.asList(projectIdentifier));
            assertEquals(experiments.size(), 1);
            assertEquals(experiments.get(0).getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        } else
        {
            try
            {
                commonServer.listExperimentsHavingDataSets(session.getSessionToken(), experimentType, Arrays.asList(projectIdentifier));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListMetaprojectExperimentsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        PersonPE person = daoFactory.getPersonDAO().tryFindPersonByUserId(user.getUserId());
        MetaprojectPE metaproject = new MetaprojectPE();
        metaproject.setName("TEST_LIST_METAPROJECT_EXPERIMENTS");
        metaproject.setOwner(person);

        ExperimentPE experiment = daoFactory.getExperimentDAO().tryGetByTechId(new TechId(23L));
        experiment.addMetaproject(metaproject);

        daoFactory.getMetaprojectDAO().createOrUpdateMetaproject(metaproject, person);

        if (user.isDisabledProjectUser())
        {
            try
            {
                commonServer.listMetaprojectExperiments(session.getSessionToken(), new MetaprojectIdentifierId(metaproject.getIdentifier()));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        } else
        {
            List<Experiment> experiments =
                    commonServer.listMetaprojectExperiments(session.getSessionToken(), new MetaprojectIdentifierId(metaproject.getIdentifier()));

            if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
            {
                assertEquals(experiments.size(), 1);
                assertEquals(experiments.get(0).isStub(), false);
                assertEquals(experiments.get(0).getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
            } else
            {
                assertEquals(experiments.size(), 1);
                assertEquals(experiments.get(0).isStub(), true);
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListMetaprojectSamplesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        PersonPE person = daoFactory.getPersonDAO().tryFindPersonByUserId(user.getUserId());
        MetaprojectPE metaproject = new MetaprojectPE();
        metaproject.setName("TEST_LIST_METAPROJECT_SAMPLES");
        metaproject.setOwner(person);

        SamplePE sample = daoFactory.getSampleDAO().tryGetByTechId(new TechId(1055L)); // /TEST-SPACE/EV-TEST
        sample.addMetaproject(metaproject);

        daoFactory.getMetaprojectDAO().createOrUpdateMetaproject(metaproject, person);

        if (user.isDisabledProjectUser())
        {
            try
            {
                commonServer.listMetaprojectSamples(session.getSessionToken(), new MetaprojectIdentifierId(metaproject.getIdentifier()));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        } else
        {
            List<Sample> samples =
                    commonServer.listMetaprojectSamples(session.getSessionToken(), new MetaprojectIdentifierId(metaproject.getIdentifier()));

            if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
            {
                assertEquals(samples.size(), 1);
                assertEquals(samples.get(0).isStub(), false);
                assertEquals(samples.get(0).getIdentifier(), "/TEST-SPACE/EV-TEST");
            } else
            {
                assertEquals(samples.size(), 1);
                assertEquals(samples.get(0).isStub(), true);
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListMetaprojectExternalDataWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        PersonPE person = daoFactory.getPersonDAO().tryFindPersonByUserId(user.getUserId());
        MetaprojectPE metaproject = new MetaprojectPE();
        metaproject.setName("TEST_LIST_METAPROJECT_DATASETS");
        metaproject.setOwner(person);

        DataPE dataSet = daoFactory.getDataDAO().tryToFindDataSetByCode("20120628092259000-41");
        dataSet.addMetaproject(metaproject);

        daoFactory.getMetaprojectDAO().createOrUpdateMetaproject(metaproject, person);

        if (user.isDisabledProjectUser())
        {
            try
            {
                commonServer.listMetaprojectExternalData(session.getSessionToken(), new MetaprojectIdentifierId(metaproject.getIdentifier()));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        } else
        {
            List<AbstractExternalData> dataSets =
                    commonServer.listMetaprojectExternalData(session.getSessionToken(), new MetaprojectIdentifierId(metaproject.getIdentifier()));

            if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
            {
                assertEquals(dataSets.size(), 1);
                assertEquals(dataSets.get(0).isStub(), false);
                assertEquals(dataSets.get(0).getCode(), "20120628092259000-41");
            } else
            {
                assertEquals(dataSets.size(), 1);
                assertEquals(dataSets.get(0).isStub(), true);
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testSearchForSamplesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        DetailedSearchCriterion spaceCriterion = new DetailedSearchCriterion();
        spaceCriterion.setField(DetailedSearchField.createAttributeField(SampleAttributeSearchFieldKind.SPACE));
        spaceCriterion.setValue("TEST-SPACE");

        DetailedSearchCriterion typeCriterion = new DetailedSearchCriterion();
        typeCriterion.setField(DetailedSearchField.createAttributeField(SampleAttributeSearchFieldKind.SAMPLE_TYPE));
        typeCriterion.setValue("CELL_PLATE");

        DetailedSearchCriteria criteria = new DetailedSearchCriteria();
        criteria.setCriteria(Arrays.asList(spaceCriterion, typeCriterion));
        criteria.setConnection(SearchCriteriaConnection.MATCH_ALL);

        if (user.isDisabledProjectUser())
        {
            try
            {
                commonServer.searchForSamples(session.getSessionToken(), criteria);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        } else
        {
            List<Sample> samples = commonServer.searchForSamples(session.getSessionToken(), criteria);

            if (user.isInstanceUser() || user.isTestSpaceUser())
            {
                assertEntities("[/TEST-SPACE/CP-TEST-4, /TEST-SPACE/FV-TEST]", samples);
            } else if (user.isTestProjectUser())
            {
                assertEquals(samples.size(), 1);
                assertEntities("[/TEST-SPACE/FV-TEST]", samples);
            } else
            {
                assertEquals(samples.size(), 0);
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testSearchForExperimentsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        DetailedSearchCriterion criterion = new DetailedSearchCriterion();
        criterion.setField(DetailedSearchField.createAttributeField(ExperimentAttributeSearchFieldKind.PROJECT));
        criterion.setValue("TEST-PROJECT");

        DetailedSearchCriteria criteria = new DetailedSearchCriteria();
        criteria.setCriteria(Arrays.asList(criterion));
        criteria.setConnection(SearchCriteriaConnection.MATCH_ANY);

        if (user.isDisabledProjectUser())
        {
            try
            {
                commonServer.searchForExperiments(session.getSessionToken(), criteria);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        } else
        {
            List<Experiment> experiments = commonServer.searchForExperiments(session.getSessionToken(), criteria);

            if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
            {
                assertEquals(experiments.size(), 1);
                assertEquals(experiments.get(0).getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
            } else
            {
                assertEquals(experiments.size(), 0);
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testSearchForDataSetsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        DetailedSearchCriterion criterion = new DetailedSearchCriterion();
        criterion.setField(DetailedSearchField.createAttributeField(ExperimentAttributeSearchFieldKind.CODE));
        criterion.setValue("20120628092259000-41");

        DetailedSearchCriteria criteria = new DetailedSearchCriteria();
        criteria.setCriteria(Arrays.asList(criterion));
        criteria.setConnection(SearchCriteriaConnection.MATCH_ANY);

        if (user.isDisabledProjectUser())
        {
            try
            {
                commonServer.searchForDataSets(session.getSessionToken(), criteria);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        } else
        {
            List<AbstractExternalData> dataSets = commonServer.searchForDataSets(session.getSessionToken(), criteria);

            if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
            {
                assertEquals(dataSets.size(), 1);
                assertEquals(dataSets.get(0).getCode(), "20120628092259000-41");
            } else
            {
                assertEquals(dataSets.size(), 0);
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testSearchForDataSetsOnBehalfOfUserWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(TEST_USER, PASSWORD);

        DetailedSearchCriterion criterion = new DetailedSearchCriterion();
        criterion.setField(DetailedSearchField.createAttributeField(ExperimentAttributeSearchFieldKind.CODE));
        criterion.setValue("20120628092259000-41");

        DetailedSearchCriteria criteria = new DetailedSearchCriteria();
        criteria.setCriteria(Arrays.asList(criterion));
        criteria.setConnection(SearchCriteriaConnection.MATCH_ANY);

        List<AbstractExternalData> dataSets =
                commonServer.searchForDataSetsOnBehalfOfUser(session.getSessionToken(), criteria, user.getUserId());

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            assertEquals(dataSets.size(), 1);
            assertEquals(dataSets.get(0).getCode(), "20120628092259000-41");
        } else
        {
            assertEquals(dataSets.size(), 0);
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testUpdateExperimentAttachmentsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId experimentId = new TechId(23L); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST

        Attachment attachment = new Attachment();
        attachment.setFileName("testExperiment.txt");
        attachment.setTitle("new title");
        attachment.setVersion(1);

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            commonServer.updateExperimentAttachments(session.getSessionToken(), experimentId, attachment);

            List<Attachment> attachments = commonServer.listExperimentAttachments(session.getSessionToken(), experimentId);
            assertEquals(attachments.size(), 1);
            assertEquals(attachments.get(0).getTitle(), attachment.getTitle());
        } else
        {
            try
            {
                commonServer.updateExperimentAttachments(session.getSessionToken(), experimentId, attachment);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testAddExperimentAttachmentWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId experimentId = new TechId(23L); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST

        NewAttachment attachment = new NewAttachment();
        attachment.setFilePath("testExperiment2.txt");
        attachment.setContent("testContent2".getBytes());

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            commonServer.addExperimentAttachment(session.getSessionToken(), experimentId, attachment);

            List<Attachment> attachments = commonServer.listExperimentAttachments(session.getSessionToken(), experimentId);
            assertEquals(attachments.size(), 2);
            assertEquals(attachments.get(0).getFileName(), "testExperiment.txt");
            assertEquals(attachments.get(1).getFileName(), "testExperiment2.txt");
        } else
        {
            try
            {
                commonServer.addExperimentAttachment(session.getSessionToken(), experimentId, attachment);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListExperimentAttachmentsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId experimentId = new TechId(23L); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<Attachment> attachments = commonServer.listExperimentAttachments(session.getSessionToken(), experimentId);
            assertEquals(attachments.size(), 1);
            assertEquals(attachments.get(0).getFileName(), "testExperiment.txt");
        } else
        {
            try
            {
                commonServer.listExperimentAttachments(session.getSessionToken(), experimentId);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetExperimentInfoByTechIdWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId experimentId = new TechId(23L); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            Experiment experiment = commonServer.getExperimentInfo(session.getSessionToken(), experimentId);
            assertNotNull(experiment);
            assertEquals(experiment.getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        } else
        {
            try
            {
                commonServer.getExperimentInfo(session.getSessionToken(), experimentId);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetSampleInfoByTechIdWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId sampleId = new TechId(1054L); // /TEST-SPACE/FV-TEST

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            SampleParentWithDerived sample = commonServer.getSampleInfo(session.getSessionToken(), sampleId);
            assertNotNull(sample);
            assertEquals(sample.getParent().getIdentifier(), "/TEST-SPACE/FV-TEST");
        } else
        {
            try
            {
                commonServer.getSampleInfo(session.getSessionToken(), sampleId);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetExperimentInfoByIdentifierWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        ExperimentIdentifier experimentIdentifier = new ExperimentIdentifier("TEST-SPACE", "TEST-PROJECT", "EXP-SPACE-TEST");

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            Experiment experiment = commonServer.getExperimentInfo(session.getSessionToken(), experimentIdentifier);
            assertNotNull(experiment);
            assertEquals(experiment.getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        } else
        {
            try
            {
                commonServer.getExperimentInfo(session.getSessionToken(), experimentIdentifier);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testUpdateExperimentWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        IEntityProperty property = new EntityProperty();
        property.setValue("test description");
        PropertyType propertyType = new PropertyType();
        propertyType.setCode("DESCRIPTION");
        property.setPropertyType(propertyType);

        ExperimentUpdatesDTO updates = new ExperimentUpdatesDTO();
        updates.setExperimentId(new TechId(23L)); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST
        updates.setProperties(Arrays.asList(new IEntityProperty[] { property }));
        updates.setAttachments(new ArrayList<NewAttachment>());

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            ExperimentUpdateResult result = commonServer.updateExperiment(session.getSessionToken(), updates);
            assertNotNull(result);

            Experiment experiment = commonServer.getExperimentInfo(session.getSessionToken(), updates.getExperimentId());
            assertEquals(experiment.getProperties().get(0).getValue(), property.getValue());
        } else
        {
            try
            {
                commonServer.updateExperiment(session.getSessionToken(), updates);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testUpdateSampleWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        IEntityProperty property = new EntityProperty();
        property.setValue("test comment");
        PropertyType propertyType = new PropertyType();
        propertyType.setCode("COMMENT");
        property.setPropertyType(propertyType);

        SampleUpdatesDTO updates = new SampleUpdatesDTO(new TechId(1055L), Arrays.asList(new IEntityProperty[] { property }), null, null,
                new ArrayList<NewAttachment>(), 0, null, null, null); // /TEST-SPACE/EV-TEST

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            SampleUpdateResult result = commonServer.updateSample(session.getSessionToken(), updates);
            assertNotNull(result);

            SampleParentWithDerived sample = commonServer.getSampleInfo(session.getSessionToken(), updates.getSampleIdOrNull());
            assertEquals(sample.getParent().getProperties().get(0).getValue(), property.getValue());
        } else
        {
            try
            {
                commonServer.updateSample(session.getSessionToken(), updates);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testUpdateDataSetWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        IEntityProperty property = new EntityProperty();
        property.setValue("test comment");
        PropertyType propertyType = new PropertyType();
        propertyType.setCode("COMMENT");
        property.setPropertyType(propertyType);

        DataSetUpdatesDTO updates = new DataSetUpdatesDTO();
        updates.setDatasetId(new TechId(22L)); // 20120619092259000-22
        updates.setProperties(Arrays.asList(new IEntityProperty[] { property }));
        updates.setFileFormatTypeCode("XML");

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            DataSetUpdateResult result = commonServer.updateDataSet(session.getSessionToken(), updates);
            assertNotNull(result);

            AbstractExternalData dataSet = commonServer.getDataSetInfo(session.getSessionToken(), updates.getDatasetId());
            assertEquals(dataSet.getProperties().get(0).getValue(), property.getValue());
        } else
        {
            try
            {
                commonServer.updateDataSet(session.getSessionToken(), updates);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testUpdateExperimentPropertiesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        PropertyUpdates property = new PropertyUpdates();
        property.setPropertyCode("DESCRIPTION");
        property.setValue("test description");

        TechId experimentId = new TechId(23L); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            commonServer.updateExperimentProperties(session.getSessionToken(), experimentId, Arrays.asList(property));

            Experiment experiment = commonServer.getExperimentInfo(session.getSessionToken(), experimentId);
            assertEquals(experiment.getProperties().get(0).getValue(), property.getValue());
        } else
        {
            try
            {
                commonServer.updateExperimentProperties(session.getSessionToken(), experimentId, Arrays.asList(property));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testUpdateSamplePropertiesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        PropertyUpdates property = new PropertyUpdates();
        property.setPropertyCode("COMMENT");
        property.setValue("test comment");

        TechId sampleId = new TechId(1054L); // /TEST-SPACE/FV-TEST

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            SampleParentWithDerived sample = commonServer.getSampleInfo(session.getSessionToken(), sampleId);
            assertEquals(sample.getParent().getProperties().size(), 1);

            commonServer.updateSampleProperties(session.getSessionToken(), sampleId, Arrays.asList(property));

            sample = commonServer.getSampleInfo(session.getSessionToken(), sampleId);
            assertEquals(sample.getParent().getProperties().size(), 2);
        } else
        {
            try
            {
                commonServer.updateSampleProperties(session.getSessionToken(), sampleId, Arrays.asList(property));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testUpdateDataSetPropertiesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        PropertyUpdates property = new PropertyUpdates();
        property.setPropertyCode("COMMENT");
        property.setValue("test comment");

        TechId dataSetId = new TechId(22L); // 20120619092259000-22

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            commonServer.updateDataSetProperties(session.getSessionToken(), dataSetId, Arrays.asList(property));

            AbstractExternalData dataSet = commonServer.getDataSetInfo(session.getSessionToken(), dataSetId);
            assertEquals(dataSet.getProperties().get(0).getValue(), property.getValue());
        } else
        {
            try
            {
                commonServer.updateDataSetProperties(session.getSessionToken(), dataSetId, Arrays.asList(property));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListSampleExternalDataWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId sampleId = new TechId(1054L); // /TEST-SPACE/FV-TEST

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<AbstractExternalData> dataSets = commonServer.listSampleExternalData(session.getSessionToken(), sampleId, true);
            assertEntities("[20120628092259000-41]", dataSets);
        } else
        {
            try
            {
                commonServer.listSampleExternalData(session.getSessionToken(), sampleId, true);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListExperimentExternalDataWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId experimentId = new TechId(23L); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<AbstractExternalData> dataSets = commonServer.listExperimentExternalData(session.getSessionToken(), experimentId, true);
            assertEquals(dataSets.size(), 9);
        } else
        {
            try
            {
                commonServer.listExperimentExternalData(session.getSessionToken(), experimentId, true);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListDataSetRelationshipsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId parentId = new TechId(28L); // VALIDATIONS_PARENT-28

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<AbstractExternalData> children =
                    commonServer.listDataSetRelationships(session.getSessionToken(), parentId, DataSetRelationshipRole.PARENT);
            assertEntities("[VALIDATIONS_IMPOS-27]", children);
        } else
        {
            try
            {
                commonServer.listDataSetRelationships(session.getSessionToken(), parentId, DataSetRelationshipRole.PARENT);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetDataSetInfoWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId dataSetId = new TechId(41L); // 20120628092259000-41

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            AbstractExternalData dataSet = commonServer.getDataSetInfo(session.getSessionToken(), dataSetId);
            assertNotNull(dataSet);
            assertEquals(dataSet.getCode(), "20120628092259000-41");
        } else
        {
            try
            {
                commonServer.getDataSetInfo(session.getSessionToken(), dataSetId);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListRelatedDataSetsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        BasicEntityInformationHolder experiment = new BasicEntityInformationHolder(EntityKind.EXPERIMENT, null, null, 23L, null); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST
        DataSetRelatedEntities related = new DataSetRelatedEntities(Arrays.asList(experiment));

        if (user.isDisabledProjectUser())
        {
            try
            {
                commonServer.listRelatedDataSets(session.getSessionToken(), related, false);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        } else
        {
            List<AbstractExternalData> dataSets = commonServer.listRelatedDataSets(session.getSessionToken(), related, false);

            if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
            {
                assertEquals(dataSets.size(), 9);
            } else
            {
                assertEquals(dataSets.size(), 0);
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListRelatedDataSetsOnBehalfOfUserWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(TEST_USER, PASSWORD);

        BasicEntityInformationHolder experiment = new BasicEntityInformationHolder(EntityKind.EXPERIMENT, null, null, 23L, null); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST
        DataSetRelatedEntities related = new DataSetRelatedEntities(Arrays.asList(experiment));

        List<AbstractExternalData> dataSets =
                commonServer.listRelatedDataSetsOnBehalfOfUser(session.getSessionToken(), related, false, user.getUserId());

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            assertEquals(dataSets.size(), 9);
        } else
        {
            assertEquals(dataSets.size(), 0);
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testDeleteDataSetsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        String dataSetCode = "20120628092259000-41";
        TechId dataSetId = new TechId(41L);

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            commonServer.deleteDataSets(session.getSessionToken(), Arrays.asList(dataSetCode), "test reason", DeletionType.TRASH, true);
            try
            {
                commonServer.getDataSetInfo(session.getSessionToken(), dataSetId);
                fail();
            } catch (UserFailureException e)
            {
                assertEquals(e.getMessage(), "Data set with ID '41' does not exist.");
            }
        } else
        {
            try
            {
                commonServer.deleteDataSets(session.getSessionToken(), Arrays.asList(dataSetCode), "test reason", DeletionType.TRASH, true);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testUploadDataSetsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        String dataSetCode = "20120628092259000-41";
        DataSetUploadContext uploadContext = new DataSetUploadContext();

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            commonServer.uploadDataSets(session.getSessionToken(), Arrays.asList(dataSetCode), uploadContext);
        } else
        {
            try
            {
                commonServer.uploadDataSets(session.getSessionToken(), Arrays.asList(dataSetCode), uploadContext);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testCreateReportFromDatasetsWithServiceKeyWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        String dataSetCode = "20120628092259000-41";
        String serviceKey = "I-DONT-EXIST";

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            try
            {
                commonServer.createReportFromDatasets(session.getSessionToken(), serviceKey, Arrays.asList(dataSetCode));
            } catch (Exception e)
            {
                assertEquals(e.getMessage(), "Data store 'STANDARD' does not have '" + serviceKey + "' report configured.");
            }
        } else
        {
            try
            {
                commonServer.createReportFromDatasets(session.getSessionToken(), serviceKey, Arrays.asList(dataSetCode));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testCreateReportFromDatasetsWithServiceDescriptionWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        String dataSetCode = "20120628092259000-41";
        DatastoreServiceDescription serviceDescription = DatastoreServiceDescription.reporting(null, null, new String[] {}, "I-DONT-EXIST", null);

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            try
            {
                commonServer.createReportFromDatasets(session.getSessionToken(), serviceDescription, Arrays.asList(dataSetCode));
            } catch (Exception e)
            {
                assertEquals(e.getMessage(), "Cannot find the data store " + serviceDescription.getDatastoreCode());
            }
        } else
        {
            try
            {
                commonServer.createReportFromDatasets(session.getSessionToken(), serviceDescription, Arrays.asList(dataSetCode));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testProcessDatasetsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        String dataSetCode = "20120628092259000-41";
        DatastoreServiceDescription serviceDescription = DatastoreServiceDescription.processing("I-DONT-EXIST", null, new String[] {}, "STANDARD");

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            try
            {
                commonServer.processDatasets(session.getSessionToken(), serviceDescription, Arrays.asList(dataSetCode));
            } catch (Exception e)
            {
                assertEquals(e.getMessage(),
                        "Data store 'STANDARD' does not have '" + serviceDescription.getKey() + "' processing plugin configured.");
            }
        } else
        {
            try
            {
                commonServer.processDatasets(session.getSessionToken(), serviceDescription, Arrays.asList(dataSetCode));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testArchiveDataSetsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        String dataSetCode = "20120628092259000-41";

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            commonServer.archiveDatasets(session.getSessionToken(), Arrays.asList(dataSetCode), false);
        } else
        {
            try
            {
                commonServer.archiveDatasets(session.getSessionToken(), Arrays.asList(dataSetCode), false);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testUnarchiveDataSetsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        String dataSetCode = "20120628092259000-41";

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            commonServer.unarchiveDatasets(session.getSessionToken(), Arrays.asList(dataSetCode));
        } else
        {
            try
            {
                commonServer.unarchiveDatasets(session.getSessionToken(), Arrays.asList(dataSetCode));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testLockDataSetsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        String dataSetCode = "20120628092259000-41";

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            commonServer.lockDatasets(session.getSessionToken(), Arrays.asList(dataSetCode));
        } else
        {
            try
            {
                commonServer.lockDatasets(session.getSessionToken(), Arrays.asList(dataSetCode));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testUnlockDataSetsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        String dataSetCode = "20120628092259000-41";

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            commonServer.unlockDatasets(session.getSessionToken(), Arrays.asList(dataSetCode));
        } else
        {
            try
            {
                commonServer.unlockDatasets(session.getSessionToken(), Arrays.asList(dataSetCode));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testRetrieveLinkFromDataSetWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        String dataSetCode = "20120628092259000-41";
        DatastoreServiceDescription serviceDescription = DatastoreServiceDescription.reporting(null, null, new String[] {}, "I-DONT-EXIST", null);

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            try
            {
                commonServer.retrieveLinkFromDataSet(session.getSessionToken(), serviceDescription, dataSetCode);
            } catch (Exception e)
            {
                assertEquals(e.getMessage(), "Cannot find the data store " + serviceDescription.getDatastoreCode());
            }
        } else
        {
            try
            {
                commonServer.retrieveLinkFromDataSet(session.getSessionToken(), serviceDescription, dataSetCode);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListPropertyTypesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            List<PropertyType> types = commonServer.listPropertyTypes(session.getSessionToken(), false);
            assertEquals(types.size(), 18);
        } else
        {
            try
            {
                commonServer.listPropertyTypes(session.getSessionToken(), false);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListEntityTypesPropertyTypesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            int count = 0;
            for (ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind entityKind : ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind
                    .values())
            {
                IEntityPropertyTypeDAO entityPropertyTypeDAO = daoFactory.getEntityPropertyTypeDAO(entityKind);
                count += entityPropertyTypeDAO.listEntityPropertyTypes().size();
            }

            List<EntityTypePropertyType<?>> types = commonServer.listEntityTypePropertyTypes(session.getSessionToken());
            assertEquals(types.size(), count);
        } else
        {
            try
            {
                commonServer.listEntityTypePropertyTypes(session.getSessionToken());
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListEntityTypesPropertyTypesForTypeWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        ExperimentType experimentType = new ExperimentType();
        experimentType.setCode("COMPOUND_HCS");

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            List<EntityTypePropertyType<?>> types = commonServer.listEntityTypePropertyTypes(session.getSessionToken(), experimentType);
            assertEquals(types.size(), 3);
        } else
        {
            try
            {
                commonServer.listEntityTypePropertyTypes(session.getSessionToken(), experimentType);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListMaterialTypesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            List<MaterialType> types = commonServer.listMaterialTypes(session.getSessionToken());
            assertEquals(types.size(), 11);
        } else
        {
            try
            {
                commonServer.listMaterialTypes(session.getSessionToken());
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetMaterialTypeWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);
        String code = "BACTERIUM";

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            MaterialType type = commonServer.getMaterialType(session.getSessionToken(), code);
            assertEquals(type.getCode(), code);
        } else
        {
            try
            {
                commonServer.getMaterialType(session.getSessionToken(), code);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListDataTypesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            List<DataType> types = commonServer.listDataTypes(session.getSessionToken());
            assertEquals(types.size(), 10);
        } else
        {
            try
            {
                commonServer.listDataTypes(session.getSessionToken());
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListFileFormatTypesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            List<FileFormatType> types = commonServer.listFileFormatTypes(session.getSessionToken());
            assertEquals(types.size(), 8);
        } else
        {
            try
            {
                commonServer.listFileFormatTypes(session.getSessionToken());
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListVocabulariesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            List<Vocabulary> vocabularies = commonServer.listVocabularies(session.getSessionToken(), false, false);
            assertEquals(vocabularies.size(), 6);
        } else
        {
            try
            {
                commonServer.listVocabularies(session.getSessionToken(), false, false);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListMaterialsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        ListMaterialCriteria criteria = ListMaterialCriteria.createFromMaterialIds(Arrays.asList(1L, 2L, 3L));

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            List<Material> materials = commonServer.listMaterials(session.getSessionToken(), criteria, false);
            assertEquals(materials.size(), 3);
        } else
        {
            try
            {
                commonServer.listMaterials(session.getSessionToken(), criteria, false);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListVocabularyTermsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setId(1L);

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            Set<VocabularyTerm> terms = commonServer.listVocabularyTerms(session.getSessionToken(), vocabulary);
            assertEquals(terms.size(), 3);
        } else
        {
            try
            {
                commonServer.listVocabularyTerms(session.getSessionToken(), vocabulary);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListDataSetTypesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            List<DataSetType> types = commonServer.listDataSetTypes(session.getSessionToken());
            assertEquals(types.size(), 11);
        } else
        {
            try
            {
                commonServer.listDataSetTypes(session.getSessionToken());
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetMaterialInfoWithMaterialIdentifierWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        MaterialIdentifier identifier = new MaterialIdentifier("BACTERIUM1", "BACTERIUM");

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            Material material = commonServer.getMaterialInfo(session.getSessionToken(), identifier);
            assertEquals(material.getCode(), "BACTERIUM1");
        } else
        {
            try
            {
                commonServer.getMaterialInfo(session.getSessionToken(), identifier);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetMaterialInfoWithMaterialIdWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId materialId = new TechId(22L);

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            Material material = commonServer.getMaterialInfo(session.getSessionToken(), materialId);
            assertEquals(material.getCode(), "BACTERIUM1");
        } else
        {
            try
            {
                commonServer.getMaterialInfo(session.getSessionToken(), materialId);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetMaterialInformationHolderWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        MaterialIdentifier identifier = new MaterialIdentifier("BACTERIUM1", "BACTERIUM");

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            IEntityInformationHolderWithPermId holder = commonServer.getMaterialInformationHolder(session.getSessionToken(), identifier);
            assertEquals(holder.getCode(), "BACTERIUM1");
        } else
        {
            try
            {
                commonServer.getMaterialInformationHolder(session.getSessionToken(), identifier);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListScriptsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        ScriptType type = ScriptType.ENTITY_VALIDATION;
        EntityKind kind = EntityKind.SAMPLE;

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            List<Script> scripts = commonServer.listScripts(session.getSessionToken(), type, kind);
            assertEquals(scripts.size(), 7);
        } else
        {
            try
            {
                commonServer.listScripts(session.getSessionToken(), type, kind);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListFiltersWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            List<GridCustomFilter> filters = commonServer.listFilters(session.getSessionToken(), "test_grid");
            assertEquals(filters.size(), 1);
        } else
        {
            try
            {
                commonServer.listFilters(session.getSessionToken(), "test_grid");
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListDataStoresWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            List<DataStore> stores = commonServer.listDataStores(session.getSessionToken());
            assertEquals(stores.size(), 1);
            assertEquals(stores.get(0).getCode(), "STANDARD");
        } else
        {
            try
            {
                commonServer.listDataStores(session.getSessionToken());
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListDataStoreServicesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        DataStoreServiceKind kind = DataStoreServiceKind.PROCESSING;

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            List<DatastoreServiceDescription> services = commonServer.listDataStoreServices(session.getSessionToken(), kind);
            assertEquals(services.size(), 1);
            assertEquals(services.get(0).getKey(), "test_service");
        } else
        {
            try
            {
                commonServer.listDataStoreServices(session.getSessionToken(), kind);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetDefaultPutDataStoreBaseURLWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            String url = commonServer.getDefaultPutDataStoreBaseURL(session.getSessionToken());
            assertEquals(url, "http://localhost:8765");
        } else
        {
            try
            {
                commonServer.getDefaultPutDataStoreBaseURL(session.getSessionToken());
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetScriptInfoWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId scriptId = new TechId(5L);

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            Script script = commonServer.getScriptInfo(session.getSessionToken(), scriptId);
            assertEquals(script.getName(), "validateOK");
        } else
        {
            try
            {
                commonServer.getScriptInfo(session.getSessionToken(), scriptId);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testSearchForMaterialsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        DetailedSearchCriterion criterion = new DetailedSearchCriterion();
        criterion.setField(DetailedSearchField.createAttributeField(MaterialAttributeSearchFieldKind.CODE));
        criterion.setValue("BACTERIUM1");

        DetailedSearchCriteria criteria = new DetailedSearchCriteria();
        criteria.setCriteria(Arrays.asList(criterion));
        criteria.setConnection(SearchCriteriaConnection.MATCH_ANY);

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            List<Material> materials = commonServer.searchForMaterials(session.getSessionToken(), criteria);
            assertEquals(materials.size(), 1);
            assertEquals(materials.get(0).getCode(), "BACTERIUM1");
        } else
        {
            try
            {
                commonServer.searchForMaterials(session.getSessionToken(), criteria);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetMetaprojectWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        IMetaprojectId metaprojectId = createMetaprojectId(createMetaproject(user.getUserId(), "PA_TEST"));

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            Metaproject metaproject = commonServer.getMetaproject(session.getSessionToken(), metaprojectId);
            assertEquals(metaproject.getCode(), "PA_TEST");
        } else
        {
            try
            {
                commonServer.getMetaproject(session.getSessionToken(), metaprojectId);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListMetaprojectsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        createMetaproject(user.getUserId(), "PA_TEST");

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            List<Metaproject> metaprojects = commonServer.listMetaprojects(session.getSessionToken());
            assertEquals(metaprojects.size(), 1);
            assertEquals(metaprojects.get(0).getCode(), "PA_TEST");
        } else
        {
            try
            {
                commonServer.listMetaprojects(session.getSessionToken());
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListMetaprojectAssignmentsCountsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        MetaprojectPE metaproject = createMetaproject(user.getUserId(), "PA_TEST");
        createMetaprojectAssignment(metaproject, "200811050951882-1028", null, null); // /CISD/NEMO/EXP1
        createMetaprojectAssignment(metaproject, "201206190940555-1032", null, null); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST
        createMetaprojectAssignment(metaproject, "200902091255058-1037", null, null); // /TEST-SPACE/NOE/EXP-TEST-2

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            List<MetaprojectAssignmentsCount> counts = commonServer.listMetaprojectAssignmentsCounts(session.getSessionToken());
            assertEquals(counts.size(), 1);
            // all connected entities are counted, the entities user has not access to are returned as stubs
            assertEquals(counts.get(0).getExperimentCount(), 3);
        } else
        {
            try
            {
                commonServer.listMetaprojectAssignmentsCounts(session.getSessionToken());
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetMetaprojectAssignmentsCountWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        MetaprojectPE metaproject = createMetaproject(user.getUserId(), "PA_TEST");
        createMetaprojectAssignment(metaproject, "200811050951882-1028", null, null); // /CISD/NEMO/EXP1
        createMetaprojectAssignment(metaproject, "201206190940555-1032", null, null); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST
        createMetaprojectAssignment(metaproject, "200902091255058-1037", null, null); // /TEST-SPACE/NOE/EXP-TEST-2

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            MetaprojectAssignmentsCount count =
                    commonServer.getMetaprojectAssignmentsCount(session.getSessionToken(), new MetaprojectIdentifierId(metaproject.getIdentifier()));
            // all connected entities are counted, the entities user has not access to are returned as stubs
            assertEquals(count.getExperimentCount(), 3);
        } else
        {
            try
            {
                commonServer.getMetaprojectAssignmentsCount(session.getSessionToken(), new MetaprojectIdentifierId(metaproject.getIdentifier()));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetMetaprojectAssignmentsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        MetaprojectPE metaproject = createMetaproject(user.getUserId(), "PA_TEST");
        createMetaprojectAssignment(metaproject, "200811050951882-1028", null, null); // /CISD/NEMO/EXP1
        createMetaprojectAssignment(metaproject, "200902091255058-1037", null, null); // /TEST-SPACE/NOE/EXP-TEST-2
        createMetaprojectAssignment(metaproject, "201206190940555-1032", null, null); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST

        if (user.isDisabledProjectUser())
        {
            try
            {
                commonServer.getMetaprojectAssignments(session.getSessionToken(), new MetaprojectIdentifierId(metaproject.getIdentifier()));
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        } else
        {
            MetaprojectAssignments assignments =
                    commonServer.getMetaprojectAssignments(session.getSessionToken(), new MetaprojectIdentifierId(metaproject.getIdentifier()));
            List<Experiment> experiments = assignments.getExperiments();

            Collections.sort(experiments, new Comparator<Experiment>()
                {
                    @Override
                    public int compare(Experiment o1, Experiment o2)
                    {
                        return o1.getPermId().compareTo(o2.getPermId());
                    }
                });

            if (user.isInstanceUser())
            {
                assertEntities("[/CISD/NEMO/EXP1, /TEST-SPACE/NOE/EXP-TEST-2, /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST]", experiments);
            } else if (user.isTestSpaceUser())
            {
                assertEquals(experiments.get(0).isStub(), true);
                assertEquals(experiments.get(0).getPermId(), "200811050951882-1028");

                assertEquals(experiments.get(1).isStub(), false);
                assertEquals(experiments.get(1).getIdentifier(), "/TEST-SPACE/NOE/EXP-TEST-2");

                assertEquals(experiments.get(2).isStub(), false);
                assertEquals(experiments.get(2).getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");

            } else if (user.isTestProjectUser())
            {
                assertEquals(experiments.get(0).isStub(), true);
                assertEquals(experiments.get(0).getPermId(), "200811050951882-1028");

                assertEquals(experiments.get(1).isStub(), true);
                assertEquals(experiments.get(1).getPermId(), "200902091255058-1037");

                assertEquals(experiments.get(2).isStub(), false);
                assertEquals(experiments.get(2).getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
            } else
            {
                assertEquals(experiments.get(0).isStub(), true);
                assertEquals(experiments.get(0).getPermId(), "200811050951882-1028");

                assertEquals(experiments.get(1).isStub(), true);
                assertEquals(experiments.get(1).getPermId(), "200902091255058-1037");

                assertEquals(experiments.get(2).isStub(), true);
                assertEquals(experiments.get(2).getPermId(), "201206190940555-1032");
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListEntityHistoryWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO adminSession = commonServer.tryAuthenticate(TEST_USER, PASSWORD);
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        EntityKind entityKind = EntityKind.EXPERIMENT;
        TechId entityId = new TechId(23L); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST

        // update project to /TEST-SPACE/NOE
        ExperimentUpdatesDTO update = new ExperimentUpdatesDTO();
        update.setExperimentId(entityId);
        update.setProjectIdentifier(new ProjectIdentifier("TEST-SPACE", "NOE"));
        update.setProperties(new ArrayList<IEntityProperty>());
        update.setAttachments(new ArrayList<NewAttachment>());
        ExperimentUpdateResult updateResult = commonServer.updateExperiment(adminSession.getSessionToken(), update);

        // update project to /TEST-SPACE/PROJECT-TO-DELETE
        update.setProjectIdentifier(new ProjectIdentifier("TEST-SPACE", "PROJECT-TO-DELETE"));
        update.setVersion(updateResult.getVersion());
        updateResult = commonServer.updateExperiment(adminSession.getSessionToken(), update);

        // update project back to /TEST-SPACE/TEST-PROJECT
        update.setProjectIdentifier(new ProjectIdentifier("TEST-SPACE", "TEST-PROJECT"));
        update.setVersion(updateResult.getVersion());
        commonServer.updateExperiment(adminSession.getSessionToken(), update);

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<EntityHistory> history = commonServer.listEntityHistory(session.getSessionToken(), entityKind, entityId);

            Collections.sort(history, new Comparator<EntityHistory>()
                {
                    @Override
                    public int compare(EntityHistory o1, EntityHistory o2)
                    {
                        return o1.tryGetRelatedProject().getIdentifier().compareTo(o2.tryGetRelatedProject().getIdentifier());
                    }
                });

            if (user.isInstanceUser() || user.isTestSpaceUser())
            {
                assertEquals(history.size(), 2);
                assertEquals(history.get(0).tryGetRelatedProject().getIdentifier(), "/TEST-SPACE/NOE");
                assertEquals(history.get(1).tryGetRelatedProject().getIdentifier(), "/TEST-SPACE/PROJECT-TO-DELETE");
            } else
            {
                assertEquals(history.size(), 1);
                assertEquals(history.get(0).tryGetRelatedProject().getIdentifier(), "/TEST-SPACE/PROJECT-TO-DELETE");
            }
        } else
        {
            try
            {
                commonServer.listEntityHistory(session.getSessionToken(), entityKind, entityId);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListMatchingEntitiesWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        SearchableEntity[] searchableEntities = SearchableEntity.values();
        String queryText = "\"/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST\"";
        boolean useWildcardSearchMode = false;
        int maxSize = Integer.MAX_VALUE;

        if (user.isDisabledProjectUser())
        {
            try
            {
                commonServer.listMatchingEntities(session.getSessionToken(), searchableEntities, queryText, useWildcardSearchMode, maxSize);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        } else
        {
            List<MatchingEntity> results =
                    commonServer.listMatchingEntities(session.getSessionToken(), searchableEntities, queryText, useWildcardSearchMode, maxSize);

            if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
            {
                assertEquals(results.size(), 1);
                assertEquals(results.get(0).getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
            } else
            {
                assertEquals(results.size(), 0);
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetEntityInformationHolderByExperimentPermIdWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        EntityKind entityKind = EntityKind.EXPERIMENT;
        String permId = "201206190940555-1032"; // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            IEntityInformationHolderWithPermId entity = commonServer.getEntityInformationHolder(session.getSessionToken(), entityKind, permId);
            assertEquals(entity.getCode(), "EXP-SPACE-TEST");
        } else
        {
            try
            {
                commonServer.getEntityInformationHolder(session.getSessionToken(), entityKind, permId);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetEntityInformationHolderBySamplePermIdWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        EntityKind entityKind = EntityKind.SAMPLE;
        String permId = "201206191219327-1054"; // /TEST-SPACE/FV-TEST

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            IEntityInformationHolderWithPermId entity = commonServer.getEntityInformationHolder(session.getSessionToken(), entityKind, permId);
            assertEquals(entity.getCode(), "FV-TEST");
        } else
        {
            try
            {
                commonServer.getEntityInformationHolder(session.getSessionToken(), entityKind, permId);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetEntityInformationHolderByDataSetPermIdWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        EntityKind entityKind = EntityKind.DATA_SET;
        String permId = "20120628092259000-41";

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            IEntityInformationHolderWithPermId entity = commonServer.getEntityInformationHolder(session.getSessionToken(), entityKind, permId);
            assertEquals(entity.getCode(), "20120628092259000-41");
        } else
        {
            try
            {
                commonServer.getEntityInformationHolder(session.getSessionToken(), entityKind, permId);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetEntityInformationHolderByMaterialPermIdWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        EntityKind entityKind = EntityKind.MATERIAL;
        String permId = new MaterialIdentifier("BACTERIUM1", "BACTERIUM").toString();

        if (user.isInstanceUserOrSpaceUserOrEnabledProjectUser())
        {
            IEntityInformationHolderWithPermId entity = commonServer.getEntityInformationHolder(session.getSessionToken(), entityKind, permId);
            assertEquals(entity.getCode(), "BACTERIUM1");
        } else
        {
            try
            {
                commonServer.getEntityInformationHolder(session.getSessionToken(), entityKind, permId);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetEntityInformationHolderByDescriptionWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(user.getUserId(), PASSWORD);

        BasicEntityDescription description = new BasicEntityDescription();
        description.setEntityIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        description.setEntityKind(EntityKind.EXPERIMENT);

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            IEntityInformationHolderWithPermId entity = commonServer.getEntityInformationHolder(session.getSessionToken(), description);
            assertEquals(entity.getCode(), "EXP-SPACE-TEST");
        } else
        {
            try
            {
                commonServer.getEntityInformationHolder(session.getSessionToken(), description);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    private void assertAssignedPropertyTypes(String expected, EntityType entityType)
    {
        List<? extends EntityTypePropertyType<?>> propTypes = entityType.getAssignedPropertyTypes();
        List<String> propertyCodes = new ArrayList<String>();
        for (EntityTypePropertyType<?> entityTypePropertyType : propTypes)
        {
            String code = entityTypePropertyType.getPropertyType().getCode();
            if (entityTypePropertyType.isMandatory())
            {
                code = code + "*";
            }
            propertyCodes.add(code);
        }
        Collections.sort(propertyCodes);
        assertEquals(expected, propertyCodes.toString());
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void testConcurrentDisplaySettingsUpdateForOneUserIsSafe()
    {
        testConcurrentDisplaySettingsUpdateForUsersIsSafe(new String[] { "test" }, 10, 10);
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void testConcurrentDisplaySettingsUpdateForDifferentUsersIsSafe()
    {
        testConcurrentDisplaySettingsUpdateForUsersIsSafe(new String[] { "test", "test_role" }, 5, 10);
    }

    @SuppressWarnings("deprecation")
    private void testConcurrentDisplaySettingsUpdateForUsersIsSafe(String[] users, int numberOfThreads, int numberOfIterations)
    {
        final String PANEL_ID = "panel_id";
        final String FINISHED_MESSAGE = "finished";

        MessageChannel sendChannel = new MessageChannel(5000);
        List<Thread> threads = new ArrayList<Thread>();
        SessionContextDTO[] sessionContext = new SessionContextDTO[users.length];

        for (int u = 0; u < users.length; u++)
        {

            for (int i = 0; i < numberOfThreads; i++)
            {
                sessionContext[u] = commonServer.tryAuthenticate(users[u], PASSWORD);
                new SetPanelSizeRunnable(commonServer, sessionContext[u].getSessionToken(), PANEL_ID, 0).run();
                IncrementPanelSizeRunnable runnable =
                        new IncrementPanelSizeRunnable(commonServer, sessionContext[u].getSessionToken(), PANEL_ID, numberOfIterations);
                runnable.setSendChannel(sendChannel);
                runnable.setFinishedMessage(FINISHED_MESSAGE);
                Thread thread = new Thread(runnable);
                thread.setDaemon(true);
                threads.add(thread);
            }
        }

        for (Thread thread : threads)
        {
            thread.start();
        }

        for (int i = 0; i < threads.size(); i++)
        {
            sendChannel.assertNextMessage(FINISHED_MESSAGE);
        }

        for (int u = 0; u < users.length; u++)
        {
            sessionContext[u] = commonServer.tryGetSession(sessionContext[u].getSessionToken());
            assertEquals(Integer.valueOf(numberOfThreads * numberOfIterations),
                    sessionContext[u].getDisplaySettings().getPanelSizeSettings().get(PANEL_ID));
        }
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void testLongRunninngDisplaySettingsUpdateForOneUserBlocksOtherUpdatesForThisUser() throws Exception
    {
        final String USER_ID = "test";
        final String PANEL_ID = "testPanelId";
        final String FINISHED_MESSAGE = "finished";
        final long TIMEOUT = 1000;

        DataSource dataSource = (DataSource) applicationContext.getBean("data-source");
        Connection connection = dataSource.getConnection();

        try
        {
            connection.setAutoCommit(false);

            /*
             * DummyAuthenticationService always returns random principals and triggers a person update during login. As we don't want to hold any
             * locks on the persons table at this point, we call tryAuthenticate methods in the main thread. The test method is marked with
             * Propagation.NEVER, which makes each of these calls to be executed in a separate transaction that is auto-committed.
             */
            SessionContextDTO sessionContext1 = commonServer.tryAuthenticate(USER_ID, PASSWORD);
            operationLog.info("User  '" + USER_ID + "' authenticated");

            SessionContextDTO sessionContext2 = commonServer.tryAuthenticate(USER_ID, PASSWORD);
            operationLog.info("User  '" + USER_ID + "' authenticated");

            /*
             * Acquire a database lock on USER_ID_1 person. It will block updating the display settings for that person.
             */
            PreparedStatement statement = connection.prepareStatement("UPDATE persons SET registration_timestamp = now() WHERE user_id = ?");
            statement.setString(1, USER_ID);
            statement.executeUpdate();
            operationLog.info("User '" + USER_ID + "' locked by a SQL query");

            MessageChannel sendChannel = new MessageChannel(TIMEOUT);

            /*
             * Will concurrently update the same person in two separate transactions.
             */

            IncrementPanelSizeRunnable runnable1 = new IncrementPanelSizeRunnable(commonServer, sessionContext1.getSessionToken(), PANEL_ID, 1);
            IncrementPanelSizeRunnable runnable2 = new IncrementPanelSizeRunnable(commonServer, sessionContext2.getSessionToken(), PANEL_ID, 1);

            runnable1.setSendChannel(sendChannel);
            runnable2.setSendChannel(sendChannel);

            runnable1.setFinishedMessage(FINISHED_MESSAGE);
            runnable2.setFinishedMessage(FINISHED_MESSAGE);

            Thread thread1 = new Thread(runnable1);
            Thread thread2 = new Thread(runnable2);

            thread1.setDaemon(true);
            thread2.setDaemon(true);

            operationLog.info("Will try to update user '" + USER_ID + "' display settings");
            /*
             * First try to update the person without releasing the database lock.
             */
            thread1.start();
            thread2.start();

            Thread.sleep(TIMEOUT);
            sendChannel.assertEmpty();

            operationLog.info("Still waiting to update user '" + USER_ID + "' display settings");

            /*
             * After releasing the database lock, updating the person should succeed.
             */
            connection.rollback();

            operationLog.info("Releasing SQL lock on user '" + USER_ID + "'");

            sendChannel.assertNextMessage(FINISHED_MESSAGE);
            sendChannel.assertNextMessage(FINISHED_MESSAGE);

            operationLog.info("Successfully updated user '" + USER_ID + "' display settings");

        } finally
        {
            connection.rollback();
            connection.setAutoCommit(true);
            connection.close();
        }
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void testLongRunninngDisplaySettingsUpdateForOneUserDoesNotBlockUpdatesForOtherUsers() throws Exception
    {
        final String USER_ID_1 = "test";
        final String USER_ID_2 = "test_role";
        final String PANEL_ID = "testPanelId";
        final String FINISHED_MESSAGE_1 = "finished1";
        final String FINISHED_MESSAGE_2 = "finished2";
        final long TIMEOUT = 1000;

        DataSource dataSource = (DataSource) applicationContext.getBean("data-source");
        Connection connection = dataSource.getConnection();

        try
        {
            connection.setAutoCommit(false);

            /*
             * DummyAuthenticationService always returns random principals and triggers a person update during login. As we don't want to hold any
             * locks on the persons table at this point, we call tryAuthenticate methods in the main thread. The test method is marked with
             * Propagation.NEVER, which makes each of these calls to be executed in a separate transaction that is auto-committed.
             */
            SessionContextDTO sessionContext1 = commonServer.tryAuthenticate(USER_ID_1, PASSWORD);
            operationLog.info("User  '" + USER_ID_1 + "' authenticated");

            SessionContextDTO sessionContext2 = commonServer.tryAuthenticate(USER_ID_2, PASSWORD);
            operationLog.info("User '" + USER_ID_2 + "' authenticated");

            /*
             * Acquire a database lock on USER_ID_1 person. It will block updating the display settings for that person.
             */
            PreparedStatement statement = connection.prepareStatement("UPDATE persons SET registration_timestamp = now() WHERE user_id = ?");
            statement.setString(1, USER_ID_1);
            statement.executeUpdate();
            operationLog.info("User '" + USER_ID_1 + "' locked by a SQL query");

            MessageChannel sendChannel = new MessageChannel(TIMEOUT);

            /*
             * Will concurrently update two different persons in two separate transactions.
             */
            IncrementPanelSizeRunnable runnable1 = new IncrementPanelSizeRunnable(commonServer, sessionContext1.getSessionToken(), PANEL_ID, 1);
            IncrementPanelSizeRunnable runnable2 = new IncrementPanelSizeRunnable(commonServer, sessionContext2.getSessionToken(), PANEL_ID, 1);

            runnable1.setSendChannel(sendChannel);
            runnable2.setSendChannel(sendChannel);

            runnable1.setFinishedMessage(FINISHED_MESSAGE_1);
            runnable2.setFinishedMessage(FINISHED_MESSAGE_2);

            Thread thread1 = new Thread(runnable1);
            Thread thread2 = new Thread(runnable2);
            thread1.setDaemon(true);
            thread2.setDaemon(true);

            operationLog.info("Will try to update user '" + USER_ID_1 + "' display settings");
            /*
             * First try to update the USER_ID_1 person that is blocked by the database lock.
             */
            thread1.start();

            Thread.sleep(TIMEOUT);
            sendChannel.assertEmpty();

            operationLog.info("Still waiting to update user '" + USER_ID_1 + "' display settings");
            operationLog.info("Will try to update user '" + USER_ID_2 + "' display settings");

            /*
             * Now try to update USER_ID_2 person that is not blocked by any database lock.
             */
            thread2.start();

            sendChannel.assertNextMessage(FINISHED_MESSAGE_2);

            operationLog.info("Successfully update user  '" + USER_ID_2 + "' display settings");

            /*
             * After releasing the database lock, updating USER_ID_1 person should also succeed.
             */
            connection.rollback();

            operationLog.info("Releasing SQL lock on user '" + USER_ID_1 + "'");

            sendChannel.assertNextMessage(FINISHED_MESSAGE_1);

            operationLog.info("Successfully updated user '" + USER_ID_1 + "' display settings");

        } finally
        {
            operationLog.info("Cleaning up");
            connection.rollback();
            connection.setAutoCommit(true);
            connection.close();
        }
    }

    private static class SetPanelSizeRunnable implements Runnable
    {
        private ICommonServer server;

        private String sessionToken;

        private String panelId;

        private int value;

        public SetPanelSizeRunnable(ICommonServer server, String sessionToken, String panelId, int value)
        {
            this.server = server;
            this.sessionToken = sessionToken;
            this.panelId = panelId;
            this.value = value;
        }

        @Override
        public void run()
        {
            IDisplaySettingsUpdate update = new IDisplaySettingsUpdate()
                {

                    private static final long serialVersionUID = 1L;

                    @SuppressWarnings("deprecation")
                    @Override
                    public DisplaySettings update(DisplaySettings displaySettings)
                    {
                        Map<String, Integer> panelSizeSettings = displaySettings.getPanelSizeSettings();
                        panelSizeSettings.put(panelId, value);
                        return displaySettings;
                    }
                };
            server.updateDisplaySettings(sessionToken, update);
        }
    }

    private static class IncrementPanelSizeRunnable implements Runnable
    {
        private ICommonServer server;

        private String sessionToken;

        private String panelId;

        private int count;

        private MessageChannel sendChannel;

        private String finishedMessage;

        public IncrementPanelSizeRunnable(ICommonServer server, String sessionToken, String panelId, int count)
        {
            this.server = server;
            this.sessionToken = sessionToken;
            this.panelId = panelId;
            this.count = count;
        }

        @Override
        public void run()
        {
            IDisplaySettingsUpdate update = new IDisplaySettingsUpdate()
                {

                    private static final long serialVersionUID = 1L;

                    @SuppressWarnings("deprecation")
                    @Override
                    public DisplaySettings update(DisplaySettings displaySettings)
                    {
                        Map<String, Integer> panelSizeSettings = displaySettings.getPanelSizeSettings();
                        Integer panelSize = panelSizeSettings.get(panelId);
                        if (panelSize == null)
                        {
                            panelSize = 0;
                        }
                        try
                        {
                            // increase probability of race condition
                            Thread.sleep(5);
                        } catch (Exception e)
                        {

                        }
                        panelSizeSettings.put(panelId, panelSize + 1);
                        return displaySettings;
                    }
                };

            for (int value = 0; value < count; value++)
            {
                server.updateDisplaySettings(sessionToken, update);
            }

            if (getSendChannel() != null && getFinishedMessage() != null)
            {
                getSendChannel().send(getFinishedMessage());
            }
        }

        public MessageChannel getSendChannel()
        {
            return sendChannel;
        }

        public void setSendChannel(MessageChannel sendChannel)
        {
            this.sendChannel = sendChannel;
        }

        public String getFinishedMessage()
        {
            return finishedMessage;
        }

        public void setFinishedMessage(String finishedMessage)
        {
            this.finishedMessage = finishedMessage;
        }

    }

    private MetaprojectPE createMetaproject(String owner, String code)
    {
        PersonPE person = daoFactory.getPersonDAO().tryFindPersonByUserId(owner);

        MetaprojectPE metaproject = new MetaprojectPE();
        metaproject.setName(code);
        metaproject.setOwner(person);

        daoFactory.getMetaprojectDAO().createOrUpdateMetaproject(metaproject, person);

        return metaproject;
    }

    private IMetaprojectId createMetaprojectId(MetaprojectPE metaproject)
    {
        return new MetaprojectIdentifierId(metaproject.getOwner().getUserId(), metaproject.getCode());
    }

    private void createMetaprojectAssignment(MetaprojectPE metaproject, String experimentPermId, String samplePermId, String dataSetPermId)
    {
        MetaprojectAssignmentPE assignment = new MetaprojectAssignmentPE();
        assignment.setMetaproject(metaproject);

        if (experimentPermId != null)
        {
            ExperimentPE experiment = daoFactory.getExperimentDAO().tryGetByPermID(experimentPermId);
            assignment.setExperiment(experiment);
        }
        if (samplePermId != null)
        {
            SamplePE sample = daoFactory.getSampleDAO().tryToFindByPermID(samplePermId);
            assignment.setSample(sample);
        }
        if (dataSetPermId != null)
        {
            DataPE dataSet = daoFactory.getDataDAO().tryToFindDataSetByCode(dataSetPermId);
            assignment.setDataSet(dataSet);
        }

        daoFactory.getSessionFactory().getCurrentSession().save(assignment);
    }

}
