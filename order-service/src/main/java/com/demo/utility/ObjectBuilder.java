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

/**
 * A static utility class for mapping DTOs (Data Transfer Objects) to
 * persistent Entities and vice-versa.
 * <p>
 * This class encapsulates the "object mother" or "builder" logic for
 * creating complex entities like {@link Order} from request objects.
 * This keeps the {@link com.demo.service.impl.OrderServiceImpl} clean
 * and focused on its transactional and orchestration logic.
 *
 * @see com.demo.service.impl.OrderServiceImpl
 */
public class ObjectBuilder {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ObjectBuilder() {}

    /**
     * Maps an {@link AddressDTO} to a new, un-persisted {@link Address} entity.
     *
     * @param addressDTO The source DTO containing address information.
     * @return A new {@link Address} entity.
     */
    public static Address address(AddressDTO addressDTO) {
        Address address = new Address();
        address.setAddress(addressDTO.getAddress());
        address.setPostalCode(addressDTO.getPostalCode());
        address.setCity(addressDTO.getCity());
        address.setCountry(addressDTO.getCountry());
        return address;
    }

    /**
     * Builds a new, un-persisted {@link Order} entity from a
     * {@link CreateOrderRequest} and a persisted {@link Address}.
     * <p>
     * This method is responsible for:
     * <ul>
     * <li>Setting all primitive fields from the request.</li>
     * <li>Setting the initial {@link Status#PLACED}.</li>
     * <li>Associating the persisted shipping address.</li>
     * <li>Building the list of child {@link OrderProduct} entities and
     * setting their back-reference to the parent {@link Order}.</li>
     * </ul>
     *
     * @param request           The source DTO containing all order details.
     * @param shippingAddress The *already persisted* {@link Address} entity to link.
     * @return A new {@link Order} entity, ready to be saved.
     */
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

    /**
     * Maps a persisted {@link Order} entity to an {@link OrderCreatedResponse} DTO.
     * <p>
     * This is used to build the API response after an order is successfully
     * created. It maps all fields from the entity, including its child
     * {@link OrderProduct} list and the associated {@link Address}.
     *
     * @param order The persisted {@link Order} entity (fresh from the database).
     * @return A new {@link OrderCreatedResponse} DTO.
     */
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
