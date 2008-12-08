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
import java.util.Set;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * The unique {@link IExperimentBO} implementation.
 * 
 * @author Izabela Adamczyk
 */
public final class ExperimentBO extends AbstractBusinessObject implements IExperimentBO
{
    private final IEntityPropertiesConverter propertiesConverter;

    private ExperimentPE experiment;

    public ExperimentBO(final IDAOFactory daoFactory, final Session session)
    {
        this(daoFactory, session, new EntityPropertiesConverter(EntityKind.EXPERIMENT, daoFactory));
    }

    @Private
    ExperimentBO(final IDAOFactory daoFactory, final Session session,
            final IEntityPropertiesConverter entityPropertiesConverter)
    {
        super(daoFactory, session);
        propertiesConverter = entityPropertiesConverter;
    }

    @SuppressWarnings("unused")
    private final ExperimentTypePE getExperimentType(final String code) throws UserFailureException
    {
        final EntityTypePE experimentType =
                getEntityTypeDAO(EntityKind.EXPERIMENT).tryToFindEntityTypeByCode(code);
        if (experimentType == null)
        {
            throw UserFailureException.fromTemplate(
                    "No experiment type with code '%s' could be found in the database.", code);
        }
        return (ExperimentTypePE) experimentType;
    }

    @SuppressWarnings("unused")
    private final void defineSampleProperties(final SampleProperty[] experimentProperties)
    {
        final String experimentTypeCode = experiment.getExperimentType().getCode();
        final List<ExperimentPropertyPE> properties =
                propertiesConverter.convertProperties(experimentProperties, experimentTypeCode,
                        experiment.getRegistrator());
        for (final ExperimentPropertyPE property : properties)
        {
            experiment.addProperty(property);
        }
    }

    public final ExperimentPE getExperiment()
    {
        checkExperimentLoaded();
        return experiment;
    }

    private void checkExperimentLoaded()
    {
        if (experiment == null)
        {
            throw new IllegalStateException("Unloaded experiment.");
        }
    }

    public final void loadByExperimentIdentifier(final ExperimentIdentifier identifier)
    {
        experiment = getExperimentByIdentifier(identifier);
        if (experiment == null)
        {
            throw UserFailureException.fromTemplate(
                    "No sample could be found with given identifier '%s'.", identifier);
        }
    }

    private ExperimentPE getExperimentByIdentifier(final ExperimentIdentifier identifier)
    {
        assert identifier != null : "Experiment identifier unspecified.";
        final ProjectPE project =
                getProjectDAO().tryFindProject(identifier.getDatabaseInstanceCode(),
                        identifier.getGroupCode(), identifier.getProjectCode());
        final ExperimentPE exp =
                getExperimentDAO().tryFindByCodeAndProject(project, identifier.getExperimentCode());
        if (exp == null)
        {
            throw UserFailureException.fromTemplate(
                    "No experiment could be found for identifier '%s'.", identifier);
        }
        return exp;
    }

    public final void enrichWithProperties()
    {
        if (experiment != null)
        {
            HibernateUtils.initialize(experiment.getProperties());
        }
    }

    public final void enrichWithAttachments()
    {
        if (experiment != null)
        {
            experiment.ensureAttachmentsLoaded();
        }
    }

    public AttachmentPE getExperimentFileAttachment(final String filename, final int version)
    {
        checkExperimentLoaded();
        experiment.ensureAttachmentsLoaded();
        final Set<AttachmentPE> attachments = experiment.getAttachments();
        for (AttachmentPE att : attachments)
        {
            if (att.getFileName().equals(filename) && att.getVersion() == version)
            {
                HibernateUtils.initialize(att.getAttachmentContent());
                return att;
            }
        }
        throw new UserFailureException("Attachment '" + filename + "' (version '" + version
                + "') not found in experiment '" + experiment.getIdentifier() + "'.");
    }

}
