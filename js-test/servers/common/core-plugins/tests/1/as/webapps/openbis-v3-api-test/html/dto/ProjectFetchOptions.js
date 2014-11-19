var ProjectFetchOptions = function()
{
    this['@type'] = 'ProjectFetchOptions';
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

}
