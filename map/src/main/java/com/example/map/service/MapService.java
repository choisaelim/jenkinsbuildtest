package com.example.map.service;

import com.example.map.jpa.MapEntity;

public interface MapService {
    Iterable<MapEntity> getMapbyUserId(String userId);
}
