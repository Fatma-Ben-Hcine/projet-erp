export interface ActiviteRequest {
  nom: string;
  description: string;
  dateDebut?: string;
  dateFin?: string;
  projetId: number;
  estDeposé?: boolean;
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
}

export interface TacheRequest {
  nom: string;
  description: string;
  dateDebut?: string;
  dateFin?: string;
  activiteId: number;
  estDeposé?: boolean;
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
}

export interface EmployeActiviteResponse {
  id: number;
  employe: {
    id: number;
    nom: string;
    prenom: string;
    poste?: string;
  };
  activite: {
    id: number;
    nom: string;
  };
  statut: string;
  progression: number;
  dateDebut: string;
  dateFin?: string;
}

export interface EmployeTacheResponse {
  id: number;
  employe: {
    id: number;
    nom: string;
    prenom: string;
    poste?: string;
  };
  tache: {
    id: number;
    nom: string;
  };
  statut: string;
  dateDebut: string;
  dateFin?: string;
}

export interface AssignEmployeToActiviteRequest {
  statut?: string;
  progression?: number;
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
