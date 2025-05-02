package za.co.api.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import za.co.api.gateway.service.LinkDecoderService;

import java.net.URI;
import java.util.Map;

@RestController
public class LinkApi {

    public static final String USER_SERVICE = "user-service";
    private final RestTemplate restTemplate;
    private final LinkDecoderService linkDecoderService;

    public LinkApi(LinkDecoderService linkDecoderService, RestTemplate restTemplate) {
        this.linkDecoderService = linkDecoderService;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/link")
    public ResponseEntity<?> activateAccount(@RequestParam("t") String token) {
        try {
            String targetUrl = linkDecoderService.decode(token); // Full decoded URL
            URI uri = URI.create(targetUrl); // Parse it to extract query params

            // Extract the JWT token from the query string
            String decodedToken = UriComponentsBuilder.fromUri(uri)
                    .build()
                    .getQueryParams()
                    .getFirst("token");

            // Now use the extracted JWT in the new URL
            String url = "http://" + USER_SERVICE + "/api/link/redirect/activate?t=" + decodedToken;
            restTemplate.getForEntity(url, Void.class);
            // TODO: Replace with actual frontend login page once it's ready
            URI redirectUri = URI.create("http://frontend-domain/back/to/login/");

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(redirectUri)
                    .build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Activation failed: " + e.getMessage()));
        }
    }

    private ResponseEntity<?> forwardToAuthService(String url, Object body) {
        try {
            ResponseEntity<?> response = restTemplate.postForEntity(url, body, Map.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (HttpStatusCodeException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of("error", ex.getResponseBodyAsString()));
        }
    }
}
