package com.bobocode.nasapictures.service;

import com.bobocode.nasapictures.model.Picture;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;


@Service
public class NasaPictureService {

    private static final String NASA_KEY_VALUE = "eBQUKf4CJvXCb4sKcVWYW0uQT4lKmufdNDPsLwT2";
    private static final String NASA_KEY_NAME = "api_key";
    private static final String SOL_NAME = "sol";
    private static final String NASA_URL = "https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos";

    public static final String PHOTOS_FIELD_NAME = "photos";
    public static final String IMG_SRC_FIELD_NAME = "img_src";


    public byte[] getLargestImage(int sol) {

        RestTemplate restTemplate = new RestTemplate();

        String url = UriComponentsBuilder.fromHttpUrl(NASA_URL)
                .queryParam(NASA_KEY_NAME, NASA_KEY_VALUE)
                .queryParam(SOL_NAME, sol)
                .toUriString();

        JsonNode fullJson = restTemplate.getForObject(url, JsonNode.class);

        assert fullJson != null;

        Optional<Picture> largestPicture = StreamSupport.stream(fullJson.get(PHOTOS_FIELD_NAME).spliterator(), false)
                .map(image -> image.get(IMG_SRC_FIELD_NAME))
                .map(JsonNode::asText)
                .map(imageSrc -> {
                    ResponseEntity<String> response = restTemplate.getForEntity(imageSrc, String.class);
                    return getOriginOrRedirectedUrl(imageSrc, response);
                })
                .map(redirect -> {
                    HttpHeaders headers = restTemplate.headForHeaders(redirect);
                    long contentLength = headers.getContentLength();
                    return new Picture(redirect, contentLength);
                })
                .max(Comparator.comparing(Picture::contentLength));

        return restTemplate.getForObject(largestPicture.orElseThrow().url(), byte[].class);
    }

    private static String getOriginOrRedirectedUrl(String imageSrc, ResponseEntity<String> response) {
        if (response.getStatusCode().is3xxRedirection()) {
            return Objects.requireNonNull(response.getHeaders().getLocation()).toString();
        } else {
            return imageSrc;
        }
    }
}


