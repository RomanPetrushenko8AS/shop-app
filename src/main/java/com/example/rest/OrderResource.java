package com.example.rest;

import com.example.JpaUtil;
import com.example.dto.OrderDTO;
import com.example.entity.Client;
import com.example.entity.Order;
import com.example.entity.Product;
import com.example.mapper.OrderMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Заказы", description = "Управление каталогом заказов")
public class OrderResource {
    private static final Logger logger = LoggerFactory.getLogger(OrderResource.class);
    @GET
    @Operation(summary = "Получить все заказы", description = "Возвращает информацию о заказах")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Заказы успешно найдены"),
            @ApiResponse(responseCode = "404", description = "Заказы не найдены")
    })
    public List<OrderDTO> getAll() {
        logger.info("Метод GET: получение всех заказов");
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Order> orders = em
                    .createNativeQuery("SELECT * FROM orders", Order.class)
                    .getResultList();
            logger.info("Все заказы получены. Количество заказов: {}",orders.size());
            return orders.stream()
                    .map(order -> OrderMapper.toDTO(order))
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Получить заказ по ID", description = "Возвращает информацию о заказе по его id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Заказ успешно найден"),
            @ApiResponse(responseCode = "404", description = "Заказ с указанным ID не найден")
    })
    public Response getById(@PathParam("id") int id) {
        logger.info("Метод GET: получение заказа с id = {}",id);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Order> result = em
                    .createNativeQuery("SELECT * FROM orders WHERE id = :id", Order.class)
                    .setParameter("id", id)
                    .getResultList();

            if (result.isEmpty()) {
                logger.warn("Заказ с id = {} не найден", id);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Заказ с id = " + id + " не найден"))
                        .build();
            }

            Order order = result.get(0);
            OrderDTO dto = OrderMapper.toDTO(order);
            logger.info("Заказ с id = {} успешно найден", id);
            return Response.ok(dto).build();
        } finally {
            em.close();
        }
    }

    @POST
    @Operation(summary = "Создать новый заказ", description = "Добавляет новый заказ в базу данных")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Заказ успешно создан"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public Response createOrder(OrderDTO dto) {
        logger.info("Создание нового заказа");
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
                em.getTransaction().rollback();
                logger.warn("Попытка создать заказ с некорректным количеством: {}", dto.getQuantity());
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Количество товара должно быть больше 0"))
                        .build();
            }

            Client client = em.find(Client.class, dto.getClientId());
            if (client == null) {
                em.getTransaction().rollback();
                logger.warn("Невозможно создать заказ: клиент с id = {} не найден", dto.getClientId());
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Клиент с id = " + dto.getClientId() + " не существует"))
                        .build();
            }

            Product product = em.find(Product.class, dto.getProductId());
            if (product == null) {
                em.getTransaction().rollback();
                logger.warn("Невозможно создать заказ: товар с id = {} не найден", dto.getProductId());
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Товар с id = " + dto.getProductId() + " не существует"))
                        .build();
            }

            if (product.getQuantity() < dto.getQuantity()) {
                em.getTransaction().rollback();
                logger.warn("Недостаточно товара id = {}. Запрошено: {}, в наличии: {}",
                        product.getId(), dto.getQuantity(), product.getQuantity());
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Недостаточно товара на складе! В наличии всего: " + product.getQuantity() + " шт."))
                        .build();
            }


            int remainingStock = product.getQuantity() - dto.getQuantity();
            product.setQuantity(remainingStock);
            logger.info("Списан товар id = {} в количестве {} шт. Новый остаток: {}",
                    product.getId(), dto.getQuantity(), remainingStock);

            if (dto.getTotalPrice() == null && product.getPrice() != null) {
                BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));
                dto.setTotalPrice(total);
            }

            Order order = OrderMapper.toEntity(dto, client, product);
            em.persist(order);
            em.getTransaction().commit();

            OrderDTO responseDto = OrderMapper.toDTO(order);
            logger.info("Новый заказ успешно создан с id = {}", responseDto.getId());
            return Response.status(Response.Status.CREATED).entity(responseDto).build();

        } catch (Exception e) {
            logger.error("Ошибка при создании нового заказа: {}", e.getMessage(), e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }


    @PUT
    @Path("/{id}")
    @Operation(summary = "Обновить заказ по ID", description = "Обновляет информацию о заказе по его id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Заказ успешно обновлен"),
            @ApiResponse(responseCode = "404", description = "Заказ с указанным ID не удалось обновить")
    })
    public Response updateOrder(@PathParam("id") int id, OrderDTO dto) {
        logger.info("Метод PUT: обновление данных заказа с id = {}",id);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            int updatedRows = em.createNativeQuery(
                            "UPDATE orders " +
                                    "SET quantity = :quantity, total_price = :totalPrice, status = :status " +
                                    "WHERE id = :id"
                    )
                    .setParameter("quantity", dto.getQuantity())
                    .setParameter("totalPrice", dto.getTotalPrice())
                    .setParameter("status", dto.getStatus())
                    .setParameter("id", id)
                    .executeUpdate();

            if (updatedRows == 0) {
                logger.warn("Заказ с id = {}  не найден для обновления ",id);
                em.getTransaction().rollback();
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Заказ с id = " + id + " не найден"))
                        .build();
            }

            em.getTransaction().commit();
            dto.setId(id);
            logger.info("Данные заказа с id = {} успешно обновлены", id);
            return Response.ok(dto).build();

        } catch (Exception e) {
            logger.error("Ошибка при обновлении заказа с id = {}: {}", id, e.getMessage(), e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Удалить заказ по ID", description = "Удаляет информацию о заказе по его id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Заказ успешно удален"),
            @ApiResponse(responseCode = "404", description = "Заказ с указанным ID не удалось удалить")
    })
    public Response deleteOrder(@PathParam("id") int id) {
        logger.info("Метод DELETE: удаление заказа с id = {}", id);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Order order = em.find(Order.class, id);
            if (order == null) {
                logger.warn("Заказ с id = {} не найден для удаления", id);
                em.getTransaction().rollback();
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Заказ с id = " + id + " не найден"))
                        .build();
            }

            Product product = order.getProduct();
            if (product != null) {
                int restoredStock = product.getQuantity() + order.getQuantity();
                product.setQuantity(restoredStock);
                logger.info("Заказ отменен. Товар id = {} возвращен на склад в количестве {} шт. Новый остаток: {}",
                        product.getId(), order.getQuantity(), restoredStock);
            }

            em.remove(order);
            em.getTransaction().commit();

            logger.info("Заказ с id = {} успешно удален", id);
            return Response.noContent().build();

        } catch (Exception e) {
            logger.error("Ошибка при удалении заказа с id = {}: {}", id, e.getMessage(), e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}