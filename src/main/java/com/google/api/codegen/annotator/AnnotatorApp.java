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
package com.google.api.codegen.annotator;

import static com.google.api.codegen.gapic.GapicGeneratorApp.GENERATOR_CONFIG_FILES;

import com.google.api.codegen.ConfigProto;
import com.google.api.codegen.gapic.GapicGeneratorApp;
import com.google.api.tools.framework.model.Diag;
import com.google.api.tools.framework.model.ProtoFile;
import com.google.api.tools.framework.model.stages.Merged;
import com.google.api.tools.framework.tools.ToolDriverBase;
import com.google.api.tools.framework.tools.ToolOptions;
import com.google.api.tools.framework.tools.ToolOptions.Option;
import com.google.api.tools.framework.tools.ToolUtil;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnnotatorApp extends ToolDriverBase {
  public static final Option<String> OUTPUT_FILE =
      ToolOptions.createOption(
          String.class, "output_file", "The path of the output file to put generated config.", "");

  /** Constructs a config generator api based on given options. */
  public AnnotatorApp(ToolOptions options) {
    super(options);
  }

  @Override
  protected void process() throws Exception {
    model.establishStage(Merged.KEY);
    if (model.getDiagReporter().getDiagCollector().getErrorCount() > 0) {
      for (Diag diag : model.getDiagReporter().getDiagCollector().getDiags()) {
        System.err.println(diag.toString());
      }
      return;
    }

    String outputPath = options.get(OUTPUT_FILE);

    List<String> configFileNames = options.get(GENERATOR_CONFIG_FILES);
    ConfigProto configProto = GapicGeneratorApp.parseConfigProto(model, configFileNames);
    model.establishStage(Merged.KEY);

    Map<String, String> outputFiles = annotate(outputPath, configProto);
    ToolUtil.writeFiles(outputFiles, "");
  }

  private Map<String, String> annotate(String outputPath, ConfigProto configProto)
      throws IOException {

    GapicConfigToProtoAnnotator annotator =
        new GapicConfigToProtoAnnotator(
            model.getFiles().stream().map(ProtoFile::getProto).collect(Collectors.toList()),
            configProto);

    ImmutableMap.Builder<String, String> outputFiles = ImmutableMap.builder();
    outputFiles.put(
        outputPath + "_gapic_v2.yaml", annotator.annotateFiles().configProtoV2().toString());

    for (FileDescriptorProto fileDescriptor : annotator.annotateFiles().annotatedProtoFiles()) {
      ByteArrayOutputStream stringBuffer = new ByteArrayOutputStream();
      fileDescriptor.writeDelimitedTo(stringBuffer);
      outputFiles.put(outputPath + fileDescriptor.getName(), stringBuffer.toString());
    }
    outputFiles.put(
        outputPath + "_gapic_v2.yaml", annotator.annotateFiles().configProtoV2().toString());

    return outputFiles.build();
  }
}
