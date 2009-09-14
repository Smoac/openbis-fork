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

package ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.DataIterator;

import ch.rinn.restrictions.Friend;

/**
 * A DAO query interface for obtaining sets of datasets or dataset-related entities based on a set
 * of dataset ids.
 * <p>
 * May need different implementations for different database engines.
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses =
    { DatasetRelationRecord.class })
interface IDatasetSetListingQuery
{
    /**
     * Returns the datasets for the given <var>datasetIds</var>.
     */
    public Iterable<DatasetRecord> getDatasets(LongSet datasetIds);

    /**
     * Returns the relations with parent datasets of the specified datasets.
     */
    public Iterable<DatasetRelationRecord> getDatasetRelationsWithParents(LongSet entityIds);

    /**
     * Returns the ids of children datasets of the specified datasets.
     */
    public DataIterator<Long> getDatasetChildrenIds(LongSet entityIds);
}
