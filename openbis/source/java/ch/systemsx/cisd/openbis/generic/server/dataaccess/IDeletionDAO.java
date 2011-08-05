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

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * An interface that contains all data access operations on {@link DeletionPE}s.
 * 
 * @author Piotr Buczek
 */
public interface IDeletionDAO extends IGenericDAO<DeletionPE>
{
    /** Inserts given {@link DeletionPE} into the database. */
    void create(final DeletionPE deletion) throws DataAccessException;

    /**
     * Moves entities with given ids to trash using specified deletion. Ignores ids of entities that
     * don't exist or are already in the trash.
     * 
     * @return number of trashed entities
     */
    int trash(EntityKind entityKind, List<TechId> entityIds, DeletionPE deletion)
            throws DataAccessException;

    /** Reverts given deletion. The deletion record will be removed from DB. */
    void revert(DeletionPE deletion);

    /**
     * Returns list of ids of samples moved to trash in specified deletions.
     */
    // This method is kept for consistency with other find methods and for tests.
    List<TechId> findTrashedSampleIds(List<TechId> deletionIds);

    /**
     * Returns list of ids of non-comonent samples (having no container) moved to trash in specified
     * deletions.
     */
    List<TechId> findTrashedNonComponentSampleIds(List<TechId> deletionIds);

    /**
     * Returns list of ids of component samples (samples with container) moved to trash in specified
     * deletions.
     */
    List<TechId> findTrashedComponentSampleIds(List<TechId> deletionIds);

    /** Returns list of ids of experiments moved to trash in specified deletions. */
    List<TechId> findTrashedExperimentIds(List<TechId> deletionIds);

    /** Returns list of codes of data sets moved to trash in specified deletions. */
    List<String> findTrashedDataSetCodes(List<TechId> deletionIds);

}
