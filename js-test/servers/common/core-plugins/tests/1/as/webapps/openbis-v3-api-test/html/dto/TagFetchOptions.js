var TagFetchOptions = function()
{
    this['@type'] = 'TagFetchOptions';
    this.fetchOwner = function()
    {
        if (!this.owner)
        {
            this.owner = new PersonFetchOptions();
        }
        return this.owner;
    }

    this.hasOwner = function()
    {
        return this.owner;
    }

}
