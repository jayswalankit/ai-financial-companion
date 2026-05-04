package com.aifinance.financialcompanion.exceptions;

public class EmailAlreadyExistException extends  RuntimeException{

    public EmailAlreadyExistException(String message){
        super(message);
    }

}
