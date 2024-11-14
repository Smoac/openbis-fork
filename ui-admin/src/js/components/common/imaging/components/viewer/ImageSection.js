import React from 'react'
import { Grid2 } from "@mui/material";
import PaperBox from "@src/js/components/common/imaging/components/common/PaperBox.js";
import Export from "@src/js/components/common/imaging/components/viewer/Exporter.jsx";
import constants from "@src/js/components/common/imaging/constants.js";
import ImageListItemSection
	from "@src/js/components/common/imaging/components/common/ImageListItemSection.js";
import messages from '@src/js/common/messages.js'


const ImageSection = ({ images, activeImageIdx, configExports, onActiveItemChange, handleExport }) => {
	return (<PaperBox>
		<Grid2 container direction='row' spacing={1} sx={{ justifyContent: "space-between", alignItems: "center" }}>
			<Grid2 size={{ xs: 9, sm: 10 }}>
				<ImageListItemSection title={messages.get(messages.IMAGES)}
					cols={3} rowHeight={150}
					type={constants.IMAGE_TYPE}
					items={images}
					activeImageIdx={activeImageIdx}
					onActiveItemChange={onActiveItemChange} />
			</Grid2>
			<Grid2 size={{ xs: 3, sm: 2 }} container direction='column' sx={{ justifyContent: "space-around" }}>
				{configExports.length > 0 &&
					<Export handleExport={handleExport} config={configExports} />}
			</Grid2>
		</Grid2>
	</PaperBox>);
}

export default ImageSection;