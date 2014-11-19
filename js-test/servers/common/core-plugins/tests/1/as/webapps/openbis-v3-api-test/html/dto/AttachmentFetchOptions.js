var AttachmentFetchOptions = function()
{
    this['@type'] = 'AttachmentFetchOptions';
    this.fetchRegistrator = function()
    {
        if (!this.registrator)
        {
            this.registrator = new PersonFetchOptions();
        }
        return this.registrator;
    }

    this.hasRegistrator = function()
    {
        return this.registrator;
    }

    this.fetchPreviousVersion = function()
    {
        if (!this.previousVersion)
        {
            this.previousVersion = new AttachmentFetchOptions();
        }
        return this.previousVersion;
    }

    this.hasPreviousVersion = function()
    {
        return this.previousVersion;
    }

    this.fetchContent = function()
    {
        if (!this.content)
        {
            this.content = new EmptyFetchOptions();
        }
        return this.content;
    }

    this.hasContent = function()
    {
        return this.content;
    }

}
