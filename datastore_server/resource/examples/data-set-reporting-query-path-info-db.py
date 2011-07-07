DATA_SOURCE = "path-info-db"
QUERY = """	
					SELECT ds.code as "DATA_SET_CODE", dsf.*
					FROM data_sets ds, data_set_files dsf
					WHERE ds.code = ? AND dsf.dase_id = ds.id
				"""
				
"""reporting table column names"""
DATA_SET_CODE = "Data Set"
RELATIVE_PATH = "Relative Path"
FILE_NAME = "File Name"
SIZE_IN_BYTES = "Size"
IS_DIRECTORY = "Is Directory?"
LAST_MODIFIED = "Last Modified"

def describe(dataSets, tableBuilder):
    
    tableBuilder.addHeader(DATA_SET_CODE)
    tableBuilder.addHeader(RELATIVE_PATH)
    tableBuilder.addHeader(FILE_NAME)
    tableBuilder.addHeader(SIZE_IN_BYTES)
    tableBuilder.addHeader(IS_DIRECTORY)
    tableBuilder.addHeader(LAST_MODIFIED)
    
    for dataSet in dataSets:
        results = query_service.select(DATA_SOURCE, QUERY, dataSet.getDataSetCode()) 
        for r in results:
            row = tableBuilder.addRow()
            row.setCell(DATA_SET_CODE, r.get("DATA_SET_CODE"))
            row.setCell(RELATIVE_PATH, r.get("RELATIVE_PATH"))
            row.setCell(FILE_NAME, r.get("FILE_NAME"))
            row.setCell(SIZE_IN_BYTES, r.get("SIZE_IN_BYTES"))
            row.setCell(IS_DIRECTORY, r.get("IS_DIRECTORY"))
            row.setCell(LAST_MODIFIED, r.get("LAST_MODIFIED"))
        results.close()