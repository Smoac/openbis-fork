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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.VocabularyTermReplacementModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.VocabularyTermColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SimpleDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.VocabularyTermWithStats;
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
    // browser consists of the grid and the paging toolbar
    private static final String BROWSER_ID = GenericConstants.ID_PREFIX + "vocabulary-term-browser";
    
    final class VoidCallback extends AbstractAsyncCallback<Void>
    {
        private VoidCallback(IViewContext<?> viewContext)
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
        Button deleteButton = new Button(viewContext.getMessage(Dict.DELETE_VOCABULARY_TERMS_BUTTON));
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
    protected void prepareExportEntities(TableExportCriteria<VocabularyTermWithStats> exportCriteria,
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
        textArea.setValidator(new VocabularyTermValidator(viewContext, vocabulary.getTerms()));
        String heading = viewContext.getMessage(Dict.ADD_VOCABULARY_TERMS_TITLE);
        String okButtonLabel = viewContext.getMessage(Dict.ADD_VOCABULARY_TERMS_OK_BUTTON);
        HorizontalPanel panel = new HorizontalPanel();
        panel.setWidth(300);
        panel.add(textArea);
        panel.setBorders(false);
        final SimpleDialog dialog = new SimpleDialog(panel, heading, okButtonLabel, viewContext);
        dialog.setScrollMode(Scroll.NONE);
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
                            new VoidCallback(viewContext));
                }
            });
        dialog.setEnableOfAcceptButton(false);
        dialog.show();
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
            MessageBox.alert(
                    viewContext.getMessage(Dict.DELETE_VOCABULARY_TERMS_INVALID_TITLE),
                    viewContext.getMessage(Dict.DELETE_VOCABULARY_TERMS_INVALID_MESSAGE), null);
            return;
        }
        Set<String> selectedTerms = new HashSet<String>();
        List<VocabularyTerm> termsToBeDeleted = new ArrayList<VocabularyTerm>();
        List<VocabularyTermReplacement> termsToBeReplaced = new ArrayList<VocabularyTermReplacement>();
        for (BaseEntityModel<VocabularyTermWithStats> model : terms)
        {
            VocabularyTerm term = model.getBaseObject().getTerm();
            selectedTerms.add(term.getCode());
            if (model.getBaseObject().getTotalUsageCounter() > 0)
            {
                VocabularyTermReplacement termTpBeReplaced = new VocabularyTermReplacement();
                termTpBeReplaced.setTerm(term);
                termsToBeReplaced.add(termTpBeReplaced);
            } else
            {
                termsToBeDeleted.add(term);
            }
        }
        if (termsToBeReplaced.isEmpty() == false)
        {
            List<VocabularyTerm> termsForReplacement = new ArrayList<VocabularyTerm>();
            for (VocabularyTerm term : vocabulary.getTerms())
            {
                if (selectedTerms.contains(term.getCode()) == false)
                {
                    termsForReplacement.add(term);
                }
            }
            if (termsForReplacement.size() == 1)
            {
                VocabularyTerm replacement = termsForReplacement.get(0);
                for (VocabularyTermReplacement termToBeReplaced : termsToBeReplaced)
                {
                    termToBeReplaced.setReplacement(replacement);
                }
            } else
            {
                ColumnConfig originalColumn = new ColumnConfig();
                originalColumn.setRenderer(new GridCellRenderer<VocabularyTermReplacementModel>()
                    {
                        public String render(VocabularyTermReplacementModel model, String property,
                                ColumnData config, int rowIndex, int colIndex,
                                ListStore<VocabularyTermReplacementModel> store)
                        {
                            VocabularyTermReplacementModel row = store.getAt(rowIndex);
                            return row.getTermReplacement().getTerm().getCode();
                        }
                    });
                originalColumn.setHeader(viewContext.getMessage(Dict.DELETE_VOCABULARY_TERMS_ORIGINAL_COLUMN));
                originalColumn.setWidth(200);
                
                ColumnConfig replacementColumn = new ColumnConfig();
                replacementColumn.setHeader(viewContext.getMessage(Dict.DELETE_VOCABULARY_TERMS_REPLACEMENT_COLUMN));
                replacementColumn.setWidth(200);
                replacementColumn.setEditor(new CellEditor(new LabelField()));
                ColumnModel columnModel = new ColumnModel(Arrays.asList(originalColumn, replacementColumn));
                ListStore<VocabularyTermReplacementModel> store = new ListStore<VocabularyTermReplacementModel>();
                for (VocabularyTermReplacement termToBeReplaced : termsToBeReplaced)
                {
                    store.add(new VocabularyTermReplacementModel(termToBeReplaced));
                }
                Grid<VocabularyTermReplacementModel> replacementGrid = new Grid<VocabularyTermReplacementModel>(store, columnModel);
                SimpleDialog dialog = new SimpleDialog(replacementGrid, "h", "o", viewContext);
                dialog.setScrollMode(Scroll.AUTOY);
                dialog.show();
            }
        }
    }
}