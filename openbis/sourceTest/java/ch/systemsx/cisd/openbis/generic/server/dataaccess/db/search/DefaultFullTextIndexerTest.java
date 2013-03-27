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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search;

import java.util.Arrays;

import org.apache.log4j.Level;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hibernate.Criteria;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.SearchFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.internal.NamedSequence;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author Franz-Josef Elmer
 */
public class DefaultFullTextIndexerTest extends AssertJUnit
{
    private BufferedAppender logRecorder;

    private Mockery context;

    private Criteria criteria;

    private DefaultFullTextIndexer indexer;

    private FullTextSession session;

    private Transaction transaction;

    private NamedSequence criteriaListSequence;

    private SearchFactory searchFactory;

    private SamplePE exampleEntity;

    @BeforeMethod
    public final void setUp()
    {
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.INFO);
        context = new Mockery();
        criteria = context.mock(Criteria.class);
        session = context.mock(FullTextSession.class);
        transaction = context.mock(Transaction.class);
        searchFactory = context.mock(SearchFactory.class);
        indexer = new DefaultFullTextIndexer(2);
        criteriaListSequence = new NamedSequence("criteria.list");
        exampleEntity = new SamplePE();
        exampleEntity.setCode("1");
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testDoFullTextIndexForNone()
    {
        prepareBeginTransaction();
        prepareGetAllIds();
        prepareCommit();

        indexer.doFullTextIndex(session, SamplePE.class);

        assertEquals("INFO  OPERATION.DefaultFullTextIndexer - ... got 0 'SamplePE' ids...\n"
                + "INFO  OPERATION.DefaultFullTextIndexer - "
                + "'SamplePE' index complete. 0 entities have been indexed.",
                logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testDoFullTextIndexInOneBatchesForOne()
    {
        prepareBeginTransaction();
        prepareGetAllIds(42L);
        prepareListEntities(42L, Integer.MAX_VALUE);
        prepareIndexEntities();
        prepareCommit();

        indexer.doFullTextIndex(session, SamplePE.class);

        assertEquals("INFO  OPERATION.DefaultFullTextIndexer - ... got 1 'SamplePE' ids...\n"
                + "INFO  OPERATION.DefaultFullTextIndexer - "
                + "1/1 SamplePEs have been indexed...\n"
                + "INFO  OPERATION.DefaultFullTextIndexer - "
                + "'SamplePE' index complete. 1 entities have been indexed.",
                logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testDoFullTextIndexInOneBatchesForTwo()
    {
        prepareBeginTransaction();
        prepareGetAllIds(42L, 43L);
        prepareListEntities(42L, Integer.MAX_VALUE);
        prepareIndexEntities();
        prepareCommit();

        indexer.doFullTextIndex(session, SamplePE.class);

        assertEquals("INFO  OPERATION.DefaultFullTextIndexer - ... got 2 'SamplePE' ids...\n"
                + "INFO  OPERATION.DefaultFullTextIndexer - "
                + "2/2 SamplePEs have been indexed...\n"
                + "INFO  OPERATION.DefaultFullTextIndexer - "
                + "'SamplePE' index complete. 2 entities have been indexed.",
                logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testDoFullTextIndexInTwoBatchesWithThree()
    {
        prepareBeginTransaction();
        prepareGetAllIds(42L, 43L, 50L);
        prepareListEntities(42L, 50L);
        prepareIndexEntities();
        prepareListEntities(50L, Integer.MAX_VALUE);
        prepareIndexEntities();
        prepareCommit();

        indexer.doFullTextIndex(session, SamplePE.class);

        assertEquals("INFO  OPERATION.DefaultFullTextIndexer - ... got 3 'SamplePE' ids...\n"
                + "INFO  OPERATION.DefaultFullTextIndexer - "
                + "2/3 SamplePEs have been indexed...\n"
                + "INFO  OPERATION.DefaultFullTextIndexer - "
                + "3/3 SamplePEs have been indexed...\n"
                + "INFO  OPERATION.DefaultFullTextIndexer - "
                + "'SamplePE' index complete. 3 entities have been indexed.",
                logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testDoFullTextIndexInTwoBatchesWithFour()
    {
        prepareBeginTransaction();
        prepareGetAllIds(42L, 43L, 50L, 52L);
        prepareListEntities(42L, 50L);
        prepareIndexEntities();
        prepareListEntities(50L, Integer.MAX_VALUE);
        prepareIndexEntities();
        prepareCommit();

        indexer.doFullTextIndex(session, SamplePE.class);

        assertEquals("INFO  OPERATION.DefaultFullTextIndexer - ... got 4 'SamplePE' ids...\n"
                + "INFO  OPERATION.DefaultFullTextIndexer - "
                + "2/4 SamplePEs have been indexed...\n"
                + "INFO  OPERATION.DefaultFullTextIndexer - "
                + "4/4 SamplePEs have been indexed...\n"
                + "INFO  OPERATION.DefaultFullTextIndexer - "
                + "'SamplePE' index complete. 4 entities have been indexed.",
                logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    private void prepareBeginTransaction()
    {
        context.checking(new Expectations()
            {
                {
                    one(session).beginTransaction();
                    will(returnValue(transaction));
                }
            });
    }

    private void prepareGetAllIds(final Long... ids)
    {
        context.checking(new Expectations()
            {
                {
                    one(session).createCriteria(SamplePE.class);
                    will(returnValue(criteria));

                    one(criteria).setProjection(with(any(Projection.class)));
                    will(returnValue(criteria));

                    one(criteria).addOrder(with(any(Order.class)));
                    will(returnValue(criteria));

                    one(criteria).list();
                    will(returnValue(Arrays.asList(ids)));
                    inSequence(criteriaListSequence);
                }
            });
    }

    private void prepareListEntities(final long minId, final long maxId)
    {
        context.checking(new Expectations()
            {
                {
                    one(session).createCriteria(SamplePE.class);
                    will(returnValue(criteria));

                    one(criteria).add(with(new BaseMatcher<Criterion>()
                        {
                            @Override
                            public boolean matches(Object item)
                            {
                                assertEquals("id>=" + minId, String.valueOf(item));
                                return true;
                            }

                            @Override
                            public void describeTo(Description description)
                            {
                                description.appendText(">= " + minId);
                            }
                        }));
                    will(returnValue(criteria));

                    one(criteria).add(with(new BaseMatcher<Criterion>()
                        {
                            @Override
                            public boolean matches(Object item)
                            {
                                assertEquals("id<" + maxId, String.valueOf(item));
                                return true;
                            }

                            @Override
                            public void describeTo(Description description)
                            {
                                description.appendText("< " + maxId);
                            }
                        }));
                    will(returnValue(criteria));

                    one(criteria).list();
                    will(returnValue(Arrays.asList(exampleEntity)));
                    inSequence(criteriaListSequence);
                }
            });
    }

    private void prepareIndexEntities()
    {
        context.checking(new Expectations()
            {
                {
                    one(session).index(exampleEntity);
                    one(session).flushToIndexes();
                    one(session).clear();
                }
            });
    }

    private void prepareCommit()
    {
        context.checking(new Expectations()
            {
                {
                    one(session).getSearchFactory();
                    will(returnValue(searchFactory));

                    one(searchFactory).optimize(SamplePE.class);
                    one(transaction).commit();
                }
            });
    }
}
