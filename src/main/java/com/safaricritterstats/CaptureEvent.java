package com.safaricritterstats;

public record CaptureEvent(String critter, long shards) {
    public CaptureEvent {
        if (critter == null || critter.isBlank()) throw new IllegalArgumentException("critter");
        if (shards < 1) throw new IllegalArgumentException("shards");
    }
}
