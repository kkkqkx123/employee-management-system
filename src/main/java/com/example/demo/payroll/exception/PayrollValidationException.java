package com.example.demo.payroll.exception;

import com.example.demo.common.exception.ValidationException;

public class PayrollValidationException extends ValidationException {
    public PayrollValidationException(String message) {
        super(message);
    }
    
    public PayrollValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}