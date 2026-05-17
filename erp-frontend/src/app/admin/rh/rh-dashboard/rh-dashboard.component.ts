import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { AdminSidebarComponent } from '../../shared/sidebar/sidebar.component';

@Component({
  selector: 'app-rh-dashboard',
  standalone: true,
  imports: [CommonModule, AdminSidebarComponent],
  templateUrl: './rh-dashboard.component.html',
  styleUrls: ['./rh-dashboard.component.css']
})
export class RhDashboardComponent implements OnInit {
  powerBiUrl!: SafeResourceUrl;

  constructor(private sanitizer: DomSanitizer) {}

  ngOnInit(): void {
    this.powerBiUrl = this.sanitizer.bypassSecurityTrustResourceUrl(
      'https://app.powerbi.com/view?r=eyJrIjoiMGUyYjRkYjYtZWFlZS00YzE4LTgxNGEtMmU1Mzk3OWRjZGZhIiwidCI6ImRiZDY2NjRkLTRlYjktNDZlYi05OWQ4LTVjNDNiYTE1M2M2MSIsImMiOjl9&pageName=2da16f82e6c173c00d75'
    );
  }
}
