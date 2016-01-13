package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hibernate.SQLQuery;
import org.hibernate.SharedSessionContract;
import org.hibernate.transform.ResultTransformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import ch.systemsx.cisd.common.collection.SimpleComparator;
import ch.systemsx.cisd.common.time.DateFormatThreadLocal;

public class EntityHistoryCreator
{
    private static final Set<String> NON_ATTRIBUTE_COLUMNS = new HashSet<>(Arrays.asList("ID", "PERM_ID"));
    
    private boolean enabled = false;

    public void setEnabled(String s)
    {
        if (s != null && s.length() > 0 && s.equalsIgnoreCase("true"))
        {
            enabled = true;
        } else
        {
            enabled = false;
        }
    }

    public String apply(SharedSessionContract session, List<Long> entityIdsToDelete,
            String propertyHistoryQuery, String relationshipHistoryQuery, String attributesQuery)
    {
        if (!enabled)
        {
            return "";
        }
        Map<String, List<? extends EntityModification>> histories = new HashMap<String, List<? extends EntityModification>>();

        if (attributesQuery != null)
        {
            List<AttributeEntry> attributeEntries =
                    selectAttributeEntries(session.createSQLQuery(attributesQuery), entityIdsToDelete);
            addToHistories(histories, attributeEntries);
        }
        
        List<PropertyHistoryEntry> propertyHistory =
                selectHistoryPropertyEntries(session.createSQLQuery(propertyHistoryQuery), entityIdsToDelete);
        addToHistories(histories, propertyHistory);

        if (relationshipHistoryQuery != null)
        {
            List<RelationshipHistoryEntry> relationshipHistory =
                    selectRelationshipHistoryEntries(session.createSQLQuery(relationshipHistoryQuery),
                            entityIdsToDelete);
            addToHistories(histories, relationshipHistory);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        ObjectWriter ow = new ObjectMapper().setDateFormat(dateFormat).writer().withDefaultPrettyPrinter();
        String content;
        try
        {
            content = ow.writeValueAsString(histories);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        return content;
    }

    private List<AttributeEntry> selectAttributeEntries(SQLQuery sqlQuery, List<Long> entityIdsToDelete)
    {
        List<Map<String, Object>> rows = getRows(sqlQuery, entityIdsToDelete);
        
        Map<Long, EntityAttributes> result = new HashMap<>();
        for (Map<String, Object> row : rows)
        {
            Long id = ((BigInteger) row.get("ID")).longValue();
            String permId = (String) row.get("PERM_ID");
            EntityAttributes attributes = new EntityAttributes(permId);
            for (Entry<String, Object> entry : row.entrySet())
            {
                String key = entry.getKey();
                if (NON_ATTRIBUTE_COLUMNS.contains(key))
                {
                    continue;
                }
                attributes.addAttribute(key, render(entry.getValue()));
                
            }
            result.put(id, attributes);
        }
        
        List<AttributeEntry> list = new ArrayList<>();
        for (EntityAttributes entityAttributes : result.values())
        {
            Map<String, String> map = entityAttributes.getAttributes();
            for (Entry<String, String> entry : map.entrySet())
            {
                AttributeEntry historyEntry = new AttributeEntry();
                historyEntry.permId = entityAttributes.getPermId();
                historyEntry.attributeName = entry.getKey();
                historyEntry.value = entry.getValue();
                list.add(historyEntry);
            }
        }
        return list;
    }


    private String render(Object value)
    {
        if (value instanceof Date)
        {
            return DateFormatThreadLocal.DATE_FORMAT.get().format((Date) value);
        }
        return value == null ? null : value.toString();
    }

    private void addToHistories(Map<String, List<? extends EntityModification>> histories, 
            List<? extends EntityModification> modifications)
    {
        for (EntityModification entry : modifications)
        {
            String permId = entry.permId;
            List<? extends EntityModification> current = histories.get(permId);
            if (current == null)
            {
                current = new ArrayList<>();
            }
            histories.put(permId, addModification(current, entry));
        }
    }

    private List<EntityModification> addModification(List<? extends EntityModification> current, EntityModification modification)
    {
        List<EntityModification> list = new ArrayList<>(current);
        list.add(modification);
        Collections.sort(list, new SimpleComparator<EntityModification, Date>()
            {
                @Override
                public Date evaluate(EntityModification item)
                {
                    return item.validFrom == null ? new Date(0) : item.validFrom;
                }
            });
        return Collections.unmodifiableList(list);
    }
    
    private List<PropertyHistoryEntry> selectHistoryPropertyEntries(
            final SQLQuery selectPropertyHistory, final List<Long> entityIds)
    {
        selectPropertyHistory.setParameterList("entityIds", entityIds);
        selectPropertyHistory.setResultTransformer(new ResultTransformer()
            {
                private static final long serialVersionUID = 1L;

                @Override
                public Object transformTuple(Object[] values, String[] aliases)
                {
                    PropertyHistoryEntry entry = new PropertyHistoryEntry();
                    int i = 0;
                    entry.permId = (String) values[i++];
                    entry.propertyCode = (String) values[i++];
                    entry.value = (String) values[i++];
                    entry.userId = (String) values[i++];
                    entry.validFrom = (Date) values[i++];
                    entry.validUntil = (Date) values[i++];
                    return entry;
                }

                @SuppressWarnings("rawtypes")
                @Override
                public List transformList(List list)
                {
                    return list;
                }
            });
        return cast(selectPropertyHistory.list());
    }

    private List<RelationshipHistoryEntry> selectRelationshipHistoryEntries(final SQLQuery selectRelationshipHistory,
            final List<Long> entityIds)
    {
        selectRelationshipHistory.setParameterList("entityIds", entityIds);
        selectRelationshipHistory.setResultTransformer(new ResultTransformer()
            {
                private static final long serialVersionUID = 1L;

                @Override
                public Object transformTuple(Object[] values, String[] aliases)
                {
                    RelationshipHistoryEntry entry = new RelationshipHistoryEntry();
                    int i = 0;
                    entry.permId = (String) values[i++];
                    entry.relationType = (String) values[i++];
                    entry.relatedEntity = (String) values[i++];
                    entry.entityType = (String) values[i++];
                    entry.userId = (String) values[i++];
                    entry.validFrom = (Date) values[i++];
                    entry.validUntil = (Date) values[i++];
                    return entry;
                }

                @SuppressWarnings("rawtypes")
                @Override
                public List transformList(List list)
                {
                    return list;
                }
            });
        return cast(selectRelationshipHistory.list());
    }

    private List<Map<String, Object>> getRows(SQLQuery sqlQuery, List<Long> entityIdsToDelete)
    {
        sqlQuery.setParameterList("entityIds", entityIdsToDelete);
        sqlQuery.setResultTransformer(new ResultTransformer()
            {
                private static final long serialVersionUID = 1L;
                @Override
                public Object transformTuple(Object[] values, String[] aliases)
                {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 0; i < values.length; i++)
                    {
                        row.put(aliases[i].toUpperCase(), values[i]);
                    }
                    return row;
                }

                @SuppressWarnings("rawtypes")
                @Override
                public List transformList(List list)
                {
                    return list;
                }
            });
        return cast(sqlQuery.list());
    }
    
    @SuppressWarnings("unchecked")
    protected final <T> List<T> cast(final List<?> list)
    {
        return (List<T>) list;
    }

    private static class EntityAttributes
    {
        private final String permId;
        private final Map<String, String> attributes = new HashMap<String, String>();

        public EntityAttributes(String permId)
        {
            this.permId = permId;
        }
        
        public void addAttribute(String attributeName, String value)
        {
            attributes.put(attributeName, value);
        }

        public String getPermId()
        {
            return permId;
        }

        public Map<String, String> getAttributes()
        {
            return attributes;
        }
        
    }
}
