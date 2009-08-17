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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business;

import java.util.ArrayList;
import java.util.List;

import net.lemnik.eodsql.DataSet;

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.translator.EntityPropertyTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IProteinQueryDAO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.SampleWithPropertiesAndAbundance;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.SampleAbundance;

/**
 * @author Franz-Josef Elmer
 */
class SampleTable extends AbstractBusinessObject implements ISampleTable
{
    private List<SampleWithPropertiesAndAbundance> samples =
            new ArrayList<SampleWithPropertiesAndAbundance>();

    SampleTable(IDAOFactory daoFactory, IPhosphoNetXDAOFactory specificDAOFactory, Session session)
    {
        super(daoFactory, specificDAOFactory, session);
    }

    public List<SampleWithPropertiesAndAbundance> getSamples()
    {
        return samples;
    }

    public void loadSamplesWithAbundance(TechId proteinID)
    {
        samples = new ArrayList<SampleWithPropertiesAndAbundance>();
        IProteinQueryDAO proteinQueryDAO = getSpecificDAOFactory().getProteinQueryDAO();
        DataSet<SampleAbundance> sampleAbundances =
                proteinQueryDAO.listSampleAbundanceByProtein(proteinID.getId());
        ISampleDAO sampleDAO = getDaoFactory().getSampleDAO();
        for (SampleAbundance sampleAbundance : sampleAbundances)
        {
            SampleWithPropertiesAndAbundance sample = new SampleWithPropertiesAndAbundance();
            sample.setAbundance(sampleAbundance.getAbundance());
            String samplePermID = sampleAbundance.getSamplePermID();
            SamplePE samplePE = sampleDAO.tryToFindByPermID(samplePermID);
            if (samplePE == null)
            {
                throw new IllegalStateException("No sample with following permanent ID found: "
                        + samplePermID);
            }
            fillSampleData(sample, samplePE);
            samples.add(sample);
        }
        sampleAbundances.close();

    }

    private final static void fillSampleData(final SampleWithPropertiesAndAbundance result,
            final SamplePE samplePE)
    {
        result.setId(HibernateUtils.getId(samplePE));
        result.setCode(StringEscapeUtils.escapeHtml(samplePE.getCode()));
        result.setIdentifier(StringEscapeUtils.escapeHtml(samplePE.getIdentifier()));
        result.setSampleType(SampleTypeTranslator.translate(samplePE.getSampleType()));
        setProperties(result, samplePE);
    }

    private static void setProperties(final SampleWithPropertiesAndAbundance result,
            final SamplePE samplePE)
    {
        if (samplePE.isPropertiesInitialized())
        {
            result.setProperties(EntityPropertyTranslator.translate(samplePE.getProperties()));
        } else
        {
            result.setProperties(new ArrayList<IEntityProperty>());
        }
    }

}
