-- Suppression de la colonne projet_nom de la table notifications
-- Le nom du projet est maintenant récupéré dynamiquement via projetId

ALTER TABLE notifications DROP COLUMN projet_nom;
