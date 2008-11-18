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

import java.util.HashSet;
import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.ParameterChecker;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleOwner;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleToRegisterDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * The unique {@link ISampleBO} implementation.
 * 
 * @author Christian Ribeaud
 */
public final class SampleBO extends AbstractSampleIdentifierBusinessObject implements ISampleBO
{
    private final IEntityPropertiesConverter propertiesConverter;

    private SamplePE sample;

    private boolean dataChanged;

    public SampleBO(final IDAOFactory daoFactory,
            final IEntityPropertiesConverter entityPropertiesConverter, final Session session)
    {
        super(daoFactory, session);
        propertiesConverter = entityPropertiesConverter;
        this.dataChanged = false;
    }

    //
    // ISampleBO
    //

    public final SamplePE getSample()
    {
        assert sample != null : "Unloaded sample.";
        return sample;
    }

    public final void loadBySampleIdentifier(final SampleIdentifier identifier)
    {
        sample = getSampleByIdentifier(identifier);
        if (sample == null)
        {
            throw UserFailureException.fromTemplate(
                    "No sample could be found with given identifier '%s'.", identifier);
        }
    }

    public final void define(final SampleToRegisterDTO newSample)
    {
        final SampleIdentifier sampleIdentifier = newSample.getSampleIdentifier();
        final String sampleTypeCode = newSample.getSampleTypeCode();
        ParameterChecker.checkIfNotNull(sampleTypeCode, "sample type");

        final SampleOwner sampleOwner = getSampleOwnerFinder().figureSampleOwner(sampleIdentifier);

        sample = new SamplePE();
        sample.setCode(sampleIdentifier.getSampleCode());
        sample.setRegistrator(findRegistrator());
        sample.setSampleType(getSampleType(sampleTypeCode));
        sample.setGroup(sampleOwner.tryGetGroup());
        sample.setDatabaseInstance(sampleOwner.tryGetDatabaseInstance());
        defineSampleProperties(newSample.getProperties());
        final SampleIdentifier generatorParentSampleIdentifier = newSample.getGeneratorParent();
        if (generatorParentSampleIdentifier != null)
        {
            final SamplePE generatedFrom = getSampleByIdentifier(generatorParentSampleIdentifier);
            if (generatedFrom != null)
            {
                if (generatedFrom.getInvalidation() != null)
                {
                    throw UserFailureException.fromTemplate(
                            "Cannot register sample '%s': generator parent '%s' is invalid.",
                            sampleIdentifier, generatorParentSampleIdentifier);
                }
                sample.setGeneratedFrom(generatedFrom);
                sample.setTop(generatedFrom.getTop() == null ? generatedFrom : generatedFrom
                        .getTop());
            }
        }
        final SampleIdentifier containerParentSampleIdentifier = newSample.getContainerParent();
        if (containerParentSampleIdentifier != null)
        {
            final SamplePE contained = getSampleByIdentifier(containerParentSampleIdentifier);
            if (contained != null)
            {
                if (contained.getInvalidation() != null)
                {
                    throw UserFailureException.fromTemplate(
                            "Cannot register sample '%s': container parent '%s' is invalid.",
                            sampleIdentifier, containerParentSampleIdentifier);
                }
                sample.setContainer(contained);
                sample.setTop(contained.getTop() == null ? contained : contained.getTop());
            }
        }
        dataChanged = true;
    }

    // other fields of sample should be already defined
    private void defineSampleProperties(final SimpleEntityProperty[] simpleProperties)
    {
        final String sampleTypeCode = sample.getSampleType().getCode();

        final List<SamplePropertyPE> properties =
                propertiesConverter.convertProperties(simpleProperties, sampleTypeCode, sample
                        .getRegistrator());
        final HashSet<SamplePropertyPE> set = new HashSet<SamplePropertyPE>();
        for (final SamplePropertyPE ep : properties)
        {
            ep.setHolder(sample);
            set.add(ep);
        }
        sample.setProperties(set);
    }

    private final SampleTypePE getSampleType(final String code) throws UserFailureException
    {
        final SampleTypePE sampleType = getSampleTypeDAO().tryFindSampleTypeByCode(code);
        if (sampleType == null)
        {
            throw UserFailureException.fromTemplate(
                    "No sample type with code '%s' could be found in the database.", code);
        }
        return sampleType;
    }

    public final void save()
    {
        assert sample != null : "Sample not loaded.";
        assert dataChanged : "Data have not been changed.";

        try
        {
            final ISampleDAO sampleDAO = getSampleDAO();
            sampleDAO.createSample(sample);
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("Sample '%s'", sample.getSampleIdentifier()));
        }
        dataChanged = false;
    }

}
