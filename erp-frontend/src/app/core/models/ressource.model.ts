export interface Ressource {
  id: number;
  nom: string;
  description?: string;
  type?: string;
  statut: 'ACTIVE' | 'NON_ACTIVE';
  situation: 'DISPONIBLE' | 'DEMANDE';
  employeDemandeur?: EmployeInfo;
  dateDemande?: string;
  dateCreation: string;
}

export interface RessourceRequest {
  nom: string;
  description?: string;
  type?: string;
}


export interface DemandeRessource {
  id: number;
  dateDemande: string;
  estTraitee: boolean;
  ressource: RessourceInfo;
  employe: EmployeInfo;
}

export interface RessourceInfo {
  id: number;
  nom: string;
  description?: string;
  prix: number;
}

export interface EmployeInfo {
  id: number;
  nom: string;
  prenom: string;
}

export interface DemandeRessourceRequest {
  ressourceId: number;
}

export interface DemandeRessourcesRequest {
  ressourceIds: number[];
}
