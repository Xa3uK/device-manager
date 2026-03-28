package com.koval.devicemanager.api.controller;

import com.koval.devicemanager.domain.model.DeviceState;
import com.koval.devicemanager.infra.entity.DeviceEntity;
import com.koval.devicemanager.infra.repository.DeviceJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@DisplayName("DeviceController IT")
class DeviceControllerIT {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private DeviceJpaRepository jpaRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        jpaRepository.deleteAll();
    }

    private DeviceEntity saveDevice(String name, String brand, DeviceState state) {
        return jpaRepository.save(DeviceEntity.builder().name(name).brand(brand).state(state).build());
    }

    @Nested
    @DisplayName("POST /api/v1/devices")
    class Create {

        @Test
        @DisplayName("creates device and persists to database")
        void createsDeviceAndPersistsToDatabase() throws Exception {
            mockMvc.perform(post("/api/v1/devices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "iPhone 15", "brand": "Apple"}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.name").value("iPhone 15"))
                    .andExpect(jsonPath("$.brand").value("Apple"))
                    .andExpect(jsonPath("$.state").value("AVAILABLE"));

            assertThat(jpaRepository.count()).isEqualTo(1);
            DeviceEntity saved = jpaRepository.findAll().get(0);
            assertThat(saved.getName()).isEqualTo("iPhone 15");
            assertThat(saved.getBrand()).isEqualTo("Apple");
            assertThat(saved.getState()).isEqualTo(DeviceState.AVAILABLE);
            assertThat(saved.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/devices/{id}")
    class Update {

        @Test
        @DisplayName("updates device and persists changes to database")
        void updatesDeviceAndPersistsToDatabase() throws Exception {
            DeviceEntity device = saveDevice("iPhone 15", "Apple", DeviceState.AVAILABLE);

            mockMvc.perform(patch("/api/v1/devices/" + device.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "iPhone 15 Pro", "state": "IN_USE"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("iPhone 15 Pro"))
                    .andExpect(jsonPath("$.state").value("IN_USE"));

            DeviceEntity updated = jpaRepository.findById(device.getId()).orElseThrow();
            assertThat(updated.getName()).isEqualTo("iPhone 15 Pro");
            assertThat(updated.getState()).isEqualTo(DeviceState.IN_USE);
            assertThat(updated.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("returns 404 when device not found")
        void returns404WhenNotFound() throws Exception {
            mockMvc.perform(patch("/api/v1/devices/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "test"}
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("returns 422 when updating name of IN_USE device")
        void returns422WhenUpdatingNameOfInUseDevice() throws Exception {
            DeviceEntity device = saveDevice("Galaxy S24", "Samsung", DeviceState.IN_USE);

            mockMvc.perform(patch("/api/v1/devices/" + device.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "Galaxy S25"}
                                    """))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.status").value(422));

            DeviceEntity unchanged = jpaRepository.findById(device.getId()).orElseThrow();
            assertThat(unchanged.getName()).isEqualTo("Galaxy S24");
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/devices/{id}")
    class Delete {

        @Test
        @DisplayName("deletes device from database")
        void deletesDeviceFromDatabase() throws Exception {
            DeviceEntity device = saveDevice("Pixel 8", "Google", DeviceState.AVAILABLE);

            mockMvc.perform(delete("/api/v1/devices/" + device.getId()))
                    .andExpect(status().isNoContent());

            assertThat(jpaRepository.findById(device.getId())).isEmpty();
        }

        @Test
        @DisplayName("returns 404 when device not found")
        void returns404WhenNotFound() throws Exception {
            mockMvc.perform(delete("/api/v1/devices/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("returns 422 and does not delete IN_USE device")
        void returns422AndDoesNotDeleteInUseDevice() throws Exception {
            DeviceEntity device = saveDevice("Galaxy S24", "Samsung", DeviceState.IN_USE);

            mockMvc.perform(delete("/api/v1/devices/" + device.getId()))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.status").value(422));

            assertThat(jpaRepository.findById(device.getId())).isPresent();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/devices/{id}")
    class GetById {

        @Test
        @DisplayName("returns device when found")
        void returnsDeviceWhenFound() throws Exception {
            DeviceEntity device = saveDevice("iPhone 15", "Apple", DeviceState.AVAILABLE);

            mockMvc.perform(get("/api/v1/devices/" + device.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(device.getId()))
                    .andExpect(jsonPath("$.name").value("iPhone 15"))
                    .andExpect(jsonPath("$.brand").value("Apple"))
                    .andExpect(jsonPath("$.state").value("AVAILABLE"));
        }

        @Test
        @DisplayName("returns 404 when not found")
        void returns404WhenNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/devices/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Device not found with id: 999"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/devices")
    class GetAll {

        @Test
        @DisplayName("returns all devices paginated")
        void returnsAllDevicesPaginated() throws Exception {
            saveDevice("iPhone 15", "Apple", DeviceState.AVAILABLE);
            saveDevice("Galaxy S24", "Samsung", DeviceState.IN_USE);
            saveDevice("Pixel 8", "Google", DeviceState.INACTIVE);

            mockMvc.perform(get("/api/v1/devices"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(3));
        }

        @Test
        @DisplayName("filters by brand")
        void filtersByBrand() throws Exception {
            saveDevice("iPhone 15", "Apple", DeviceState.AVAILABLE);
            saveDevice("iPhone 14", "Apple", DeviceState.IN_USE);
            saveDevice("Galaxy S24", "Samsung", DeviceState.AVAILABLE);

            mockMvc.perform(get("/api/v1/devices").param("brand", "Apple"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.content[0].brand").value("Apple"))
                    .andExpect(jsonPath("$.content[1].brand").value("Apple"));
        }

        @Test
        @DisplayName("filters by state")
        void filtersByState() throws Exception {
            saveDevice("iPhone 15", "Apple", DeviceState.AVAILABLE);
            saveDevice("Galaxy S24", "Samsung", DeviceState.IN_USE);
            saveDevice("Pixel 8", "Google", DeviceState.AVAILABLE);

            mockMvc.perform(get("/api/v1/devices").param("state", "AVAILABLE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.content[0].state").value("AVAILABLE"))
                    .andExpect(jsonPath("$.content[1].state").value("AVAILABLE"));
        }

        @Test
        @DisplayName("returns 400 when page size exceeds limit")
        void returns400WhenPageSizeExceedsLimit() throws Exception {
            mockMvc.perform(get("/api/v1/devices").param("size", "101"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Requested page size 101 exceeds the maximum allowed size of 100"));
        }
    }
}
