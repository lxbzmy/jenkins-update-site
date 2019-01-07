package cn.devit.tools.jenkins;

import java.io.FileNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(annotations = Controller.class)
public class ExceptionMapping {

    @ExceptionHandler(value = FileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    void exceptionHa() {
    }
}
