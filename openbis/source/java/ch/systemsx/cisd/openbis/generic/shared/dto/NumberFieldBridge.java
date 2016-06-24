package ch.systemsx.cisd.openbis.generic.shared.dto;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SortableNumberBridgeUtils;

public class NumberFieldBridge implements FieldBridge
{

    @Override
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions)
    {
        if (value == null)
        {
            return;
        }

        Number numberValue = (Number) value;
        String textValue = SortableNumberBridgeUtils.getNumberForLucene(numberValue);

        Field field = new Field(name, textValue, luceneOptions.getStore(), Field.Index.NOT_ANALYZED_NO_NORMS);
        SortedNumericDocValuesField fieldIsdocTypeSortedNumeric = new SortedNumericDocValuesField(name, numberValue.longValue());

        field.setBoost(luceneOptions.getBoost());
        document.add(field);
        document.add(fieldIsdocTypeSortedNumeric);
    }

}