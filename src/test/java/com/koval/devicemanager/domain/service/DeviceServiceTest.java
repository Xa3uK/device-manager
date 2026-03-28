package com.koval.devicemanager.domain.service;

import com.koval.devicemanager.domain.exception.DeviceNotFoundException;
import com.koval.devicemanager.domain.exception.PageSizeExceededException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeviceService")
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private DeviceService deviceService;

    private final List<Device> devices = List.of(
            Device.builder().id(1L).name("iPhone 15").brand("Apple").state(DeviceState.AVAILABLE).createdAt(Instant.now()).build(),
            Device.builder().id(2L).name("Galaxy S24").brand("Samsung").state(DeviceState.IN_USE).createdAt(Instant.now()).build(),
            Device.builder().id(3L).name("Pixel 8").brand("Google").state(DeviceState.INACTIVE).createdAt(Instant.now()).build()
    );

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("returns device when found")
        void returnsDeviceWhenFound() {
            when(deviceRepository.findById(1L)).thenReturn(Optional.of(devices.get(0)));

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
            Page<Device> page = new PageImpl<>(devices, pageable, devices.size());
            when(deviceRepository.findAll(pageable)).thenReturn(page);

            Page<Device> result = deviceService.getAll(pageable);

            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(3);
            verify(deviceRepository).findAll(pageable);
        }

        @Test
        @DisplayName("throws PageSizeExceededException when page size exceeds limit")
        void throwsExceptionWhenPageSizeExceedsLimit() {
            Pageable pageable = PageRequest.of(0, 101);

            assertThatThrownBy(() -> deviceService.getAll(pageable))
                    .isInstanceOf(PageSizeExceededException.class)
                    .hasMessageContaining("Requested page size 101")
                    .hasMessageContaining("exceeds the maximum allowed size of 100");

            verifyNoInteractions(deviceRepository);
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
                    devices.get(0),
                    devices.get(0).toBuilder().id(4L).name("iPhone 14").build(),
                    devices.get(0).toBuilder().id(5L).name("iPhone 13").build()
            );
            Page<Device> page = new PageImpl<>(appleDevices, pageable, appleDevices.size());
            when(deviceRepository.findAllByBrand("Apple", pageable)).thenReturn(page);

            Page<Device> result = deviceService.getAllByBrand("Apple", pageable);

            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent()).allMatch(d -> d.getBrand().equals("Apple"));
            verify(deviceRepository).findAllByBrand("Apple", pageable);
        }

        @Test
        @DisplayName("throws PageSizeExceededException when page size exceeds limit")
        void throwsExceptionWhenPageSizeExceedsLimit() {
            Pageable pageable = PageRequest.of(0, 101);

            assertThatThrownBy(() -> deviceService.getAllByBrand("Apple", pageable))
                    .isInstanceOf(PageSizeExceededException.class);

            verifyNoInteractions(deviceRepository);
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
                    devices.get(0).toBuilder().state(state).build(),
                    devices.get(0).toBuilder().id(4L).name("Galaxy S24").brand("Samsung").state(state).build(),
                    devices.get(0).toBuilder().id(5L).name("Pixel 8").brand("Google").state(state).build()
            );
            Page<Device> page = new PageImpl<>(stateDevices, pageable, stateDevices.size());
            when(deviceRepository.findAllByState(state, pageable)).thenReturn(page);

            Page<Device> result = deviceService.getAllByState(state, pageable);

            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent()).allMatch(d -> d.getState() == state);
            verify(deviceRepository).findAllByState(state, pageable);
        }

        @Test
        @DisplayName("throws PageSizeExceededException when page size exceeds limit")
        void throwsExceptionWhenPageSizeExceedsLimit() {
            Pageable pageable = PageRequest.of(0, 101);

            assertThatThrownBy(() -> deviceService.getAllByState(DeviceState.AVAILABLE, pageable))
                    .isInstanceOf(PageSizeExceededException.class);

            verifyNoInteractions(deviceRepository);
        }
    }
}
