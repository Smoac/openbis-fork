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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * A (mutable) object representing the specification of a search. A search is specified by
 * MatchClause objects and an operator for combining match clauses.
 * <p>
 * A MatchClause is made up of a property or attribute to compare against and a desired value for
 * that property or attribute.
 * <p>
 * Example:<br>
 * <blockquote> Match all of the following clauses: <blockquote> Attribute('TYPE') = [desired value]
 * <br>
 * Property('PROPERTY_CODE') = [desired value] </blockquote> </blockquote>
 * <p>
 * Looks like this:<br>
 * <blockquote><code>
        SearchCriteria sc = new SearchCriteria();<br>
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, "A_TYPE_CODE"));<br>
        sc.addMatchClause(MatchClause.createPropertyMatch("PROPERTY_CODE", "a property value"));<br>
 * </code></blockquote>
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class SearchCriteria implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * An enum listing the different field types that can be compared against.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static enum MatchClauseFieldType
    {
        PROPERTY, ATTRIBUTE
        // Commented out fields are not yet supported, but may be in the future.
        /* ANY_FIELD, ANY_PROPERTY, */
    }

    /**
     * An enum listing the different attributes that can be compared against.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static enum MatchClauseAttribute
    {
        CODE, TYPE
    }

    /**
     * A specification of one field (either property or attribute) and desired value for that field.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static class MatchClause implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private final MatchClauseFieldType fieldType;

        private final String fieldCode;

        private final String desiredValue;

        /**
         * Protected constructor. Use one of the factory methods to instantiate a MatchClause.
         * 
         * @param fieldType
         * @param fieldCode
         * @param desiredValue
         */
        protected MatchClause(MatchClauseFieldType fieldType, String fieldCode, String desiredValue)
        {
            this.fieldType = fieldType;
            this.fieldCode = fieldCode;
            this.desiredValue = desiredValue;
        }

        /**
         * Factory method to create a MatchClause matching against a specific property.
         * 
         * @param propertyCode The name of the property to compare against.
         * @param desiredValue The desired value for the property.
         */
        public static MatchClause createPropertyMatch(String propertyCode, String desiredValue)
        {
            return new PropertyMatchClause(propertyCode, desiredValue);
        }

        /**
         * Factory method to create a MatchClause matching against a specific attribute.
         * 
         * @param attribute The attribute to compare against.
         * @param desiredValue The desired value for the attribute.
         */
        public static MatchClause createAttributeMatch(MatchClauseAttribute attribute,
                String desiredValue)
        {
            return new AttributeMatchClause(attribute, desiredValue);
        }

        /**
         * The field type this MatchClause matches against. Could be either a property or attribute.
         */
        public MatchClauseFieldType getFieldType()
        {
            return fieldType;
        }

        /**
         * The code of the field.
         */
        private String getFieldCode()
        {
            return fieldCode;
        }

        public String getDesiredValue()
        {
            return desiredValue;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (obj instanceof MatchClause == false)
            {
                return false;
            }

            MatchClause other = (MatchClause) obj;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(getFieldType(), other.getFieldType());
            builder.append(getFieldCode(), other.getFieldCode());
            builder.append(getDesiredValue(), other.getDesiredValue());
            return builder.isEquals();
        }

        @Override
        public int hashCode()
        {
            HashCodeBuilder builder = new HashCodeBuilder();
            builder.append(getFieldType());
            builder.append(getFieldCode());
            builder.append(getDesiredValue());
            return builder.toHashCode();
        }

        @Override
        public String toString()
        {
            ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
            builder.append(getFieldType());
            builder.append(getFieldCode());
            builder.append(getDesiredValue());
            return builder.toString();
        }
    }

    /**
     * A MatchClause for checking that a property equals a desired value.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static class PropertyMatchClause extends MatchClause
    {
        private static final long serialVersionUID = 1L;

        private final String propertyCode;

        /**
         * Factory method to create a MatchClause matching against a specific property.
         * 
         * @param propertyCode The property to compare against.
         * @param desiredValue The desired value of the property.
         */
        protected PropertyMatchClause(String propertyCode, String desiredValue)
        {
            super(MatchClauseFieldType.PROPERTY, propertyCode, desiredValue);
            this.propertyCode = propertyCode;
            assert null != propertyCode;
            assert null != desiredValue;
        }

        /**
         * Return the code of the property to compare against.
         */
        public String getPropertyCode()
        {
            return propertyCode;
        }
    }

    /**
     * A MatchClause for checking that an attribute equals a desired value.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static class AttributeMatchClause extends MatchClause
    {
        private static final long serialVersionUID = 1L;

        private final MatchClauseAttribute attribute;

        /**
         * Factory method to create a MatchClause matching on a specific attribute.
         * 
         * @param attribute The attribute to compare against.
         * @param desiredValue The desired value of the attribute.
         */
        protected AttributeMatchClause(MatchClauseAttribute attribute, String desiredValue)
        {
            super(MatchClauseFieldType.ATTRIBUTE, attribute.toString(), desiredValue);
            assert null != desiredValue;
            this.attribute = attribute;
        }

        /**
         * Return the code of the attribute to compare against.
         */
        public MatchClauseAttribute getAttribute()
        {
            return attribute;
        }
    }

    /**
     * Operators for combining MatchClause objects.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static enum SearchOperator
    {
        MATCH_ALL_CLAUSES, MATCH_ANY_CLAUSES
    }

    private SearchOperator operator = SearchOperator.MATCH_ALL_CLAUSES;

    private ArrayList<MatchClause> matchClauses = new ArrayList<MatchClause>();

    /**
     * Set the operator for combining MatchClause objects.
     */
    public void setOperator(SearchOperator operator)
    {
        this.operator = operator;
    }

    /**
     * Gets the operator for combining MatchClause objects. Default value is {@link SearchOperator}
     * .MATCH_ALL_CRITERIA.
     */
    public SearchOperator getOperator()
    {
        return operator;
    }

    /**
     * Get a list of MatchClause objects this SearchCriteria will match against.
     */
    public List<MatchClause> getMatchClauses()
    {
        return Collections.unmodifiableList(matchClauses);
    }

    /**
     * Add a new match clause.
     */
    public void addMatchClause(MatchClause criterion)
    {
        matchClauses.add(criterion);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof SearchCriteria == false)
        {
            return false;
        }

        SearchCriteria other = (SearchCriteria) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(getOperator(), other.getOperator());
        builder.append(getMatchClauses(), other.getMatchClauses());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getOperator());
        builder.append(getMatchClauses());
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append(getOperator());
        builder.append(getMatchClauses());
        return builder.toString();
    }
}
