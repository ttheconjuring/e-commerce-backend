package com.demo.utility;

import com.demo.common.dto.AddressDTO;
import com.demo.common.dto.OrderProductDTO;
import com.demo.model.Address;
import com.demo.model.Order;
import com.demo.model.OrderProduct;
import com.demo.model.Status;
import com.demo.model.dto.CreateOrderRequest;
import com.demo.model.dto.OrderCreatedResponse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ObjectBuilder {

    private ObjectBuilder() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    public static Address address(AddressDTO addressDTO) {
        Address address = new Address();
        address.setAddress(addressDTO.getAddress());
        address.setPostalCode(addressDTO.getPostalCode());
        address.setCity(addressDTO.getCity());
        address.setCountry(addressDTO.getCountry());
        return address;
    }

    public static Order order(CreateOrderRequest request, Address shippingAddress) {
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setStatus(Status.PLACED);
        order.setCancellationReason(null);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        order.setShippingAddress(shippingAddress);
        order.setTotalAmount(request.getTotalAmount());
        order.setCurrency(request.getCurrency());
        order.setPaymentMethodId(request.getPaymentMethodId());
        order.setCarrier(request.getCarrier());
        List<OrderProductDTO> dtoProducts = request.getProducts();
        List<OrderProduct> products = new ArrayList<>();
        for (OrderProductDTO dtoProduct : dtoProducts) {
            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setOrder(order);
            orderProduct.setProductId(dtoProduct.getProductId());
            orderProduct.setQuantity(dtoProduct.getQuantity());
            orderProduct.setPricePerUnit(dtoProduct.getPricePerUnit());
            products.add(orderProduct);
        }
        order.setProducts(products);
        return order;
    }

    public static OrderCreatedResponse orderCreatedResponse(Order order) {
        OrderCreatedResponse orderCreatedResponse = new OrderCreatedResponse();
        orderCreatedResponse.setOrderId(order.getId());
        orderCreatedResponse.setCustomerId(order.getCustomerId());
        orderCreatedResponse.setStatus(order.getStatus().toString());
        orderCreatedResponse.setCreatedAt(order.getCreatedAt());
        orderCreatedResponse.setUpdatedAt(order.getUpdatedAt());
        // Map the list of child OrderProduct entities to DTOs
        orderCreatedResponse.setProducts(order.getProducts().stream().map(product -> {
            OrderProductDTO orderProductDTO = new OrderProductDTO();
            orderProductDTO.setProductId(product.getProductId());
            orderProductDTO.setQuantity(product.getQuantity());
            orderProductDTO.setPricePerUnit(product.getPricePerUnit());
            return orderProductDTO;
        }).collect(Collectors.toList()));
        // Map the associated Address entity to its DTO
        AddressDTO shippingAddressDTO = new AddressDTO();
        shippingAddressDTO.setAddress(order.getShippingAddress().getAddress());
        shippingAddressDTO.setPostalCode(order.getShippingAddress().getPostalCode());
        shippingAddressDTO.setCity(order.getShippingAddress().getCity());
        shippingAddressDTO.setCountry(order.getShippingAddress().getCountry());
        orderCreatedResponse.setShippingAddress(shippingAddressDTO);
        // Map remaining fields
        orderCreatedResponse.setTotalAmount(order.getTotalAmount());
        orderCreatedResponse.setCurrency(order.getCurrency());
        orderCreatedResponse.setPaymentMethodId(order.getPaymentMethodId());
        orderCreatedResponse.setCarrier(order.getCarrier());
        return orderCreatedResponse;
    }

}
