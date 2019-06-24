// Copyright 2019 Tulip Solutions B.V.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package nl.tulipsolutions.compiler.interceptor;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.base.CaseFormat;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InterceptorPlugin {

    static class RequestType {
        String type;
        String javaPackage;

        public RequestType(String type, String javaPackage) {
            this.type = type;
            this.javaPackage = javaPackage;
        }

        public String getProtobufTypeName() {
            return this.type.substring(this.type.lastIndexOf(".") + 1);
        }

        public String getValidatorClassName() {
            String protoClassName = getProtobufTypeName();

            return String.join(".", javaPackage, protoClassName + "Validator");
        }

        public String getValidatorVariableName() {
            return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, this.getProtobufTypeName()) + "Validator";
        }
    }

    static class ValidatorInterceptorTemplate {
        String package_name;
        String class_name;
        List<RequestType> request_types;

        public ValidatorInterceptorTemplate(String package_name, String class_name, List<RequestType> request_types) {
            this.package_name = package_name;
            this.class_name = class_name;
            this.request_types = request_types;
        }

        public RequestType getFirstRequestType() {
            return request_types.get(0);
        }

        public List<RequestType> skipFirstRequestType() {
            return request_types.subList(1, request_types.size());
        }
    }

    private static String getFileNameWithoutExtension(String filename) {
        return filename.substring(filename.lastIndexOf("/") + 1, filename.lastIndexOf("."));
    }

    // Use `java_package` if specified, otherwise use the proto package
    private static String getJavaPackage(DescriptorProtos.FileDescriptorProto file) {
        String javaPackage = file.getOptions().getJavaPackage();
        if (javaPackage == null || javaPackage.equals("")) {
            javaPackage = file.getPackage();
        }
        return javaPackage;
    }

    public static void main(String[] args) throws IOException {
        PluginProtos.CodeGeneratorRequest request = PluginProtos.CodeGeneratorRequest.parseFrom(System.in);
        PluginProtos.CodeGeneratorResponse.Builder response = PluginProtos.CodeGeneratorResponse.newBuilder();

        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile("interceptor.mustache");

        for (DescriptorProtos.FileDescriptorProto file : request.getProtoFileList()) {
            if (file.getDependencyList().stream().noneMatch(dependency -> dependency.contains("validate.proto"))) {
                continue;
            }

            String javaPackage = getJavaPackage(file);
            String javaPackagePath = javaPackage.replace(".", "/");

            for (DescriptorProtos.ServiceDescriptorProto serviceDescriptorProto : file.getServiceList()) {
                List<RequestType> requestTypes = serviceDescriptorProto.getMethodList().stream()
                    .map(DescriptorProtos.MethodDescriptorProto::getInputType)
                    .distinct()
                    .map(inputType -> new RequestType(inputType, javaPackage))
                    .collect(Collectors.toList());

                String validatorName = serviceDescriptorProto.getName() + "ValidateInterceptor";

                StringWriter writer = new StringWriter();
                mustache.execute(
                    writer,
                    new ValidatorInterceptorTemplate(javaPackage, validatorName, requestTypes)
                );

                response.addFileBuilder()
                    .setName(javaPackagePath + "/" + validatorName + ".java")
                    .setContent(writer.toString());
            }
        }

        response.build().toByteString().writeTo(System.out);
    }
}
