import re

def calculate_maxID(): 

	max_plasmid_id=max(entity.propertyValue('PLASMID_ID'))

	return max_plasmid_id
     
def calculate():
	return calculate_maxID()
