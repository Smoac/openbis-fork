/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Izabela Adamczyk
 */
public class Sample implements IsSerializable
{

    String code;

    SampleType sampleType;

    private Group group;

    DatabaseInstance databaseInstance;

    private String identifier;

    private Person registrator;

    private Date registrationDate;

    private Sample container;

    private Sample generatedFrom;

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public SampleType getSampleType()
    {
        return sampleType;
    }

    public void setSampleType(SampleType sampleType)
    {
        this.sampleType = sampleType;
    }

    public void setGroup(Group group)
    {
        this.group = group;

    }

    public Group getGroup()
    {
        return group;
    }

    public DatabaseInstance getDatabaseInstance()
    {
        return databaseInstance;
    }

    public void setDatabaseInstance(DatabaseInstance databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public Person getRegistrator()
    {
        return registrator;
    }

    public void setRegistrator(Person registrator)
    {
        this.registrator = registrator;
    }

    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    public Sample getContainer()
    {
        return container;
    }

    public void setContainer(Sample container)
    {
        this.container = container;
    }

    public Sample getGeneratedFrom()
    {
        return generatedFrom;
    }

    public void setGeneratedFrom(Sample generatedFrom)
    {
        this.generatedFrom = generatedFrom;
    }

}
