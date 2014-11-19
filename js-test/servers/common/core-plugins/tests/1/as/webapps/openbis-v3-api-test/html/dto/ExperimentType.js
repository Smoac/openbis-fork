var ExperimentType = function()
{
    this['@type'] = 'ExperimentType';
    this.getFetchOptions = function()
    {
        return fetchOptions;
    }

    this.setFetchOptions = function(fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    this.getPermId = function()
    {
        return permId;
    }

    this.setPermId = function(permId)
    {
        this.permId = permId;
    }

    this.getCode = function()
    {
        return code;
    }

    this.setCode = function(code)
    {
        this.code = code;
    }

    this.getDescription = function()
    {
        return description;
    }

    this.setDescription = function(description)
    {
        this.description = description;
    }

    this.getModificationDate = function()
    {
        return modificationDate;
    }

    this.setModificationDate = function(modificationDate)
    {
        this.modificationDate = modificationDate;
    }

}
