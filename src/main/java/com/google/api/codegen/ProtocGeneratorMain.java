package com.google.api.codegen;


import com.google.api.codegen.common.CodeGenerator;
import com.google.api.codegen.gapic.GapicGenerator;
import com.google.api.codegen.gapic.GapicGeneratorApp;
import com.google.api.codegen.util.InputFileUtil;
import com.google.api.tools.framework.tools.ToolOptions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.compiler.PluginProtos;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/** Entrypoint for protoc-invoked generation. */
public class ProtocGeneratorMain {
  public static void main(String[] args) throws IOException {

    PluginProtos.CodeGeneratorRequest request = PluginProtos.CodeGeneratorRequest.
        parseFrom(System.in);
    PluginProtos.CodeGeneratorResponse response;
    try {
      Descriptor descriptor = request.getDescriptorForType();
      // Parse options.
      Options options = new Options();
      options.addOption("h", "help", false, "show usage");

      options.addOption(SERVICE_YAML_NONREQUIRED_OPTION);
      // TODO make required after artman passes this in
      options.addOption(LANGUAGE_NONREQUIRED_OPTION);
      options.addOption(GAPIC_YAML_NONREQUIRED_OPTION);
      options.addOption(PACKAGE_YAML2_OPTION);
      options.addOption(TARGET_API_PROTO_PACKAGE);
      options.addOption(OUTPUT_OPTION);
      Option enabledArtifactsOption =
          Option.builder()
              .longOpt("enabled_artifacts")
              .desc(
                  "Optional. Artifacts enabled for the generator. "
                      + "Currently supports 'surface' and 'test'.")
              .hasArg()
              .argName("ENABLED_ARTIFACTS")
              .required(false)
              .build();
      options.addOption(enabledArtifactsOption);

      Option devSamplesOption =
          Option.builder()
              .longOpt("dev_samples")
              .desc("Whether to generate samples in non-production-ready languages.")
              .argName("DEV_SAMPLES")
              .required(false)
              .build();
      options.addOption(devSamplesOption);

      CommandLine cl = (new DefaultParser()).parse(options, args);
      if (cl.hasOption("help")) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("GapicGeneratorTool", options);
      }

      ToolOptions toolOptions = ToolOptions.create();


      // TODO(andrealin): Write system tests to ensure at least one option given.
      checkAtLeastOneOption(cl, SERVICE_YAML_NONREQUIRED_OPTION, TARGET_API_PROTO_PACKAGE);
      checkAtLeastOneOption(cl, GAPIC_YAML_NONREQUIRED_OPTION, TARGET_API_PROTO_PACKAGE);

      toolOptions.set(
          GapicGeneratorApp.PROTO_PACKAGE, cl.getOptionValue(TARGET_API_PROTO_PACKAGE.getLongOpt()));
      toolOptions.set(
          GapicGeneratorApp.LANGUAGE, cl.getOptionValue(LANGUAGE_NONREQUIRED_OPTION.getLongOpt()));
      toolOptions.set(
          GapicGeneratorApp.OUTPUT_FILE, cl.getOptionValue(OUTPUT_OPTION.getLongOpt(), ""));
      toolOptions.set(
          GapicGeneratorApp.PACKAGE_CONFIG2_FILE,
          cl.getOptionValue(PACKAGE_YAML2_OPTION.getLongOpt()));


      if (cl.getOptionValues(SERVICE_YAML_NONREQUIRED_OPTION.getLongOpt()) != null) {
        toolOptions.set(
            ToolOptions.CONFIG_FILES,
            Lists.newArrayList(cl.getOptionValues(SERVICE_YAML_NONREQUIRED_OPTION.getLongOpt())));
        InputFileUtil.checkFiles(toolOptions.get(ToolOptions.CONFIG_FILES));
      }
      if (cl.getOptionValues(GAPIC_YAML_NONREQUIRED_OPTION.getLongOpt()) != null) {
        toolOptions.set(
            GapicGeneratorApp.GENERATOR_CONFIG_FILES,
            Lists.newArrayList(cl.getOptionValues(GAPIC_YAML_NONREQUIRED_OPTION.getLongOpt())));
        InputFileUtil.checkFiles(toolOptions.get(GapicGeneratorApp.GENERATOR_CONFIG_FILES));
      }
      if (!Strings.isNullOrEmpty(toolOptions.get(GapicGeneratorApp.PACKAGE_CONFIG2_FILE))) {
        InputFileUtil.checkFile(toolOptions.get(GapicGeneratorApp.PACKAGE_CONFIG2_FILE));
      }

      if (cl.getOptionValues(enabledArtifactsOption.getLongOpt()) != null) {
        toolOptions.set(
            GapicGeneratorApp.ENABLED_ARTIFACTS,
            Lists.newArrayList(cl.getOptionValues(enabledArtifactsOption.getLongOpt())));
      }

      toolOptions.set(GapicGeneratorApp.DEV_SAMPLES, cl.hasOption(devSamplesOption.getLongOpt()));

      GapicGeneratorApp codeGen = new GapicGeneratorApp(toolOptions, artifactType);
      int exitCode = codeGen.run();
      System.exit(exitCode);
      response =
    } catch (Exception e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      pw.flush();
      PluginProtos.CodeGeneratorResponse.newBuilder().setError(sw.toString()).
          build().writeTo(System.out);
      System.out.flush();
      return;
    }
    response.writeTo(System.out);
    System.out.flush();
  }

}
