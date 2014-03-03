package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.Validator;

/**
 * A {@link Validator} implementation which validates vocabulary terms in a text area.
 * 
 * @author Christian Ribeaud
 */
final class LabelAndDescriptionTermsValidator implements Validator
{
    private final String failureMessage;

    private final TextArea termsField;

    public LabelAndDescriptionTermsValidator(TextArea termsField, String failureMessage)
    {
        this.termsField = termsField;
        this.failureMessage = failureMessage;
    }

    @Override
    final public String validate(Field<?> field, String value)
    {

        final List<VocabularyTerm> terms = VocabularyTermValidator.getTerms(termsField.getValue());
        final String[] labelsOrDescriptions = value.split(",");
        if (labelsOrDescriptions.length == 0 || labelsOrDescriptions.length == terms.size())
        {
            return null;
        } else
        {
            return failureMessage;
        }
    }
}
