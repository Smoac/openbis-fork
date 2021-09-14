import React from 'react'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import VocabularyLink from '@src/js/components/common/link/VocabularyLink.jsx'
import ids from '@src/js/common/consts/ids.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

class VocabulariesGrid extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'VocabulariesGrid.render')

    const { id, rows, selectedRowId, onSelectedRowChange, controllerRef } =
      this.props

    return (
      <Grid
        settingsId={{
          webAppId: ids.WEB_APP_ID,
          gridId: id
        }}
        controllerRef={controllerRef}
        header={messages.get(messages.VOCABULARY_TYPES)}
        columns={[
          {
            name: 'code',
            label: messages.get(messages.CODE),
            sort: 'asc',
            getValue: ({ row }) => row.code,
            renderValue: ({ row }) => {
              return <VocabularyLink vocabularyCode={row.code} />
            }
          },
          {
            name: 'description',
            label: messages.get(messages.DESCRIPTION),
            getValue: ({ row }) => row.description,
            renderValue: ({ value, classes }) => (
              <span className={classes.wrap}>{value}</span>
            )
          },
          {
            name: 'urlTemplate',
            label: messages.get(messages.URL_TEMPLATE),
            getValue: ({ row }) => row.urlTemplate
          }
        ]}
        rows={rows}
        selectedRowId={selectedRowId}
        onSelectedRowChange={onSelectedRowChange}
      />
    )
  }
}

export default VocabulariesGrid
