package ch.ethz.bsse.cisd.DbDataTypeComparison;

import net.lemnik.eodsql.ResultColumn;
import ch.systemsx.cisd.common.utilities.AbstractHashable;

/**
 * @author Manuel Kohler
 */
class TermsOfControlledVocabulariesDTO extends AbstractHashable
{

    @ResultColumn("controlledVocabularies")
    private String controlledVocabularies;

    @ResultColumn("controlledVocabulariesTerms")
    private String controlledVocabulariesTerms;

    TermsOfControlledVocabulariesDTO()
    {
        // All Data-Object classes must have a default constructor.
    }

    public TermsOfControlledVocabulariesDTO(String controlledVocabularies,
            String controlledVocabulariesTerms)
    {
        super();
        this.controlledVocabularies = controlledVocabularies;
        this.controlledVocabulariesTerms = controlledVocabulariesTerms;
    }

    public String getControlledVocabularies()
    {
        return controlledVocabularies;
    }

    public void setControlledVocabularies(String controlledVocabularies)
    {
        this.controlledVocabularies = controlledVocabularies;
    }

    public String getControlledVocabulariesTerms()
    {
        return controlledVocabulariesTerms;
    }

    public void setControlledVocabulariesTerms(String controlledVocabulariesTerms)
    {
        this.controlledVocabulariesTerms = controlledVocabulariesTerms;
    }
}
