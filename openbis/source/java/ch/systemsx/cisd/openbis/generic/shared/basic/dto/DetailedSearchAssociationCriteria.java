/*
 * Copyright 2011 ETH Zuerich, CISD
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

import java.util.Collection;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;

/**
 * Describes detailed search assiciation criteria for with specified entity kind.
 * 
 * @author Piotr Buczek
 */
public class DetailedSearchAssociationCriteria implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private SearchableEntity entityKind;

    private Collection<Long> ids;

    public DetailedSearchAssociationCriteria(SearchableEntity entityKind, Collection<Long> ids)
    {
        this.entityKind = entityKind;
        this.ids = ids;
    }

    public Collection<Long> getIds()
    {
        return ids;
    }

    public SearchableEntity getEntityKind()
    {
        return entityKind;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(entityKind + ": ");
        sb.append(getIds());
        return sb.toString();
    }

}
