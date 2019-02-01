package com.google.api.codegen.gapic;

import com.google.api.codegen.CodegenTestUtil;
import com.google.api.codegen.ProtocGeneratorMain;
import com.google.api.codegen.protoannotations.GapicCodeGeneratorAnnotationsTest;
import com.google.api.tools.framework.model.Model;
import com.google.api.tools.framework.model.ProtoFile;
import com.google.api.tools.framework.model.testing.TestDataLocator;
import com.google.common.truth.Truth;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Collectors;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


public class ProtocGapicPluginGeneratorTest {

  private static String[] protoFiles = {"multiple_services.proto"};
  private static TestDataLocator testDataLocator;
  private static Model model;
  @ClassRule
  public static TemporaryFolder tempDir = new TemporaryFolder();

  @BeforeClass
  public static void startUp() {
    testDataLocator = TestDataLocator.create(GapicCodeGeneratorAnnotationsTest.class);
    testDataLocator.addTestDataSource(CodegenTestUtil.class, "testsrc/common");

    model =
        CodegenTestUtil.readModel(
            testDataLocator, tempDir, protoFiles, new String[]{});
  }

    @Test
  public void testGenerator() throws IOException {
    CodeGeneratorRequest codeGeneratorRequest = CodeGeneratorRequest
        .newBuilder()
        // All proto files, including dependencies
        .addAllProtoFile(model.getFiles().stream().map(ProtoFile::getProto).collect(Collectors.toList()))
        // Only the file to generate a client for (don't generate dependencies)
        .addFileToGenerate("multiple_services.proto")
        .setParameter("language=java")
        .build();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    codeGeneratorRequest.writeTo(outputStream);
    InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

    CodeGeneratorResponse response = ProtocGeneratorMain.generate(inputStream);

    // TODO(andrealin): Look into setting these up as baseline files.
    Truth.assertThat(response).isNotNull();
    Truth.assertThat(response.getFileCount()).isEqualTo(15);
    Truth.assertThat(response.getFile(0).getContent()).contains("DecrementerServiceClient");
  }
}
