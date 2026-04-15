export interface Employee {
  id: string;
  firstName: string;
  lastName: string;
  role: 'admin' | 'employee';
  isTemporary: boolean;
  joinDate: string;
  poste: string;
  photo?: string;
}
