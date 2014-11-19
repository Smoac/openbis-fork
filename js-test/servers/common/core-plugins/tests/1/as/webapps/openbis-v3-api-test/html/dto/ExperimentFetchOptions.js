var ExperimentFetchOptions = function()
{
    this['@type'] = 'ExperimentFetchOptions';
    this.fetchType = function()
    {
        if (!this.type)
        {
            this.type = new ExperimentTypeFetchOptions();
        }
        return this.type;
    }

    this.hasType = function()
    {
        return this.type;
    }

    this.fetchProject = function()
    {
        if (!this.project)
        {
            this.project = new ProjectFetchOptions();
        }
        return this.project;
    }

    this.hasProject = function()
    {
        return this.project;
    }

    this.fetchProperties = function()
    {
        if (!this.properties)
        {
            this.properties = new PropertyFetchOptions();
        }
        return this.properties;
    }

    this.hasProperties = function()
    {
        return this.properties;
    }

    this.fetchTags = function()
    {
        if (!this.tags)
        {
            this.tags = new TagFetchOptions();
        }
        return this.tags;
    }

    this.hasTags = function()
    {
        return this.tags;
    }

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

    this.fetchModifier = function()
    {
        if (!this.modifier)
        {
            this.modifier = new PersonFetchOptions();
        }
        return this.modifier;
    }

    this.hasModifier = function()
    {
        return this.modifier;
    }

    this.fetchAttachments = function()
    {
        if (!this.attachments)
        {
            this.attachments = new AttachmentFetchOptions();
        }
        return this.attachments;
    }

    this.hasAttachments = function()
    {
        return this.attachments;
    }

}
