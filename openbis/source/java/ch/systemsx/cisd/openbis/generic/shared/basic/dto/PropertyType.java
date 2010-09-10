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

import java.io.Serializable;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The <i>GWT</i> version of PropertyTypePE.
 * 
 * @author Izabela Adamczyk
 */
public class PropertyType extends Code<PropertyType> implements IPropertyTypeUpdates,
        IsSerializable, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Long id;

    /**
     * Only used for displaying/viewing. With <code>managedInternally</code> is unambiguous (meaning
     * that <code>simpleCode</code> alone could be not unique).
     * <p>
     * We have to use it, partly because <i>Javascript</i> handle '.' in an object-oriented way.
     * </p>
     */
    private String simpleCode;

    private String label;

    private boolean internalNamespace;

    private boolean managedInternally;

    private DataType dataType;

    private Vocabulary vocabulary;

    private MaterialType materialType;

    private String description;

    private List<SampleTypePropertyType> sampleTypePropertyTypes;

    private List<MaterialTypePropertyType> materialTypePropertyTypes;

    private List<ExperimentTypePropertyType> experimentTypePropertyTypes;

    private List<DataSetTypePropertyType> dataSetTypePropertyTypes;

    // xml type specifig

    private String schema;

    private String transformation;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getSimpleCode()
    {
        return simpleCode;
    }

    public void setSimpleCode(final String simpleCode)
    {
        this.simpleCode = simpleCode;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(final String label)
    {
        this.label = label;
    }

    public boolean isInternalNamespace()
    {
        return internalNamespace;
    }

    public void setInternalNamespace(final boolean internalNamespace)
    {
        this.internalNamespace = internalNamespace;
    }

    public DataType getDataType()
    {
        return dataType;
    }

    public void setDataType(final DataType dataType)
    {
        this.dataType = dataType;
    }

    public Vocabulary getVocabulary()
    {
        return vocabulary;
    }

    public void setVocabulary(final Vocabulary vocabulary)
    {
        this.vocabulary = vocabulary;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    public List<SampleTypePropertyType> getSampleTypePropertyTypes()
    {
        return sampleTypePropertyTypes;
    }

    public void setSampleTypePropertyTypes(
            final List<SampleTypePropertyType> sampleTypePropertyTypes)
    {
        this.sampleTypePropertyTypes = sampleTypePropertyTypes;
    }

    public List<DataSetTypePropertyType> getDataSetTypePropertyTypes()
    {
        return dataSetTypePropertyTypes;
    }

    public void setDataSetTypePropertyTypes(
            final List<DataSetTypePropertyType> dataSetTypePropertyTypes)
    {
        this.dataSetTypePropertyTypes = dataSetTypePropertyTypes;
    }

    public List<MaterialTypePropertyType> getMaterialTypePropertyTypes()
    {
        return materialTypePropertyTypes;
    }

    public void setMaterialTypePropertyTypes(
            final List<MaterialTypePropertyType> materialTypePropertyTypes)
    {
        this.materialTypePropertyTypes = materialTypePropertyTypes;
    }

    public List<ExperimentTypePropertyType> getExperimentTypePropertyTypes()
    {
        return experimentTypePropertyTypes;
    }

    public void setExperimentTypePropertyTypes(
            final List<ExperimentTypePropertyType> experimentTypePropertyTypes)
    {
        this.experimentTypePropertyTypes = experimentTypePropertyTypes;
    }

    public final boolean isManagedInternally()
    {
        return managedInternally;
    }

    public final void setManagedInternally(final boolean managedInternally)
    {
        this.managedInternally = managedInternally;
    }

    public MaterialType getMaterialType()
    {
        return materialType;
    }

    public void setMaterialType(MaterialType materialType)
    {
        this.materialType = materialType;
    }

    public String getSchema()
    {
        return schema;
    }

    public void setSchema(String schema)
    {
        this.schema = schema;
    }

    public String getTransformation()
    {
        return transformation;
    }

    public void setTransformation(String transformation)
    {
        this.transformation = transformation;
    }
}
