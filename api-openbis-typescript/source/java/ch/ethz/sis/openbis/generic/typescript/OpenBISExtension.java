

/*
 *
 *
 * Copyright 2023 Simone Baffelli (simone.baffelli@empa.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.ethz.sis.openbis.generic.typescript;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;

import ch.systemsx.cisd.base.annotation.JsonObject;
import cz.habarta.typescript.generator.DefaultTypeProcessor;
import cz.habarta.typescript.generator.Extension;
import cz.habarta.typescript.generator.Logger;
import cz.habarta.typescript.generator.TsProperty;
import cz.habarta.typescript.generator.TsType;
import cz.habarta.typescript.generator.TypeProcessor;
import cz.habarta.typescript.generator.TypeScriptGenerator;
import cz.habarta.typescript.generator.compiler.EnumMemberModel;
import cz.habarta.typescript.generator.compiler.ModelCompiler;
import cz.habarta.typescript.generator.compiler.Symbol;
import cz.habarta.typescript.generator.compiler.SymbolTable;
import cz.habarta.typescript.generator.compiler.TsModelTransformer;
import cz.habarta.typescript.generator.emitter.EmitterExtensionFeatures;
import cz.habarta.typescript.generator.emitter.TsBeanCategory;
import cz.habarta.typescript.generator.emitter.TsBeanModel;
import cz.habarta.typescript.generator.emitter.TsEnumModel;
import cz.habarta.typescript.generator.emitter.TsHelper;
import cz.habarta.typescript.generator.emitter.TsMethodModel;
import cz.habarta.typescript.generator.emitter.TsModel;
import cz.habarta.typescript.generator.emitter.TsModifierFlags;
import cz.habarta.typescript.generator.emitter.TsParameterModel;
import cz.habarta.typescript.generator.emitter.TsPropertyModel;
import cz.habarta.typescript.generator.emitter.TsStringLiteral;

/**
 * This extension for the typescript-generator ({@link cz.habarta.typescript.generator}) Gradle <a href="URL#https://github.com/vojtechhabarta/typescript-generator">plugin</a> adds method and constructor signatures to the generated typescript interfaces.
 * The methods are extracted from the java classes using reflection. Currently, it can only create interface signatures to be exported in a d.ts. file, not the implementation.
 *
 * @author Simone Baffelli
 * @author pkupczyk
 */
public class OpenBISExtension extends Extension
{

    private static final Logger logger = TypeScriptGenerator.getLogger();

    private static TsType resolveType(ProcessingContext processingContext, TsBeanModel bean, Type type)
    {
        type = convertByteArrayToString(type);

        TypeToken<?> typeToken = TypeToken.of(bean.getOrigin()).resolveType(type);
        TypeProcessor.Context context = new TypeProcessor.Context(processingContext.getSymbolTable(), processingContext.getLocalProcessor(), null);
        TsType tsType = context.processType(typeToken.getType()).getTsType();

        if (tsType instanceof TsType.ReferenceType)
        {
            if (!((TsType.ReferenceType) tsType).symbol.isResolved())
            {
                throw new UnresolvedTypeException(type);
            }
        }

        return convertDateToNumber(tsType);
    }

    private static List<TsType.GenericVariableType> resolveTypeParameters(ProcessingContext processingContext, TsBeanModel bean,
            TypeVariable<?>[] typeParameters, boolean withBounds)
    {
        List<TsType.GenericVariableType> tsTypeParameters = new ArrayList<>();

        for (TypeVariable<?> typeParameter : typeParameters)
        {
            Type[] boundsTypes = typeParameter.getBounds();

            if (withBounds && boundsTypes.length > 0)
            {
                try
                {
                    List<String> boundsStrings = new ArrayList<>();
                    for (Type boundType : boundsTypes)
                    {
                        TsType tsBoundType = resolveType(processingContext, bean, boundType);
                        boundsStrings.add(tsBoundType.toString());
                    }

                    tsTypeParameters.add(new TsType.GenericVariableType(typeParameter.getName() + " extends " + boundsStrings.get(0)));
                } catch (UnresolvedTypeException e)
                {
                    tsTypeParameters.add(new TsType.GenericVariableType(typeParameter.getName()));
                }
            } else
            {
                tsTypeParameters.add(new TsType.GenericVariableType(typeParameter.getName()));
            }
        }

        return tsTypeParameters;
    }

