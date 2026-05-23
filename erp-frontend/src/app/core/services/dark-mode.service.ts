import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DarkModeService {
  private readonly storageKey = 'darkMode';
  private readonly darkModeSubject = new BehaviorSubject<boolean>(this.readPreference());

  public readonly isDarkMode$ = this.darkModeSubject.asObservable();

  constructor() {
    this.applyClass(this.darkModeSubject.value);
  }

  initialize(): void {
    this.applyClass(this.darkModeSubject.value);
  }

  toggle(): void {
    this.set(!this.isDarkMode);
  }

  set(value: boolean): void {
    localStorage.setItem(this.storageKey, String(value));
    this.darkModeSubject.next(value);
    this.applyClass(value);
  }

  get isDarkMode(): boolean {
    return this.darkModeSubject.value;
  }

  private readPreference(): boolean {
    return localStorage.getItem(this.storageKey) === 'true';
  }

  private applyClass(enabled: boolean): void {
    document.body.classList.toggle('dark', enabled);
    document.documentElement.classList.toggle('dark', enabled);
  }
}
