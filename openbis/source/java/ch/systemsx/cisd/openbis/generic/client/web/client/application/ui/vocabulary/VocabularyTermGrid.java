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

import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
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
        Button button = new Button(viewContext.getMessage(Dict.ADD_VOCABULARY_TERMS_BUTTON));
        button.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    askForNewTerms();
                }
            });
        pagingToolbar.add(new AdapterToolItem(button));
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
        panel.setBorders(true);
        final SimpleDialog dialog = new SimpleDialog(panel, heading, okButtonLabel, viewContext);
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
        dialog.show();
    }
}