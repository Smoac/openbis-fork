import React from 'react'
import { Divider, Typography } from "@mui/material";
import { isObjectEmpty } from "@src/js/components/common/imaging/utils.js";
import PaperBox from "@src/js/components/common/imaging/components/common/PaperBox.js";
import DefaultMetadaField
    from "@src/js/components/common/imaging/components/gallery/DefaultMetadaField.js";


const MetadataSection = ({activePreview, activeImage, configMetadata}) => {
	const currPreviewMetadata = activePreview.metadata;

	if (isObjectEmpty(configMetadata) && isObjectEmpty(currPreviewMetadata))
		return (
			<PaperBox>
				<Typography gutterBottom variant='h6'>
					No Metadata to display
				</Typography>
			</PaperBox>
		);

	return (<PaperBox>
			<Typography gutterBottom variant='h6'>
				Preview Metadata Section
			</Typography>
			<Typography key={`preview-metadata-${activePreview.index}`} variant="body2"
				component={'span'} sx={{
					color: "textSecondary"
				}}>
				{isObjectEmpty(currPreviewMetadata) ?
					<p>No preview metadata to display</p>
					: Object.entries(currPreviewMetadata).map(([key, value], pos) =>
						<DefaultMetadaField key={'preview-property-' + pos} keyProp={key}
							valueProp={value} idx={activeImage.index}
							pos={pos} />)
				}
			</Typography>
			<Divider />
			<Typography gutterBottom variant='h6'>
				Image Metadata Section
			</Typography>
			<Typography key={`image-metadata-${activeImage.index}`} variant="body2"
				component={'span'} sx={{
					color: "textSecondary"
				}}>
				{isObjectEmpty(activeImage.metadata) ?
					<p>No image metadata to display</p>
					: Object.entries(activeImage.metadata).map(([key, value], pos) =>
						<DefaultMetadaField key={'image-property-' + pos} keyProp={key}
							valueProp={value} idx={activePreview.index}
							pos={pos} />)
				}
			</Typography>
			<Divider />
			<Typography gutterBottom variant='h6'>
				Config Metadata section
			</Typography>
			<Typography key={`config-metadata`} variant="body2"
				component={'span'} sx={{
					color: "textSecondary"
				}}>
				{isObjectEmpty(configMetadata) ?
					<p>No config metadata to display</p>
					: Object.entries(configMetadata).map(([key, value], pos) =>
						<DefaultMetadaField key={'config-property-' + pos} keyProp={key}
							valueProp={value} idx={activePreview.index}
							pos={pos} />)
				}
			</Typography>
		</PaperBox>
	);
};

export default MetadataSection;