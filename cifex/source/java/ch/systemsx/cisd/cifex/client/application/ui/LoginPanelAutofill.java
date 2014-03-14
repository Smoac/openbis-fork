/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.client.application.ui;

import static ch.systemsx.cisd.cifex.client.application.utils.InfoDictionary.START_PAGE_WELCOME_NOTE;
import static ch.systemsx.cisd.cifex.client.application.utils.InfoDictionary.info;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.LOGIN_FAILED_MSG;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.MESSAGE_BOX_WARNING_TITLE;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.msg;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;

import ch.systemsx.cisd.cifex.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.page.MainPage;
import ch.systemsx.cisd.cifex.client.application.utils.ImageUtils;
import ch.systemsx.cisd.cifex.shared.basic.dto.CurrentUserInfoDTO;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

/**
 * This class uses a variant of a trick described in the GWT discussion forum to support autofill.
 * Browsers do not support autofill on input fields that are generated on the client by javascript,
 * so it is necessary for the page to statically contain the input fields we want to autofill. These
 * fields are unhidden and used on the login page. Additionally we do a dummy POST operation to
 * convince the more stubborn browsers that this is a login call that deserves to be autofilled.
 * 
 * @see <a href
 *      ="http://groups.google.com/group/Google-Web-Toolkit/browse%5Fthread/thread/2b2ce0b6aaa82461">GWT
 *      Discussion Forum</a>
 * @author Chandrasekhar Ramakrishnan
 */
public class LoginPanelAutofill extends VerticalPanel
{
    private final ViewContext context;

    private final FormPanel formPanel;

    private static final String LOGIN_FORM_ID = "cifex_login_form";

    private static final String USERNAME_ID = "cifex-username";

    private static final String PASSWORD_ID = "cifex-password";

    private static final String SUBMIT_ID = "cifex-submit";

    private static LoginPanelAutofill singleton = null;

    /**
     * Method to get the singleton instance of the login autofill panel
     */
    public static LoginPanelAutofill get(final ViewContext context)
    {
        if (singleton == null)
        {
            singleton = new LoginPanelAutofill(context);
        }
        return singleton;
    }

    private LoginPanelAutofill(final ViewContext context)
    {
        this.context = context;

        setHorizontalAlign(HorizontalAlignment.CENTER);

        Element formElement = Document.get().getElementById(LOGIN_FORM_ID);
        if (formElement == null)
        {
            // This is a severe error and should never happen.
            MessageBox.alert("Internal Error", "Login form not found.", null);
            formPanel = null;
            return;
        }
        
        final Image cisdLogo = ImageUtils.getCIFEXSymbolImage();
        cisdLogo.setTitle(MainPage.CISD_LOGO_TITLE);
        cisdLogo.setPixelSize((int)(165*1.7), (int)(113*1.7));
        Anchor logo =  new Anchor(cisdLogo.getElement().getString(), true, "http://www.cisd.ethz.ch/", "_blank");
        
        formPanel = FormPanel.wrap(formElement, true);

        formPanel.addSubmitHandler(new SubmitHandler()
            {
                @Override
                public void onSubmit(SubmitEvent event)
                {
                    // Disable button until we have had a chance to validate the input.
                    getButtonElement().setDisabled(true);
                }
            });

        // Called when the dummy POST operation returns.
        formPanel.addSubmitCompleteHandler(new SubmitCompleteHandler()
            {
                @Override
                public void onSubmitComplete(SubmitCompleteEvent event)
                {
                    if (isUserInputValid() == false)
                    {
                        final String title = msg(MESSAGE_BOX_WARNING_TITLE);
                        MessageBox.alert(title, msg(LOGIN_FAILED_MSG), null);
                        getButtonElement().setDisabled(false);
                    } else
                    {
                        doLogin();
                    }
                }
            });

        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setHorizontalAlign(HorizontalAlignment.CENTER);
        verticalPanel.add(logo);
        verticalPanel.add(new InlineHTML("<p style='margin:15px; font-family:\"Helvetica Neue\",sans-serif; font-size:25px; font-weight:300;'>Sign in to CIFEX</p>"));
        verticalPanel.add(formPanel);
        verticalPanel.setStyleName("box-gradient box-gradient-silver");
        add(verticalPanel);
        
        add(new InlineHTML("<div style='padding:20px; font-family:\"Helvetica Neue\",sans-serif; font-size:16px; font-weight:300;'>Or:</div>"));
        add(createDownloadsPanelLittle());
        
        Text label = new Text(info(START_PAGE_WELCOME_NOTE));
        label.setStyleName("cifex-welcome-warning");
        add(label);
    }

