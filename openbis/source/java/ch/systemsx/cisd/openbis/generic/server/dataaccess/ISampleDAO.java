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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * An interface that contains all data access operations on {@link SamplePE}s.
 * 
 * @author Tomasz Pylak
 */
public interface ISampleDAO extends IGenericDAO<SamplePE>
{
    /**
     * Inserts given {@link SamplePE} into the database or updates it if it already exists.
     */
    void createOrUpdateSample(final SamplePE sample, final PersonPE modifier)
            throws DataAccessException;

    /**
     * Tries to find a sample by its permanent ID. Returns <code>null</code> if not found.
     */
    SamplePE tryToFindByPermID(String permID) throws DataAccessException;

    /**
     * Returns the sample specified by given <var>sampleCode</var> and given
     * <var>databaseInstance</var>.
     */
    SamplePE tryFindByCodeAndDatabaseInstance(final String sampleCode,
            final DatabaseInstancePE databaseInstance) throws DataAccessException;

    /**
     * Returns a list of samples with given <var>databaseInstance</var> and one of given codes.
     * 
     * @param containerCodeOrNull if specified all returned samples should have container with
     *            specified code, otherwise they shouldn't have any container
     */
    List<SamplePE> listByCodesAndDatabaseInstance(final List<String> sampleCodes,
            String containerCodeOrNull, final DatabaseInstancePE databaseInstance);

    /**
     * Returns the sample specified by given <var>sampleCode</var> and given <var>space</var>.
     */
    SamplePE tryFindByCodeAndSpace(final String sampleCode, final SpacePE space)
            throws DataAccessException;

    /**
     * Returns a list of samples with given <var>space</var> and one of given codes.
     * 
     * @param containerCodeOrNull if specified all returned samples should have container with
     *            specified code, otherwise they shouldn't have any container
     */
    List<SamplePE> listByCodesAndSpace(final List<String> sampleCodes, String containerCodeOrNull,
            final SpacePE space);

    /**
     * Inserts or updates given list of {@link SamplePE} into the database in one go.
     */
    void createOrUpdateSamples(final List<SamplePE> samples, final PersonPE modifier)
            throws DataAccessException;

    /**
     * Updates given <var>sample</var>.
     */
    public void updateSample(final SamplePE sample, final PersonPE modifier)
            throws DataAccessException;

    /**
     * For given <var>sample</var> returns all {@link SamplePE}s that are generated from it.
     */
    List<SamplePE> listSamplesByGeneratedFrom(final SamplePE sample) throws DataAccessException;

    /**
     * Returns ids of parents of samples specified by given ids and connected by chosen relationship
     * type.
     */
    public Set<TechId> listSampleIdsByChildrenIds(Collection<TechId> children, TechId relationship);

    /**
     * Returns ids of children of samples specified by given ids.
     * <p>
     * NOTES:
     * <li>we don't use relationship type as don't really support different relationship types
     * <li>we need a set as the connection is many-to-many and we want unique ids
     */
    public Set<TechId> listSampleIdsByParentIds(Collection<TechId> parentIds);

    /** Returns ids of components of samples specified by given ids. */
    public List<TechId> listSampleIdsByContainerIds(Collection<TechId> containerIds);

    /** Returns ids of samples connected with experiments specified by given ids. */
    List<TechId> listSampleIdsByExperimentIds(Collection<TechId> experimentIds);

    /**
     * Lists samples (with minimal additional information) belonging to the given <code>space</code>
     * and having a property with the specified value.
     */
    List<SamplePE> listSamplesBySpaceAndProperty(final String propertyCode,
            final String propertyValue, final SpacePE space) throws DataAccessException;

    /**
     * Lists samples (with minimal additional information) with permanent identifier in given set of
     * values.
     */
    List<SamplePE> listByPermID(Set<String> values);

    List<SamplePE> listByIDs(Collection<Long> ids);

    /**
     * Delete samples with given ids by specified registrator with specified reason.
     */
    void delete(List<TechId> sampleIds, PersonPE registrator, String reason)
            throws DataAccessException;

    /**
     * lists all children ids for a set of trashed samples.
     */
    public Set<TechId> listChildrenForTrashedSamples(Collection<TechId> parentIds);

}
