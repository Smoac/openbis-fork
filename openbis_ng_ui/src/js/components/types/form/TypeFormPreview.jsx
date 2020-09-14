import _ from 'lodash'
import React from 'react'
import { DragDropContext, Droppable } from 'react-beautiful-dnd'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import TypeFormSelectionType from '@src/js/components/types/form/TypeFormSelectionType.js'
import TypeFormPreviewHeader from '@src/js/components/types/form/TypeFormPreviewHeader.jsx'
import TypeFormPreviewProperty from '@src/js/components/types/form/TypeFormPreviewProperty.jsx'
import TypeFormPreviewSection from '@src/js/components/types/form/TypeFormPreviewSection.jsx'
import logger from '@src/js/common/logger.js'

const styles = () => ({
  container: {
    flex: '1 1 auto',
    display: 'flex'
  },
  form: {
    width: '100%'
  },
  droppable: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'flex-start',
    alignContent: 'flex-start'
  }
})

class TypeFormPreview extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {}
    this.handleClick = this.handleClick.bind(this)
    this.handleDragStart = this.handleDragStart.bind(this)
    this.handleDragEnd = this.handleDragEnd.bind(this)
  }

  handleClick() {
    const { dragging } = this.state
    if (!dragging) {
      this.props.onSelectionChange()
    }
  }

  handleDragStart(start) {
    this.setState({ dragging: true })

    this.props.onSelectionChange(start.type, {
      id: start.draggableId
    })
  }

  handleDragEnd(result) {
    this.setState({ dragging: false })

    if (!result.destination) {
      return
    }

    if (result.type === TypeFormSelectionType.SECTION) {
      this.props.onOrderChange(TypeFormSelectionType.SECTION, {
        fromIndex: result.source.index,
        toIndex: result.destination.index
      })
    } else if (result.type === TypeFormSelectionType.PROPERTY) {
      this.props.onOrderChange(TypeFormSelectionType.PROPERTY, {
        fromSectionId: result.source.droppableId,
        fromIndex: result.source.index,
        toSectionId: result.destination.droppableId,
        toIndex: result.destination.index
      })
    }
  }

  render() {
    logger.log(logger.DEBUG, 'TypeFormPreview.render')

    const { mode, classes, type, sections, preview, onChange } = this.props

    return (
      <Container className={classes.container} onClick={this.handleClick}>
        <div className={classes.form}>
          <TypeFormPreviewHeader
            type={type}
            preview={preview}
            mode={mode}
            onChange={onChange}
          />
          <DragDropContext
            onDragStart={this.handleDragStart}
            onDragEnd={this.handleDragEnd}
          >
            <Droppable droppableId='root' type={TypeFormSelectionType.SECTION}>
              {provided => (
                <div
                  ref={provided.innerRef}
                  {...provided.droppableProps}
                  className={classes.droppable}
                >
                  {sections.map((section, index) =>
                    this.renderSection(section, index)
                  )}
                  {provided.placeholder}
                </div>
              )}
            </Droppable>
          </DragDropContext>
        </div>
      </Container>
    )
  }

  renderSection(section, index) {
    const { mode, properties, selection, onSelectionChange } = this.props

    const sectionProperties = section.properties.map(id =>
      _.find(properties, ['id', id])
    )

    return (
      <TypeFormPreviewSection
        key={section.id}
        section={section}
        index={index}
        selection={selection}
        mode={mode}
        onSelectionChange={onSelectionChange}
      >
        {this.renderProperties(sectionProperties, 0)}
      </TypeFormPreviewSection>
    )
  }

  renderProperties(properties, index) {
    const {
      mode,
      controller,
      preview,
      selection,
      onChange,
      onSelectionChange
    } = this.props

    return properties.map((property, offset) => {
      const value = _.get(preview, [property.id, 'value'])
      return (
        <TypeFormPreviewProperty
          key={property.id}
          controller={controller}
          property={property}
          value={value}
          index={index + offset}
          selection={selection}
          mode={mode}
          onChange={onChange}
          onSelectionChange={onSelectionChange}
        />
      )
    })
  }
}

export default withStyles(styles)(TypeFormPreview)
