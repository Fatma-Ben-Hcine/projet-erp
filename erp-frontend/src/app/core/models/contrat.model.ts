export interface ContratRequest {
  dateDebut: string;
  dateFin: string;
  montant: number;
  statut: string;
  clientId: number;
}

export interface ContratResponse {
  id: number;
  dateDebut: string;
  dateFin: string;
  montant: number;
  statut: string;
  clientId: number;
  clientNom: string;
  clientPrenom: string;
  clientEmail: string;
}

export const STATUTS_CONTRAT = [
  'EN_COURS',
  'TERMINE',
  'SUSPENDU',
  'EN_ATTENTE'
];
