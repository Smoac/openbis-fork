#! /bin/bash
# Find out several things of the openbis installation on all servers
# This is a pattern that can editet for several (SQL) queries on several
# openbis servers, was initially just for querying memory
 
SERVERS=" openbis@sprint-openbis.ethz.ch
          openbis@cisd-openbis.ethz.ch
          openbis@imsb-us-openbis.ethz.ch
          openbis@openbis-phosphonetx.ethz.ch
          openbis@openbis-liverx.ethz.ch
          openbis@agronomics.ethz.ch
          sbsuser@openbis-dsu.ethz.ch
          openbis@openbis-scu.ethz.ch
          openbis@openbis-test.ethz.ch
          openbis@basysbio.ethz.ch
          openbis@bs-plasmids.ethz.ch"

SQL="WITH RECURSIVE data_set_parents(id, parent_ids, cycle) AS (
        SELECT  r.data_id_child AS data_id, 
                ARRAY[CAST (r.data_id_parent AS bigint)] AS parent_ids, 
                false AS cycle 
        FROM data_set_relationships r
    UNION ALL
        SELECT  r.data_id_child,
                CAST (r.data_id_parent AS bigint) || p.parent_ids,
                r.data_id_child = ANY(p.parent_ids)
        FROM data_set_relationships r, data_set_parents p
        WHERE r.data_id_parent = p.id AND NOT cycle
)
SELECT count(*) AS cycles FROM data_set_parents WHERE cycle = true;"

SQL_QUERY="psql -c \"$SQL\" openbis_productive"
JAVA_MAX_HEAP_SIZE="echo -n JAVA_MAX_HEAP_SIZE: ;cat ~openbis/sprint/openBIS-server/jetty/bin/openbis.conf | grep JAVA_MEM_OPTS | cut -d \- -f 2"
JAVA_INIT_HEAP_SIZE="echo -n JAVA_INIT_HEAP_SIZE: ;cat ~openbis/sprint/openBIS-server/jetty/bin/openbis.conf | grep JAVA_MEM_OPTS | cut -d \- -f 3"
JAVA_VERSION="java -version"
PSQL_VERSION="psql --version | grep PostgreSQL"
OPENBIS_VERSION="ls -1d ~openbis/sprint-* | cut -d - -f2"

#STATEMENTS="$JAVA_MAX_HEAP_SIZE; $JAVA_INIT_HEAP_SIZE; $PSQL_VERSION; $JAVA_VERSION"
STATEMENTS="$OPENBIS_VERSION"

if [ -n "${1}" ]
then
  STATEMENTS="$STATEMENTS;$1" 
else STATEMENTS="$STATEMENTS;echo"
fi

echo Statistics for all openbis servers
echo ====================================
for j in $SERVERS; 
  do
    echo $j:  
    ssh $j $STATEMENTS
    echo -e "\n"
done
