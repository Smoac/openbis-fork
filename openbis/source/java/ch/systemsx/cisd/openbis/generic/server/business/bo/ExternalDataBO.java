/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProcedureTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedurePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedureTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SourceType;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.DataSetTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.ProcedureTypeCode;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ExternalDataBO extends AbstractBusinessObject implements IExternalDataBO
{
    private ExternalDataPE externalData;
    private SourceType sourceType;
    
    public ExternalDataBO(IDAOFactory daoFactory, Session session)
    {
        super(daoFactory, session);
    }

    public ExternalDataPE getExternalData()
    {
        return externalData;
    }
    
    public void define(ExternalData data, ProcedurePE procedure, SamplePE sample,
            SourceType type)
    {
        assert data != null : "Undefined data.";
        final DataSetType dataSetType = data.getDataSetType();
        assert dataSetType != null : "Undefined data set type.";
        final FileFormatType fileFormatType = data.getFileFormatType();
        assert fileFormatType != null : "Undefined file format type.";
        final String location = data.getLocation();
        assert location != null : "Undefined location.";
        final LocatorType locatorType = data.getLocatorType();
        assert locatorType != null : "Undefined location type.";
        assert type != null : "Undefined source type.";
        assert procedure != null : "Unspecified procedure";

        sourceType = type;
        externalData = new ExternalDataPE();
        externalData.setProcedure(procedure);
        externalData.setDataProducerCode(data.getDataProducerCode());
        externalData.setProductionDate(data.getProductionDate());
        externalData.setCode(data.getCode());
        externalData.setDataSetType(getDataSetType(dataSetType));
        externalData.setFileFormatType(getFileFomatType(fileFormatType));
        externalData.setComplete(data.getComplete());
        externalData.setLocation(location);
        externalData.setStorageFormatVocabularyTerm(tryToFindStorageFormatTerm(data.getStorageFormat()));
        externalData.setLocatorType(getLocatorTypeDAO().tryToFindLocatorTypeByCode(
                locatorType.getCode()));
        final String parentDataSetCode = data.getParentDataSetCode();
        if (parentDataSetCode != null)
        {
            final Set<DataPE> parents = new HashSet<DataPE>();
            ExperimentPE experiment = procedure.getExperiment();
            parents.add(getOrCreateParentData(parentDataSetCode, experiment, sample));
            externalData.setParents(parents);
        }
        sourceType.setSample(externalData, sample);
    }
    
    private VocabularyTermPE tryToFindStorageFormatTerm(StorageFormat storageFormat)
    {
        IVocabularyDAO vocabularyDAO = getVocabularyDAO();
        VocabularyPE vocabulary = vocabularyDAO.tryFindVocabularyByCode(StorageFormat.VOCABULARY_CODE);
        Set<VocabularyTermPE> terms = vocabulary.getTerms();
        for (VocabularyTermPE term : terms)
        {
            if (storageFormat.getCode().equals(term.getCode()))
            {
                return term;
            }
        }
        return null;
    }

    private final DataSetTypePE getDataSetType(final DataSetType dataSetType)
    {
        final String dataSetTypeCode = dataSetType.getCode();
        final DataSetTypePE dataSetTypeOrNull =
                getDataSetTypeDAO().tryToFindDataSetTypeByCode(dataSetTypeCode);
        if (dataSetTypeOrNull == null)
        {
            throw UserFailureException.fromTemplate("There is no data set type with code '%s'",
                    dataSetTypeCode);
        }
        return dataSetTypeOrNull;
    }

    private final FileFormatTypePE getFileFomatType(final FileFormatType fileFormatType)
    {
        final String fileFormatTypeCode = fileFormatType.getCode();
        final FileFormatTypePE fileFormatTypeOrNull =
                getFileFormatTypeDAO().tryToFindFileFormatTypeByCode(fileFormatTypeCode);
        if (fileFormatTypeOrNull == null)
        {
            throw UserFailureException.fromTemplate("There is no file format type with code '%s'",
                    fileFormatTypeCode);
        }
        return fileFormatTypeOrNull;
    }
    
    private final DataPE getOrCreateParentData(final String parentDataSetCode,
            ExperimentPE experiment, SamplePE sample)
    {
        assert parentDataSetCode != null : "Unspecified parent data set code.";

        final IExternalDataDAO dataDAO = getExternalDataDAO();
        DataPE parent = dataDAO.tryToFindDataSetByCode(parentDataSetCode);
        if (parent == null)
        {
            final ProcedurePE procedure = getOrCreateUnknownProcedure(experiment);
            parent = new DataPE();
            parent.setCode(parentDataSetCode);
            String code = DataSetTypeCode.UNKNOWN.getCode();
            parent.setDataSetType(getDataSetTypeDAO().tryToFindDataSetTypeByCode(code));
            parent.setProcedure(procedure);
            parent.setSampleDerivedFrom(sample);
            parent.setPlaceholder(true);
            dataDAO.createDataSet(parent);
        }
        return parent;
    }

    private final ProcedurePE getOrCreateUnknownProcedure(ExperimentPE experiment)
    {
        List<ProcedurePE> procedures = experiment.getProcedures();
        for (ProcedurePE procedure : procedures)
        {
            if (procedure.getProcedureType().getCode().equals(ProcedureTypeCode.UNKNOWN.getCode()))
            {
                return procedure;
            }
        }
        ProcedurePE procedure = new ProcedurePE();
        procedure.setExperiment(experiment);
        final IProcedureTypeDAO procedureTypeDAO = getProcedureTypeDAO();
        final String code = ProcedureTypeCode.UNKNOWN.getCode();
        final ProcedureTypePE procedureTypeDTO = procedureTypeDAO.tryFindProcedureTypeByCode(code);
        procedure.setProcedureType(procedureTypeDTO);
        getProcedureDAO().createProcedure(procedure);
        return procedure;
    }

    public void save() throws UserFailureException
    {
        assert externalData != null : "Undefined external data.";
        IExternalDataDAO externalDataDAO = getExternalDataDAO();
        String dataCode = externalData.getCode();
        ExternalDataPE data = externalDataDAO.tryToFindDataSetByCode(dataCode);
        if (data == null)
        {
            externalDataDAO.createDataSet(externalData);
        } else
        {
            if (data.isPlaceholder() == false)
            {
                throw new UserFailureException("Already existing data set for code '"
                        + dataCode + "' can not be updated by data set " + externalData);
            }
            data.setPlaceholder(false);
            data.setComplete(externalData.getComplete());
            data.setDataProducerCode(externalData.getDataProducerCode());
            data.setDataSetType(externalData.getDataSetType());
            data.setFileFormatType(externalData.getFileFormatType());
            data.setLocatorType(externalData.getLocatorType());
            data.setLocation(externalData.getLocation());
            data.setProcedure(externalData.getProcedure());
            data.setParents(externalData.getParents());
            data.setProductionDate(externalData.getProductionDate());
            data.setRegistrationDate(new Date());
            data.setSampleAcquiredFrom(externalData.getSampleAcquiredFrom());
            data.setSampleDerivedFrom(externalData.getSampleDerivedFrom());
            data.setStorageFormatVocabularyTerm(externalData.getStorageFormatVocabularyTerm());
            externalDataDAO.updateDataSet(externalData);
        }
    }

}
