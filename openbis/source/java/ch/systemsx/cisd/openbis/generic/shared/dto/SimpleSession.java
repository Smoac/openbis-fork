/*
 * Copyright 2009 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.shared.dto;

/**
 * 
 *
 * @author    Franz-Josef Elmer
 */
public class SimpleSession implements IAuthSession
{
    private static final long serialVersionUID = 1L;
    
    private String userName;
    private String homeGroupCode;
    private PersonPE person;
    
    public SimpleSession()
    {
    }
    
    public SimpleSession(IAuthSession session)
    {
        setUserName(session.getUserName());
        setHomeGroupCode(session.tryGetHomeGroupCode());
        setPerson(session.tryGetPerson());
    }

    public final String getHomeGroupCode()
    {
        return homeGroupCode;
    }

    public final void setHomeGroupCode(String homeGroupCode)
    {
        this.homeGroupCode = homeGroupCode;
    }

    public final PersonPE getPerson()
    {
        return person;
    }

    public final void setPerson(PersonPE person)
    {
        this.person = person;
    }

    public final void setUserName(String userName)
    {
        this.userName = userName;
    }

    public final String getUserName()
    {
        return userName;
    }

    public String tryGetHomeGroupCode()
    {
        return getHomeGroupCode();
    }

    public PersonPE tryGetPerson()
    {
        return getPerson();
    }


}
