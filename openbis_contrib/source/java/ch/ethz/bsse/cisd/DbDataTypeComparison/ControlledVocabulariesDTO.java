package ch.ethz.bsse.cisd.DbDataTypeComparison;

import net.lemnik.eodsql.ResultColumn;
import ch.systemsx.cisd.common.utilities.AbstractHashable;

/**
 * @author Manuel Kohler
 */

class ControlledVocabulariesDTO extends AbstractHashable
{
    @ResultColumn("controlledVocabularies")
    private String controlledVocabularies;

    ControlledVocabulariesDTO()
    {
        // All Data-Object classes must have a default constructor.
    }

    public ControlledVocabulariesDTO(String controlledVocabularies)
    {
        super();
        this.controlledVocabularies = controlledVocabularies;
    }

    public String getControlledVocabularies()
    {
        return controlledVocabularies;
    }

    public void setControlledVocabularies(String controlledVocabularies)
    {
        this.controlledVocabularies = controlledVocabularies;
    }

}
