package com.koval.devicemanager.infra.converter;

import com.koval.devicemanager.domain.exception.InvalidDeviceStateException;
import com.koval.devicemanager.domain.model.DeviceState;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToDeviceStateConverter implements Converter<String, DeviceState> {

    @Override
    public DeviceState convert(String source) {
        try {
            return DeviceState.valueOf(source.toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            throw new InvalidDeviceStateException(source);
        }
    }
}
