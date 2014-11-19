var Experiment = function()
{
    this['@type'] = 'Experiment';
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

    this.getType = function()
    {
        if (this.getFetchOptions().hasType())
        {
            return type;
        }
        else
        {
            throw 'Experiment type has not been fetched.'
        }
    }

    this.setType = function(type)
    {
        this.type = type;
    }

    this.getProject = function()
    {
        if (this.getFetchOptions().hasProject())
        {
            return project;
        }
        else
        {
            throw 'Project has not been fetched.'
        }
    }

    this.setProject = function(project)
    {
        this.project = project;
    }

    this.getProperties = function()
    {
        if (this.getFetchOptions().hasProperties())
        {
            return properties;
        }
        else
        {
            throw 'Properties has not been fetched.'
        }
    }

    this.setProperties = function(properties)
    {
        this.properties = properties;
    }

    this.getTags = function()
    {
        if (this.getFetchOptions().hasTags())
        {
            return tags;
        }
        else
        {
            throw 'Tags has not been fetched.'
        }
    }

    this.setTags = function(tags)
    {
        this.tags = tags;
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

    this.getAttachments = function()
    {
        if (this.getFetchOptions().hasAttachments())
        {
            return attachments;
        }
        else
        {
            throw 'Attachments has not been fetched.'
        }
    }

    this.setAttachments = function(attachments)
    {
        this.attachments = attachments;
    }

}
