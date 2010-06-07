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

package ch.systemsx.cisd.cina.shared.labview;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class Cluster
{
    private String name;

    private int numberOfElements;

    private List<LVDataString> strings = new ArrayList<LVDataString>();

    private List<LVDataTimestamp> timestamps = new ArrayList<LVDataTimestamp>();

    private List<DBL> dbls = new ArrayList<DBL>();

    private List<I32> i32s = new ArrayList<I32>();

    private List<U8> u8s = new ArrayList<U8>();

    private List<U32> u32s = new ArrayList<U32>();

    private List<LVDataBoolean> booleans = new ArrayList<LVDataBoolean>();

    private List<EW> ews = new ArrayList<EW>();

    private List<Cluster> clusters = new ArrayList<Cluster>();

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
    public int getNumberOfElements()
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

    @XmlElement(name = "U8", namespace = "http://www.ni.com/LVData")
    public List<U8> getU8s()
    {
        return u8s;
    }

    public void setU8s(List<U8> u8s)
    {
        this.u8s = u8s;
    }

    @XmlElement(name = "U32", namespace = "http://www.ni.com/LVData")
    public void setU32s(List<U32> u32s)
    {
        this.u32s = u32s;
    }

    public List<U32> getU32s()
    {
        return u32s;
    }

    @XmlElement(name = "Boolean", namespace = "http://www.ni.com/LVData")
    public List<LVDataBoolean> getBooleans()
    {
        return booleans;
    }

    public void setBooleans(List<LVDataBoolean> booleans)
    {
        this.booleans = booleans;
    }

    @XmlElement(name = "EW", namespace = "http://www.ni.com/LVData")
    public void setEws(List<EW> ews)
    {
        this.ews = ews;
    }

    public List<EW> getEws()
    {
        return ews;
    }

    @XmlElement(name = "Cluster", namespace = "http://www.ni.com/LVData")
    public List<Cluster> getClusters()
    {
        return clusters;
    }

    public void setClusters(List<Cluster> clusters)
    {
        this.clusters = clusters;
    }
}
