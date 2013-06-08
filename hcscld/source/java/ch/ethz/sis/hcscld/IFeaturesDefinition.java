/*
 * Copyright 2011-2013 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.hcscld;

import java.util.List;

/**
 * An interface for defining features.
 * <p>
 * The features are only made persistent when one of the methods {@link #create()} or
 * {@link #createFeatureGroup(String)} is called. Only {@link #createFeatureGroup(String)} supports
 * creation of multiple feature groups.
 * 
 * @author Bernd Rinn
 */
public interface IFeaturesDefinition
{
    /**
     * Adds a 8-bit integer feature (corresponding to the Java type <code>byte</code>).
     * 
     * @param name The name of the feature.
     * @return This object (for call chaining).
     */
    public IFeaturesDefinition addInt8Feature(String name);

    /**
     * Adds a 16-bit integer feature (corresponding to the Java type <code>short</code>).
     * 
     * @param name The name of the feature.
     * @return This object (for call chaining).
     */
    public IFeaturesDefinition addInt16Feature(String name);

    /**
     * Adds a 32-bit integer feature (corresponding to the Java type <code>int</code>).
     * 
     * @param name The name of the feature.
     * @return This object (for call chaining).
     */
    public IFeaturesDefinition addInt32Feature(String name);

    /**
     * Adds a 64-bit integer feature (corresponding to the Java type <code>long</code>).
     * 
     * @param name The name of the feature.
     * @return This object (for call chaining).
     */
    public IFeaturesDefinition addInt64Feature(String name);

    /**
     * Adds a 32-bit float feature (corresponding to the Java type <code>float</code>).
     * 
     * @param name The name of the feature.
     * @return This object (for call chaining).
     */
    public IFeaturesDefinition addFloat32Feature(String name);

    /**
     * Adds a 64-bit float feature (corresponding to the Java type <code>double</code>).
     * 
     * @param name The name of the feature.
     * @return This object (for call chaining).
     */
    public IFeaturesDefinition addFloat64Feature(String name);

    /**
     * Adds a boolean feature (corresponding to the Java type <code>boolean</code>).
     * 
     * @param name The name of the feature.
     * @return This object (for call chaining).
     */
    public IFeaturesDefinition addBooleanFeature(String name);

    /**
     * Adds a string feature (corresponding to the Java type <code>java.lang.String</code>).
     * 
     * @param name The name of the feature.
     * @param len The (maximum) length of the string values.
     * @return This object (for call chaining).
     */
    public IFeaturesDefinition addStringFeature(String name, int len);

    /**
     * Adds an enumeration feature (corresponding to a Java <code>enum</code>).
     * 
     * @param name The name of the feature.
     * @param enumName The name of the enumeration type.
     * @param options The options defining this enumeration.
     * @return This object (for call chaining).
     */
    public IFeaturesDefinition addEnumFeature(String name, String enumName, List<String> options);

    /**
     * Adds an enumeration feature (corresponding to a Java <code>enum</code>).
     * 
     * @param name The name of the feature.
     * @param enumClass The class that defines this enumeration type.
     * @return This object (for call chaining).
     */
    public IFeaturesDefinition addEnumFeature(String name, Class<? extends Enum<?>> enumClass);

    /**
     * Creates the default feature group with these feature definitions.
     * <p>
     * <i>This method and {@link #createFeatureGroup(String)} are mutual exclusive on a dataset.</i>
     * 
     * @see ICellLevelFeatureWritableDataset#writeFeatures(ImageId, Object[][])
     */
    public void create();

    /**
     * Creates a feature group with given <var>name</var> and these feature definitions.
     * <p>
     * <i>This method and {@link #create()} are mutual exclusive on a dataset.</i>
     * 
     * @param id The identifier of the feature group.
     * @return The feature group created.
     */
    public IFeatureGroup createFeatureGroup(String id);

    /**
     * Returns the features defined.
     * 
     * @return The list of features.
     */
    public List<Feature> getFeatures();

}