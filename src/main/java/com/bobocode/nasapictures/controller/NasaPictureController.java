package com.bobocode.nasapictures.controller;

import com.bobocode.nasapictures.service.NasaPictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NasaPictureController {

    private final NasaPictureService service;

    @GetMapping(value = "/largestImage")
    public ResponseEntity<?> getLargestImage(@RequestParam int sol) {

        long start = System.currentTimeMillis();
        byte[] image = service.getLargestImage(sol);
        System.out.println(System.currentTimeMillis() - start + "ms");

        return ResponseEntity
                .status(HttpStatus.OK)
                .header("Content-Type", MediaType.IMAGE_JPEG_VALUE)
                .body(image);
    }
}
