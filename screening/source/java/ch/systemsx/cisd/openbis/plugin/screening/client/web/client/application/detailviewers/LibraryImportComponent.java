package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField.wrapUnaware;

import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.FormPanelListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.file.BasicFileFieldManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningModule;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;

/**
 * Allows to specify search criteria for materials contained in wells. Used in experiment section
 * panel or as a standalone module.
 * 
 * @author Tomasz Pylak
 */
public class LibraryImportComponent extends TabContent
{

    private static class LibraryImportForm extends AbstractRegistrationForm
    {
        protected final class RegisterOrUpdateSamplesAndMaterialsCallback
                extends
                AbstractRegistrationForm.AbstractRegistrationCallback<List<BatchRegistrationResult>>
        {
            RegisterOrUpdateSamplesAndMaterialsCallback(
                    final IViewContext<IGenericClientServiceAsync> viewContext)
            {
                super(viewContext);
            }

            @Override
            protected String createSuccessfullRegistrationInfo(
                    final List<BatchRegistrationResult> result)
            {
                final StringBuilder builder = new StringBuilder();
                for (final BatchRegistrationResult batchRegistrationResult : result)
                {
                    builder.append("<b>" + batchRegistrationResult.getFileName() + "</b>: ");
                    builder.append(batchRegistrationResult.getMessage());
                    builder.append("<br />");
                }
                return builder.toString();
            }
        }

        private static final String PREFIX = "library-import";

        private static final String SESSION_KEY = PREFIX;

        private static final String FIELD_LABEL_TEMPLATE = "File";

        private static final int NUMBER_OF_FIELDS = 1;

        private final BasicFileFieldManager fileFieldsManager;

        private final IViewContext<IGenericClientServiceAsync> genericViewContext;

        private static String createId(String sessionKey)
        {
            return ScreeningModule.ID + sessionKey;
        }

        /**
         * @param viewContext
         */
        protected LibraryImportForm(IViewContext<IGenericClientServiceAsync> viewContext)
        {
            super(viewContext, createId(SESSION_KEY));

            this.genericViewContext = viewContext;
            setScrollMode(Scroll.AUTO);
            fileFieldsManager =
                    new BasicFileFieldManager(SESSION_KEY, NUMBER_OF_FIELDS, FIELD_LABEL_TEMPLATE);
            fileFieldsManager.setMandatory();
            addUploadFeatures(SESSION_KEY);
        }

        @Override
        protected final void onRender(final Element target, final int index)
        {
            super.onRender(target, index);
            addFormFields();
        }

        @Override
        protected void submitValidForm()
        {
        }

        @Override
        protected void resetFieldsAfterSave()
        {
            for (FileUploadField attachmentField : fileFieldsManager.getFields())
            {
                attachmentField.reset();
            }
        }

        private final void addFormFields()
        {
            for (FileUploadField attachmentField : fileFieldsManager.getFields())
            {
                formPanel.add(wrapUnaware((Field<?>) attachmentField).get());
            }
            formPanel.addListener(Events.Submit, new FormPanelListener(infoBox)
                {
                    @Override
                    protected void onSuccessfullUpload()
                    {
                        save();
                    }

                    @Override
                    protected void setUploadEnabled()
                    {
                        LibraryImportForm.this.setUploadEnabled(true);
                    }
                });
            redefineSaveListeners();
        }

        void redefineSaveListeners()
        {
            saveButton.removeAllListeners();
            saveButton.addSelectionListener(new SelectionListener<ButtonEvent>()
                {
                    @Override
                    public final void componentSelected(final ButtonEvent ce)
                    {
                        if (formPanel.isValid())
                        {
                            if (fileFieldsManager.filesDefined() > 0)
                            {
                                setUploadEnabled(false);
                                formPanel.submit();
                            } else
                            {
                                save();
                            }
                        }
                    }
                });
        }

        protected void save()
        {
            genericViewContext.getService().registerOrUpdateSamplesAndMaterials(SESSION_KEY, null,
                    true, new RegisterOrUpdateSamplesAndMaterialsCallback(genericViewContext));
        }
    }

    private final IViewContext<IGenericClientServiceAsync> genericViewContext;

    public static String getTabTitle(IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        return viewContext.getMessage(Dict.LIBRARY_IMPORT_TAB_TITLE);
    }

    public LibraryImportComponent(IViewContext<IScreeningClientServiceAsync> screeningViewContext)
    {
        this(screeningViewContext, ExperimentSearchCriteria.createAllExperiments());

        setContentVisible(true);
    }

    public LibraryImportComponent(IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            ExperimentSearchCriteria experimentSearchCriteria)
    {
        super(getTabTitle(screeningViewContext), screeningViewContext, null);
        this.genericViewContext =
                new GenericViewContext(screeningViewContext.getCommonViewContext());
    }

    @Override
    protected void showContent()
    {
        removeAll();
        add(new LibraryImportForm(genericViewContext));
        layout();
    }
}