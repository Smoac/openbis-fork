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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.collection.SimpleComparator;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.registrator.IRollbackStack;
import ch.systemsx.cisd.etlserver.registrator.ITransactionalCommand;

/**
 * A package internal class to manage the rollback stack.
 * <p>
 * Since there is no persistent stack class, the rollback stack is implemented with two queues, optimized for pushing onto the stack (poping from the
 * stack is slower).
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class RollbackStack implements IRollbackStack
{
    private static final String ELEMENT_FILE_PREFIX = "element-";
    private static final String ELEMENT_FILE_TYPE = ".ser";
    private static final String ELEMENT_FILE_FORMAT = ELEMENT_FILE_PREFIX + "%07d" + ELEMENT_FILE_TYPE;

    /**
     * Delegate methods for the rollback stack, giving clients of the stack control over its behavior.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static interface IRollbackStackDelegate
    {

        /**
         * Informs clients that the stack will rollback another item. Implementations may throw exceptions or block the thread until the stack can
         * continue.
         */
        void willContinueRollbackAll(RollbackStack stack);
    }

    // The files that store the persistent queue. Used for discarding the queues.
    private final File queue1File;

    private final File queue2File;

    private final File lockedMarkerFile;

    private final Logger operationLog;

    private int size;

    /**
     * Constructor for a rollback stack that uses queue1File and queue2File for the persistent queues.
     * 
     * @param queue1File File for a persistent queue
     * @param queue2File File for a persistent queue
     */
    public RollbackStack(File queue1File, File queue2File)
    {
        this(queue1File, queue2File, null);
    }

    public RollbackStack(File queue1File, File queue2File, Logger operationLog)
    {
        this.queue1File = queue1File;
        this.queue2File = queue2File;

        if (operationLog == null)
        {
            this.operationLog = LogFactory.getLogger(LogCategory.OPERATION, RollbackStack.class);
        } else
        {
            this.operationLog = operationLog;
        }

        this.lockedMarkerFile =
                new File(queue1File.getParentFile(), queue1File.getName() + ".LOCKED");
        queue1File.mkdirs();
        size = getElementFiles().size();
    }

    private List<File> getElementFiles()
    {
        
        List<File> elementFiles = new ArrayList<File>();
        File[] files = this.queue1File.listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    return name.startsWith(ELEMENT_FILE_PREFIX) && name.endsWith(ELEMENT_FILE_TYPE);
                }
            });
        if (files != null && files.length > 0)
        {
            elementFiles.addAll(Arrays.asList(files));
        }
        Collections.sort(elementFiles, new SimpleComparator<File, String>()
            {
                @Override
                public String evaluate(File item)
                {
                    return item.getName();
                }
            });
        return elementFiles;
    }

    /**
     * The size of the rollback stack
     */
    public int getSize()
    {
        return getElementFiles().size();
    }

    /**
     * Push the command onto the stack and execute it.
     */
    @Override
    public void pushAndExecuteCommand(ITransactionalCommand cmd)
    {
        File file = new File(queue1File, String.format(ELEMENT_FILE_FORMAT, size));
        FileUtilities.writeToFile(file, new StackElement(cmd, size));
        size++;
        cmd.execute();
    }

    /**
     * Rollback any commands that have been executed. Rollback is done in the reverse order of execution.
     */
    public void rollbackAll()
    {
        rollbackAll(new IRollbackStackDelegate()
            {
                @Override
                public void willContinueRollbackAll(RollbackStack stack)
                {
                    // Don't do anything
                }
            });
    }

    /**
     * Rollback any commands that have been executed. Rollback is done in the reverse order of execution.
     */
    public void rollbackAll(IRollbackStackDelegate delegate)
    {
        if (isLockedState())
        {
            throw new IllegalStateException(
                    "Rollback stack is in the locked state. Triggering rollback forbidden.");
        }
        List<File> elementFiles = getElementFiles();
        if (elementFiles.isEmpty() == false)
        {
            int numberOfElements = elementFiles.size();
            operationLog.info("Rolling back " + numberOfElements + " commands");
            for (int i = numberOfElements - 1; i >= 0; i--)
            {
                File file = elementFiles.get(i);
                StackElement stackElement = FileUtilities.loadToObject(file, StackElement.class);
                try
                {
                    // Roll it back
                    stackElement.command.rollback();
                } catch (Throwable ex)
                {
                    // If any problems happen rolling back a command, log them
                    operationLog.error("Encountered error rolling back command " + stackElement.toString(), ex);
                }
                file.delete();
                delegate.willContinueRollbackAll(this);
                if (numberOfElements - i > 1 && (numberOfElements - i) % 100 == 0)
                {
                    operationLog.info((numberOfElements - i) + " commands rolled back");
                }
            }
        }
    }

    /**
     * Throws away the persistent stack.
     */
    public void discard()
    {
        if (isLockedState())
        {
            throw new IllegalStateException(
                    "Discarding of locked rollback stack is illegal. Set locked to false first.");
        }
        FileUtilities.deleteRecursively(queue1File);
    }
    
    @Override
    public String toString()
    {
        List<File> elementFiles = getElementFiles();
        return "RollbackStack " + queue1File + " with " + elementFiles.size() + " commands to roll back";
    }

    /**
     * Internal getter for clients that need to serialize the rollback stack. Use this method with caution since it exposes implementation details.
     */
    public File[] getBackingFiles()
    {
        return new File[]
        { queue1File, queue2File };
    }

    @Override
    public void setLockedState(boolean lockedState)
    {
        if (!lockedState && isLockedState())
        {
            deleteLockedMarkerFile();
        } else if (lockedState && false == isLockedState())
        {
            createLockedMarkerFile();
        }
    }

    @Override
    public boolean isLockedState()
    {
        return lockedMarkerFile.exists();
    }

    private void deleteLockedMarkerFile()
    {
        lockedMarkerFile.delete();
    }

    private void createLockedMarkerFile()
    {
        try
        {
            lockedMarkerFile.createNewFile();
        } catch (IOException ex)
        {
            operationLog.fatal("Failed to create rollback stack lock marker file "
                    + lockedMarkerFile.getAbsolutePath());
        }
    }

    /**
     * A stack element combines the command with an order. The order is used to implement the ordering in the queue.
     * <p>
     * The queue of rollback actions should be LIFO (a stack), but there is no LIFO queue in the available libraries that we can use for this purpose.
     * Thus, we use a priority queue, which is available, and define the priority comparison operator such that it results in a LIFO queue.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    protected static class StackElement implements Serializable, Comparable<StackElement>
    {
        private static final long serialVersionUID = 1L;

        private final ITransactionalCommand command;

        private final int order;

        protected StackElement(ITransactionalCommand command, int order)
        {
            this.command = command;
            this.order = order;
        }

        @Override
        public int compareTo(StackElement o)
        {
            // The order should be the reverse of the step order (later steps should come first in
            // the ordering).
            if (o.order < this.order)
            {
                return -1;
            } else if (o.order > this.order)
            {
                return 1;
            } else
            {
                return 0;
            }
        }

        @Override
        public String toString()
        {
            return "StackElement [command=" + command + ", order=" + order + "]";
        }

    }


}
