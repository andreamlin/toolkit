/* Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.api.codegen;

import static com.google.api.codegen.ArtifactType.GAPIC_CODE;
import static com.google.api.codegen.GeneratorMain.createCodeGeneratorOptions;

import com.google.api.codegen.gapic.GapicGeneratorApp;
import com.google.api.tools.framework.model.Diag;
import com.google.api.tools.framework.tools.ToolOptions;
import com.google.api.tools.framework.tools.ToolUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.compiler.PluginProtos;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

/** Entrypoint for protoc-invoked generation. */
public class ProtocGeneratorMain {

  private static final ArtifactType DEFAULT_ARTIFACT_TYPE = GAPIC_CODE;

  public static void main(String[] args) throws Exception {

    CodeGeneratorRequest request = PluginProtos.CodeGeneratorRequest.parseFrom(System.in);
    CodeGeneratorResponse response;
    int exitCode;
    try {

      ToolOptions toolOptions = parseOptions(request);

      GapicGeneratorApp codeGen = new GapicGeneratorApp(toolOptions, DEFAULT_ARTIFACT_TYPE, true);

      exitCode = codeGen.run();
      response = codeGen.getCodeGeneratorProtoResponse();
      if (response == null) {
        throw new RuntimeException(collectDiags(codeGen));
      }
      System.exit(exitCode);
    } catch (Exception e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      pw.flush();
      response = PluginProtos.CodeGeneratorResponse.newBuilder().setError(sw.toString()).build();
      exitCode = 1;
    }
    response.writeTo(System.out);
    System.out.flush();
    System.exit(exitCode);
  }

  @VisibleForTesting
  static ToolOptions parseOptions(CodeGeneratorRequest request) throws Exception {
    List<FileDescriptorProto> fileDescriptorProtoList = request.getProtoFileList();
    FileDescriptorSet descriptorSet =
        FileDescriptorSet.newBuilder().addAllFile(fileDescriptorProtoList).build();

    // Write out DescriptorSet to temp file.
    File descriptorSetFile = File.createTempFile("api", ".desc");

    List<String> parsedArgs = new LinkedList<>();

    // Parse plugin params, ignoring unknown params.
    String[] requestArgs = request.getParameter().split(",");
    for (String arg : requestArgs) {
      if (arg.startsWith("descriptor=")) {
        arg = String.format("descriptor=%s", descriptorSetFile.getAbsolutePath());
      }
      parsedArgs.add("--" + arg);
      // String[] keyValues = arg.split("=");
      // parsedArgs.add("--" + keyValues[0]);
      // if (keyValues.length > 1) {
      //   parsedArgs.add();
      // }
    }

    String[] argsArray = parsedArgs.toArray(new String[] {});

    return createCodeGeneratorOptions(argsArray);
  }

  private static String collectDiags(GapicGeneratorApp app) {
    StringBuilder stringBuilder = new StringBuilder();
    for (Diag diag : app.getDiags()) {
      stringBuilder.append(ToolUtil.diagToString(diag, true));
      stringBuilder.append("\n");
    }

    return stringBuilder.toString();
  }
}
