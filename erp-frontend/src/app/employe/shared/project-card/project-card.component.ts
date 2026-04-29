import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Project } from '../../../core/models/project.model';

@Component({
  selector: 'app-project-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './project-card.component.html',
  styleUrls: ['./project-card.component.css']
})
export class ProjectCardComponent {
  @Input() project!: Project;
  @Input() currentUserId: string = '';
  @Output() viewDetails = new EventEmitter<string>();
  @Output() deposit = new EventEmitter<string>();
  @Output() start = new EventEmitter<string>();

  get isProjectManager(): boolean {
    return this.currentUserId === this.project.projectManager;
  }

  get daysRemaining(): number {
    const deadline = new Date(this.project.deadline);
    const today = new Date();
    deadline.setHours(0, 0, 0, 0);
    today.setHours(0, 0, 0, 0);
    return Math.ceil((deadline.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
  }

  get borderColor(): string {
    switch (this.project.status) {
      case 'new': return 'border-blue';
      case 'in-progress': return 'border-yellow';
      case 'completed': return 'border-green';
      case 'late': return 'border-red';
      default: return 'border-gray';
    }
  }

  onDepositClick(): void {
    this.deposit.emit(this.project.id);
  }

  onViewDetailsClick(): void {
    this.viewDetails.emit(this.project.id);
  }
}
