var SampleFetchOptions = function()
{
    this['@type'] = 'SampleFetchOptions';
    this.fetchType = function()
    {
        if (!this.type)
        {
            this.type = new SampleTypeFetchOptions();
        }
        return this.type;
    }

    this.hasType = function()
    {
        return this.type;
    }

    this.fetchSpace = function()
    {
        if (!this.space)
        {
            this.space = new SpaceFetchOptions();
        }
        return this.space;
    }

    this.hasSpace = function()
    {
        return this.space;
    }

    this.fetchExperiment = function()
    {
        if (!this.experiment)
        {
            this.experiment = new ExperimentFetchOptions();
        }
        return this.experiment;
    }

    this.hasExperiment = function()
    {
        return this.experiment;
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

    this.fetchParents = function()
    {
        if (!this.parents)
        {
            this.parents = new SampleFetchOptions();
        }
        return this.parents;
    }

    this.hasParents = function()
    {
        return this.parents;
    }

    this.fetchChildren = function()
    {
        if (!this.children)
        {
            this.children = new SampleFetchOptions();
        }
        return this.children;
    }

    this.hasChildren = function()
    {
        return this.children;
    }

    this.fetchContainer = function()
    {
        if (!this.container)
        {
            this.container = new SampleFetchOptions();
        }
        return this.container;
    }

    this.hasContainer = function()
    {
        return this.container;
    }

    this.fetchContained = function()
    {
        if (!this.contained)
        {
            this.contained = new SampleFetchOptions();
        }
        return this.contained;
    }

    this.hasContained = function()
    {
        return this.contained;
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
