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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
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
        DropDownList<VocabularyTermModel, VocabularyTerm>
{

    private static final String CHOOSE_MSG = "Choose...";

    private static final String VALUE_NOT_IN_LIST_MSG = "Value not in the list";

    private static final String EMPTY_MSG = "- No terms found -";

    private final IViewContext<?> viewContextOrNull;

    private Vocabulary vocabularyOrNull;

    private String initialTermCodeOrNull;

    private boolean dataLoaded = false;

    /**
     * Allows to choose one of the specified vocabulary's terms, is able to refresh the available
     * terms by calling the server.
     */
    public static DatabaseModificationAwareField<VocabularyTermModel> create(String idSuffix,
            String label, Vocabulary vocabulary, final boolean mandatory,
            IViewContext<?> viewContext, String initialTermCodeOrNull)
    {
        return new VocabularyTermSelectionWidget(idSuffix, label, mandatory, vocabulary,
                viewContext, null, initialTermCodeOrNull).asDatabaseModificationAware();
    }

    // TODO 2009-10-08, Piotr Buczek: use this for ordinal edition
    /**
     * Allows to choose one of the specified vocabulary terms.
     */
    public VocabularyTermSelectionWidget(String idSuffix, String label, final boolean mandatory,
            List<VocabularyTerm> initialTermsOrNull, String initialTermOrNull)
    {
        this(idSuffix, label, mandatory, null, null, initialTermsOrNull, initialTermOrNull);
    }

    protected VocabularyTermSelectionWidget(String idSuffix, String label, boolean mandatory,
            Vocabulary vocabularyOrNull, IViewContext<?> viewContextOrNull,
            List<VocabularyTerm> termsOrNull, String initialTermCodeOrNull)
    {
        super(idSuffix, ModelDataPropertyNames.CODE_WITH_LABEL, label, CHOOSE_MSG, EMPTY_MSG,
                VALUE_NOT_IN_LIST_MSG, mandatory, viewContextOrNull, termsOrNull == null);
        this.viewContextOrNull = viewContextOrNull;
        this.vocabularyOrNull = vocabularyOrNull;
        this.initialTermCodeOrNull = initialTermCodeOrNull;
        FieldUtil.setMandatoryFlag(this, mandatory);
        setAllowBlank(mandatory == false);
        if (termsOrNull != null)
        {
            setTerms(termsOrNull);
        }
        setTemplate(getTooltipTemplate(ModelDataPropertyNames.CODE_WITH_LABEL,
                ModelDataPropertyNames.TOOLTIP));
    }

    public void setVocabulary(Vocabulary vocabulary)
    {
        vocabularyOrNull = vocabulary;
        refreshStore();
    }

    private void setTerms(List<VocabularyTerm> terms)
    {
        final List<VocabularyTermModel> models = new ArrayList<VocabularyTermModel>();
        models.addAll(convertItems(terms));
        updateStore(models);
        getPropertyEditor().setList(store.getModels());
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
            viewContextOrNull.getCommonService().listVocabularyTerms(vocabularyOrNull,
                    new ListTermsCallback(viewContextOrNull));
        }
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.VOCABULARY_TERM);
    }

    public void selectTermAndUpdateOriginal(String term)
    {
        this.initialTermCodeOrNull = term;
        if (dataLoaded && initialTermCodeOrNull != null)
        {
            trySelectByCode(initialTermCodeOrNull);
            updateOriginalValue();
        }
    }

    public void trySelectByCode(String termCode)
    {
        GWTUtils.setSelectedItem(this, ModelDataPropertyNames.CODE, termCode);
    }

    public void updateOriginalValue()
    {
        setOriginalValue(getValue());
    }

    public class ListTermsCallback extends VocabularyTermSelectionWidget.ListItemsCallback
    {

        protected ListTermsCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        public void process(List<VocabularyTerm> result)
        {
            super.process(result);
            dataLoaded = true;
            selectTermAndUpdateOriginal(initialTermCodeOrNull);
        }
    }

    private native String getTooltipTemplate(String displayField, String tooltipField) /*-{ 
                   return  [ 
                   '<tpl for=".">', 
                   '<div class="x-combo-list-item" qtip="{[values.',
                   tooltipField,
                   ']}">{[values.',
                   displayField,
                   ']}</div>', 
                   '</tpl>' 
                   ].join(""); 
                 }-*/;

}
