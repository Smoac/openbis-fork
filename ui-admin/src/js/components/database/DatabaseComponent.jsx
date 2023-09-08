import React from 'react'
import Container from '@src/js/components/common/form/Container.jsx'
import AppController from '@src/js/components/AppController.js'
import DataBrowser from '@src/js/components/database/data-browser/DataBrowser.jsx'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'
import logger from '@src/js/common/logger.js'

class DatabaseComponent extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {
      json: null,
      showDataBrowser: false
    }
  }

  async componentDidMount() {
    try {
      const { object } = this.props

      let json = null
      let showDataBrowser = false
      if (object.type === objectType.SPACE) {
        const spaces = await openbis.getSpaces(
          [new openbis.SpacePermId(object.id)],
          new openbis.SpaceFetchOptions()
        )
        json = spaces[object.id]
      } else if (object.type === objectType.PROJECT) {
        const projects = await openbis.getProjects(
          [new openbis.ProjectPermId(object.id)],
          new openbis.ProjectFetchOptions()
        )
        json = projects[object.id]
      } else if (object.type === objectType.COLLECTION) {
        const experiments = await openbis.getExperiments(
          [new openbis.ExperimentPermId(object.id)],
          new openbis.ExperimentFetchOptions()
        )
        json = experiments[object.id]
        showDataBrowser = true
      } else if (object.type === objectType.OBJECT) {
        const fetchOptions = new openbis.SampleFetchOptions()
        fetchOptions.withSpace()
        fetchOptions.withProject()
        fetchOptions.withExperiment()
        fetchOptions.withParents()
        const samples = await openbis.getSamples(
          [new openbis.SamplePermId(object.id)],
          fetchOptions
        )
        json = samples[object.id]
        showDataBrowser = true
      } else if (object.type === objectType.DATA_SET) {
        const fetchOptions = new openbis.DataSetFetchOptions()
        fetchOptions.withExperiment()
        fetchOptions.withSample()
        fetchOptions.withParents()
        const dataSets = await openbis.getDataSets(
          [new openbis.DataSetPermId(object.id)],
          fetchOptions
        )
        json = dataSets[object.id]
      }

      this.setState({
        json,
        showDataBrowser
      })
    } catch (error) {
      AppController.getInstance().errorChange(error)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'DatabaseComponent.render')
    const { object } = this.props

    return this.state.showDataBrowser ? (
      <DataBrowser
        id={object.id}
        viewType='list'
        sessionToken={AppController.getInstance().getSessionToken()}
      />
    ) : (
      <Container>
        <pre>{JSON.stringify(this.state.json || {}, null, 2)}</pre>
      </Container>
    )
  }
}

export default DatabaseComponent
