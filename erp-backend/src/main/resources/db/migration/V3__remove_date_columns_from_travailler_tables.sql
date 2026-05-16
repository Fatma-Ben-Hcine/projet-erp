-- Suppression des colonnes de dates redondantes
-- Les dates sont déjà présentes dans les tables activités et taches

ALTER TABLE travailler_activite DROP COLUMN date_debut;
ALTER TABLE travailler_activite DROP COLUMN date_fin;

ALTER TABLE travailler_tache DROP COLUMN date_debut;
ALTER TABLE travailler_tache DROP COLUMN date_fin_reelle;
