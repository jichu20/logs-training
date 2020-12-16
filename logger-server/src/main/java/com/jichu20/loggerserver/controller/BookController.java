package com.jichu20.loggerserver.controller;

import com.jichu20.loggerserver.service.BookService;

import com.jichu20.commons.dto.BookDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/book")
public class BookController {

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    @Autowired
    BookService bookService;

    @GetMapping("/{bookName}")
    public ResponseEntity<BookDto> getBook(@PathVariable("bookName") String bookName) throws InterruptedException {

        logger.info("Hello Sleuth - server");
        return new ResponseEntity<BookDto>(bookService.getBook(bookName), HttpStatus.ACCEPTED);

    }

}
