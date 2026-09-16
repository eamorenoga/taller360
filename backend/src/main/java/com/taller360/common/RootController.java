package com.taller360.common;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {
  @GetMapping("/")
  Map<String, String> root() {
    return Map.of(
        "service", "Taller 360 API",
        "status", "ok",
        "docs", "/swagger-ui.html");
  }
}
