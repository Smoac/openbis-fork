/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.optimistic_locking;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.concurrent.MessageChannelBuilder;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.common.conversation.context.ServiceConversationsThreadContext;
import ch.systemsx.cisd.openbis.common.conversation.progress.IServiceConversationProgressListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.util.TimeIntervalChecker;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ProjectUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentTypeBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PropertyBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.AtomicEntityOperationDetailsBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * @author Franz-Josef Elmer
 */
public class ProjectOptimisticLockingTest extends OptimisticLockingTestCase
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ProjectOptimisticLockingTest.class);

    private static final String REGISTERED = "registered";

    private static final String FIRST_REGISTERED = "First registered";

    private static final String CREATE_EXPERIMENTS_PHASE = "createExperiments";

    @Test
    public void testCreateProject()
    {
        Project project = new Project();
        project.setCode("POLT-1");
        String identifier = "/" + space1.getCode() + "/POLT-1";
        project.setIdentifier(identifier);
        project.setDescription("ProjectOptimisticLockingTest test");
        project.setSpace(space1);
        logIntoCommonClientService();
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker(1);

        commonClientService.registerProject(SESSION_KEY, project);

        Project p =
                commonServer
                        .getProjectInfo(systemSessionToken, createProjectIdentifier(identifier));
        assertEquals(project.getDescription(), p.getDescription());
        assertEquals("test", p.getRegistrator().getUserId());
        assertEquals("test", p.getModifier().getUserId());
        timeIntervalChecker.assertDateInInterval(p.getRegistrationDate());
        timeIntervalChecker.assertDateInInterval(p.getModificationDate());
    }

    @Test
    public void testUpdateProjectAndCheckModificationDateAndModifier()
    {
        ProjectIdentifier projectIdentifier = createProjectIdentifier(project1.getIdentifier());
        Project p = commonServer.getProjectInfo(systemSessionToken, projectIdentifier);
        ProjectUpdates updates = new ProjectUpdates();
        updates.setVersion(p.getVersion());
        updates.setTechId(new TechId(p));
        updates.setDescription(p.getDescription() + " 2");
        updates.setAttachments(Collections.<NewAttachment> emptyList());
        updates.setAttachmentSessionKey(SESSION_KEY);
        logIntoCommonClientService();
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker(1);

        commonClientService.updateProject(updates);

        p = commonServer.getProjectInfo(systemSessionToken, projectIdentifier);
        assertEquals(project1.getDescription() + " 2", p.getDescription());
        assertEquals("system", p.getRegistrator().getUserId());
        assertEquals("test", p.getModifier().getUserId());
        timeIntervalChecker.assertDateInInterval(p.getModificationDate());

    }

    @Test
    public void testUpdateProjectWithOldVersion()
    {
        ProjectIdentifier projectIdentifier = createProjectIdentifier(project1.getIdentifier());
        Project currentProject = commonServer.getProjectInfo(systemSessionToken, projectIdentifier);
        ProjectUpdates updates = new ProjectUpdates();
        updates.setVersion(currentProject.getVersion());
        updates.setTechId(new TechId(currentProject));
        updates.setDescription(currentProject.getDescription() + " 1");
        updates.setAttachments(Collections.<NewAttachment> emptyList());
        updates.setAttachmentSessionKey(SESSION_KEY);
        logIntoCommonClientService();
        commonClientService.updateProject(updates);

        try
        {
            commonClientService.updateProject(updates);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Project has been modified in the meantime. Reopen tab to be able "
                    + "to continue with refreshed data.", ex.getMessage());
        }
    }

    @Test
    public void testRegisterExperimentAndCheckModificationDateAndModifierOfProject()
    {
        String sessionToken = logIntoCommonClientService().getSessionID();
        NewExperiment experiment =
                new NewExperiment(project1.getIdentifier() + "/POLT-1", EXPERIMENT_TYPE_CODE);
        experiment.setAttachments(Collections.<NewAttachment> emptyList());
        experiment.setProperties(new IEntityProperty[]
            { new PropertyBuilder("DESCRIPTION").value("hello").getProperty() });
        assertEquals("system", project1.getModifier().getUserId());
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker(1);

        genericServer.registerExperiment(sessionToken, experiment,
                Collections.<NewAttachment> emptyList());

        checkModifierAndModificationDateOfProject1(timeIntervalChecker);
    }

    @Test
    public void testRegisterExperiments()
    {
        assertEquals("system", project1.getModifier().getUserId());
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        builder.user(USER_ID);
        builder.experiment(experiment(1)).experiment(experiment(2));
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker(1);

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        List<Experiment> experiments =
                commonServer.listExperiments(systemSessionToken,
                        new ExperimentTypeBuilder().code(EXPERIMENT_TYPE_CODE).getExperimentType(),
                        createProjectIdentifier(project1.getIdentifier()));
        assertEquals("[OLT-E1, OLT-E2]", extractCodes(experiments).toString());
        checkModifierAndModificationDateOfProject1(timeIntervalChecker, USER_ID);
    }

    /*
     * This test registers three experiments for the same project. Two of them are registered by the
     * main thread using performEntityOperations(). A second thread registers an experiment between
     * the registration of the two other experiments. This is done by using a
     * IServiceConversationProgressListener together with two message channels to coordinate the
     * order of actions in both threads.
     */
    @Test
    public void testRegisterExperimentsInTwoThreads()
    {
        assertEquals("system", project1.getModifier().getUserId());
        final StringBuilder stringBuilder = new StringBuilder();
        final MessageChannel messageChannelMain =
                new MessageChannelBuilder(10000).name("main").logger(operationLog).getChannel();
        final MessageChannel messageChannelSecond =
                new MessageChannelBuilder(10000).name("second").logger(operationLog).getChannel();
        final IServiceConversationProgressListener listener =
                new AbstractServiceConversationProgressListener(operationLog)
                    {
                        @Override
                        public void handleProgress(String phaseName, int totalItemsToProcess,
                                int numItemsProcessed)
                        {
                            stringBuilder.append(phaseName).append(" ").append(numItemsProcessed)
                                    .append("/").append(totalItemsToProcess).append("\n");
                            if (phaseName.equals(CREATE_EXPERIMENTS_PHASE)
                                    && numItemsProcessed == 1 && totalItemsToProcess == 2)
                            {
                                messageChannelMain.send(FIRST_REGISTERED);
                            }
                        }
                    };
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker(1);

        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    NewExperiment experiment3 = experiment(3);
                    String sessionToken =
                            genericServer.tryToAuthenticate("test", "a").getSessionToken();
                    messageChannelMain.assertNextMessage(FIRST_REGISTERED);
                    genericServer.registerExperiment(sessionToken, experiment3,
                            Collections.<NewAttachment> emptyList());
                    messageChannelSecond.send(REGISTERED);
                }
            }).start();

        ServiceConversationsThreadContext.setProgressListener(listener);
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        builder.user(USER_ID);
        builder.experiment(experiment(1)).experiment(experiment(2));
        AtomicEntityOperationDetails details = builder.getDetails();

        etlService.performEntityOperations(systemSessionToken, details);

        messageChannelSecond.assertNextMessage(REGISTERED);

        List<Experiment> experiments =
                commonServer.listExperiments(systemSessionToken,
                        new ExperimentTypeBuilder().code(EXPERIMENT_TYPE_CODE).getExperimentType(),
                        createProjectIdentifier(project1.getIdentifier()));
        assertEquals("[OLT-E1, OLT-E2, OLT-E3]", extractCodes(experiments).toString());
        checkModifierAndModificationDateOfProject1(timeIntervalChecker);
        assertEquals("authorize 1/2\n" + "authorize 2/2\n" + "createExperiments 1/2\n"
                + "createExperiments 2/2\n", stringBuilder.toString());
    }

    private void checkModifierAndModificationDateOfProject1(TimeIntervalChecker timeIntervalChecker)
    {
        checkModifierAndModificationDateOfProject1(timeIntervalChecker, "test");
    }

    private void checkModifierAndModificationDateOfProject1(
            TimeIntervalChecker timeIntervalChecker, String modifier)
    {
        ProjectIdentifier projectIdentifier = createProjectIdentifier(project1.getIdentifier());
        Project p = commonServer.getProjectInfo(systemSessionToken, projectIdentifier);
        assertEquals("system", p.getRegistrator().getUserId());
        assertEquals(project1.getRegistrationDate(), p.getRegistrationDate());
        assertEquals(modifier, p.getModifier().getUserId());
        timeIntervalChecker.assertDateInInterval(p.getModificationDate());
    }

}
