-- Migration from 059 to 060

------------------------------------------------------------------------------------
--  Drop trigger on queries.entity_type_code column to enable storing a regexp there.
------------------------------------------------------------------------------------

DROP TRIGGER query_entity_type_code_check ON queries;
DROP FUNCTION query_entity_type_code_check();

