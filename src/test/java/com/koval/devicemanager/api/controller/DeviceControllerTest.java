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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    private final List<Device> devices = List.of(
            Device.builder().id(1L).name("iPhone 15").brand("Apple").state(DeviceState.AVAILABLE).createdAt(Instant.now()).build(),
            Device.builder().id(2L).name("Galaxy S24").brand("Samsung").state(DeviceState.IN_USE).createdAt(Instant.now()).build(),
            Device.builder().id(3L).name("Pixel 8").brand("Google").state(DeviceState.INACTIVE).createdAt(Instant.now()).build()
    );

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(deviceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Nested
    @DisplayName("GET /api/devices/{id}")
    class GetById {

        @Test
        @DisplayName("returns 200 with device when found")
        void returns200WhenFound() throws Exception {
            when(deviceService.getById(1L)).thenReturn(devices.get(0));

            mockMvc.perform(get("/api/devices/1"))
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

            mockMvc.perform(get("/api/devices/99"))
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
    @DisplayName("GET /api/devices")
    class GetAll {

        @Test
        @DisplayName("returns 200 with paginated response")
        void returns200WithPagedResponse() throws Exception {
            var page = new PageImpl<>(devices, PageRequest.of(0, 10), devices.size());
            when(deviceService.getAll(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/devices"))
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
                    devices.get(0),
                    devices.get(0).toBuilder().id(4L).name("iPhone 14").build(),
                    devices.get(0).toBuilder().id(5L).name("iPhone 13").build()
            );
            var page = new PageImpl<>(appleDevices, PageRequest.of(0, 10), appleDevices.size());
            when(deviceService.getAllByBrand(eq("Apple"), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/devices").param("brand", "Apple"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                            {
                              "content": [
                                {"id": 1, "brand": "Apple"},
                                {"id": 4, "brand": "Apple"},
                                {"id": 5, "brand": "Apple"}
                              ],
                              "totalElements": 3
                            }
                            """));
        }

        @Test
        @DisplayName("returns 200 filtered by state")
        void returns200FilteredByState() throws Exception {
            List<Device> availableDevices = List.of(
                    devices.get(0),
                    devices.get(0).toBuilder().id(4L).name("Galaxy S24").brand("Samsung").build(),
                    devices.get(0).toBuilder().id(5L).name("Pixel 8").brand("Google").build()
            );
            var page = new PageImpl<>(availableDevices, PageRequest.of(0, 10), availableDevices.size());
            when(deviceService.getAllByState(eq(DeviceState.AVAILABLE), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/devices").param("state", "AVAILABLE"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                            {
                              "content": [
                                {"id": 1, "state": "AVAILABLE"},
                                {"id": 4, "state": "AVAILABLE"},
                                {"id": 5, "state": "AVAILABLE"}
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

            mockMvc.perform(get("/api/devices").param("size", "101"))
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
