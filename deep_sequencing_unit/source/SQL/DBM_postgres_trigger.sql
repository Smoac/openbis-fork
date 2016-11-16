CREATE OR REPLACE FUNCTION set_dbm_relations() RETURNS trigger AS $dbm_relations$
DECLARE
    groups RECORD;
    next_ag_id tech_id;
    next_ra_id tech_id;
    space_id tech_id;
    robert_user_id int := 240;
    florian_user_id int := 404;
    role authorization_role := 'OBSERVER';
    dbm_string varchar:= 'DBM%';
BEGIN

    FOR groups IN SELECT code FROM spaces WHERE code LIKE dbm_string EXCEPT ALL
                  SELECT code FROM authorization_groups WHERE code LIKE dbm_string LOOP
        SELECT nextval('authorization_group_id_seq') INTO next_ag_id;
        INSERT INTO authorization_groups (id, code, pers_id_registerer) VALUES (next_ag_id, groups.code, 1);
        INSERT INTO authorization_group_persons (ag_id, pers_id) VALUES (next_ag_id, robert_user_id);
        INSERT INTO authorization_group_persons (ag_id, pers_id) VALUES (next_ag_id, florian_user_id);

        SELECT id FROM spaces WHERE code=groups.code INTO space_id;
        SELECT nextval('role_assignment_id_seq') INTO next_ra_id;
        INSERT INTO role_assignments (id, role_code, space_id, pers_id_registerer, ag_id_grantee) VALUES (next_ra_id, role, space_id, 1, next_ag_id);
    END LOOP;
    RETURN NULL;
END;
$dbm_relations$ LANGUAGE plpgsql;

-- DROP TRIGGER IF EXISTS dbm_relations ON spaces;

CREATE TRIGGER dbm_relations AFTER INSERT ON spaces
FOR EACH ROW EXECUTE PROCEDURE set_dbm_relations();