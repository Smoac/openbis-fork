var Tag = function()
{
    this['@type'] = 'Tag';
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

    this.isPrivate = function()
    {
        return isPrivate;
    }

    this.setPrivate = function(isPrivate)
    {
        this.isPrivate = isPrivate;
    }

    this.getRegistrationDate = function()
    {
        return registrationDate;
    }

    this.setRegistrationDate = function(registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    this.getOwner = function()
    {
        if (this.getFetchOptions().hasOwner())
        {
            return owner;
        }
        else
        {
            throw 'Owner has not been fetched.'
        }
    }

    this.setOwner = function(owner)
    {
        this.owner = owner;
    }

}
