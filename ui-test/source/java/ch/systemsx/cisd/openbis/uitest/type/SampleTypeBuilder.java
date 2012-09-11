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

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import ch.systemsx.cisd.openbis.uitest.infra.ApplicationRunner;

/**
 * @author anttil
 */
public class SampleTypeBuilder implements Builder<SampleType>
{

    private ApplicationRunner openbis;

    private String code;

    private String description;

    private boolean listable;

    private boolean showsContainer;

    private boolean showsParents;

    private boolean hasUniqueSubcodes;

    private boolean generatesCodes;

    private boolean showsParentMetadata;

    private String generatedCodePrefix;

    private Collection<PropertyTypeAssignment> propertyTypeAssignments;

    public SampleTypeBuilder(ApplicationRunner openbis)
    {
        this.openbis = openbis;
        this.code = UUID.randomUUID().toString();
        this.description = "";
        this.listable = true;
        this.showsContainer = false;
        this.showsParents = true;
        this.hasUniqueSubcodes = false;
        this.generatesCodes = false;
        this.showsParentMetadata = false;
        this.generatedCodePrefix = "S";
        this.propertyTypeAssignments = new HashSet<PropertyTypeAssignment>();
    }

    public SampleTypeBuilder thatIsListable()
    {
        this.listable = true;
        return this;
    }

    public SampleTypeBuilder thatIsNotListable()
    {
        this.listable = false;
        return this;
    }

    public SampleTypeBuilder withCode(String code)
    {
        this.code = code;
        return this;
    }

    public SampleTypeBuilder thatShowsParents()
    {
        this.showsParents = true;
        return this;
    }

    public SampleTypeBuilder thatShowsContainer()
    {
        this.showsContainer = true;
        return this;
    }

    @Override
    public SampleType build()
    {
        return openbis.create(new SampleType(code, description, listable, showsContainer,
                showsParents,
                hasUniqueSubcodes, generatesCodes, showsParentMetadata, generatedCodePrefix,
                propertyTypeAssignments));
    }
}
