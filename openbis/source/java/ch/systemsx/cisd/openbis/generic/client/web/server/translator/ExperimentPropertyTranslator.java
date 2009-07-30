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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPropertyPE;

/**
 * A {@link IEntityProperty} &lt;---&gt; {@link ExperimentPropertyPE} translator.
 * 
 * @author Izabela Adamczyk
 */
public final class ExperimentPropertyTranslator
{
    private ExperimentPropertyTranslator()
    {
        // Can not be instantiated.
    }

    public final static IEntityProperty translate(final ExperimentPropertyPE experimentPropertyPE)
    {
        final DataTypeCode typeCode = PropertyTranslatorUtils.getDataTypeCode(experimentPropertyPE);
        final IEntityProperty result = PropertyTranslatorUtils.createEntityProperty(typeCode);
        result.setPropertyType(PropertyTypeTranslator.translate(experimentPropertyPE
                .getEntityTypePropertyType().getPropertyType()));
        switch (typeCode)
        {
            case CONTROLLEDVOCABULARY:
                result.setVocabularyTerm(VocabularyTermTranslator.translate(experimentPropertyPE
                        .getVocabularyTerm()));
                break;
            case MATERIAL:
                result.setMaterial(MaterialTranslator.translate(
                        experimentPropertyPE.getMaterialValue(), false));
                break;
            default:
                result
                        .setValue(StringEscapeUtils.escapeHtml(experimentPropertyPE
                                .tryGetUntypedValue()));
        }
        return result;
    }

    public final static List<IEntityProperty> translate(final Set<ExperimentPropertyPE> list)
    {
        if (list == null)
        {
            return null;
        }
        final List<IEntityProperty> result = new ArrayList<IEntityProperty>();
        for (final ExperimentPropertyPE experimentProperty : list)
        {
            result.add(translate(experimentProperty));
        }
        return result;
    }
}
