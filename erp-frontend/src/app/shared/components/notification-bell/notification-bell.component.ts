import {
  Component,
  OnInit,
  OnDestroy,
  HostListener,
  ElementRef,
  ViewChild,
  Renderer2,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthService } from '../../../auth/auth.service';
import { Notification } from '../../../core/models/notification.model';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification-bell.component.html',
  styleUrls: ['./notification-bell.component.css'],
})
export class NotificationBellComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];
  countNonLues = 0;
  isOpen = false;
  dropdownStyles: any = {};

  @ViewChild('dropdown') dropdownRef!: ElementRef;
  @ViewChild('notifBtn') notifBtnRef!: ElementRef;

  private subscriptions: Subscription[] = [];
  private pollingInterval: any;

  constructor(
    private notificationService: NotificationService,
    private authService: AuthService,
    private elementRef: ElementRef,
    private renderer: Renderer2
  ) {}

  ngOnInit(): void {
    const currentUser = this.authService.getCurrentUser();
    console.log('🔔 NotificationBell - User:', currentUser);
    if (currentUser && currentUser.id) {
      // Connecter WebSocket
      this.notificationService.connectWebSocket(currentUser.id);

      // S'abonner aux notifications
      this.subscriptions.push(
        this.notificationService.notifications$.subscribe((notifs) => {
          console.log('🔔 NotificationBell - Notifications reçues:', notifs.length, notifs);
          this.notifications = notifs;
        })
      );

      // S'abonner au compteur
      this.subscriptions.push(
        this.notificationService.countNonLues$.subscribe((count) => {
          console.log('🔔 NotificationBell - Compteur:', count);
          this.countNonLues = count;
        })
      );

      // Charger les notifications initiales
      console.log('🔔 NotificationBell - Chargement initial...');
      this.notificationService.loadNotifications();

      // Polling toutes les 30 secondes pour le compteur (fallback)
      this.pollingInterval = setInterval(() => {
        this.notificationService.loadCountNonLues();
      }, 30000);
    } else {
      console.log('🔔 NotificationBell - Pas d\'utilisateur connecté');
    }
  }

  ngOnDestroy(): void {
    // Déconnecter WebSocket
    this.notificationService.disconnectWebSocket();

    // Nettoyer les subscriptions
    this.subscriptions.forEach((sub) => sub.unsubscribe());

    // Nettoyer le polling
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval);
    }
  }

  toggleDropdown(): void {
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
      this.notificationService.loadNotifications();
      // Calculer la position après le rendu
      setTimeout(() => this.calculatePosition(), 0);
    }
  }

  private calculatePosition(): void {
    const btn = this.notifBtnRef?.nativeElement || this.elementRef.nativeElement.querySelector('.notif-btn');
    if (!btn) return;

    const rect = btn.getBoundingClientRect();
    const dropdownWidth = 350;
    const dropdownHeight = 400;

    // Position en dessous du bouton (pour header en haut)
    let left = rect.left + rect.width / 2 - dropdownWidth / 2;
    let top = rect.bottom + 10;

    // Vérifier si ça dépasse à droite de l'écran
    if (left + dropdownWidth > window.innerWidth) {
      left = window.innerWidth - dropdownWidth - 20;
    }

    // Vérifier si ça dépasse à gauche
    if (left < 10) {
      left = 10;
    }

    // Vérifier si ça dépasse en bas
    if (top + dropdownHeight > window.innerHeight) {
      top = rect.top - dropdownHeight - 10;
    }

    this.dropdownStyles = {
      position: 'fixed',
      left: `${left}px`,
      top: `${top}px`,
      'z-index': '2147483647'
    };

    // Appliquer les styles au dropdown
    if (this.dropdownRef?.nativeElement) {
      Object.assign(this.dropdownRef.nativeElement.style, this.dropdownStyles);
    }
  }

  onNotifClick(notif: Notification): void {
    if (!notif.estLue) {
      this.notificationService.marquerLue(notif.id).subscribe(() => {
        notif.estLue = true;
        this.countNonLues = Math.max(0, this.countNonLues - 1);
      });
    }
  }

  marquerToutesLues(): void {
    this.notificationService.marquerToutesLues().subscribe(() => {
      this.notifications.forEach((n) => (n.estLue = true));
      this.countNonLues = 0;
    });
  }

  getNotifIcon(type: string): string {
    return type === 'PROJET_ASSIGNE' ? '📋' : '⚠️';
  }

  /**
   * Fermer le dropdown si clic en dehors
   */
  @HostListener('document:click', ['$event'])
  onClickOutside(event: Event): void {
    if (this.isOpen && !this.elementRef.nativeElement.contains(event.target)) {
      this.isOpen = false;
    }
  }
}
