package com.example.map.service;

import org.springframework.stereotype.Service;

import com.example.map.jpa.MapEntity;
import com.example.map.jpa.MapRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MapServiceImpl implements MapService {
    private final MapRepository repository;

    @Override
    public Iterable<MapEntity> getMapbyUserId(String userId) {
        return repository.findByUserId(userId);
    }

}