    private static InlineHTML createDownloadsPanelLittle() {
        String style = "style='text-align:left; font-family:\"Helvetica Neue\",sans-serif; font-size:16px; font-weight:300;'";
        
        String  downloadPanelInlineHTML = "";
        downloadPanelInlineHTML += "<div class='box-gradient box-gradient-silver' style='width: 330px;'>";
        downloadPanelInlineHTML += "<div class='downloadButton' style='width: 200px; margin: 0 auto;'><a href='./tools.html'>App Download</a></div>";
        downloadPanelInlineHTML += "<br />";
        downloadPanelInlineHTML += "<p " + style + ">Advantages of the apps over the web interface:</p>";
        downloadPanelInlineHTML += "<p " + style + ">- Support for uploading files larger than 2GB.</p>";
        downloadPanelInlineHTML += "<p " + style + ">- Resuming interrupted uploads.</p>";
        downloadPanelInlineHTML += "<p " + style + ">- Support for encryption.</p>";
        downloadPanelInlineHTML += "</div>";
        return new InlineHTML(downloadPanelInlineHTML);
    }
    private final boolean isUserInputValid()
    {
        String username = getUsernameElement().getValue();
        String password = getPasswordElement().getValue();
        // Validate the input -- neither field can be blank
        return StringUtils.isBlank(username) == false && StringUtils.isBlank(password) == false;
    }

    private void giveFocusToFirstField()
    {
        getUsernameElement().focus();
    }

    @Override
    protected final void onLoad()
    {
        super.onLoad();
        getButtonElement().setDisabled(false);
        giveFocusToFirstField();
    }

    private final void doLogin()
    {
        InputElement usernameElement = getUsernameElement();
        InputElement passwordElement = getPasswordElement();

        final String user = usernameElement.getValue();
        final String password = passwordElement.getValue();

        context.getCifexService().tryLogin(user, password, new LoginAsyncCallBack());
    }

    public InputElement getPasswordElement()
    {
        return InputElement.as(Document.get().getElementById(PASSWORD_ID));
    }

    public InputElement getUsernameElement()
    {
        return InputElement.as(Document.get().getElementById(USERNAME_ID));
    }

    public final InputElement getButtonElement()
    {
        return InputElement.as(Document.get().getElementById(SUBMIT_ID));
    }

    /**
     * Method that gets called when everything goes right.
     * <p>
     * By default this method does nothing.
     * </p>
     */
    protected void loginSuccessful(final CurrentUserInfoDTO currentUser)
    {
        // Clear the password
        getPasswordElement().setValue(getPasswordElement().getDefaultValue());
        PageControllerHelper.activatePageBasedOnCurrentContext(context, currentUser);
    }

    //
    // Helper classes
    //

    private final class LoginAsyncCallBack extends AbstractAsyncCallback<CurrentUserInfoDTO>
    {

        LoginAsyncCallBack()
        {
            super(context);
        }

        @Override
        public final void onFailure(final Throwable caught)
        {
            super.onFailure(caught);
            getButtonElement().setDisabled(false);
        }

        @Override
        public final void onSuccess(final CurrentUserInfoDTO result)
        {
            if (result != null)
            {
                loginSuccessful(result);
            } else
            {
                final String title = msg(MESSAGE_BOX_WARNING_TITLE);
                MessageBox.alert(title, msg(LOGIN_FAILED_MSG), null);
                getButtonElement().setDisabled(false);
            }
        }
    }
}
