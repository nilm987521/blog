package cc.nilm.blog.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @GetMapping("/test")
    public String testEndpoint() {
        return "Debug endpoint working!";
    }
    
    @GetMapping("/comments-test")
    public String testCommentsEndpoint() {
        try {
            return "Attempting to access comments endpoint...";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
