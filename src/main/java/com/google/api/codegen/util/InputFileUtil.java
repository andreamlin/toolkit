package com.google.api.codegen.util;

import java.io.File;
import java.util.List;

public class InputFileUtil {

  public static void checkFiles(List<String> files) {
    for (String filePath : files) {
      checkFile(filePath);
    }
  }

  public static void checkFile(String filePath) {
    if (!new File(filePath).exists()) {
      throw new IllegalArgumentException("File not found: " + filePath);
    }
  }
}
