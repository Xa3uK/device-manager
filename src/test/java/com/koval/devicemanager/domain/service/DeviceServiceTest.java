package com.koval.devicemanager.domain.service;

import com.koval.devicemanager.domain.exception.DeviceNotFoundException;
import com.koval.devicemanager.domain.model.Device;
import com.koval.devicemanager.domain.model.DeviceState;
import com.koval.devicemanager.domain.repository.DeviceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
        @DisplayName("returns page when page size is valid")
        void returnsPageWhenSizeIsValid() {
            Pageable pageable = PageRequest.of(0, 10);
            List<Device> deviceList = List.of(
                    Device.builder().id(1L).name("iPhone 15").brand("Apple").state(DeviceState.AVAILABLE).createdAt(Instant.now()).build(),
                    Device.builder().id(2L).name("Galaxy S24").brand("Samsung").state(DeviceState.IN_USE).createdAt(Instant.now()).build(),
                    Device.builder().id(3L).name("Pixel 8").brand("Google").state(DeviceState.INACTIVE).createdAt(Instant.now()).build()
            );
            when(deviceRepository.findAll(pageable)).thenReturn(new PageImpl<>(deviceList, pageable, deviceList.size()));

            Page<Device> result = deviceService.getAll(pageable);

            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(3);
            verify(deviceRepository).findAll(pageable);
        }

        @Test
        @DisplayName("clamps page size to maximum when exceeded")
        void clampsPageSizeWhenExceeded() {
            Pageable pageable = PageRequest.of(0, 101);
            when(deviceRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

            deviceService.getAll(pageable);

            verify(deviceRepository).findAll(argThat(p -> p.getPageSize() == 100));
        }
    }

    @Nested
    @DisplayName("getAllByBrand")
    class GetAllByBrand {

        @Test
        @DisplayName("returns filtered page when page size is valid")
        void returnsFilteredPageWhenSizeIsValid() {
            Pageable pageable = PageRequest.of(0, 10);
            List<Device> appleDevices = List.of(
                    Device.builder().id(1L).name("iPhone 15").brand("Apple").state(DeviceState.AVAILABLE).createdAt(Instant.now()).build(),
                    Device.builder().id(2L).name("iPhone 14").brand("Apple").state(DeviceState.IN_USE).createdAt(Instant.now()).build(),
                    Device.builder().id(3L).name("iPhone 13").brand("Apple").state(DeviceState.INACTIVE).createdAt(Instant.now()).build()
            );
            when(deviceRepository.findAllByBrand("Apple", pageable)).thenReturn(new PageImpl<>(appleDevices, pageable, appleDevices.size()));

            Page<Device> result = deviceService.getAllByBrand("Apple", pageable);

            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent()).allMatch(d -> d.getBrand().equals("Apple"));
            verify(deviceRepository).findAllByBrand("Apple", pageable);
        }

        @Test
        @DisplayName("clamps page size to maximum when exceeded")
        void clampsPageSizeWhenExceeded() {
            Pageable pageable = PageRequest.of(0, 101);
            when(deviceRepository.findAllByBrand(eq("Apple"), any(Pageable.class))).thenReturn(Page.empty());

            deviceService.getAllByBrand("Apple", pageable);

            verify(deviceRepository).findAllByBrand(eq("Apple"), argThat(p -> p.getPageSize() == 100));
        }
    }

    @Nested
    @DisplayName("getAllByState")
    class GetAllByState {

        @ParameterizedTest(name = "state={0}")
        @EnumSource(DeviceState.class)
        @DisplayName("returns filtered page for each state")
        void returnsFilteredPageForEachState(DeviceState state) {
            Pageable pageable = PageRequest.of(0, 10);
            List<Device> stateDevices = List.of(
                    Device.builder().id(1L).name("iPhone 15").brand("Apple").state(state).createdAt(Instant.now()).build(),
                    Device.builder().id(2L).name("Galaxy S24").brand("Samsung").state(state).createdAt(Instant.now()).build(),
                    Device.builder().id(3L).name("Pixel 8").brand("Google").state(state).createdAt(Instant.now()).build()
            );
            when(deviceRepository.findAllByState(state, pageable)).thenReturn(new PageImpl<>(stateDevices, pageable, stateDevices.size()));

            Page<Device> result = deviceService.getAllByState(state, pageable);

            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent()).allMatch(d -> d.getState() == state);
            verify(deviceRepository).findAllByState(state, pageable);
        }

        @Test
        @DisplayName("clamps page size to maximum when exceeded")
        void clampsPageSizeWhenExceeded() {
            Pageable pageable = PageRequest.of(0, 101);
            when(deviceRepository.findAllByState(eq(DeviceState.AVAILABLE), any(Pageable.class))).thenReturn(Page.empty());

            deviceService.getAllByState(DeviceState.AVAILABLE, pageable);

            verify(deviceRepository).findAllByState(eq(DeviceState.AVAILABLE), argThat(p -> p.getPageSize() == 100));
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
