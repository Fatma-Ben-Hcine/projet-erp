-- Suppression de la colonne progression de la table travailler_activite
-- La progression sera calculée dynamiquement dans le DTO

ALTER TABLE travailler_activite DROP COLUMN IF EXISTS progression;
