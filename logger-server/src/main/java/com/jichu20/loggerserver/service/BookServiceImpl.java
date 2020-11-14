package com.jichu20.loggerserver.service;

import com.jichu20.loggerlib.dto.BookDto;

import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService {

    public BookDto getBook(String bookName) {

        return new BookDto(bookName, "resume of the book", 2);

    }
}
