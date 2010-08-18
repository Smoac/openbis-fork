DB=openbis_screening_mydb
TEMPLATE_USER=openbis-user-name

psql -U postgres -d $DB -c "update persons set display_settings = (select display_settings from persons where user_id = '$TEMPLATE_USER') where user_id != '$TEMPLATE_USER'"
