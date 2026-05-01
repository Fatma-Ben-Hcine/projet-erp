export interface Ressource {
  id: number;
  nom: string;
  description?: string;
  prix: number;
  statut: 'ACTIVE' | 'NON_ACTIVE';
  situation: 'DISPONIBLE' | 'DEMANDE' | 'NON_DEMANDE';
  dateDebutAbonnement?: string | null;
  dateFinAbonnement?: string | null;
  estAbonne?: boolean;
  abonnementExpire?: boolean;
  statutForceManuel?: boolean;
  nombreDemandes?: number;
  // Anciens champs pour compatibilité
  dateDebut?: string;
  dateFin?: string;
  employeDemandeurNom?: string;
  dateDemande?: string;
  dejaDemandeParMoi?: boolean;
}

export interface RessourceRequest {
  nom: string;
  description?: string;
  prix: number;
  statut: 'ACTIVE' | 'NON_ACTIVE';
  dateDebutAbonnement?: string | null;
  dateFinAbonnement?: string | null;
  statutForceManuel?: boolean;
}

export interface DemandeRessource {
  id: number;
  dateDemande: string;
  statutDemande: 'EN_ATTENTE' | 'APPROUVEE' | 'ANNULEE';
  ressource: RessourceInfo;
  employe: EmployeInfo;
}

export interface RessourceInfo {
  id: number;
  nom: string;
  description?: string;
  prix?: number;
  dateDebut?: string;
  dateFin?: string;
}

export interface EmployeInfo {
  id: number;
  nom: string;
  prenom: string;
}

export interface DemandeRessourceRequest {
  ressourceId: number;
}

export interface DemandeMultipleRequest {
  ressourceIds: number[];
}

export interface DemandeRessourceResponse {
  id: number;
  dateDemande: string;
  statutDemande: 'EN_ATTENTE' | 'APPROUVEE' | 'ANNULEE';
  ressource: RessourceInfo;
  employe: EmployeInfo;
}

export interface RessourceDisponible {
  id: number;
  nom: string;
  description?: string;
  prix: number;
  statut: 'ACTIVE' | 'NON_ACTIVE';
  situation: 'DISPONIBLE' | 'DEMANDE' | 'NON_DEMANDE'; // Types mis à jour pour compatibilité backend
  dateDebutAbonnement?: string | null;
  dateFinAbonnement?: string | null;
  estAbonne: boolean;
  dejaDemandeParMoi: boolean;
  nombreDemandes: number;
  // Anciens champs pour compatibilité
  dateDebut?: string;
  dateFin?: string;
  employeDemandeur?: string;
  dateDemande?: string;
}
