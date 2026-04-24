import { Component, Input, Output, EventEmitter, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ProjetResponse, DepotResponse } from '../../../core/models/projet.model';
import { ProjetService } from '../../../core/services/projet.service';

@Component({
  selector: 'app-depot-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './depot-modal.component.html',
  styleUrls: ['./depot-modal.component.scss']
})
export class DepotModalComponent {
  @Input() isVisible: boolean = false;
  @Input() projet: ProjetResponse | null = null;
  @Input() mode: 'create' | 'view' = 'create';
  @Input() depotsExistant: DepotResponse[] = [];
  @Output() closed = new EventEmitter<void>();
  @Output() depotSubmitted = new EventEmitter<{type: 'lien' | 'fichier', value: string | File}>();

  depotForm!: FormGroup;
  selectedTab: 'lien' | 'fichier' = 'lien';
  selectedFile: File | null = null;
  errorMessage: string = '';

  constructor(
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef,
    private projetService: ProjetService
  ) {}

  ngOnInit(): void {
    this.initForm();
  }

  ngOnChanges(): void {
    if (this.mode === 'view' && this.depotsExistant && this.depotsExistant.length > 0) {
      // Pré-remplir avec le dernier dépôt
      const dernierDepot = this.depotsExistant[this.depotsExistant.length - 1];
      if (dernierDepot.type === 'lien' && dernierDepot.lien) {
        this.depotForm.get('lien')?.setValue(dernierDepot.lien);
        this.selectedTab = 'lien';
      } else if (dernierDepot.type === 'fichier' && dernierDepot.nomFichier) {
        this.selectedTab = 'fichier';
      }
    }
  }

  private initForm(): void {
    this.depotForm = this.fb.group({
      lien: ['', [Validators.pattern(/^https?:\/\/.+/)]],
      fichier: [null]
    });
  }

  onTabChange(tab: 'lien' | 'fichier'): void {
    this.selectedTab = tab;
    this.errorMessage = '';
    // Ne pas réinitialiser les données lors du changement d'onglet
    // Conserver l'état des liens et fichiers
    this.cdr.detectChanges();
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      this.depotForm.get('fichier')?.setValue(file);
      this.errorMessage = '';
      this.cdr.detectChanges();
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    
    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.selectedFile = files[0];
      this.depotForm.get('fichier')?.setValue(files[0]);
      this.errorMessage = '';
      this.cdr.detectChanges();
    }
  }

  onSubmit(): void {
    console.log('onSubmit appelé');
    this.errorMessage = '';
    
    if (this.selectedTab === 'lien') {
      const lien = this.depotForm.get('lien')?.value?.trim();
      if (!lien) {
        this.errorMessage = 'Veuillez entrer un lien valide';
        return;
      }
      this.depotSubmitted.emit({ type: 'lien', value: lien });
    } else {
      if (!this.selectedFile) {
        this.errorMessage = 'Veuillez sélectionner un fichier';
        return;
      }
      this.depotSubmitted.emit({ type: 'fichier', value: this.selectedFile });
    }
  }

  close(): void {
    this.closed.emit();
    this.resetForm();
  }

  private resetForm(): void {
    this.selectedTab = 'lien';
    this.selectedFile = null;
    this.errorMessage = '';
    this.depotForm.reset();
  }

  // Helper pour vérifier si un fichier est valide
  private isValidFileType(file: File): boolean {
    const validTypes = ['application/pdf', 'application/zip', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'image/png', 'image/jpeg'];
    return validTypes.includes(file.type);
  }

  // Helper pour déclencher le sélecteur de fichier
  triggerFileInput(): void {
    const input = document.getElementById('fileInput') as HTMLInputElement;
    input?.click();
  }

  // Helper pour formater la taille du fichier
  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
  }

  // Télécharger un fichier de dépôt
  downloadFile(depotId: number, filename: string): void {
    this.projetService.downloadDepotFile(depotId, filename).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        console.error('Erreur lors du téléchargement du fichier:', err);
      }
    });
  }

  // Getter pour vérifier si le formulaire est valide (lien OU fichier)
  get isFormValid(): boolean {
    const lienValue = this.depotForm.get('lien')?.value?.trim();
    const lienValid = lienValue && /^https?:\/\//.test(lienValue);
    const fichierValid = this.selectedFile !== null;
    return lienValid || fichierValid;
  }

  // Méthode pour supprimer le fichier sélectionné
  removeFile(): void {
    this.selectedFile = null;
    this.depotForm.get('fichier')?.setValue(null);
    const input = document.getElementById('fileInput') as HTMLInputElement;
    if (input) {
      input.value = '';
    }
    this.cdr.detectChanges();
  }
}
