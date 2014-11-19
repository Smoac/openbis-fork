var Space = function()
{
    this['@type'] = 'Space';
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

    this.getRegistrationDate = function()
    {
        return registrationDate;
    }

    this.setRegistrationDate = function(registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    this.getRegistrator = function()
    {
        if (this.getFetchOptions().hasRegistrator())
        {
            return registrator;
        }
        else
        {
            throw 'Registrator has not been fetched.'
        }
    }

    this.setRegistrator = function(registrator)
    {
        this.registrator = registrator;
    }

}
