# Required software for the build
# brew install git
# brew install npm
# brew install yarn

# Example Build Environment
# git clone https://github.com/ckeditor/ckeditor5-build-inline.git
# cd ckeditor5-build-inline

# Build commands

npm install --save "@ckeditor/ckeditor5-adapter-ckfinder@^16.0.0" \
    "@ckeditor/ckeditor5-autoformat@^16.0.0" \
	"@ckeditor/ckeditor5-basic-styles@^16.0.0" \
	"@ckeditor/ckeditor5-block-quote@^16.0.0" \
	"@ckeditor/ckeditor5-ckfinder@^16.0.0" \
	"@ckeditor/ckeditor5-core@^16.0.0" \
	"@ckeditor/ckeditor5-dev-utils@^12.0.7" \
	"@ckeditor/ckeditor5-dev-webpack-plugin@^8.0.7" \
	"@ckeditor/ckeditor5-easy-image@^16.0.0" \
	"@ckeditor/ckeditor5-essentials@^16.0.0" \
	"@ckeditor/ckeditor5-heading@^16.0.0" \
	"@ckeditor/ckeditor5-image@^16.0.0" \
	"@ckeditor/ckeditor5-indent@^16.0.0" \
	"@ckeditor/ckeditor5-link@^16.0.0" \
	"@ckeditor/ckeditor5-list@^16.0.0" \
	"@ckeditor/ckeditor5-media-embed@^16.0.0" \
	"@ckeditor/ckeditor5-paragraph@^16.0.0" \
	"@ckeditor/ckeditor5-paste-from-office@^16.0.0" \
	"@ckeditor/ckeditor5-table@^16.0.0" \
	"@ckeditor/ckeditor5-theme-lark@^16.0.0" \
	"eslint" \
	"eslint-config-ckeditor5" \
	"husky" \
	"lint-staged" \
	"postcss-loader" \
	"raw-loader" \
	"style-loader" \
	"stylelint" \
	"stylelint-config-ckeditor5" \
	"terser-webpack-plugin" \
	"webpack" \
	"webpack-cli" \
	'@ckeditor/ckeditor5-editor-inline' \
	'@ckeditor/ckeditor5-alignment@^16.0.0' \
	'@ckeditor/ckeditor5-font@^16.0.0' \
	'@ckeditor/ckeditor5-highlight@^16.0.0' \
	'@ckeditor/ckeditor5-upload@^16.0.0' \
	'@ckeditor/ckeditor5-remove-format@^16.0.0' \
	'@wiris/mathtype-ckeditor5@^7.17.1' \
	'@ckeditor/ckeditor5-editor-decoupled@^16.0.0';


yarn run build