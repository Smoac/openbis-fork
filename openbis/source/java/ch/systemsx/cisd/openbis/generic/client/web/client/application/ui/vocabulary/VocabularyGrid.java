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
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.VocabularyColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.VocabularyRegistrationFieldSet.CommonVocabularyRegistrationAndEditionFieldsFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
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

    private final IDelegatedAction postEditionCallback;

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

        final Button addButton =
                new Button(viewContext.getMessage(Dict.BUTTON_ADD, "Vocabulary"),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public void componentSelected(ButtonEvent ce)
                                {
                                    DispatcherHelper.dispatchNaviEvent(new ComponentProvider(
                                            viewContext).getVocabularyRegistration());
                                }
                            });
        addButton(addButton);

        Button showDetailsButton =
                createSelectedItemButton(viewContext.getMessage(Dict.BUTTON_SHOW_DETAILS),
                        new ISelectedEntityInvoker<BaseEntityModel<Vocabulary>>()
                            {
                                public void invoke(BaseEntityModel<Vocabulary> selectedItem,
                                        boolean keyPressed)
                                {
                                    showEntityViewer(selectedItem.getBaseObject(), false,
                                            keyPressed);
                                }
                            });
        showDetailsButton.setId(SHOW_DETAILS_BUTTON_ID);
        addButton(showDetailsButton);

        addButton(createSelectedItemButton(viewContext.getMessage(Dict.BUTTON_EDIT),
                new ISelectedEntityInvoker<BaseEntityModel<Vocabulary>>()
                    {

                        public void invoke(BaseEntityModel<Vocabulary> selectedItem,
                                boolean keyPressed)
                        {
                            Vocabulary vocabulary = selectedItem.getBaseObject();
                            if (vocabulary.isManagedInternally())
                            {
                                String errorMsg = "Internally managed vocabulary cannot be edited.";
                                MessageBox.alert("Error", errorMsg, null);
                            } else
                            {
                                createEditEntityDialog(vocabulary).show();
                            }
                        }

                    }));

        addButton(createSelectedItemsButton(viewContext.getMessage(Dict.BUTTON_DELETE),
                new AbstractCreateDialogListener()
                    {

                        @Override
                        protected Dialog createDialog(List<Vocabulary> vocabularies,
                                IBrowserGridActionInvoker invoker)
                        {
                            return new VocabularyListDeletionConfirmationDialog(viewContext,
                                    vocabularies, createDeletionCallback(invoker));
                        }

                        @Override
                        protected boolean validateSelectedData(List<Vocabulary> data)
                        {
                            String errorMsg = "Internally managed vocabularies cannot be deleted.";
                            for (Vocabulary vocabulary : data)
                            {
                                if (vocabulary.isManagedInternally())
                                {
                                    MessageBox.alert("Error", errorMsg, null);
                                    return false;
                                }
                            }
                            return true;
                        }
                    }));

        allowMultipleSelection(); // we allow deletion of multiple vocabularies

        addEntityOperationsSeparator();
    }

    private VocabularyGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, GRID_ID, DisplayTypeIDGenerator.VOCABULARY_BROWSER_GRID);
        postEditionCallback = createRefreshGridAction();
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
        schema.setGridCellRendererFor(VocabularyColDefKind.DESCRIPTION.id(),
                createMultilineStringCellRenderer());
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
    protected void showEntityViewer(final Vocabulary vocabulary, boolean editMode,
            boolean inBackground)
    {
        final AbstractTabItemFactory tabFactory = new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component =
                            VocabularyTermGrid.create(viewContext, vocabulary);
                    return DefaultTabItem.create(getTabTitle(), component, viewContext);
                }

                @Override
                public String getId()
                {
                    return VocabularyTermGrid.createBrowserId(vocabulary);
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.TERM, HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return viewContext.getMessage(Dict.VOCABULARY_TERMS_BROWSER, vocabulary
                            .getCode());
                }
            };
        tabFactory.setInBackground(inBackground);
        DispatcherHelper.dispatchNaviEvent(tabFactory);
    }

    private Component createEditEntityDialog(final Vocabulary vocabulary)
    {
        String title =
                viewContext.getMessage(Dict.EDIT_TITLE, Dict.VOCABULARY, vocabulary.getCode());
        return new AbstractRegistrationDialog(viewContext, title, postEditionCallback)
            {
                private final TextField<String> codeField;

                private final DescriptionField descriptionField;

                private final TextField<String> urlTemplateField;

                private final static int LABEL_WIDTH = 100;

                private final static int FIELD_WIDTH = 350;

                private final CheckBox chosenFromList;

                {
                    form.setLabelWidth(LABEL_WIDTH);
                    form.setFieldWidth(FIELD_WIDTH);
                    setWidth(LABEL_WIDTH + FIELD_WIDTH + 50);

                    codeField = createMandatoryCodeField();
                    codeField.setValue(getOldVocabularyCode());
                    addField(codeField);

                    descriptionField = createDescriptionField(viewContext);
                    descriptionField.setValueAndUnescape(vocabulary.getDescription());
                    addField(descriptionField);

                    urlTemplateField = createURLTemplateField();
                    urlTemplateField.setValue(StringEscapeUtils.unescapeHtml(vocabulary
                            .getURLTemplate()));
                    addField(urlTemplateField);

                    chosenFromList = createChosenFromListCheckbox();
                    FieldUtil.setValueWithoutEvents(chosenFromList, vocabulary.isChosenFromList());
                    addField(chosenFromList);

                }

                @Override
                protected void register(AsyncCallback<Void> registrationCallback)
                {
                    vocabulary.setCode(codeField.getValue());
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

                private String getOldVocabularyCode()
                {
                    return StringEscapeUtils.unescapeHtml(vocabulary.getCode());
                }
            };
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { DatabaseModificationKind.createOrDelete(ObjectKind.VOCABULARY) };
    }

}
