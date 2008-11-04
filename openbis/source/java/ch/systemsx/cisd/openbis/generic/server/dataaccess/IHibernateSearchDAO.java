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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.shared.dto.IMatchingEntity;

/**
 * <i>Data Access Object</i> for accessing <i>Hibernate Search</i>.
 * 
 * @author Christian Ribeaud
 */
public interface IHibernateSearchDAO
{
    /**
     * Searches in entities of type <var>entityClass</var> for given <var>searchTerm</var> in the
     * specified <var>fields</var>.
     * 
     * @param searchTerm could be something like "<code>code:C11 AND registrator.lastName:System User</code>".
     */
    public <T extends IMatchingEntity> List<T> searchEntitiesByTerm(final Class<T> entityClass,
            final String[] fields, final String searchTerm) throws DataAccessException;
}