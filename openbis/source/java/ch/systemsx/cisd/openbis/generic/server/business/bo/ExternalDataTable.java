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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.DataStoreServerSessionManager;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedurePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SourceType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * The only productive implementation of {@link IExternalDataTable}.
 * <p>
 * We are using an interface here to keep the system testable.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class ExternalDataTable extends AbstractExternalDataBusinessObject implements
        IExternalDataTable
{
    @Private static final int MAX_LENGTH_OF_CIFEX_COMMENT = 1000;
    @Private static final String UPLOAD_COMMENT_TEXT = "Uploaded zip file contains the following data sets:";
    @Private static final String NEW_LINE = "\n";
    @Private static final String AND_MORE_TEMPLATE = "and %d more.";
    @Private static final String DELETION_DESCRIPTION = "single deletion";
    
    @Private static String createUploadComment(List<ExternalDataPE> dataSets)
    {
        StringBuilder builder = new StringBuilder(UPLOAD_COMMENT_TEXT);
        for (int i = 0, n = dataSets.size(); i < n; i++)
        {
            builder.append(NEW_LINE);
            String code = dataSets.get(i).getCode();
            int length = builder.length() + code.length();
            if (i < n - 1)
            {
                length += NEW_LINE.length() + String.format(AND_MORE_TEMPLATE, n - i - 1).length();
            }
            if (length < MAX_LENGTH_OF_CIFEX_COMMENT)
            {
                builder.append(code);
            } else
            {
                builder.append(String.format(AND_MORE_TEMPLATE, n - i));
                break;
            }
        }
        return builder.toString();
    }
    

    private List<ExternalDataPE> externalData;

    public ExternalDataTable(final IDAOFactory daoFactory, final Session session)
    {
        super(daoFactory, session);
    }

    //
    // IExternalDataTable
    //

    public final List<ExternalDataPE> getExternalData()
    {
        assert externalData != null : "External data not loaded.";
        return externalData;
    }

    public void setExternalData(List<ExternalDataPE> externalData)
    {
        this.externalData = externalData;
    }

    public void loadByDataSetCodes(List<String> dataSetCodes)
    {
        IExternalDataDAO externalDataDAO = getExternalDataDAO();
        externalData = new ArrayList<ExternalDataPE>();
        for (String dataSetCode : dataSetCodes)
        {
            ExternalDataPE dataSet = externalDataDAO.tryToFindFullDataSetByCode(dataSetCode);
            if (dataSet != null)
            {
                externalData.add(dataSet);
            }
        }
    }

    public final void loadBySampleIdentifier(final SampleIdentifier sampleIdentifier)
    {
        assert sampleIdentifier != null : "Unspecified sample identifier";
        externalData = new ArrayList<ExternalDataPE>();
        final SamplePE sample = getSampleByIdentifier(sampleIdentifier);
        externalData.addAll(getExternalDataDAO().listExternalData(sample, SourceType.MEASUREMENT));
        externalData.addAll(getExternalDataDAO().listExternalData(sample, SourceType.DERIVED));
        for (ExternalDataPE externalDataPE : externalData)
        {
            enrichWithParentsAndProcedure(externalDataPE);
        }
    }

    public void loadByExperimentIdentifier(ExperimentIdentifier identifier)
    {
        assert identifier != null : "Unspecified experiment identifier";

        ProjectPE project =
                getProjectDAO().tryFindProject(identifier.getDatabaseInstanceCode(),
                        identifier.getGroupCode(), identifier.getProjectCode());
        ExperimentPE experiment =
                getExperimentDAO().tryFindByCodeAndProject(project, identifier.getExperimentCode());
        externalData = new ArrayList<ExternalDataPE>();
        List<ProcedurePE> procedures = experiment.getProcedures();
        for (ProcedurePE procedure : procedures)
        {

            Set<DataPE> data = procedure.getData();
            for (DataPE dataSet : data)
            {
                if (dataSet.isDeleted() == false && dataSet instanceof ExternalDataPE)
                {
                    ExternalDataPE externalDataPE = (ExternalDataPE) dataSet;
                    HibernateUtils.initialize(dataSet.getParents());
                    enrichWithParentsAndProcedure(externalDataPE);
                    externalData.add(externalDataPE);
                }
            }
        }
    }

    public void deleteLoadedDataSets(DataStoreServerSessionManager dssSessionManager, String reason)
    {
        assertDataSetsAreKnown(dssSessionManager);
        for (ExternalDataPE dataSet : externalData)
        {
            IExternalDataDAO externalDataDAO = getExternalDataDAO();
            externalDataDAO.markAsDeleted(dataSet, session.tryGetPerson(), DELETION_DESCRIPTION, reason);
        }
        Collection<DataStoreServerSession> sessions = dssSessionManager.getSessions();
        List<String> locations = getLocations();
        for (DataStoreServerSession dssSession : sessions)
        {
            dssSession.getService().deleteDataSets(dssSession.getSessionToken(), locations);
        }
    }

    public void uploadLoadedDataSetsToCIFEX(DataStoreServerSessionManager dssSessionManager,
            DataSetUploadContext uploadContext)
    {
        assertDataSetsAreKnown(dssSessionManager);
        Collection<DataStoreServerSession> sessions = dssSessionManager.getSessions();
        List<String> locations = getLocations();
        uploadContext.setUserEMail(session.getPrincipal().getEmail());
        if (StringUtils.isBlank(uploadContext.getComment()))
        {
            uploadContext.setComment(createUploadComment(externalData));
        }
        for (DataStoreServerSession dssSession : sessions)
        {
            dssSession.getService().uploadDataSetsToCIFEX(dssSession.getSessionToken(), locations,
                    uploadContext);
        }
    }

    private void assertDataSetsAreKnown(DataStoreServerSessionManager dssSessionManager)
    {
        List<String> locations = getLocations();
        Set<String> knownLocations = new LinkedHashSet<String>();
        Collection<DataStoreServerSession> sessions = dssSessionManager.getSessions();
        for (DataStoreServerSession dssSession : sessions)
        {
            IDataStoreService service = dssSession.getService();
            String dssSessionToken = dssSession.getSessionToken();
            knownLocations.addAll(service.getKnownDataSets(dssSessionToken, locations));
        }
        List<String> unknownDataSets = new ArrayList<String>();
        for (ExternalDataPE dataSet : externalData)
        {
            if (knownLocations.contains(dataSet.getLocation()) == false)
            {
                unknownDataSets.add(dataSet.getCode());
            }
        }
        if (unknownDataSets.isEmpty() == false)
        {
            throw new UserFailureException(
                    "The following data sets are unknown by any registered Data Store Server. "
                            + "May be the responsible Data Store Server is not running.\n"
                            + unknownDataSets);
        }
    }
    
    private List<String> getLocations()
    {
        List<String> locations = new ArrayList<String>();
        for (ExternalDataPE dataSet : externalData)
        {
            locations.add(dataSet.getLocation());
        }
        return locations;
    }
}
