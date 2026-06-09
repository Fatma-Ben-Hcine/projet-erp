export enum TypeConge {
  MALADIE = 'MALADIE',
  ANNUEL = 'ANNUEL',
  MATERNITE = 'MATERNITE',
  PATERNITE = 'PATERNITE',
  SANS_SOLDE = 'SANS_SOLDE',
  FORMATION = 'FORMATION',
  DECES = 'DECES',
  MARIAGE = 'MARIAGE'
}

export enum StatutConge {
  EN_ATTENTE = 'EN_ATTENTE',
  VALIDE = 'VALIDE',
  REFUSE = 'REFUSE'
}

export interface Conge {
  id?: number;
  typeConge: TypeConge;
  dateDebut: string;
  dateFin: string;
  statut: StatutConge;
  employeId?: number;
  employeNom?: string;
  employePrenom?: string;
  employeNomComplet?: string;
}

export interface CongeRequest {
  typeConge: TypeConge;
  dateDebut: string;
  dateFin: string;
}
