def calculateValue():
    mat= entity.propertyValue('MAT')+":" 
    
    result1=entity.propertyValue('DISRUPTIONS_1')+"::" +entity.propertyValue('MARKERS_1')+" " +entity.propertyValue('UNMARKED_MUTATIONS_1') +" " 
        
    result2=entity.propertyValue('DISRUPTIONS_2')+"::" +entity.propertyValue('MARKERS_2')+" " +entity.propertyValue('UNMARKED_MUTATIONS_2') +" " 
        
    result3=entity.propertyValue('DISRUPTIONS_3')+"::" +entity.propertyValue('MARKERS_3')+" " +entity.propertyValue('UNMARKED_MUTATIONS_3') +" " 
    
    result4=entity.propertyValue('DISRUPTIONS_4')+"::" +entity.propertyValue('MARKERS_4')+" " +entity.propertyValue('UNMARKED_MUTATIONS_4') +" " 
    
    result5=entity.propertyValue('DISRUPTIONS_5')+"::" +entity.propertyValue('MARKERS_5')+" " +entity.propertyValue('UNMARKED_MUTATIONS_5') +" " 
    
    
    result_tot= mat+result1+result2+result3+result4+result5
    return result_tot

def calculate():
    return calculateValue()
