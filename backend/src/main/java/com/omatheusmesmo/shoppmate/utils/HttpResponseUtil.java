package com.omatheusmesmo.shoppmate.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HttpResponseUtil {

    public static <T> ResponseEntity<T> ok(T body){
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    public static <T> ResponseEntity<T> created(T body){
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    public static ResponseEntity<Void> noContent(){
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public static <T> ResponseEntity<T> badRequest(T body){
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    public static <T> ResponseEntity<T> notFound(){
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    public static <T> ResponseEntity<T> internalServerError(){
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
