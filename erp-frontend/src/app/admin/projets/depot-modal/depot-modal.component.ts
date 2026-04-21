import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ProjetResponse } from '../../../core/models/projet.model';

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
  @Output() closed = new EventEmitter<void>();
  @Output() depotSubmitted = new EventEmitter<{type: 'lien' | 'fichier', value: string | File}>();

  depotForm!: FormGroup;
  selectedTab: 'lien' | 'fichier' = 'lien';
  selectedFile: File | null = null;
  errorMessage: string = '';

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(): void {
    this.depotForm = this.fb.group({
      lien: ['', [Validators.required, Validators.pattern(/^https?:\/\/.+/)]],
      fichier: [null]
    });
  }

  onTabChange(tab: 'lien' | 'fichier'): void {
    this.selectedTab = tab;
    this.errorMessage = '';
    // Reset validation when switching tabs
    if (tab === 'lien') {
      this.depotForm.get('fichier')?.setValue(null);
      this.depotForm.get('lien')?.setValidators([Validators.required, Validators.pattern(/^https?:\/\/.+/)]);
    } else {
      this.depotForm.get('lien')?.setValue('');
      this.depotForm.get('lien')?.setValidators([]);
    }
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      this.depotForm.get('fichier')?.setValue(file);
      this.errorMessage = '';
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
    }
  }

  onSubmit(): void {
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
    const f = (bytes / Math.pow(k, i)).toFixed(2);
    return parseFloat(f) + ' ' + sizes[i];
  }
}
