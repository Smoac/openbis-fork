"""An aggregation service that aggregates data for the sample-bottom-up display"""

from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

def aggregate(parameters, table_builder):
	table_builder.addHeader("TEST")
	row = table_builder.addRow()
	row.setCell("TEST", "TEST")
