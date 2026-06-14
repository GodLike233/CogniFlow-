package com.cogniflow.repository;

import com.cogniflow.entity.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    List<SystemLog> findTop100ByOrderByCreatedAtDesc();
}
