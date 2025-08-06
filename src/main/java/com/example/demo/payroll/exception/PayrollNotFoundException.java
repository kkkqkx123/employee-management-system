package com.example.demo.payroll.exception;

import com.example.demo.common.exception.BusinessException;

public class PayrollNotFoundException extends BusinessException {
    public PayrollNotFoundException(String message) {
        super(message);
    }
    
    public PayrollNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}