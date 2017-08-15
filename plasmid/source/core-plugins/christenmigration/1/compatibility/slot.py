import re

def calculate_slot(): 

	plasmid_ID= float(entity.propertyValue('PLASMID_ID'))
	location=(plasmid_ID/81)+0.499999
	round_location=int (round(location))
	

	
	if (location > 1.1):
		new_location=(location-1)*81
		slot = plasmid_ID-new_location
	else:
		slot = int(plasmid_ID)

	return slot
     
def calculate():
	return calculate_slot()


