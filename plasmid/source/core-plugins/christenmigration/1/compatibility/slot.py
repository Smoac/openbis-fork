import re

def calculate_slot(): 

	location=(entity.propertyValue('PLASMID_ID')/81)+0.499999
	
	if (location > 1.1):
		slot = entity.propertyValue('PLASMID_ID') - ((location)*81)
	else:
		slot = entity.propertyValue('PLASMID_ID')

	return slot
     
def calculate():
	return calculate_slot()
