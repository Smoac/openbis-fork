/*
 * Copyright 2015 ETH Zuerich, CISD
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

/**
 * @author pkupczyk
 */
public abstract class AbstractAssociationCriteria implements IAssociationCriteria
{

    private static final long serialVersionUID = 1L;

    private AssociatedEntityKind entityKind;

    public AbstractAssociationCriteria(AssociatedEntityKind entityKind)
    {
        this.entityKind = entityKind;
    }

    @Override
    public AssociatedEntityKind getEntityKind()
    {
        return entityKind;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(getEntityKind() + ": ");
        sb.append(getSearchPatterns());
        return sb.toString();
    }

}
