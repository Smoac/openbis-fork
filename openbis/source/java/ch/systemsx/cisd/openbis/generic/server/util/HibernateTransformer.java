/*     
 * Copyright 2008 ETH Zuerich, CISD         
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

package ch.systemsx.cisd.openbis.generic.server.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.beanlib.hibernate.HibernateBeanReplicator;
import net.sf.beanlib.hibernate3.Hibernate3BeanTransformer;
import net.sf.beanlib.hibernate3.Hibernate3CollectionReplicator;
import net.sf.beanlib.hibernate3.Hibernate3JavaBeanReplicator;
import net.sf.beanlib.spi.BeanTransformerSpi;
import net.sf.beanlib.spi.replicator.BeanReplicatorSpi;
import net.sf.beanlib.spi.replicator.CollectionReplicatorSpi;
import net.sf.beanlib.spi.replicator.CollectionReplicatorSpi.Factory;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.Hibernate;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.proxy.HibernateProxy;

import ch.systemsx.cisd.common.collections.UnmodifiableCollectionDecorator;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;

/**
 * A {@link MethodInterceptor} implementation to get rid of <i>Hibernate</i> proxy classes like
 * {@link PersistentCollection} or {@link HibernateProxy}.
 * <p>
 * Note that this interceptor should only be used on the service layer (when the object is
 * <i>detached</i> and when database transactions are closed). Otherwise they might trigger
 * <i>SQL</i> calls.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class HibernateTransformer implements MethodInterceptor
{

    public final static ThreadLocal<HibernateBeanReplicator> HIBERNATE_BEAN_REPLICATOR =
            new ThreadLocal<HibernateBeanReplicator>()
                {

                    private final HibernateBeanReplicator createHibernateBeanReplicator()
                    {
                        final Hibernate3BeanTransformer beanTransformer =
                                Hibernate3BeanTransformer.newBeanTransformer();
                        beanTransformer
                                .initCollectionReplicatable(new CollectionReplicatorSpiFactory());
                        beanTransformer.initBeanReplicatable(new BeanReplicatorSpiFactory());
                        return new HibernateBeanReplicator(beanTransformer);
                    }

                    //          
                    // ThreadLocal
                    //          

                    @Override
                    protected final HibernateBeanReplicator initialValue()
                    {
                        return createHibernateBeanReplicator();
                    }
                };

    //          
    // MethodInterceptor
    //          

    public final Object invoke(final MethodInvocation methodInvocation) throws Throwable
    {
        final Object proceed = methodInvocation.proceed();
        if (proceed != null)
        {
            return HIBERNATE_BEAN_REPLICATOR.get().copy(proceed);
        }
        return null;
    }

    //          
    // Helper classes
    //          

    private static final class BeanReplicatorSpiFactory implements BeanReplicatorSpi.Factory
    {

        //          
        // Factory
        //          

        public final BeanReplicatorSpi newBeanReplicatable(final BeanTransformerSpi beanTransformer)
        {
            return new NoInitializationHibernate3JavaBeanReplicator(beanTransformer);
        }
    }

    private static final class CollectionReplicatorSpiFactory implements Factory
    {

        //          
        // Factory
        //          

        public final CollectionReplicatorSpi newCollectionReplicatable(
                final BeanTransformerSpi beanTransformerSpi)
        {
            return new NoInitializationHibernate3CollectionReplicator(beanTransformerSpi);
        }
    }

    private static final class NoInitializationHibernate3CollectionReplicator extends
            Hibernate3CollectionReplicator
    {

        NoInitializationHibernate3CollectionReplicator(final BeanTransformerSpi beanTransformer)
        {
            super(beanTransformer);
        }

        //          
        // Hibernate3CollectionReplicator
        //          

        @SuppressWarnings("unchecked")
        @Override
        public final <V, T> T replicateCollection(final Collection<V> from, final Class<T> toClass)
        {
            final boolean unmodifiable = (from instanceof UnmodifiableCollectionDecorator);
            final Collection<V> realFrom =
                    (unmodifiable) ? ((UnmodifiableCollectionDecorator<V>) from).getDecorated()
                            : from;
            if (Hibernate.isInitialized(realFrom) == false)
            {
                if (realFrom instanceof Set)
                {
                    return (T) new HashSet(0);
                } else if (realFrom instanceof List)
                {
                    return (T) new ArrayList(0);
                } else
                {
                    throw new NotImplementedException();
                }
            }
            return super.replicateCollection(realFrom, toClass);
        }
    }

    private static final class NoInitializationHibernate3JavaBeanReplicator extends
            Hibernate3JavaBeanReplicator
    {

        NoInitializationHibernate3JavaBeanReplicator(final BeanTransformerSpi beanTransformer)
        {
            super(beanTransformer);
        }

        //          
        // Hibernate3JavaBeanReplicator
        //          

        @Override
        public final <V, T> T replicateBean(final V from, final Class<T> toClass)
        {
            if (Hibernate.isInitialized(from) == false)
            {
                return null;
            }
            return super.replicateBean(from, toClass);
        }
    }
}
