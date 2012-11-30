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

package ch.systemsx.cisd.etlserver.registrator.api.impl;

import org.testng.annotations.Test;

import ch.systemsx.cisd.etlserver.registrator.api.RollbackStack;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.AbstractTestWithRollbackStack;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.AbstractTransactionalCommand;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class RollbackStackTest extends AbstractTestWithRollbackStack
{

    @Test
    public void testRollback()
    {
        // Create some commands
        TrackingCommand cmd1 = new TrackingCommand();
        TrackingCommand cmd2 = new TrackingCommand(cmd1);

        // Add them to the stack
        rollbackStack.pushAndExecuteCommand(cmd1);
        rollbackStack.pushAndExecuteCommand(cmd2);
        assertEquals(TrackingCommandStatus.EXECUTED, cmd1.status);
        assertEquals(TrackingCommandStatus.EXECUTED, cmd2.status);

        assertEquals(
                "RollbackStack[{StackElement [command=TrackingCommand [status=EXECUTED], order=0],StackElement [command=TrackingCommand [status=EXECUTED], order=1]}]",
                rollbackStack.toString());

        // Rollback and check that the rollback occurred correctly
        rollbackStack.rollbackAll();
        assertEquals(TrackingCommandStatus.ROLLEDBACK, cmd1.status);
        assertEquals(TrackingCommandStatus.ROLLEDBACK, cmd2.status);
        assertTrue(cmd2.rolledbackBeforePredecessor);
        assertEquals(cmd2.predecessor, cmd1);
    }

    @Test
    public void testCantRollbackIfLocked()
    {
        // Create some commands
        TrackingCommand cmd1 = new TrackingCommand();
        TrackingCommand cmd2 = new TrackingCommand(cmd1);

        // Add them to the stack
        rollbackStack.pushAndExecuteCommand(cmd1);
        rollbackStack.pushAndExecuteCommand(cmd2);
        assertEquals(TrackingCommandStatus.EXECUTED, cmd1.status);
        assertEquals(TrackingCommandStatus.EXECUTED, cmd2.status);

        assertEquals(
                "RollbackStack[{StackElement [command=TrackingCommand [status=EXECUTED], order=0],StackElement [command=TrackingCommand [status=EXECUTED], order=1]}]",
                rollbackStack.toString());

        rollbackStack.setLockedState(true);

        try
        {
            rollbackStack.rollbackAll();
            fail("The rollbackAll should fail when the rollback stack is in locked state");
        } catch (Exception ex)
        {
            // should catch the exception, because the rollback stack is in the locked state
        }

        assertEquals(TrackingCommandStatus.EXECUTED, cmd1.status);
        assertEquals(TrackingCommandStatus.EXECUTED, cmd2.status);

        rollbackStack.setLockedState(false);

        // Rollback and check that the rollback occurred correctly
        rollbackStack.rollbackAll();

        assertEquals(TrackingCommandStatus.ROLLEDBACK, cmd1.status);
        assertEquals(TrackingCommandStatus.ROLLEDBACK, cmd2.status);
        assertTrue(cmd2.rolledbackBeforePredecessor);
        assertEquals(cmd2.predecessor, cmd1);
    }

    @Test
    public void testResume()
    {
        // Create some commands
        TrackingCommand cmd1 = new TrackingCommand();
        TrackingCommand cmd2 = new TrackingCommand(cmd1);
        TrackingCommand cmd3 = new TrackingCommand(cmd2);

        // Add them to the stack
        rollbackStack.pushAndExecuteCommand(cmd1);
        rollbackStack.pushAndExecuteCommand(cmd2);
        rollbackStack.pushAndExecuteCommand(cmd3);
        assertEquals(TrackingCommandStatus.EXECUTED, cmd1.status);
        assertEquals(TrackingCommandStatus.EXECUTED, cmd2.status);
        assertEquals(TrackingCommandStatus.EXECUTED, cmd3.status);

        // Rollback one
        rollbackStack.rollbackAndPop();
        assertEquals(TrackingCommandStatus.EXECUTED, cmd1.status);
        assertEquals(TrackingCommandStatus.EXECUTED, cmd2.status);
        assertEquals(TrackingCommandStatus.ROLLEDBACK, cmd3.status);
        assertTrue(cmd3.rolledbackBeforePredecessor);

        // Recreate the rollback stack from the files
        rollbackStack = new RollbackStack(queue1File, queue2File);
        assertEquals(2, rollbackStack.size());

        // Continue rolling back
        cmd2 = (TrackingCommand) rollbackStack.rollbackAndPop();
        assertEquals(TrackingCommandStatus.ROLLEDBACK, cmd2.status);
        assertTrue(cmd2.rolledbackBeforePredecessor);

        cmd1 = (TrackingCommand) rollbackStack.rollbackAndPop();
        assertEquals(TrackingCommandStatus.ROLLEDBACK, cmd1.status);
    }

    @Test
    public void testEquality()
    {
        // Create some commands
        TrackingCommand cmd1 = new EqualityTrackingCommand(0);
        TrackingCommand cmd2 = new EqualityTrackingCommand(cmd1, 1);
        TrackingCommand cmd3 = new EqualityTrackingCommand(cmd2, 2);

        // Add them to the stack
        rollbackStack.pushAndExecuteCommand(cmd1);
        rollbackStack.pushAndExecuteCommand(cmd2);
        rollbackStack.pushAndExecuteCommand(cmd3);
        assertEquals(TrackingCommandStatus.EXECUTED, cmd1.status);
        assertEquals(TrackingCommandStatus.EXECUTED, cmd2.status);
        assertEquals(TrackingCommandStatus.EXECUTED, cmd3.status);

        // Recreate the rollback stack from the files
        rollbackStack = new RollbackStack(queue1File, queue2File);
        assertEquals(3, rollbackStack.size());

        // Continue rolling back
        cmd3 = (TrackingCommand) rollbackStack.rollbackAndPop();
        assertEquals(TrackingCommandStatus.ROLLEDBACK, cmd3.status);
        assertTrue(cmd3.rolledbackBeforePredecessor);

        // Continue rolling back
        cmd2 = (TrackingCommand) rollbackStack.rollbackAndPop();
        assertEquals(TrackingCommandStatus.ROLLEDBACK, cmd2.status);
        assertTrue(cmd2.rolledbackBeforePredecessor);
        assertEquals(cmd3.predecessor, cmd2);
        assertFalse(cmd3.predecessor == cmd2);

    }

    @SuppressWarnings("deprecation")
    @Test
    public void testResumeWithInterruption()
    {
        // Create some commands
        TrackingCommand cmd1 = new TrackingCommand();
        TrackingCommand cmd2 = new TrackingCommand(cmd1);
        TrackingCommand cmd3 = new TrackingCommand(cmd2);

        // Add them to the stack
        rollbackStack.pushAndExecuteCommand(cmd1);
        rollbackStack.pushAndExecuteCommand(cmd2);
        rollbackStack.pushAndExecuteCommand(cmd3);
        assertEquals(TrackingCommandStatus.EXECUTED, cmd1.status);
        assertEquals(TrackingCommandStatus.EXECUTED, cmd2.status);
        assertEquals(TrackingCommandStatus.EXECUTED, cmd3.status);

        // Simulate beginning, but not completing, a rollback step
        rollbackStack.doNotCallJustForTesting_MoveOneElement();
        assertEquals(TrackingCommandStatus.EXECUTED, cmd1.status);
        assertEquals(TrackingCommandStatus.EXECUTED, cmd2.status);
        assertEquals(TrackingCommandStatus.EXECUTED, cmd3.status);

        // Reinitialize the stack from the file
        rollbackStack = new RollbackStack(queue1File, queue2File);
        assertEquals(3, rollbackStack.size());

        // Finish the rollback
        cmd3 = (TrackingCommand) rollbackStack.rollbackAndPop();
        assertEquals(TrackingCommandStatus.ROLLEDBACK, cmd3.status);

        cmd2 = (TrackingCommand) rollbackStack.rollbackAndPop();
        assertEquals(TrackingCommandStatus.ROLLEDBACK, cmd2.status);

        cmd1 = (TrackingCommand) rollbackStack.rollbackAndPop();
        assertEquals(TrackingCommandStatus.ROLLEDBACK, cmd1.status);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testResumeWithInterruptionDuringMove()
    {
        // Create some commands
        TrackingCommand cmd1 = new TrackingCommand();
        TrackingCommand cmd2 = new TrackingCommand(cmd1);
        TrackingCommand cmd3 = new TrackingCommand(cmd2);

        // Add them to the stack
        rollbackStack.pushAndExecuteCommand(cmd1);
        rollbackStack.pushAndExecuteCommand(cmd2);
        rollbackStack.pushAndExecuteCommand(cmd3);
        assertEquals(TrackingCommandStatus.EXECUTED, cmd1.status);
        assertEquals(TrackingCommandStatus.EXECUTED, cmd2.status);
        assertEquals(TrackingCommandStatus.EXECUTED, cmd3.status);

        // Simulate beginning, but not completing, a rollback step
        rollbackStack.doNotCallJustForTesting_MoveOneElement();
        rollbackStack.doNotCallJustForTesting_PartiallyMoveOneElement();
        assertEquals(TrackingCommandStatus.EXECUTED, cmd1.status);
        assertEquals(TrackingCommandStatus.EXECUTED, cmd2.status);
        assertEquals(TrackingCommandStatus.EXECUTED, cmd3.status);

        // Reinitialize the stack from the file
        rollbackStack = new RollbackStack(queue1File, queue2File);
        // One command should have been duplicated -- this is ok
        assertEquals(4, rollbackStack.size());

        // Finish the rollback
        cmd3 = (TrackingCommand) rollbackStack.rollbackAndPop();
        assertEquals(TrackingCommandStatus.ROLLEDBACK, cmd3.status);

        cmd2 = (TrackingCommand) rollbackStack.rollbackAndPop();
        assertEquals(TrackingCommandStatus.ROLLEDBACK, cmd2.status);

        cmd1 = (TrackingCommand) rollbackStack.rollbackAndPop();
        assertEquals(TrackingCommandStatus.ROLLEDBACK, cmd1.status);

        TrackingCommand cmdN = (TrackingCommand) rollbackStack.rollbackAndPop();
        assertEquals(TrackingCommandStatus.ROLLEDBACK, cmdN.status);
    }

    @Test
    public void testIncreasingSerializedCommandSize()
    {
        // Create some commands
        // Add them to the stack
        for (int i = 0; i < 129; ++i)
        {
            StringTrackingCommand cmd = new StringTrackingCommand(createStringOfLength(i));
            rollbackStack.pushAndExecuteCommand(cmd);
            assertEquals(TrackingCommandStatus.EXECUTED, cmd.status);
        }

        // Rollback and check that the rollback occurred correctly
        rollbackStack.rollbackAll();
    }

    private String createStringOfLength(int length)
    {
        String item = "";
        for (int j = 0; j < length; ++j)
        {
            item += "1";
        }
        return item;
    }

    private static enum TrackingCommandStatus
    {
        PENDING_EXECUTE, EXECUTED, ROLLEDBACK
    }

    private static class TrackingCommand extends AbstractTransactionalCommand
    {
        private static final long serialVersionUID = 1L;

        protected TrackingCommandStatus status = TrackingCommandStatus.PENDING_EXECUTE;

        private final TrackingCommand predecessor;

        private boolean rolledbackBeforePredecessor = false;

        protected TrackingCommand()
        {
            this.predecessor = null;
        }

        protected TrackingCommand(TrackingCommand predecessor)
        {
            this.predecessor = predecessor;
        }

        @Override
        public void execute()
        {
            status = TrackingCommandStatus.EXECUTED;
        }

        @Override
        public void rollback()
        {
            status = TrackingCommandStatus.ROLLEDBACK;
            rolledbackBeforePredecessor =
                    (predecessor != null) ? predecessor.status == TrackingCommandStatus.EXECUTED
                            : true;
        }

        @Override
        public String toString()
        {
            return "TrackingCommand [status=" + status + "]";
        }

    }

    private static class EqualityTrackingCommand extends TrackingCommand
    {
        private static final long serialVersionUID = 1L;

        private final int id;

        private EqualityTrackingCommand(int id)
        {
            super();
            this.id = id;
        }

        protected EqualityTrackingCommand(TrackingCommand predecessor, int id)
        {
            super(predecessor);
            this.id = id;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + id;
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            EqualityTrackingCommand other = (EqualityTrackingCommand) obj;
            if (id != other.id)
                return false;
            return true;
        }

        @Override
        public String toString()
        {
            return "EqualityTrackingCommand [id=" + id + ",status=" + status + "]";
        }

    }

    private static class StringTrackingCommand extends TrackingCommand
    {
        private static final long serialVersionUID = 1L;

        private final String text;

        private StringTrackingCommand(String text)
        {
            super();
            this.text = text;
        }

        @Override
        public int hashCode()
        {
            return text.hashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            StringTrackingCommand other = (StringTrackingCommand) obj;
            return text.equals(other.text);
        }

        @Override
        public String toString()
        {
            return "StringTrackingCommand [text=" + text + ", status=" + status + "]";
        }
    }
}
