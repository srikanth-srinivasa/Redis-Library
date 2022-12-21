package net.apmoller.crb.telikos.microservices.cache.library.exception;

public class CacheException extends RuntimeException{

    String str;

    public CacheException(String str){
        super(str);
        this.str = str;
    }
}
