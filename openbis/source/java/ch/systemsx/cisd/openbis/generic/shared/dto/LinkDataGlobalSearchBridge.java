package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.LuceneOptions;

import ch.systemsx.cisd.common.string.StringUtilities;

public class LinkDataGlobalSearchBridge extends GlobalSearchBridge<LinkDataPE>
{

    @Override
    public Map<String, IndexedValue> collect(LinkDataPE data)
    {
        DataGlobalSearchBridge<LinkDataPE> db = new DataGlobalSearchBridge<>();
        Map<String, IndexedValue> values = db.collect(data);

        Set<String> externalCodes = new HashSet<>();
        Set<String> externalDmsCodes = new HashSet<>();

        for (ContentCopyPE copy : data.getContentCopies())
        {
            if (copy.getExternalCode() != null)
            {
                externalCodes.add(copy.getExternalCode());
            }
            externalDmsCodes.add(copy.getExternalDataManagementSystem().getCode());
        }

        if (externalCodes.isEmpty() == false)
        {
            put(values, "External code", StringUtilities.concatenateWithSpace(new ArrayList<>(externalCodes)));
        }

        if (externalDmsCodes.isEmpty() == false)
        {
            put(values, "External dms", StringUtilities.concatenateWithSpace(new ArrayList<>(externalDmsCodes)));
        }
        return values;
    }

    @Override
    protected boolean shouldIndex(String name, Object value, Document document, LuceneOptions luceneOptions)
    {
        return true;
    }
}
