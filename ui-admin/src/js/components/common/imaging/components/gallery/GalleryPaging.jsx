import React from 'react';
import withStyles from '@mui/styles/withStyles';
import {Typography, FormControlLabel, IconButton, Grid} from '@mui/material';
import FirstPageIcon from '@mui/icons-material/FirstPage';
import KeyboardArrowLeft from '@mui/icons-material/KeyboardArrowLeft';
import KeyboardArrowRight from '@mui/icons-material/KeyboardArrowRight';
import LastPageIcon from '@mui/icons-material/LastPage';
import SelectField from '@src/js/components/common/form/SelectField.jsx';
import GridPagingOptions from '@src/js/components/common/grid/GridPagingOptions.js';
import messages from '@src/js/common/messages.js';
import logger from '@src/js/common/logger.js';

const styles = theme => ({
    container: {
        display: 'flex',
        alignItems: 'center',
        flexShrink: 0
    },
    pageSize: {
        marginLeft: -theme.spacing(2)
    },
    pageSizeLabelPlacement: {
        marginRight: 0,
        marginBottom: 0
    },
    pageSizeLabel: {
        fontSize: theme.typography.body2.fontSize,
        marginRight: '12px',
        whiteSpace: 'nowrap',
        lineHeight: '46px'
    },
    pageRange: {
        marginLeft: theme.spacing(1),
        marginRight: theme.spacing(2)
    },
    pagePrevButtons: {},
    pageNextButtons: {},
    separator: {
        borderLeftWidth: '1px',
        borderLeftStyle: 'solid',
        borderLeftColor: theme.palette.border.secondary,
        height: theme.spacing(3),
        marginLeft: theme.spacing(2),
        marginRight: theme.spacing(2)
    }
})

class GalleryPaging extends React.PureComponent {
    constructor(props) {
        super(props)
        this.handlePageSizeChange = this.handlePageSizeChange.bind(this)
        this.handleFirstPageButtonClick = this.handleFirstPageButtonClick.bind(this)
        this.handleBackButtonClick = this.handleBackButtonClick.bind(this)
        this.handleNextButtonClick = this.handleNextButtonClick.bind(this)
        this.handleLastPageButtonClick = this.handleLastPageButtonClick.bind(this)
    }

    handlePageSizeChange(event) {
        this.props.onPageSizeChange(event.target.value);
    }

    handleFirstPageButtonClick() {
        this.props.onPageChange(0);
    }

    handleBackButtonClick() {
        this.props.onPageChange(this.props.page - 1);
    }

    handleNextButtonClick() {
        this.props.onPageChange(this.props.page + 1);
    }

    handleLastPageButtonClick() {
        this.props.onPageChange(
            Math.max(0, Math.ceil(this.props.count / this.props.pageSize) - 1)
        );
    }

    render() {
        logger.log(logger.DEBUG, 'GridPaging.render')

        const { id, classes, count, page, pageSize, pageColumns, onColumnChange, options, isGridView } = this.props

        return (
            (<Grid container spacing={2} className={classes.container}>
                <Grid item xs={3} sm className={classes.pagePrevButtons}>
                    <IconButton
                        id={id + '.first-page-id'}
                        onClick={this.handleFirstPageButtonClick}
                        disabled={page === 0}
                        aria-label={messages.get(messages.FIRST_PAGE)}
                        data-part='firstPage'
                        size="large">
                        <FirstPageIcon fontSize='small'/>
                    </IconButton>
                    <IconButton
                        id={id + '.prev-page-id'}
                        onClick={this.handleBackButtonClick}
                        disabled={page === 0}
                        aria-label={messages.get(messages.PREVIOUS_PAGE)}
                        data-part='prevPage'
                        size="large">
                        <KeyboardArrowLeft fontSize='small'/>
                    </IconButton>
                </Grid>
                <Grid item xs={4} sm id={id + '.page-range-id'} className={classes.pageRange}>
                    <Typography variant='body2' data-part='range'>
                        {this.renderRange()}
                    </Typography>
                </Grid>
                <Grid item xs={3} sm className={classes.pageNextButtons}>
                    <IconButton
                        id={id + '.next-page-id'}
                        onClick={this.handleNextButtonClick}
                        disabled={page >= Math.ceil(count / pageSize) - 1}
                        aria-label={messages.get(messages.NEXT_PAGE)}
                        data-part='nextPage'
                        size="large">
                        <KeyboardArrowRight fontSize='small'/>
                    </IconButton>
                    <IconButton
                        id={id + '.last-page-id'}
                        onClick={this.handleLastPageButtonClick}
                        disabled={page >= Math.ceil(count / pageSize) - 1}
                        aria-label={messages.get(messages.LAST_PAGE)}
                        data-part='lastPage'
                        size="large">
                        <LastPageIcon fontSize='small'/>
                    </IconButton>
                </Grid>
                <Grid item xs={6} sm id={id + '.page-size-id'} className={classes.pageSize}>
                    <FormControlLabel
                        control={
                            <SelectField
                                value={pageSize}
                                options={options}
                                onChange={this.handlePageSizeChange}
                                variant='standard'
                            />
                        }
                        classes={{
                            label: classes.pageSizeLabel,
                            labelPlacementStart: classes.pageSizeLabelPlacement
                        }}
                        label={messages.get(messages.ITEMS_PER_PAGE)}
                        labelPlacement='start'
                    />
                </Grid>
                <Grid item xs={6} sm id={id + '.grid-cols-id'} className={classes.pageSize}>
                    <FormControlLabel
                        control={
                            <SelectField
                                value={pageColumns}
                                options={GridPagingOptions.COLUMN_OPTIONS.map(cols => ({
                                    label: cols,
                                    value: cols
                                }))}
                                onChange={event => onColumnChange(event.target.value)}
                                variant='standard'
                            />
                        }
                        classes={{
                            label: classes.pageSizeLabel,
                            labelPlacementStart: classes.pageSizeLabelPlacement
                        }}
                        label={messages.get(messages.COLUMNS)}
                        labelPlacement='start'
                        disabled={!isGridView}
                    />
                </Grid>
            </Grid>)
        );
    }

    renderRange() {
        const {count, page, pageSize} = this.props

        if (count === 0) {
            return <span>{messages.get(messages.NO_RESULTS_FOUND)}</span>
        } else if (count === 1) {
            return <span>{messages.get(messages.RESULTS_RANGE, 1, 1)}</span>
        } else {
            const from = Math.min(count, page * pageSize + 1)
            const to = Math.min(count, (page + 1) * pageSize)

            return (
                <span>
          {messages.get(messages.RESULTS_RANGE, from + '-' + to, count)}
        </span>
            )
        }
    }
}

export default withStyles(styles)(GalleryPaging)
