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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import com.google.gwt.http.client.URL;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField.SampleChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.DataSetUploadInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.DataSetUploadInfo.DataSetUploadInfoHelper;

/**
 * Panel that allows to specify information necessary to upload data sets and redirects user to
 * specified upload service.
 * 
 * @author Izabela Adamczyk
 */
public class DataSetUploadForm extends AbstractRegistrationForm
{

    private static final String ID_SUFFIX = "data-set-upload-panel";

    public static final String ID = GenericConstants.ID_PREFIX + ID_SUFFIX;

    private String cifexURL;

    private String cifexRecipient;

    private DataSetTypeSelectionWidget dataSetTypeSelectionWidget;

    private SampleChooserFieldAdaptor sampleChooser;

    private FileFormatTypeSelectionWidget fileTypeSelectionWidget;

    public DataSetUploadForm(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, ID_SUFFIX);
        cifexURL = viewContext.getModel().getApplicationInfo().getCIFEXURL();
        cifexRecipient = viewContext.getModel().getApplicationInfo().getCifexRecipient();
        saveButton.setText(viewContext.getMessage(Dict.BUTTON_UPLOAD_DATA_VIA_CIFEX));
        sampleChooser =
                SampleChooserField.create(viewContext.getMessage(Dict.SAMPLE), true, null, false,
                        true, viewContext);
        formPanel.add(sampleChooser.getField());
        formPanel.add(dataSetTypeSelectionWidget =
                new DataSetTypeSelectionWidget(viewContext, ID_SUFFIX));
        FieldUtil.markAsMandatory(dataSetTypeSelectionWidget);
        formPanel.add(fileTypeSelectionWidget =
                new FileFormatTypeSelectionWidget(viewContext, ID_SUFFIX));
        FieldUtil.markAsMandatory(fileTypeSelectionWidget);
        if (StringUtils.isBlank(cifexRecipient) || StringUtils.isBlank(cifexURL))
        {
            formPanel.disable();
            infoBox.displayError(viewContext.getMessage(Dict.MESSAGE_NO_EXTERNAL_UPLOAD_SERVICE));
        }
    }

    public static DatabaseModificationAwareComponent create(
            IViewContext<ICommonClientServiceAsync> viewContext)
    {
        DataSetUploadForm form = new DataSetUploadForm(viewContext);
        IDatabaseModificationObserver observer = form.createDatabaseModificationObserver();
        return new DatabaseModificationAwareComponent(form, observer);
    }

    private IDatabaseModificationObserver createDatabaseModificationObserver()
    {
        CompositeDatabaseModificationObserver observer =
                new CompositeDatabaseModificationObserver();
        observer.addObserver(dataSetTypeSelectionWidget);
        observer.addObserver(fileTypeSelectionWidget);
        return observer;
    }

    @Override
    protected void submitValidForm()
    {
        String comment =
                encodeDataSetInfo(sampleChooser.getValue(), dataSetTypeSelectionWidget
                        .tryGetSelected().getCode(), fileTypeSelectionWidget.tryGetSelected()
                        .getCode());
        WindowUtils.openWindow(encodeCifexRequest(cifexURL, cifexRecipient, comment));
    }

    private String encodeDataSetInfo(String sample, String dataSetType, String fileType)
    {
        return DataSetUploadInfoHelper.encodeAsCifexComment(new DataSetUploadInfo(sample,
                dataSetType, fileType));
    }

    private static String encodeCifexRequest(String cifexUrl, String recipient, String comment)
    {
        URLMethodWithParameters url = new URLMethodWithParameters(cifexUrl);
        url.addParameter(BasicConstant.CIFEX_URL_PARAMETER_COMMENT, comment);
        url.addParameter(BasicConstant.CIFEX_URL_PARAMETER_RECIPIENT, recipient);
        return URL.encode(url.toString());
    }

}
