package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.Map;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.LuceneOptions;

public class ExternalDataGlobalSearchBridge extends GlobalSearchBridge<ExternalDataPE>
{

    @Override
    public Map<String, IndexedValue> collect(ExternalDataPE data)
    {
        DataGlobalSearchBridge<ExternalDataPE> db = new DataGlobalSearchBridge<>();
        Map<String, IndexedValue> values = db.collect(data);

        if (data.getLocatorType() != null)
        {
            put(values, "Locator type", data.getLocatorType().getCode());
        }
        put(values, "Location", data.getLocation());
        put(values, "Share id", data.getShareId());
        if (data.getSize() != null)
        {
            put(values, "Size", data.getSize().toString());
        }
        if (data.getStorageFormatVocabularyTerm() != null)
        {
            put(values, "Storage format", data.getStorageFormatVocabularyTerm().getCode());
        }
        if (data.getFileFormatType() != null)
        {
            put(values, "File format type", data.getFileFormatType().getCode());
        }
        if (data.getComplete() != null)
        {
            put(values, "Complete", data.getComplete().name());
        }
        if (data.getStatus() != null)
        {
            put(values, "Status", data.getStatus().name());
        }
        put(values, "Present in archive", Boolean.toString(data.isPresentInArchive()));
        put(values, "Storage confirmation", Boolean.toString(data.isStorageConfirmation()));
        put(values, "Speed hint", Integer.toString(data.getSpeedHint()));
        return values;
    }

    @Override
    protected boolean shouldIndex(String name, Object value, Document document, LuceneOptions luceneOptions)
    {
        return true;
    }
}
