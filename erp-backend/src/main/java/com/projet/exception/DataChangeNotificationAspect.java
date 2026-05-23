package com.projet.exception;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import com.projet.service.DataVersionService;

/**
 * DataChangeNotificationAspect
 * 
 * Aspect qui intercepte les modifications de données critiques
 * et déclenche automatiquement une notification de refresh
 * 
 * Annote vos services métier avec @DataChangeNotification("dataset_name")
 * pour activer le refresh automatique du dashboard
 * 
 * Exemple:
 * @DataChangeNotification("dashboard_main")
 * public Projet createProjet(ProjetRequest request) { ... }
 */
@Aspect
@Component
@Slf4j
public class DataChangeNotificationAspect {

    private final DataVersionService dataVersionService;
    private final DataChangeNotificationService notificationService;

    public DataChangeNotificationAspect(
            DataVersionService dataVersionService,
            DataChangeNotificationService notificationService) {
        this.dataVersionService = dataVersionService;
        this.notificationService = notificationService;
    }

    /**
     * Intercepte les méthodes annotées @DataChangeNotification
     * Vérifie si la méthode a réussi, puis déclenche un refresh
     */
    @Around("@annotation(com.projet.exception.DataChangeNotification) && args(..)")
    public Object notifyDataChange(ProceedingJoinPoint joinPoint) throws Throwable {
        String datasetName = null;
        
        try {
            // Récupérer l'annotation
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            DataChangeNotification annotation = 
                    signature.getMethod()
                    .getAnnotation(DataChangeNotification.class);
            
            if (annotation != null) {
                datasetName = annotation.value();
            }

            // Exécuter la méthode
            Object result = joinPoint.proceed();

            // Si succès, notifier les clients
            if (datasetName != null) {
                log.info("Notification de changement pour: {}", datasetName);
                notificationService.notifyDataChange(datasetName);
            }

            return result;

        } catch (Exception e) {
            log.error("Erreur lors de la notification: {}", e.getMessage());
            throw e;
        }
    }
}
