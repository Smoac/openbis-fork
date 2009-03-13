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

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.VocabularyTermColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

/**
 * Grid displaying vocabularies.
 * 
 * @author Tomasz Pylak
 */
public class VocabularyTermGrid extends AbstractSimpleBrowserGrid<VocabularyTerm>
{
    // browser consists of the grid and the paging toolbar
    private static final String BROWSER_ID = GenericConstants.ID_PREFIX + "vocabulary-term-browser";

    private final Vocabulary vocabulary;

    public static DisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext, Vocabulary vocabulary)
    {
        return new VocabularyTermGrid(viewContext, vocabulary).asDisposableWithoutToolbar();
    }

    private VocabularyTermGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            Vocabulary vocabulary)
    {
        super(viewContext, createBrowserId(vocabulary), createGridId(vocabulary));
        this.vocabulary = vocabulary;
    }

    public static String createGridId(Vocabulary vocabulary)
    {
        return createBrowserId(vocabulary) + "-grid";
    }

    public static String createBrowserId(Vocabulary vocabulary)
    {
        return BROWSER_ID + "-" + vocabulary.getCode();
    }

    @Override
    protected IColumnDefinitionKind<VocabularyTerm>[] getStaticColumnsDefinition()
    {
        return VocabularyTermColDefKind.values();
    }

    @Override
    protected List<IColumnDefinition<VocabularyTerm>> getAvailableFilters()
    {
        return asColumnFilters(new VocabularyTermColDefKind[]
            { VocabularyTermColDefKind.CODE });
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, VocabularyTerm> resultSetConfig,
            AbstractAsyncCallback<ResultSet<VocabularyTerm>> callback)
    {
        viewContext.getService().listVocabularyTerms(vocabulary, resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<VocabularyTerm> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportVocabularyTerms(exportCriteria, callback);
    }
}