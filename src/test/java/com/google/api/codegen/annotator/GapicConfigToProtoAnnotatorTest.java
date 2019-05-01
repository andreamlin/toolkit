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

import static com.google.api.codegen.annotator.GapicConfigToProtoAnnotator.CLIENT_PROTO_IMPORT;

import com.google.api.codegen.CodegenTestUtil;
import com.google.api.codegen.ConfigProto;
import com.google.api.codegen.MixedPathTestDataLocator;
import com.google.api.codegen.gapic.GapicGeneratorApp;
import com.google.api.codegen.util.MultiYamlReader;
import com.google.api.tools.framework.model.BoundedDiagCollector;
import com.google.api.tools.framework.model.DiagCollector;
import com.google.api.tools.framework.model.Model;
import com.google.api.tools.framework.model.ProtoFile;
import com.google.api.tools.framework.model.testing.TestDataLocator;
import com.google.api.tools.framework.tools.ToolUtil;
import com.google.common.truth.Truth;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class GapicConfigToProtoAnnotatorTest {

  private final static TestDataLocator testDataLocator = MixedPathTestDataLocator.create(GapicConfigToProtoAnnotatorTest.class);
  private static String[] protoFiles = {"library.proto", "another_service.proto"};

  @ClassRule
  public static TemporaryFolder tempDir = new TemporaryFolder();
  private static Model model;
  private static ProtoFile libraryProtoFile;
  private static ProtoFile anotherServiceProtoFile;
  private static final DiagCollector diagCollector = new BoundedDiagCollector();

  private static GapicConfigToProtoAnnotator annotator; // Object under test.

  @BeforeClass
  public static void setup() throws Exception {

    testDataLocator.addTestDataSource(CodegenTestUtil.class, "testsrc/common");

    model =
        CodegenTestUtil.readModel(
            testDataLocator, tempDir, protoFiles, new String[] {"library.yaml"});

    libraryProtoFile =
        model
            .getFiles()
            .stream()
            .filter(f -> f.getSimpleName().equals("library.proto"))
            .findFirst()
            .get();

    anotherServiceProtoFile =
        model
            .getFiles()
            .stream()
            .filter(f -> f.getSimpleName().equals("another_service.proto"))
            .findFirst()
            .get();

    model.addRoot(libraryProtoFile);

    List<FileDescriptorProto> protoFileDescriptors =  model.getFiles().stream().map(ProtoFile::getProto).collect(
        Collectors.toList());


    URL libraryServicePath = testDataLocator.findTestData("library.yaml");
    URL libraryGapicPath = testDataLocator.findTestData("library_gapic.yaml");
    List<String> configFiles = new ArrayList<>();
    ConfigProto configProto = GapicGeneratorApp.parseConfigProto(model,
        Arrays.asList(libraryServicePath.getPath(), libraryGapicPath.getPath()));
    annotator = new GapicConfigToProtoAnnotator(
       protoFileDescriptors, configProto);
  }

  @Test
  public void testAnnotator() {
    AnnotatedOutput output = annotator.annotateFiles();
    Truth.assertThat(output.configProtoV2()).isNotNull();
    Truth.assertThat(output.annotatedProtoFiles().get(0).getDependencyList()).contains(CLIENT_PROTO_IMPORT);
  }
}
