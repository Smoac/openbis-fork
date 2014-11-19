var Attachment = function()
{
    this['@type'] = 'Attachment';
    this.getFetchOptions = function()
    {
        return fetchOptions;
    }

    this.setFetchOptions = function(fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    this.getFileName = function()
    {
        return fileName;
    }

    this.setFileName = function(fileName)
    {
        this.fileName = fileName;
    }

    this.getTitle = function()
    {
        return title;
    }

    this.setTitle = function(title)
    {
        this.title = title;
    }

    this.getDescription = function()
    {
        return description;
    }

    this.setDescription = function(description)
    {
        this.description = description;
    }

    this.getPermlink = function()
    {
        return permlink;
    }

    this.setPermlink = function(permlink)
    {
        this.permlink = permlink;
    }

    this.getLatestVersionPermlink = function()
    {
        return latestVersionPermlink;
    }

    this.setLatestVersionPermlink = function(latestVersionPermlink)
    {
        this.latestVersionPermlink = latestVersionPermlink;
    }

    this.getVersion = function()
    {
        return version;
    }

    this.setVersion = function(version)
    {
        this.version = version;
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

    this.getPreviousVersion = function()
    {
        if (this.getFetchOptions().hasPreviousVersion())
        {
            return previousVersion;
        }
        else
        {
            throw 'Previous version of attachment  has not been fetched.'
        }
    }

    this.setPreviousVersion = function(previousVersion)
    {
        this.previousVersion = previousVersion;
    }

    this.getContent = function()
    {
        if (this.getFetchOptions().hasContent())
        {
            return content;
        }
        else
        {
            throw 'Content has not been fetched.'
        }
    }

    this.setContent = function(content)
    {
        this.content = content;
    }

}
