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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ISampleListingQuery;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ISampleSetListingQuery;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;

/**
 * The DAO for rapid sample listing.
 *
 * @author Bernd Rinn
 */
public interface ISampleListerDAO
{

    /**
     * Returns true if this DAO is enabled.
     */
    public abstract boolean isEnabled();

    /**
     * Returns the technical id of the home database instance.
     */
    public abstract long getDatabaseInstanceId();

    /**
     * Returns the DTO of the home database instance.
     */
    public abstract DatabaseInstance getDatabaseInstance();

    /**
     * Returns the query object of this DAO. Use this for all except set-based queries.
     */
    public abstract ISampleListingQuery getQuery();

    /**
     * Returns the set query object of this DAO. Use this for set-based queries.
     */
    public abstract ISampleSetListingQuery getIdSetQuery();

}