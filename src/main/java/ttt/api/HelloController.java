package ttt.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Backend is alive 👋";
    }
    @GetMapping("/health")
    public String health() { return "ok"; }
}


