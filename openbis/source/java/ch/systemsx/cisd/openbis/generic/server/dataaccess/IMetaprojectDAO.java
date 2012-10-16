/*
 * Copyright 2012 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author Pawel Glyzewski
 */
public interface IMetaprojectDAO extends IGenericDAO<MetaprojectPE>
{
    /**
     * Finds a metaproject by the specified owner id and metaproject name. Returns null is no
     * metaproject is found.
     */
    public MetaprojectPE tryFindByOwnerAndName(String ownerId, String metaprojectName);

    /**
     * Lists all metaprojects defined by given user.
     */
    public List<MetaprojectPE> listMetaprojects(PersonPE owner);

    /**
     * Creates a new metaproject or updates existing one.
     */
    public void createOrUpdateMetaproject(MetaprojectPE metaproject, PersonPE owner);

}
