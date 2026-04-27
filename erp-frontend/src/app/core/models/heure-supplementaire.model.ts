export enum StatutHeureSupplementaire {
  EN_ATTENTE = 'EN_ATTENTE',
  APPROUVEE = 'APPROUVEE',
  REFUSEE = 'REFUSEE'
}

export interface HeureSupplementaire {
  id?: number;
  date: string;
  nombreHeures: number;
  mission: string;
  statut: StatutHeureSupplementaire;
  tarifHeuresSupp: number;
  employeId?: number;
  employeNom?: string;
  employePrenom?: string;
  employeNomComplet?: string;
}

export interface HeureSupplementaireRequest {
  date: string;
  nombreHeures: number;
  mission: string;
  statut?: StatutHeureSupplementaire;
  tarifHeuresSupp: number;
  employeId: number;
}
