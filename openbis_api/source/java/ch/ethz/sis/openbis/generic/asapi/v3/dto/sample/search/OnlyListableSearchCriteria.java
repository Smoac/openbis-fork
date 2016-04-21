package ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractCompositeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchCriteriaToStringBuilder;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.sample.search.OnlyListableSearchCriteria")
public class OnlyListableSearchCriteria extends AbstractCompositeSearchCriteria
{

    private static final long serialVersionUID = 1L;

    @Override
    protected SearchCriteriaToStringBuilder createBuilder()
    {
        SearchCriteriaToStringBuilder builder = super.createBuilder();
        builder.setName("ONLY_LISTABLE_SAMPLES_SEARCH");
        return builder;
    }

}
