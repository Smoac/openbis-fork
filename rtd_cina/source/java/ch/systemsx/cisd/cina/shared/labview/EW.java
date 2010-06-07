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
public class EW extends AbstractLVDataElement
{
    private List<String> choices = new ArrayList<String>();

    private Integer value;

    @XmlElement(name = "Choice", namespace = "http://www.ni.com/LVData")
    public void setChoices(List<String> choices)
    {
        this.choices = choices;
    }

    public List<String> getChoices()
    {
        return choices;
    }

    @XmlElement(name = "Val", namespace = "http://www.ni.com/LVData")
    public Integer getValue()
    {
        return value;
    }

    void setValue(Integer value)
    {
        this.value = value;
    }

    public String getChosenValue()
    {
        return choices.get(value);
    }
}
