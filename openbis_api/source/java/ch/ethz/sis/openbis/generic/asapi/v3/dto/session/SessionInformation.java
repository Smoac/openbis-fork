package ch.ethz.sis.openbis.generic.asapi.v3.dto.session;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.session.SessionInformation")
public class SessionInformation implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private String homeGroupCode;

    @JsonProperty
    private String userName;

    @JsonProperty
    private Person person;

    @JsonProperty
    private Person creatorPerson;

    public String getHomeGroupCode()
    {
        return homeGroupCode;
    }

    public void setHomeGroupCode(String homeGroupCode)
    {
        this.homeGroupCode = homeGroupCode;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public Person getPerson()
    {
        return person;
    }

    public void setPerson(Person person)
    {
        this.person = person;
    }

    public Person getCreatorPerson()
    {
        return creatorPerson;
    }

    public void setCreatorPerson(Person creatorPerson)
    {
        this.creatorPerson = creatorPerson;
    }

}
