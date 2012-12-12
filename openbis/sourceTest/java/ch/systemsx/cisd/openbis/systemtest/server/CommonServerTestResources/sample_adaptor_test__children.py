def validate(sample, isNew):
    try:
        assertEntities(sample.children(), ['CP1-A1','CP1-A2'], 'children')
    except Exception, ex:
        return ex.args[0]
