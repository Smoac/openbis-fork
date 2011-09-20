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
import java.util.List;

import ch.systemsx.cisd.hdf5.HDF5CompoundMemberMapping;
import ch.systemsx.cisd.hdf5.HDF5EnumerationType;

/**
 * A class that supports defining features.
 * 
 * @author Bernd Rinn
 */
public class FeaturesDefinition
{
    private CellLevelFeatureWritableDataset dataset;

    private final List<HDF5CompoundMemberMapping> features =
            new ArrayList<HDF5CompoundMemberMapping>();

    FeaturesDefinition(CellLevelFeatureWritableDataset dataset)
    {
        this.dataset = dataset;
    }

    HDF5CompoundMemberMapping[] getFeatures()
    {
        return features.toArray(new HDF5CompoundMemberMapping[features.size()]);
    }

    /**
     * Adds a 8-bit integer feature.
     * 
     * @param name The name of the feature.
     * @return This object (for call chaining).
     */
    public FeaturesDefinition addInt8Feature(String name)
    {
        features.add(mapping(name).memberClass(Byte.TYPE));
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
        features.add(mapping(name).memberClass(Short.TYPE));
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
        features.add(mapping(name).memberClass(Integer.TYPE));
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
        features.add(mapping(name).memberClass(Long.TYPE));
        return this;
    }

    /**
     * Adds a 32-bit float feature.
     * 
     * @param name The name of the feature.
     * @return This object (for call chaining).
     */
    public FeaturesDefinition addFloatSinglePrecisionFeature(String name)
    {
        features.add(mapping(name).memberClass(Float.TYPE));
        return this;
    }

    /**
     * Adds a 64-bit float feature.
     * 
     * @param name The name of the feature.
     * @return This object (for call chaining).
     */
    public FeaturesDefinition addFloatDoublePrecisionFeature(String name)
    {
        features.add(mapping(name).memberClass(Double.TYPE));
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
        features.add(mapping(name).memberClass(Boolean.TYPE));
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
        features.add(mapping(name).memberClass(String.class).length(len));
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
        final HDF5EnumerationType enumType = dataset.addEnum(enumName, options);
        features.add(mapping(name).enumType(enumType));
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
        final HDF5EnumerationType enumType = dataset.addEnum(enumClass);
        features.add(mapping(name).enumType(enumType));
        return this;
    }

}
