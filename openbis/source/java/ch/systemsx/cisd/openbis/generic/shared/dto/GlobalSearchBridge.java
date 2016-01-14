package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

public abstract class GlobalSearchBridge<T> implements FieldBridge
{

    protected static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");

    protected abstract Map<String, String> collect(T entity);

    protected void addSpace(Map<String, String> values, SpacePE space)
    {
        values.put("Space code", space.getCode());
    }

    protected void addProject(Map<String, String> values, ProjectPE project)
    {
        values.put("Project code", project.getCode());
        addSpace(values, project.getSpace());
    }

    protected void addExperiment(Map<String, String> values, ExperimentPE experiment)
    {
        values.put("Experiment code", experiment.getCode());
        addProject(values, experiment.getProject());
    }

    protected <U extends EntityPropertyPE> void addProperties(Map<String, String> values, Collection<U> properties)
    {
        if (properties != null)
        {
            for (U property : properties)
            {
                values.put("Property '" + property.getEntityTypePropertyType().getPropertyType().getLabel() + "'", property.tryGetUntypedValue());
            }
        }
    }

    protected void addAttachments(Map<String, String> values, Collection<AttachmentPE> attachments)
    {
        if (attachments != null)
        {
            for (AttachmentPE attachment : attachments)
            {
                values.put("Title of '" + attachment.getFileName() + "'", attachment.getTitle());
                values.put("Description of '" + attachment.getFileName() + "'", attachment.getDescription());
                values.put("File name of '" + attachment.getFileName() + "'", attachment.getFileName());
            }
        }
    }

    protected void addPerson(Map<String, String> values, String role, PersonPE person)
    {
        if (person != null)
        {
            values.put("User id of " + role, person.getUserId());
            values.put("First name of " + role, person.getFirstName());
            values.put("Last name of " + role, person.getLastName());
            values.put("Email of " + role, person.getEmail());
        }

    }

    @Override
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions)
    {
        try
        {
            @SuppressWarnings("unchecked")
            T entity = (T) value;
            Map<String, String> data = collect(entity);

            List<String> keys = new ArrayList<>();
            List<String> values = new ArrayList<>();
            for (Map.Entry<String, String> entry : data.entrySet())
            {
                keys.add(entry.getKey());
                values.add(entry.getValue());
            }

            String indexedValue = StringUtils.joinList(values, " ||| ");

            WhitespaceAnalyzer an = new WhitespaceAnalyzer();
            Field field;
            try
            {
                TokenStream tokenStream = an.tokenStream("global_search", new StringReader(indexedValue.toLowerCase()));
                field = new Field("global_search", indexedValue, TextField.TYPE_STORED);
                field.setTokenStream(tokenStream);
            } catch (IOException e)
            {
                e.printStackTrace(); // WHAT SHOULD BE DONE HERE?!
                field = new TextField("global_search", indexedValue, Store.YES);
            } finally
            {
                an.close();
            }
            document.add(field);

            field = new StoredField("global_search_fields", StringUtils.joinList(keys, " ||| "));
            document.add(field);

        } catch (Exception e)
        {
            e.printStackTrace(); // WHAT SHOULD BE DONE HERE?!
        }
    }
}
