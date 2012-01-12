/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.ethz.cisd.hcscld;

import static ch.systemsx.cisd.hdf5.HDF5CompoundMemberMapping.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.ethz.cisd.hcscld.Feature.FeatureDataType;
import ch.systemsx.cisd.hdf5.HDF5CompoundMappingHints;
import ch.systemsx.cisd.hdf5.HDF5CompoundMemberInformation;
import ch.systemsx.cisd.hdf5.HDF5CompoundMemberMapping;
import ch.systemsx.cisd.hdf5.HDF5DataTypeInformation;
import ch.systemsx.cisd.hdf5.HDF5EnumerationType;

/**
 * A class that supports defining features.
 * 
 * @author Bernd Rinn
 */
class FeaturesDefinition implements IFeaturesDefinition
{
    private final CellLevelFeatureWritableDataset datasetOrNull;
    
    private String namespaceId;
    
    private FeatureNamespaceKind namespaceKind;
    
    private final List<HDF5CompoundMemberMapping> members;

    private final List<Feature> memberDefinitions;

    FeaturesDefinition(CellLevelFeatureWritableDataset dataset)
    {
        this.datasetOrNull = dataset;
        this.members = new ArrayList<HDF5CompoundMemberMapping>();
        this.memberDefinitions = new ArrayList<Feature>();
    }

    FeaturesDefinition(CellLevelFeatureDataset dataset,
            List<HDF5CompoundMemberInformation> memberInfos)
    {
        this.datasetOrNull = null;
        this.members = null;
        this.memberDefinitions = new ArrayList<Feature>(memberInfos.size());
        for (HDF5CompoundMemberInformation memberInfo : memberInfos)
        {
            final String name = memberInfo.getName();
            final HDF5DataTypeInformation typeInfo = memberInfo.getType();
            final int elemSize = typeInfo.getElementSize();
            switch (typeInfo.getDataClass())
            {
                case BOOLEAN:
                    memberDefinitions.add(new Feature(name, FeatureDataType.BOOL));
                    break;
                case INTEGER:
                    if (elemSize == 1)
                    {
                        memberDefinitions.add(new Feature(name, FeatureDataType.INT8));
                    } else if (elemSize == 2)
                    {
                        memberDefinitions.add(new Feature(name, FeatureDataType.INT16));
                    } else if (elemSize == 4)
                    {
                        memberDefinitions.add(new Feature(name, FeatureDataType.INT32));
                    } else
                    {
                        memberDefinitions.add(new Feature(name, FeatureDataType.INT64));
                    }
                    break;
                case FLOAT:
                    if (elemSize == 4)
                    {
                        memberDefinitions.add(new Feature(name, FeatureDataType.FLOAT32));
                    } else
                    {
                        memberDefinitions.add(new Feature(name, FeatureDataType.FLOAT64));
                    }
                    break;
                case STRING:
                    memberDefinitions.add(new Feature(name, elemSize));
                    break;
                case ENUM:
                    memberDefinitions.add(new Feature(name, memberInfo.tryGetEnumValues()));
                    break;
                default:
                    memberDefinitions.add(new Feature(name, FeatureDataType.OTHER));
            }
        }
    }

    HDF5CompoundMemberMapping[] getMembers(HDF5CompoundMappingHints hintsOrNull)
    {
        return HDF5CompoundMemberMapping.addHints(members.toArray(
                new HDF5CompoundMemberMapping[members.size()]), hintsOrNull);
    }

    public IFeaturesDefinition objectTypeId(String objectTypeId)
    {
        this.namespaceId = objectTypeId.toUpperCase();
        this.namespaceKind = FeatureNamespaceKind.OBJECT_TYPE;
        return this;
    }

    public IFeaturesDefinition companionGroupId(String companionGroupId)
    {
        this.namespaceId = companionGroupId;
        this.namespaceKind = FeatureNamespaceKind.COMPANION_GROUP;
        return this;
    }

