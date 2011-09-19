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
import ch.systemsx.cisd.openbis.generic.client.jython.api.v1.IEntityType;
import ch.systemsx.cisd.openbis.generic.client.jython.api.v1.IExperimentType;
import ch.systemsx.cisd.openbis.generic.client.jython.api.v1.IExperimentTypeImmutable;
import ch.systemsx.cisd.openbis.generic.client.jython.api.v1.IFileFormatType;
import ch.systemsx.cisd.openbis.generic.client.jython.api.v1.IFileFormatTypeImmutable;
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

    private final List<FileFormatType> createdFileTypes = new ArrayList<FileFormatType>();

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

    public IFileFormatType createNewFileFormatType(String code)
    {
        FileFormatType fileFormatType = new FileFormatType(code);
        createdFileTypes.add(fileFormatType);
        return fileFormatType;
    }

    public IFileFormatTypeImmutable getFileFormatType(String code)
    {
        return findTypeForCode(commonServer.listFileFormatTypes(), code);
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

    public IPropertyAssignment assignPropertyType(IEntityType entityType,
            IPropertyTypeImmutable propertyType)
    {
        if (entityType instanceof IExperimentTypeImmutable)
        {
            return createAssignment(EntityKind.EXPERIMENT, entityType, propertyType);
        } else if (entityType instanceof ISampleTypeImmutable)
        {
            return createAssignment(EntityKind.SAMPLE, entityType, propertyType);
        } else

        if (entityType instanceof IDataSetTypeImmutable)
        {
            return createAssignment(EntityKind.DATA_SET, entityType, propertyType);
        } else if (entityType instanceof IMaterialTypeImmutable)
        {
            return createAssignment(EntityKind.MATERIAL, entityType, propertyType);
        }

        throw new IllegalArgumentException(
                "The argument entityType must be one of IExperimentTypeImmutable, ISampleTypeImmutable, IDataSetTypeImmutable, or IMaterialTypeImmutable. "
                        + entityType + " is not valid.");
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

    private PropertyAssignment createAssignment(EntityKind entityKind, IEntityType type,
            IPropertyTypeImmutable propertyType)
    {
        PropertyAssignment assignment =
                new PropertyAssignment(entityKind, type.getCode(), propertyType.getCode());
        createdAssignments.add(assignment);
        return assignment;

    }

    void commit()
    {
        registerFileFormatTypes(createdFileTypes);
        registerExperimentTypes(createdExperimentTypes);
        registerSampleTypes(createdSampleTypes);
        registerDataSetTypes(createdDataSetTypes);
        registerMaterialTypes(createdMaterialTypes);
        registerPropertyTypes(createdPropertyTypes);
        registerPropertyAssignments(createdAssignments);
    }

    private void registerFileFormatTypes(List<FileFormatType> fileFormatTypes)
    {
        for (FileFormatType fileFormatType : fileFormatTypes)
        {
            try
            {
                commonServer.registerFileFormatType(fileFormatType);
            } catch (Exception ex)
            {
                transactionErrors.addTypeRegistrationError(ex, fileFormatType);
            }
        }
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
