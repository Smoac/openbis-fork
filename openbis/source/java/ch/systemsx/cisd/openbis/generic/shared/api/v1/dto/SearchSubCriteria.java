package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;

/**
 * A specification of criteria for a subquery about a connected entity.
 * 
 * @author Piotr Buczek
 */
public class SearchSubCriteria implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final SearchCriteria criteria;

    private final SearchableEntityKind targetEntityKind;

    public static SearchSubCriteria createSampleParentCriteria(SearchCriteria criteria)
    {
        return new SearchSubCriteria(SearchableEntityKind.SAMPLE_PARENT, criteria);
    }

    public static SearchSubCriteria createSampleContainerCriteria(SearchCriteria criteria)
    {
        return new SearchSubCriteria(SearchableEntityKind.SAMPLE_CONTAINER, criteria);
    }

    public static SearchSubCriteria createSampleCriteria(SearchCriteria criteria)
    {
        return new SearchSubCriteria(SearchableEntityKind.SAMPLE, criteria);
    }

    public static SearchSubCriteria createExperimentCriteria(SearchCriteria criteria)
    {
        return new SearchSubCriteria(SearchableEntityKind.EXPERIMENT, criteria);
    }

    /**
     * Protected constructor. Use one of the factory methods to instantiate a
     * {@link SearchSubCriteria}.
     * 
     * @param targetEntityKind
     * @param criteria
     */
    protected SearchSubCriteria(SearchableEntityKind targetEntityKind, SearchCriteria criteria)
    {
        this.targetEntityKind = targetEntityKind;
        this.criteria = criteria;
    }

    public SearchCriteria getCriteria()
    {
        return criteria;
    }

    public SearchableEntityKind getTargetEntityKind()
    {
        return targetEntityKind;
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

        SearchSubCriteria other = (SearchSubCriteria) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(getTargetEntityKind(), other.getTargetEntityKind());
        builder.append(getCriteria(), other.getCriteria());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getTargetEntityKind());
        builder.append(getCriteria());
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append(getTargetEntityKind());
        builder.append(getCriteria());
        return builder.toString();
    }
}