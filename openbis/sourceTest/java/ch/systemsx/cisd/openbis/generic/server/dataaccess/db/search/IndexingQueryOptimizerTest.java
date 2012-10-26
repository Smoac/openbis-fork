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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

import java.util.Set;

import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author anttil
 */

public class IndexingQueryOptimizerTest
{

    private Mockery context;

    private Criteria criteria;

    @BeforeMethod
    public final void setUp()
    {
        context = new Mockery();
        criteria = context.mock(Criteria.class);
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void returnsSameCriteriaInstanceThanWasGivenAsAParameter() throws Exception
    {

        class Entity
        {
            @ManyToOne(fetch = FetchType.EAGER)
            public Object getContent()
            {
                return null;
            }
        }
        context.checking(new Expectations()
            {
                {
                    allowing(anything());
                }
            });

        Criteria returnedCriteria =
                IndexingQueryOptimizer.minimizeAmountOfSubqueries(Entity.class, criteria);
        assertThat(returnedCriteria, is(sameInstance(criteria)));
    }

    @Test
    public void setsFetchModeToJoinForAllIndexedEmbeddedAssociations() throws Exception
    {
        class InnerContent
        {
        }

        class OuterContent
        {
            @IndexedEmbedded
            public InnerContent getInnerContent()
            {
                return null;
            }
        }

        class Entity
        {
            @IndexedEmbedded
            public OuterContent getContent()
            {
                return null;
            }
        }
        context.checking(new Expectations()
            {
                {
                    oneOf(criteria).setFetchMode("content", FetchMode.JOIN);
                    oneOf(criteria).setFetchMode("content.innerContent", FetchMode.JOIN);
                }
            });

        IndexingQueryOptimizer.minimizeAmountOfSubqueries(Entity.class, criteria);
    }

    @Test
    public void setsFetchModeToJoinForAllAssociationsWithEagerFetching() throws Exception
    {

        final class LazyEagerEntity
        {
            @OneToOne(fetch = FetchType.LAZY)
            public Object getOneToOneLazily()
            {
                return null;
            }

            @OneToOne(fetch = FetchType.EAGER)
            public Object getOneToOneEagerly()
            {
                return null;
            }

            @OneToMany(fetch = FetchType.LAZY)
            public Object getOneToManyLazily()
            {
                return null;
            }

            @OneToMany(fetch = FetchType.EAGER)
            public Object getOneToManyEagerly()
            {
                return null;
            }

            @ManyToOne(fetch = FetchType.LAZY)
            public Object getManyToOneLazily()
            {
                return null;
            }

            @ManyToOne(fetch = FetchType.EAGER)
            public Object getManyToOneEagerly()
            {
                return null;
            }

            @ManyToMany(fetch = FetchType.LAZY)
            public Object getManyToManyLazily()
            {
                return null;
            }

            @ManyToMany(fetch = FetchType.EAGER)
            public Object getManyToManyEagerly()
            {
                return null;
            }
        }

        context.checking(new Expectations()
            {
                {
                    oneOf(criteria).setFetchMode("oneToOneEagerly", FetchMode.JOIN);
                    oneOf(criteria).setFetchMode("oneToManyEagerly", FetchMode.JOIN);
                    oneOf(criteria).setFetchMode("manyToOneEagerly", FetchMode.JOIN);
                    oneOf(criteria).setFetchMode("manyToManyEagerly", FetchMode.JOIN);

                }
            });

        IndexingQueryOptimizer.minimizeAmountOfSubqueries(LazyEagerEntity.class, criteria);
    }

    @Test
    public void setsFetchModeToJoinToNonPublicMethods() throws Exception
    {

        final class NonPublicEntity
        {
            @OneToOne(fetch = FetchType.EAGER)
            protected Object getProtected()
            {
                return null;
            }

            @ManyToOne(fetch = FetchType.EAGER)
            Object getPackageProtected()
            {
                return null;
            }

            @IndexedEmbedded
            private Object getPrivate()
            {
                return null;
            }
        }
        context.checking(new Expectations()
            {
                {
                    oneOf(criteria).setFetchMode("protected", FetchMode.JOIN);
                    oneOf(criteria).setFetchMode("packageProtected", FetchMode.JOIN);
                    oneOf(criteria).setFetchMode("private", FetchMode.JOIN);
                }
            });

        IndexingQueryOptimizer.minimizeAmountOfSubqueries(NonPublicEntity.class, criteria);
    }

    final class RecursiveEntity1
    {
        @IndexedEmbedded
        public RecursiveEntity2 getOne()
        {
            return null;
        }
    }

    final class RecursiveEntity2
    {
        @IndexedEmbedded
        public RecursiveEntity1 getAnother()
        {
            return null;
        }
    }

    @Test
    public void respectsGivenDepth() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    oneOf(criteria).setFetchMode("one", FetchMode.JOIN);
                    oneOf(criteria).setFetchMode("one.another", FetchMode.JOIN);
                    oneOf(criteria).setFetchMode("one.another.one", FetchMode.JOIN);
                }
            });

        IndexingQueryOptimizer.minimizeAmountOfSubqueries(RecursiveEntity1.class, criteria, 3);
    }

    @Test
    public void breaksDirectSelfReferences() throws Exception
    {
        final class RecursiveEntity
        {
            @IndexedEmbedded
            public RecursiveEntity getSelf()
            {
                return null;
            }

            @IndexedEmbedded
            public Object getSome()
            {
                return null;
            }
        }
        context.checking(new Expectations()
            {
                {
                    oneOf(criteria).setFetchMode("some", FetchMode.JOIN);
                }
            });

        IndexingQueryOptimizer.minimizeAmountOfSubqueries(RecursiveEntity.class, criteria);
    }

    @Test
    public void setsFetchModeToJoinToInheritedMethods() throws Exception
    {

        class Super
        {
            @IndexedEmbedded
            public Object getObject()
            {
                return null;
            }
        }

        class Derived extends Super
        {
            @IndexedEmbedded
            public Object getAnotherObject()
            {
                return null;
            }
        }

        context.checking(new Expectations()
            {
                {
                    oneOf(criteria).setFetchMode("object", FetchMode.JOIN);
                    oneOf(criteria).setFetchMode("anotherObject", FetchMode.JOIN);
                }
            });

        IndexingQueryOptimizer.minimizeAmountOfSubqueries(Derived.class, criteria);
    }

    @Test
    public void setsFetchModeToJoinToCollectionAssociations() throws Exception
    {
        class AnotherEntity
        {
            @IndexedEmbedded
            public Object getValue()
            {
                return null;
            }
        }
        class Entity
        {
            @IndexedEmbedded
            public Set<AnotherEntity> getSet()
            {
                return null;
            }
        }

        context.checking(new Expectations()
            {
                {
                    oneOf(criteria).setFetchMode("set", FetchMode.JOIN);
                    oneOf(criteria).setFetchMode("set.value", FetchMode.JOIN);
                }
            });

        IndexingQueryOptimizer.minimizeAmountOfSubqueries(Entity.class, criteria);
    }
}
