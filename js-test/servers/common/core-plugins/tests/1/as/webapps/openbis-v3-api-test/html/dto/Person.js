var Person = function()
{
    this['@type'] = 'Person';
    this.getFetchOptions = function()
    {
        return fetchOptions;
    }

    this.setFetchOptions = function(fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    this.getUserId = function()
    {
        return userId;
    }

    this.setUserId = function(userId)
    {
        this.userId = userId;
    }

    this.getFirstName = function()
    {
        return firstName;
    }

    this.setFirstName = function(firstName)
    {
        this.firstName = firstName;
    }

    this.getLastName = function()
    {
        return lastName;
    }

    this.setLastName = function(lastName)
    {
        this.lastName = lastName;
    }

    this.getEmail = function()
    {
        return email;
    }

    this.setEmail = function(email)
    {
        this.email = email;
    }

    this.getRegistrationDate = function()
    {
        return registrationDate;
    }

    this.setRegistrationDate = function(registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    this.isActive = function()
    {
        return active;
    }

    this.setActive = function(active)
    {
        this.active = active;
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

}
