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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.VocabularyTermModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SimpleDropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

/**
 * @author Izabela Adamczyk
 */
public class VocabularyTermSelectionWidget extends SimpleDropDownList<VocabularyTermModel, String>
{

    private static final String CHOOSE_MSG = "Choose...";

    private static final String VALUE_NOT_IN_LIST_MSG = "Value not in the list";

    private static final String EMPTY_MSG = "- No terms found -";

    public VocabularyTermSelectionWidget(String idSuffix, String label, List<VocabularyTerm> terms,
            final boolean mandatory)
    {
        super(idSuffix, ModelDataPropertyNames.CODE, label, CHOOSE_MSG, EMPTY_MSG,
                VALUE_NOT_IN_LIST_MSG, mandatory);
        FieldUtil.setMandatoryFlag(this, mandatory);
        setAllowBlank(mandatory == false);
        final List<VocabularyTermModel> models = new ArrayList<VocabularyTermModel>();
        models.add(new VocabularyTermModel(GWTUtils.NONE_LIST_ITEM));
        models.addAll(VocabularyTermModel.convert(terms));
        updateStore(models);
        getPropertyEditor().setList(store.getModels());
    }

}
