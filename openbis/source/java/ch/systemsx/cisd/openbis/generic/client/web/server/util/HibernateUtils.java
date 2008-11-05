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

package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import org.hibernate.Hibernate;

import ch.systemsx.cisd.common.collections.UnmodifiableCollectionDecorator;

/**
 * Some utility methods around <i>Hibernate</i>.
 * 
 * @author Christian Ribeaud
 */
public final class HibernateUtils
{
    private HibernateUtils()
    {
        // Can not instantiate.
    }

    /**
     * Extends {@link Hibernate#isInitialized(Object)} by checking for
     * {@link UnmodifiableCollectionDecorator}.
     */
    public final static boolean isInitialized(final Object proxy)
    {
        final boolean unmodifiable = (proxy instanceof UnmodifiableCollectionDecorator);
        final Object realProxy =
                (unmodifiable) ? ((UnmodifiableCollectionDecorator<?>) proxy).getDecorated()
                        : proxy;
        return Hibernate.isInitialized(realProxy);
    }
}
