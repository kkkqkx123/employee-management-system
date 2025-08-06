package com.example.demo.payroll.exception;

import com.example.demo.common.exception.BusinessException;

public class PayrollCalculationException extends BusinessException {
    public PayrollCalculationException(String message) {
        super(message);
    }
    
    public PayrollCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}