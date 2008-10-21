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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.server;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleQueryConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleTypePropertyType;

/**
 * Extracts useful information from {@link HttpServletRequest}.
 * 
 * @author Izabela Adamczyk
 */
public class SampleRequestParameters
{
    final SampleType sampleType;

    final List<PropertyType> propertyTypes;

    final private String group;

    final private boolean includeShared;

    final private boolean includeGroup;

    public SampleRequestParameters(HttpServletRequest request)
    {
        group = request.getParameter(SampleQueryConstants.GROUP);

        includeGroup =
                request.getParameter(SampleQueryConstants.INCLUDE_GROUP).equals(
                        SampleQueryConstants.TRUE);

        includeShared =
                request.getParameter(SampleQueryConstants.INCLUDE_SHARED).equals(
                        SampleQueryConstants.TRUE);

        propertyTypes = new ArrayList<PropertyType>();
        int numberOfProperties =
                Integer.parseInt(request.getParameter(SampleQueryConstants.NUMBER_OF_PROPERTIES));
        for (int i = 0; i < numberOfProperties; i++)
        {
            final PropertyType propertyType = new PropertyType();
            propertyType.setInternalNamespace(request.getParameter(
                    SampleQueryConstants.PROPERTY_INTERNAL + i).equals(SampleQueryConstants.TRUE));
            propertyType.setLabel(request.getParameter(SampleQueryConstants.PROPRETY_LABEL + i));
            propertyType.setSimpleCode(request.getParameter(SampleQueryConstants.PROPERTY + i));
            propertyTypes.add(propertyType);
        }

        int partOfDepth =
                Integer.parseInt(request.getParameter(SampleQueryConstants.PARENT_OF_DEPTH));
        int generatedDepth =
                Integer.parseInt(request.getParameter(SampleQueryConstants.GENERATED_FROM_DEPTH));
        sampleType =
                buildSampleType(request.getParameter(SampleQueryConstants.SAMPLE_TYPE),
                        propertyTypes, generatedDepth, partOfDepth);
    }

    public List<PropertyType> getPropertyTypes()
    {
        return propertyTypes;
    }

    public SampleType getSampleType()
    {
        return sampleType;
    }

    public String getFileName()
    {
        return "sample-export" + (isIncludeGroup() ? "-" + getGroup() : "")
                + (isIncludeShared() ? "-SHARED" : "") + "-" + getSampleType().getCode() + "-"
                + getPropertyTypes().size() + ".tsv";
    }

    public boolean isIncludeGroup()
    {
        return includeGroup;
    }

    public boolean isIncludeShared()
    {
        return includeShared;
    }

    public String getGroup()
    {
        return group;
    }

    private SampleType buildSampleType(String code, List<PropertyType> properties,
            int generatedDepth, int partOfDepth)
    {
        final SampleType result = new SampleType();
        result.setCode(code);
        result.setGeneratedFromHierarchyDepth(generatedDepth);
        result.setPartOfHierarchyDepth(partOfDepth);
        List<SampleTypePropertyType> sampleTypePropertyTypes =
                new ArrayList<SampleTypePropertyType>();
        for (PropertyType pt : properties)
        {
            final SampleTypePropertyType sampleTypePropertyType = new SampleTypePropertyType();
            sampleTypePropertyType.setDisplayed(true);
            sampleTypePropertyType.setPropertyType(pt);
            sampleTypePropertyTypes.add(sampleTypePropertyType);
        }
        result.setSampleTypePropertyTypes(sampleTypePropertyTypes);
        return result;
    }
}