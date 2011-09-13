import sys

def did_encounter_secondary_transaction_errors(service, transaction, errors):
    global didSecondaryTransactionErrorNotificationHappen
    didSecondaryTransactionErrorNotificationHappen = True

# Execute a query
tr = service.transaction()
query = tr.getDatabaseQuery("path-info-db")
result = query.select("SELECT * from data_set_files WHERE parent_id is NULL", None)
sys.stdout.write("Roots: " + str(result.size()) + "\n")