var PersonFetchOptions = function()
{
    this['@type'] = 'PersonFetchOptions';
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

}
