export interface Notification {
  id: number;
  message: string;
  type: 'PROJET_ASSIGNE' | 'DATE_LIMITE_PROCHE';
  projetId: number;
  projetNom: string;
  estLue: boolean;
  dateCreation: string;
}
