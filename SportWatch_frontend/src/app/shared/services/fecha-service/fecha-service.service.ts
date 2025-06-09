import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class FechaService {

  constructor() { }

  getTimeAgo(createdAt: string): string {
    const now = new Date();
    const created = new Date(createdAt);
    const diffMs = now.getTime() - created.getTime();

    const seconds = Math.floor(diffMs / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);
    const weeks = Math.floor(days / 7);
    const months = Math.floor(days / 30);
    const years = Math.floor(days / 365);

    if (seconds < 60) return 'hace unos segundos';
    if (minutes < 60) return `hace ${minutes} minuto${minutes > 1 ? 's' : ''}`;
    if (hours < 24) return `hace ${hours} hora${hours > 1 ? 's' : ''}`;
    if (days < 7) return `hace ${days} día${days > 1 ? 's' : ''}`;
    if (weeks < 5) return `hace ${weeks} semana${weeks > 1 ? 's' : ''}`;
    if (months < 12) return `hace ${months} mes${months > 1 ? 'es' : ''}`;
    return `hace ${years} año${years > 1 ? 's' : ''}`;
  }

}
