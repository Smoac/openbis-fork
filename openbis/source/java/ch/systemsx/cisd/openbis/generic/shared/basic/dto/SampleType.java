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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.common.annotation.CollectionMapping;

/**
 * A {@link EntityType} extension for <i>Sample Type</i>.
 * 
 * @author Izabela Adamczyk
 */
public final class SampleType extends EntityType implements IsSerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private int generatedFromHierarchyDepth;

    private int partOfHierarchyDepth;

    private boolean listable;

    private List<SampleTypePropertyType> sampleTypePropertyTypes;

    public final void setGeneratedFromHierarchyDepth(final int generatedFromHierarchyDepth)
    {
        this.generatedFromHierarchyDepth = generatedFromHierarchyDepth;
    }

    public final void setPartOfHierarchyDepth(final int partOfHierarchyDepth)
    {
        this.partOfHierarchyDepth = partOfHierarchyDepth;
    }

    public final int getGeneratedFromHierarchyDepth()
    {
        return generatedFromHierarchyDepth;
    }

    public final int getPartOfHierarchyDepth()
    {
        return partOfHierarchyDepth;
    }

    @Override
    public final List<SampleTypePropertyType> getAssignedPropertyTypes()
    {
        return sampleTypePropertyTypes;
    }

    @CollectionMapping(collectionClass = ArrayList.class, elementClass = SampleTypePropertyType.class)
    public final void setSampleTypePropertyTypes(
            final List<SampleTypePropertyType> sampleTypePropertyTypes)
    {
        this.sampleTypePropertyTypes = sampleTypePropertyTypes;
    }

    public final boolean isListable()
    {
        return listable;
    }

    public final void setListable(final boolean listable)
    {
        this.listable = listable;
    }

}
