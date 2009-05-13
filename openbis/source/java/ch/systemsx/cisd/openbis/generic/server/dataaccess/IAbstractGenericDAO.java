/*
 * Copyright 2009 ETH Zuerich, CISD
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

import org.springframework.dao.EmptyResultDataAccessException;

import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * Generic interface for DAOs.
 * 
 * @author Piotr Buczek
 */
public interface IAbstractGenericDAO<T extends IIdHolder>
{
    /**
     * Returns the entity for given technical id with no lazy connections initialized.
     * 
     * @param techId the entity technical identifier.
     * @throws EmptyResultDataAccessException if the entity with given identifier does not exist in
     *             the database.
     */
    public T getByTechId(final TechId techId);

    
    /**
     * @param techId the entity technical identifier
     * @param connections the (lazy) connections to additionally initialize
     * @return entity with the given technical identifier or null if it is not found
     */
    public T tryGetByTechId(final TechId techId, String... connections);
}
