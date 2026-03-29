package com.koval.devicemanager.unit.service;

import com.koval.devicemanager.domain.exception.DeviceNotFoundException;
import com.koval.devicemanager.domain.model.Device;
import com.koval.devicemanager.domain.model.DeviceInput;
import com.koval.devicemanager.domain.model.DeviceState;
import com.koval.devicemanager.domain.repository.DeviceRepository;
import com.koval.devicemanager.domain.service.DeviceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeviceService")
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private DeviceService deviceService;

    @Nested
    @DisplayName("createBulk")
    class CreateBulk {

        @Test
        @DisplayName("creates all devices with AVAILABLE state")
        void createsAllDevicesWithAvailableState() {
            List<Device> saved = List.of(
                    Device.builder().id(1L).name("iPhone 15").brand("Apple").state(DeviceState.AVAILABLE).createdAt(Instant.now()).build(),
                    Device.builder().id(2L).name("Galaxy S24").brand("Samsung").state(DeviceState.AVAILABLE).createdAt(Instant.now()).build()
            );
            when(deviceRepository.saveAll(any())).thenReturn(saved);

            List<Device> result = deviceService.createBulk(List.of(
                    new DeviceInput("iPhone 15", "Apple"),
                    new DeviceInput("Galaxy S24", "Samsung")
            ));

            assertThat(result).hasSize(2);
            assertThat(result).allMatch(d -> d.getState() == DeviceState.AVAILABLE);
            verify(deviceRepository).saveAll(any());
        }
    }

    @Nested
    @DisplayName("deleteBulk")
    class DeleteBulk {

        @Test
        @DisplayName("deletes all devices successfully")
        void deletesAllDevicesSuccessfully() {
            List<Device> devices = List.of(
                    Device.builder().id(1L).name("iPhone 15").brand("Apple").state(DeviceState.AVAILABLE).createdAt(Instant.now()).build(),
                    Device.builder().id(2L).name("Pixel 8").brand("Google").state(DeviceState.INACTIVE).createdAt(Instant.now()).build()
            );
            when(deviceRepository.findAllById(List.of(1L, 2L))).thenReturn(devices);

            deviceService.deleteBulk(List.of(1L, 2L));

            verify(deviceRepository).deleteAll(List.of(1L, 2L));
        }

        @Test
        @DisplayName("throws DeviceNotFoundException when any ID is not found")
        void throwsWhenAnyIdNotFound() {
            when(deviceRepository.findAllById(List.of(1L, 99L))).thenReturn(
                    List.of(Device.builder().id(1L).name("iPhone 15").brand("Apple").state(DeviceState.AVAILABLE).createdAt(Instant.now()).build())
            );

            assertThatThrownBy(() -> deviceService.deleteBulk(List.of(1L, 99L)))
                    .isInstanceOf(DeviceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(deviceRepository, never()).deleteAll(any());
        }

        @Test
        @DisplayName("throws IllegalStateException when any device is IN_USE")
        void throwsWhenAnyDeviceIsInUse() {
            List<Device> devices = List.of(
                    Device.builder().id(1L).name("iPhone 15").brand("Apple").state(DeviceState.AVAILABLE).createdAt(Instant.now()).build(),
                    Device.builder().id(2L).name("Galaxy S24").brand("Samsung").state(DeviceState.IN_USE).createdAt(Instant.now()).build()
            );
            when(deviceRepository.findAllById(List.of(1L, 2L))).thenReturn(devices);

            assertThatThrownBy(() -> deviceService.deleteBulk(List.of(1L, 2L)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("2");

            verify(deviceRepository, never()).deleteAll(any());
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("creates device with AVAILABLE state")
        void createsDeviceWithAvailableState() {
            Device saved = Device.builder().id(1L).name("iPhone 15").brand("Apple").state(DeviceState.AVAILABLE).createdAt(Instant.now()).build();
            when(deviceRepository.save(any(Device.class))).thenReturn(saved);

            Device result = deviceService.create("iPhone 15", "Apple");

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("iPhone 15");
            assertThat(result.getBrand()).isEqualTo("Apple");
            assertThat(result.getState()).isEqualTo(DeviceState.AVAILABLE);
            verify(deviceRepository).save(any(Device.class));
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("returns device when found")
        void returnsDeviceWhenFound() {
            Device iphone = Device.builder().id(1L).name("iPhone 15").brand("Apple").state(DeviceState.AVAILABLE).createdAt(Instant.now()).build();
            when(deviceRepository.findById(1L)).thenReturn(Optional.of(iphone));

            Device result = deviceService.getById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("iPhone 15");
            assertThat(result.getBrand()).isEqualTo("Apple");
            verify(deviceRepository).findById(1L);
        }

        @Test
        @DisplayName("throws DeviceNotFoundException when not found")
        void throwsExceptionWhenNotFound() {
            when(deviceRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> deviceService.getById(99L))
                    .isInstanceOf(DeviceNotFoundException.class)
                    .hasMessageContaining("Device not found with id: 99");

            verify(deviceRepository).findById(99L);
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @Test
        @DisplayName("returns all devices when no filters")
        void returnsAllDevicesWhenNoFilters() {
            Pageable pageable = PageRequest.of(0, 10);
            List<Device> deviceList = List.of(
                    Device.builder().id(1L).name("iPhone 15").brand("Apple").state(DeviceState.AVAILABLE).createdAt(Instant.now()).build(),
                    Device.builder().id(2L).name("Galaxy S24").brand("Samsung").state(DeviceState.IN_USE).createdAt(Instant.now()).build(),
                    Device.builder().id(3L).name("Pixel 8").brand("Google").state(DeviceState.INACTIVE).createdAt(Instant.now()).build()
            );
            when(deviceRepository.findAll(null, null, pageable)).thenReturn(new PageImpl<>(deviceList, pageable, deviceList.size()));

            Page<Device> result = deviceService.getAll(null, null, pageable);

            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(3);
            verify(deviceRepository).findAll(null, null, pageable);
        }

        @Test
        @DisplayName("returns filtered page when brand provided")
        void returnsFilteredPageWhenBrandProvided() {
            Pageable pageable = PageRequest.of(0, 10);
            List<Device> appleDevices = List.of(
                    Device.builder().id(1L).name("iPhone 15").brand("Apple").state(DeviceState.AVAILABLE).createdAt(Instant.now()).build(),
                    Device.builder().id(2L).name("iPhone 14").brand("Apple").state(DeviceState.IN_USE).createdAt(Instant.now()).build()
            );
            when(deviceRepository.findAll("Apple", null, pageable)).thenReturn(new PageImpl<>(appleDevices, pageable, appleDevices.size()));

            Page<Device> result = deviceService.getAll("Apple", null, pageable);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).allMatch(d -> d.getBrand().equals("Apple"));
            verify(deviceRepository).findAll("Apple", null, pageable);
        }

        @Test
        @DisplayName("returns filtered page when state provided")
        void returnsFilteredPageWhenStateProvided() {
            Pageable pageable = PageRequest.of(0, 10);
            List<Device> availableDevices = List.of(
                    Device.builder().id(1L).name("iPhone 15").brand("Apple").state(DeviceState.AVAILABLE).createdAt(Instant.now()).build(),
                    Device.builder().id(2L).name("Pixel 8").brand("Google").state(DeviceState.AVAILABLE).createdAt(Instant.now()).build()
            );
            when(deviceRepository.findAll(null, DeviceState.AVAILABLE, pageable)).thenReturn(new PageImpl<>(availableDevices, pageable, availableDevices.size()));

            Page<Device> result = deviceService.getAll(null, DeviceState.AVAILABLE, pageable);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).allMatch(d -> d.getState() == DeviceState.AVAILABLE);
            verify(deviceRepository).findAll(null, DeviceState.AVAILABLE, pageable);
        }

        @Test
        @DisplayName("returns filtered page when both brand and state provided")
        void returnsFilteredPageWhenBrandAndStateProvided() {
            Pageable pageable = PageRequest.of(0, 10);
            List<Device> result_devices = List.of(
                    Device.builder().id(1L).name("iPhone 15").brand("Apple").state(DeviceState.AVAILABLE).createdAt(Instant.now()).build()
            );
            when(deviceRepository.findAll("Apple", DeviceState.AVAILABLE, pageable)).thenReturn(new PageImpl<>(result_devices, pageable, result_devices.size()));

            Page<Device> result = deviceService.getAll("Apple", DeviceState.AVAILABLE, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getBrand()).isEqualTo("Apple");
            assertThat(result.getContent().get(0).getState()).isEqualTo(DeviceState.AVAILABLE);
            verify(deviceRepository).findAll("Apple", DeviceState.AVAILABLE, pageable);
        }

        @Test
        @DisplayName("clamps page size to maximum when exceeded")
        void clampsPageSizeWhenExceeded() {
            Pageable pageable = PageRequest.of(0, 101);
            when(deviceRepository.findAll(isNull(), isNull(), any(Pageable.class))).thenReturn(Page.empty());

            deviceService.getAll(null, null, pageable);

            verify(deviceRepository).findAll(isNull(), isNull(), argThat(p -> p.getPageSize() == 100));
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deletes device successfully")
        void deletesDeviceSuccessfully() {
            Device available = Device.builder().id(1L).name("iPhone 15").brand("Apple").state(DeviceState.AVAILABLE).createdAt(Instant.now()).build();
            when(deviceRepository.findById(1L)).thenReturn(Optional.of(available));

            deviceService.delete(1L);

            verify(deviceRepository).delete(1L);
        }

        @Test
        @DisplayName("throws DeviceNotFoundException when device not found")
        void throwsExceptionWhenNotFound() {
            when(deviceRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> deviceService.delete(99L))
                    .isInstanceOf(DeviceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(deviceRepository, never()).delete(any());
        }

        @Test
        @DisplayName("throws IllegalStateException when device is IN_USE")
        void throwsExceptionWhenDeviceIsInUse() {
            Device inUseDevice = Device.builder().id(2L).name("Galaxy S24").brand("Samsung").state(DeviceState.IN_USE).createdAt(Instant.now()).build();
            when(deviceRepository.findById(2L)).thenReturn(Optional.of(inUseDevice));

            assertThatThrownBy(() -> deviceService.delete(2L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("in use");

            verify(deviceRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("updates device successfully")
        void updatesDeviceSuccessfully() {
            Device existing = Device.builder().id(1L).name("iPhone 15").brand("Apple").state(DeviceState.AVAILABLE).createdAt(Instant.now()).build();
            Device updated = existing.toBuilder().name("iPhone 15 Pro").state(DeviceState.IN_USE).build();
            when(deviceRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(deviceRepository.update(any(Device.class))).thenReturn(updated);

            Device result = deviceService.update(1L, "iPhone 15 Pro", null, DeviceState.IN_USE);

            assertThat(result.getName()).isEqualTo("iPhone 15 Pro");
            assertThat(result.getState()).isEqualTo(DeviceState.IN_USE);
            verify(deviceRepository).findById(1L);
            verify(deviceRepository).update(any(Device.class));
        }

        @Test
        @DisplayName("throws DeviceNotFoundException when device not found")
        void throwsExceptionWhenNotFound() {
            when(deviceRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> deviceService.update(99L, "name", null, null))
                .isInstanceOf(DeviceNotFoundException.class)
                .hasMessageContaining("99");

            verify(deviceRepository, never()).update(any());
        }

        @Test
        @DisplayName("allows updating only state when device is IN_USE")
        void allowsUpdatingStateOfInUseDevice() {
            Device inUseDevice = Device.builder().id(2L).name("Galaxy S24").brand("Samsung").state(DeviceState.IN_USE).createdAt(Instant.now()).build();
            Device updated = inUseDevice.toBuilder().state(DeviceState.AVAILABLE).build();
            when(deviceRepository.findById(2L)).thenReturn(Optional.of(inUseDevice));
            when(deviceRepository.update(any(Device.class))).thenReturn(updated);

            Device result = deviceService.update(2L, null, null, DeviceState.AVAILABLE);

            assertThat(result.getState()).isEqualTo(DeviceState.AVAILABLE);
        }

        @Test
        @DisplayName("throws IllegalStateException when updating name of IN_USE device")
        void throwsExceptionWhenUpdatingNameOfInUseDevice() {
            Device inUseDevice = Device.builder().id(2L).name("Galaxy S24").brand("Samsung").state(DeviceState.IN_USE).createdAt(Instant.now()).build();
            when(deviceRepository.findById(2L)).thenReturn(Optional.of(inUseDevice));

            assertThatThrownBy(() -> deviceService.update(2L, "New Name", null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("in use");

            verify(deviceRepository, never()).update(any());
        }

        @Test
        @DisplayName("throws IllegalStateException when updating brand of IN_USE device")
        void throwsExceptionWhenUpdatingBrandOfInUseDevice() {
            Device inUseDevice = Device.builder().id(2L).name("Galaxy S24").brand("Samsung").state(DeviceState.IN_USE).createdAt(Instant.now()).build();
            when(deviceRepository.findById(2L)).thenReturn(Optional.of(inUseDevice));

            assertThatThrownBy(() -> deviceService.update(2L, null, "Google", null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("in use");

            verify(deviceRepository, never()).update(any());
        }
    }
}
