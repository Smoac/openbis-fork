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

package ch.systemsx.cisd.openbis.generic.server.business.bo.common;

import net.lemnik.eodsql.DataIterator;

import ch.rinn.restrictions.Private;

/**
 * Interfaces to query entity properties of one or all entities.
 * 
 * @author Tomasz Pylak
 */
@Private
public interface IEntityPropertyListingQuery
{
    /**
     * Returns all generic property values of the entity with <var>entityId</var>.
     */
    public DataIterator<GenericEntityPropertyRecord> getEntityPropertyGenericValues(long entityId);

    /**
     * Returns all generic property values of all entitys.
     */
    public DataIterator<GenericEntityPropertyRecord> getEntityPropertyGenericValues();

    /**
     * Returns all controlled vocabulary property values of the entity with <var>entityId</var>.
     */
    public DataIterator<VocabularyTermRecord> getEntityPropertyVocabularyTermValues(long entityId);

    /**
     * Returns all controlled vocabulary property values of all entitys.
     */
    public DataIterator<VocabularyTermRecord> getEntityPropertyVocabularyTermValues();

    /**
     * Returns all material-type property values of the entity with <var>entityId</var>
     */
    public DataIterator<MaterialEntityPropertyRecord> getEntityPropertyMaterialValues(long entityId);

    /**
     * Returns all material-type property values of all entitys.
     */
    public DataIterator<MaterialEntityPropertyRecord> getEntityPropertyMaterialValues();
}
