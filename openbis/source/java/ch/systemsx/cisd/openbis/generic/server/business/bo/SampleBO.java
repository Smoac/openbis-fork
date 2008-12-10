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

import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleOwner;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

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

    public SampleBO(final IDAOFactory daoFactory, final Session session)
    {
        this(daoFactory, session, new EntityPropertiesConverter(EntityKind.SAMPLE, daoFactory));
    }

    @Private
    SampleBO(final IDAOFactory daoFactory, final Session session,
            final IEntityPropertiesConverter entityPropertiesConverter)
    {
        super(daoFactory, session);
        propertiesConverter = entityPropertiesConverter;
        this.dataChanged = false;
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

    private final void defineSampleProperties(final SampleProperty[] sampleProperties)
    {
        final String sampleTypeCode = sample.getSampleType().getCode();
        final List<SamplePropertyPE> properties =
                propertiesConverter.convertProperties(sampleProperties, sampleTypeCode, sample
                        .getRegistrator());
        for (final SamplePropertyPE sampleProperty : properties)
        {
            sample.addProperty(sampleProperty);
        }
    }

    //
    // ISampleBO
    //

    public final SamplePE getSample()
    {
        if (sample == null)
        {
            throw new IllegalStateException("Unloaded sample.");
        }
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

    public final void define(final NewSample newSample)
    {
        final SampleIdentifier sampleIdentifier =
                SampleIdentifierFactory.parse(newSample.getIdentifier());
        final SampleOwner sampleOwner = getSampleOwnerFinder().figureSampleOwner(sampleIdentifier);
        sample = new SamplePE();
        sample.setCode(sampleIdentifier.getSampleCode());
        sample.setRegistrator(findRegistrator());
        sample.setSampleType(getSampleType(newSample.getSampleType().getCode()));
        sample.setGroup(sampleOwner.tryGetGroup());
        sample.setDatabaseInstance(sampleOwner.tryGetDatabaseInstance());
        defineSampleProperties(newSample.getProperties());
        final String parent = newSample.getParentIdentifier();
        if (parent != null)
        {
            final SamplePE parentPE = getSampleByIdentifier(SampleIdentifierFactory.parse(parent));
            if (parentPE.getInvalidation() != null)
            {
                throw UserFailureException.fromTemplate(
                        "Cannot register sample '%s': parent '%s' is invalid.", sampleIdentifier,
                        parent);
            }
            sample.setGeneratedFrom(parentPE);
            sample.setTop(parentPE.getTop() == null ? parentPE : parentPE.getTop());
        }
        final String container = newSample.getContainerIdentifier();
        if (container != null)
        {
            final SamplePE containerPE =
                    getSampleByIdentifier(SampleIdentifierFactory.parse(container));
            if (containerPE.getInvalidation() != null)
            {
                throw UserFailureException.fromTemplate(
                        "Cannot register sample '%s': container '%s' is invalid.",
                        sampleIdentifier, container);
            }
            sample.setContainer(containerPE);
            sample.setTop(containerPE.getTop() == null ? containerPE : containerPE.getTop());
        }
        SampleGenericBusinessRules.assertValidParents(sample);
        dataChanged = true;
    }

    public final void save()
    {
        assert sample != null : "Sample not loaded.";
        assert dataChanged : "Data have not been changed.";

        try
        {
            getSampleDAO().createSample(sample);
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("Sample '%s'", sample.getSampleIdentifier()));
        }
        dataChanged = false;
    }

}
