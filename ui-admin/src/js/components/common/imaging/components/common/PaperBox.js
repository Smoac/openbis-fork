import React from 'react';
import makeStyles from '@mui/styles/makeStyles';
import Paper from '@mui/material/Paper';

const useStyles = makeStyles(() => ({
    root: {
        padding: '8px',
        margin: '6px 0 12px 0',
        borderColor: '#ebebeb',
        borderStyle: 'solid',
        borderWidth: '1px 2px 2px 1px',
        backgroundColor: '#fff',
        '&:hover': {
            borderColor: '#dbdbdb'
        }
    },
}));

export default function PaperBox({className, children, elevation= 1}) {
    const classes = useStyles();

    return (
        <Paper elevation={elevation} className={classes.root + " " + className}>
            {children}
        </Paper>
    );
}