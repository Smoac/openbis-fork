package ch.systemsx.cisd.openbis.generic.server.authorization.predicate.v3ToV1;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;

public class DataSetIdTranslator
{

    public static String translate(IDataSetId v3datasetId)
    {
        if (v3datasetId instanceof CreationId)
        {

        } else if (v3datasetId instanceof DataSetPermId)
        {
            return ((DataSetPermId) v3datasetId).getPermId();
        }

        return "";
    }
}
