/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.user;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IntegerField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractSaveDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DialogWithOnlineHelpUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RealNumberFormatingParameters;

/**
 * {@link Window} containing form for changing logged user settings.
 * 
 * @author Piotr Buczek
 */
public class ChangeUserSettingsDialog extends AbstractSaveDialog
{
    public static final String DIALOG_ID =
            GenericConstants.ID_PREFIX + "change-user-settings-dialog";

    public static final String GROUP_FIELD_ID = DIALOG_ID + "-group-field";

    private final IViewContext<?> viewContext;

    private final GroupSelectionWidget homeGroupField;

    private final CheckBoxField useWildcardSearchModeCheckbox;

    private final FieldSet formatingFields;

    private final CheckBoxField scientificFormatingField;

    private final CheckBoxField errorMessageFormattingField;

    private final IntegerField precisionField;

    private final IDelegatedAction resetCallback;

    public ChangeUserSettingsDialog(final IViewContext<?> viewContext,
            final IDelegatedAction saveCallback, final IDelegatedAction resetCallback)
    {
        super(viewContext, viewContext.getMessage(Dict.CHANGE_USER_SETTINGS_DIALOG_TITLE),
                saveCallback);
        this.viewContext = viewContext;
        this.resetCallback = resetCallback;
        form.setLabelWidth(150);
        form.setFieldWidth(400);
        // setHeight(250);

        addField(homeGroupField = createHomeGroupField());
        addField(useWildcardSearchModeCheckbox = createUseWildcardSearchModeField());
        formatingFields = createRealFormatingFieldSet();
        precisionField = createPrecisionField();
        formatingFields.add(precisionField);
        scientificFormatingField = createScientificCheckBox();
        formatingFields.add(scientificFormatingField);
        addField(formatingFields);
        errorMessageFormattingField = createErrorMessageFormattingCheckBox();
        addField(errorMessageFormattingField);
        fbar.insert(createResetButton(), 1); // inserting Reset button in between Save and Cancel

        DialogWithOnlineHelpUtils.addHelpButton(viewContext.getCommonViewContext(), this,
                createHelpPageIdentifier());
    }

    //
    // Change
    //

    private final GroupSelectionWidget createHomeGroupField()
    {
        GroupSelectionWidget field = new GroupSelectionWidget(viewContext, GROUP_FIELD_ID, false);
        FieldUtil.setMandatoryFlag(field, false);
        field.setFieldLabel(viewContext.getMessage(Dict.HOME_GROUP_LABEL));
        return field;
    }

    private final CheckBoxField createUseWildcardSearchModeField()
    {
        CheckBoxField field =
                new CheckBoxField(viewContext.getMessage(Dict.USE_WILDCARD_SEARCH_MODE_LABEL), true);
        GWTUtils.setToolTip(field, viewContext.getMessage(Dict.USE_WILDCARD_SEARCH_MODE_TOOLTIP));
        field.setValue(viewContext.getDisplaySettingsManager().isUseWildcardSearchMode());
        return field;
    }

    @Override
    protected void save(AsyncCallback<Void> saveCallback)
    {
        Group group = homeGroupField.tryGetSelected();
        String groupCodeOrNull = group == null ? null : group.getCode();
        TechId groupIdOrNull = TechId.create(group);
        viewContext.getModel().getSessionContext().getUser().setHomeGroupCode(groupCodeOrNull);
        viewContext.getService().changeUserHomeGroup(groupIdOrNull, saveCallback);

        RealNumberFormatingParameters formatingParameters = getRealNumberFormatingParameters();
        formatingParameters.setFormatingEnabled(formatingFields.isExpanded());
        formatingParameters.setPrecision(precisionField.getValue().intValue());
        formatingParameters.setScientific(scientificFormatingField.getValue());
        DisplaySettingsManager displaySettingsManager = viewContext.getDisplaySettingsManager();
        boolean useWildcardSearchMode = extractUseWildcardSearchMode();
        displaySettingsManager.updateUseWildcardSearchMode(useWildcardSearchMode);

        boolean useCustomColumnDebuggingMessages = errorMessageFormattingField.getValue();
        displaySettingsManager
                .setDisplayCustomColumnDebuggingErrorMessages(useCustomColumnDebuggingMessages);

        displaySettingsManager.storeSettings();
    }

