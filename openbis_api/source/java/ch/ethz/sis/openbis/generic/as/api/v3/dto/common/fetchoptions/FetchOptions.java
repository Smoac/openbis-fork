package ch.ethz.sis.openbis.generic.as.api.v3.dto.common.fetchoptions;

import java.io.Serializable;

import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("dto.common.fetchoptions.FetchOptions")
public abstract class FetchOptions<OBJECT> implements Serializable
{

    private static final long serialVersionUID = 1L;

    private Integer count;

    private Integer from;

    private CacheMode cacheMode = CacheMode.NO_CACHE;

    @SuppressWarnings("hiding")
    public FetchOptions<OBJECT> count(Integer count)
    {
        this.count = count;
        return this;
    }

    public Integer getCount()
    {
        return count;
    }

    @SuppressWarnings("hiding")
    public FetchOptions<OBJECT> from(Integer from)
    {
        this.from = from;
        return this;
    }

    public Integer getFrom()
    {
        return from;
    }

    public FetchOptions<OBJECT> cacheMode(CacheMode mode)
    {
        this.cacheMode = mode;
        return this;
    }

    public CacheMode getCacheMode()
    {
        return cacheMode;
    }

    public abstract SortOptions<OBJECT> sortBy();

    public abstract SortOptions<OBJECT> getSortBy();

}
