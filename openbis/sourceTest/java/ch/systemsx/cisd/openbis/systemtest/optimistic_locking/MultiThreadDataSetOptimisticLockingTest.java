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

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.concurrent.MessageChannelBuilder;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.common.conversation.context.ServiceConversationsThreadContext;
import ch.systemsx.cisd.openbis.common.conversation.progress.IServiceConversationProgressListener;
import ch.systemsx.cisd.openbis.generic.server.util.TimeIntervalChecker;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.AtomicEntityOperationDetailsBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class MultiThreadDataSetOptimisticLockingTest extends MultiThreadOptimisticLockingTestCase
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MultiThreadDataSetOptimisticLockingTest.class);

    @Test
    public void testRegisterChildDataSetsForAParentDataSetInTwoThreads()
    {
        final Experiment experiment = toolBox.createAndLoadExperiment(1);
        final ExternalData parentDataSet =
                toolBox.createAndLoadDataSet(toolBox.dataSet("DS-PARENT", experiment));
        final MessageChannel messageChannelMain =
                new MessageChannelBuilder(10000).name("child data sets main").logger(operationLog)
                        .getChannel();
        final MessageChannel messageChannelSecond =
                new MessageChannelBuilder(10000).name("child data sets second")
                        .logger(operationLog).getChannel();
        final IServiceConversationProgressListener listener =
                new AbstractServiceConversationProgressListener(operationLog)
                    {
                        @Override
                        public void handleProgress(String phaseName, int totalItemsToProcess,
                                int numItemsProcessed)
                        {
                            logger.info(phaseName + " " + numItemsProcessed + "/"
                                    + totalItemsToProcess);
                            if (phaseName.equals("createDataSets") && numItemsProcessed == 1
                                    && totalItemsToProcess == 2)
                            {
                                messageChannelMain.send(ToolBox.FIRST_REGISTERED);
                            }
                        }
                    };
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();
        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    String sessionToken =
                            genericServer.tryAuthenticate("test", "a").getSessionToken();
                    NewDataSet child3 = toolBox.dataSet("DS-CHILD3", experiment);
                    child3.setParentDataSetCodes(Arrays.asList(parentDataSet.getCode()));
                    AtomicEntityOperationDetailsBuilder builder =
                            new AtomicEntityOperationDetailsBuilder().dataSet(child3);
                    messageChannelMain.assertNextMessage(ToolBox.FIRST_REGISTERED);
                    etlService.performEntityOperations(sessionToken, builder.getDetails());
                    messageChannelSecond.send(ToolBox.REGISTERED);
                }
            }).start();
        ServiceConversationsThreadContext.setProgressListener(listener);
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        builder.user(ToolBox.USER_ID).batchSize(1);
        NewDataSet child1 = toolBox.dataSet("DS-CHILD1", experiment);
        child1.setParentDataSetCodes(Arrays.asList(parentDataSet.getCode()));
        NewDataSet child2 = toolBox.dataSet("DS-CHILD2", experiment);
        child2.setParentDataSetCodes(Arrays.asList(parentDataSet.getCode()));
        builder.dataSet(child1).dataSet(child2);

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());
        messageChannelSecond.assertNextMessage(ToolBox.REGISTERED);

        ExternalData loadedParentDataSet = toolBox.loadDataSet(parentDataSet.getCode());
        assertEquals(experiment.getIdentifier(), loadedParentDataSet.getExperiment()
                .getIdentifier());
        assertEquals("[DS-CHILD1, DS-CHILD2, DS-CHILD3]",
                toolBox.extractCodes(loadedParentDataSet.getChildren()));
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedParentDataSet,
                "test");
    }

}
