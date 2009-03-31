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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.VocabularyTermModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SimpleDropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * @author Izabela Adamczyk
 */
public class VocabularyTermSelectionWidget extends
        SimpleDropDownList<VocabularyTermModel, VocabularyTerm>
{

    private static final String CHOOSE_MSG = "Choose...";

    private static final String VALUE_NOT_IN_LIST_MSG = "Value not in the list";

    private static final String EMPTY_MSG = "- No terms found -";

    private final Vocabulary vocabularyOrNull;

    private final IViewContext<?> viewContextOrNull;

    /**
     * Allows to choose one of the specified vocabulary's terms, is able to refresh the available
     * terms by calling the server.
     */
    public static DatabaseModificationAwareField<VocabularyTermModel> create(String idSuffix,
            String label, Vocabulary vocabulary, final boolean mandatory,
            IViewContext<?> viewContext)
    {
        return new VocabularyTermSelectionWidget(idSuffix, label, vocabulary.getTerms(), mandatory,
                vocabulary, viewContext).asDatabaseModificationAware();
    }

    /**
     * Allows to choose one of the specified vocabulary terms.
     */
    public VocabularyTermSelectionWidget(String idSuffix, String label, List<VocabularyTerm> terms,
            final boolean mandatory)
    {
        this(idSuffix, label, terms, mandatory, null, null);
    }

    private VocabularyTermSelectionWidget(String idSuffix, String label,
            List<VocabularyTerm> terms, boolean mandatory, Vocabulary vocabularyOrNull,
            IViewContext<?> viewContextOrNull)
    {
        super(idSuffix, ModelDataPropertyNames.CODE, label, CHOOSE_MSG, EMPTY_MSG,
                VALUE_NOT_IN_LIST_MSG, mandatory);
        this.viewContextOrNull = viewContextOrNull;
        this.vocabularyOrNull = vocabularyOrNull;

        FieldUtil.setMandatoryFlag(this, mandatory);
        setAllowBlank(mandatory == false);
        setTerms(terms);
    }

    private void setTerms(List<VocabularyTerm> terms)
    {
        final List<VocabularyTermModel> models = new ArrayList<VocabularyTermModel>();
        models.add(new VocabularyTermModel(GWTUtils.NONE_LIST_ITEM));
        models.addAll(convertItems(terms));
        updateStore(models);
        getPropertyEditor().setList(store.getModels());
    }

    @Override
    public void refreshStore()
    {
        if (viewContextOrNull != null)
        {
            refreshStore(viewContextOrNull);
        }
    }

    @Override
    protected List<VocabularyTermModel> convertItems(List<VocabularyTerm> result)
    {
        return VocabularyTermModel.convert(result);
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<VocabularyTerm>> callback)
    {
        if (viewContextOrNull != null && vocabularyOrNull != null)
        {
            viewContextOrNull.getCommonService().listVocabularyTerms(vocabularyOrNull, callback);
        }
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.VOCABULARY_TERM);
    }
}
