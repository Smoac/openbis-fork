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

package ch.systemsx.cisd.openbis.generic.shared.util;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

import ch.systemsx.cisd.common.collections.UnmodifiableCollectionDecorator;
import ch.systemsx.cisd.openbis.generic.shared.dto.IIdHolder;

/**
 * Some utility methods around <i>Hibernate</i>.
 * <p>
 * This should be used instead of {@link Hibernate} because we check here for
 * {@link UnmodifiableCollectionDecorator}.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class HibernateUtils
{
    private HibernateUtils()
    {
        // Can not instantiate.
    }

    private final static Object getRealProxy(final Object proxy)
    {
        final boolean unmodifiable = (proxy instanceof UnmodifiableCollectionDecorator);
        final Object realProxy =
                (unmodifiable) ? ((UnmodifiableCollectionDecorator<?>) proxy).getDecorated()
                        : proxy;
        return realProxy;
    }

    /**
     * Extends {@link Hibernate#initialize(Object)} by checking for
     * {@link UnmodifiableCollectionDecorator}.
     */
    public final static void initialize(final Object proxy)
    {
        Hibernate.initialize(getRealProxy(proxy));
    }

    /**
     * Extends {@link Hibernate#isInitialized(Object)} by checking for
     * {@link UnmodifiableCollectionDecorator}.
     */
    public final static boolean isInitialized(final Object proxy)
    {
        return Hibernate.isInitialized(getRealProxy(proxy));
    }

    /**
     * For given <i>idHolder</i> returns corresponding {@link Long} id.
     * <p>
     * Do not use this method. It is a hack till we use full object instead of id in proxied objects
     * for DAO requests.
     * </p>
     */
    public final static Long getId(final IIdHolder idHolder)
    {
        if (idHolder instanceof HibernateProxy)
        {
            return (Long) ((HibernateProxy) idHolder).getHibernateLazyInitializer().getIdentifier();
        }
        return idHolder.getId();
    }
}
