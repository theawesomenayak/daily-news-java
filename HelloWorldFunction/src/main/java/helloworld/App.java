package helloworld;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Handler for requests to Lambda function.
 */
public class App implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  private static final String NEWS_URL = "https://news.google.com/news/rss";

  private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
      .version(Version.HTTP_2)
      .followRedirects(HttpClient.Redirect.NORMAL)
      .connectTimeout(Duration.ofSeconds(10))
      .build();

  public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input,
      final Context context) {

    final Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");
    headers.put("X-Custom-Header", "application/json");

    final APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
        .withHeaders(headers);
    try {
      final String output = getNewsFromGoogle();
      return response.withStatusCode(200).withBody(output);
    } catch (final IOException | InterruptedException | XMLStreamException e) {
      return response.withBody("{}").withStatusCode(500);
    }
  }

  private String getNewsFromGoogle()
      throws IOException, InterruptedException, XMLStreamException {

    final HttpRequest request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(NEWS_URL))
        .setHeader("User-Agent", "Daily News Lambda")
        .build();

    final HttpResponse<String> response = HTTP_CLIENT
        .send(request, HttpResponse.BodyHandlers.ofString());

    return rssToNewsItems(response.body()).toString();
  }

  private List<NewsItem> rssToNewsItems(final String rssFeed)
      throws XMLStreamException {

    final List<NewsItem> newsItems = new ArrayList<>();
    final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    final InputStream inputStream = new ByteArrayInputStream(rssFeed.getBytes());
    final XMLEventReader reader = inputFactory.createXMLEventReader(inputStream);
    NewsItem newsItem = null;
    while (reader.hasNext()) {
      XMLEvent nextEvent = reader.nextEvent();
      if (nextEvent.isStartElement()) {
        final StartElement startElement = nextEvent.asStartElement();
        switch (startElement.getName().getLocalPart()) {
          case "item":
            newsItem = new NewsItem();
            break;
          case "title":
            nextEvent = reader.nextEvent();
            setTitle(nextEvent, newsItem);
            break;
          case "pubDate":
            nextEvent = reader.nextEvent();
            setPubDate(nextEvent, newsItem);
            break;
          default:
            break;
        }
      } else if (nextEvent.isEndElement()) {
        final EndElement endElement = nextEvent.asEndElement();
        if (endElement.getName().getLocalPart().equals("item")) {
          newsItems.add(newsItem);
        }
      }
    }
    return newsItems;
  }

  private void setTitle(final XMLEvent xmlEvent, final NewsItem newsItem) {

    if (null != newsItem) {
      newsItem.setTitle(xmlEvent.asCharacters().getData());
    }
  }

  private void setPubDate(final XMLEvent xmlEvent, final NewsItem newsItem) {

    if (null != newsItem) {
      newsItem.setPubDate(xmlEvent.asCharacters().getData());
    }
  }
}
