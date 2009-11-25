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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model.renderer;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.EntityPropertyColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ExternalHyperlink;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.MultilineHTML;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

/**
 * An {@link AbstractPropertyColRenderer} which renders vocabulary term with link.
 * 
 * @author Izabela Adamczyk
 */
public class VocabularyPropertyColRenderer<T extends IEntityPropertiesHolder> extends
        AbstractPropertyColRenderer<T>
{
    public VocabularyPropertyColRenderer(EntityPropertyColDef<T> colDef)
    {
        super(colDef);
    }

    @Override
    protected String renderValue(GridRowModel<T> entity)
    {
        final IEntityProperty property = colDef.tryGetProperty(entity.getOriginalObject());
        String result = "";
        if (property != null)
        {
            final VocabularyTerm term = property.getVocabularyTerm();
            if (term != null)
            {
                result = renderTerm(term);
            }
        }
        return result;
    }

    public static final String renderTerm(VocabularyTerm term)
    {
        assert term != null : "term is not set";

        final String description = term.getDescription();
        final String url = term.getUrl();

        String result = renderCodeWithLabel(term);
        if (url != null)
        {
            result = ExternalHyperlink.createAnchorString(result, url);
        }
        result = MultilineHTML.wrapUpInDivWithTooltip(result, description);

        return result;
    }

    public static final String renderCodeWithLabel(VocabularyTerm term)
    {
        return term.toString();
    }

    public static final String renderAsTooltip(VocabularyTerm term)
    {
        final String code = term.getCode();
        final String label = term.getLabel();
        final String description = term.getDescription();
        String result = "";
        if (label == null)
        {
            result += "<b>" + code + "</b>";
        } else
        {
            result += "<b>" + label + "</b>";
            result += "<br>code: " + code;
        }
        if (description != null)
        {
            result += "<br><hr>description: <i>" + description + "</i>";
        }
        return result.replace(".", ". ");
    }
}
