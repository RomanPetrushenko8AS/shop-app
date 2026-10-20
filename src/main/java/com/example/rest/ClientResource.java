package com.example.rest;

import com.example.JpaUtil;
import com.example.dto.ClientDTO;
import com.example.dto.ClientReportDTO;
import com.example.entity.Client;
import com.example.mapper.ClientMapper;
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

@Path("/clients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Клиенты", description = "Управление каталогом клиентов")
public class ClientResource {
    private static final Logger logger = LoggerFactory.getLogger(ClientResource.class);

    @GET
    @Operation(summary ="Получение всех клиентов" , description ="Возвращает информацию о всех клиентах" )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Клиенты успешно найдены"),
            @ApiResponse(responseCode = "404", description = "Клиентыне найдены")
    })
    public List<ClientDTO> getAll() {
        logger.info("Метод GET: запрос на получение всех клиентов");
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Client> clients = em.createNativeQuery("SELECT * FROM client", Client.class)
                    .getResultList();

            logger.info("Клиенты успешно получены, количество: {}", clients.size());

            return clients.stream()
                    .map(ClientMapper::toDTO)
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Получить клиента по ID", description = "Возвращает информацию о клиенте по его id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Клиент успешно найден"),
            @ApiResponse(responseCode = "404", description = "Клиент с указанным ID не найден")
    })
    public Response getById(@PathParam("id") int id) {
        logger.info("Метод GET: получение клиента с id = {}", id);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Client> result = em.createNativeQuery("SELECT * FROM client WHERE client_id = :id", Client.class)
                    .setParameter("id", id)
                    .getResultList();

            if (result.isEmpty()) {
                logger.warn("Клиент с id = {} не найден", id);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Клиент с id = " + id + " не найден"))
                        .build();
            }

            Client client = result.get(0);
            ClientDTO dto = ClientMapper.toDTO(client);
            logger.info("Клиент с id = {} успешно найден", id);
            return Response.ok(dto).build();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("/reports/spending")
    @Operation(summary = "Отчет о заказах клиентов", description = "Возвращает информацию о клиентах, их количестве и стоимости всех заказов")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Отчет успешно создан"),
            @ApiResponse(responseCode = "404", description = "Не удалось создать отчет")
    })
    public Response getReport(){
        logger.info("Метод GET: создание отчета");
        EntityManager em = JpaUtil.getEntityManager();
        String sql = """
                    SELECT c.client_id, c.name, c.surname, COUNT(o.id), COALESCE(SUM(o.total_price), 0)
                    FROM client c
                    LEFT JOIN orders o ON c.client_id = o.client_id
                    GROUP BY c.client_id, c.name, c.surname
                    ORDER BY COALESCE(SUM(o.total_price), 0) DESC
                    """;
        try{
            @SuppressWarnings("unchecked")
            List<Object[]> rows = em.createNativeQuery(sql).getResultList();

            List<ClientReportDTO> report = rows.stream()
                    .map(row -> new ClientReportDTO(((Number) row[0]).intValue(),
                    row[1] + " "+ row[2],((Number) row[3]).longValue(),(BigDecimal) row[4])).toList();

            logger.info("Отчет сформирован записей: {}", report.size());
            return Response.ok(report).build();

        }catch(Exception e){
            logger.error("Произошла ошибка при создании отчета {}", e.getMessage(),e);
            throw e;
        }finally{
            em.close();
        }
    }

    @POST
    @Operation(summary = "Создать нового клиента", description = "Добавляет нового клиента в базу данных")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Клиент успешно создан"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public Response createClient(ClientDTO dto) {
        logger.info("Метод POST: создание нового клиента");
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Client client = ClientMapper.toEntity(dto);

            em.persist(client);
            em.getTransaction().commit();

            ClientDTO clientDTO = ClientMapper.toDTO(client);
            logger.info("Новый клиент успешно создан с id = {}", clientDTO.getId());
            return Response.status(Response.Status.CREATED).entity(clientDTO).build();
        } catch (Exception e) {
            logger.error("Ошибка при создании нового клиента: {}", e.getMessage(), e);
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
    @Operation(summary = "Обновить клиента по ID", description = "Обновляет информацию о клиенте по его id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Клиент успешно обновлен"),
            @ApiResponse(responseCode = "404", description = "Клиента с указанным ID не удалось обновить")
    })
    public Response updateClient(@PathParam("id") int id, ClientDTO dto) {
        logger.info("Метод PUT: обновление данных клиента с id = {}", id);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            int updateRows = em.createNativeQuery(
                            "UPDATE client " +
                                    "SET name = :name, surname = :surname, email = :email, phone = :phone " +
                                    "WHERE client_id = :id"
                    )
                    .setParameter("name", dto.getName())
                    .setParameter("surname", dto.getSurname())
                    .setParameter("email", dto.getEmail())
                    .setParameter("phone", dto.getPhone())
                    .setParameter("id", id)
                    .executeUpdate();

            if (updateRows == 0) {
                logger.warn("Клиент с id = {} не найден для обновления", id);
                em.getTransaction().rollback();
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Клиент с id = " + id + " не найден"))
                        .build();
            }

            em.getTransaction().commit();
            dto.setId(id);
            logger.info("Данные клиента с id = {} успешно обновлены", id);
            return Response.ok(dto).build();
        } catch (Exception e) {
            logger.error("Ошибка при обновлении клиента с id = {}: {}", id, e.getMessage(), e);
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
    @Operation(summary = "Удалить клиента по ID", description = "Удаляет информацию о клиента по его id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Клиент успешно удален"),
            @ApiResponse(responseCode = "404", description = "Клиента с указанным ID не удалось удалить")
    })
    public Response deleteClient(@PathParam("id") int id) {
        logger.info("Метод DELETE: удаление клиента с id = {}", id);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            int updatedRows = em.createNativeQuery("DELETE FROM client WHERE client_id = :id")
                    .setParameter("id", id)
                    .executeUpdate();

            if (updatedRows == 0) {
                logger.warn("Клиент с id = {} не найден для удаления", id);
                em.getTransaction().rollback();
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Клиент с id = " + id + " не найден"))
                        .build();
            }

            em.getTransaction().commit();
            logger.info("Клиент с id = {} успешно удален", id);
            return Response.noContent().build();
        } catch (Exception e) {
            logger.error("Ошибка при удалении клиента с id = {}: {}", id, e.getMessage(), e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}