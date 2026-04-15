-- Migration pour ajouter la colonne statut à la table projets
-- Ajout de la colonne statut avec la valeur par défaut 'NOUVEAU'

ALTER TABLE projets ADD COLUMN statut VARCHAR(20) NOT NULL DEFAULT 'NOUVEAU';

-- Mise à jour des projets existants pour s'assurer qu'ils ont tous un statut
UPDATE projets SET statut = 'NOUVEAU' WHERE statut IS NULL;
