/*
 * Copyright 2014 ETH Zuerich, CISD
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.fetchoptions.EntityTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.property.fetchoptions.PropertyAssignmentFetchOptions")
public class PropertyAssignmentFetchOptions extends FetchOptions<PropertyAssignment> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private EntityTypeFetchOptions entityType;

    @JsonProperty
    private PropertyTypeFetchOptions propertyType;

    @JsonProperty
    private PersonFetchOptions registrator;

    @JsonProperty
    private PropertyAssignmentSortOptions sort;

    // Method automatically generated with DtoGenerator
    public EntityTypeFetchOptions withEntityType()
    {
        if (entityType == null)
        {
            entityType = new EntityTypeFetchOptions();
        }
        return entityType;
    }

    // Method automatically generated with DtoGenerator
    public EntityTypeFetchOptions withEntityTypeUsing(EntityTypeFetchOptions fetchOptions)
    {
        return entityType = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasEntityType()
    {
        return entityType != null;
    }

    // Method automatically generated with DtoGenerator
    public PropertyTypeFetchOptions withPropertyType()
    {
        if (propertyType == null)
        {
            propertyType = new PropertyTypeFetchOptions();
        }
        return propertyType;
    }

    // Method automatically generated with DtoGenerator
    public PropertyTypeFetchOptions withPropertyTypeUsing(PropertyTypeFetchOptions fetchOptions)
    {
        return propertyType = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasPropertyType()
    {
        return propertyType != null;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withRegistrator()
    {
        if (registrator == null)
        {
            registrator = new PersonFetchOptions();
        }
        return registrator;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withRegistratorUsing(PersonFetchOptions fetchOptions)
    {
        return registrator = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasRegistrator()
    {
        return registrator != null;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public PropertyAssignmentSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new PropertyAssignmentSortOptions();
        }
        return sort;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public PropertyAssignmentSortOptions getSortBy()
    {
        return sort;
    }
    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("PropertyAssignment", this);
        f.addFetchOption("EntityType", entityType);
        f.addFetchOption("PropertyType", propertyType);
        f.addFetchOption("Registrator", registrator);
        return f;
    }

}
