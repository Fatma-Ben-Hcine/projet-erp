import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface ConfirmDialogData {
  title: string;
  message: string;
  confirmText: string;
  cancelText: string;
}

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirm-dialog.component.html',
  styleUrls: ['./confirm-dialog.component.css']
})
export class ConfirmDialogComponent {
  @Input() data: ConfirmDialogData = {
    title: '',
    message: '',
    confirmText: 'Confirmer',
    cancelText: 'Annuler'
  };
  @Input() isVisible = false;
  @Output() confirm = new EventEmitter<boolean>();
  @Output() cancel = new EventEmitter<boolean>();

  onConfirm(): void {
    this.confirm.emit(true);
  }

  onCancel(): void {
    this.cancel.emit(false);
  }
}
