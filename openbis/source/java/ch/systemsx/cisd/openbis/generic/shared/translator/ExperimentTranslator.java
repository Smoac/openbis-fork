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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

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

    private static void setProperties(final ExperimentPE experiment, final Experiment result)
    {
        if (experiment.isPropertiesInitialized())
        {
            result.setProperties(EntityPropertyTranslator.translate(experiment.getProperties(),
                    new HashMap<PropertyTypePE, PropertyType>()));
        } else
        {
            result.setProperties(new ArrayList<IEntityProperty>());
        }
    }

    public final static Experiment translate(final ExperimentPE experiment, String baseIndexURL,
            final LoadableFields... withFields)
    {
        if (experiment == null)
        {
            return null;
        }
        final Experiment result = new Experiment();
        result.setId(HibernateUtils.getId(experiment));
        result.setModificationDate(experiment.getModificationDate());
        result.setCode(experiment.getCode());
        result.setPermId(experiment.getPermId());
        result.setPermlink(PermlinkUtilities.createPermlinkURL(baseIndexURL, EntityKind.EXPERIMENT,
                experiment.getPermId()));
        result.setExperimentType(translate(experiment.getExperimentType(),
                new HashMap<PropertyTypePE, PropertyType>()));
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
                    setProperties(experiment, result);
                    break;
                case ATTACHMENTS:
                    result.setAttachments(AttachmentTranslator.translate(
                            experiment.getAttachments(), baseIndexURL));
                    break;
                default:
                    break;
            }
        }

        return result;
    }

    public final static List<Experiment> translate(final List<ExperimentPE> experiments,
            String baseIndexURL, final LoadableFields... withFields)
    {
        final List<Experiment> result = new ArrayList<Experiment>(experiments.size());
        for (final ExperimentPE experiment : experiments)
        {
            result.add(ExperimentTranslator.translate(experiment, baseIndexURL, withFields));
        }
        return result;
    }

    public final static ExperimentType translate(final ExperimentTypePE experimentType,
            Map<PropertyTypePE, PropertyType> cacheOrNull)
    {
        final ExperimentType result = new ExperimentType();
        result.setCode(experimentType.getCode());
        result.setDescription(experimentType.getDescription());
        result.setDatabaseInstance(DatabaseInstanceTranslator.translate(experimentType
                .getDatabaseInstance()));
        result.setExperimentTypePropertyTypes(ExperimentTypePropertyTypeTranslator.translate(
                experimentType.getExperimentTypePropertyTypes(), result, cacheOrNull));

        return result;
    }

    public final static ExperimentTypePE translate(final ExperimentType experimentType)
    {
        final ExperimentTypePE result = new ExperimentTypePE();
        result.setCode(experimentType.getCode());
        result.setDescription(experimentType.getDescription());
        result.setDatabaseInstance(DatabaseInstanceTranslator.translate(experimentType
                .getDatabaseInstance()));

        return result;
    }

    public final static List<ExperimentType> translate(final List<ExperimentTypePE> experimentTypes)
    {
        final List<ExperimentType> result = new ArrayList<ExperimentType>(experimentTypes.size());
        for (final ExperimentTypePE experimentType : experimentTypes)
        {
            result.add(translate(experimentType, new HashMap<PropertyTypePE, PropertyType>()));
        }
        return result;
    }

}
