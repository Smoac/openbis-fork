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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.displaysettings.IDisplaySettingsUpdate;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

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
        return CollectionUtils.find(spaces, new Predicate<AuthorizationGroup>()
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

    @Test
    public void testListSamplesByMaterialProperties()
    {
        List<TechId> materialIds = Arrays.asList(new TechId(34));
        List<Sample> samples = commonServer.listSamplesByMaterialProperties(systemSessionToken, materialIds);

        assertEntities("[/CISD/CP-TEST-1, /CISD/PLATE_WELLSEARCH:WELL-A01]", samples);

        String observerSessionToken = commonServer.tryAuthenticate("observer", "a").getSessionToken();
        samples = commonServer.listSamplesByMaterialProperties(observerSessionToken, materialIds);

        assertEntities("[]", samples);

        // delete a sample
        commonServer.deleteSamples(systemSessionToken, Arrays.asList(new TechId(1051)), "test", DeletionType.TRASH);
        samples = commonServer.listSamplesByMaterialProperties(systemSessionToken, materialIds);

        assertEntities("[/CISD/CP-TEST-1]", samples);

    }

    @Test
    public void testUpdateProjectWithSpaceAdminWithProjectAuthorizationOff()
    {
        testUpdateAndExpectUserHasAccess(TEST_SPACE_PA_OFF);
    }

    @Test
    public void testUpdateProjectWithSpaceAdminWithProjectAuthorizationOn()
    {
        testUpdateAndExpectUserHasAccess(TEST_SPACE_PA_ON);
    }

    @Test
    public void testUpdateProjectWithProjectAdminWithProjectAuthorizationOff()
    {
        testUpdateAndExpectUserDoesNotHaveAccess(TEST_PROJECT_PA_OFF);
    }

    @Test
    public void testUpdateProjectWithProjectAdminWithProjectAuthorizationOn()
    {
        testUpdateAndExpectUserHasAccess(TEST_PROJECT_PA_ON);
    }

    private void testUpdateAndExpectUserHasAccess(String userId)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(userId, PASSWORD);

        ProjectUpdatesDTO updates = new ProjectUpdatesDTO();
        updates.setTechId(new TechId(5L)); // /TEST-SPACE/TEST-PROJECT
        updates.setAttachments(Collections.<NewAttachment> emptyList());
        updates.setDescription(String.valueOf(System.currentTimeMillis()));

        commonServer.updateProject(session.getSessionToken(), updates);
        Project info = commonServer.getProjectInfo(session.getSessionToken(), new ProjectIdentifier("TEST-SPACE", "TEST-PROJECT"));

        assertEquals(info.getDescription(), updates.getDescription());
    }

    private void testUpdateAndExpectUserDoesNotHaveAccess(String userId)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(userId, PASSWORD);

        ProjectUpdatesDTO updates = new ProjectUpdatesDTO();
        updates.setTechId(new TechId(5L)); // /TEST-SPACE/TEST-PROJECT
        updates.setAttachments(Collections.<NewAttachment> emptyList());
        updates.setDescription(String.valueOf(System.currentTimeMillis()));

        try
        {
            commonServer.updateProject(session.getSessionToken(), updates);
            Assert.fail();
        } catch (AuthorizationFailureException e)
        {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testListProjectsWithSpaceAdminWithProjectAuthorizationOff()
    {
        SessionContextDTO session = commonServer.tryAuthenticate(TEST_SPACE_PA_OFF, PASSWORD);

        List<Project> projects = commonServer.listProjects(session.getSessionToken());

        assertEntities("[/TEST-SPACE/NOE, /TEST-SPACE/PROJECT-TO-DELETE, /TEST-SPACE/TEST-PROJECT]", projects);
    }

    @Test
    public void testListProjectsWithSpaceAdminWithProjectAuthorizationOn()
    {
        SessionContextDTO session = commonServer.tryAuthenticate(TEST_SPACE_PA_ON, PASSWORD);

        List<Project> projects = commonServer.listProjects(session.getSessionToken());

        assertEntities("[/TEST-SPACE/NOE, /TEST-SPACE/PROJECT-TO-DELETE, /TEST-SPACE/TEST-PROJECT]", projects);
    }

    @Test
    public void testListProjectsWithProjectAdminWithProjectAuthorizationOff()
    {
        SessionContextDTO session = commonServer.tryAuthenticate(TEST_PROJECT_PA_OFF, PASSWORD);

        List<Project> projects = commonServer.listProjects(session.getSessionToken());

        assertEntities("[]", projects);
    }

    @Test
    public void testListProjectsWithProjectAdminWithProjectAuthorizationOn()
    {
        SessionContextDTO session = commonServer.tryAuthenticate(TEST_PROJECT_PA_ON, PASSWORD);

        List<Project> projects = commonServer.listProjects(session.getSessionToken());

        assertEntities("[/TEST-SPACE/TEST-PROJECT]", projects);
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

}
