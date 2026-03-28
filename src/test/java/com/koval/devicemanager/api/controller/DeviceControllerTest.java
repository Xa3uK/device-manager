package com.koval.devicemanager.api.controller;

import com.koval.devicemanager.api.exception.GlobalExceptionHandler;
import com.koval.devicemanager.domain.exception.DeviceNotFoundException;
import com.koval.devicemanager.domain.exception.PageSizeExceededException;
import com.koval.devicemanager.domain.model.Device;
import com.koval.devicemanager.domain.model.DeviceState;
import com.koval.devicemanager.domain.service.DeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeviceController")
class DeviceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DeviceService deviceService;

    @InjectMocks
    private DeviceController deviceController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(deviceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/devices")
    class Create {

        @Test
        @DisplayName("returns 201 with created device")
        void returns201WithCreatedDevice() throws Exception {
            Device created = Device.builder().id(1L).name("iPhone 15").brand("Apple").state(DeviceState.AVAILABLE).createdAt(Instant.now()).build();
            when(deviceService.create("iPhone 15", "Apple")).thenReturn(created);

            mockMvc.perform(post("/api/v1/devices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "iPhone 15", "brand": "Apple"}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(content().json("""
                            {"id": 1, "name": "iPhone 15", "brand": "Apple", "state": "AVAILABLE"}
                            """));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/devices/{id}")
    class Update {

        @Test
        @DisplayName("returns 200 with updated device")
        void returns200WithUpdatedDevice() throws Exception {
            Device updated = Device.builder().id(1L).name("iPhone 15 Pro").brand("Apple").state(DeviceState.IN_USE).createdAt(Instant.now()).build();
            when(deviceService.update(eq(1L), eq("iPhone 15 Pro"), isNull(), eq(DeviceState.IN_USE))).thenReturn(updated);

            mockMvc.perform(patch("/api/v1/devices/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "iPhone 15 Pro", "state": "IN_USE"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                            {"id": 1, "name": "iPhone 15 Pro", "brand": "Apple", "state": "IN_USE"}
                            """));
        }

        @Test
        @DisplayName("returns 404 when device not found")
        void returns404WhenNotFound() throws Exception {
            when(deviceService.update(eq(99L), any(), any(), any()))
                    .thenThrow(new DeviceNotFoundException(99L));

            mockMvc.perform(patch("/api/v1/devices/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "test"}
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(content().json("""
                            {"status": 404, "message": "Device not found with id: 99"}
                            """));
        }

        @Test
        @DisplayName("returns 422 when updating name/brand of IN_USE device")
        void returns422WhenDeviceIsInUse() throws Exception {
            when(deviceService.update(eq(2L), any(), any(), any()))
                    .thenThrow(new IllegalStateException("Name and brand cannot be updated while device is in use"));

            mockMvc.perform(patch("/api/v1/devices/2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "New Name"}
                                    """))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(content().json("""
                            {"status": 422, "message": "Name and brand cannot be updated while device is in use"}
                            """));
        }

        @Test
        @DisplayName("returns 400 when explicit null field is provided")
        void returns400WhenNullFieldProvided() throws Exception {
            mockMvc.perform(patch("/api/v1/devices/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"state": null}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().json("""
                            {"status": 400, "message": "Field 'state' must not be null"}
                            """));
        }

        @Test
        @DisplayName("returns 400 when invalid state value is provided")
        void returns400WhenInvalidStateProvided() throws Exception {
            mockMvc.perform(patch("/api/v1/devices/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"state": "INVALID"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().json("""
                            {"status": 400, "message": "Invalid value 'INVALID' for field 'state'. Valid values are: AVAILABLE, IN_USE, INACTIVE"}
                            """));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/devices/{id}")
    class Delete {

        @Test
        @DisplayName("returns 204 when device deleted")
        void returns204WhenDeleted() throws Exception {
            doNothing().when(deviceService).delete(1L);

            mockMvc.perform(delete("/api/v1/devices/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("returns 404 when device not found")
        void returns404WhenNotFound() throws Exception {
            doThrow(new DeviceNotFoundException(99L)).when(deviceService).delete(99L);

            mockMvc.perform(delete("/api/v1/devices/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().json("""
                            {"status": 404, "message": "Device not found with id: 99"}
                            """));
        }

        @Test
        @DisplayName("returns 422 when device is IN_USE")
        void returns422WhenDeviceIsInUse() throws Exception {
            doThrow(new IllegalStateException("Device cannot be deleted while it is in use")).when(deviceService).delete(2L);

            mockMvc.perform(delete("/api/v1/devices/2"))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(content().json("""
                            {"status": 422, "message": "Device cannot be deleted while it is in use"}
                            """));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/devices/{id}")
    class GetById {

        @Test
        @DisplayName("returns 200 with device when found")
        void returns200WhenFound() throws Exception {
            Device iphone = Device.builder().id(1L).name("iPhone 15").brand("Apple").state(DeviceState.AVAILABLE).createdAt(Instant.now()).build();
            when(deviceService.getById(1L)).thenReturn(iphone);

            mockMvc.perform(get("/api/v1/devices/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                            {
                              "id": 1,
                              "name": "iPhone 15",
                              "brand": "Apple",
                              "state": "AVAILABLE"
                            }
                            """));
        }

        @Test
        @DisplayName("returns 404 when device not found")
        void returns404WhenNotFound() throws Exception {
            when(deviceService.getById(99L)).thenThrow(new DeviceNotFoundException(99L));

            mockMvc.perform(get("/api/v1/devices/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().json("""
                            {
                              "status": 404,
                              "message": "Device not found with id: 99"
                            }
                            """));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/devices")
    class GetAll {

        @Test
        @DisplayName("returns 200 with paginated response")
        void returns200WithPagedResponse() throws Exception {
            List<Device> deviceList = List.of(
                    Device.builder().id(1L).name("iPhone 15").brand("Apple").state(DeviceState.AVAILABLE).createdAt(Instant.now()).build(),
                    Device.builder().id(2L).name("Galaxy S24").brand("Samsung").state(DeviceState.IN_USE).createdAt(Instant.now()).build(),
                    Device.builder().id(3L).name("Pixel 8").brand("Google").state(DeviceState.INACTIVE).createdAt(Instant.now()).build()
            );
            when(deviceService.getAll(any(Pageable.class))).thenReturn(new PageImpl<>(deviceList, PageRequest.of(0, 10), deviceList.size()));

            mockMvc.perform(get("/api/v1/devices"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                            {
                              "content": [
                                {"id": 1, "name": "iPhone 15",  "brand": "Apple",   "state": "AVAILABLE"},
                                {"id": 2, "name": "Galaxy S24", "brand": "Samsung", "state": "IN_USE"},
                                {"id": 3, "name": "Pixel 8",    "brand": "Google",  "state": "INACTIVE"}
                              ],
                              "page": 0,
                              "size": 10,
                              "totalElements": 3,
                              "totalPages": 1
                            }
                            """));
        }

        @Test
        @DisplayName("returns 200 filtered by brand")
        void returns200FilteredByBrand() throws Exception {
            List<Device> appleDevices = List.of(
                    Device.builder().id(1L).name("iPhone 15").brand("Apple").state(DeviceState.AVAILABLE).createdAt(Instant.now()).build(),
                    Device.builder().id(2L).name("iPhone 14").brand("Apple").state(DeviceState.IN_USE).createdAt(Instant.now()).build(),
                    Device.builder().id(3L).name("iPhone 13").brand("Apple").state(DeviceState.INACTIVE).createdAt(Instant.now()).build()
            );
            when(deviceService.getAllByBrand(eq("Apple"), any(Pageable.class))).thenReturn(new PageImpl<>(appleDevices, PageRequest.of(0, 10), appleDevices.size()));

            mockMvc.perform(get("/api/v1/devices").param("brand", "Apple"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                            {
                              "content": [
                                {"id": 1, "brand": "Apple"},
                                {"id": 2, "brand": "Apple"},
                                {"id": 3, "brand": "Apple"}
                              ],
                              "totalElements": 3
                            }
                            """));
        }

        @Test
        @DisplayName("returns 200 filtered by state")
        void returns200FilteredByState() throws Exception {
            List<Device> availableDevices = List.of(
                    Device.builder().id(1L).name("iPhone 15").brand("Apple").state(DeviceState.AVAILABLE).createdAt(Instant.now()).build(),
                    Device.builder().id(2L).name("Galaxy S24").brand("Samsung").state(DeviceState.AVAILABLE).createdAt(Instant.now()).build(),
                    Device.builder().id(3L).name("Pixel 8").brand("Google").state(DeviceState.AVAILABLE).createdAt(Instant.now()).build()
            );
            when(deviceService.getAllByState(eq(DeviceState.AVAILABLE), any(Pageable.class))).thenReturn(new PageImpl<>(availableDevices, PageRequest.of(0, 10), availableDevices.size()));

            mockMvc.perform(get("/api/v1/devices").param("state", "AVAILABLE"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                            {
                              "content": [
                                {"id": 1, "state": "AVAILABLE"},
                                {"id": 2, "state": "AVAILABLE"},
                                {"id": 3, "state": "AVAILABLE"}
                              ],
                              "totalElements": 3
                            }
                            """));
        }

        @Test
        @DisplayName("returns 400 when page size exceeds limit")
        void returns400WhenPageSizeExceedsLimit() throws Exception {
            when(deviceService.getAll(any(Pageable.class)))
                    .thenThrow(new PageSizeExceededException(101, 100));

            mockMvc.perform(get("/api/v1/devices").param("size", "101"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().json("""
                            {
                              "status": 400,
                              "message": "Requested page size 101 exceeds the maximum allowed size of 100"
                            }
                            """));
        }
    }
}
