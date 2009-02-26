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
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
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
                HibernateUtils.initialize(dataSet.getParents());
                if (dataSet instanceof ExternalDataPE)
                {
                    ExternalDataPE externalDataPE = (ExternalDataPE) dataSet;
                    enrichWithParentsAndProcedure(externalDataPE);
                    externalData.add(externalDataPE);
                }
            }
        }
    }
}
