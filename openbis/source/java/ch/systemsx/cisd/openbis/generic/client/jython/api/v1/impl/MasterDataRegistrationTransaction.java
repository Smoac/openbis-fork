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

package ch.systemsx.cisd.openbis.generic.client.jython.api.v1.impl;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.jython.api.v1.DataType;
import ch.systemsx.cisd.openbis.generic.client.jython.api.v1.IAbstractType;
import ch.systemsx.cisd.openbis.generic.client.jython.api.v1.IDataSetType;
import ch.systemsx.cisd.openbis.generic.client.jython.api.v1.IDataSetTypeImmutable;
import ch.systemsx.cisd.openbis.generic.client.jython.api.v1.IExperimentType;
import ch.systemsx.cisd.openbis.generic.client.jython.api.v1.IExperimentTypeImmutable;
import ch.systemsx.cisd.openbis.generic.client.jython.api.v1.IMasterDataRegistrationTransaction;
import ch.systemsx.cisd.openbis.generic.client.jython.api.v1.IMaterialType;
import ch.systemsx.cisd.openbis.generic.client.jython.api.v1.IMaterialTypeImmutable;
import ch.systemsx.cisd.openbis.generic.client.jython.api.v1.IPropertyAssignment;
import ch.systemsx.cisd.openbis.generic.client.jython.api.v1.IPropertyType;
import ch.systemsx.cisd.openbis.generic.client.jython.api.v1.IPropertyTypeImmutable;
import ch.systemsx.cisd.openbis.generic.client.jython.api.v1.ISampleType;
import ch.systemsx.cisd.openbis.generic.client.jython.api.v1.ISampleTypeImmutable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * @author Kaloyan Enimanev
 */
public class MasterDataRegistrationTransaction implements IMasterDataRegistrationTransaction
{
    private final EncapsulatedCommonServer commonServer;

    private final List<ExperimentType> createdExperimentTypes = new ArrayList<ExperimentType>();

    private final List<SampleType> createdSampleTypes = new ArrayList<SampleType>();

    private final List<DataSetType> createdDataSetTypes = new ArrayList<DataSetType>();

    private final List<MaterialType> createdMaterialTypes = new ArrayList<MaterialType>();

    private final List<PropertyType> createdPropertyTypes = new ArrayList<PropertyType>();

    private final List<PropertyAssignment> createdAssignments = new ArrayList<PropertyAssignment>();

    private final MasterDataTransactionErrors transactionErrors = new MasterDataTransactionErrors();

    MasterDataRegistrationTransaction(EncapsulatedCommonServer commonServer)
    {
        this.commonServer = commonServer;
    }

    public MasterDataTransactionErrors getTransactionErrors()
    {
        return transactionErrors;
    }

    public boolean hasErrors()
    {
        return transactionErrors.hasErrors();
    }

    public IExperimentType createNewExperimentType(String code)
    {
        ExperimentType experimentType = new ExperimentType(code);
        createdExperimentTypes.add(experimentType);
        return experimentType;
    }

    public IExperimentTypeImmutable getExperimentType(String code)
    {
        return findTypeForCode(commonServer.listExperimentTypes(), code);
    }

    public ISampleType createNewSampleType(String code)
    {
        SampleType sampleType = new SampleType(code);
        createdSampleTypes.add(sampleType);
        return sampleType;
    }

    public ISampleTypeImmutable getSampleType(String code)
    {
        return findTypeForCode(commonServer.listSampleTypes(), code);
    }

    public IDataSetType createNewDataSetType(String code)
    {
        DataSetType dataSetType = new DataSetType(code);
        createdDataSetTypes.add(dataSetType);
        return dataSetType;
    }

    public IDataSetTypeImmutable getDataSetType(String code)
    {
        return findTypeForCode(commonServer.listDataSetTypes(), code);
    }

    public IMaterialType createNewMaterialType(String code)
    {
        MaterialType materialType = new MaterialType(code);
        createdMaterialTypes.add(materialType);
        return materialType;
    }

    public IMaterialTypeImmutable getMaterialType(String code)
    {
        return findTypeForCode(commonServer.listMaterialTypes(), code);
    }

