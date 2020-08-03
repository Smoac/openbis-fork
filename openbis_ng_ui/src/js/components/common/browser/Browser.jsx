import _ from 'lodash'
import React from 'react'
import { connect } from 'react-redux'
import { Resizable } from 're-resizable'
import { withStyles } from '@material-ui/core/styles'
import FilterField from '@src/js/components/common/form/FilterField.jsx'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import selectors from '@src/js/store/selectors/selectors.js'
import logger from '@src/js/common/logger.js'

import BrowserNodes from './BrowserNodes.jsx'
import BrowserButtons from './BrowserButtons.jsx'
import BrowserDialogRemoveNode from './BrowserDialogRemoveNode.jsx'

const styles = theme => ({
  resizable: {
    zIndex: 200,
    position: 'relative'
  },
  paper: {
    height: '100%',
    display: 'flex',
    flexDirection: 'column',
    borderRight: `1px solid ${theme.palette.border.primary}`
  },
  nodes: {
    height: '100%',
    overflow: 'auto'
  }
})

function mapStateToProps() {
  return (state, ownProps) => {
    return {
      selectedObject: selectors.getSelectedObject(
        state,
        ownProps.controller.getPage()
      ),
      lastObjectModifications: selectors.getLastObjectModifications(state)
    }
  }
}

class Browser extends React.PureComponent {
  constructor(props) {
    super(props)

    this.state = {}

    this.controller = props.controller
    this.controller.init(new ComponentContext(this))
  }

  componentDidMount() {
    this.controller.load()
  }

  componentDidUpdate(prevProps) {
    if (this.props.selectedObject !== prevProps.selectedObject) {
      this.controller.objectSelect(this.props.selectedObject)
    }
    if (
      this.props.lastObjectModifications !== prevProps.lastObjectModifications
    ) {
      this.controller.refresh(this.props.lastObjectModifications)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'Browser.render')

    const { controller } = this

    if (!controller.getLoaded()) {
      return null
    }

    const { classes } = this.props

    return (
      <Resizable
        defaultSize={{
          width: 300,
          height: 'auto'
        }}
        enable={{
          right: true,
          left: false,
          top: false,
          bottom: false,
          topRight: false,
          bottomRight: false,
          bottomLeft: false,
          topLeft: false
        }}
        className={classes.resizable}
      >
        <div className={classes.paper}>
          <FilterField
            filter={controller.getFilter()}
            filterChange={controller.filterChange}
          />
          <div className={classes.nodes}>
            <BrowserNodes
              controller={controller}
              nodes={controller.getNodes()}
              level={0}
            />
          </div>
          <BrowserButtons
            controller={controller}
            addEnabled={controller.isAddNodeEnabled()}
            removeEnabled={controller.isRemoveNodeEnabled()}
          />
          <BrowserDialogRemoveNode
            open={controller.isRemoveNodeDialogOpen()}
            node={controller.getSelectedNode()}
            onConfirm={controller.nodeRemoveConfirm}
            onCancel={controller.nodeRemoveCancel}
          />
        </div>
      </Resizable>
    )
  }
}

export default _.flow(connect(mapStateToProps), withStyles(styles))(Browser)
