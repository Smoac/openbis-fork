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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search;

import java.util.Set;

import org.hibernate.search.annotations.Indexed;

/**
 * An finder of entities annotated with {@link Indexed} annotation.
 * 
 * @author Christian Ribeaud
 */
public interface IIndexedEntityFinder
{
    /**
     * Returns the <i>Hibernate</i> entities that are annotated with {@link Indexed} annotation.
     * 
     * @return never <code>null</code> but could return an empty set.
     */
    public Set<Class<?>> getIndexedEntities();
}
