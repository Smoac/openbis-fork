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
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * <i>Java Bean</i> which contain information about <i>sample</i>.
 * 
 * @author Izabela Adamczyk
 */
public class Sample implements IsSerializable
{
    public static final Sample[] EMPTY_ARRAY = new Sample[0];

    private String code;

    private SampleType sampleType;

    private Group group;

    private DatabaseInstance databaseInstance;

    private String identifier;

    private Person registrator;

    private Date registrationDate;

    private Sample container;

    private Sample generatedFrom;

    private List<SampleProperty> properties;

    private boolean isInvalid;

    public String getCode()
    {
        return code;
    }

    public void setCode(final String code)
    {
        this.code = code;
    }

    public SampleType getSampleType()
    {
        return sampleType;
    }

    public void setSampleType(final SampleType sampleType)
    {
        this.sampleType = sampleType;
    }

    public void setGroup(final Group group)
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

    public void setDatabaseInstance(final DatabaseInstance databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(final String identifier)
    {
        this.identifier = identifier;
    }

    public Person getRegistrator()
    {
        return registrator;
    }

    public void setRegistrator(final Person registrator)
    {
        this.registrator = registrator;
    }

    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    public void setRegistrationDate(final Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    public Sample getContainer()
    {
        return container;
    }

    public void setContainer(final Sample container)
    {
        this.container = container;
    }

    public Sample getGeneratedFrom()
    {
        return generatedFrom;
    }

    public void setGeneratedFrom(final Sample generatedFrom)
    {
        this.generatedFrom = generatedFrom;
    }

    public List<SampleProperty> getProperties()
    {
        return properties;
    }

    public void setProperties(final List<SampleProperty> properties)
    {
        this.properties = properties;
    }

    public boolean isInvalid()
    {
        return isInvalid;
    }

    public void setInvalid(boolean isInvalid)
    {
        this.isInvalid = isInvalid;
    }
}
