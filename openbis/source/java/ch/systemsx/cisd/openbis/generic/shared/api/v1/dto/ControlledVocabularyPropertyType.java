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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class ControlledVocabularyPropertyType extends PropertyType
{
    private static final long serialVersionUID = 1L;

    public static class VocabularyTerm implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private final String code;

        private final String label;

        public VocabularyTerm(String code, String label)
        {
            this.code = code;
            this.label = label;
        }

        public String getCode()
        {
            return code;
        }

        public String getLabel()
        {
            return label;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (obj instanceof VocabularyTerm == false)
            {
                return false;
            }

            EqualsBuilder builder = new EqualsBuilder();
            VocabularyTerm other = (VocabularyTerm) obj;
            builder.append(getCode(), other.getCode());
            return builder.isEquals();
        }

        @Override
        public int hashCode()
        {
            HashCodeBuilder builder = new HashCodeBuilder();
            builder.append(getCode());
            return builder.toHashCode();
        }

        @Override
        public String toString()
        {
            ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
            builder.append(getCode());
            builder.append(getLabel());
            return builder.toString();
        }
    }

    public static class ControlledVocabularyPropertyTypeInitializer extends PropertyTypeInitializer
    {
        private final ArrayList<VocabularyTerm> terms = new ArrayList<VocabularyTerm>();

        public void setTerms(List<VocabularyTerm> validValues)
        {
            this.terms.clear();
            this.terms.addAll(validValues);
        }
    }

    private final ArrayList<VocabularyTerm> terms;

    /**
     * @param initializer
     */
    public ControlledVocabularyPropertyType(ControlledVocabularyPropertyTypeInitializer initializer)
    {
        super(initializer);
        terms = initializer.terms;
        if (terms == null || terms.isEmpty())
        {
            throw new IllegalArgumentException(
                    "A controlled vocabulary property type must have terms");
        }
    }

    public List<VocabularyTerm> getTerms()
    {
        return terms;
    }

    @Override
    protected void appendFieldsToStringBuilder(ToStringBuilder builder)
    {
        builder.append(terms);
    }
}
