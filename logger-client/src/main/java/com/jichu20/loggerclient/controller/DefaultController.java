package com.jichu20.loggerclient.controller;

import com.jichu20.loggerclient.service.BookService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DefaultController {

    private static final Logger logger = LoggerFactory.getLogger(DefaultController.class);

    @Autowired
    BookService bookService;

    @GetMapping("/")
    public ResponseEntity<String> getBook() throws InterruptedException {

        logger.info("Hello Sleuth");
        bookService.getBook("bookName");
        return new ResponseEntity<String>("Arriba", HttpStatus.ACCEPTED);

    }

}
