/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.ModifierSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.RegistratorSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.TagSearchCriteria;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.common.search.AbstractEntitySearchCriteria")
public abstract class AbstractEntitySearchCriteria<ID extends IObjectId> extends AbstractObjectSearchCriteria<ID>
{

    private static final long serialVersionUID = 1L;

    public CodeSearchCriteria withCode()
    {
        return with(new CodeSearchCriteria());
    }

    public CodesSearchCriteria withCodes()
    {
        return with(new CodesSearchCriteria());
    }

    public PermIdSearchCriteria withPermId()
    {
        return with(new PermIdSearchCriteria());
    }

    public RegistratorSearchCriteria withRegistrator()
    {
        return with(new RegistratorSearchCriteria());
    }

    public ModifierSearchCriteria withModifier()
    {
        return with(new ModifierSearchCriteria());
    }

    public RegistrationDateSearchCriteria withRegistrationDate()
    {
        return with(new RegistrationDateSearchCriteria());
    }

    public ModificationDateSearchCriteria withModificationDate()
    {
        return with(new ModificationDateSearchCriteria());
    }

    public TagSearchCriteria withTag()
    {
        return with(new TagSearchCriteria());
    }

    public NumberPropertySearchCriteria withNumberProperty(String propertyName)
    {
        return with(new NumberPropertySearchCriteria(propertyName));
    }

    public StringPropertySearchCriteria withProperty(String propertyName)
    {
        return with(new StringPropertySearchCriteria(propertyName));
    }

    public StrictlyStringPropertySearchCriteria withStringProperty(final String propertyName)
    {
        return with(new StrictlyStringPropertySearchCriteria(propertyName));
    }

    public DatePropertySearchCriteria withDateProperty(String propertyName)
    {
        return with(new DatePropertySearchCriteria(propertyName));
    }

    public BooleanPropertySearchCriteria withBooleanProperty(final String propertyName)
    {
        return with(new BooleanPropertySearchCriteria(propertyName));
    }

    public AnyPropertySearchCriteria withAnyProperty()
    {
        return with(new AnyPropertySearchCriteria());
    }

    public AnyNumberPropertySearchCriteria withAnyNumberProperty()
    {
        return with(new AnyNumberPropertySearchCriteria());
    }

    public AnyFieldSearchCriteria withAnyField()
    {
        return with(new AnyFieldSearchCriteria());
    }

    public abstract AbstractEntitySearchCriteria<ID> withOrOperator();

    public abstract AbstractEntitySearchCriteria<ID> withAndOperator();
}
