package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.LuceneOptions;

public class LinkDataGlobalSearchBridge extends GlobalSearchBridge<LinkDataPE>
{

    @Override
    public Map<String, IndexedValue> collect(LinkDataPE data)
    {
        DataGlobalSearchBridge<LinkDataPE> db = new DataGlobalSearchBridge<>();
        Map<String, IndexedValue> values = db.collect(data);

        ArrayList<ContentCopyPE> copies = new ArrayList<>(data.getContentCopies());
        Collections.sort(copies, new Comparator<ContentCopyPE>()
            {
                @Override
                public int compare(ContentCopyPE copy1, ContentCopyPE copy2)
                {
                    return copy1.getId().compareTo(copy2.getId());
                }
            });

        int index = 1;
        for (ContentCopyPE copy : copies)
        {
            addCopy(values, index++, copy);
        }
        return values;
    }

    private void addCopy(Map<String, IndexedValue> values, int index, ContentCopyPE copy)
    {
        ExternalDataManagementSystemPE edms = copy.getExternalDataManagementSystem();
        String code = edms.getCode();
        String label = edms.getLabel();
        String externalDms;
        if (label == null || label.length() == 0)
        {
            externalDms = code;
        } else
        {
            externalDms = code + " (" + label + ")";
        }

        put(values, "External DMS of copy " + index, externalDms);
        put(values, "Address of copy " + index, copy.getExternalDataManagementSystem().getAddress());

        String externalCode = copy.getExternalCode();
        String path = copy.getPath();
        String hash = copy.getGitCommitHash();

        if (externalCode != null)
        {
            put(values, "External code of copy " + index, externalCode);
        }
        if (path != null)
        {
            put(values, "Path of copy " + index, path);
        }
        if (hash != null)
        {
            put(values, "Commit hash of copy " + index, hash);
        }
    }

    @Override
    protected boolean shouldIndex(String name, Object value, Document document, LuceneOptions luceneOptions)
    {
        return true;
    }
}
