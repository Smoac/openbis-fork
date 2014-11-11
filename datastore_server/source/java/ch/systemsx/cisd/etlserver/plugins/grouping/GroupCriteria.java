package ch.systemsx.cisd.etlserver.plugins.grouping;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

public interface GroupCriteria
{
    String group(AbstractExternalData dataset);
}