    public IPropertyType createNewPropertyType(String code, DataType dataType)
    {
        PropertyType propertyType = new PropertyType(code, dataType);
        createdPropertyTypes.add(propertyType);
        return propertyType;
    }

    public IPropertyTypeImmutable getPropertyType(String code)
    {
        return findTypeForCode(commonServer.listPropertyTypes(), code);
    }

    public IPropertyAssignment assignToExperimentType(IExperimentTypeImmutable experimentType,
            IPropertyTypeImmutable propertyType)
    {
        return createAssignment(EntityKind.EXPERIMENT, experimentType, propertyType);
    }

    public IPropertyAssignment assignToSampleType(ISampleTypeImmutable sampleType,
            IPropertyTypeImmutable propertyType)
    {
        return createAssignment(EntityKind.SAMPLE, sampleType, propertyType);
    }

    public IPropertyAssignment assignToDataSetType(IDataSetTypeImmutable dataSetType,
            IPropertyTypeImmutable propertyType)
    {
        return createAssignment(EntityKind.DATA_SET, dataSetType, propertyType);
    }

    public IPropertyAssignment assignToMaterialType(IMaterialTypeImmutable materialType,
            IPropertyTypeImmutable propertyType)
    {
        return createAssignment(EntityKind.MATERIAL, materialType, propertyType);
    }

    private <T extends IAbstractType> T findTypeForCode(List<T> types, String code)
    {
        for (T type : types)
        {
            if (type.getCode().equalsIgnoreCase(code))
            {
                return type;
            }
        }
        return null;
    }

    private PropertyAssignment createAssignment(EntityKind entityKind, IAbstractType type,
            IPropertyTypeImmutable propertyType)
    {
        PropertyAssignment assignment =
                new PropertyAssignment(entityKind, type.getCode(), propertyType.getCode());
        createdAssignments.add(assignment);
        return assignment;

    }

    void commit()
    {
        registerExperimentTypes(createdExperimentTypes);
        registerSampleTypes(createdSampleTypes);
        registerDataSetTypes(createdDataSetTypes);
        registerMaterialTypes(createdMaterialTypes);
        registerPropertyTypes(createdPropertyTypes);
        registerPropertyAssignments(createdAssignments);
    }

    private void registerExperimentTypes(List<ExperimentType> experimentTypes)
    {
        for (ExperimentType experimentType : experimentTypes)
        {
            try
            {
                commonServer.registerExperimentType(experimentType);
            } catch (Exception ex)
            {
                transactionErrors.addTypeRegistrationError(ex, experimentType);
            }
        }
    }

    private void registerSampleTypes(List<SampleType> sampleTypes)
    {
        for (SampleType sampleType : sampleTypes)
        {
            try
            {
                commonServer.registerSampleType(sampleType);
            } catch (Exception ex)
            {
                transactionErrors.addTypeRegistrationError(ex, sampleType);
            }
        }
    }

    private void registerDataSetTypes(List<DataSetType> dataSetTypes)
    {
        for (DataSetType dataSetType : dataSetTypes)
        {
            try
            {
                commonServer.registerDataSetType(dataSetType);
            } catch (Exception ex)
            {
                transactionErrors.addTypeRegistrationError(ex, dataSetType);
            }
        }
    }

    private void registerMaterialTypes(List<MaterialType> materialTypes)
    {
        for (MaterialType materialType : materialTypes)
        {
            try
            {
                commonServer.registerMaterialType(materialType);
            } catch (Exception ex)
            {
                transactionErrors.addTypeRegistrationError(ex, materialType);
            }
        }
    }

    private void registerPropertyTypes(List<PropertyType> propertyTypes)
    {
        for (PropertyType propertyType : propertyTypes)
        {
            try
            {
                commonServer.registerPropertyType(propertyType);
            } catch (Exception ex)
            {
                transactionErrors.addTypeRegistrationError(ex, propertyType);
            }
        }
    }

    private void registerPropertyAssignments(List<PropertyAssignment> propertyAssigments)
    {
        for (PropertyAssignment assignment : propertyAssigments)
        {
            try
            {
                commonServer.registerPropertyAssignment(assignment);
            } catch (Exception ex)
            {
                transactionErrors.addPropertyAssignmentError(ex, assignment);
            }
        }
    }
}
