workspace(name = "com_google_api_codegen")

load(
    "//:repositories.bzl",
    "com_google_api_codegen_repositories",
    "com_google_api_codegen_test_repositories",
    "com_google_api_codegen_tools_repositories",
)
load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

com_google_api_codegen_repositories()

com_google_api_codegen_test_repositories()

com_google_api_codegen_tools_repositories()

#
# protoc-java-resource-names-plugin repository dependencies (required to support resource names
# feature in gapic generator)
#
git_repository(
    name = "com_google_protoc_java_resource_names_plugin",
    commit = "a1ad58ad508cfb9463d061f57f99f728eb72cfa3",
    remote = "https://github.com/googleapis/protoc-java-resource-names-plugin.git",
)

load(
    "@com_google_protoc_java_resource_names_plugin//:repositories.bzl",
    "com_google_protoc_java_resource_names_plugin_repositories",
)

com_google_protoc_java_resource_names_plugin_repositories(omit_com_google_protobuf = True)
