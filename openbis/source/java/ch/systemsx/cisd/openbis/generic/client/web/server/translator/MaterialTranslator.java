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

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;

/**
 * A {@link Material} &lt;---&gt; {@link MaterialPE} translator.
 * 
 * @author Izabela Adamczyk
 */
public final class MaterialTranslator
{

    private MaterialTranslator()
    {
        // Can not be instantiated.
    }

    public final static List<Material> translate(final List<MaterialPE> materials)
    {
        final List<Material> result = new ArrayList<Material>();
        for (final MaterialPE material : materials)
        {
            result.add(MaterialTranslator.translate(material, true));
        }
        return result;
    }

    private final static Material translate(final MaterialPE materialPE, final boolean withDetails)
    {
        if (materialPE == null)
        {
            return null;
        }
        final Material result = new Material();
        result.setCode(StringEscapeUtils.escapeHtml(materialPE.getCode()));
        result.setId(materialPE.getId());
        result.setModificationDate(materialPE.getModificationDate());
        if (withDetails)
        {
            result.setMaterialType(MaterialTypeTranslator.translate(materialPE.getMaterialType()));
            result.setDatabaseInstance(DatabaseInstanceTranslator.translate(materialPE
                    .getDatabaseInstance()));
            result.setRegistrator(PersonTranslator.translate(materialPE.getRegistrator()));
            result.setRegistrationDate(materialPE.getRegistrationDate());
            result.setProperties(MaterialPropertyTranslator.translate(materialPE.getProperties()));
            result.setInhibitorOf(MaterialTranslator.translate(materialPE.getInhibitorOf(), false));
        }
        return result;
    }

}
