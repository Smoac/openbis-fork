package ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.SortOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("dto.fetchoptions.EmptyFetchOptions")
public class EmptyFetchOptions extends FetchOptions<Void>
{

    private static final long serialVersionUID = 1L;

    @Override
    public SortOptions<Void> sortBy()
    {
        return null;
    }

    @Override
    public SortOptions<Void> getSortBy()
    {
        return null;
    }

}
