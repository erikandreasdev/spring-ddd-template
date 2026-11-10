package com.CHANGEME.shared.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.CHANGEME.shared.domain.DomainException;
import com.CHANGEME.shared.domain.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = ApiExceptionHandlerTest.FailingController.class)
@Import(ApiExceptionHandlerTest.FailingController.class)
class ApiExceptionHandlerTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void givenNotFoundException_whenHandled_thenNotFoundProblemIsReturned() {
    // when / then
    assertThat(mvc.post().uri("/test/not-found"))
        .hasStatus(HttpStatus.NOT_FOUND)
        .bodyJson()
        .extractingPath("$.detail")
        .asString()
        .isEqualTo("Thing 7 not found");
  }

  @Test
  void givenDomainException_whenHandled_thenConflictProblemIsReturned() {
    // when / then
    assertThat(mvc.post().uri("/test/conflict"))
        .hasStatus(HttpStatus.CONFLICT)
        .bodyJson()
        .extractingPath("$.detail")
        .asString()
        .isEqualTo("Thing 7 is locked");
  }

  @Test
  void givenIllegalArgumentException_whenHandled_thenBadRequestProblemIsReturned() {
    // when / then
    assertThat(mvc.post().uri("/test/bad-request"))
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .extractingPath("$.detail")
        .asString()
        .isEqualTo("Quantity must be positive");
  }

  @Test
  void givenUnexpectedException_whenHandled_thenGenericServerErrorProblemIsReturned() {
    // when / then
    assertThat(mvc.post().uri("/test/unexpected"))
        .hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        .bodyJson()
        .extractingPath("$.detail")
        .asString()
        .isEqualTo("Unexpected error");
  }

  @Test
  void givenMalformedJson_whenHandled_thenBadRequestProblemIsReturned() {
    // when / then
    assertThat(
            mvc.post().uri("/test/echo").contentType(MediaType.APPLICATION_JSON).content("{"))
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .extractingPath("$.status")
        .isEqualTo(400);
  }

  @RestController
  static class FailingController {

    @PostMapping("/test/not-found")
    void notFound() {
      throw new ThingNotFoundException();
    }

    @PostMapping("/test/conflict")
    void conflict() {
      throw new ThingLockedException();
    }

    @PostMapping("/test/bad-request")
    void badRequest() {
      throw new IllegalArgumentException("Quantity must be positive");
    }

    @PostMapping("/test/unexpected")
    void unexpected() {
      throw new IllegalStateException("boom");
    }

    @PostMapping("/test/echo")
    String echo(@RequestBody final Thing thing) {
      return thing.name();
    }
  }

  record Thing(String name) {}

  static final class ThingNotFoundException extends NotFoundException {
    ThingNotFoundException() {
      super("Thing 7 not found");
    }
  }

  static final class ThingLockedException extends DomainException {
    ThingLockedException() {
      super("Thing 7 is locked");
    }
  }
}
