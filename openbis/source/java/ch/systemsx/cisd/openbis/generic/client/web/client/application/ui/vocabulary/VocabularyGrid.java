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

import static ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant.USER_NAMESPACE_PREFIX;

import java.util.List;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.VocabularyColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.VocabularyRegistrationFieldSet.CommonVocabularyRegistrationAndEditionFieldsFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * Grid displaying vocabularies.
 * 
 * @author Tomasz Pylak
 */
public class VocabularyGrid extends AbstractSimpleBrowserGrid<Vocabulary>
{
    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "vocabulary-browser";

    public static final String GRID_ID = BROWSER_ID + "_grid";

    public static final String SHOW_DETAILS_BUTTON_ID = BROWSER_ID + "_show-details-button";

    private IDelegatedAction postEditionCallback;

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final VocabularyGrid grid = new VocabularyGrid(viewContext);
        grid.extendBottomToolbar();
        return grid.asDisposableWithoutToolbar();
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        Button showDetailsButton =
                createSelectedItemButton(viewContext.getMessage(Dict.BUTTON_SHOW_DETAILS),
                        new ISelectedEntityInvoker<BaseEntityModel<Vocabulary>>()
                            {
                                public void invoke(BaseEntityModel<Vocabulary> selectedItem)
                                {
                                    showEntityViewer(selectedItem.getBaseObject(), false);
                                }
                            });
        showDetailsButton.setId(SHOW_DETAILS_BUTTON_ID);
        pagingToolbar.add(new AdapterToolItem(showDetailsButton));

        Button editButton =
                createSelectedItemButton(viewContext.getMessage(Dict.BUTTON_EDIT),
                        new ISelectedEntityInvoker<BaseEntityModel<Vocabulary>>()
                            {

                                public void invoke(BaseEntityModel<Vocabulary> selectedItem)
                                {
                                    Vocabulary vocabulary = selectedItem.getBaseObject();
                                    if (vocabulary.isInternalNamespace())
                                    {
                                        MessageBox.alert("Error",
                                                "Internally managed vocabulary cannot be edited.",
                                                null);
                                    } else
                                    {
                                        createEditEntityDialog(vocabulary).show();
                                    }
                                }

                            });
        pagingToolbar.add(new AdapterToolItem(editButton));

        addEntityOperationsSeparator();
    }

    private VocabularyGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, GRID_ID);
        setDisplayTypeIDGenerator(DisplayTypeIDGenerator.VOCABULARY_BROWSER_GRID);
        postEditionCallback = new IDelegatedAction()
            {
                public void execute()
                {
                    refresh();
                }
            };
    }

    @Override
    protected IColumnDefinitionKind<Vocabulary>[] getStaticColumnsDefinition()
    {
        return VocabularyColDefKind.values();
    }

    @Override
    protected ColumnDefsAndConfigs<Vocabulary> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<Vocabulary> schema = super.createColumnsDefinition();
        schema.setGridCellRendererFor(VocabularyColDefKind.CODE.id(), LinkRenderer
                .createLinkRenderer());
        return schema;
    }

    @Override
    protected List<IColumnDefinition<Vocabulary>> getInitialFilters()
    {
        return asColumnFilters(new VocabularyColDefKind[]
            { VocabularyColDefKind.CODE });
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, Vocabulary> resultSetConfig,
            AbstractAsyncCallback<ResultSet<Vocabulary>> callback)
    {
        viewContext.getService().listVocabularies(false, false, resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<Vocabulary> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportVocabularies(exportCriteria, callback);
    }

    @Override
    protected void showEntityViewer(final Vocabulary vocabulary, boolean editMode)
    {
        final ITabItemFactory tabFactory = new ITabItemFactory()
            {
                public ITabItem create()
                {
                    IDisposableComponent component =
                            VocabularyTermGrid.create(viewContext, vocabulary);
                    String tabTitle =
                            viewContext.getMessage(Dict.VOCABULARY_TERMS_BROWSER, vocabulary
                                    .getCode());
                    return DefaultTabItem.create(tabTitle, component, viewContext);
                }

                public String getId()
                {
                    return VocabularyTermGrid.createBrowserId(vocabulary);
                }
            };
        DispatcherHelper.dispatchNaviEvent(tabFactory);
    }

    private Component createEditEntityDialog(final Vocabulary vocabulary)
    {
        String title =
                viewContext.getMessage(Dict.EDIT_TITLE, Dict.VOCABULARY, vocabulary.getCode());
        return new AbstractRegistrationDialog(viewContext, title, postEditionCallback)
            {
                private final TextField<String> codeField;

                private final TextField<String> descriptionField;

                private final TextField<String> urlTemplateField;

                private final CheckBox chosenFromList;

                {
                    codeField = createMandatoryCodeField();
                    codeField.setValue(getOldVocabularyCodeWithoutPrefix());
                    addField(codeField);

                    descriptionField = createDescriptionField();
                    descriptionField.setValue(StringEscapeUtils.unescapeHtml(vocabulary
                            .getDescription()));
                    addField(descriptionField);

                    urlTemplateField = createURLTemplateField();
                    urlTemplateField.setValue(StringEscapeUtils.unescapeHtml(vocabulary
                            .getURLTemplate()));
                    addField(urlTemplateField);

                    chosenFromList = createChosenFromListCheckbox();
                    chosenFromList.setValue(vocabulary.isChosenFromList());
                    addField(chosenFromList);
                }

                @Override
                protected void register(AsyncCallback<Void> registrationCallback)
                {
                    vocabulary.setCode(getCodePrefix() + codeField.getValue());
                    vocabulary.setDescription(descriptionField.getValue());
                    vocabulary.setURLTemplate(urlTemplateField.getValue());
                    vocabulary.setChosenFromList(chosenFromList.getValue());
                    viewContext.getService().updateVocabulary(vocabulary, registrationCallback);
                }

                private TextField<String> createMandatoryCodeField()
                {
                    return CommonVocabularyRegistrationAndEditionFieldsFactory
                            .createCodeField(viewContext);
                }

                private TextField<String> createURLTemplateField()
                {
                    return CommonVocabularyRegistrationAndEditionFieldsFactory
                            .createURLTemplateField(viewContext);
                }

                private CheckBox createChosenFromListCheckbox()
                {
                    return CommonVocabularyRegistrationAndEditionFieldsFactory
                            .createChosenFromListCheckbox(viewContext);
                }

                private String getOldVocabularyCodeWithoutPrefix()
                {
                    String code = vocabulary.getCode();
                    String prefix = getCodePrefix();
                    assert code.startsWith(prefix) : "code does not start with " + prefix;
                    return StringEscapeUtils.unescapeHtml(code.substring(prefix.length()));
                }

                private String getCodePrefix()
                {
                    return USER_NAMESPACE_PREFIX;
                }
            };
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { DatabaseModificationKind.createOrDelete(ObjectKind.VOCABULARY),
                    DatabaseModificationKind.createOrDelete(ObjectKind.VOCABULARY_TERM) };
    }

}