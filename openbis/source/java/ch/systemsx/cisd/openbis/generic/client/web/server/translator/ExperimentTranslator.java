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

package ch.systemsx.cisd.openbis.generic.client.web.server.translator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;

/**
 * A {@link Experiment} &lt;---&gt; {@link ExperimentPE} translator.
 * 
 * @author Tomasz Pylak
 */
public final class ExperimentTranslator
{

    public enum LoadableFields
    {
        ATTACHMENTS, PROPERTIES
    }

    private ExperimentTranslator()
    {
        // Can not be instantiated.
    }

    public final static Experiment translate(final ExperimentPE experiment,
            final LoadableFields... withFields)
    {
        if (experiment == null)
        {
            return null;
        }
        final Experiment result = new Experiment();
        result.setId(experiment.getId());
        result.setModificationDate(experiment.getModificationDate());
        result.setCode(experiment.getCode());
        result.setExperimentType(translate(experiment.getExperimentType()));
        result.setIdentifier(experiment.getIdentifier());
        result.setProject(ProjectTranslator.translate(experiment.getProject()));
        result.setRegistrationDate(experiment.getRegistrationDate());
        result.setRegistrator(PersonTranslator.translate(experiment.getRegistrator()));
        result.setInvalidation(InvalidationTranslator.translate(experiment.getInvalidation()));
        for (final LoadableFields field : withFields)
        {
            switch (field)
            {
                case PROPERTIES:
                    result.setProperties(ExperimentPropertyTranslator.translate(experiment
                            .getProperties()));
                    break;
                case ATTACHMENTS:
                    result.setAttachments(translate(experiment.getAttachments()));
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    public final static ExperimentType translate(final ExperimentTypePE experimentType)
    {
        final ExperimentType result = new ExperimentType();
        result.setCode(experimentType.getCode());
        result.setDescription(StringEscapeUtils.escapeHtml(experimentType.getDescription()));
        result.setDatabaseInstance(DatabaseInstanceTranslator.translate(experimentType
                .getDatabaseInstance()));
        result.setExperimentTypePropertyTypes(ExperimentTypePropertyTypeTranslator.translate(
                experimentType.getExperimentTypePropertyTypes(), result));
        return result;
    }

    public final static Attachment translate(final AttachmentPE attachment)
    {
        final Attachment result = new Attachment();
        result.setRegistrator(PersonTranslator.translate(attachment.getRegistrator()));
        result.setFileName(StringEscapeUtils.escapeHtml(attachment.getFileName()));
        result.setVersion(attachment.getVersion());
        result.setRegistrationDate(attachment.getRegistrationDate());
        return result;
    }

    public final static List<Attachment> translate(final Set<AttachmentPE> set)
    {
        if (set == null)
        {
            return null;
        }
        final List<Attachment> result = new ArrayList<Attachment>();
        for (final AttachmentPE attachment : set)
        {
            result.add(translate(attachment));
        }
        return result;
    }

    public final static ExperimentTypePE translate(final ExperimentType experimentType)
    {
        final ExperimentTypePE result = new ExperimentTypePE();
        result.setCode(experimentType.getCode());
        result.setDescription(StringEscapeUtils.escapeHtml(experimentType.getDescription()));
        result.setDatabaseInstance(DatabaseInstanceTranslator.translate(experimentType
                .getDatabaseInstance()));
        return result;
    }

}
