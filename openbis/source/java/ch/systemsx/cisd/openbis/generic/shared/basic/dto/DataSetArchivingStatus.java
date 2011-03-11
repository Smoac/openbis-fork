/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Status of data set archiving process.
 * 
 * <pre>
 *                                                                                                                  |                                                         
 *       archive                                           
 *    ---------------> ARCHIVE_PENDING -------> ARCHIVED 
 *    |                                          |             
 *    |                                          |  unarchive
 *    |                                          |           
 * AVAILABLE  <--------------------|             |  
 *    ^                            |             |  
 *    |                            |             V             
 *    |------>LOCKED               |----- UNARCHIVE_PENDING
 * 
 * </pre>
 * 
 * @author Piotr Buczek
 */
public enum DataSetArchivingStatus implements IsSerializable
{
    AVAILABLE("AVAILABLE", true), // the data set is present in the data store only

    // TODO KE: we might want to archive data sets in this state too
    LOCKED("AVAILABLE (LOCKED)", true),

    ARCHIVED("ARCHIVED", false), // the data set is present in the archive only

    UNARCHIVE_PENDING("UNARCHIVE PENDING", false),

    ARCHIVE_PENDING("ARCHIVE PENDING", false);

    private final String description;

    private final boolean available;

    DataSetArchivingStatus(String description, boolean available)
    {
        this.description = description;
        this.available = available;
    }

    public String getDescription()
    {
        return description;
    }

    public boolean isAvailable()
    {
        return available;
    }

    /**
     * when deleting datasets from the archive
     */
    public boolean isDeletable()
    {
        return isAvailable() || this == ARCHIVED;
    }

}
