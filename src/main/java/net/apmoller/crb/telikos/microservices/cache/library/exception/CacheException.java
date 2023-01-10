package net.apmoller.crb.telikos.microservices.cache.library.exception;


/**
 * This class  is used to handle the Exception that occurs during Redis caching
 */

public class CacheException extends RuntimeException{

    /*
    message string
   */
   String str;

    public CacheException(String str){
        super(str);
        this.str = str;
    }
}
