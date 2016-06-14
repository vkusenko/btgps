package com.vkusenko.btgpslogger.events;

import com.vkusenko.btgpslogger.util.gps.MockLocationProvider;

public class MockProviderEvent {
    public final MockLocationProvider mockLocationProvider;

    public MockProviderEvent(MockLocationProvider mockLocationProvider) {
        this.mockLocationProvider = mockLocationProvider;
    }
}
