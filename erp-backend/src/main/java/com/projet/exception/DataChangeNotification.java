package com.projet.exception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @DataChangeNotification
 * 
 * Annotation pour marquer les méthodes qui modifient les données critiques du dashboard
 * 
 * Utilisation: Ajouter au-dessus des méthodes qui créent/modifient/suppriment des données
 * que Power BI affiche
 * 
 * Exemple:
 * 
 * @DataChangeNotification("dashboard_main")
 * public ProjetResponse createProjet(ProjetRequest request) {
 *     Projet projet = new Projet();
 *     // ... logique de création ...
 *     return toResponse(projet);
 * }
 * 
 * Cela va déclencher automatiquement:
 * 1. Mise à jour de la version dans DataVersionService
 * 2. Notification WebSocket aux clients Angular
 * 3. Les clients vont rafraîchir le dashboard Power BI
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataChangeNotification {
    /**
     * Nom du dataset qui a changé
     * Exemples: "dashboard_main", "projects", "resources", "employees"
     */
    String value();
}