    private static Type convertByteArrayToString(Type type)
    {
        if (type instanceof Class<?>)
        {
            Class<?> clazz = (Class<?>) type;
            if (clazz.isArray() && clazz.getComponentType().equals(byte.class))
            {
                return String.class;
            }
        }
        return type;
    }

    private static TsType convertDateToNumber(TsType tsType)
    {
        if (TsType.Date.equals(tsType))
        {
            return TsType.Number;
        }

        if (tsType instanceof TsType.BasicArrayType)
        {
            TsType elementType = ((TsType.BasicArrayType) tsType).elementType;
            return new TsType.BasicArrayType(convertDateToNumber(elementType));
        }

        if (tsType instanceof TsType.GenericReferenceType)
        {
            List<TsType> typeArguments = ((TsType.GenericReferenceType) tsType).typeArguments;
            if (typeArguments != null && !typeArguments.isEmpty())
            {
                List<TsType> newTypeArguments = new ArrayList<>();
                for (TsType typeArgument : typeArguments)
                {
                    newTypeArguments.add(convertDateToNumber(typeArgument));
                }
                return new TsType.GenericReferenceType(((TsType.GenericReferenceType) tsType).symbol, newTypeArguments);
            }
        }

        return tsType;
    }

    @Override
    public EmitterExtensionFeatures getFeatures()
    {
        final EmitterExtensionFeatures features = new EmitterExtensionFeatures();
        features.generatesRuntimeCode = false;
        features.generatesModuleCode = true;
        features.worksWithPackagesMappedToNamespaces = true;
        features.generatesJaxrsApplicationClient = false;
        return features;
    }