    private FieldSet createRealFormatingFieldSet()
    {
        FieldSet fields = new FieldSet();
        fields.setHeading(viewContext.getMessage(Dict.REAL_NUMBER_FORMATING_FIELDS));
        fields.setCheckboxToggle(true);
        fields.setExpanded(getRealNumberFormatingParameters().isFormatingEnabled());
        FormLayout layout = new FormLayout();
        layout.setLabelWidth(139);
        fields.setLayout(layout);
        Listener<BaseEvent> listener = new Listener<BaseEvent>()
            {

                public void handleEvent(BaseEvent be)
                {
                    layout(true);
                }

            };
        fields.addListener(Events.Expand, listener);
        fields.addListener(Events.Collapse, listener);
        return fields;
    }

    private IntegerField createPrecisionField()
    {
        IntegerField field =
                new IntegerField(viewContext.getMessage(Dict.REAL_NUMBER_FORMATING_PRECISION),
                        false);
        field.setValue(getRealNumberFormatingParameters().getPrecision());
        return field;
    }

    private CheckBoxField createScientificCheckBox()
    {
        CheckBoxField field =
                new CheckBoxField(viewContext.getMessage(Dict.SCIENTIFIC_FORMATING), false);
        field.setValue(getRealNumberFormatingParameters().isScientific());
        return field;
    }

    private CheckBoxField createErrorMessageFormattingCheckBox()
    {
        CheckBoxField field =
                new CheckBoxField(viewContext.getMessage(Dict.ERROR_MESSAGE_FORMATING), false);
        field.setValue(viewContext.getDisplaySettingsManager().isDisplayCustomColumnDebuggingErrorMessages());
        return field;
    }

    private RealNumberFormatingParameters getRealNumberFormatingParameters()
    {
        return viewContext.getDisplaySettingsManager().getRealNumberFormatingParameters();
    }

    private boolean extractUseWildcardSearchMode()
    {
        return useWildcardSearchModeCheckbox.getValue();
    }

    //
    // Reset
    //

    private Button createResetButton()
    {
        final String buttonTitle = viewContext.getMessage(Dict.RESET_USER_SETTINGS_BUTTON);
        final Button button = new Button(buttonTitle, new SelectionListener<ButtonEvent>()
            {
                @Override
                public final void componentSelected(ButtonEvent ce)
                {
                    final String title = viewContext.getMessage(Dict.CONFIRM_TITLE);
                    final String msg =
                            viewContext.getMessage(Dict.RESET_USER_SETTINGS_CONFIRMATION_MSG);
                    MessageBox.confirm(title, msg, new Listener<MessageBoxEvent>()
                        {
                            public void handleEvent(MessageBoxEvent messageEvent)
                            {
                                if (messageEvent.getButtonClicked().getItemId().equals(Dialog.YES))
                                {
                                    resetUserSettings();
                                }
                            }
                        });
                }
            });
        return button;
    }

    private void resetUserSettings()
    {
        viewContext.getService().resetDisplaySettings(new ResetUserSettingsCallback(viewContext));
    }

    public final class ResetUserSettingsCallback extends AbstractAsyncCallback<DisplaySettings>
    {
        private ResetUserSettingsCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        public final void process(final DisplaySettings defaultDisplaySettings)
        {
            // reinitialize DisplaySettingsManager with updated SessionContext
            viewContext.getModel().getSessionContext().setDisplaySettings(defaultDisplaySettings);
            viewContext.initDisplaySettingsManager();
            resetCallback.execute();
            hide();
        }
    }

    private HelpPageIdentifier createHelpPageIdentifier()
    {
        return new HelpPageIdentifier(HelpPageIdentifier.HelpPageDomain.CHANGE_USER_SETTINGS,
                HelpPageIdentifier.HelpPageAction.ACTION);
    }
}
