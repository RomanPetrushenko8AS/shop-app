package com.example.rest;

import com.example.JpaUtil;
import com.example.dto.ProductDTO;
import com.example.entity.Product;
import com.example.mapper.ProductMapper;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Товары", description = "Управление каталогом товаров")
public class ProductResource {
    private static final Logger logger = LoggerFactory.getLogger(ProductResource.class);

    @GET
    @Operation(summary = "Получить все товары", description = "Возвращает информацию о товарах")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Товары успешно найдены"),
            @ApiResponse(responseCode = "404", description = "Товары не найдены")
    })
    public List<ProductDTO> getAll() {
        logger.info("Метод GET: запрос на получение всех товаров");
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Product> products =  em.createNativeQuery("SELECT * FROM product", Product.class)
                    .getResultList();

            logger.info("Товары успешно получены, количество: {}", products.size());
            return products.stream().map(product -> ProductMapper.toDTO(product))
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Получить товар по ID", description = "Возвращает информацию о товаре по его id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Товар успешно найден"),
            @ApiResponse(responseCode = "404", description = "Товар с указанным ID не найден")
    })
    public Response getById(@PathParam("id") int id) {
        logger.info("Метод GET: получение товара с id = {}", id);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Product> result = em
                    .createNativeQuery("SELECT * FROM product WHERE id = :id", Product.class)
                    .setParameter("id", id)
                    .getResultList();
            if (result.isEmpty()) {
                logger.warn("Товар с id = {} не найден", id);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", " Товар с id = " + id + " не найден"))
                        .build();
            }
            Product product = result.get(0);
            ProductDTO dto = ProductMapper.toDTO(product);
            logger.info("Товар с id = {} успешно найден", id);
            return Response.ok(dto).build();
        } finally {
            em.close();
        }
    }

    @POST
    @Operation(summary = "Создать новый товар", description = "Добавляет новый товар в базу данных")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Товар успешно создан"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public Response createProduct(ProductDTO incomingDto){
        logger.info("Метод POST: создание нового товара");
        EntityManager em = JpaUtil.getEntityManager();
        try{
            em.getTransaction().begin();
             Product product = ProductMapper.toEntity(incomingDto);

             em.persist(product);
             em.getTransaction().commit();

             ProductDTO productDTO = ProductMapper.toDTO(product);
            logger.info("Новый товар успешно создан с id = {}", productDTO.getId());
             return Response.status(Response.Status.CREATED).entity(productDTO).build();
        }catch (Exception e){
            logger.error("Ошибка при создании нового товара: {}", e.getMessage(), e);
            if(em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw e;
        }finally {
            em.close();
        }
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Обновить товар по ID", description = "Обновляет информацию о товаре по его id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Товар успешно обновлен"),
            @ApiResponse(responseCode = "404", description = "Товар с указанным ID не удалось обновить")
    })
    public Response updateProduct(@PathParam("id") int id, ProductDTO incomingDto){
        logger.info("Метод PUT: обновление данных товара с id = {}", id);
        EntityManager em = JpaUtil.getEntityManager();
        try{
            em.getTransaction().begin();
            int updatedRows = em.createNativeQuery(
                    "UPDATE product " +
                            "SET name = :name, description = :description, price = :price, quantity = :quantity " +
                             "WHERE id = :id")
                    .setParameter("name",incomingDto.getName())
                    .setParameter("description", incomingDto.getDescription())
                    .setParameter("price", incomingDto.getPrice())
                    .setParameter("quantity", incomingDto.getQuantity())
                    .setParameter("price", incomingDto.getPrice())
                    .setParameter("id", id)
                    .executeUpdate();
            if(updatedRows ==0){
                logger.warn("Товарт с id = {} не найден для обновления", id);
                em.getTransaction().rollback();
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Товар с id = " + id + "не найден")
                        .build();
            }

            em.getTransaction().commit();

            incomingDto.setId(id);
            logger.info("Данные товара с id = {} успешно обновлены", id);
            return Response.ok(incomingDto).build();
        }catch (Exception e){
            logger.error("Ошибка при обновлении товара с id = {}: {}", id, e.getMessage(), e);
            if(em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw e;
        }finally{
            em.close();
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Удалить товар по ID", description = "Удаляет информацию о товаре по его id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Товар успешно удален"),
            @ApiResponse(responseCode = "404", description = "Товар с указанным ID не удалось удалить")
    })
    public Response deleteProduct(@PathParam("id") int id){
        logger.info("Метод DELETE: удаление товара с id = {}", id);
        EntityManager em = JpaUtil.getEntityManager();

        try{
            em.getTransaction().begin();

            int updatedRows = em.createNativeQuery(
                    "DELETE FROM product WHERE id = :id"
            ).setParameter("id",id).executeUpdate();

            if(updatedRows == 0){
                logger.warn("Товар с id = {} не найден для удаления", id);
                em.getTransaction().rollback();
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(" Товар с id = " + id + "не найден")
                        .build();
            }

            em.getTransaction().commit();
            logger.info("Товар с id = {} успешно удален", id);
            return Response.noContent().build();
        }catch(Exception e){
            logger.error("Ошибка при удалении товара с id = {}: {}", id, e.getMessage(), e);
            if(em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw e;
        }finally{
            em.close();
        }

    }
}
