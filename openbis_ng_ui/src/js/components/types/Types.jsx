import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Content from '@src/js/components/common/content/Content.jsx'
import ContentTab from '@src/js/components/common/content/ContentTab.jsx'
import TypeBrowser from '@src/js/components/types/browser/TypeBrowser.jsx'
import TypeSearch from '@src/js/components/types/search/TypeSearch.jsx'
import TypeForm from '@src/js/components/types/form/TypeForm.jsx'
import VocabularyForm from '@src/js/components/types/form/VocabularyForm.jsx'
import pages from '@src/js/common/consts/pages.js'
import objectType from '@src/js/common/consts/objectType.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = () => ({
  container: {
    display: 'flex',
    width: '100%'
  }
})

class Types extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'Types.render')

    const classes = this.props.classes

    return (
      <div className={classes.container}>
        <TypeBrowser />
        <Content
          page={pages.TYPES}
          renderComponent={this.renderComponent}
          renderTab={this.renderTab}
        />
      </div>
    )
  }

  renderComponent(tab) {
    const { object } = tab
    if (object.type === objectType.SEARCH) {
      return <TypeSearch searchText={object.id} />
    } else if (object.type === objectType.OVERVIEW) {
      return <TypeSearch objectType={object.id} />
    } else if (
      object.type === objectType.VOCABULARY_TYPE ||
      object.type === objectType.NEW_VOCABULARY_TYPE
    ) {
      return <VocabularyForm object={object} />
    } else {
      return <TypeForm object={object} />
    }
  }

  renderTab(tab) {
    const { object, changed } = tab

    let label = null

    if (object.type === objectType.OVERVIEW) {
      const labels = {
        [objectType.OBJECT_TYPE]: messages.get(messages.OBJECT_TYPES),
        [objectType.COLLECTION_TYPE]: messages.get(messages.COLLECTION_TYPES),
        [objectType.DATA_SET_TYPE]: messages.get(messages.DATA_SET_TYPES),
        [objectType.MATERIAL_TYPE]: messages.get(messages.MATERIAL_TYPES),
        [objectType.VOCABULARY_TYPE]: messages.get(messages.VOCABULARY_TYPES)
      }
      label = labels[object.id]
    } else {
      const prefixes = {
        [objectType.NEW_OBJECT_TYPE]:
          messages.get(messages.NEW_OBJECT_TYPE) + ' ',
        [objectType.NEW_COLLECTION_TYPE]:
          messages.get(messages.NEW_COLLECTION_TYPE) + ' ',
        [objectType.NEW_DATA_SET_TYPE]:
          messages.get(messages.NEW_DATA_SET_TYPE) + ' ',
        [objectType.NEW_MATERIAL_TYPE]:
          messages.get(messages.NEW_MATERIAL_TYPE) + ' ',
        [objectType.NEW_VOCABULARY_TYPE]:
          messages.get(messages.NEW_VOCABULARY_TYPE) + ' ',
        [objectType.OBJECT_TYPE]: messages.get(messages.OBJECT_TYPE) + ': ',
        [objectType.COLLECTION_TYPE]:
          messages.get(messages.COLLECTION_TYPE) + ': ',
        [objectType.DATA_SET_TYPE]: messages.get(messages.DATA_SET_TYPE) + ': ',
        [objectType.MATERIAL_TYPE]: messages.get(messages.MATERIAL_TYPE) + ': ',
        [objectType.VOCABULARY_TYPE]:
          messages.get(messages.VOCABULARY_TYPE) + ': ',
        [objectType.SEARCH]: messages.get(messages.SEARCH) + ': '
      }
      label = prefixes[object.type] + object.id
    }

    return <ContentTab label={label} changed={changed} />
  }
}

export default withStyles(styles)(Types)
