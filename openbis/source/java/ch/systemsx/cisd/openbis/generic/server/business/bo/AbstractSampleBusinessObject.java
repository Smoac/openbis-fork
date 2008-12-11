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
 * An <i>abstract</i> {@link AbstractSampleIdentifierBusinessObject} extension for <i>Business
 * Object</i> which has to do with {@link SamplePE}.
 * 
 * @author Christian Ribeaud
 */
abstract class AbstractSampleBusinessObject extends AbstractSampleIdentifierBusinessObject
{
    private final IEntityPropertiesConverter entityPropertiesConverter;

    AbstractSampleBusinessObject(final IDAOFactory daoFactory, final Session session)
    {
        this(daoFactory, session, new EntityPropertiesConverter(EntityKind.SAMPLE, daoFactory));
    }

    AbstractSampleBusinessObject(final IDAOFactory daoFactory, final Session session,
            final IEntityPropertiesConverter entityPropertiesConverter)
    {
        super(daoFactory, session);
        this.entityPropertiesConverter = entityPropertiesConverter;
    }

    private final void defineSampleProperties(final SamplePE sample,
            final SampleProperty[] sampleProperties)
    {
        final String sampleTypeCode = sample.getSampleType().getCode();
        final List<SamplePropertyPE> properties =
                entityPropertiesConverter.convertProperties(sampleProperties, sampleTypeCode,
                        sample.getRegistrator());
        for (final SamplePropertyPE sampleProperty : properties)
        {
            sample.addProperty(sampleProperty);
        }
    }

    /**
     * Creates an new {@link SamplePE} object out of given <var>newSample</var>.
     * <p>
     * Does not trigger any insert in the database.
     * </p>
     */
    final SamplePE createSample(final NewSample newSample) throws UserFailureException
    {
        final SampleIdentifier sampleIdentifier =
                SampleIdentifierFactory.parse(newSample.getIdentifier());
        final SampleOwner sampleOwner = getSampleOwnerFinder().figureSampleOwner(sampleIdentifier);
        final SamplePE samplePE = new SamplePE();
        samplePE.setCode(sampleIdentifier.getSampleCode());
        samplePE.setRegistrator(findRegistrator());
        samplePE.setSampleType(getSampleType(newSample.getSampleType().getCode()));
        samplePE.setGroup(sampleOwner.tryGetGroup());
        samplePE.setDatabaseInstance(sampleOwner.tryGetDatabaseInstance());
        defineSampleProperties(samplePE, newSample.getProperties());
        final String parent = newSample.getParentIdentifier();
        if (parent != null)
        {
            final SamplePE parentPE = getSampleByIdentifier(SampleIdentifierFactory.parse(parent));
            if (parentPE.getInvalidation() != null)
            {
                throw UserFailureException.fromTemplate(
                        "Cannot register sample '%s': parent '%s' has been invalidated.",
                        sampleIdentifier, parent);
            }
            samplePE.setGeneratedFrom(parentPE);
            samplePE.setTop(parentPE.getTop() == null ? parentPE : parentPE.getTop());
        }
        final String container = newSample.getContainerIdentifier();
        if (container != null)
        {
            final SamplePE containerPE =
                    getSampleByIdentifier(SampleIdentifierFactory.parse(container));
            if (containerPE.getInvalidation() != null)
            {
                throw UserFailureException.fromTemplate(
                        "Cannot register sample '%s': container '%s' has been invalidated.",
                        sampleIdentifier, container);
            }
            samplePE.setContainer(containerPE);
            samplePE.setTop(containerPE.getTop() == null ? containerPE : containerPE.getTop());
        }
        SampleGenericBusinessRules.assertValidParents(samplePE);
        return samplePE;
    }

    final SampleTypePE getSampleType(final String code) throws UserFailureException
    {
        final SampleTypePE sampleType = getSampleTypeDAO().tryFindSampleTypeByCode(code);
        if (sampleType == null)
        {
            throw UserFailureException.fromTemplate(
                    "No sample type with code '%s' could be found in the database.", code);
        }
        return sampleType;
    }

}
