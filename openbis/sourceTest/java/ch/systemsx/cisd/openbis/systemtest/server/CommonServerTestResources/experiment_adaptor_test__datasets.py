def validate(experiment, isNew):
    try:
        assertEntitiesCount(experiment.dataSets(), 18, 'data sets')
        assertEntities(experiment.dataSetsOfType('CONTAINER_TYPE'), ['CONTAINER_1', 'CONTAINER_2', 'ROOT_CONTAINER', '20081105092259000-19', '20110509092359990-10'], 'data sets of type CONTAINER_TYPE')
    except Exception, ex:
        return ex.args[0]
