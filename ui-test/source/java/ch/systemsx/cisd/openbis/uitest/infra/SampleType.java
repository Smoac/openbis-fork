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

package ch.systemsx.cisd.openbis.uitest.infra;

import java.util.Map;
import java.util.UUID;

/**
 * @author anttil
 */
public class SampleType implements Browsable
{

    private String code;

    private String description;

    private String validationScript;

    private boolean listable;

    private boolean showContainer;

    private boolean showParents;

    private boolean uniqueSubcodes;

    private boolean generateCodes;

    private boolean showParentMetadata;

    private String generatedCodePrefix;

    public SampleType()
    {
        this.code = UUID.randomUUID().toString();
        this.description = "";
        this.validationScript = "";
        this.listable = true;
        this.showContainer = false;
        this.showParents = true;
        this.uniqueSubcodes = false;
        this.generateCodes = false;
        this.showParentMetadata = false;
        this.generatedCodePrefix = "S";
    }

    public String getCode()
    {
        return code;
    }

    public SampleType setCode(String code)
    {
        this.code = code;
        return this;
    }

    public String getDescription()
    {
        return description;
    }

    public SampleType setDescription(String description)
    {
        this.description = description;
        return this;
    }

    public String getValidationScript()
    {
        return validationScript;
    }

    public SampleType setValidationScript(String validationScript)
    {
        this.validationScript = validationScript;
        return this;
    }

    public boolean isListable()
    {
        return listable;
    }

    public SampleType setListable(boolean listable)
    {
        this.listable = listable;
        return this;
    }

    public boolean isShowContainer()
    {
        return showContainer;
    }

    public SampleType setShowContainer(boolean showContainer)
    {
        this.showContainer = showContainer;
        return this;
    }

    public boolean isShowParents()
    {
        return showParents;
    }

    public SampleType setShowParents(boolean showParents)
    {
        this.showParents = showParents;
        return this;
    }

    public boolean isUniqueSubcodes()
    {
        return uniqueSubcodes;
    }

    public SampleType setUniqueSubcodes(boolean uniqueSubcodes)
    {
        this.uniqueSubcodes = uniqueSubcodes;
        return this;
    }

    public boolean isGenerateCodes()
    {
        return generateCodes;
    }

    public SampleType setGenerateCodes(boolean generateCodes)
    {
        this.generateCodes = generateCodes;
        return this;
    }

    public boolean isShowParentMetadata()
    {
        return showParentMetadata;
    }

    public SampleType setShowParentMetadata(boolean showParentMetadata)
    {
        this.showParentMetadata = showParentMetadata;
        return this;
    }

    public String getGeneratedCodePrefix()
    {
        return generatedCodePrefix;
    }

    public SampleType setGeneratedCodePrefix(String generatedCodePrefix)
    {
        this.generatedCodePrefix = generatedCodePrefix;
        return this;
    }

    @Override
    public boolean isRepresentedBy(Map<String, String> row)
    {
        return this.code.equalsIgnoreCase(row.get("Code"));
    }

    @Override
    public String toString()
    {
        return "SampleType " + this.code;
    }

}
