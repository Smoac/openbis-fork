import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import GridFilterOptions from '@src/js/components/common/grid/GridFilterOptions.js'
import TextField from '@src/js/components/common/form/TextField.jsx'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'
import CustomSwitch from "@src/js/components/database/imaging/components/common/CustomSwitch";

const styles = theme => ({
    container: {
        display: 'flex',
        alignItems: 'center'
    },
    operator: {
        flex: '0 0 auto',
        marginRight: theme.spacing(2)
    },
    text: {
        width: '100%'
    }
})

class GalleryFilter extends React.PureComponent {
    constructor(props) {
        super(props)
    }

    handleGalleryFilterChange = (event) => {
        //console.log('handleGalleryFilterChange - event.target: ', event.target.value);
        const { galleryFilter, onGalleryFilterChange } = this.props
        if (onGalleryFilterChange) {
            const newGlobalFilter = { ...galleryFilter }
            newGlobalFilter[event.target.name] = event.target.value
            onGalleryFilterChange(newGlobalFilter)
        }
    }

    render() {
        logger.log(logger.DEBUG, 'GridGlobalFilter.render')

        const { id, options, galleryFilter, classes } = this.props

        return (
            <div className={classes.container}>
                <div className={classes.operator}>
                    <SelectField
                        name='operator'
                        options={[
                            {
                                label: messages.get(messages.OPERATOR_AND),
                                value: GridFilterOptions.OPERATOR_AND
                            },
                            {
                                label: messages.get(messages.OPERATOR_OR),
                                value: GridFilterOptions.OPERATOR_OR
                            }
                        ]}
                        value={galleryFilter.operator}
                        onChange={this.handleGalleryFilterChange}
                        variant='standard'
                    />

                </div>
                <div className={classes.operator}>
                    <SelectField
                        name='property'
                        options={options}
                        value={galleryFilter.property}
                        onChange={this.handleGalleryFilterChange}
                        variant='standard'
                    />
                </div>
                <div className={classes.text}>
                    <TextField
                        name='text'
                        id={id + '.gallery-filter'}
                        value={galleryFilter.text}
                        onChange={this.handleGalleryFilterChange}
                        placeholder='property value'
                        variant='standard'
                    />
                </div>
            </div>
        )
    }
}

export default withStyles(styles)(GalleryFilter)