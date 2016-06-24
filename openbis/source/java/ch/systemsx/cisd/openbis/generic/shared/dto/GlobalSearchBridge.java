package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

public abstract class GlobalSearchBridge<T extends IEntityWithMetaprojects> implements FieldBridge
{

    protected class IndexedValue
    {
        public final String displayValue;

        public final String indexedValue;

        public IndexedValue(String displayValue, String indexedValue)
        {
            this.displayValue = displayValue;
            this.indexedValue = indexedValue;
        }

        @Override
        public String toString()
        {
            return "(" + displayValue + ", " + indexedValue + ")";
        }
    }

    protected IndexedValue iv(String s)
    {
        return new IndexedValue(s, s);
    }

    protected void put(Map<String, IndexedValue> map, String key, String value)
    {
        map.put(key, new IndexedValue(value, value));
    }

    protected static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");

    protected abstract Map<String, IndexedValue> collect(T entity);

    protected void addSpace(Map<String, IndexedValue> values, SpacePE space)
    {
        put(values, "Space code", space.getCode());
    }

    protected void addProject(Map<String, IndexedValue> values, ProjectPE project)
    {
        put(values, "Project code", project.getCode());
        addSpace(values, project.getSpace());
    }

    protected void addExperiment(Map<String, IndexedValue> values, ExperimentPE experiment)
    {
        put(values, "Experiment code", experiment.getCode());
        addProject(values, experiment.getProject());
    }

    protected <U extends EntityPropertyPE> void addProperties(Map<String, IndexedValue> values, Collection<U> properties)
    {
        if (properties != null)
        {
            for (U property : properties)
            {
                switch (property.getEntityTypePropertyType().getPropertyType().getType().getCode())
                {
                    case MULTILINE_VARCHAR:
                    case XML:
                        XMLInputFactory xif = XMLInputFactory.newFactory();
                        StringBuffer value = new StringBuffer();
                        String rawValue = property.tryGetUntypedValue().trim();
                        try
                        {
                            if (!rawValue.startsWith("<") || !rawValue.endsWith(">"))
                            {
                                throw new XMLStreamException("early fail");
                            }
                            XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(rawValue));
                            while (xsr.hasNext())
                            {
                                int x = xsr.next();
                                if (x == XMLStreamConstants.CHARACTERS)
                                {
                                    value.append(xsr.getText() + " ");
                                }
                            }
                        } catch (Exception e)
                        {
                            value = new StringBuffer(rawValue);
                        }
                        values.put("Property '" + property.getEntityTypePropertyType().getPropertyType().getLabel() + "'",
                                new IndexedValue(property.tryGetUntypedValue(), value.toString()));
                        break;
                    default:
                        put(values, "Property '" + property.getEntityTypePropertyType().getPropertyType().getLabel() + "'",
                                property.tryGetUntypedValue());
                        break;
                }

            }
        }
    }

    protected void addAttachments(Map<String, IndexedValue> values, Collection<AttachmentPE> attachments)
    {
        if (attachments != null)
        {
            for (AttachmentPE attachment : attachments)
            {
                put(values, "Title of '" + attachment.getFileName() + "'", attachment.getTitle());
                put(values, "Description of '" + attachment.getFileName() + "'", attachment.getDescription());
                put(values, "File name of '" + attachment.getFileName() + "'", attachment.getFileName());
            }
        }
    }

    protected void addPerson(Map<String, IndexedValue> values, String role, PersonPE person)
    {
        if (person != null)
        {
            put(values, "User id of " + role, person.getUserId());
            put(values, "Name of " + role, person.getLastName() + ", " + person.getFirstName());
            put(values, "Email of " + role, person.getEmail());
        }
    }

    protected abstract boolean shouldIndex(String name, Object value, Document document, LuceneOptions luceneOptions);

    @Override
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions)
    {
        if (shouldIndex(name, value, document, luceneOptions) == false)
        {
            return;
        }

        try
        {
            @SuppressWarnings("unchecked")
            T entity = (T) value;
            Map<String, IndexedValue> data = collect(entity);

            List<String> keys = new ArrayList<>();
            List<String> displayValues = new ArrayList<>();
            List<String> indexedValues = new ArrayList<>();
            for (Map.Entry<String, IndexedValue> entry : data.entrySet())
            {
                if (entry.getValue().displayValue == null)
                {
                    continue;
                }
                keys.add(entry.getKey());

                String displayVal = entry.getValue().displayValue;
                while (displayVal.indexOf(" ||| ") != -1)
                {
                    displayVal = displayVal.replace(" ||| ", " \\|\\|\\| ");
                }
                displayValues.add(displayVal);

                String indexedVal = entry.getValue().indexedValue;
                while (indexedVal.indexOf(" ||| ") != -1)
                {
                    indexedVal = indexedVal.replace(" ||| ", " \\|\\|\\| ");
                }
                indexedValues.add(indexedVal);

            }

            String indexedValue = StringUtils.joinList(indexedValues, " ||| ");
            String displayValue = StringUtils.joinList(displayValues, " ||| ");

            WhitespaceAnalyzer an = new WhitespaceAnalyzer();
            Field field;
            try
            {
                TokenStream tokenStream = an.tokenStream("global_search", new StringReader(indexedValue.toLowerCase()));
                field = new Field("global_search", displayValue, TextField.TYPE_STORED);
                field.setTokenStream(tokenStream);
            } catch (IOException e)
            {
                e.printStackTrace();
                field = new TextField("global_search", indexedValue, Store.YES);
            } finally
            {
                an.close();
            }
            document.add(field);

            field = new StoredField("global_search_fields", StringUtils.joinList(keys, " ||| "));
            document.add(field);

            List<String> metaprojects = new ArrayList<String>();
            for (MetaprojectPE m : entity.getMetaprojects())
            {
                metaprojects.add(m.getIdentifier());
            }
            indexedValue = StringUtils.joinList(metaprojects, " ");

            Field metaprojectField;
            an = new WhitespaceAnalyzer();
            try
            {
                TokenStream tokenStream = an.tokenStream("global_search_metaprojects", new StringReader(indexedValue.toLowerCase()));
                metaprojectField = new Field("global_search_metaprojects", indexedValue, TextField.TYPE_STORED);
                metaprojectField.setTokenStream(tokenStream);
                document.add(metaprojectField);
            } catch (IOException e)
            {
                e.printStackTrace();
            } finally
            {
                an.close();
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
