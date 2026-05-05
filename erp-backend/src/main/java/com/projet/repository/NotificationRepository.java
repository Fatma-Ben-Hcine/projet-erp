package com.projet.repository;

import com.projet.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByDestinataireIdOrderByDateCreationDesc(Long employeId);

    long countByDestinataireIdAndEstLueFalse(Long employeId);

    List<Notification> findByDestinataireIdAndEstLueFalse(Long employeId);
}
