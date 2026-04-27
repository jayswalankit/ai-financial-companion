package com.aifinance.financialcompanion.auth.exceptions;

public class EmailAlreadyExistException extends  RuntimeException{

    public EmailAlreadyExistException(String message){
        super(message);
    }

}
