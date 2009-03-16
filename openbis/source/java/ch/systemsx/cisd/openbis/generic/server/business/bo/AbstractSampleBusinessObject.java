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
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleOwner;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedurePE;
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
    protected final IEntityPropertiesConverter entityPropertiesConverter;

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
        final SamplePE parentPE =
                tryGetValidSample(newSample.getParentIdentifier(), sampleIdentifier);
        if (parentPE != null)
        {
            samplePE.setGeneratedFrom(parentPE);
            samplePE.setTop(parentPE.getTop() == null ? parentPE : parentPE.getTop());
        }
        final SamplePE containerPE =
                tryGetValidSample(newSample.getContainerIdentifier(), sampleIdentifier);
        if (containerPE != null)
        {
            samplePE.setContainer(containerPE);
        }
        SampleGenericBusinessRules.assertValidParents(samplePE);
        return samplePE;
    }

    private SamplePE tryGetValidSample(final String parentIdentifierOrNull,
            final SampleIdentifier sampleIdentifier)
    {
        if (parentIdentifierOrNull == null)
        {
            return null;
        }
        final SamplePE parentPE =
                getSampleByIdentifier(SampleIdentifierFactory.parse(parentIdentifierOrNull));
        if (parentPE.getInvalidation() != null)
        {
            throw UserFailureException.fromTemplate(
                    "Cannot register sample '%s': parent '%s' has been invalidated.",
                    sampleIdentifier, parentIdentifierOrNull);
        }
        if (parentPE.getContainer() != null)
        {
            throw UserFailureException.fromTemplate(
                    "Cannot register sample '%s': parent '%s' is part of another sample.",
                    sampleIdentifier, parentIdentifierOrNull);
        }
        return parentPE;
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

    /**
     * Enriches given <code>sample</code> with at most one procedure that contains a
     * non-invalidated experiment.
     * <p>
     * So if <code>sample</code> belongs only to invalidated experiments or does not belong to any
     * experiment at all, no procedure are joined.
     * </p>
     */
    final static void enrichWithProcedure(final SamplePE sampleOrNull)
    {
        if (sampleOrNull != null)
        {
            sampleOrNull.setValidProcedure(tryGetValidProcedure(sampleOrNull.getProcedures()));
        }
    }

    /**
     * Throws exception if there are more than 1 valid procedures or return <code>null</code> if
     * no valid procedure could be found.
     */
    private final static ProcedurePE tryGetValidProcedure(final List<ProcedurePE> procedures)
    {
        ProcedurePE foundProcedure = null;
        for (final ProcedurePE procedure : procedures)
        {
            final ExperimentPE experiment = procedure.getExperiment();
            // Invalid experiment can not be considered.
            if (experiment.getInvalidation() == null)
            {
                if (foundProcedure != null)
                {
                    throw UserFailureException.fromTemplate(
                            "Expected exactly one valid procedure, but found %d: %s", procedures
                                    .size(), procedures);
                }
                foundProcedure = procedure;
            }
        }
        return foundProcedure;
    }

}
