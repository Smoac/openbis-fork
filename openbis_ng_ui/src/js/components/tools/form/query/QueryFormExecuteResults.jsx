import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Header from '@src/js/components/common/form/Header.jsx'
import Loading from '@src/js/components/common/loading/Loading.jsx'
import GridContainer from '@src/js/components/common/grid/GridContainer.jsx'
import QueryFormExecuteResultsGrid from '@src/js/components/tools/form/query/QueryFormExecuteResultsGrid.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class QueryFormExecuteResults extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'QueryFormExecuteResults.render')

    const { results } = this.props

    if (results) {
      const { loading, loaded } = results
      return (
        <Loading loading={loading}>
          {loaded && (
            <GridContainer>
              <Header>Results</Header>
              {this.renderMessageAuthorizationColumns()}
              {this.renderGrid()}
            </GridContainer>
          )}
        </Loading>
      )
    } else {
      return null
    }
  }

  renderMessageAuthorizationColumns() {
    const { query, results, dictionaries } = this.props

    const queryDatabase = dictionaries.queryDatabases.find(
      queryDatabase => queryDatabase.name === query.databaseId.value
    )

    if (queryDatabase && !queryDatabase.space) {
      const authorizationColumns = {
        Experiment: 'experiment_key',
        Sample: 'sample_key',
        'Data Set': 'data_set_key'
      }

      const foundColumns = []
      results.tableModel.columns.forEach(column => {
        const foundColumn = authorizationColumns[column.title]
        if (foundColumn) {
          foundColumns.push(column.title + '(' + foundColumn + ')')
        }
      })

      if (foundColumns.length > 0) {
        return (
          <Message type='info'>
            Detected authorization column(s) that will be used for automatic
            results filtering: {foundColumns.join(', ')}.
          </Message>
        )
      }
    }
    return null
  }

  renderGrid() {
    const { results } = this.props

    return (
      <QueryFormExecuteResultsGrid
        key={results.timestamp}
        tableModel={results.tableModel}
      />
    )
  }
}

export default withStyles(styles)(QueryFormExecuteResults)
