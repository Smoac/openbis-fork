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

import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.HierarchyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;

/**
 * An interface that contains all data access operations on {@link SamplePE}s.
 * 
 * @author Tomasz Pylak
 */
public interface ISampleDAO
{
    /**
     * Lists {@link SamplePE}s of given type from the given group. Returned {@link SamplePE}s are
     * enriched with procedures and their experiments.
     */
    List<SamplePE> listSamplesByTypeAndGroup(final SampleTypePE sampleType, final GroupPE group)
            throws DataAccessException;

    /**
     * The same as {@link #listSamplesByTypeAndGroup(SampleTypePE, GroupPE)}, but lists samples
     * from the database instance instead of the group.
     */
    List<SamplePE> listSamplesByTypeAndDatabaseInstance(final SampleTypePE sampleType,
            final DatabaseInstancePE databaseInstance) throws DataAccessException;

    /**
     * Inserts given {@link SamplePE} into the database.
     */
    void createSample(final SamplePE sample) throws DataAccessException;

    /**
     * Returns the sample specified by given <var>sampleCode</var> and given <var>databaseInstance</var>.
     */
    SamplePE tryFindByCodeAndDatabaseInstance(final String sampleCode,
            final DatabaseInstancePE databaseInstance, final HierarchyType hierarchyType)
            throws DataAccessException;

    /**
     * Returns the sample specified by given <var>sampleCode</var> and given <var>group</var>.
     */
    SamplePE tryFindByCodeAndGroup(final String sampleCode, final GroupPE group,
            final HierarchyType hierarchyType) throws DataAccessException;

    /**
     * For given <var>sample</var> returns all {@link SamplePE}s that are generated from it.
     */
    List<SamplePE> listSamplesByGeneratedFrom(final SamplePE sample) throws DataAccessException;

    /**
     * Lists all {@link SamplePE}s which are part of the specified <var>container</var>.
     */
    List<SamplePE> listSamplesByContainer(final SamplePE container) throws DataAccessException;

    /**
     * Lists all {@link SamplePE}s which are associated with <var>experiment</var>.
     */
    List<SamplePE> listSamplesByExperiment(final ExperimentPE experiment)
            throws DataAccessException;

    /**
     * Inserts given list of {@link SamplePE} into the database in one go.
     */
    void createSamples(List<SamplePE> samples) throws DataAccessException;
}
