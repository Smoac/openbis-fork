/*
 * Copyright 2007 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * <i>Data Access Object</i> for persons.
 * 
 * @author Franz-Josef Elmer
 */
public interface IPersonDAO
{
    /**
     * Finds the technical id of the person with the specified user id.
     * 
     * @param userId user id. Can not be blank.
     * @return <code>null</code>, if no person with that id exists.
     */
    public PersonPE tryFindPersonByUserId(String userId) throws DataAccessException;

    /**
     * Inserts given <code>Person</code> into the database.
     * <p>
     * As side effect the <i>unique identifier</i> returned by the database is set to given
     * <code>Person</code> object using {@link PersonPE#setId(Long)}.
     * </p>
     * 
     * @param person <code>Person</code> object to be inserted into the database. Can not be
     *            <code>null</code>.
     */
    public void createPerson(PersonPE person) throws DataAccessException;

    /**
     * For the given <code>id</code> returns the corresponding <code>Person</code>, or throw
     * {@link DataAccessException}, if a person with the given <var>id</var> does not exist.
     */
    public PersonPE getPerson(long id) throws DataAccessException;

    /**
     * @returns The list of all persons currently present in the database.
     */
    public List<PersonPE> listPersons() throws DataAccessException;

    /**
     * Updates given <var>PersonPE</var>.
     */
    public void updatePerson(final PersonPE person) throws DataAccessException;

}
