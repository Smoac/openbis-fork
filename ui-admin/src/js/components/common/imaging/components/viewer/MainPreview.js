import React from 'react'
import { Box, Grid2, Typography } from "@mui/material";
import messages from '@src/js/common/messages.js'
import makeStyles from '@mui/styles/makeStyles';

const useStyles = makeStyles((theme) => ({
	imgContainer: {
		maxHeight: '800px',
		textAlign: 'center',
		overflow: 'auto',
	}
}));

const MainPreview = ({ activePreview, resolution }) => {
	const classes = useStyles();

	return (<Grid2 container xs={12} sm={8} sx={{ justifyContent: "space-between", alignItems: "center" }}>
		<Box className={classes.imgContainer}>
			{activePreview.bytes === null ?
				<Typography variant='body2'>
					{messages.get(messages.NO_PREVIEW)}
				</Typography>
				: <img
					src={`data:image/${activePreview.format};base64,${activePreview.bytes}`}
					alt={""}
					height={resolution[0]}
					width={resolution[1]}
				/>}
		</Box>
	</Grid2>
	);
};

export default MainPreview;