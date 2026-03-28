package com.projects.util.http;

import java.time.ZonedDateTime;
import java.util.List;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class HttpErrorInfo {
  private final ZonedDateTime timestamp;
  private final String path;
  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
  private final List<FieldErrorInfo> fieldErrors;

  public HttpErrorInfo() {
    timestamp = null;
    this.httpStatus = null;
    this.path = null;
    this.code = null;
    this.message = null;
    this.fieldErrors = null;
  }

  public HttpErrorInfo(HttpStatus httpStatus, String path, String code, String message) {
    this(httpStatus, path, code, message, List.of());
  }

  public HttpErrorInfo(HttpStatus httpStatus, String path, String code, String message, List<FieldErrorInfo> fieldErrors) {
    timestamp = ZonedDateTime.now();
    this.httpStatus = httpStatus;
    this.path = path;
    this.code = code;
    this.message = message;
    this.fieldErrors = fieldErrors == null ? List.of() : List.copyOf(fieldErrors);
  }

  public int getStatus() {
    return httpStatus.value();
  }

  public String getError() {
    return httpStatus.getReasonPhrase();
  }

  public String getHumanMessage() {
    return message;
  }
}
