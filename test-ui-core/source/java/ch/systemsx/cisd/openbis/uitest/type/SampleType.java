/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.uitest.type;

import java.util.Collection;

/**
 * @author anttil
 */
public abstract class SampleType implements EntityType
{
    @Override
    public abstract String getCode();

    public abstract String getDescription();

    public abstract boolean isListable();

    public abstract boolean isShowContainer();

    public abstract boolean isShowParents();

    public abstract boolean isUniqueSubcodes();

    public abstract boolean isGenerateCodes();

    public abstract boolean isShowParentMetadata();

    public abstract String getGeneratedCodePrefix();

    @Override
    public abstract Collection<PropertyTypeAssignment> getPropertyTypeAssignments();

    public abstract Script getValidationScript();

    @Override
    public final boolean equals(Object o)
    {
        if (o instanceof SampleType)
        {
            return ((SampleType) o).getCode().equalsIgnoreCase(getCode());
        }
        return false;
    }

    @Override
    public final int hashCode()
    {
        return getCode().toUpperCase().hashCode();
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + " " + this.getCode();
    }

}
