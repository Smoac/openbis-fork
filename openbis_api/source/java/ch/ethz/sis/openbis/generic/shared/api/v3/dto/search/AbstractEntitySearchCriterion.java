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

package ch.ethz.sis.openbis.generic.shared.api.v3.dto.search;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.IObjectId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("dto.search.AbstractEntitySearchCriterion")
public class AbstractEntitySearchCriterion<ID extends IObjectId> extends AbstractObjectSearchCriterion<ID>
{

    private static final long serialVersionUID = 1L;

    public CodeSearchCriterion withCode()
    {
        return with(new CodeSearchCriterion());
    }

    public EntityTypeSearchCriterion withType()
    {
        return with(new EntityTypeSearchCriterion());
    }

    public PermIdSearchCriterion withPermId()
    {
        return with(new PermIdSearchCriterion());
    }

    public RegistrationDateSearchCriterion withRegistrationDate()
    {
        return with(new RegistrationDateSearchCriterion());
    }

    public ModificationDateSearchCriterion withModificationDate()
    {
        return with(new ModificationDateSearchCriterion());
    }

    public TagSearchCriterion withTag()
    {
        return with(new TagSearchCriterion());
    }

    public StringPropertySearchCriterion withProperty(String propertyName)
    {
        return with(new StringPropertySearchCriterion(propertyName));
    }

    public DatePropertySearchCriterion withDateProperty(String propertyName)
    {
        return with(new DatePropertySearchCriterion(propertyName));
    }

    public AnyPropertySearchCriterion withAnyProperty()
    {
        return with(new AnyPropertySearchCriterion());
    }

    public AnyFieldSearchCriterion withAnyField()
    {
        return with(new AnyFieldSearchCriterion());
    }

    @Override
    protected SearchCriterionToStringBuilder createBuilder()
    {
        SearchCriterionToStringBuilder builder = super.createBuilder();
        builder.setOperator(operator);
        return builder;
    }

}
