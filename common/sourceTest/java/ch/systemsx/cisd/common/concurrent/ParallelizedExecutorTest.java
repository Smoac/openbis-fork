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

package ch.systemsx.cisd.common.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;

/**
 * Tests of {@link ParallelizedExecutor}.
 * 
 * @author Tomasz Pylak
 */
public class ParallelizedExecutorTest extends AssertJUnit
{
    @Test
    public void testAllExecuted()
    {
        int itemsNum = 20;
        final boolean executed[] = new boolean[itemsNum];
        List<Integer> items = createTaskItems(itemsNum);
        ITaskExecutor<Integer> taskExecutor = new ITaskExecutor<Integer>()
            {
                public Status execute(Integer item)
                {
                    if (executed[item])
                    {
                        fail("Invalid attempt to perform job on the same item twice: item " + item);
                    }
                    work(item, 10);
                    executed[item] = true;
                    return Status.OK;
                }
            };
        Collection<FailureRecord<Integer>> errors = process(items, taskExecutor);
        assertEquals(0, errors.size());
        assertAllExecuted(executed);
    }

    @Test
    public void testFailuresReported()
    {
        List<Integer> items = createTaskItems(100);
        items.add(0); // item 0 occurs twice
        ITaskExecutor<Integer> taskExecutor = new ITaskExecutor<Integer>()
            {
                public Status execute(Integer item)
                {
                    work(item, 10);
                    if (item.intValue() == 0)
                    {
                        return Status.createError();
                    } else
                    {
                        return Status.OK;
                    }
                }
            };
        Collection<FailureRecord<Integer>> errors = process(items, taskExecutor);
        assertEquals(2, errors.size());
        for (FailureRecord<Integer> error : errors)
        {
            assertEquals(0, error.getFailedItem().intValue());
        }
    }

    private static void work(Integer item, int timeMsec)
    {
        try
        {
            synchronized (item)
            {
                // System.out.println("working on "+item);
                item.wait(timeMsec);
            }
        } catch (InterruptedException ex)
        {
        }
    }

    private Collection<FailureRecord<Integer>> process(List<Integer> items,
            ITaskExecutor<Integer> taskExecutor)
    {
        return ParallelizedExecutor.process(items, taskExecutor, 10, 10, "test", 1);
    }

    private static void assertAllExecuted(final boolean[] executed)
    {
        for (int i = 0; i < executed.length; i++)
        {
            if (executed[i] == false)
            {
                fail("Job not executed for item " + i);
            }
        }
    }

    private static List<Integer> createTaskItems(int itemsNum)
    {
        List<Integer> items = new ArrayList<Integer>();
        for (int i = 0; i < itemsNum; i++)
        {
            items.add(i);
        }
        return items;
    }
}
