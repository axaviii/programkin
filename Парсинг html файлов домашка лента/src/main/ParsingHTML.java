package main;

import java.io.IOException;

public class ParsingHTML {
  public static void main(String[] args) {
    try {
      Parser parser = new Parser();
      parser.parsingHTML(URL.RIA.getUrl());
    } catch (IOException ioException) {
      ioException.printStackTrace();
    }
  }
}
