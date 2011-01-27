/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.io.IOException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataStoreRegistrationTransactionTest extends AbstractFileSystemTestCase
{
    private DataSetRegistrationTransaction tr;

    @BeforeMethod
    @Override
    public void setUp() throws IOException
    {
        super.setUp();

        tr = new DataSetRegistrationTransaction();
    }

    @Test
    public void testRollback()
    {
        TrackingCommand cmd1 = new TrackingCommand();
        TrackingCommand cmd2 = new TrackingCommand(cmd1);

        tr.executeCommand(cmd1);
        tr.executeCommand(cmd2);
        assertEquals(TrackingCommandStatus.EXECUTED, cmd1.status);
        assertEquals(TrackingCommandStatus.EXECUTED, cmd2.status);

        tr.rollback();

        assertEquals(TrackingCommandStatus.ROLLEDBACK, cmd1.status);
        assertEquals(TrackingCommandStatus.ROLLEDBACK, cmd2.status);
        assertTrue(cmd2.rolledbackBeforePredecessor);
    }

    private static enum TrackingCommandStatus
    {
        PENDING_EXECUTE, EXECUTED, ROLLEDBACK
    }

    private static class TrackingCommand extends AbstractTransactionalCommand
    {
        private static final long serialVersionUID = 1L;

        private TrackingCommandStatus status = TrackingCommandStatus.PENDING_EXECUTE;

        private final TrackingCommand predecessor;

        private boolean rolledbackBeforePredecessor = false;

        private TrackingCommand()
        {
            this.predecessor = null;
        }

        private TrackingCommand(TrackingCommand predecessor)
        {
            this.predecessor = predecessor;
        }

        public void execute()
        {
            status = TrackingCommandStatus.EXECUTED;
        }

        public void rollback()
        {
            status = TrackingCommandStatus.ROLLEDBACK;
            rolledbackBeforePredecessor =
                    (predecessor != null) ? predecessor.status == TrackingCommandStatus.EXECUTED
                            : true;
        }
    }
}
