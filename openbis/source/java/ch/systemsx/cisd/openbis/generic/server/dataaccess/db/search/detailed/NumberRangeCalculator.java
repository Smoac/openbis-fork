package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.detailed;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CompareType;

import org.apache.lucene.search.NumericRangeQuery;

public class NumberRangeCalculator
{
    public static boolean isInteger(String string) {
        try {
            return Long.valueOf(string) != null;
        } catch(Exception ex) {
            return false;
        }
    }
    
    public static boolean isReal(String string) {
        try {
            return Double.valueOf(string) != null;
        } catch(Exception ex) {
            return false;
        }
    }
    
    public static NumericRangeQuery getRangeQuery(CompareType type, String fieldName, String fieldValue) {
        NumericRangeQuery luceneQuery = null;
        if(type != null && isReal(fieldValue) && !isInteger(fieldValue))
        {
            switch(type) {
                case LESS_THAN:
                    luceneQuery = NumericRangeQuery.newDoubleRange(fieldName, null, Double.parseDouble(fieldValue), false, false);
                    break;
                case LESS_THAN_OR_EQUAL:
                    luceneQuery = NumericRangeQuery.newDoubleRange(fieldName, null, Double.parseDouble(fieldValue), false, true);
                    break;
                case EQUALS:
                    luceneQuery = NumericRangeQuery.newDoubleRange(fieldName, Double.parseDouble(fieldValue), Double.parseDouble(fieldValue), true, true);
                    break;
                case MORE_THAN_OR_EQUAL:
                    luceneQuery = NumericRangeQuery.newDoubleRange(fieldName, Double.parseDouble(fieldValue), null, true, false);
                    break;
                case MORE_THAN:
                    luceneQuery = NumericRangeQuery.newDoubleRange(fieldName, Double.parseDouble(fieldValue), null, false, false);
                    break;
            }
        } else if(type != null && isInteger(fieldValue))
        {
            switch(type) {
                case LESS_THAN:
                    luceneQuery = NumericRangeQuery.newLongRange(fieldName, null, Long.parseLong(fieldValue), false, false);
                    break;
                case LESS_THAN_OR_EQUAL:
                    luceneQuery = NumericRangeQuery.newLongRange(fieldName, null, Long.parseLong(fieldValue), false, true);
                    break;
                case EQUALS:
                    luceneQuery = NumericRangeQuery.newLongRange(fieldName, Long.parseLong(fieldValue), Long.parseLong(fieldValue), true, true);
                    break;
                case MORE_THAN_OR_EQUAL:
                    luceneQuery = NumericRangeQuery.newLongRange(fieldName, Long.parseLong(fieldValue), null, true, false);
                    break;
                case MORE_THAN:
                    luceneQuery = NumericRangeQuery.newLongRange(fieldName, Long.parseLong(fieldValue), null, false, false);
                    break;
            }
        }
        return luceneQuery;
    }
    
}
