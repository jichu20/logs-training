package com.jichu20.loggerclient.service;

import com.jichu20.loggerlib.dto.BookDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BookServiceImpl implements BookService {

    private RestTemplate restTemplate;

    @Autowired
    public BookServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public BookDto getBook(String bookName) {

        ResponseEntity<BookDto> book = restTemplate.getForEntity("http://localhost:8081/book/elbarcodelpirata", BookDto.class);

        return book.getBody();

    }
}
