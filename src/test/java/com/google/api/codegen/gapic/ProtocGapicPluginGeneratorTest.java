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

  private static ProtoFile libraryProtoFile;

  @BeforeClass
  public static void startUp() {
    // Load and parse protofile.

    testDataLocator = TestDataLocator.create(GapicCodeGeneratorAnnotationsTest.class);
    testDataLocator.addTestDataSource(CodegenTestUtil.class, "testsrc/common");

    model =
        CodegenTestUtil.readModel(
            testDataLocator, tempDir, protoFiles, new String[]{});

    libraryProtoFile =
        model
            .getFiles()
            .stream()
            .filter(f -> f.getSimpleName().equals("multiple_services.proto"))
            .findFirst()
            .get();
  }

    @Test
  public void testGenerator() throws IOException {
    CodeGeneratorRequest codeGeneratorRequest = CodeGeneratorRequest
        .newBuilder()
        .addProtoFile(libraryProtoFile.getProto())
        .setParameter("language=java")
        .build();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    codeGeneratorRequest.writeTo(outputStream);
    InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

    CodeGeneratorResponse response = ProtocGeneratorMain.generate(inputStream);

    Truth.assertThat(response).isNotNull();
    Truth.assertThat(response.getFileCount()).isGreaterThan(0);
  }
}
