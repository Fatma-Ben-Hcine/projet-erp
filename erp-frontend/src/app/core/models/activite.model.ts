export interface ActiviteRequest {
  nom: string;
  description: string;
  dateDebut?: string;
  dateFin?: string;
  projetId: number;
  estDeposé?: boolean;
  employeIds?: number[];
}

export interface ActiviteResponse {
  id: number;
  nom: string;
  description: string;
  dateDebut: string;
  dateFin: string | null;
  projet: {
    id: number;
    nom: string;
  };
  taches: TacheResponse[];
  employeActivites: EmployeActiviteResponse[];
  progressionMoyenne: number;
  nombreEmployesAssignes: number;
  estDepose: boolean;
  depots: any[];
}

export interface TacheRequest {
  nom: string;
  description: string;
  dateDebut?: string;
  dateFin?: string | null;
  activiteId: number;
  estDeposé?: boolean;
  employeIds?: number[];
}

export interface TacheResponse {
  id: number;
  nom: string;
  description: string;
  dateDebut: string;
  dateFin: string | null;
  activite: {
    id: number;
    nom: string;
    description: string;
  };
  employeTaches: EmployeTacheResponse[];
  progression: number;
  nombreEmployesAssignes: number;
  nombreEmployesTermines: number;
  estDepose: boolean;
  depots: any[];
}

export interface EmployeActiviteResponse {
  id: number;
  employeId: number;
  employeNom: string;
  employePrenom: string;
  progression: number;
}

export interface EmployeTacheResponse {
  id: number;
  employeId: number;
  employeNom: string;
  employePrenom: string;
}

export interface AssignEmployeToActiviteRequest {
  statut?: string;
  // progression supprimée - calculée dynamiquement par le backend
}

export interface AssignEmployeToTacheRequest {
  statut?: string;
}

export interface ActiviteProgressionResponse {
  progressionMoyenne: number;
  nombreEmployesAssignes: number;
  details: EmployeActiviteResponse[];
}

export interface TacheProgressionResponse {
  progression: number;
  nombreEmployesAssignes: number;
  nombreEmployesTermines: number;
  details: EmployeTacheResponse[];
}

export enum StatutActivite {
  NOUVEAU = 'NOUVEAU',
  EN_COURS = 'EN_COURS',
  EN_PAUSE = 'EN_PAUSE',
  TERMINE = 'TERMINE',
  ANNULE = 'ANNULE'
}

export enum StatutTache {
  NOUVEAU = 'NOUVEAU',
  EN_COURS = 'EN_COURS',
  EN_PAUSE = 'EN_PAUSE',
  TERMINE = 'TERMINE',
  ANNULE = 'ANNULE',
  BLOQUE = 'BLOQUE'
}
