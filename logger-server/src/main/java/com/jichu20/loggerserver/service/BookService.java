package com.jichu20.loggerserver.service;

import com.jichu20.loggerlib.dto.BookDto;

public interface BookService {

    public BookDto getBook(String bookName);

}