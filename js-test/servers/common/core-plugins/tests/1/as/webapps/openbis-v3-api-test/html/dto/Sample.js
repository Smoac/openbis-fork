var Sample = function()
{
    this['@type'] = 'Sample';
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
            throw 'Sample type has not been fetched.'
        }
    }

    this.setType = function(type)
    {
        this.type = type;
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

    this.getExperiment = function()
    {
        if (this.getFetchOptions().hasExperiment())
        {
            return experiment;
        }
        else
        {
            throw 'Experiment has not been fetched.'
        }
    }

    this.setExperiment = function(experiment)
    {
        this.experiment = experiment;
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

    this.getParents = function()
    {
        if (this.getFetchOptions().hasParents())
        {
            return parents;
        }
        else
        {
            throw 'Parents has not been fetched.'
        }
    }

    this.setParents = function(parents)
    {
        this.parents = parents;
    }

    this.getChildren = function()
    {
        if (this.getFetchOptions().hasChildren())
        {
            return children;
        }
        else
        {
            throw 'Children has not been fetched.'
        }
    }

    this.setChildren = function(children)
    {
        this.children = children;
    }

    this.getContainer = function()
    {
        if (this.getFetchOptions().hasContainer())
        {
            return container;
        }
        else
        {
            throw 'Container sample has not been fetched.'
        }
    }

    this.setContainer = function(container)
    {
        this.container = container;
    }

    this.getContained = function()
    {
        if (this.getFetchOptions().hasContained())
        {
            return contained;
        }
        else
        {
            throw 'Contained samples has not been fetched.'
        }
    }

    this.setContained = function(contained)
    {
        this.contained = contained;
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
