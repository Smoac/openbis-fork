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

package ch.systemsx.cisd.openbis.plugin.proteomics.server.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.lemnik.eodsql.DataSet;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.EntityPropertyTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.dataaccess.IProteinQueryDAO;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.SampleWithPropertiesAndAbundance;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.dto.SampleAbundance;

/**
 * @author Franz-Josef Elmer
 */
class SampleTable extends AbstractBusinessObject implements ISampleTable
{
    private List<SampleWithPropertiesAndAbundance> samples =
            new ArrayList<SampleWithPropertiesAndAbundance>();

    private SampleIDProvider sampleIDProvider;

    private IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    SampleTable(IDAOFactory daoFactory, IPhosphoNetXDAOFactory specificDAOFactory, Session session,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        super(daoFactory, specificDAOFactory, session);
        sampleIDProvider = new SampleIDProvider(daoFactory.getSampleDAO());
        this.managedPropertyEvaluatorFactory = managedPropertyEvaluatorFactory;
    }

    @Override
    public List<SampleWithPropertiesAndAbundance> getSamples()
    {
        return samples;
    }

    @Override
    public void loadSamplesWithAbundance(TechId experimentID, TechId proteinReferenceID)
    {
        samples = new ArrayList<SampleWithPropertiesAndAbundance>();
        IProteinQueryDAO proteinQueryDAO = getSpecificDAOFactory().getProteinQueryDAO(experimentID);
        IDAOFactory daoFactory = getDaoFactory();
        String experimentPermID =
                daoFactory.getExperimentDAO().getByTechId(experimentID).getPermId();
        DataSet<SampleAbundance> sampleAbundances =
                proteinQueryDAO.listSampleAbundanceByProtein(experimentPermID,
                        proteinReferenceID.getId());
        try
        {
            ISampleDAO sampleDAO = daoFactory.getSampleDAO();
            for (SampleAbundance sampleAbundance : sampleAbundances)
            {
                SampleWithPropertiesAndAbundance sample = new SampleWithPropertiesAndAbundance();
                sample.setAbundance(sampleAbundance.getAbundance());
                String samplePermID = sampleAbundance.getSamplePermID();
                long sampleID = sampleIDProvider.getSampleIDOrParentSampleID(samplePermID);
                SamplePE samplePE = sampleDAO.getByTechId(new TechId(sampleID));
                fillSampleData(sample, samplePE, managedPropertyEvaluatorFactory);
                samples.add(sample);
            }
        } finally
        {
            sampleAbundances.close();
        }
    }

    private final static void fillSampleData(final SampleWithPropertiesAndAbundance result,
            final SamplePE samplePE,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        result.setId(HibernateUtils.getId(samplePE));
        result.setPermId(samplePE.getPermId());
        result.setCode(samplePE.getCode());
        result.setIdentifier(samplePE.getIdentifier());
        result.setSampleType(SampleTypeTranslator.translate(samplePE.getSampleType(),
                new HashMap<MaterialTypePE, MaterialType>(), new HashMap<PropertyTypePE, PropertyType>()));
        result.setProperties(EntityPropertyTranslator.translate(samplePE.getProperties(),
                new HashMap<MaterialTypePE, MaterialType>(), new HashMap<PropertyTypePE, PropertyType>(), 
                managedPropertyEvaluatorFactory));
    }
}
