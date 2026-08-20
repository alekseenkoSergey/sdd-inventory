import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface Notification {
  id: string;
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
  duration: number;
  timestamp: number;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notificationsSubject = new BehaviorSubject<Notification[]>([]);
  notifications$ = this.notificationsSubject.asObservable();

  private notificationCounter = 0;
  private defaultDuration = 4000;

  constructor() {}

  show(message: string, type: 'success' | 'error' | 'warning' | 'info' = 'info', duration: number = this.defaultDuration): string {
    const id = `notification-${this.notificationCounter++}`;
    const notification: Notification = {
      id,
      message,
      type,
      duration,
      timestamp: Date.now()
    };

    const current = this.notificationsSubject.getValue();
    this.notificationsSubject.next([...current, notification]);

    setTimeout(() => {
      this.dismiss(id);
    }, duration);

    return id;
  }

  success(message: string, duration?: number): string {
    return this.show(message, 'success', duration);
  }

  error(message: string, duration?: number): string {
    return this.show(message, 'error', duration);
  }

  warning(message: string, duration?: number): string {
    return this.show(message, 'warning', duration);
  }

  info(message: string, duration?: number): string {
    return this.show(message, 'info', duration);
  }

  dismiss(id: string): void {
    const current = this.notificationsSubject.getValue();
    this.notificationsSubject.next(current.filter(n => n.id !== id));
  }

  dismissAll(): void {
    this.notificationsSubject.next([]);
  }
}
