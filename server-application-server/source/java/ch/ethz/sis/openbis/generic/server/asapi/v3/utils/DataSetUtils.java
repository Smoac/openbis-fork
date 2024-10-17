package ch.ethz.sis.openbis.generic.server.asapi.v3.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnsupportedObjectIdException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

public class DataSetUtils
{

    public static <T> T executeWithAfsDataVisible(IDAOFactory daoFactory, Collection<? extends IDataSetId> dataSetIds, Supplier<T> action)
    {
        Collection<String> dataSetCodes = new ArrayList<>();

        for (IDataSetId dataSetId : dataSetIds)
        {
            if (dataSetId != null)
            {
                if (dataSetId instanceof DataSetPermId)
                {
                    dataSetCodes.add(((DataSetPermId) dataSetId).getPermId());
                } else
                {
                    throw new UnsupportedObjectIdException(dataSetId);
                }
            }
        }

        List<TechId> afsDataSetIds = daoFactory.getDataDAO().listAfsDataSetIdsByCodes(dataSetCodes);

        if (!afsDataSetIds.isEmpty())
        {
            daoFactory.getDataDAO().updateAfsDataFlag(afsDataSetIds, false);
        }

        try
        {
            return action.get();
        } finally
        {
            if (!afsDataSetIds.isEmpty())
            {
                daoFactory.getDataDAO().updateAfsDataFlag(afsDataSetIds, true);
            }
        }
    }

}
