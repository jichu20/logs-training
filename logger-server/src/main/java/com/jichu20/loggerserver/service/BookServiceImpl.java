package com.jichu20.loggerserver.service;

import com.jichu20.loggerlib.dto.BookDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BookServiceImpl implements BookService {

    private RestTemplate restTemplate;

    public BookDto getBook(String bookName) {

        return new BookDto(bookName, "resume", 2);

    }
}
