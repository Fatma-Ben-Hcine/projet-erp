export interface Ressource {
  id: number;
  nom: string;
  description?: string;
  prix: number;
  statut: 'ACTIVE' | 'NON_ACTIVE';
  dateDebutAbonnement?: string | null;
  dateFinAbonnement?: string | null;
  abonnementExpire?: boolean;
  nombreDemandes: number;
}

export interface RessourceRequest {
  nom: string;
  description?: string;
  prix: number;
  statut: 'ACTIVE' | 'NON_ACTIVE';
  dateDebutAbonnement?: string | null;
  dateFinAbonnement?: string | null;
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
  dateDebutAbonnement?: string | null;
  dateFinAbonnement?: string | null;
  nombreDemandes: number;
  dejaDemandeParMoi: boolean;
}