    public FeaturesDefinition addInt8Feature(String name)
    {
        checkDataset();
        members.add(mapping(name).memberClass(Byte.TYPE));
        memberDefinitions.add(new Feature(name, FeatureDataType.INT8));
        return this;
    }

    public FeaturesDefinition addInt16Feature(String name)
    {
        checkDataset();
        members.add(mapping(name).memberClass(Short.TYPE));
        memberDefinitions.add(new Feature(name, FeatureDataType.INT16));
        return this;
    }

    public FeaturesDefinition addInt32Feature(String name)
    {
        checkDataset();
        members.add(mapping(name).memberClass(Integer.TYPE));
        memberDefinitions.add(new Feature(name, FeatureDataType.INT32));
        return this;
    }

    public FeaturesDefinition addInt64Feature(String name)
    {
        checkDataset();
        members.add(mapping(name).memberClass(Long.TYPE));
        memberDefinitions.add(new Feature(name, FeatureDataType.INT64));
        return this;
    }

    public FeaturesDefinition addFloat32Feature(String name)
    {
        checkDataset();
        members.add(mapping(name).memberClass(Float.TYPE));
        memberDefinitions.add(new Feature(name, FeatureDataType.FLOAT32));
        return this;
    }

    public FeaturesDefinition addFloat64Feature(String name)
    {
        checkDataset();
        members.add(mapping(name).memberClass(Double.TYPE));
        memberDefinitions.add(new Feature(name, FeatureDataType.FLOAT64));
        return this;
    }

    public FeaturesDefinition addBooleanFeature(String name)
    {
        checkDataset();
        members.add(mapping(name).memberClass(Boolean.TYPE));
        memberDefinitions.add(new Feature(name, FeatureDataType.BOOL));
        return this;
    }

    public FeaturesDefinition addStringFeature(String name, int len)
    {
        checkDataset();
        members.add(mapping(name).memberClass(String.class).length(len));
        memberDefinitions.add(new Feature(name, len));
        return this;
    }

    public FeaturesDefinition addEnumFeature(String name, String enumName, List<String> options)
    {
        checkDataset();
        final HDF5EnumerationType enumType = datasetOrNull.addEnum(enumName, options);
        members.add(mapping(name).enumType(enumType));
        memberDefinitions.add(new Feature(name, options.toArray(new String[options.size()])));
        return this;
    }

    public FeaturesDefinition addEnumFeature(String name, Class<? extends Enum<?>> enumClass)
    {
        checkDataset();
        final HDF5EnumerationType enumType = datasetOrNull.addEnum(enumClass);
        members.add(mapping(name).enumType(enumType));
        memberDefinitions.add(new Feature(name, CellLevelBaseWritableDataset
                .getEnumOptions(enumClass)));
        return this;
    }

    public void create()
    {
        checkDataset();
        checkNamespace();
        datasetOrNull.addFeatureGroupInternal(
                CellLevelFeatureWritableDataset.DEFAULT_FEATURE_GROUP_NAME, this);
    }

    public IFeatureGroup createFeatureGroup(String id)
    {
        checkDataset();
        checkNamespace();
        return datasetOrNull.addFeatureGroup(id, this);
    }

    public List<Feature> getFeatures()
    {
        return Collections.unmodifiableList(memberDefinitions);
    }

    String getNamespaceId()
    {
        return namespaceId;
    }
    
    FeatureNamespaceKind getNamespaceKind()
    {
        return namespaceKind;
    }

    private void checkDataset()
    {
        if (datasetOrNull == null)
        {
            throw new IllegalStateException("This feature group is not writable.");
        }
    }
    
    private void checkNamespace()
    {
        if (namespaceId == null)
        {
            throw new IllegalStateException("No namespace id set.");
        }
        if (namespaceKind == null)
        {
            throw new IllegalStateException("No namespace kind set.");
        }
    }

}