    @Override
    public List<TransformerDefinition> getTransformers()
    {
        return List.of(new TransformerDefinition(ModelCompiler.TransformationPhase.AfterDeclarationSorting, (TsModelTransformer) (context, model) ->
        {
            logger.info("Started processing beans");

            ProcessingContext processingContext = new ProcessingContext(context.getSymbolTable());

            List<TsBeanModel> tsBeans = new ArrayList<>();
            List<TsPropertyModel> tsBundleProperties = new ArrayList<>();
            List<TsHelper> tsHelpers = new ArrayList<>();

            /*

            For class "X" like:

                @JsonObject("a.b.c.X")
                class X<T> {
                    public X(String p){}

                    public String m(int p){}
                }

            Generate:

                interface XConstructor {
                    new <T>(p:string): X<T>
                }

                interface X<T> {
                    m(p:number): string;
                }

                type a_b_c_X<T> = X<T>

                bundle {
                    ...
                    X:XConstructor,
                    a_b_c_X:XConstructor
                    ...
                }

                export const X:XConstructor
                export const a_b_c_X:XConstructor

            To be able to do things like:

                var test:openbis.X<string> = new openbis.X<string>("abc")
                var test:openbis.X<string> = new openbis.a_b_c_X<string>("abc")

                var test:openbis.a_b_c_X<string> = new openbis.X<string>("abc")
                var test:openbis.a_b_c_X<string> = new openbis.a_b_c_X<string>("abc")

                var bundle:openbis.bundle = ...;
                var test:openbis.X<string> = new bundle.X<string>("abc")
                var test:openbis.X<string> = new bundle.a_b_c_X<string>("abc")

                var bundle:openbis.bundle = ...;
                var test:openbis.a_b_c_X<string> = new bundle.X<string>("abc")
                var test:openbis.a_b_c_X<string> = new bundle.a_b_c_X<string>("abc")

            */

            for (TsBeanModel tsBean : model.getBeans())
            {
                JsonObject tsBeanJsonObject = tsBean.getOrigin().getAnnotation(JsonObject.class);
                TypeScriptObject tsBeanTypeScriptObject = tsBean.getOrigin().getAnnotation(TypeScriptObject.class);

                if (tsBeanJsonObject == null && tsBeanTypeScriptObject == null)
                {
                    logger.info("Skipping bean " + tsBean.getOrigin().getName() + " as it is missing both " + JsonObject.class.getSimpleName()
                            + " and " + TypeScriptObject.class + " annotations.");
                    continue;
                }

                List<TsType.GenericVariableType> tsBeanTypeParametersWithBounds =
                        resolveTypeParameters(processingContext, tsBean, tsBean.getOrigin().getTypeParameters(), true);
                List<TsType.GenericVariableType> tsBeanTypeParametersWithoutBounds =
                        resolveTypeParameters(processingContext, tsBean, tsBean.getOrigin().getTypeParameters(), false);

                String tsBeanAdditionalName = null;

                if (tsBeanJsonObject != null)
                {
                    tsBeanAdditionalName = tsBeanJsonObject.value().replaceAll("\\.", "_");
                } else
                {
                    tsBeanAdditionalName = tsBeanTypeScriptObject.value();
                }

                List<TsMethodModel> tsBeanMethods = new ArrayList<>();

                for (Method method : tsBean.getOrigin().getMethods())
                {
                    TypeScriptMethod typeScriptMethodAnnotation = method.getAnnotation(TypeScriptMethod.class);

                    if (method.isBridge() || (method.getDeclaringClass() == Object.class) || (method.getName()
                            .matches("hashCode|toString|equals")) || (typeScriptMethodAnnotation != null && typeScriptMethodAnnotation.ignore()))
                    {
                        continue;
                    }

                    try
                    {
                        List<TsParameterModel> tsMethodParameters = new ArrayList<>();

                        for (int methodParameterIndex = 0; methodParameterIndex < method.getParameters().length; methodParameterIndex++)
                        {
                            Parameter methodParameter = method.getParameters()[methodParameterIndex];

                            if (methodParameterIndex == 0 && typeScriptMethodAnnotation != null && typeScriptMethodAnnotation.sessionToken())
                            {
                                continue;
                            }

                            TsType tsMethodParameterType = resolveType(processingContext, tsBean, methodParameter.getParameterizedType());
                            tsMethodParameters.add(new TsParameterModel(methodParameter.getName(), tsMethodParameterType));
                        }

                        TsType tsMethodReturnType = resolveType(processingContext, tsBean, method.getGenericReturnType());

                        if (typeScriptMethodAnnotation != null && typeScriptMethodAnnotation.async())
                        {
                            tsMethodReturnType = new TsType.GenericBasicType("Promise", List.of(tsMethodReturnType));
                        }

                        List<TsType.GenericVariableType> tsMethodTypeParameters =
                                resolveTypeParameters(processingContext, tsBean, method.getTypeParameters(), false);

                        tsBeanMethods.add(new TsMethodModel(method.getName(), TsModifierFlags.None, tsMethodTypeParameters, tsMethodParameters,
                                tsMethodReturnType,
                                null, null));

                    } catch (UnresolvedTypeException e)
                    {
                        logger.warning("Skipping method " + method.getDeclaringClass() + "." + method.getName()
                                + " as it contains unresolved type: " + e.getType());
                    }
                }

                List<TsMethodModel> tsConstructors = new ArrayList<>();

                for (Constructor<?> constructor : tsBean.getOrigin().getDeclaredConstructors())
                {
                    if (!Modifier.isPublic(constructor.getModifiers()))
                    {
                        continue;
                    }

                    try
                    {
                        List<TsParameterModel> tsConstructorParameter = new ArrayList<>();

                        for (Parameter constructorParameter : constructor.getParameters())
                        {
                            TsType tsConstructorParameterType = resolveType(processingContext, tsBean, constructorParameter.getParameterizedType());
                            tsConstructorParameter.add(new TsParameterModel(constructorParameter.getName(), tsConstructorParameterType));
                        }

                        TsType tsConstructorReturnType;

                        if (tsBeanTypeParametersWithoutBounds.isEmpty())
                        {
                            tsConstructorReturnType = new TsType.ReferenceType(tsBean.getName());
                        } else
                        {
                            tsConstructorReturnType = new TsType.GenericReferenceType(tsBean.getName(), tsBeanTypeParametersWithoutBounds);
                        }

                        tsConstructors.add(new TsMethodModel("new ", TsModifierFlags.None, tsBeanTypeParametersWithBounds, tsConstructorParameter,
                                tsConstructorReturnType, null, null));

                    } catch (UnresolvedTypeException e)
                    {
                        logger.warning(
                                "Skipping method " + constructor.getDeclaringClass() + "." + constructor.getName()
                                        + " as it contains unresolved type: " + e.getType());
                    }
                }

                /*

                Generate part (if there are constructors):

                    interface XConstructor {
                        new <T>(p:string): X<T>
                    }

                    bundle {
                        ...
                        X:XConstructor,
                        a_b_c_X:XConstructor
                        ...
                    }

                    export const X:XConstructor
                    export const a_b_c_X:XConstructor

                 */

                if (!tsConstructors.isEmpty())
                {
                    String tsConstructorBeanName = tsBean.getName().getSimpleName() + "Constructor";

                    tsConstructors.sort(Comparator.comparing(c -> c.getParameters().size()));

                    tsBeans.add(new TsBeanModel(tsBean.getOrigin(), tsBean.getCategory(), tsBean.isClass(),
                            new Symbol(tsConstructorBeanName),
                            Collections.emptyList(), null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null,
                            tsConstructors, Collections.emptyList()));

                    tsBundleProperties.add(new TsPropertyModel(tsBean.getName().getSimpleName(),
                            new TsType.ReferenceType(new Symbol(tsConstructorBeanName)), null, true, null));

                    tsHelpers.add(
                            new TsHelper(
                                    Collections.singletonList("export const " + tsBean.getName().getSimpleName() + ":" + tsConstructorBeanName)));

                    if (!Strings.isNullOrEmpty(tsBeanAdditionalName) && !tsBeanAdditionalName.equals(tsBean.getName().getSimpleName()))
                    {
                        tsBundleProperties.add(new TsPropertyModel(tsBeanAdditionalName,
                                new TsType.ReferenceType(new Symbol(tsConstructorBeanName)), null, true, null));

                        tsHelpers.add(new TsHelper(Collections.singletonList("export const " + tsBeanAdditionalName + ":" + tsConstructorBeanName)));
                    }
                }

                /*

                Generate part (with or without generic parameters):

                    type a_b_c_X<T> = X<T>

                 */

                if (tsBeanTypeParametersWithBounds.isEmpty())
                {
                    tsHelpers.add(
                            new TsHelper(Collections.singletonList("type " + tsBeanAdditionalName + " = " + tsBean.getName().getSimpleName())));
                } else
                {
                    String tsBeanTypeParametersWithBoundsString =
                            tsBeanTypeParametersWithBounds.stream().map(TsType::toString).collect(Collectors.joining(","));
                    String tsBeanTypeParametersWithoutBoundsString =
                            tsBeanTypeParametersWithoutBounds.stream().map(TsType::toString).collect(Collectors.joining(","));

                    tsHelpers.add(new TsHelper(Collections.singletonList(
                            "type " + tsBeanAdditionalName + "<" + tsBeanTypeParametersWithBoundsString + "> = " + tsBean.getName()
                                    .getSimpleName() + "<" + tsBeanTypeParametersWithoutBoundsString + ">")));
                }

                tsBeanMethods.sort(Comparator.comparing(TsMethodModel::getName).thenComparing(m -> m.getParameters().size())
                        .thenComparing(m -> m.getParameters().stream().map(p -> p.getTsType().toString()).collect(Collectors.toList()).toString()));

                tsBeans.add(
                        new TsBeanModel(tsBean.getOrigin(), tsBean.getCategory(), tsBean.isClass(), tsBean.getName(), tsBeanTypeParametersWithBounds,
                                tsBean.getParent(), tsBean.getExtendsList(), tsBean.getImplementsList(), Collections.emptyList(),
                                tsBean.getConstructor(),
                                tsBeanMethods, tsBean.getComments()));
            }

            /*

             For enum "X" like:

                @JsonObject("a.b.c.X")
                enum X {
                    VALUE1,
                    VALUE2
                }

             Generate:

                 const X = {
                     VALUE1 : "VALUE1",
                     VALUE2 : "VALUE2"
                 } as const

                 const a_b_c_X = {
                     VALUE1 : "VALUE1",
                     VALUE2 : "VALUE2"
                 } as const

                 type X = typeof X[keyof typeof X];
                 type a_b_c_X = typeof a_b_c_X[keyof typeof a_b_c_X];

                 interface XObject {
                     VALUE1:X,
                     VALUE2:X
                 }

                 bundle {
                    ...
                    X:XObject,
                    a_b_c_X:XObject
                    ...
                 }

             To be able to do things like:

                 var test:openbis.X = openbis.X.VALUE1
                 var test:openbis.X = openbis.a_b_c_X.VALUE1
                 var test:openbis.X = "VALUE1"

                 var test:openbis.a_b_c_X = openbis.X.VALUE1
                 var test:openbis.a_b_c_X = openbis.a_b_c_X.VALUE1
                 var test:openbis.a_b_c_X = "VALUE1"

                 var bundle:openbis.bundle = ...;
                 var test:openbis.X = bundle.X.VALUE1
                 var test:openbis.X = bundle.a_b_c_X.VALUE1

                 var bundle:openbis.bundle = ...;
                 var test:openbis.a_b_c_X = bundle.X.VALUE1
                 var test:openbis.a_b_c_X = bundle.a_b_c_X.VALUE1

             But not things like:

                 var test:openbis.X = "ILLEGAL_VALUE"
                 var test:openbis.a_b_c_X = "ILLEGAL_VALUE"

            */

            for (TsEnumModel tsEnum : model.getOriginalStringEnums())
            {
                JsonObject tsEnumJsonObject = tsEnum.getOrigin().getAnnotation(JsonObject.class);
                TypeScriptObject tsEnumTypeScriptObject = tsEnum.getOrigin().getAnnotation(TypeScriptObject.class);

                if (tsEnumJsonObject == null && tsEnumTypeScriptObject == null)
                {
                    logger.info("Skipping enum " + tsEnum.getOrigin().getName() + " as it is missing both " + JsonObject.class.getSimpleName()
                            + " and " + TypeScriptObject.class + " annotations.");
                    continue;
                }

                /*

                Generate part:

                    interface XObject {
                        VALUE1:X,
                        VALUE2:X
                    }

                    bundle {
                        ...
                        X:XObject,
                        ...
                    }

                */

                String tsEnumObjectBeanName = tsEnum.getName().getSimpleName() + "Object";
                List<TsPropertyModel> tsEnumObjectBeanProperties = new ArrayList<>();
                List<String> tsEnumConstProperties = new ArrayList<>();

                for (EnumMemberModel tsMember : tsEnum.getMembers())
                {
                    tsEnumObjectBeanProperties.add(
                            new TsPropertyModel(tsMember.getPropertyName(), new TsType.GenericReferenceType(tsEnum.getName()),
                                    Collections.emptyList(), TsModifierFlags.None, true,
                                    new TsStringLiteral(tsMember.getPropertyName()), Collections.emptyList()));

                    tsEnumConstProperties.add(tsMember.getPropertyName() + " : \"" + tsMember.getPropertyName() + "\"");
                }

                tsEnumObjectBeanProperties.sort(Comparator.comparing(TsProperty::getName));
                tsEnumConstProperties.sort(Comparator.naturalOrder());

                tsBeans.add(new TsBeanModel(tsEnum.getOrigin(), tsEnum.getCategory(), false, new Symbol(tsEnumObjectBeanName),
                        Collections.emptyList(), null, Collections.emptyList(), Collections.emptyList(), tsEnumObjectBeanProperties, null, null,
                        Collections.emptyList()));

                tsBundleProperties.add(new TsPropertyModel(tsEnum.getName().getSimpleName(),
                        new TsType.ReferenceType(new Symbol(tsEnumObjectBeanName)), null, true, null));

                /*

                Generate part:

                     const X = {
                        VALUE1 : "VALUE1",
                        VALUE2 : "VALUE2"
                     } as const

                     type X = typeof X[keyof typeof X];

                 */

                tsHelpers.add(new TsHelper(
                        Collections.singletonList(
                                "const " + tsEnum.getName().getSimpleName() + " = {\n" + String.join(",\n", tsEnumConstProperties) + "} as const")));
                tsHelpers.add(new TsHelper(Collections.singletonList(
                        "type " + tsEnum.getName().getSimpleName() + " = typeof " + tsEnum.getName().getSimpleName() + "[keyof typeof "
                                + tsEnum.getName().getSimpleName() + "]")));

                String tsEnumAdditionalName = tsEnumJsonObject.value().replaceAll("\\.", "_");

                /*

                Generate part:

                     const a_b_c_X = {
                         VALUE1 : "VALUE1",
                         VALUE2 : "VALUE2"
                     } as const

                     type a_b_c_X = typeof a_b_c_X[keyof typeof a_b_c_X];


                     bundle {
                        ...
                        a_b_c_X:XObject
                        ...
                     }

                 */

                if (!tsEnumAdditionalName.equals(tsEnum.getName().getSimpleName()))
                {
                    tsBundleProperties.add(new TsPropertyModel(tsEnumAdditionalName,
                            new TsType.ReferenceType(new Symbol(tsEnumObjectBeanName)), null, true, null));

                    tsHelpers.add(
                            new TsHelper(Collections.singletonList(
                                    "const " + tsEnumAdditionalName + " = {\n" + String.join(",\n", tsEnumConstProperties) + "} as const")));
                    tsHelpers.add(new TsHelper(Collections.singletonList(
                            "type " + tsEnumAdditionalName + " = typeof " + tsEnumAdditionalName + "[keyof typeof "
                                    + tsEnumAdditionalName + "]")));
                }
            }

            tsBundleProperties.sort(Comparator.comparing(TsProperty::getName));

            tsBeans.add(
                    new TsBeanModel(null, TsBeanCategory.Data, false, new Symbol("bundle"), null, null, null, null, tsBundleProperties, null, null,
                            null));

            tsBeans.sort(Comparator.comparing(b -> b.getName().getSimpleName()));
            tsHelpers.sort(Comparator.comparing(h -> h.getLines().toString()));

            return new TsModel(tsBeans, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), tsHelpers);
        }));
    }

    private static class ProcessingContext
    {
        private final SymbolTable symbolTable;

        private final TypeProcessor localProcessor;

        ProcessingContext(SymbolTable symbolTable)
        {
            this.symbolTable = symbolTable;
            this.localProcessor = new DefaultTypeProcessor();
        }

        public SymbolTable getSymbolTable()
        {
            return symbolTable;
        }

        public TypeProcessor getLocalProcessor()
        {
            return localProcessor;
        }
    }

    private static class UnresolvedTypeException extends RuntimeException
    {
        private final Type type;

        public UnresolvedTypeException(final Type type)
        {
            this.type = type;
        }

        public Type getType()
        {
            return type;
        }
    }

}
