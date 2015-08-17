package ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions;

import java.io.Serializable;

import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("dto.fetchoptions.FetchOptions")
public abstract class FetchOptions implements Serializable
{

    private static final long serialVersionUID = 1L;

    private Integer pageSize;

    private Integer pageIndex;

    private Integer cacheMode;

    public FetchOptions count(Integer size)
    {
        this.pageSize = size;
        return this;
    }

    public Integer getPageSize()
    {
        return pageSize;
    }

    public FetchOptions from(Integer index)
    {
        this.pageIndex = index;
        return this;
    }

    public Integer getPageIndex()
    {
        return pageIndex;
    }

    public FetchOptions cacheMode(Integer mode)
    {
        this.cacheMode = mode;
        return this;
    }

    public Integer getCacheMode()
    {
        return cacheMode;
    }

}
