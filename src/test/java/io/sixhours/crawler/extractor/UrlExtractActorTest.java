package io.sixhours.crawler.extractor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import io.sixhours.crawler.extractor.UrlExtractActor.ExtractUrls;
import io.sixhours.crawler.extractor.UrlExtractActor.UrlExtractError;
import io.sixhours.crawler.extractor.UrlExtractActor.UrlsExtracted;
import java.util.Collections;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UrlExtractActorTest {

  static ActorSystem system;

  @Mock
  private UrlExtractor urlExtractor;

  @BeforeClass
  public static void setUpClass() {
    system = ActorSystem.create();
  }

  @AfterClass
  public static void tearDownClass() {
    TestKit.shutdownActorSystem(system);
    system = null;
  }

  @Test
  public void givenUrl_whenUrlExtract_thenReturnExtractedUrls() throws Exception {
    when(urlExtractor.extractUrls(any(String.class), any(String.class)))
        .thenReturn(new UrlExtractResult(Collections.emptySet()));

    TestKit probe = new TestKit(system);
    ActorRef urlExtractorActor = system.actorOf(UrlExtractActor.props(urlExtractor));

    ExtractUrls extractUrlsMessage = new ExtractUrls("http://some.url", "/some/path",
        "http://some.base.uri");
    urlExtractorActor.tell(extractUrlsMessage, probe.getRef());

    UrlsExtracted response = probe.expectMsgClass(UrlsExtracted.class);

    verify(urlExtractor, times(1))
        .extractUrls(any(String.class), any(String.class));
    assertThat(response.getUrl()).isEqualTo("http://some.url");
    assertThat(response.getPath()).isEqualTo("/some/path");
    assertThat(response.getUrls()).isEqualTo(Collections.emptySet());
  }

  @Test
  public void givenNotExistingUrl_whenDownloadFile_thenFailWithException() throws Exception {
    when(urlExtractor.extractUrls(any(String.class), any(String.class)))
        .thenThrow(new UrlExtractException("error"));

    TestKit probe = new TestKit(system);
    ActorRef urlExtractorActor = system.actorOf(UrlExtractActor.props(urlExtractor));

    ExtractUrls extractUrlsMessage = new ExtractUrls("http://some.url", "/some/path",
        "http://some.base.uri");
    urlExtractorActor.tell(extractUrlsMessage, probe.getRef());

    UrlExtractError response = probe.expectMsgClass(UrlExtractError.class);

    verify(urlExtractor, times(1))
        .extractUrls(any(String.class), any(String.class));
    assertThat(response.getUrl()).isEqualTo("http://some.url");
  }
}