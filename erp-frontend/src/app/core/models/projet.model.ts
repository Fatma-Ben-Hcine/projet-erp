export interface ProjetRequest {
  nom: string;
  description: string;
  budget: number;
  dateDebut: string;
  dateLimite: string;
  progression?: number;
  clientId?: number;
  chefDeProjetId?: number;
  employeIds?: number[];
  activites?: ActiviteRequest[];
}

export interface ActiviteRequest {
  nom: string;
  description: string;
}

export interface ProjetResponse {
  id: number;
  nom: string;
  description: string;
  budget: number;
  dateDebut: string;
  dateLimite: string;
  progression: number;
  statut?: string;
  estDepose?: boolean;
  depots?: DepotResponse[];
  client?: {
    id: number;
    nom: string;
    prenom: string;
  };
  chefDeProjet?: {
    id: number;
    nom: string;
    prenom: string;
  };
  employes?: Array<{
    id: number;
    nom: string;
    prenom: string;
    poste?: string;
  }>;
  activites?: ActiviteResponse[];
  joursRestants?: number;
}

export interface DepotResponse {
  id: number;
  type: string;
  lien?: string;
  nomFichier?: string;
  cheminFichier?: string;
  dateDepot?: string;
}

export interface ActiviteResponse {
  id: number;
  nom: string;
  description: string;
  projetId: number;
}

export enum StatutProjet {
  NOUVEAU = 'NOUVEAU',
  EN_COURS = 'EN_COURS', 
  TERMINE = 'TERMINE',
  EN_RETARD = 'EN_RETARD'
}

export interface ClientResponse {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  numeroTelephone?: string;
  matriculeFiscale?: string;
}

export interface EmployeResponse {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  poste: string;
  CIN: string;
  numeroTel: string;
  competences: string;
  typeUtilisateur: string;
  actif: boolean;
}
