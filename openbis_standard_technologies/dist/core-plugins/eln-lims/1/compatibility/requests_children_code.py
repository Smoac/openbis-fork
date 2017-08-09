def retrieve_childrenCode(): 

    sample= entity.samplePE()
    children=sample.children
    if len(children) != 0: 
        children_code = children[0].getCode()
    else:
        children_code="n.a."
        

    return children_code
     
def calculate():
    return retrieve_childrenCode()
