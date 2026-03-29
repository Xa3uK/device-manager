package com.koval.devicemanager.unit.mapper;

import com.koval.devicemanager.domain.model.Device;
import com.koval.devicemanager.domain.model.DeviceState;
import com.koval.devicemanager.infra.entity.DeviceEntity;
import com.koval.devicemanager.infra.mapper.DeviceMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DeviceMapper")
class DeviceMapperTest {

    private final DeviceMapper mapper = new DeviceMapper();

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("maps all fields from entity to domain")
        void mapsAllFields() {
            DeviceEntity entity = DeviceEntity.builder()
                    .id(1L)
                    .name("iPhone 15")
                    .brand("Apple")
                    .state(DeviceState.AVAILABLE)
                    .build();

            Device result = mapper.toDomain(entity);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("iPhone 15");
            assertThat(result.getBrand()).isEqualTo("Apple");
            assertThat(result.getState()).isEqualTo(DeviceState.AVAILABLE);
            assertThat(result.getCreatedAt()).isNull();
            assertThat(result.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("maps createdAt and updatedAt when present")
        void mapsTimestamps() {
            Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");
            Instant updatedAt = Instant.parse("2024-06-01T00:00:00Z");
            DeviceEntity entity = DeviceEntity.builder()
                    .id(2L)
                    .name("Galaxy S24")
                    .brand("Samsung")
                    .state(DeviceState.IN_USE)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();

            Device result = mapper.toDomain(entity);

            assertThat(result.getCreatedAt()).isEqualTo(createdAt);
            assertThat(result.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("maps name, brand, state from domain to entity")
        void mapsCoreFields() {
            Device device = Device.builder()
                    .id(1L)
                    .name("iPhone 15")
                    .brand("Apple")
                    .state(DeviceState.AVAILABLE)
                    .createdAt(Instant.now())
                    .build();

            DeviceEntity result = mapper.toEntity(device);

            assertThat(result.getName()).isEqualTo("iPhone 15");
            assertThat(result.getBrand()).isEqualTo("Apple");
            assertThat(result.getState()).isEqualTo(DeviceState.AVAILABLE);
        }

        @Test
        @DisplayName("does not map id or timestamps — those are managed by JPA")
        void doesNotMapIdOrTimestamps() {
            Device device = Device.builder()
                    .id(99L)
                    .name("Pixel 8")
                    .brand("Google")
                    .state(DeviceState.INACTIVE)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            DeviceEntity result = mapper.toEntity(device);

            assertThat(result.getId()).isNull();
            assertThat(result.getCreatedAt()).isNull();
            assertThat(result.getUpdatedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("mergeToEntity")
    class MergeToEntity {

        @Test
        @DisplayName("updates name, brand, state when all provided")
        void updatesAllProvidedFields() {
            DeviceEntity target = DeviceEntity.builder()
                    .id(1L)
                    .name("iPhone 15")
                    .brand("Apple")
                    .state(DeviceState.AVAILABLE)
                    .build();
            Device source = Device.builder()
                    .name("iPhone 15 Pro")
                    .brand("Apple Inc")
                    .state(DeviceState.IN_USE)
                    .build();

            mapper.mergeToEntity(source, target);

            assertThat(target.getName()).isEqualTo("iPhone 15 Pro");
            assertThat(target.getBrand()).isEqualTo("Apple Inc");
            assertThat(target.getState()).isEqualTo(DeviceState.IN_USE);
        }

        @Test
        @DisplayName("skips null fields — keeps existing values")
        void skipsNullFields() {
            DeviceEntity target = DeviceEntity.builder()
                    .id(1L)
                    .name("iPhone 15")
                    .brand("Apple")
                    .state(DeviceState.AVAILABLE)
                    .build();
            Device source = Device.builder()
                    .state(DeviceState.IN_USE)
                    .build();

            mapper.mergeToEntity(source, target);

            assertThat(target.getName()).isEqualTo("iPhone 15");
            assertThat(target.getBrand()).isEqualTo("Apple");
            assertThat(target.getState()).isEqualTo(DeviceState.IN_USE);
        }

        @Test
        @DisplayName("does not modify target when all source fields are null")
        void doesNotModifyWhenAllNull() {
            DeviceEntity target = DeviceEntity.builder()
                    .id(1L)
                    .name("Pixel 8")
                    .brand("Google")
                    .state(DeviceState.INACTIVE)
                    .build();
            Device source = Device.builder().build();

            mapper.mergeToEntity(source, target);

            assertThat(target.getName()).isEqualTo("Pixel 8");
            assertThat(target.getBrand()).isEqualTo("Google");
            assertThat(target.getState()).isEqualTo(DeviceState.INACTIVE);
        }
    }
}
