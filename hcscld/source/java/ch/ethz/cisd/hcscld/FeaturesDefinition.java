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
public class FeaturesDefinition implements IFeaturesDefinition
{
    private final CellLevelFeatureWritableDataset datasetOrNull;

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

    /**
     * Adds a 8-bit integer feature.
     * 
     * @param name The name of the feature.
     * @return This object (for call chaining).
     */
    public FeaturesDefinition addInt8Feature(String name)
    {
        checkDataset();
        members.add(mapping(name).memberClass(Byte.TYPE));
        memberDefinitions.add(new Feature(name, FeatureDataType.INT8));
        return this;
    }

    /**
     * Adds a 16-bit integer feature.
     * 
     * @param name The name of the feature.
     * @return This object (for call chaining).
     */
    public FeaturesDefinition addInt16Feature(String name)
    {
        checkDataset();
        members.add(mapping(name).memberClass(Short.TYPE));
        memberDefinitions.add(new Feature(name, FeatureDataType.INT16));
        return this;
    }

    /**
     * Adds a 32-bit integer feature.
     * 
     * @param name The name of the feature.
     * @return This object (for call chaining).
     */
    public FeaturesDefinition addInt32Feature(String name)
    {
        checkDataset();
        members.add(mapping(name).memberClass(Integer.TYPE));
        memberDefinitions.add(new Feature(name, FeatureDataType.INT32));
        return this;
    }

    /**
     * Adds a 64-bit integer feature.
     * 
     * @param name The name of the feature.
     * @return This object (for call chaining).
     */
    public FeaturesDefinition addInt64Feature(String name)
    {
        checkDataset();
        members.add(mapping(name).memberClass(Long.TYPE));
        memberDefinitions.add(new Feature(name, FeatureDataType.INT64));
        return this;
    }

    /**
     * Adds a 32-bit float feature.
     * 
     * @param name The name of the feature.
     * @return This object (for call chaining).
     */
    public FeaturesDefinition addFloat32Feature(String name)
    {
        checkDataset();
        members.add(mapping(name).memberClass(Float.TYPE));
        memberDefinitions.add(new Feature(name, FeatureDataType.FLOAT32));
        return this;
    }

    /**
     * Adds a 64-bit float feature.
     * 
     * @param name The name of the feature.
     * @return This object (for call chaining).
     */
    public FeaturesDefinition addFloat64Feature(String name)
    {
        checkDataset();
        members.add(mapping(name).memberClass(Double.TYPE));
        memberDefinitions.add(new Feature(name, FeatureDataType.FLOAT64));
        return this;
    }

    /**
     * Adds a boolean feature.
     * 
     * @param name The name of the feature.
     * @return This object (for call chaining).
     */
    public FeaturesDefinition addBooleanFeature(String name)
    {
        checkDataset();
        members.add(mapping(name).memberClass(Boolean.TYPE));
        memberDefinitions.add(new Feature(name, FeatureDataType.BOOL));
        return this;
    }

    /**
     * Adds a string feature.
     * 
     * @param name The name of the feature.
     * @param len The (maximum) length of the string values.
     * @return This object (for call chaining).
     */
    public FeaturesDefinition addStringFeature(String name, int len)
    {
        checkDataset();
        members.add(mapping(name).memberClass(String.class).length(len));
        memberDefinitions.add(new Feature(name, len));
        return this;
    }

    /**
     * Adds an enumeration feature.
     * 
     * @param name The name of the feature.
     * @param enumName The name of the enumeration type.
     * @param options The options defining this enumeration.
     * @return This object (for call chaining).
     */
    public FeaturesDefinition addEnumFeature(String name, String enumName, List<String> options)
    {
        checkDataset();
        final HDF5EnumerationType enumType = datasetOrNull.addEnum(enumName, options);
        members.add(mapping(name).enumType(enumType));
        memberDefinitions.add(new Feature(name, options.toArray(new String[options.size()])));
        return this;
    }

    /**
     * Adds an enumeration feature.
     * 
     * @param name The name of the feature.
     * @param enumClass The class that defines this enumeration type.
     * @return This object (for call chaining).
     */
    public FeaturesDefinition addEnumFeature(String name, Class<? extends Enum<?>> enumClass)
    {
        checkDataset();
        final HDF5EnumerationType enumType = datasetOrNull.addEnum(enumClass);
        members.add(mapping(name).enumType(enumType));
        memberDefinitions.add(new Feature(name, CellLevelBaseWritableDataset
                .getEnumOptions(enumClass)));
        return this;
    }

    /**
     * Creates the default feature group with these feature definitions.
     */
    public void create()
    {
        checkDataset();
        datasetOrNull.addFeatureGroupInternal(
                CellLevelFeatureWritableDataset.DEFAULT_FEATURE_GROUP_NAME, this);
    }

    /**
     * Creates a feature group with given <var>name</var> and these feature definitions.
     * 
     * @param name The name of the feature group.
     * @return The feature group created.
     */
    public IFeatureGroup createFeatureGroup(String name)
    {
        checkDataset();
        return datasetOrNull.addFeatureGroup(name, this);
    }

    /**
     * Returns the features.
     */
    public List<Feature> getFeatures()
    {
        return Collections.unmodifiableList(memberDefinitions);
    }

    private void checkDataset()
    {
        if (datasetOrNull == null)
        {
            throw new IllegalStateException("This feature group is not writable");
        }
    }

}
