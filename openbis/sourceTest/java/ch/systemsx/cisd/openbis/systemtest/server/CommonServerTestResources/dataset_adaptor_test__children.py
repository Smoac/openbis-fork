def validate(dataset, isNew):
    try:
        assertEntities(dataset.children(), ["20081105092259900-0", "20081105092259900-1"], 'children')
    except Exception, ex:
        return ex.args[0]
