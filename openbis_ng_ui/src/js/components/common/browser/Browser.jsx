import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import { DragDropContext } from 'react-beautiful-dnd'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import FilterField from '@src/js/components/common/form/FilterField.jsx'
import BrowserRoot from '@src/js/components/common/browser/BrowserRoot.jsx'
import BrowserNode from '@src/js/components/common/browser/BrowserNode.jsx'
import BrowserNodeAutoShowSelected from '@src/js/components/common/browser/BrowserNodeAutoShowSelected.jsx'
import BrowserNodeCollapseAll from '@src/js/components/common/browser/BrowserNodeCollapseAll.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  browser: {
    height: '100%',
    display: 'flex',
    flexDirection: 'column',
    borderRight: `1px solid ${theme.palette.border.primary}`
  },
  header: {},
  footer: {},
  filterButtons: {
    marginLeft: '-12px',
    marginRight: '16px',
    display: 'flex'
  },
  nodes: {
    height: '100%',
    overflow: 'auto'
  },
  visible: {
    display: 'block'
  },
  hidden: {
    display: 'none'
  }
})

class Browser extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {}
    this.controller = props.controller
    this.controller.init(new ComponentContext(this))
    this.handleDragEnd = this.handleDragEnd.bind(this)
  }

  async componentDidMount() {
    await this.controller.load()
  }

  handleDragEnd(result) {
    const { node, controller } = this.props
    controller.changeCustomSorting(
      result.destination.droppableId,
      result.source.index,
      result.destination.index
    )
  }

  render() {
    logger.log(logger.DEBUG, 'Browser.render')

    const { controller } = this
    const { renderHeader, renderFooter, classes } = this.props

    if (!controller.isLoaded()) {
      return (
        <div className={classes.browser}>
          <FilterField filter={controller.getFilter() || ''} loading={true} />
          <BrowserRoot rootNode={controller.getNodeSetAsRoot()} />
        </div>
      )
    }

    const fullTree = controller.getFullTree()
    const filteredTree = controller.getFilteredTree()

    return (
      <div className={classes.browser}>
        {renderHeader && <div className={classes.header}>{renderHeader()}</div>}
        {this.renderFilter()}
        <BrowserRoot
          rootNode={controller.getNodeSetAsRoot()}
          onRootChange={node => {
            controller.setNodeAsRoot(node)
          }}
          onRootClear={() => {
            controller.setNodeAsRoot(null)
          }}
        />
        <div className={classes.nodes}>
          {fullTree && (
            <div
              className={
                !controller.isLoading() && controller.isFullTreeVisible()
                  ? classes.visible
                  : classes.hidden
              }
            >
              <DragDropContext onDragEnd={this.handleDragEnd}>
                <BrowserNode
                  controller={controller}
                  node={fullTree}
                  level={-1}
                />
              </DragDropContext>
            </div>
          )}
          {filteredTree && (
            <div
              className={
                !controller.isLoading() && controller.isFilteredTreeVisible()
                  ? classes.visible
                  : classes.hidden
              }
            >
              <DragDropContext onDragEnd={this.handleDragEnd}>
                <BrowserNode
                  controller={controller}
                  node={filteredTree}
                  level={-1}
                />
              </DragDropContext>
            </div>
          )}
        </div>
        {renderFooter && <div className={classes.footer}>{renderFooter()}</div>}
      </div>
    )
  }

  renderFilter() {
    const { controller } = this
    const { classes } = this.props

    return (
      <FilterField
        filter={controller.getFilter() || ''}
        filterChange={controller.filterChange}
        filterClear={controller.filterClear}
        loading={controller.isLoading() || controller.isTreeLoading()}
        endAdornments={
          <div className={classes.filterButtons}>
            <BrowserNodeAutoShowSelected
              value={controller.isAutoShowSelectedObject()}
              onClick={controller.changeAutoShowSelectedObject}
            />
            <BrowserNodeCollapseAll
              node={
                controller.getNodeSetAsRoot() ||
                controller.getRoot() || { canHaveChildren: true }
              }
              onClick={controller.collapseAllNodes}
            />
          </div>
        }
      />
    )
  }
}

export default withStyles(styles)(Browser)
