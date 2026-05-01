-- Migration pour mettre à jour la structure des tables selon le diagramme de classes

-- Mise à jour de la table ressources
ALTER TABLE ressources 
DROP COLUMN IF EXISTS type,
DROP COLUMN IF EXISTS date_creation,
DROP COLUMN IF EXISTS projet_id;

-- Ajout des colonnes manquantes dans la table ressources
ALTER TABLE ressources 
ADD COLUMN IF NOT EXISTS prix DECIMAL(10,2),
ADD COLUMN IF NOT EXISTS date_debut DATE,
ADD COLUMN IF NOT EXISTS date_fin DATE;

-- Mise à jour des colonnes existantes avec valeurs par défaut
ALTER TABLE ressources 
MODIFY COLUMN nom VARCHAR(255) NOT NULL,
MODIFY COLUMN situation ENUM('DISPONIBLE', 'DEMANDE') DEFAULT 'DISPONIBLE',
MODIFY COLUMN statut ENUM('ACTIVE', 'NON_ACTIVE') DEFAULT 'ACTIVE';

-- S'assurer que toutes les ressources ont un statut par défaut
UPDATE ressources 
SET statut = 'ACTIVE' 
WHERE statut IS NULL;

-- S'assurer que toutes les ressources ont une situation par défaut
UPDATE ressources 
SET situation = 'DISPONIBLE' 
WHERE situation IS NULL;

-- Recréation de la table demande_ressource avec la bonne structure
DROP TABLE IF EXISTS demandes_ressources;

CREATE TABLE demande_ressource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employe_id BIGINT NOT NULL,
    ressource_id BIGINT NOT NULL,
    date_demande DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    statut_demande ENUM('EN_ATTENTE','APPROUVEE','ANNULEE') DEFAULT 'EN_ATTENTE',
    FOREIGN KEY (employe_id) REFERENCES utilisateur(id),
    FOREIGN KEY (ressource_id) REFERENCES ressources(id),
    INDEX idx_demande_ressource_employe (employe_id),
    INDEX idx_demande_ressource_ressource (ressource_id)
);

-- Nettoyage des données existantes si nécessaire
UPDATE ressources 
SET situation = 'DISPONIBLE', 
    statut = 'ACTIVE',
    employe_demandeur_id = NULL,
    date_demande = NULL
WHERE situation IS NULL OR statut IS NULL;
