package net.apmoller.crb.telikos.microservices.cache.library.repository;


import net.apmoller.crb.telikos.microservices.cache.library.entity.Booking;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BookingRepository extends ReactiveCrudRepository<Booking, String> {

    Mono<Booking> findById(String id);

    @Query("select * from booking where price >= $1")
    Flux<Booking> findByPrice(String  price);


}
