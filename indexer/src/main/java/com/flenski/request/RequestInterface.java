package com.flenski.request;

import java.net.http.HttpRequest;

public interface RequestInterface {
    HttpRequest build();
}
