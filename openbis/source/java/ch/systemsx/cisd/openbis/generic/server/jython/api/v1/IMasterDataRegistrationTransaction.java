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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1;

import java.util.List;

/**
 * API for master data registration. Offers methods for creation and retrieval of openBIS types,
 * property assignments, vocabularies etc.
 * 
 * @author Kaloyan Enimanev
 */
public interface IMasterDataRegistrationTransaction
{

    /**
     * Create a new experiment type to register with the openBIS AS.
     * 
     * @param code the experiment type's code.
     */
    IExperimentType createNewExperimentType(String code);

    /**
     * Get an experiment type from the openBIS AS. Returns null if the experiment type does not
     * exist.
     * 
     * @return An experiment type or null
     */
    IExperimentTypeImmutable getExperimentType(String code);

    /**
     * Gets or creates experiment type from the openBIS AS.
     * 
     * @return the already existing type or a freshly created one if it doesn't exist. Setter
     *         methods on the returned type are ignored if the type already exists.
     */
    IExperimentType getOrCreateNewExperimentType(String code);

    /**
     * Return all experiment types existing in the openBIS AS.
     */
    List<IExperimentTypeImmutable> listExperimentTypes();

    /**
     * Create a new sample type to register with the openBIS AS.
     * 
     * @param code the sample type's code.
     */
    ISampleType createNewSampleType(String code);

    /**
     * Get a sample type from the openBIS AS. Returns null if the sample type does not exist.
     * 
     * @return A sample type or null
     */
    ISampleTypeImmutable getSampleType(String code);

    /**
     * Gets or creates sample type from the openBIS AS.
     * 
     * @return the already existing type or a freshly created one if it doesn't exist. Setter
     *         methods on the returned type are ignored if the type already exists.
     */
    ISampleType getOrCreateNewSampleType(String code);

    /**
     * Return all sample types existing in the openBIS AS.
     */
    List<ISampleTypeImmutable> listSampleTypes();

    /**
     * Create a new data set type to register with the openBIS AS.
     * 
     * @param code the data set type's code.
     */
    IDataSetType createNewDataSetType(String code);

    /**
     * Get a data set type from the openBIS AS. Returns null if the data set type does not exist.
     * 
     * @return A data set type or null
     */
    IDataSetTypeImmutable getDataSetType(String code);

    /**
     * Gets or creates data set type from the openBIS AS.
     * 
     * @return the already existing type or a freshly created one if it doesn't exist. Setter
     *         methods on the returned type are ignored if the type already exists.
     */
    IDataSetType getOrCreateNewDataSetType(String code);

    /**
     * Return all data set types existing in the openBIS AS.
     */
    List<IDataSetTypeImmutable> listDataSetTypes();

    /**
     * Create a new material type to register with the openBIS AS.
     * 
     * @param code the material type's code.
     */
    IMaterialType createNewMaterialType(String code);

    /**
     * Get a material type from the openBIS AS. Returns null if the material type does not exist.
     * 
     * @return A material type or null
     */
    IMaterialTypeImmutable getMaterialType(String code);

    /**
     * Gets or creates material type from the openBIS AS.
     * 
     * @return the already existing type or a freshly created one if it doesn't exist. Setter
     *         methods on the returned type are ignored if the type already exists.
     */
    IMaterialType getOrCreateNewMaterialType(String code);

    /**
     * Return all material types existing in the openBIS AS.
     */
    List<IMaterialTypeImmutable> listMaterialTypes();

    /**
     * Create a new property type to register with the openBIS AS.
     * 
     * @param code the property type's code.
     * @param dataType the data type of the property
     */
    IPropertyType createNewPropertyType(String code, DataType dataType);

    /**
     * Get a property type from the openBIS AS. Returns null if the property type does not exist.
     * 
     * @return A property type or null
     */
    IPropertyTypeImmutable getPropertyType(String code);

    /**
     * Gets or creates property type from the openBIS AS.
     * 
     * @param dataType the data type of the property in case it has to be created.
     * @return the already existing type or a freshly created one if it doesn't exist. Setter
     *         methods on the returned type are ignored if the type already exists.
     */
    IPropertyType getOrCreateNewPropertyType(String code, DataType dataType);

    /**
     * Return all property types existing in the openBIS AS.
     */
    List<IPropertyTypeImmutable> listPropertyTypes();

    /**
     * Assigns a property type to an entity type.
     * 
     * @param entityType One of IExperimentTypeImmutable, ISampleTypeImmutable,
     *            IDataSetTypeImmutable, or IMaterialTypeImmutable.
     * @param propertyType The property type to assign to the entity type.
     * @return An object representing the assignment.
     */
    IPropertyAssignment assignPropertyType(IEntityType entityType,
            IPropertyTypeImmutable propertyType);

    /**
     * Return a list of all existing property assignments.
     */
    List<IPropertyAssignmentImmutable> listPropertyAssignments();

    /**
     * Create a new file format type to register with the openBIS AS.
     * 
     * @param code the file format type's code.
     */
    IFileFormatType createNewFileFormatType(String code);

    /**
     * Get a file format type from the openBIS AS. Returns null if the file format type does not
     * exist.
     * 
     * @return A file format type or null
     */
    IFileFormatTypeImmutable getFileFormatType(String code);

    /**
     * Gets or creates file format type from the openBIS AS.
     * 
     * @return the already existing type or a freshly created one if it doesn't exist. Setter
     *         methods on the returned type are ignored if the type already exists.
     */
    IFileFormatType getOrCreateNewFileFormatType(String code);

    /**
     * Return all file format types existing in the openBIS AS.
     */
    List<IFileFormatTypeImmutable> listFileFormatTypes();

    /**
     * Create a new sample type to register with the openBIS AS.
     * 
     * @param code the sample type's code.
     */
    IVocabulary createNewVocabulary(String code);

    /**
     * Get a vocabulary from the openBIS AS. Returns null if the vocabulary does not exist.
     * 
     * @return A vocabulary or null
     */
    IVocabularyImmutable getVocabulary(String code);

    /**
     * Gets or creates vocabulary type from the openBIS AS.
     * 
     * @return the already existing type or a freshly created one if it doesn't exist. Setter
     *         methods on the returned type are ignored if the type already exists.
     */
    IVocabulary getOrCreateNewVocabulary(String code);

    /**
     * Return all vocabularies existing in the openBIS AS.
     */
    List<IVocabularyImmutable> listVocabularies();

    /**
     * Create a new vocabulary term. The resulting object can be added to a vocabulary via the
     * {@link IVocabulary#addTerm(IVocabularyTerm)} method.
     * 
     * @param code the vocabulary term's code
     */
    IVocabularyTerm createNewVocabularyTerm(String code);

}
