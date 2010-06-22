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
import java.util.Set;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;

/**
 * An interface that contains all data access operations on {@link SamplePE}s.
 * 
 * @author Tomasz Pylak
 */
public interface ISampleDAO extends IGenericDAO<SamplePE>
{
    /**
     * Inserts given {@link SamplePE} into the database.
     */
    void createSample(final SamplePE sample) throws DataAccessException;

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
     * Returns the sample specified by given <var>sampleCode</var> and given <var>group</var>.
     */
    SamplePE tryFindByCodeAndGroup(final String sampleCode, final GroupPE group)
            throws DataAccessException;

    /**
     * Inserts given list of {@link SamplePE} into the database in one go.
     */
    void createSamples(List<SamplePE> samples) throws DataAccessException;

    /**
     * Updates given <var>sample</var>.
     */
    public void updateSample(SamplePE sample) throws DataAccessException;

    /**
     * For given <var>sample</var> returns all {@link SamplePE}s that are generated from it.
     */
    List<SamplePE> listSamplesByGeneratedFrom(final SamplePE sample) throws DataAccessException;

    /**
     * Lists {@link SamplePE}s belonging to given <code>experiment</code>. Fetches also properties.
     */
    public List<SamplePE> listSamplesWithPropertiesByExperiment(final ExperimentPE experiment)
            throws DataAccessException;

    /**
     * Lists all {@link SamplePE}s which are part of the specified <var>container</var>. Fetches
     * also properties and experiment.
     */
    List<SamplePE> listSamplesWithPropertiesByContainer(final SamplePE container)
            throws DataAccessException;

    /**
     * Lists {@link SamplePE}s of given group. Fetches also properties and experiment.
     */
    List<SamplePE> listSamplesWithPropertiesByGroup(final GroupPE group) throws DataAccessException;

    /**
     * The same as {@link #listSamplesWithPropertiesByGroup(GroupPE)}, but lists samples from the
     * database instance instead of the group.
     */
    List<SamplePE> listSamplesWithPropertiesByDatabaseInstance(
            final DatabaseInstancePE databaseInstance) throws DataAccessException;

    /**
     * Lists {@link SamplePE}s of given type from the given group. Fetches also properties and
     * experiment.
     */
    List<SamplePE> listSamplesWithPropertiesByTypeAndGroup(final SampleTypePE sampleType,
            final GroupPE group) throws DataAccessException;

    /**
     * The same as {@link #listSamplesWithPropertiesByTypeAndGroup(SampleTypePE, GroupPE)}, but
     * lists samples from the database instance instead of the group.
     */
    List<SamplePE> listSamplesWithPropertiesByTypeAndDatabaseInstance(
            final SampleTypePE sampleType, final DatabaseInstancePE databaseInstance)
            throws DataAccessException;

    /**
     * Lists samples (with minimal additional information) belonging to the given <code>group</code>
     * and having a property with the specified value.
     */
    List<SamplePE> listSamplesByGroupAndProperty(final String propertyCode,
            final String propertyValue, final GroupPE group) throws DataAccessException;

	/**
     * Lists samples (with minimal additional information) with permanent identifier in given set of values.
     */
    List<SamplePE> listByPermID(Set<String> values);

    /**
     * Delete samples with given by specified registrator with specified reason.
     */
    void delete(List<TechId> sampleIds, PersonPE registrator, String reason) throws DataAccessException;
}
