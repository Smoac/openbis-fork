/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cina.dss.labview;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class Cluster
{
    private String name;

    private int numberOfElements;

    private List<LVDataString> strings;

    private List<LVDataTimestamp> timestamps;

    private List<DBL> dbls;

    private List<I32> i32s;

    @XmlElement(name = "Name", namespace = "http://www.ni.com/LVData")
    public String getName()
    {
        return name;
    }

    void setName(String name)
    {
        this.name = name;
    }

    @XmlElement(name = "NumElts", namespace = "http://www.ni.com/LVData")
    int getNumberOfElements()
    {
        return numberOfElements;
    }

    void setNumberOfElements(int numberOfElements)
    {
        this.numberOfElements = numberOfElements;
    }

    @XmlElement(name = "String", namespace = "http://www.ni.com/LVData")
    public List<LVDataString> getStrings()
    {
        return strings;
    }

    public void setStrings(List<LVDataString> strings)
    {
        this.strings = strings;
    }

    @XmlElement(name = "Timestamp", namespace = "http://www.ni.com/LVData")
    public List<LVDataTimestamp> getTimestamps()
    {
        return timestamps;
    }

    public void setTimestamps(List<LVDataTimestamp> timestamps)
    {
        this.timestamps = timestamps;
    }

    @XmlElement(name = "DBL", namespace = "http://www.ni.com/LVData")
    public List<DBL> getDbls()
    {
        return dbls;
    }

    public void setDbls(List<DBL> dbls)
    {
        this.dbls = dbls;
    }

    @XmlElement(name = "I32", namespace = "http://www.ni.com/LVData")
    public List<I32> getI32s()
    {
        return i32s;
    }

    public void setI32s(List<I32> i32s)
    {
        this.i32s = i32s;
    }

}
