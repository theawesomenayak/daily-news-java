package helloworld;

public final class NewsItem {

  private String title;
  private String pubDate;

  public String getTitle() {

    return title;
  }

  public void setTitle(final String title) {

    this.title = title;
  }

  public String getPubDate() {

    return pubDate;
  }

  public void setPubDate(final String pubDate) {

    this.pubDate = pubDate;
  }

  @Override
  public String toString() {

    return String.format("{\"title\": \"%s\", \"date\": \"%s\"}", this.title, this.pubDate);
  }
}
