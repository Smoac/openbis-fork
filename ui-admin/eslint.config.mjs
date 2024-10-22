import react from "eslint-plugin-react";
import prettier from "eslint-plugin-prettier";
import globals from "globals";
import babelParser from "babel-eslint";
import path from "node:path";
import { fileURLToPath } from "node:url";
import js from "@eslint/js";
import { FlatCompat } from "@eslint/eslintrc";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const compat = new FlatCompat({
    baseDirectory: __dirname,
    recommendedConfig: js.configs.recommended,
    allConfig: js.configs.all
});

export default [...compat.extends(
    "eslint:recommended",
    "plugin:react/recommended",
    "plugin:prettier/recommended",
), {
    plugins: {
        react,
        prettier,
    },

    languageOptions: {
        globals: {
            ...globals.browser,
            ...globals.jest,
        },

        parser: babelParser,
        ecmaVersion: 2017,
        sourceType: "module",

        parserOptions: {
            ecmaFeatures: {
                jsx: true,
            },
        },
    },

    settings: {
        react: {
            createClass: "createReactClass",
            pragma: "React",
            version: "16.4.2",
        },

        propWrapperFunctions: ["forbidExtraProps"],
    },

    rules: {
        "react/jsx-uses-react": "error",
        "react/jsx-uses-vars": "error",
        "react/prop-types": "off",
        "prettier/prettier": "error",
        eqeqeq: ["error", "always"],
        "no-cond-assign": ["error", "always"],
    },
}];