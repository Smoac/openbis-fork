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
import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.VocabularyTermModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField.CodeFieldKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

/**
 * @author Izabela Adamczyk
 */
public class VocabularyTermSelectionWidget extends
        DropDownList<VocabularyTermModel, VocabularyTerm>
{

    private class UnofficialTermRegistrationDialog extends AbstractRegistrationDialog
    {
        private final IViewContext<?> viewContext;

        private final String code;

        private final Vocabulary vocabulary;

        public UnofficialTermRegistrationDialog(IViewContext<?> viewContext, String code)
        {
            super(viewContext, viewContext
                    .getMessage(Dict.ADD_UNOFFICIAL_VOCABULARY_TERM_DIALOG_TITLE),
                    createRefreshAction(code));

            this.viewContext = viewContext;
            this.code = code;
            this.vocabulary = vocabularyOrNull;

            addField(new Label(viewContext.getMessage(
                    Dict.ADD_UNOFFICIAL_VOCABULARY_TERM_DIALOG_MESSAGE, code, vocabulary.getCode())));
        }

        @Override
        protected void register(AsyncCallback<Void> registrationCallback)
        {
            ICommonClientServiceAsync service = viewContext.getCommonService();

            service.addUnofficialVocabularyTerms(TechId.create(vocabulary), Arrays.asList(code),
                    getMaxOrdinal(), registrationCallback);
            hide();
        }

        private long getMaxOrdinal()
        {
            long result = 0l;

            // WORKAROUND for some strange reason getStore().getModels() returns empty list
            for (VocabularyTermModel term : VocabularyTermSelectionWidget.this.store.getModels())
            {
                if (term.getTerm().getOrdinal() > result)
                {
                    result = term.getTerm().getOrdinal();
                }
            }
            return result;
        }
    }

    private static final String CHOOSE_MSG = "Choose...";

    private static final String CHOOSE_OR_ADD_MSG = "Choose or add new...";

    private static final String VALUE_NOT_IN_LIST_MSG = "Value not in the list";

    private static final String EMPTY_MSG = "- No terms found -";

    private final IViewContext<?> viewContextOrNull;

    private Vocabulary vocabularyOrNull;

    private String initialTermCodeOrNull;

    private String typedValueOrNull = null;

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

    /**
     * Allows to choose one of the specified vocabulary terms.
     */
    public VocabularyTermSelectionWidget(String idSuffix, String label, final boolean mandatory,
            List<VocabularyTerm> initialTermsOrNull, String initialTermOrNull)
    {
        this(idSuffix, label, mandatory, null, null, initialTermsOrNull, initialTermOrNull);
    }

    protected VocabularyTermSelectionWidget(String idSuffix, String label, boolean mandatory,
            Vocabulary vocabularyOrNull, final IViewContext<?> viewContextOrNull,
            List<VocabularyTerm> termsOrNull, String initialTermCodeOrNull)
    {
        super(idSuffix, ModelDataPropertyNames.CODE_WITH_LABEL, label,
                allowAddingUnofficialTerms(viewContextOrNull) ? CHOOSE_OR_ADD_MSG : CHOOSE_MSG,
                EMPTY_MSG, VALUE_NOT_IN_LIST_MSG, mandatory, viewContextOrNull, termsOrNull == null);
        this.viewContextOrNull = viewContextOrNull;
        this.vocabularyOrNull = vocabularyOrNull;
        this.initialTermCodeOrNull = initialTermCodeOrNull;
        FieldUtil.setMandatoryFlag(this, mandatory);
        setAllowBlank(mandatory == false);
        if (termsOrNull != null)
        {
            setTerms(termsOrNull);
        }

        setLazyRender(false);
        setTemplate(GWTUtils.getTooltipTemplate(VocabularyTermModel.DISPLAY_FIELD,
                ModelDataPropertyNames.TOOLTIP));

        if (allowAddingUnofficialTerms(viewContextOrNull))
        {
            this.addListener(Events.Blur, createListenerAddingOnofficialTerms());
        }
    }

    private static boolean allowAddingUnofficialTerms(IViewContext<?> viewContextOrNull)
    {
        return viewContextOrNull != null
                && viewContextOrNull.getModel().getApplicationInfo().getWebClientConfiguration()
                        .getAllowAddingUnofficialTerms();
    }

    private Listener<BaseEvent> createListenerAddingOnofficialTerms()
    {
        return new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    if (VocabularyTermSelectionWidget.this.vocabularyOrNull != null
                            && getSelection().size() != 1
                            && (false == StringUtils.isBlank(VocabularyTermSelectionWidget.this
                                    .getRawValue())))
                    {
                        final String code = getRawValue().toUpperCase();
                        if (!code.matches(CodeFieldKind.CODE_WITH_COLON.getPattern()))
                        {
                            Dialog d = new Dialog()
                                {
                                    @Override
                                    protected void onButtonPressed(Button button)
                                    {
                                        super.onButtonPressed(button);
                                        VocabularyTermSelectionWidget.this.focus();
                                    }
                                };
                            d.setHeading(viewContextOrNull.getMessage(Dict.MESSAGEBOX_ERROR));
                            d.addText(viewContextOrNull.getMessage(Dict.INVALID_CODE_MESSAGE,
                                    CodeFieldKind.CODE_WITH_COLON.getAllowedCharacters()));
                            d.setSize(400, 200);
                            d.setHideOnButtonClick(true);
                            d.setButtons(Dialog.OK);
                            d.show();
                        } else
                        {
                            UnofficialTermRegistrationDialog d =
                                    new UnofficialTermRegistrationDialog(viewContextOrNull, code);
                            d.show();
                        }
                    }
                }
            };
    }

    private IDelegatedAction createRefreshAction(final String _typedValue)
    {
        return new IDelegatedAction()
            {
                public void execute()
                {
                    VocabularyTermSelectionWidget.this.typedValueOrNull = _typedValue;
                    refreshStore();
                    clearInvalid();
                    focus();
                }
            };
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
        getPropertyEditor().setList(store.getModels()); // see workaround description above
        selectInitialValue();
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
        callback.ignore();
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.VOCABULARY_TERM);
    }

    public void selectInitialValue()
    {
        if (typedValueOrNull != null)
        {
            try
            {
                trySelectByCode(typedValueOrNull);
                updateOriginalValue();
            } finally
            {
                typedValueOrNull = null;
            }
        } else if (initialTermCodeOrNull != null)
        {
            trySelectByCode(initialTermCodeOrNull);
            updateOriginalValue();
        }
    }

    public void trySelectByCode(String termCode)
    {
        GWTUtils.setSelectedItem(this, ModelDataPropertyNames.CODE, termCode);
    }

    private class ListTermsCallback extends VocabularyTermSelectionWidget.ListItemsCallback
    {

        protected ListTermsCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        public void process(List<VocabularyTerm> result)
        {
            super.process(result);
            selectInitialValue();
        }
    }

}
