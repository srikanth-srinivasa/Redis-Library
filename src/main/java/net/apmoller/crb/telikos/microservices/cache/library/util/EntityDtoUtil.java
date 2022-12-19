package net.apmoller.crb.telikos.microservices.cache.library.util;


import net.apmoller.crb.telikos.microservices.cache.library.dto.ProductDto;
import net.apmoller.crb.telikos.microservices.cache.library.entity.Booking;

public class EntityDtoUtil {

//    public static ProductDto toDto(Product product) {
//        ProductDto dto = new ProductDto();
//        //  dto.setId(product.getId());
//        dto.setPrice(product.getPrice());
//        dto.setDescription(product.getDescription());
//        return dto;
//    }

  /*  public static ProductDto toDtoProduct(ProductEntity product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setPrice(product.getPrice());
        dto.setDescription(product.getDescription());
        return dto;
    }*/

    public static Booking toEntity(ProductDto dto) {

        Booking  booking = new Booking();
        booking.setId(dto.getId());
        booking.setDescription(dto.getDescription());
        booking.setPrice(dto.getPrice());
        booking.setQtyavailable(dto.getPrice());

        return booking;



    }

}
