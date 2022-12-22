# Redis Reactive Cache  as Library

## Description

This Redis Reactive Cache library brings reactive cache functionality to  Spring Boot project<br/> 
It is self Auto Configurable, all you need is to import it as dependency.

```
This library provides 2 annotations:
1) @CacheAsideRead` - get the  stored value  for teh given Key .
2) @CacheAsideWrite` - set the cache value for the given Key.

```
All of those annotations has 1 arguments:
* key - cache key, either String or evaluated expressions started with `#` (see in usage examples)

```
## Usage Example:

java File <br/> 

@Service
public class ProductServiceImpl implements ProductService {

  @Override
  @CacheAsideRead(key = "#id")
  public Mono<Booking> cacheAsideRead(String id) {
    return this.cacheAsideRead(id);
  }


  @Override
  @CacheAsideWrite(key =  "#productDto.id")
  public Mono<Void> cacheAsideWrite(ProductDto productDto) {
    return Mono.empty().then();

  }
  
```


```
Redis connection properties are defined in the  Calling project as shown  in the yml <br/>
redis:
  ssl: true
  host: 127.0.0.1
  port: 6379
  cache-name: "cache-test"
  cache-ttl: 1
  
```  
``` 
Include  the Version of the Library in the Calling projecta as shown <br/>

    <dependency>
        <groupId>net.apmoller.crb.telikos</groupId>
        <artifactId>redis-reactive-cache-library</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>



```

