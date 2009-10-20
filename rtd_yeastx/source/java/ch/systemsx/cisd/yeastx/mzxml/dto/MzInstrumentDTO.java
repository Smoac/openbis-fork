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

package ch.systemsx.cisd.yeastx.mzxml.dto;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Tomasz Pylak
 */
public class MzInstrumentDTO
{
    private MzCategoryValuePairDTO instrumentType;

    private MzCategoryValuePairDTO instrumentManufacturer;

    private MzCategoryValuePairDTO instrumentModel;

    private MzCategoryValuePairDTO methodIonisation;

    @XmlElement(name = "msMassAnalyzer", namespace = MzXmlDTO.NAMESPACE)
    public MzCategoryValuePairDTO getInstrumentType()
    {
        return instrumentType;
    }

    public void setInstrumentType(MzCategoryValuePairDTO instrumentType)
    {
        this.instrumentType = instrumentType;
    }

    @XmlElement(name = "msManufacturer", namespace = MzXmlDTO.NAMESPACE)
    public MzCategoryValuePairDTO getInstrumentManufacturer()
    {
        return instrumentManufacturer;
    }

    public void setInstrumentManufacturer(MzCategoryValuePairDTO instrumentManufacturer)
    {
        this.instrumentManufacturer = instrumentManufacturer;
    }

    @XmlElement(name = "msModel", namespace = MzXmlDTO.NAMESPACE)
    public MzCategoryValuePairDTO getInstrumentModel()
    {
        return instrumentModel;
    }

    public void setInstrumentModel(MzCategoryValuePairDTO instrumentModel)
    {
        this.instrumentModel = instrumentModel;
    }

    @XmlElement(name = "msIonisation", namespace = MzXmlDTO.NAMESPACE)
    public MzCategoryValuePairDTO getMethodIonisation()
    {
        return methodIonisation;
    }

    public void setMethodIonisation(MzCategoryValuePairDTO methodIonisation)
    {
        this.methodIonisation = methodIonisation;
    }
}
