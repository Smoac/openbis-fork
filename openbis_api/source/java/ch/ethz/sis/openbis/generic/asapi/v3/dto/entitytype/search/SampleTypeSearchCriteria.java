package ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchCriteriaToStringBuilder;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.entitytype.search.SampleTypeSearchCriteria")
public class SampleTypeSearchCriteria extends EntityTypeSearchCriteria
{

    private static final long serialVersionUID = 1L;

    public ListableSampleTypeSearchCriteria withListable() {
        return with(new ListableSampleTypeSearchCriteria());
    }
    
    @Override
    protected SearchCriteriaToStringBuilder createBuilder()
    {
        SearchCriteriaToStringBuilder builder = super.createBuilder();
        builder.setName("SAMPLES_TYPE");
        return builder;
    }

}
