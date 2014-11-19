var Project = function()
{
    this['@type'] = 'Project';
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

    this.getIdentifier = function()
    {
        return identifier;
    }

    this.setIdentifier = function(identifier)
    {
        this.identifier = identifier;
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

    this.getModificationDate = function()
    {
        return modificationDate;
    }

    this.setModificationDate = function(modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    this.getSpace = function()
    {
        if (this.getFetchOptions().hasSpace())
        {
            return space;
        }
        else
        {
            throw 'Space has not been fetched.'
        }
    }

    this.setSpace = function(space)
    {
        this.space = space;
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

    this.getModifier = function()
    {
        if (this.getFetchOptions().hasModifier())
        {
            return modifier;
        }
        else
        {
            throw 'Modifier has not been fetched.'
        }
    }

    this.setModifier = function(modifier)
    {
        this.modifier = modifier;
    }

}
