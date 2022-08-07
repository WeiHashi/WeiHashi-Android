package cn.devmeteor.responsegenerator

import com.squareup.javapoet.*
import java.io.FileWriter
import java.io.IOException
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements


class AProcessor : AbstractProcessor() {

    private lateinit var mfiler: Filer
    private lateinit var mElementUtils: Elements

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        mfiler = processingEnv.filer
        mElementUtils = processingEnv.elementUtils
    }

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        generateFile(roundEnv, ResponseModel::class.java)
        generateFile(roundEnv, ResponseListModel::class.java, true)
        return true
    }
    private fun generateFile(
        roundEnv: RoundEnvironment,
        java: Class<out Annotation>,
        isListModel: Boolean = false
    ) {
        val elements = roundEnv.getElementsAnnotatedWith(java)
        for (e in elements) {
            try {
                JavaFile.builder(
                    "cn.devmeteor.weihashi.response",
                    TypeSpec.classBuilder(
                        "${e.simpleName}${if (isListModel) "List" else ""}Response"
                    ).addModifiers(Modifier.PUBLIC)
                        .superclass(Response::class.java)
                        .apply {
                            if (isListModel) {
                                addFieldAndGetterSetter(
                                    ParameterizedTypeName.get(
                                        ClassName.get(List::class.java),
                                        TypeName.get(e.asType())
                                    ), "data"
                                )
                            } else {
                                addFieldAndGetterSetter(TypeName.get(e.asType()), "data")
                            }
                        }.addMethod(MethodSpec.methodBuilder("toString")
                            .addAnnotation(Override::class.java)
                            .returns(String::class.java)
                            .addModifiers(Modifier.PUBLIC)
                            .addCode(getToStringBody(e,isListModel))
                            .build()
                        )
                        .build()
                ).build().writeTo(mfiler)
            } catch (e: Exception) {
                log(e.toString())
            }
        }
    }

    private fun getToStringBody(e:Element,isListModel: Boolean):String="""
        return "${e.simpleName}${if (isListModel) "List" else ""}Response:"+
            "\ncode:" + getCode() +
            "\nmsg:" + getMsg() +
            "\ndata:" + data;
    """.trimIndent()

    private fun log(msg: String) {
        try {
            FileWriter("processor.log", true).use { wr ->
                wr.write(
                    """
                $msg
                
                """.trimIndent()
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> =
        mutableSetOf(
            ResponseModel::class.java.canonicalName!!,
            ResponseListModel::class.java.canonicalName!!
        )

}