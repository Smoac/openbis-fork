package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.Validator;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;

/**
 * A {@link Validator} implementation which validates vocabulary terms in a text area.
 * 
 * @author Christian Ribeaud
 */
final class VocabularyTermValidator implements Validator<String, TextArea>
{

    private final IMessageProvider messageProvider;

    VocabularyTermValidator(final IMessageProvider messageProvider)
    {
        this.messageProvider = messageProvider;
    }

    final static List<String> getTerms(final String value)
    {
        final String[] split = value.split("[,\n\r\t\f ]");
        final List<String> terms = new ArrayList<String>();
        for (final String text : split)
        {
            if (StringUtils.isBlank(text) == false)
            {
                terms.add(text);
            }
        }
        return terms;
    }

    //
    // Validator
    //

    public final String validate(final TextArea field, final String value)
    {
        if (StringUtils.isBlank(value))
        {
            return null;
        }
        final List<String> terms = VocabularyTermValidator.getTerms(value);
        if (terms.size() == 0)
        {
            return null;
        }
        for (final String term : terms)
        {
            if (term.matches(CodeField.CODE_PATTERN) == false)
            {
                return messageProvider.getMessage(Dict.INVALID_CODE_MESSAGE, "Term '" + term + "'");
            }
        }
        return null;
    }
}