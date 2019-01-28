workspace(name = "com_google_api_codegen")

load(
    "//:repositories.bzl",
    "com_google_api_codegen_repositories",
    "com_google_api_codegen_test_repositories",
    "com_google_api_codegen_tools_repositories",
)

http_archive(
    name = "com_google_protobuf",
    sha256 = "983975ab66113cbaabea4b8ec9f3a73406d89ed74db9ae75c74888e685f956f8",
    strip_prefix = "protobuf-66dc42d891a4fc8e9190c524fd67961688a37bbe",
    url = "https://github.com/google/protobuf/archive/66dc42d891a4fc8e9190c524fd67961688a37bbe.tar.gz",
)

com_google_api_codegen_repositories()

com_google_api_codegen_test_repositories()

com_google_api_codegen_tools_repositories()

#
# protoc-java-resource-names-plugin repository dependencies (required to support resource names
# feature in gapic generator)
#
git_repository(
    name = "com_google_protoc_java_resource_names_plugin",
    remote = "https://github.com/googleapis/protoc-java-resource-names-plugin.git",
    commit = "a1ad58ad508cfb9463d061f57f99f728eb72cfa3",
)

load(
    "@com_google_protoc_java_resource_names_plugin//:repositories.bzl",
    "com_google_protoc_java_resource_names_plugin_repositories",
)

com_google_protoc_java_resource_names_plugin_repositories(omit_com_google_protobuf = True)
