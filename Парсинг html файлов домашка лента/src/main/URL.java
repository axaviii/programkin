package main;

public enum URL {
  LENTA("https://lenta.ru/"),
  PIKABU("https://pikabu.ru/"),
  RIA("https://ria.ru/");

  private final String url;

  URL(String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }
}
