/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.type;

import java.util.Arrays;
import java.util.Collection;

import ch.systemsx.cisd.openbis.uitest.infra.application.GuiApplicationRunner;
import ch.systemsx.cisd.openbis.uitest.page.tab.BrowserRow;

/**
 * @author anttil
 */
public class SampleType implements Browsable, EntityType
{

    private final String code;

    private String description;

    private boolean listable;

    private boolean showContainer;

    private boolean showParents;

    private boolean uniqueSubcodes;

    private boolean generateCodes;

    private boolean showParentMetadata;

    private String generatedCodePrefix;

    private Collection<PropertyTypeAssignment> propertyTypeAssignments;

    SampleType(String code, String description, boolean listable, boolean showContainer,
            boolean showParents, boolean uniqueSubcodes, boolean generateCodes,
            boolean showParentMetadata, String generatedCodePrefix,
            Collection<PropertyTypeAssignment> propertyTypeAssignments)
    {
        this.code = code;
        this.description = description;
        this.listable = listable;
        this.showContainer = showContainer;
        this.showParents = showParents;
        this.uniqueSubcodes = uniqueSubcodes;
        this.generateCodes = generateCodes;
        this.showParentMetadata = showParentMetadata;
        this.generatedCodePrefix = generatedCodePrefix;
        this.propertyTypeAssignments = propertyTypeAssignments;
    }

    @Override
    public String toString()
    {
        return "SampleType " + this.code;
    }

    @Override
    public String getCode()
    {
        return code;
    }

    public String getDescription()
    {
        return description;
    }

    public boolean isListable()
    {
        return listable;
    }

    public boolean isShowContainer()
    {
        return showContainer;
    }

    public boolean isShowParents()
    {
        return showParents;
    }

    public boolean isUniqueSubcodes()
    {
        return uniqueSubcodes;
    }

    public boolean isGenerateCodes()
    {
        return generateCodes;
    }

    public boolean isShowParentMetadata()
    {
        return showParentMetadata;
    }

    public String getGeneratedCodePrefix()
    {
        return generatedCodePrefix;
    }

    public Collection<PropertyTypeAssignment> getPropertyTypeAssignments()
    {
        return propertyTypeAssignments;
    }

    void setDescription(String description)
    {
        this.description = description;
    }

    void setListable(boolean listable)
    {
        this.listable = listable;
    }

    void setShowContainer(boolean showContainer)
    {
        this.showContainer = showContainer;
    }

    void setShowParents(boolean showParents)
    {
        this.showParents = showParents;
    }

    void setUniqueSubcodes(boolean uniqueSubcodes)
    {
        this.uniqueSubcodes = uniqueSubcodes;
    }

    void setGenerateCodes(boolean generateCodes)
    {
        this.generateCodes = generateCodes;
    }

    void setShowParentMetadata(boolean showParentMetadata)
    {
        this.showParentMetadata = showParentMetadata;
    }

    void setGeneratedCodePrefix(String generatedCodePrefix)
    {
        this.generatedCodePrefix = generatedCodePrefix;
    }

    void setPropertyTypeAssignments(Collection<PropertyTypeAssignment> propertyTypeAssignments)
    {
        this.propertyTypeAssignments = propertyTypeAssignments;
    }

    @Override
    public BrowserRow getBrowserContent(GuiApplicationRunner openbis)
    {
        return openbis.browseTo(this);
    }

    @Override
    public Collection<String> getColumns()
    {
        return Arrays.asList("Code", "Description", "Database Instance", "Validation Script",
                "Listable?", "Show Container?", "Show Parents?", "Unique Subcodes",
                "Generate Codes Automatically", "Show Parent Metadata?", "Generated Code Prefix");
    }

    @Override
    public int hashCode()
    {
        return code.toUpperCase().hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof SampleType))
        {
            return false;
        }
        return code.equalsIgnoreCase(((SampleType) o).getCode());
    }
}
