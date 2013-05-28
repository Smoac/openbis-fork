package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.CallbackListenerAdapter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.VocabularyRegistrationForm.VocabularyRegistrationCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class AddVocabularyDialog extends AbstractRegistrationDialog
{

    private final VocabularyRegistrationForm vocabularyRegistrationForm;

    public class VocabularyPopUpCallbackListener extends CallbackListenerAdapter<Void>
    {
        @Override
        public void onFailureOf(final IMessageProvider messageProvider,
                final AbstractAsyncCallback<Void> callback, final String failureMessage,
                final Throwable throwable)
        {
            MessageBox.alert("Error", failureMessage, null);
        }

        @Override
        public void finishOnSuccessOf(final AbstractAsyncCallback<Void> callback, final Void result)
        {
            VocabularyRegistrationCallback vocabularyCallback = (VocabularyRegistrationCallback) callback;
            MessageBox.info("Success", vocabularyCallback.createSuccessfullRegistrationInfo(null), null);
            hide();
        }

    }

    public AddVocabularyDialog(IViewContext<ICommonClientServiceAsync> viewContext, IDelegatedAction postRegistrationCallback)
    {
        super(viewContext, "Register Vocabulary", postRegistrationCallback);
        vocabularyRegistrationForm = new VocabularyRegistrationForm(viewContext, true, new VocabularyPopUpCallbackListener());
        addField(vocabularyRegistrationForm);
    }

    @Override
    protected void afterRender()
    {
        super.afterRender();

        this.getFormPanel().layout();
        this.layout();

        vocabularyRegistrationForm.getBody().setStyleAttribute("background-color", "transparent");
        vocabularyRegistrationForm.getFormPanel().getBody().setStyleAttribute("background-color", "transparent");
        int innerWidth = AbstractRegistrationForm.DEFAULT_LABEL_WIDTH + AbstractRegistrationForm.DEFAULT_FIELD_WIDTH + 111;
        int innerHeight = 400;
        vocabularyRegistrationForm.getFormPanel().setSize(innerWidth, innerHeight);
        this.setSize(innerWidth - 30, innerHeight + 80);
        this.center();
    }

    @Override
    protected void register(AsyncCallback<Void> registrationCallback)
    {
        vocabularyRegistrationForm.submitValidForm();
    }
}
