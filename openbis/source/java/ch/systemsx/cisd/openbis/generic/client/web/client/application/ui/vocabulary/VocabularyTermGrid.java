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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.VocabularyTermModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.VocabularyTermColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VocabularyTermSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SimpleDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;

/**
 * Grid displaying vocabularies.
 * 
 * @author Tomasz Pylak
 */
public class VocabularyTermGrid extends AbstractSimpleBrowserGrid<VocabularyTermWithStats>
{
    private static final int FIELD_WITH_IN_REPLACEMENT_DIALOG = 200;
    private static final int LABEL_WIDTH_IN_REPLACEMENT_DIALOG = 200;
    
    // browser consists of the grid and the paging toolbar
    private static final String BROWSER_ID = GenericConstants.ID_PREFIX + "vocabulary-term-browser";
    
    final class RefreshCallback extends AbstractAsyncCallback<Void>
    {
        private RefreshCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(Void result)
        {
            refresh();
        }
    }

    private final Vocabulary vocabulary;

    public static DisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext, Vocabulary vocabulary)
    {
        return new VocabularyTermGrid(viewContext, vocabulary).asDisposableWithoutToolbar();
    }

    private VocabularyTermGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            Vocabulary vocabulary)
    {
        super(viewContext, createBrowserId(vocabulary), createGridId(vocabulary.getCode()));
        this.vocabulary = vocabulary;
        if (vocabulary.isManagedInternally() == false)
        {
            Button addButton = new Button(viewContext.getMessage(Dict.ADD_VOCABULARY_TERMS_BUTTON));
            addButton.addSelectionListener(new SelectionListener<ButtonEvent>()
                    {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    askForNewTerms();
                }
                    });
            pagingToolbar.add(new AdapterToolItem(addButton));
            Button deleteButton =
                new Button(viewContext.getMessage(Dict.DELETE_VOCABULARY_TERMS_BUTTON));
            deleteButton.addSelectionListener(new SelectionListener<ButtonEvent>()
                    {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    deleteTerms();
                }
                    });
            pagingToolbar.add(new AdapterToolItem(deleteButton));
            allowMultipleSelection();
        }
    }

    public static String createGridId(String vocabularyCode)
    {
        return createBrowserId(vocabularyCode) + "-grid";
    }

    public static String createBrowserId(Vocabulary vocabulary)
    {
        return createBrowserId(vocabulary.getCode());
    }

    public static String createBrowserId(String vocabularyCode)
    {
        return BROWSER_ID + "-" + vocabularyCode;
    }

    @Override
    protected IColumnDefinitionKind<VocabularyTermWithStats>[] getStaticColumnsDefinition()
    {
        return VocabularyTermColDefKind.values();
    }

    @Override
    protected List<IColumnDefinition<VocabularyTermWithStats>> getAvailableFilters()
    {
        return asColumnFilters(new VocabularyTermColDefKind[]
            { VocabularyTermColDefKind.CODE });
    }

    @Override
    protected void listEntities(
            DefaultResultSetConfig<String, VocabularyTermWithStats> resultSetConfig,
            AbstractAsyncCallback<ResultSet<VocabularyTermWithStats>> callback)
    {
        viewContext.getService().listVocabularyTerms(vocabulary, resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<VocabularyTermWithStats> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportVocabularyTerms(exportCriteria, callback);
    }

    private void askForNewTerms()
    {
        final TextArea textArea = new TextArea();
        textArea.setWidth(250);
        textArea.setHeight(200);
        textArea.setEmptyText(viewContext.getMessage(Dict.VOCABULARY_TERMS_EMPTY));
        textArea.setValidator(new VocabularyTermValidator(viewContext));
        String heading = viewContext.getMessage(Dict.ADD_VOCABULARY_TERMS_TITLE);
        String okButtonLabel = viewContext.getMessage(Dict.ADD_VOCABULARY_TERMS_OK_BUTTON);
        HorizontalPanel panel = new HorizontalPanel();
        panel.setWidth(300);
        panel.add(textArea);
        panel.setBorders(false);
        final SimpleDialog dialog = new SimpleDialog(panel, heading, okButtonLabel, viewContext);
        dialog.setScrollMode(Scroll.NONE);
        dialog.setResizable(false);
        textArea.addKeyListener(new KeyListener()
            {
                @Override
                public void handleEvent(ComponentEvent ce)
                {
                    textArea.validate();
                    dialog.setEnableOfAcceptButton(textArea.isValid());
                }
            });
        dialog.setAcceptAction(new IDelegatedAction()
            {
                public void execute()
                {
                    viewContext.getCommonService().addVocabularyTerms(vocabulary.getCode(),
                            VocabularyTermValidator.getTerms(textArea.getValue()),
                            new RefreshCallback(viewContext));
                }
            });
        dialog.setEnableOfAcceptButton(false);
        dialog.show();
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        // grid is refreshed manually when a new object is added, so there can be no auto-refresh
        return new DatabaseModificationKind[] {};
    }

    private void deleteTerms()
    {
        List<BaseEntityModel<VocabularyTermWithStats>> terms = getSelectedItems();
        if (terms.isEmpty())
        {
            return;
        }
        if (terms.size() == vocabulary.getTerms().size())
        {
            MessageBox.alert(viewContext.getMessage(Dict.DELETE_VOCABULARY_TERMS_INVALID_TITLE),
                    viewContext.getMessage(Dict.DELETE_VOCABULARY_TERMS_INVALID_MESSAGE), null);
            return;
        }
        Set<String> selectedTerms = new HashSet<String>();
        List<VocabularyTerm> termsToBeDeleted = new ArrayList<VocabularyTerm>();
        List<VocabularyTermReplacement> termsToBeReplaced =
                new ArrayList<VocabularyTermReplacement>();
        for (BaseEntityModel<VocabularyTermWithStats> model : terms)
        {
            VocabularyTerm term = model.getBaseObject().getTerm();
            selectedTerms.add(term.getCode());
            if (model.getBaseObject().getTotalUsageCounter() > 0)
            {
                VocabularyTermReplacement termToBeReplaced = new VocabularyTermReplacement();
                termToBeReplaced.setTerm(term);
                termsToBeReplaced.add(termToBeReplaced);
            } else
            {
                termsToBeDeleted.add(term);
            }
        }
        deleteAndReplace(selectedTerms, termsToBeDeleted, termsToBeReplaced);
    }

    private void deleteAndReplace(Set<String> selectedTerms,
            final List<VocabularyTerm> termsToBeDeleted,
            final List<VocabularyTermReplacement> termsToBeReplaced)
    {
        if (termsToBeReplaced.isEmpty())
        {
            String title = viewContext.getMessage(Dict.DELETE_VOCABULARY_TERMS_CONFIRMATION_TITLE);
            int size = termsToBeDeleted.size();
            String message;
            if (size == 1)
            {
                message =
                        viewContext
                                .getMessage(Dict.DELETE_VOCABULARY_TERMS_CONFIRMATION_MESSAGE_NO_REPLACEMENTS_SINGULAR);
            } else
            {
                message =
                        viewContext.getMessage(
                                Dict.DELETE_VOCABULARY_TERMS_CONFIRMATION_MESSAGE_NO_REPLACEMENTS,
                                size);
            }
            ConfirmationDialog confirmationDialog = new ConfirmationDialog(title, message)
                {
                    @Override
                    protected void onYes()
                    {
                        deleteAndReplace(termsToBeDeleted, termsToBeReplaced);
                    }
                };
            confirmationDialog.show();
        } else
        {
            List<VocabularyTerm> termsForReplacement = new ArrayList<VocabularyTerm>();
            for (VocabularyTerm term : vocabulary.getTerms())
            {
                if (selectedTerms.contains(term.getCode()) == false)
                {
                    termsForReplacement.add(term);
                }
            }
            askForReplacements(termsToBeDeleted, termsToBeReplaced, termsForReplacement);
        }
    }

    private void askForReplacements(final List<VocabularyTerm> termsToBeDeleted,
            final List<VocabularyTermReplacement> termsToBeReplaced,
            List<VocabularyTerm> termsForReplacement)
    {
        VerticalPanel panel = new VerticalPanel();
        int totalNumber = termsToBeDeleted.size() + termsToBeReplaced.size();
        panel.add(new Text(viewContext.getMessage(
                Dict.DELETE_VOCABULARY_TERMS_CONFIRMATION_MESSAGE_FOR_REPLACEMENTS, totalNumber)));
        final FormPanel formPanel = new FormPanel();
        formPanel.setLabelWidth(LABEL_WIDTH_IN_REPLACEMENT_DIALOG);
        formPanel.setFieldWidth(FIELD_WITH_IN_REPLACEMENT_DIALOG);
        formPanel.setBorders(false);
        formPanel.setHeaderVisible(false);
        formPanel.setBodyBorder(false);
        panel.add(formPanel);
        String title = viewContext.getMessage(Dict.DELETE_VOCABULARY_TERMS_CONFIRMATION_TITLE);
        String okButtonLable = viewContext.getMessage(Dict.ADD_VOCABULARY_TERMS_OK_BUTTON);
        final SimpleDialog dialog = new SimpleDialog(panel, title, okButtonLable, viewContext);
        dialog.setScrollMode(Scroll.AUTOY);
        dialog.setWidth(LABEL_WIDTH_IN_REPLACEMENT_DIALOG + FIELD_WITH_IN_REPLACEMENT_DIALOG + 50);
        dialog.setEnableOfAcceptButton(false);
        for (final VocabularyTermReplacement termToBeReplaced : termsToBeReplaced)
        {
            String term = termToBeReplaced.getTerm().getCode();
            final VocabularyTermSelectionWidget s =
                    new VocabularyTermSelectionWidget(getId() + term, term, termsForReplacement,
                            true);
            s.addSelectionChangedListener(new SelectionChangedListener<VocabularyTermModel>()
                {
                    @Override
                    public void selectionChanged(SelectionChangedEvent<VocabularyTermModel> se)
                    {
                        VocabularyTermModel selectedItem = se.getSelectedItem();
                        termToBeReplaced.setReplacement(selectedItem == null ? null : selectedItem
                                .getTerm());
                        dialog.setEnableOfAcceptButton(formPanel.isValid());
                    }
                });
            formPanel.add(s);
        }
        dialog.setAcceptAction(new IDelegatedAction()
            {

                public void execute()
                {
                    deleteAndReplace(termsToBeDeleted, termsToBeReplaced);
                }

            });
        dialog.show();
    }
    
    private void deleteAndReplace(List<VocabularyTerm> termsToBeDeleted,
            List<VocabularyTermReplacement> termsToBeReplaced)
    {
        String code = vocabulary.getCode();
        RefreshCallback callback = new RefreshCallback(viewContext);
        viewContext.getService().deleteVocabularyTerms(code, termsToBeDeleted, termsToBeReplaced,
                callback);
        
    }
}