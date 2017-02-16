package com.compiler;

import com.google.auto.service.AutoService;
import com.linked.annotion.Action;
import com.linked.annotion.Module;
import com.linked.annotion.Modules;
import com.linked.annotion.Provider;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class ActionPrecessor extends AbstractProcessor {
    private Filer filer;
    private Messager messager;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportTypes = new HashSet<>();
        supportTypes.add(Action.class.getCanonicalName());
        supportTypes.add(Provider.class.getCanonicalName());
        return supportTypes;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        } else {
            Set<? extends Element> provideList = roundEnv.getElementsAnnotatedWith(Provider.class);
            Set<? extends Element> actionList = roundEnv.getElementsAnnotatedWith(Action.class);
            if (provideList.isEmpty() || actionList.isEmpty()) {
                return false;
            } else {
                Set<? extends Element> modulesList = roundEnv.getElementsAnnotatedWith(Modules.class);
                Set<? extends Element> moduleList = roundEnv.getElementsAnnotatedWith(Module.class);
                String aptModuleName = "ProviderMappingInit";
                if (!modulesList.isEmpty()) {
                    Element element = modulesList.iterator().next();
                    Modules modules = element.getAnnotation(Modules.class);
                    generateModulesProviderMappingInit(modules.modules());
                } else if (moduleList.isEmpty()) {
                    generateDefaultProviderMappingInit();
                } else if (moduleList.size() > 1) {
                    throw new IllegalArgumentException("one Modules annotation is enough");
                }

                if (!moduleList.isEmpty()) {
                    Element element = moduleList.iterator().next();
                    Module modules = element.getAnnotation(Module.class);
                    aptModuleName += "_" + modules.name();
                }
                generateProviderMapping(aptModuleName, roundEnv);
                return true;
            }
        }
    }

    private void generateDefaultProviderMappingInit() {
        debug("generateDefaultProviderMappingInit");
        MethodSpec.Builder initBuilder = MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(HashMap.class, "providerMap")
                .addParameter(HashMap.class, "actionMap");
        initBuilder.addStatement("ProviderMappingInit.init(providerMap, actionMap)");
        TypeSpec providerInit = TypeSpec.classBuilder("ProviderInit")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(initBuilder.build())
                .build();
        try {
            JavaFile.builder("com.provider", providerInit)
                    .build()
                    .writeTo(filer);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void generateModulesProviderMappingInit(String[] modules) {
        MethodSpec.Builder initBuilder = MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(HashMap.class, "providerMap")
                .addParameter(HashMap.class, "actionMap")
                .addStatement("ProviderMappingInit.init(providerMap, actionMap)");
        for (String moduleName : modules) {
            initBuilder.addStatement("ProviderMappingInit_" + moduleName + ".init(providerMap, actionMap)");
        }
        TypeSpec providerInit = TypeSpec.classBuilder("ProviderInit")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(initBuilder.build())
                .build();
        try {
            JavaFile.builder("com.provider", providerInit)
                    .build()
                    .writeTo(filer);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void generateProviderMapping(String fileName, RoundEnvironment roundEnv) {
        Set<? extends Element> provideList = roundEnv.getElementsAnnotatedWith(Provider.class);
        Set<? extends Element> actionList = roundEnv.getElementsAnnotatedWith(Action.class);
        MethodSpec.Builder initBuilder = MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(HashMap.class, "providerMap")
                .addParameter(HashMap.class, "actionMap");
        ClassName arrayList = ClassName.get("java.util", "ArrayList");
        for (Element element : provideList) {
            if (element.getKind() == ElementKind.CLASS) {
                ClassName providerClassName = ClassName.get((TypeElement) element);
                Provider provider = element.getAnnotation(Provider.class);
                initBuilder.addStatement("$T $N = new $T()", providerClassName, providerClassName.simpleName().toLowerCase(), providerClassName);
                initBuilder.addCode("if( providerMap.get($S) == null ){\n", provider.processName());
                initBuilder.addCode("providerMap.put($S, new $T());\n}\n", provider.processName(), arrayList);
                initBuilder.addCode("(($T)providerMap.get($S)).add($N);\n", arrayList, provider.processName(), providerClassName.simpleName().toLowerCase());
            }
        }

        for (Element element : actionList) {
            if (element.getKind() == ElementKind.CLASS) {
                Action action = element.getAnnotation(Action.class);
                ClassName actionClassName = ClassName.get((TypeElement) element);
                initBuilder.addStatement("$T $N = new $T()", actionClassName, actionClassName.simpleName().toLowerCase(), actionClassName);
                String key = action.processName() + "_" + action.providerName();
                initBuilder.addCode("if(actionMap.get($S) == null ){\n", key);
                initBuilder.addCode("actionMap.put($S, new $T());\n}\n", key, arrayList);
                initBuilder.addCode("(($T)actionMap.get($S)).add($N);\n", arrayList, key, actionClassName.simpleName().toLowerCase());
            }
        }
        TypeSpec providerInit = TypeSpec.classBuilder(fileName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(initBuilder.build())
                .build();
        try {
            JavaFile.builder("com.provider", providerInit)
                    .build()
                    .writeTo(filer);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void debug(String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }
}
