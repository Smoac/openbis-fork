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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.etlserver.DispatcherStorageProcessor.IDispatchableStorageProcessor;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.IStorageProcessorTransaction;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Test of {@link DispatcherStorageProcessor}.
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses = DispatcherStorageProcessor.class)
public class DispatcherStorageProcessorTest extends AssertJUnit
{
    private Mockery context;

    private IDispatchableStorageProcessor dummyA, dummyB;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        dummyA = context.mock(IDispatchableStorageProcessor.class, "mock A");
        dummyB = context.mock(IDispatchableStorageProcessor.class, "mock B");
    }

    @AfterMethod
    public void afterMethod()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testDispatching()
    {
        DispatcherStorageProcessor dispatcher =
                new DispatcherStorageProcessor(Arrays.asList(dummyA, dummyB), new Properties());

        final DataSetInformation dataset1 = createDatasetInfo("a");
        final DataSetInformation dataset2 = createDatasetInfo("b");
        context.checking(new Expectations()
            {
                {
                    one(dummyA).accepts(dataset1, null);
                    will(returnValue(false));

                    one(dummyB).accepts(dataset1, null);
                    will(returnValue(true));
                    store(one(dummyB), dataset1);
                    one(dummyB).commit(null, null);

                    one(dummyA).accepts(dataset2, null);
                    will(returnValue(true));
                    store(one(dummyA), dataset2);
                    
                    one(dummyA).rollback(with(aNull(File.class)), with(aNull(File.class)),
                            with(any(Throwable.class)));
                }
            });

        storeTransactionally(dispatcher, dataset1).commit();
        storeTransactionally(dispatcher, dataset2).rollback(new Throwable());
    }

    @Test
    public void testStateErrorsAfterCommit()
    {
        DispatcherStorageProcessor dispatcher =
                new DispatcherStorageProcessor(Arrays.asList(dummyA, dummyB), new Properties());

        final DataSetInformation dataset1 = createDatasetInfo("a");
        context.checking(new Expectations()
            {
                {
                    allowing(dummyA).accepts(dataset1, null);
                    will(returnValue(true));

                    store(allowing(dummyA), dataset1);
                    commit(one(dummyA));
                    one(dummyA).rollback(with(aNull(File.class)), with(aNull(File.class)),
                            with(any(Throwable.class)));
                }
            });

        IStorageProcessorTransaction transaction1 = storeTransactionally(dispatcher, dataset1);
        transaction1.commit();
        assertTransactionNotUsable(transaction1);

        IStorageProcessorTransaction transaction2 = storeTransactionally(dispatcher, dataset1);
        transaction2.rollback(new Throwable());
        assertTransactionNotUsable(transaction2);
    }

    private void assertTransactionNotUsable(IStorageProcessorTransaction transaction)
    {
        String failMessage = "Transaction should not be usable after commit or rollback.";
        try
        {
            transaction.rollback(new Throwable());
            fail(failMessage);
        } catch (IllegalStateException ex)
        {
            // test passed
        }

        try
        {
            transaction.commit();
            fail(failMessage);
        } catch (IllegalStateException ex)
        {
            // test passed
        }
    }

    private void commit(IStorageProcessor storageProcessor)
    {
        storageProcessor.commit(null, null);
    }

    private void store(IStorageProcessor storageProcessor,
            final DataSetInformation dataset)
    {
        storageProcessor.storeData(dataset, null, null, null, null);
    }

    private IStorageProcessorTransaction storeTransactionally(
            IStorageProcessorTransactional storageProcessor, final DataSetInformation dataset)
    {
        IStorageProcessorTransaction transaction = storageProcessor.createTransaction();
        transaction.storeData(dataset, null, null, null, null);
        return transaction;
    }

    private static DataSetInformation createDatasetInfo(String datasetCode)
    {
        DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setDataSetCode(datasetCode);
        return dataSetInformation;
    }
}
