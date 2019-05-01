package com.google.api.codegen.annotator;

import com.google.api.codegen.ConfigProto;
import com.google.api.codegen.util.ConfigVersionValidator;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import java.util.List;

public class GapicConfigToProtoAnnotator {

  public static final String CLIENT_PROTO_IMPORT = "google/api/client.proto";

  private final ImmutableList<FileDescriptorProto>
      startFileDescriptorProtos; // Input FileDescriptorProtos.
  private final ConfigProto gapicConfig;

  private AnnotatedOutput annotatedOutput;
  private com.google.api.codegen.v2.ConfigProto gapicConfigV2;

  public GapicConfigToProtoAnnotator(
      List<FileDescriptorProto> FileDescriptorProtos, ConfigProto gapicConfig) {
    this.startFileDescriptorProtos = ImmutableList.copyOf(FileDescriptorProtos);
    this.gapicConfig = gapicConfig;
  }

  public AnnotatedOutput annotateFiles() {
    if (annotatedOutput != null) return annotatedOutput;
    com.google.api.codegen.v2.ConfigProto.Builder configV2 = setUpConfigV2();
    ImmutableList.Builder<FileDescriptorProto> annotatedProtoFiles = ImmutableList.builder();

    for (FileDescriptorProto FileDescriptorProto : startFileDescriptorProtos) {
      annotatedProtoFiles.add(annotate(FileDescriptorProto, gapicConfig, configV2));
    }
    AnnotatedOutput.Builder result = AnnotatedOutput.newBuilder();
    result.annotatedProtoFiles(annotatedProtoFiles.build());
    result.configProtoV2(configV2.build());
    annotatedOutput = result.build();
    return annotatedOutput;
  }

  private com.google.api.codegen.v2.ConfigProto.Builder setUpConfigV2() {
    return com.google.api.codegen.v2.ConfigProto.newBuilder()
        .setConfigSchemaVersion(ConfigVersionValidator.CONFIG_V2_VERSION);
  }

  private FileDescriptorProto annotate(
      FileDescriptorProto input,
      ConfigProto gapicConfig,
      com.google.api.codegen.v2.ConfigProto.Builder configV2) {
    FileDescriptorProto.Builder protoFileBuilder = input.toBuilder();

    // This is just a test line.
    addProtoImport(protoFileBuilder, CLIENT_PROTO_IMPORT);

    // Required fields.

    // Method signatures.

    // Scopes.

    return protoFileBuilder.build();
  }

  private FileDescriptorProto.Builder addProtoImport(
      FileDescriptorProto.Builder input, String protoImport) {
    if (!input.getDependencyList().contains(protoImport)) {
      input.addPublicDependency(input.getDependencyCount());
      input.addDependency(protoImport);
    }
    return input;
  }
}
