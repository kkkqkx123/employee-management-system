package com.example.demo.payroll.exception;

import com.example.demo.common.exception.BusinessException;

public class PayrollPeriodException extends BusinessException {
    public PayrollPeriodException(String message) {
        super(message);
    }
    
    public PayrollPeriodException(String message, Throwable cause) {
        super(message, cause);
    }
}