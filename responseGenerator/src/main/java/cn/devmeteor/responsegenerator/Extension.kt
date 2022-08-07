package cn.devmeteor.responsegenerator

import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import java.util.*
import javax.lang.model.element.Modifier

fun TypeSpec.Builder.addFieldAndGetterSetter(typeName: TypeName, name: String): TypeSpec.Builder {
    val bigName = name.substring(0, 1).toUpperCase(Locale.getDefault()) +
            name.substring(1, name.length)
    addField(typeName, name, Modifier.PRIVATE)
    addMethod(
        MethodSpec.methodBuilder("get$bigName")
            .addModifiers(Modifier.PUBLIC)
            .returns(typeName)
            .addCode("return $name;")
            .build()
    )
    addMethod(
        MethodSpec.methodBuilder("set$bigName")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(typeName, name)
            .addCode("this.$name=$name;")
            .build()
    )
    return this
}